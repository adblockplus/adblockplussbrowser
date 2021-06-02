package org.adblockplus.adblockplussbrowser.core.downloader

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import okio.buffer
import okio.sink
import okio.source
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.AppInfo
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.exists
import org.adblockplus.adblockplussbrowser.core.data.model.ifExists
import org.adblockplus.adblockplussbrowser.core.extensions.sanatizeUrl
import org.adblockplus.adblockplussbrowser.core.retryIO
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.io.File
import kotlin.time.ExperimentalTime


@ExperimentalTime
internal class OkHttpDownloader(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val repository: CoreRepository,
    private val appInfo: AppInfo
) : Downloader {

    override suspend fun download(subscription: Subscription,
                                  newSubscription: Boolean,
                                  forceDownload: Boolean): DownloadResult = coroutineScope {
        try {
            val previousDownload = getDownloadedSubscription(subscription)

            /* We check for some conditions here:
             *  - If we are not forcing the download (periodic or manual updates)
             *  - If the previous downloaded file is NOT "expired"
             *      - If we are adding the subscription we use MIN_REFRESH_INTERVAL (5 minutes)
             *      - Otherwise we use REFRESH_INTERVAL (8 hours)
             *  - If the previous downloaded file still exists on the filesystem.
             *
             *  If all conditions are met we just return the previously downloaded file and don't
             *  hit the network.
             */
            Timber.d("Subscription: ${subscription.url}: $forceDownload, ${previousDownload.isNotExpired(newSubscription)}, ${previousDownload.exists()}")
            if (!forceDownload && previousDownload.isNotExpired(newSubscription) && previousDownload.exists()) {
                Timber.d("Returning pre-downloaded subscriptions: $previousDownload")
                return@coroutineScope DownloadResult.NotModified(previousDownload)
            }

            val url = createUrl(subscription, previousDownload)
            val downloadFile = File(previousDownload.path)
            val request = createDownloadRequest(url, downloadFile, previousDownload)

            Timber.d("Downloading $url - previous subscription: $previousDownload")

            val response = retryIO(description = subscription.title) {
                okHttpClient.newCall(request).await()
            }

            val result = when (response.code) {
                200 -> {
                    val tempFile = writeTempFile(response.body!!.source())
                    context.downloadsDir().mkdirs()
                    tempFile.renameTo(downloadFile)

                    DownloadResult.Success(previousDownload.copy(
                        lastUpdated = System.currentTimeMillis(),
                        lastModified = response.headers["Last-Modified"] ?: "",
                        version = extractVersion(downloadFile),
                        etag = response.headers["ETag"] ?: "",
                        downloadCount = previousDownload.downloadCount + 1
                    ))
                }
                304 -> {
                    DownloadResult.NotModified(previousDownload.copy(
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
                else -> {
                    Timber.e("Error downloading $url, response code: ${response.code}")
                    DownloadResult.Failed(previousDownload.ifExists())
                }
            }
            response.close()
            result
        } catch (ex: Exception) {
            ex.printStackTrace()
            val previousDownload = getDownloadedSubscription(subscription)
            Timber.e(ex, "Error downloading ${previousDownload.url}")
            DownloadResult.Failed(previousDownload.ifExists())
        }
    }

    override suspend fun validate(subscription: Subscription): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = createUrl(subscription)
            val request = createHeadRequest(url)

            val response = retryIO(description = subscription.title) {
                okHttpClient.newCall(request).await()
            }
            response.code == 200
        } catch (ex: Exception) {
            Timber.d(ex, "Error downloading ${subscription.url}")
            false
        }
    }

    private suspend fun getDownloadedSubscription(subscription: Subscription): DownloadedSubscription {
        return try {
            val url = subscription.url.sanatizeUrl().toHttpUrl()
            val coreData = repository.getDataSync()
            return coreData.downloadedSubscription.firstOrNull {
                it.url == subscription.url
            } ?: DownloadedSubscription(
                subscription.url,
                path = context.downloadFile(url.toFileName()).absolutePath
            )
        } catch (ex: Exception) {
            Timber.e(ex, "Error parsing url: ${subscription.url}")
            DownloadedSubscription(subscription.url)
        }
    }

    private fun createUrl(subscription: Subscription,
                          previousDownload: DownloadedSubscription =
                              DownloadedSubscription(subscription.url)
    ): HttpUrl {
        return subscription.url.sanatizeUrl().toHttpUrl().newBuilder().apply {
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
        previousDownload: DownloadedSubscription
    ): Request =
        Request.Builder().url(url).apply {
            // Don't apply If-Modified-Since and If-None-Match if the file doesn't exists on the filesystem
            if (file.exists()) {
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

    // Parse Version from subscription file
    private fun extractVersion(file: File): String {
        val version = readHeader(file).asSequence().map { it.trim() }
            .filter { it.startsWith("!") }
            .filter { it.contains(":") }
            .map { line ->
                val split = line.split(":", limit = 2)
                Pair(split[0].trim(), split[1].trim())
            }
            .filter { pair -> pair.first.contains("version", true) }
            .map { pair -> pair.second }
            .firstOrNull()

        return version ?: "0"
    }

    private fun readHeader(file: File): List<String> {
        file.source().buffer().use { source ->
            return generateSequence { source.readUtf8Line() }
                .takeWhile { it.isNotEmpty() && (it[0] == '[' || it[0] == '!') }
                .toList()
        }
    }

    private fun HttpUrl.toFileName(): String = "${this.toString().hashCode()}.txt"

    private fun DownloadedSubscription.isNotExpired(newSubscription: Boolean) =
        !this.isExpired(newSubscription)

    private fun DownloadedSubscription.isExpired(newSubscription: Boolean): Boolean {
        val elapsed = System.currentTimeMillis() - this.lastUpdated
        return if (newSubscription) elapsed > MIN_REFRESH_INTERVAL else elapsed > REFRESH_INTERVAL
    }

    companion object {
        private const val MIN_REFRESH_INTERVAL = 5 * 60 * 1000 // 5 minutes
        private const val REFRESH_INTERVAL = 8 * 60 * 60 * 1000 // 8 hours
    }
}

private fun Int.asDownloadCount(): String = if (this < 4) this.toString() else "4+"

private fun Context.downloadsDir(): File =
    File(applicationContext.filesDir, "downloads")

private fun Context.downloadFile(filename: String): File =
    File(downloadsDir(), filename)