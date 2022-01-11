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

package org.adblockplus.adblockplussbrowser.app.ui


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences.Companion.isFilterRequestExpired
import org.adblockplus.adblockplussbrowser.base.data.prefs.AppPreferences
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
