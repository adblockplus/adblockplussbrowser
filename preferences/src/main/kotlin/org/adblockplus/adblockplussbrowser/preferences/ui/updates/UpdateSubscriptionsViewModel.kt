/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.adblockplussbrowser.preferences.ui.updates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig
import timber.log.Timber
import javax.inject.Inject
import org.adblockplus.adblockplussbrowser.base.BuildConfig

@HiltViewModel
class UpdateSubscriptionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val subscriptionsManager: SubscriptionsManager
) : ViewModel() {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    // Spinner Adapter positions
    val updateType = settingsRepository.settings.map { settings ->
        settings.updateConfig.toUpdateConfigType()
    }.asLiveData()

    val updateStatus: LiveData<SubscriptionUpdateStatus> = subscriptionsManager.status.asLiveData()

    val lastUpdate = subscriptionsManager.lastUpdate.asLiveData()

    fun setUpdateConfigType(configType: UpdateConfigType) {
        viewModelScope.launch {
            settingsRepository.setUpdateConfig(configType.toUpdateConfig())
            if (configType == UpdateConfigType.UPDATE_WIFI_ONLY) {
                analyticsProvider.logEvent(AnalyticsEvent.AUTOMATIC_UPDATES_WIFI)
            } else if (configType == UpdateConfigType.UPDATE_ALWAYS) {
                analyticsProvider.logEvent(AnalyticsEvent.AUTOMATIC_UPDATES_ALWAYS)
            }
        }
    }

    fun updateSubscriptions() {
        if (updateStatus.value is SubscriptionUpdateStatus.Progress) {
            Timber.d("Update is already in progress")
            return
        }
        subscriptionsManager.scheduleImmediate(force = true)
        analyticsProvider.logEvent(AnalyticsEvent.MANUAL_UPDATE)
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