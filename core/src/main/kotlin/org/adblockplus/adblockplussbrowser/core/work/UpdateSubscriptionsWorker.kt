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
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.proto.toSavedState
import org.adblockplus.adblockplussbrowser.core.downloader.DownloadResult
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.downloader.hasFailedResult
import org.adblockplus.adblockplussbrowser.core.extensions.toAllowRule
import org.adblockplus.adblockplussbrowser.core.extensions.toBlockRule
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
    private val downloader: Downloader
) : CoroutineWorker(appContext, params) {

    private var totalSteps: Int = 0
    private var currentStep: Int = 0

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // if it is a periodic check, force update subscriptions
        return@withContext try {
            Timber.d("DOWNLOAD JOB")

            val settings = settingsRepository.currentSettings()
            Timber.d("Downloader settings: $settings")
            val savedState = coreRepository.currentSavedState()
            Timber.d("Saved state: $savedState")
            val changes = settings.changes(savedState)
            Timber.d("Diff: $changes")
            Timber.d("Run Attempt: $runAttemptCount")

            // Don't let a failing worker run eternally...
            if (hasReachedMaxAttempts()) {
                Timber.d("Max attempts reached...")
                return@withContext Result.failure()
            }

            // Has settings changes from last update Job???
            // Don't skip force refresh, periodic updates or retries (runAttemptCount > 0)
            if (!changes.hasChanges() && !tags.isForceRefresh() && !tags.isPeriodic() && runAttemptCount == 0) {
                Timber.d("No changes from last update")
                return@withContext Result.success()
            }

            // Current active subscriptions
            val activeSubscriptions =
                settings.activePrimarySubscriptions.ensureEasylist(settingsRepository.getEasylistSubscription()) +
                        settings.activeOtherSubscriptions +
                        acceptableAdsSubscription(settings.acceptableAdsEnabled)
            totalSteps = activeSubscriptions.size + 1

            // Download new subscriptions
            updateStatus(ProgressType.PROGRESS)
            val results = downloadSubscriptions(
                settings, activeSubscriptions, changes, tags.isForceRefresh(),
                tags.isPeriodic()
            )

            // check if Work is stopped and return
            if (isStopped) return@withContext Result.success()

            val subscriptions = results.mapNotNull { it.subscription }
            val filtersFile =
                writeFiles(subscriptions, settings.allowedDomains, settings.blockedDomains)

            // check if Work is stopped and return
            if (isStopped) return@withContext Result.success()
            coreRepository.updateDownloadedSubscriptions(
                subscriptions,
                tags.isForceRefresh() or tags.isPeriodic()
            )
            coreRepository.updateSavedState(settings.toSavedState())
            dispatchUpdate()
            cleanOldFiles(filtersFile)

            updateStatus(ProgressType.PROGRESS)
            updateSubscriptionsLastUpdated(settings, subscriptions)

            if (results.hasFailedResult()) {
                Timber.w("Failed subscriptions updates, retrying shortly")
                delay(DELAY_DEFAULT)
                updateStatus(ProgressType.FAILED)
                failedResult()
            } else {
                Timber.i("Subscriptions downloaded")
                delay(DELAY_DEFAULT)
                updateStatus(ProgressType.SUCCESS)
                Result.success()
            }
        } catch (ex: Exception) {
            Timber.w(ex, "Failed subscriptions updates, retrying shortly")
            delay(DELAY_DEFAULT)
            updateStatus(ProgressType.FAILED)
            if (ex is CancellationException) Result.success() else failedResult()
        }
    }

    private fun failedResult(): Result =
        if (tags.isForceRefresh()) Result.failure() else Result.retry()

    private suspend fun updateStatus(type: ProgressType) {
        val progress = if (tags.isForceRefresh()) {
            when (type) {
                ProgressType.PROGRESS -> {
                    val step = 100 / totalSteps
                    SubscriptionUpdateStatus.Progress(step * currentStep++)
                }
                ProgressType.SUCCESS -> SubscriptionUpdateStatus.Success
                ProgressType.FAILED -> SubscriptionUpdateStatus.Failed
            }
        } else {
            SubscriptionUpdateStatus.None
        }

        subscriptionsManager.updateStatus(progress)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun downloadSubscriptions(
        settings: Settings,
        activeSubscriptions: List<Subscription>,
        changes: Changes,
        forced: Boolean,
        periodic: Boolean
    ): List<DownloadResult> = coroutineScope {
        // List of newly added subscriptions to Download
        val newSubscriptions = changes.newSubscriptions +
                acceptableAdsSubscription(changes, settings.acceptableAdsEnabled)

        // check if Work is stopped and return
        if (isStopped) throw CancellationException()

        val results = mutableListOf<DownloadResult>()
        activeSubscriptions.forEach { subscription ->
            // check if Work is stopped and return
            if (isStopped) throw CancellationException()

            val isNewSubscription = newSubscriptions.any { it.url == subscription.url }
            val result = downloader.download(subscription, forced, periodic, isNewSubscription)
            if (result is DownloadResult.Success) {
                updateStatus(ProgressType.PROGRESS)
            }
            Timber.d("Subscription: ${subscription.title} -> $result")
            results.add(result)
        }
        results
    }

    private suspend fun acceptableAdsSubscription(enabled: Boolean) =
        if (enabled) listOf(settingsRepository.getAcceptableAdsSubscription()) else emptyList()

    private suspend fun acceptableAdsSubscription(changes: Changes, acceptableAdsEnabled: Boolean) =
        if (changes.acceptableAdsChanged() && acceptableAdsEnabled) {
            listOf(settingsRepository.getAcceptableAdsSubscription())
        } else {
            emptyList()
        }

    private suspend fun updateSubscriptionsLastUpdated(
        settings: Settings,
        subscriptions: List<DownloadedSubscription>
    ) {
        val primarySubscriptions = settings.activePrimarySubscriptions.mapNotNull { subscription ->
            subscriptions.firstOrNull { it.url == subscription.url }?.let { downloaded ->
                subscription.copy(lastUpdate = downloaded.lastUpdated)
            }
        }
        val otherSubscriptions = settings.activeOtherSubscriptions.mapNotNull { subscription ->
            subscriptions.firstOrNull { it.url == subscription.url }?.let { downloaded ->
                subscription.copy(lastUpdate = downloaded.lastUpdated)
            }
        }

        settingsRepository.updatePrimarySubscriptionsLastUpdate(primarySubscriptions)
        settingsRepository.updateOtherSubscriptionsLastUpdate(otherSubscriptions)
    }

    private suspend fun dispatchUpdate() = withContext(Dispatchers.Main) {
        Timber.i("Dispatching UPDATE Intent to Browser...")
        val intent = Intent()
        intent.action = "com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE"
        intent.data = Uri.parse("package:" + appContext.packageName)
        val isAAEnabled = coreRepository.currentSavedState().acceptableAdsEnabled
        Timber.i("Is AA enabled: $isAAEnabled")
        intent.putExtra("com.samsung.android.sbrowser.contentBlocker.IS_AA_ENABLED", isAAEnabled)
        appContext.sendBroadcast(intent)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun writeFiles(
        subscriptions: List<DownloadedSubscription>,
        allowedDomains: List<String>,
        blockedDomains: List<String>
    ): File = coroutineScope {
        val dir = appContext.getCacheDownloadDir()
        val newFile = File.createTempFile("filter", ".txt", dir)

        var customRules = 0
        val filtersSet = subscriptions.toFiltersSet()
        newFile.sink().buffer().use { sink ->
            sink.writeUtf8("[Adblock Plus 2.0]\n")
            sink.writeUtf8("! This file was automatically created.\n")
            subscriptions.forEach { subscription ->
                sink.writeUtf8("! ${subscription.url}\n")
            }

            allowedDomains.forEach { domain ->
                sink.writeUtf8(domain.toAllowRule())
                sink.writeUtf8("\n")
                customRules++
            }

            blockedDomains.forEach { domain ->
                sink.writeUtf8(domain.toBlockRule())
                sink.writeUtf8("\n")
                customRules++
            }

            filtersSet.forEach { filter ->
                sink.writeUtf8(filter)
                sink.writeUtf8("\n")
            }
        }

        Timber.d("filters file: ${newFile.name}, rules: ${filtersSet.size}, custom rules: $customRules")

        val oldPath = coreRepository.subscriptionsPath
        coreRepository.subscriptionsPath = newFile.absolutePath

        oldPath?.let { path -> File(path).delete() }
        newFile
    }

    private suspend fun cleanOldFiles(filtersFile: File) = coroutineScope {
        val dir = appContext.getCacheDownloadDir()

        dir.listFiles { file -> file.isFile && file != filtersFile }?.forEach { file ->
            Timber.d("Removing old file: ${file.absolutePath}")
            file.delete()
        }
    }


    private fun List<DownloadedSubscription>.toFiltersSet(): Set<String> {
        val filters = mutableSetOf<String>()
        this.forEach { subscription ->
            val file = File(subscription.path)
            file.source().use { fileSource ->
                fileSource.buffer().use { source ->
                    val set = generateSequence {
                        source.readUtf8Line()
                    }.filter {
                        it.isFilter()
                    }.toSet()
                    Timber.d("Filter: ${subscription.url} - ${file.name} : rule size: ${set.size}")
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

    private suspend fun SettingsRepository.currentSettings() =
        this.settings.take(1).single()

    private suspend fun CoreRepository.currentSavedState() =
        this.data.take(1).single().lastState

    private fun Set<String>.isPeriodic(): Boolean = this.contains(UPDATE_KEY_PERIODIC_WORK)
    private fun Set<String>.isForceRefresh(): Boolean = this.contains(UPDATE_KEY_FORCE_REFRESH)

    private fun CoroutineWorker.hasReachedMaxAttempts() = runAttemptCount > RUN_ATTEMPT_MAX_COUNT

    private enum class ProgressType {
        PROGRESS, SUCCESS, FAILED
    }

    companion object {
        private const val DELAY_DEFAULT = 500L
        private const val RUN_ATTEMPT_MAX_COUNT = 4

        internal const val UPDATE_KEY_PERIODIC_WORK = "UPDATE_PERIODIC_KEY"
        internal const val UPDATE_KEY_ONESHOT_WORK = "UPDATE_ONESHOT_WORK"
        internal const val UPDATE_KEY_FORCE_REFRESH = "UPDATE_FORCE_REFRESH"
    }
}
