package org.adblockplus.adblockplussbrowser.core.work

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import okio.buffer
import okio.sink
import okio.source
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.downloader.DownloadResult
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.downloader.hasFailedResult
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import timber.log.Timber
import java.io.File

@HiltWorker
internal class UpdateSubscriptionsWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val subscriptionsManager: SubscriptionsManager,
    private val settingsRepository: SettingsRepository,
    private val coreRepository: CoreRepository,
    private val downloader: Downloader,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("DOWNLOAD JOB")

            val settings = settingsRepository.settings.take(1).single()
            Timber.d("Downloader settings: $settings")

            val activeSubscriptions = settings.activePrimarySubscriptions.ensureEasylist(settingsRepository.getEasylistSubscription()) +
                    settings.activeOtherSubscriptions +
                    aceptableAdsSubscription(settings.acceptableAdsEnabled)

            // check if Work is stopped and return
            if (isStopped) return@withContext Result.success()

            val results = mutableListOf<DownloadResult>()
            activeSubscriptions.forEachIndexed { index, subscription ->

                // check if Work is stopped and return
                if (isStopped) return@withContext Result.success()

                subscriptionsManager._status.postValue(SubscriptionsManager.Status.Downloading(index + 1))
                val result = downloader.download(subscription)
                Timber.d("Subscription: ${subscription.title} -> $result")
                results.add(result)
            }

            // check if Work is stopped and return
            if (isStopped) return@withContext Result.success()

            val subscriptions = results.mapNotNull { it.subscription }
            writeFiles(subscriptions)
            coreRepository.updateDownloadedSubscriptions(subscriptions)
            dispatchUpdate()

            updateSubscriptionsLastUpdated(settings, subscriptions)

            if (results.hasFailedResult()) {
                Timber.w("Failed subscriptions updates, retrying shortly")
                subscriptionsManager._status.postValue(SubscriptionsManager.Status.Failed)
                Result.retry()
            } else {
                Timber.i("Subscriptions downloaded")
                subscriptionsManager._status.postValue(SubscriptionsManager.Status.Success)
                Result.success()
            }
        } catch (ex: Exception) {
            Timber.w(ex, "Failed subscriptions updates, retrying shortly")
            ex.printStackTrace()
            Result.retry()
        }
    }

    private suspend fun aceptableAdsSubscription(enabled: Boolean) =
        if (enabled) listOf(settingsRepository.getAcceptableAdsSubscription()) else emptyList()

    private suspend fun updateSubscriptionsLastUpdated(
        settings: Settings,
        subscriptions: List<DownloadedSubscription>
    ) {
        val adSubscriptions = settings.activePrimarySubscriptions.mapNotNull { subscription ->
            subscriptions.firstOrNull { it.url == subscription.url }?.let { downloaded ->
                subscription.copy(lastUpdate = downloaded.lastUpdated)
            }
        }
        val otherSubscriptions = settings.activeOtherSubscriptions.mapNotNull { subscription ->
            subscriptions.firstOrNull { it.url == subscription.url }?.let { downloaded ->
                subscription.copy(lastUpdate = downloaded.lastUpdated)
            }
        }

        settingsRepository.setActivePrimarySubscriptions(adSubscriptions)
        settingsRepository.setActiveOtherSubscriptions(otherSubscriptions)
    }

    private suspend fun dispatchUpdate() = withContext(Dispatchers.Main) {
        Timber.i("Dispatching UPDATE Intent to Browser...")
        val intent = Intent()
        intent.action = "com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE"
        intent.data = Uri.parse("package:" + appContext.packageName)
        appContext.sendBroadcast(intent)
    }

    private suspend fun writeFiles(subscriptions: List<DownloadedSubscription>) {
        coroutineScope {
            val dir = appContext.getCacheDownloadDir()
            val temp = File.createTempFile("filter", ".txt", dir)

            val files = subscriptions.map { File(it.path) }
            val filters = files.toFiltersSet()
            Timber.d("filters file: ${temp.name}, rules: ${filters.size}")

            temp.sink().buffer().use { sink ->
                filters.forEach { filter ->
                    sink.writeUtf8(filter)
                    sink.writeUtf8("\n")
                }
            }

            val oldPath = coreRepository.subscriptionsPath
            coreRepository.subscriptionsPath = temp.absolutePath

            oldPath?.let { path -> File(path).delete() }
        }
    }

    private fun List<File>.toFiltersSet(): Set<String> {
        val filters = mutableSetOf<String>()
        this.forEach { file ->
            file.source().use { fileSource ->
                fileSource.buffer().use { source ->
                    val set = generateSequence {
                        source.readUtf8Line()
                    }.filter {
                        it.isFilter()
                    }.toSet()
                    Timber.d("File: ${file.name} : rule size: ${set.size}")
                    filters += set
                }
            }
        }
        return filters
    }

    private fun String.isFilter(): Boolean = this.isNotEmpty() && this[0] != '[' && this[0] != '!'

    private fun Context.getCacheDownloadDir(): File {
        val directory = File(this.filesDir, "cache")
        directory.mkdirs()
        return directory
    }
}

/**
 * If active primary subscriptions are not empty and doesn't contains EasyList main filter list,
 * ensure it is downloaded
 */
private fun List<Subscription>.ensureEasylist(easylistSubscription: Subscription): List<Subscription> {
    val easylist = if (this.isNotEmpty() && this.none { it.url == easylistSubscription.url }) {
        listOf(easylistSubscription)
    } else {
        emptyList()
    }
    return easylist + this
}
