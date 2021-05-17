package org.adblockplus.adblockplussbrowser.core

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Suppress("PropertyName")
class SubscriptionsManager internal constructor(
    private val appContext: Context,
    private val settingsRepository: SettingsRepository,
    private val coreRepository: CoreRepository
) : CoroutineScope {

    override val coroutineContext = Dispatchers.Default + SupervisorJob()

    internal val _status = MutableLiveData<Status>()
    val status: LiveData<Status>
        get() = _status

    private lateinit var currentSettings: Settings

    fun initialize() {
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
                scheduleOneTime()
                schedule(currentSettings.updateConfig)
                coreRepository.setInitialized()
            } else {
                Timber.d("CORE already initialized")
            }
        }
    }

    private suspend fun listenSettingsChanges() = coroutineScope {
        launch {
            settingsRepository.settings.debounce(4000).onEach { settings ->
                Timber.d("Old settings: $currentSettings, new settings: $settings")

                if (currentSettings.changed(settings)) {
                    scheduleOneTime()
                    schedule(settings.updateConfig)
                    currentSettings = settings
                }
            }.launchIn(this)
        }
    }

    fun scheduleOneTime() {
        val request = OneTimeWorkRequestBuilder<UpdateSubscriptionsWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(UpdateConfig.ALWAYS.toNetworkType()).build())
            .addTag(KEY_ONESHOT_WORK)
            .build()

        val manager = WorkManager.getInstance(appContext)
        manager.cancelAllWorkByTag(KEY_ONESHOT_WORK)
        manager.enqueue(request)
    }

    fun schedule(updateConfig: UpdateConfig) {
        val request = PeriodicWorkRequestBuilder<UpdateSubscriptionsWorker>(30, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(updateConfig.toNetworkType()).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(KEY_PERIODIC_WORK)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        val manager = WorkManager.getInstance(appContext)
        manager.cancelAllWorkByTag(KEY_PERIODIC_WORK)
        manager.enqueue(request)
    }

    private fun UpdateConfig.toNetworkType(force: Boolean = false): NetworkType =
        if (this == UpdateConfig.WIFI_ONLY && !force) NetworkType.CONNECTED else NetworkType.NOT_ROAMING

    sealed class Status {
        data class Downloading(val current: Int) : Status()
        object Failed : Status()
        object Success : Status()
    }

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

    companion object {
        private const val KEY_PERIODIC_WORK = "PERIODIC_KEY"
        private const val KEY_ONESHOT_WORK = "ONESHOT_WORK"
    }
}