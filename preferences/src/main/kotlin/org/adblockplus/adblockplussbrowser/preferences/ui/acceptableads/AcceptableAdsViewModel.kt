package org.adblockplus.adblockplussbrowser.preferences.ui.acceptableads

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class AcceptableAdsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val enabled = settingsRepository.settings.map { settings ->
        Log.d("ViewModel", "Acceptable Ads: ${settings.acceptableAdsEnabled}")
        settings.acceptableAdsEnabled
    }.asLiveData()

    fun enableAcceptableAds() {
        viewModelScope.launch {
            settingsRepository.setAcceptableAdsEnabled(true)
        }
    }

    fun disableAcceptableAds() {
        viewModelScope.launch {
            settingsRepository.setAcceptableAdsEnabled(false)
        }
    }
}