package org.adblockplus.adblockplussbrowser.core

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.extensions.currentData
import org.adblockplus.adblockplussbrowser.core.extensions.currentSettings
import org.adblockplus.adblockplussbrowser.core.extensions.periodicWorkRequestBuilder
import org.adblockplus.adblockplussbrowser.core.extensions.setBackoffCriteria
import org.adblockplus.adblockplussbrowser.core.extensions.setBackoffTime
import org.adblockplus.adblockplussbrowser.core.extensions.setInitialDelay
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounter
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker.Companion.UPDATE_KEY_FORCE_REFRESH
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker.Companion.UPDATE_KEY_ONESHOT_WORK
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker.Companion.UPDATE_KEY_PERIODIC_WORK
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Suppress("PropertyName")
@ExperimentalTime
class CoreSubscriptionsManager(
    private val appContext: Context
) : SubscriptionsManager, CoroutineScope {

    private val settingsRepository: SettingsRepository
    private val coreRepository: CoreRepository
    private val downloader: Downloader
    private val userCounter: UserCounter

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface SubscriptionsManagerEntryPoint {
        fun getSettingsRepository(): SettingsRepository
        fun getCoreRepository(): CoreRepository
        fun getDownloader(): Downloader
        fun getUserCounter(): UserCounter
    }

    override val coroutineContext = Dispatchers.Default + SupervisorJob()

    override val lastUpdate: Flow<Long>
        get() = coreRepository.data.map { data ->
            data.lastUpdated
        }

    private val _status: MutableStateFlow<SubscriptionUpdateStatus> = MutableStateFlow(SubscriptionUpdateStatus.None)
    override val status: Flow<SubscriptionUpdateStatus>
        get() = _status

    private lateinit var currentSettings: Settings

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            SubscriptionsManagerEntryPoint::class.java
        )
        settingsRepository = entryPoint.getSettingsRepository()
        coreRepository = entryPoint.getCoreRepository()
        downloader = entryPoint.getDownloader()
        userCounter = entryPoint.getUserCounter()
    }

    override fun initialize() {
        launch {
            currentSettings = settingsRepository.currentSettings()
            Timber.d("SubscriptionsManager initialization: $currentSettings")
            initialSetup()
            listenSettingsChanges()
        }
    }

    private suspend fun initialSetup() = coroutineScope {
        launch {
            val coreData = coreRepository.currentData()
            if (!coreData.configured) {
                Timber.d("Initializing CORE: Scheduling updates")
                scheduleImmediate(force = true)
                schedule(currentSettings.updateConfig)
                coreRepository.setConfigured()
            } else {
                Timber.d("CORE already initialized")
            }
        }
    }

    private suspend fun listenSettingsChanges() = coroutineScope {
        settingsRepository.settings.debounce(500).onEach { settings ->
            Timber.d("Old settings: $currentSettings, new settings: $settings")

            if (currentSettings.changed(settings)) {
                scheduleImmediate()
            }
            if (currentSettings.changedUpdateConfig(settings)) {
                schedule(settings.updateConfig)
            }
            currentSettings = settings
        }.launchIn(this)
    }

    override fun scheduleImmediate(force: Boolean) {
        val request = OneTimeWorkRequestBuilder<UpdateSubscriptionsWorker>().apply {
                setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
                setBackoffTime(Duration.minutes(1))
                addTag(UPDATE_KEY_ONESHOT_WORK)
                if (force) {
                    addTag(UPDATE_KEY_FORCE_REFRESH)
                }
            }.build()

        val manager = WorkManager.getInstance(appContext)
        // REPLACE old enqueued works
        manager.enqueueUniqueWork(UPDATE_KEY_ONESHOT_WORK, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    private fun schedule(updateConfig: UpdateConfig) {
        Timber.d("Scheduling periodic worker for: $updateConfig")
        val requestSubs = periodicWorkRequestBuilder<UpdateSubscriptionsWorker>(UPDATE_INTERVAL, FLEX_UPDATE_INTERVAL)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(updateConfig.toNetworkType()).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.minutes(1))
            .addTag(UPDATE_KEY_PERIODIC_WORK)
            .setInitialDelay(INITIAL_UPDATE_DELAY)
            .build()

        val manager = WorkManager.getInstance(appContext)
        // REPLACE old enqueued works
        manager.enqueueUniquePeriodicWork(UPDATE_KEY_PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.REPLACE, requestSubs)
        Timber.d("Scheduled %s", UPDATE_KEY_PERIODIC_WORK)
    }

    override suspend fun validateSubscription(subscription: Subscription): Boolean {
        return downloader.validate(subscription)
    }

    override suspend fun updateStatus(status: SubscriptionUpdateStatus) {
        _status.value = status
    }

    private fun UpdateConfig.toNetworkType(): NetworkType =
        if (this == UpdateConfig.WIFI_ONLY) NetworkType.UNMETERED else NetworkType.CONNECTED

    private fun Settings.changed(other: Settings): Boolean {
        return this.adblockEnabled != other.adblockEnabled ||
            this.acceptableAdsEnabled != other.acceptableAdsEnabled ||
            this.allowedDomains != other.allowedDomains ||
            this.blockedDomains != other.blockedDomains ||
            this.activePrimarySubscriptions.changed(other.activePrimarySubscriptions) ||
            this.activeOtherSubscriptions.changed(other.activeOtherSubscriptions)
    }

    private fun Settings.changedUpdateConfig(other: Settings): Boolean =
        this.updateConfig != other.updateConfig

    private fun List<Subscription>.changed(other: List<Subscription>): Boolean {
        val list = this.map { it.url }
        val otherList = other.map { it.url }

        return (list.filterNot { otherList.contains(it) } + otherList.filterNot { list.contains(it) }).isNotEmpty()
    }

    private companion object {
        private val UPDATE_INTERVAL = Duration.hours(6)
        private val FLEX_UPDATE_INTERVAL = Duration.minutes(30)
        private val INITIAL_UPDATE_DELAY = Duration.hours(6)
    }
}