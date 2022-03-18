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
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.preferences.R
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

    private var addOtherSubscriptionsCount: Int = 0

    val subscriptions: LiveData<List<OtherSubscriptionsItem>> = settingsRepository.settings.map { settings ->
        val defaultSubscriptions = settingsRepository.getDefaultOtherSubscriptions()
        val activeSubscriptions = settings.activeOtherSubscriptions
        val customSubscriptions = activeSubscriptions.filter { subscription ->
            defaultSubscriptions.none { it.url == subscription.url }
        }
        defaultSubscriptions.defaultItems(activeSubscriptions) + customSubscriptions.customItems()
    }.asLiveData()

    private val _uiState = MutableStateFlow<UiState>(UiState.Done)
    val uiState = _uiState.asLiveData()

    val nonRemovableItemCount: Int
        get() {
        val defaultOtherListCount = subscriptions.value!!.count { it is OtherSubscriptionsItem.DefaultItem }
        val headerCount = subscriptions.value!!.count { it is OtherSubscriptionsItem.HeaderItem }
        return defaultOtherListCount + headerCount
    }

    fun toggleActiveSubscription(defaultItem: OtherSubscriptionsItem.DefaultItem) {
        viewModelScope.launch {
            if (defaultItem.active) {
                settingsRepository.removeActiveOtherSubscription(defaultItem.subscription)
                if (defaultItem.subscription.title ==
                    settingsRepository.getAdditionalTrackingSubscription().title) {
                    analyticsProvider.logEvent(AnalyticsEvent.DISABLE_TRACKING_OFF)
                } else if (defaultItem.subscription.title ==
                    settingsRepository.getSocialMediaTrackingSubscription().title) {
                    analyticsProvider.logEvent(AnalyticsEvent.SOCIAL_MEDIA_BUTTONS_OFF)
                }
            } else {
                settingsRepository.addActiveOtherSubscription(defaultItem.subscription)
                if (defaultItem.subscription.title ==
                    settingsRepository.getAdditionalTrackingSubscription().title) {
                    analyticsProvider.logEvent(AnalyticsEvent.DISABLE_TRACKING_ON)
                } else if (defaultItem.subscription.title ==
                    settingsRepository.getSocialMediaTrackingSubscription().title) {
                    analyticsProvider.logEvent(AnalyticsEvent.SOCIAL_MEDIA_BUTTONS_ON)
                }
            }
        }
    }

    fun addCustomUrl(url: String) {
        viewModelScope.launch {
            val subscription = Subscription(url, url, 0L)
            _uiState.value = UiState.Loading
            addOtherSubscriptionsCount += 1
            if (!subscriptionManager.validateSubscription(subscription)) {
                _uiState.value = UiState.Error
                addOtherSubscriptionsCount -= 1
                delay(100)
            } else {
                settingsRepository.addActiveOtherSubscription(subscription)
                addOtherSubscriptionsCount -= 1
                analyticsProvider.logEvent(AnalyticsEvent.CUSTOM_FILTER_LIST_ADDED)
            }
            if (addOtherSubscriptionsCount == 0) {
                _uiState.value = UiState.Done
            }
        }
    }

    fun removeSubscription(customItem: OtherSubscriptionsItem.CustomItem) {
        viewModelScope.launch {
            settingsRepository.removeActiveOtherSubscription(customItem.subscription)
            analyticsProvider.logEvent(AnalyticsEvent.CUSTOM_FILTER_LIST_REMOVED)
        }
    }

    private fun List<Subscription>.defaultItems(activeSubscriptions: List<Subscription>): List<OtherSubscriptionsItem> {
        val result = mutableListOf<OtherSubscriptionsItem>()
        if (this.isNotEmpty()) {
            result.add(OtherSubscriptionsItem.HeaderItem(R.string.other_subscriptions_default_category))
            this.forEachIndexed { index, subscription ->
                val activeSubscription = activeSubscriptions.find { it.url == subscription.url }
                val active = activeSubscription != null
                val layout = this.layoutForIndex(index)
                result.add(OtherSubscriptionsItem.DefaultItem(activeSubscription?: subscription, layout, active))
            }
        }
        return result
    }

    private fun List<Subscription>.customItems(): List<OtherSubscriptionsItem> {
        val result = mutableListOf<OtherSubscriptionsItem>()
        if (this.isNotEmpty()) {
            result.add(OtherSubscriptionsItem.HeaderItem(R.string.other_subscriptions_custom_category))
            this.forEachIndexed { index, subscription ->
                val layout = this.layoutForIndex(index)
                result.add(OtherSubscriptionsItem.CustomItem(subscription, layout))
            }
        }
        return result
    }

}