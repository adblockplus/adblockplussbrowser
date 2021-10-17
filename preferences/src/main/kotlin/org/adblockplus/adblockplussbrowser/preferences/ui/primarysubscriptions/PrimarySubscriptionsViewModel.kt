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

package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.ui.layoutForIndex
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class PrimarySubscriptionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    val subscriptions: LiveData<List<PrimarySubscriptionsItem>> =
        settingsRepository.settings.map { settings ->
            val defaultSubscriptions = settingsRepository.getDefaultPrimarySubscriptions()
            val activeSubscriptions = settings.activePrimarySubscriptions
            val inactiveSubscriptions = defaultSubscriptions.filter { subscription ->
                activeSubscriptions.none { it.url == subscription.url }
            }
            activeSubscriptions.subscriptionItems(true) + inactiveSubscriptions.subscriptionItems(
                false
            )
        }.asLiveData()

    fun toggleActiveSubscription(subscriptionItem: PrimarySubscriptionsItem.SubscriptionItem) {
        viewModelScope.launch {
            if (subscriptionItem.active) {
                settingsRepository.removeActivePrimarySubscription(subscriptionItem.subscription)
                analyticsProvider.logEvent(AnalyticsEvent.LANGUAGE_LIST_REMOVED)
            } else {
                settingsRepository.addActivePrimarySubscription(subscriptionItem.subscription)
                analyticsProvider.logEvent(AnalyticsEvent.LANGUAGE_LIST_ADDED)
            }
        }
    }

    private fun List<Subscription>.subscriptionItems(active: Boolean): List<PrimarySubscriptionsItem> {
        val result = mutableListOf<PrimarySubscriptionsItem>()
        if (this.isNotEmpty()) {
            val headerResId =
                if (active) R.string.primary_subscriptions_active_category else R.string.primary_subscriptions_inactive_category
            result.add(PrimarySubscriptionsItem.HeaderItem(headerResId))
            this.forEachIndexed { index, subscription ->
                val layout = this.layoutForIndex(index)
                result.add(
                    (PrimarySubscriptionsItem.SubscriptionItem(
                        subscription,
                        layout,
                        active
                    ))
                )
            }
        }
        return result
    }
}