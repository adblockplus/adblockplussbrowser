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

package org.adblockplus.adblockplussbrowser.telemetry

import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.adblockplus.adblockplussbrowser.base.BuildConfig
import org.adblockplus.adblockplussbrowser.base.data.HttpConstants
import org.adblockplus.adblockplussbrowser.base.os.AppInfo
import org.adblockplus.adblockplussbrowser.base.os.CallingApp
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.currentSettings
import org.adblockplus.adblockplussbrowser.telemetry.data.TelemetryRepository
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.net.HttpRetryException
import java.net.HttpURLConnection
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class OkHttpTelemetry(
    private val okHttpClient: OkHttpClient,
    private val repository: TelemetryRepository,
    private val settings: SettingsRepository,
    private val appInfo: AppInfo,
) : UserCounter {

    @Suppress("LongMethod")
    override suspend fun count(callingApp: CallingApp): Result<Unit> = coroutineScope {
        try {
            val savedLastUserCountingResponse = repository.currentData().lastUserCountingResponse
            Timber.d(
                "User count lastUserCountingResponse saved is `%d`",
                savedLastUserCountingResponse
            )
            val acceptableAdsEnabled = settings.currentSettings().acceptableAdsEnabled
            val currentUserCountingCount = repository.currentData().userCountingCount
            val url =
                "https://test-telemetry.data.eyeo.it/topic/webextension_activeping/version/1".toHttpUrl()
            val request = Request.Builder().url(url).head().build()
            // in the old http counter we used #retryIO method to retry the request thrice,
            // though I don't see a reason to do it here since we are retrying after one hour
            // if the request wasn't successful
            val response = okHttpClient.newCall(request).await()
            val result = when (response.code) {
                HttpURLConnection.HTTP_OK -> {
                    val newLastVersion = parseDateString(
                        response.headers["Date"] ?: ""
                    )
                    Timber.d(
                        "User count saves new lastUserCountingResponse `%s`",
                        newLastVersion
                    )
                    repository.updateLastUserCountingResponse(newLastVersion.toLong())
                    // No point to update the value otherwise as we send max as "4+"
                    if (currentUserCountingCount < MAX_USER_COUNTING_COUNT)
                        repository.updateUserCountingCount(currentUserCountingCount + 1)
                    Result.success(Unit)
                }

                else -> {
                    val error = ("$HTTP_ERROR_LOG_HEADER_USER_COUNTER ${response.code}"
                            + "\nHeaders:\n${
                        response.headers.toString()
                            .take(HttpConstants.HTTP_ERROR_AVERAGE_HEADERS_SIZE)
                    }"
                            + "\nBody:\n${
                        response.body?.string()
                            ?.take(HttpConstants.HTTP_ERROR_MAX_BODY_SIZE) ?: ""
                    }")
                    // TODO analyticsProvider.logError(error)
                    Result.failure(HttpRetryException(error, response.code))
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
                // TODO log to analytics
            }
            Result.failure(ex)
        }
    }

    companion object {
        fun parseDateString(rawDate: String): String {
            Timber.d("HTTP response Date header: %s", rawDate)
            lastUserCountingResponseFormat.timeZone = serverTimeZone
            return try {
                // Expected date format in "Date" header: "Thu, 23 Sep 2021 17:31:01 GMT"
                lastUserCountingResponseFormat.format(
                    serverDateParser.parse(rawDate)
                )
            } catch (ex: ParseException) {
                Timber.e(ex)
                if (BuildConfig.DEBUG) {
                    throw ex
                } else {
                    Timber.e("Parsing 'Date' from header failed, using client GMT time")
                    lastUserCountingResponseFormat.format(Calendar.getInstance().time)
                }
            }
        }

        private val lastUserCountingResponseFormat = SimpleDateFormat(
            "yyyyMMddHHmm",
            Locale.ENGLISH
        )
        private val serverDateParser = SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z",
            Locale.ENGLISH
        )
        private val serverTimeZone = TimeZone.getTimeZone("GMT")
        private const val MAX_USER_COUNTING_COUNT = 4

        internal const val HTTP_ERROR_LOG_HEADER_USER_COUNTER =
            "OkHttpUserCounter HTTP error, return code"
    }
}