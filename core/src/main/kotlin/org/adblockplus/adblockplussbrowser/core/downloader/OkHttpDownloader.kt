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

package org.adblockplus.adblockplussbrowser.core.downloader

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import okio.buffer
import okio.sink
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.data.DownloaderConstants.METERED_REFRESH_INTERVAL_DAYS
import org.adblockplus.adblockplussbrowser.base.data.DownloaderConstants.UNMETERED_REFRESH_INTERVAL_HOURS
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.AppInfo
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.exists
import org.adblockplus.adblockplussbrowser.core.data.model.ifExists
import org.adblockplus.adblockplussbrowser.core.extensions.sanitizeUrl
import org.adblockplus.adblockplussbrowser.core.retryIO
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter.Companion.HTTP_ERROR_AVERAGE_HEADERS_SIZE
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter.Companion.HTTP_ERROR_MAX_BODY_SIZE
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.io.File
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_OK
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@ExperimentalTime
internal class OkHttpDownloader(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val repository: CoreRepository,
    private val appInfo: AppInfo,
    private val analyticsProvider: AnalyticsProvider
) : Downloader {

    private val connectivityManager = ContextCompat.getSystemService(context,
        ConnectivityManager::class.java)

    override suspend fun download(
        subscription: Subscription,
        forced: Boolean,
        periodic: Boolean,
        newSubscription: Boolean,
    ): DownloadResult = coroutineScope {
        try {
            val previousDownload = getDownloadedSubscription(subscription)

            if (canSkipDownload(previousDownload, forced, periodic, newSubscription)) {
                Timber.d("Returning pre-downloaded subscription: ${previousDownload.url}")
                return@coroutineScope DownloadResult.NotModified(previousDownload)
            }

            val url = createUrl(subscription, previousDownload)
            val downloadFile = File(previousDownload.path)
            val request = createDownloadRequest(url, downloadFile, previousDownload, forced)

            Timber.d("Downloading $url - previous subscription: $previousDownload")
            val response = retryIO(description = subscription.title) {
                okHttpClient.newCall(request).await()
            }

            val result = when (response.code) {
                HTTP_OK -> {
                    val tempFile = writeTempFile(response.body!!.source())
                    context.downloadsDir().mkdirs()
                    tempFile.renameTo(downloadFile)

                    val newLastVersion = OkHttpUserCounter.parseDateString(
                        response.headers["Date"] ?: "",
                        analyticsProvider
                    )

                    DownloadResult.Success(previousDownload.copy(
                        lastUpdated = System.currentTimeMillis(),
                        lastModified = response.headers["Last-Modified"] ?: "",
                        version = newLastVersion,
                        etag = response.headers["ETag"] ?: "",
                        downloadCount = previousDownload.downloadCount + 1
                    ))
                }
                HTTP_NOT_MODIFIED -> {
                    DownloadResult.NotModified(previousDownload.copy(
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
                else -> {
                    Timber.e("Error downloading $url, response code: ${response.code}")
                    analyticsProvider.logError(
                        "$HTTP_ERROR_LOG_HEADER_DOWNLOADER ${response.code}"
                                + "\nHeaders:\n${response.headers.toString().take(HTTP_ERROR_AVERAGE_HEADERS_SIZE)}"
                                + "\nBody:\n${response.body?.string()?.take(HTTP_ERROR_MAX_BODY_SIZE) ?: ""}")
                    DownloadResult.Failed(previousDownload.ifExists())
                }
            }
            response.close()
            result
        } catch (ex: Exception) {
            val previousDownload = getDownloadedSubscription(subscription)
            Timber.e(ex, "Error downloading ${previousDownload.url}")
            analyticsProvider.logException(ex)
            DownloadResult.Failed(previousDownload.ifExists())
        }
    }

    internal fun canSkipDownload(
        previousDownload: DownloadedSubscription,
        forced: Boolean,
        periodic: Boolean,
        newSubscription: Boolean
    ): Boolean {
        val isMetered = connectivityManager?.isActiveNetworkMetered ?: false
        val expired = previousDownload.isExpired(newSubscription, isMetered)
        val exists = previousDownload.exists()

        Timber.d("Url: %s: forced: %b, periodic: %b, new: %b, expired: %b, exists: %b, metered: %b",
            previousDownload.url, forced, periodic, newSubscription, expired, exists, isMetered)
        /* We check for some conditions here:
         *  - NEVER SKIP force refresh updates.
         *  - If this is a new subscription or a periodic update, DO NOT SkIP if it is not expired,
         *    AND the file still exists.
         *  - Otherwise if the file still exists, SKIP the update
         *
         *  Subscription expiration logic:
         *   - New subscriptions expires in MIN_REFRESH_INTERVAL (1 hour)
         *   - Metered connection in METERED_REFRESH_INTERVAL (3 days)
         *   - Unmetered connection in UNMETERED_REFRESH_INTERVAL (24 hours)
         */
        return if (forced) {
            false
        } else if (newSubscription || periodic) {
            !expired && exists
        } else {
            exists
        }
    }

    override suspend fun validate(subscription: Subscription): Boolean = coroutineScope {
        try {
            val url = createUrl(subscription)
            val request = createHeadRequest(url)

            val response = retryIO(description = subscription.title) {
                okHttpClient.newCall(request).await()
            }
            response.code == HTTP_OK
        } catch (ex: Exception) {
            Timber.e(ex, "Error downloading ${subscription.url}")
            false
        }
    }

    internal suspend fun getDownloadedSubscription(subscription: Subscription): DownloadedSubscription {
        return try {
            val url = subscription.url.sanitizeUrl().toHttpUrl()
            val coreData = repository.getDataSync()
            return coreData.downloadedSubscription.firstOrNull {
                it.url == subscription.url
            } ?: DownloadedSubscription(
                subscription.url,
                path = context.downloadFile(url.toFileName()).absolutePath
            )
        } catch (ex: Exception) {
            Timber.e(ex, "Error parsing url: ${subscription.url}")
            analyticsProvider.logException(ex)
            DownloadedSubscription(subscription.url)
        }
    }

    private fun createUrl(subscription: Subscription,
                          previousDownload: DownloadedSubscription =
                              DownloadedSubscription(subscription.url)
    ): HttpUrl {
        return subscription.randomizedUrl.sanitizeUrl().toHttpUrl().newBuilder().apply {
            addQueryParameter("addonName", appInfo.addonName)
            addQueryParameter("addonVersion", appInfo.addonVersion)
            addQueryParameter("application", appInfo.application)
            addQueryParameter("applicationVersion", appInfo.applicationVersion)
            addQueryParameter("platform", appInfo.platform)
            addQueryParameter("platformVersion", appInfo.platformVersion)
            addQueryParameter("lastVersion", previousDownload.version)
            addQueryParameter("downloadCount", previousDownload.downloadCount.asDownloadCount())
        }.build()
    }

    private fun createDownloadRequest(
        url: HttpUrl,
        file: File,
        previousDownload: DownloadedSubscription,
        forced: Boolean
    ): Request =
        Request.Builder().url(url).apply {
            // Don't apply If-Modified-Since and If-None-Match if the file doesn't exists on the filesystem
            if (!forced && file.exists()) {
                if (previousDownload.lastModified.isNotEmpty()) {
                    addHeader("If-Modified-Since", previousDownload.lastModified)
                }
                if (previousDownload.etag.isNotEmpty()) {
                    addHeader("If-None-Match", previousDownload.etag)
                }
            }
        }.build()

    private fun createHeadRequest(url: HttpUrl): Request =
        Request.Builder().url(url).head().build()

    private fun writeTempFile(input: BufferedSource): File {
        val file = File.createTempFile("list", ".txt", context.cacheDir)
        input.use { source ->
            file.sink().buffer().use { dest -> dest.writeAll(source) }
        }
        return file
    }

    private fun HttpUrl.toFileName(): String = "${this.toString().hashCode()}.txt"

    private fun DownloadedSubscription.isExpired(newSubscription: Boolean, isMetered: Boolean): Boolean {
        val elapsed = Duration.milliseconds(System.currentTimeMillis()) - Duration.milliseconds(this.lastUpdated)
        Timber.d("Elapsed: $elapsed, newSubscription: $newSubscription, isMetered: $isMetered")
        Timber.d("Min: $MIN_REFRESH_INTERVAL, Metered: $METERED_REFRESH_INTERVAL, Wifi: $UNMETERED_REFRESH_INTERVAL")
        val interval = if (newSubscription) {
            MIN_REFRESH_INTERVAL
        } else {
            if (isMetered) METERED_REFRESH_INTERVAL else UNMETERED_REFRESH_INTERVAL
        }

        Timber.d("Expired: ${elapsed > interval}")

        return elapsed > interval
    }

    companion object {
        private val MIN_REFRESH_INTERVAL = Duration.hours(1)
        private val UNMETERED_REFRESH_INTERVAL: Duration = Duration.hours(UNMETERED_REFRESH_INTERVAL_HOURS)
        private val METERED_REFRESH_INTERVAL = Duration.days(METERED_REFRESH_INTERVAL_DAYS)
        internal const val HTTP_ERROR_LOG_HEADER_DOWNLOADER = "OkHttpDownloader HTTP error, return code"
    }
}

private const val MAX_RETRY_COUNT = 4

private fun Int.asDownloadCount(): String = if (this < MAX_RETRY_COUNT) this.toString() else "4+"

private fun Context.downloadsDir(): File =
    File(applicationContext.filesDir, "downloads")

private fun Context.downloadFile(filename: String): File =
    File(downloadsDir(), filename)

