package org.adblockplus.adblockplussbrowser.preferences.ui.acceptableads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class AcceptableAdsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    val enabled = settingsRepository.settings.map { settings ->
        Timber.d("Acceptable Ads: ${settings.acceptableAdsEnabled}")
        settings.acceptableAdsEnabled
    }.asLiveData()

    fun enableAcceptableAds() {
        viewModelScope.launch {
            analyticsProvider.setUserProperty(AnalyticsUserProperty.IS_AA_ENABLED, true.toString())
            settingsRepository.setAcceptableAdsEnabled(true)
            analyticsProvider.logEvent(AnalyticsEvent.AA_ON)
        }
    }

    fun disableAcceptableAds() {
        viewModelScope.launch {
            analyticsProvider.setUserProperty(AnalyticsUserProperty.IS_AA_ENABLED, false.toString())
            settingsRepository.setAcceptableAdsEnabled(false)
            analyticsProvider.logEvent(AnalyticsEvent.AA_OFF)
        }
    }
}