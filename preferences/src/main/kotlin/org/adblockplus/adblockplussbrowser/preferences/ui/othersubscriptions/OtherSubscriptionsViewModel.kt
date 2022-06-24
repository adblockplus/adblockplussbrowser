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

package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.CustomSubscriptionType.FROM_URL
import org.adblockplus.adblockplussbrowser.base.data.model.CustomSubscriptionType.LOCAL_FILE
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.preferences.ui.layoutForIndex
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class OtherSubscriptionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val subscriptionManager: SubscriptionsManager
) : ViewModel() {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 101

    val activeSubscriptions: LiveData<List<Subscription>> =
        settingsRepository.settings.map { settings ->
            settings.activeOtherSubscriptions
        }.asLiveData()

    val customSubscriptions: LiveData<List<OtherSubscriptionsItem.CustomItem>> =
        settingsRepository.settings.map { settings ->
            val defaultSubscriptions = settingsRepository.getDefaultOtherSubscriptions()
            val activeSubscriptions = settings.activeOtherSubscriptions
            val customSubscriptions = activeSubscriptions.filter { subscription ->
                defaultSubscriptions.none { it.url == subscription.url }
            }
            customSubscriptions.customItems()
        }.asLiveData()

    val additionalTrackingSubscription: LiveData<Subscription> = settingsRepository.settings.map {
        settingsRepository.getAdditionalTrackingSubscription()
    }.asLiveData()

    val socialMediaTrackingSubscription: LiveData<Subscription> = settingsRepository.settings.map {
        settingsRepository.getSocialMediaTrackingSubscription()
    }.asLiveData()

    val blockAdditionalTracking = MutableLiveData<Boolean?>().apply { value = false }
    val blockSocialMediaTracking = MutableLiveData<Boolean?>().apply { value = false }
    val additionalTrackingLastUpdate = MutableLiveData<Long>().apply { value = 0L }
    val socialMediaIconsTrackingLastUpdate = MutableLiveData<Long>().apply { value = 0L }

    private val _uiState = MutableStateFlow<UiState>(UiState.Done)
    val uiState = _uiState.asLiveData()

    private val addOtherSubscriptionsCount = MutableLiveData<Int>().apply { value = 0 }

    fun toggleAdditionalTracking() {
        blockAdditionalTracking.apply { value?.let { it -> value = !it } }
        handleDefaultSubscriptions(
            blockAdditionalTracking.value!!, additionalTrackingSubscription.value!!,
            AnalyticsEvent.DISABLE_TRACKING_OFF, AnalyticsEvent.DISABLE_TRACKING_ON
        )
    }

    fun toggleSocialMediaTracking() {
        blockSocialMediaTracking.apply { value?.let { it -> value = !it } }
        handleDefaultSubscriptions(
            blockSocialMediaTracking.value!!, socialMediaTrackingSubscription.value!!,
            AnalyticsEvent.SOCIAL_MEDIA_BUTTONS_OFF, AnalyticsEvent.SOCIAL_MEDIA_BUTTONS_ON
        )
    }

    private fun handleDefaultSubscriptions(
        checkboxSelected: Boolean, subscription: Subscription,
        analyticsEventOnSelected: AnalyticsEvent,
        analyticsEventOnDeselected: AnalyticsEvent
    ) {
        if (checkboxSelected) {
            viewModelScope.launch {
                settingsRepository.addActiveOtherSubscription(subscription)
            }
            analyticsProvider.logEvent(analyticsEventOnSelected)
        } else {
            viewModelScope.launch {
                settingsRepository.removeActiveOtherSubscription(subscription)
            }
            analyticsProvider.logEvent(analyticsEventOnDeselected)
        }

    }

    fun addCustomUrl(url: String) {
        viewModelScope.launch {
            val subscription = Subscription(url, url,0L, FROM_URL)
            _uiState.value = UiState.Loading
            addOtherSubscriptionsCount.apply { value = value?.plus(1) }
            if (!subscriptionManager.validateSubscription(subscription)) {
                _uiState.value = UiState.Error
                delay(100)
            } else {
                settingsRepository.addActiveOtherSubscription(subscription)
                analyticsProvider.logEvent(AnalyticsEvent.CUSTOM_FILTER_LIST_ADDED)
            }
            finishAddingCustomSubscription()
        }
    }

    fun addCustomFilterFile(url: String, title: String) {
        viewModelScope.launch {
            val subscription = Subscription(url, title, 0L, LOCAL_FILE)
            _uiState.value = UiState.Loading
            addOtherSubscriptionsCount.apply { value = value?.plus(1) }
            settingsRepository.addActiveOtherSubscription(subscription)
            analyticsProvider.logEvent(AnalyticsEvent.CUSTOM_FILTER_LIST_ADDED)
            finishAddingCustomSubscription()
        }
    }

    fun removeSubscription(customItem: OtherSubscriptionsItem.CustomItem) {
        viewModelScope.launch {
            settingsRepository.removeActiveOtherSubscription(customItem.subscription)
            analyticsProvider.logEvent(AnalyticsEvent.CUSTOM_FILTER_LIST_REMOVED)
        }
    }

    private fun List<Subscription>.customItems(): List<OtherSubscriptionsItem.CustomItem> {
        val result = mutableListOf<OtherSubscriptionsItem.CustomItem>()
        if (this.isNotEmpty()) {
            this.forEachIndexed { index, subscription ->
                val layout = this.layoutForIndex(index)
                result.add(OtherSubscriptionsItem.CustomItem(subscription, layout))
            }
        }
        return result
    }

    private fun finishAddingCustomSubscription() {
        addOtherSubscriptionsCount.apply { value = value?.minus(1) }
        if (addOtherSubscriptionsCount.value == 0) {
            _uiState.value = UiState.Done
        }
    }

}
