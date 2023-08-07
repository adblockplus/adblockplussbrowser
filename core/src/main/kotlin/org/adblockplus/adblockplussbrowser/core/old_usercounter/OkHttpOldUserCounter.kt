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

package org.adblockplus.adblockplussbrowser.core.old_usercounter

import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.AppInfo
import org.adblockplus.adblockplussbrowser.core.BuildConfig
import org.adblockplus.adblockplussbrowser.core.CallingApp
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.extensions.currentData
import org.adblockplus.adblockplussbrowser.core.extensions.currentSettings
import org.adblockplus.adblockplussbrowser.core.extensions.sanitizeUrl
import org.adblockplus.adblockplussbrowser.core.retryIO
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_OK
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.time.ExperimentalTime
import org.adblockplus.adblockplussbrowser.base.data.HttpConstants


@ExperimentalTime
internal class OkHttpOldUserCounter(
    private val okHttpClient: OkHttpClient,
    private val repository: CoreRepository,
    private val settings: SettingsRepository,
    private val appInfo: AppInfo,
    private val analyticsProvider: AnalyticsProvider
) : OldUserCounter {

    @Suppress("TooGenericExceptionCaught", "LongMethod")
    override suspend fun count(callingApp: CallingApp): CountUserResult = coroutineScope {
        try {
            val savedLastUserCountingResponse = repository.currentData().lastUserCountingResponse
            Timber.d(
                "User count lastUserCountingResponse saved is `%d`",
                savedLastUserCountingResponse
            )
            val acceptableAdsEnabled = settings.currentSettings().acceptableAdsEnabled
            analyticsProvider.setUserProperty(AnalyticsUserProperty.IS_AA_ENABLED, acceptableAdsEnabled.toString())
            val acceptableAdsSubscription = settings.getAcceptableAdsSubscription()
            val currentUserCountingCount = repository.currentData().userCountingCount
            val url = createUrl(
                acceptableAdsSubscription, acceptableAdsEnabled,
                savedLastUserCountingResponse, currentUserCountingCount, callingApp
            )
            val request = Request.Builder().url(url).head().build()
            val response = retryIO(description = "User counting HEAD request") {
                okHttpClient.newCall(request).await()
            }
            val result = when (response.code) {
                HTTP_OK -> {
                    val newLastVersion = parseDateString(
                        response.headers["Date"] ?: "",
                        analyticsProvider
                    )
                    Timber.d(
                        "User count saves new lastUserCountingResponse `%s`",
                        newLastVersion
                    )
                    repository.updateLastUserCountingResponse(newLastVersion.toLong())
                    // No point to update the value otherwise as we send max as "4+"
                    if (currentUserCountingCount < MAX_USER_COUNTING_COUNT)
                        repository.updateUserCountingCount(currentUserCountingCount + 1)
                    CountUserResult.Success()
                }
                else -> {
                    analyticsProvider.logError(
                        "$HTTP_ERROR_LOG_HEADER_USER_COUNTER ${response.code}"
                                + "\nHeaders:\n${response.headers.toString()
                                        .take(HttpConstants.HTTP_ERROR_AVERAGE_HEADERS_SIZE)}"
                                + "\nBody:\n${response.body?.string()
                                        ?.take(HttpConstants.HTTP_ERROR_MAX_BODY_SIZE) ?: ""}")
                    CountUserResult.Failed()
                }
            }
            response.close()
            result
        } catch (ex: Exception) {
            if (BuildConfig.DEBUG && ex is ParseException) {
                throw ex
            }
            Timber.e("User count request failed")
            Timber.e(ex)
            if (ex !is java.net.SocketTimeoutException &&
                ex !is java.net.ConnectException &&
                ex !is java.net.UnknownHostException
            ) {
                analyticsProvider.logException(ex)
            }
            CountUserResult.Failed()
        }
    }

    private fun Int.asDownloadCount(): String =
        if (this < MAX_USER_COUNTING_COUNT) this.toString() else "4+"

    private fun createUrl(
        subscription: Subscription,
        acceptableAdsEnabled: Boolean,
        savedLastUserCountingResponse: Long,
        currentUserCountingCount: Int,
        callingApp: CallingApp
    ): HttpUrl {
        return subscription.randomizedUrl.sanitizeUrl().toHttpUrl().newBuilder().apply {
            addQueryParameter("addonName", appInfo.addonName)
            addQueryParameter("addonVersion", appInfo.addonVersion)
            addQueryParameter("application", callingApp.applicationName)
            addQueryParameter("applicationVersion", callingApp.applicationVersion)
            addQueryParameter("platform", appInfo.platform)
            addQueryParameter("platformVersion", appInfo.platformVersion)
            addQueryParameter("disabled", (!acceptableAdsEnabled).toString())
            addQueryParameter("lastVersion", savedLastUserCountingResponse.toString())
            addQueryParameter("downloadCount", currentUserCountingCount.asDownloadCount())
        }.build()
    }

    companion object {
        fun parseDateString(rawDate: String, analyticsProvider: AnalyticsProvider?): String {
            Timber.d("HTTP response Date header: %s", rawDate)
            lastUserCountingResponseFormat.timeZone = serverTimeZone
            return try {
                // Expected date format in "Date" header: "Thu, 23 Sep 2021 17:31:01 GMT"
                lastUserCountingResponseFormat.format(
                    serverDateParser.parse(rawDate)
                )
            } catch (ex: ParseException) {
                Timber.e(ex)
                analyticsProvider?.logException(ex)
                if (BuildConfig.DEBUG) {
                    throw ex
                } else {
                    Timber.e("Parsing 'Date' from header failed, using client GMT time")
                    lastUserCountingResponseFormat.format(Calendar.getInstance().time)
                }
            }
        }

        val lastUserCountingResponseFormat = SimpleDateFormat(
            "yyyyMMddHHmm",
            Locale.ENGLISH
        )
        private val serverDateParser = SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z",
            Locale.ENGLISH
        )
        private val serverTimeZone = TimeZone.getTimeZone("GMT")
        private const val MAX_USER_COUNTING_COUNT = 4

        internal const val HTTP_ERROR_LOG_HEADER_USER_COUNTER = "OkHttpUserCounter HTTP error, return code"
    }
}
