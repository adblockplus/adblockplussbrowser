package org.adblockplus.adblockplussbrowser.app.ui

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.app.data.prefs.AppPreferences
import org.adblockplus.adblockplussbrowser.app.ui.LauncherViewModel.Companion.isFilterRequestExpired
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val subscriptionsManager: SubscriptionsManager,
    private val settingsRepository: SettingsRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    val updateStatus: LiveData<SubscriptionUpdateStatus> = subscriptionsManager.status.asLiveData()

    fun updateSubscriptions() {
        subscriptionsManager.scheduleImmediate(force = true)
    }

    fun sendAudienceAAEvent() {
        viewModelScope.launch {
            val acceptableAdsEnabled = settingsRepository.settings.map { settings ->
                settings.acceptableAdsEnabled
            }.first()
            if (acceptableAdsEnabled)
                analyticsProvider.logEvent(AnalyticsEvent.AUDIENCE_AA_ENABLED)
            else
                analyticsProvider.logEvent(AnalyticsEvent.AUDIENCE_AA_DISABLED)
        }
    }

    fun fetchAdblockActivationStatus(): MutableLiveData<Boolean> {
        val isAdblockActivated = MutableLiveData<Boolean>()
        viewModelScope.launch {
            appPreferences.lastFilterListRequest.map {
                it != 0L && !isFilterRequestExpired(it)
            }.collect {
                isAdblockActivated.postValue(it)
            }
        }
        return isAdblockActivated
    }
}