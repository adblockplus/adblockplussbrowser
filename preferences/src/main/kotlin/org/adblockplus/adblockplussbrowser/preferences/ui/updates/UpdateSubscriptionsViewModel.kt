package org.adblockplus.adblockplussbrowser.preferences.ui.updates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.ValueWrapper
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig
import javax.inject.Inject

@HiltViewModel
class UpdateSubscriptionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val subscriptionsManager: SubscriptionsManager
) : ViewModel() {

    // Spinner Adapter positions
    val updateType = settingsRepository.settings.map { settings ->
        settings.updateConfig.toUpdateConfigType()
    }.asLiveData()

    private val _updates = MutableLiveData<ValueWrapper<Unit>>()
    val updates: LiveData<ValueWrapper<Unit>>
        get() = _updates

    val lastUpdate = subscriptionsManager.lastUpdate.asLiveData()

    fun setUpdateConfigType(configType: UpdateConfigType) {
        viewModelScope.launch {
            settingsRepository.setUpdateConfig(configType.toUpdateConfig())
        }
    }

    fun updateSubscriptions() {
        subscriptionsManager.scheduleImmediate(force = true)
        // Post an event...
        _updates.value = ValueWrapper(Unit)
    }

    enum class UpdateConfigType {
        UPDATE_WIFI_ONLY,
        UPDATE_ALWAYS
    }

    private fun UpdateConfig.toUpdateConfigType(): UpdateConfigType =
        if (this == UpdateConfig.WIFI_ONLY) UpdateConfigType.UPDATE_WIFI_ONLY else UpdateConfigType.UPDATE_ALWAYS

    private fun UpdateConfigType.toUpdateConfig(): UpdateConfig =
        if (this == UpdateConfigType.UPDATE_WIFI_ONLY) UpdateConfig.WIFI_ONLY else UpdateConfig.ALWAYS
}