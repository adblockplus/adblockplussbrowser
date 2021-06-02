package org.adblockplus.adblockplussbrowser.core

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker.Companion.KEY_FORCE_REFRESH
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker.Companion.KEY_ONESHOT_WORK
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker.Companion.KEY_PERIODIC_WORK
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Suppress("PropertyName")
class CoreSubscriptionsManager(
    private val appContext: Context
) : SubscriptionsManager, CoroutineScope {

    private val settingsRepository: SettingsRepository
    private val coreRepository: CoreRepository
    private val downloader: Downloader

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface SubscriptionsManagerEntryPoint {
        fun getSettingsRepository(): SettingsRepository
        fun getCoreRepository(): CoreRepository
        fun getDownloader(): Downloader
    }

    override val coroutineContext = Dispatchers.Default + SupervisorJob()

    private val _status = MutableLiveData<SubscriptionsManager.Status>()
    override val status: LiveData<SubscriptionsManager.Status>
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
    }

    override fun initialize() {
        launch {
            currentSettings = settingsRepository.settings.take(1).single()
            Timber.d("SubscriptionsManager initialization: $currentSettings")
            initialSetup()
            listenSettingsChanges()
        }
    }

    private suspend fun initialSetup() = coroutineScope {
        launch {
            val coreData = coreRepository.data.take(1).single()
            if (!coreData.configured) {
                Timber.d("Initializing CORE: Scheduling updates")
                scheduleImmediate()
                coreRepository.setConfigured()
            } else {
                Timber.d("CORE already initialized")
            }
        }
    }

    private suspend fun listenSettingsChanges() = coroutineScope {
        launch {
            settingsRepository.settings.debounce(500).onEach { settings ->
                Timber.d("Old settings: $currentSettings, new settings: $settings")

                if (currentSettings.changed(settings)) {
                    currentSettings = settings
                    scheduleImmediate()
                }
            }.launchIn(this)
        }
    }

    override fun scheduleImmediate(force: Boolean) {
        // reschedule periodic downloader
        // Make sure we don't do a periodic update right after a manual one.
        schedule(currentSettings.updateConfig)

        val request = OneTimeWorkRequestBuilder<UpdateSubscriptionsWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(UpdateConfig.ALWAYS.toNetworkType()).build()).apply {
                addTag(KEY_ONESHOT_WORK)
                if (force) {
                    addTag(KEY_FORCE_REFRESH)
                }
            }
            .build()

        val manager = WorkManager.getInstance(appContext)

        // REPLACE old enqueued works
        manager.enqueueUniqueWork(KEY_ONESHOT_WORK, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    private fun schedule(updateConfig: UpdateConfig) {
        val request = PeriodicWorkRequestBuilder<UpdateSubscriptionsWorker>(30, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(updateConfig.toNetworkType()).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(KEY_PERIODIC_WORK)
            .setInitialDelay(30, TimeUnit.MINUTES)
            .build()

        val manager = WorkManager.getInstance(appContext)
        // REPLACE old enqueued works
        manager.enqueueUniquePeriodicWork(KEY_PERIODIC_WORK, ExistingPeriodicWorkPolicy.REPLACE, request)
    }

    override suspend fun validateSubscription(subscription: Subscription): Boolean {
        return downloader.validate(subscription)
    }

    override fun updateStatus(status: SubscriptionsManager.Status) {
        _status.postValue(status)
    }

    private fun UpdateConfig.toNetworkType(force: Boolean = false): NetworkType =
        if (this == UpdateConfig.WIFI_ONLY && !force) NetworkType.CONNECTED else NetworkType.NOT_ROAMING

    private fun Settings.changed(other: Settings): Boolean {
        return this.adblockEnabled != other.adblockEnabled ||
            this.acceptableAdsEnabled != other.acceptableAdsEnabled ||
            this.allowedDomains != other.allowedDomains ||
            this.blockedDomains != other.blockedDomains ||
            this.activePrimarySubscriptions.changed(other.activePrimarySubscriptions) ||
            this.activeOtherSubscriptions.changed(other.activeOtherSubscriptions) ||
            this.updateConfig != other.updateConfig
    }

    private fun List<Subscription>.changed(other: List<Subscription>): Boolean {
        val list = this.map { it.url }
        val otherList = other.map { it.url }

        return (list.filterNot { otherList.contains(it) } + otherList.filterNot { list.contains(it) }).isNotEmpty()
    }
}