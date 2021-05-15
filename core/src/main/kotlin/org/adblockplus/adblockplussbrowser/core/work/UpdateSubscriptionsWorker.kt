package org.adblockplus.adblockplussbrowser.core.work

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

@HiltWorker
internal class UpdateSubscriptionsWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val coreRepository: CoreRepository,
    private val downloader: Downloader,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("DOWNLOAD JOB")

            val subscriptions = mutableListOf<DownloadedSubscription>()
            val settings = settingsRepository.settings.take(1).single()
            Timber.d("Downloader settings: $settings")
            settings.activePrimarySubscriptions.forEach { subscription ->
                val downloadedSubscription = downloader.download(subscription)
                downloadedSubscription?.let {
                    Timber.d("Downloaded: $downloadedSubscription")
                    subscriptions.add(it)
                }
            }

            // TODO - decide how to handle failure on individual subscriptions

            if (subscriptions.isEmpty()) {
                Result.retry()
            } else {
                writeFiles(subscriptions)
                coreRepository.updateDownloadedSubscriptions(subscriptions)
                dispatchUpdate()
                Result.success()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Result.retry()
        }
    }

    private suspend fun dispatchUpdate() = withContext(Dispatchers.Main) {
        val intent = Intent()
        intent.action = "com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE"
        intent.data = Uri.parse("package:" + appContext.packageName)
        appContext.sendBroadcast(intent)
    }

    // TODO - move elsewhere
    private suspend fun writeFiles(subscriptions: List<DownloadedSubscription>) {
        coroutineScope {
            val dir = getCacheDir(appContext)
            val temp = File.createTempFile("filter", ".txt", dir)

            subscriptions.forEach { subscription ->
                val file = File(subscription.path)
                file.source().use { source ->
                    temp.sink(append = true).buffer().use { dest -> dest.writeAll(source) }
                }
            }

            // TODO - use a repository
            val oldPath = coreRepository.subscriptionsPath
            coreRepository.subscriptionsPath = temp.absolutePath

            oldPath?.let { path -> File(path).delete() }
        }
    }

    companion object {
        private const val KEY_PERIODIC_WORK = "PERIODIC_KEY"

        fun scheduleOneTime(context: Context) {
            val request = OneTimeWorkRequestBuilder<UpdateSubscriptionsWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(UpdateConfig.ALWAYS.toNetworkType()).build())
                .build()
//            WorkManager.getInstance(context).enqueue(request)
        }

        fun schedule(context: Context, updateConfig: UpdateConfig) {
            val request = PeriodicWorkRequestBuilder<UpdateSubscriptionsWorker>(5, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(updateConfig.toNetworkType()).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(KEY_PERIODIC_WORK)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

            val manager = WorkManager.getInstance(context)
            manager.cancelAllWorkByTag(KEY_PERIODIC_WORK)
            manager.enqueue(request)
        }

        fun getCacheDir(context: Context): File {
            val directory = File(context.filesDir, "cache")
            directory.mkdirs()
            return directory
        }

        private fun UpdateConfig.toNetworkType(force: Boolean = false): NetworkType =
            if (this == UpdateConfig.WIFI_ONLY && !force) NetworkType.CONNECTED else NetworkType.NOT_ROAMING
    }
}
