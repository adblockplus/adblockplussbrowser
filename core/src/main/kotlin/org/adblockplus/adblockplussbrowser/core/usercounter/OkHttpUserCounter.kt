/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.adblockplussbrowser.core.usercounter

import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.AppInfo
import org.adblockplus.adblockplussbrowser.core.BuildConfig
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.extensions.currentData
import org.adblockplus.adblockplussbrowser.core.extensions.currentSettings
import org.adblockplus.adblockplussbrowser.core.extensions.sanatizeUrl
import org.adblockplus.adblockplussbrowser.core.retryIO
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.time.ExperimentalTime


@ExperimentalTime
internal class OkHttpUserCounter(
    private val okHttpClient: OkHttpClient,
    private val repository: CoreRepository,
    private val settings: SettingsRepository,
    private val appInfo: AppInfo,
    private val analyticsProvider: AnalyticsProvider
) : UserCounter {

    override suspend fun count() : CountUserResult = coroutineScope {
        try {
            val savedLastUserCountingResponse = repository.currentData().lastUserCountingResponse
            Timber.d("User count lastUserCountingResponse saved is `%d`",
                savedLastUserCountingResponse)
            val acceptableAdsEnabled = settings.currentSettings().acceptableAdsEnabled
            val acceptableAdsSubscription = settings.getAcceptableAdsSubscription()
            val url = createUrl(acceptableAdsSubscription, acceptableAdsEnabled,
                savedLastUserCountingResponse)
            val request = Request.Builder().url(url).head().build()
            val response = retryIO(description = "User counting HEAD request") {
                okHttpClient.newCall(request).await()
            }

            val result = when (response.code) {
                200 -> {
                    Timber.d("User count response date: %s", response.headers["Date"])
                    lastUserCountingResponseFormat.timeZone = serverTimeZone
                    val newLastVersion = try {
                        // Expected date format in "Date" header: "Thu, 23 Sep 2021 17:31:01 GMT"
                        lastUserCountingResponseFormat.format(
                            serverDateParser.parse(response.headers["Date"]))
                    } catch (ex: ParseException) {
                        Timber.e(ex)
                        analyticsProvider.logEvent(AnalyticsEvent.HEAD_RESPONSE_DATA_PARSING_FAILED)
                        if (BuildConfig.DEBUG) {
                            throw ex
                        } else {
                            Timber.e("Parsing 'Date' from header failed, using client GMT time")
                            lastUserCountingResponseFormat.format(Calendar.getInstance().time)
                        }
                    }
                    Timber.d("User count saves new lastUserCountingResponse `%s`",
                        newLastVersion)
                    repository.updateLastUserCountingResponse(newLastVersion.toLong())
                    CountUserResult.Success()
                }
                else -> {
                    CountUserResult.Failed()
                }
            }
            response.close()
            result
        } catch (ex: Exception) {
            Timber.e("User count request failed")
            Timber.e(ex)
            if (BuildConfig.DEBUG && ex is ParseException) {
                throw ex
            }
            CountUserResult.Failed()
        }
    }

    private fun createUrl(subscription: Subscription,
                          acceptableAdsEnabled: Boolean,
                          savedLastUserCountingResponse: Long
    ): HttpUrl {
        return subscription.url.sanatizeUrl().toHttpUrl().newBuilder().apply {
            addQueryParameter("addonName", appInfo.addonName)
            addQueryParameter("addonVersion", appInfo.addonVersion)
            addQueryParameter("application", appInfo.application)
            addQueryParameter("applicationVersion", appInfo.applicationVersion)
            addQueryParameter("platform", appInfo.platform)
            addQueryParameter("platformVersion", appInfo.platformVersion)
            addQueryParameter("disabled", (!acceptableAdsEnabled).toString())
            addQueryParameter("lastVersion", savedLastUserCountingResponse.toString())
        }.build()
    }

    companion object {
        private val lastUserCountingResponseFormat = SimpleDateFormat("yyyyMMddHHmm",
            Locale.ENGLISH)
        private val serverDateParser = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
            Locale.ENGLISH)
        private val serverTimeZone = TimeZone.getTimeZone("GMT")
    }
}
