package org.adblockplus.adblockplussbrowser.preferences.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.ValueWrapper
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class MainPreferencesViewModel @Inject constructor(
    val settingsRepository: SettingsRepository,
    private val subscriptionsManager: SubscriptionsManager
) : ViewModel() {
    val acceptableAdsEnabled = settingsRepository.settings.map { settings ->
        settings.acceptableAdsEnabled
    }.asLiveData()

    private val _updates = MutableLiveData<ValueWrapper<Unit>>()
    val updates: LiveData<ValueWrapper<Unit>>
        get() = _updates


    fun updateSubscriptions() {
        subscriptionsManager.scheduleImmediate(force = true)
        // Post an event...
        _updates.value = ValueWrapper(Unit)
    }
}