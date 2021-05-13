package org.adblockplus.adblockplussbrowser.core.downloader

import android.content.Context
import kotlinx.coroutines.coroutineScope
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
import org.adblockplus.adblockplussbrowser.core.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.exists
import org.adblockplus.adblockplussbrowser.core.retryIO
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.io.File


internal class OkHttpDownloader(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val repository: CoreRepository,
    private val appInfo: AppInfo
) : Downloader {
    override suspend fun download(subscription: Subscription): Downloader.Result = coroutineScope {
        val previousDownload = getDownloadedSubscription(subscription)
        try {
            val url = createUrl(subscription, previousDownload.version, previousDownload.downloadCount)
            val file = File(previousDownload.path)
            val request = createDownloadRequest(url, file, previousDownload)

            Timber.d("Downloading $url - previous subscription: $previousDownload")

            val response = retryIO(description = subscription.title) {
                okHttpClient.newCall(request).await()
            }

            when (response.code) {
                200 -> {
                    val tempFile = writeTempFile(response.body!!.source())
                    context.downloadsDir().mkdirs()
                    tempFile.renameTo(file)

                    Downloader.Result.Success(previousDownload.copy(
                        lastUpdated = System.currentTimeMillis(),
                        lastModified = response.headers["Last-Modified"] ?: "",
                        version = extractVersion(file),
                        etag = response.headers["ETag"] ?: "",
                        downloadCount = previousDownload.downloadCount + 1
                    ))
                }
                304 -> {
                    Downloader.Result.NotModified(previousDownload.copy(
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
                else -> {
                    Timber.e("Error downloading $url, response code: ${response.code}")
                    Downloader.Result.Failed(if (previousDownload.exists()) previousDownload else null)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Timber.e(ex, "Error downloading ${previousDownload.url}")
            Downloader.Result.Failed(if (previousDownload.exists()) previousDownload else null)
        }
    }

    private suspend fun getDownloadedSubscription(subscription: Subscription): DownloadedSubscription {
        val url = subscription.url.toHttpUrl()
        val coreData = repository.getDataSync()
        return coreData.downloadedSubscription.firstOrNull {
            it.url == subscription.url
        } ?: DownloadedSubscription(
            subscription.url,
            path = context.downloadFile(url.pathSegments.last()).absolutePath
        )
    }

    private fun createUrl(subscription: Subscription, version: String, downloadCount: Int): HttpUrl {
        return subscription.url.toHttpUrl().newBuilder().apply {
            addQueryParameter("addonName", appInfo.addonName)
            addQueryParameter("addonVersion", appInfo.addonVersion)
            addQueryParameter("application", appInfo.application)
            addQueryParameter("applicationVersion", appInfo.applicationVersion)
            addQueryParameter("platform", appInfo.platform)
            addQueryParameter("platformVersion", appInfo.platformVersion)
            addQueryParameter("lastVersion", version)
            addQueryParameter("downloadCount", downloadCount.asDownloadCount())
        }.build()
    }

    private fun createDownloadRequest(
        url: HttpUrl,
        file: File,
        previousDownload: DownloadedSubscription
    ): Request =
        Request.Builder().url(url).apply {
            if (file.exists()) {
                if (previousDownload.lastModified.isNotEmpty()) {
                    addHeader("If-Modified-Since", previousDownload.lastModified)
                }
                if (previousDownload.etag.isNotEmpty()) {
                    addHeader("If-None-Match", previousDownload.etag)
                }
            }
        }.build()

    private fun writeTempFile(source: BufferedSource): File {
        val file = File.createTempFile("list", ".txt", context.cacheDir)
        source.use { source ->
            file.sink().buffer().use { dest -> dest.writeAll(source) }
        }
        return file
    }

    // Parse Version from subscription file
    private fun extractVersion(file: File): String {
        val version = readHeader(file).asSequence().map { it.trim() }
            .filter { it.startsWith("!") }
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
}

private fun Int.asDownloadCount(): String = if (this < 4) this.toString() else "4+"

private fun Context.downloadsDir(): File =
    File(applicationContext.filesDir, "downloads")

private fun Context.downloadFile(filename: String): File =
    File(downloadsDir(), filename)