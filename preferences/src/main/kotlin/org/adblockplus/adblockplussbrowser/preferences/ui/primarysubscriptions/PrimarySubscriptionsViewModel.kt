package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.ui.GroupItemLayout
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class PrimarySubscriptionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val primarySubscriptions: LiveData<List<PrimarySubscriptionsItem>> = settingsRepository.settings.map { settings ->
        val defaultSubscriptions = settingsRepository.getDefaultPrimarySubscriptions()
        val activeSubscriptions = settings.activePrimarySubscriptions
        val inactiveSubscriptions = defaultSubscriptions.filter { subscription ->
            activeSubscriptions.none { it.url == subscription.url }
        }
        activeSubscriptions.subscriptionItems(true) + inactiveSubscriptions.subscriptionItems(false)
    }.asLiveData()

    fun toggleActivePrimarySubscription(primarySubscriptionsItem: PrimarySubscriptionsItem.SubscriptionItem) {
        viewModelScope.launch {
            if (primarySubscriptionsItem.active) {
                settingsRepository.removeActivePrimarySubscription(primarySubscriptionsItem.subscription)
            } else {
                settingsRepository.addActivePrimarySubscription(primarySubscriptionsItem.subscription)
            }
        }
    }

    private fun List<Subscription>.subscriptionItems(active: Boolean): List<PrimarySubscriptionsItem> {
        val result = mutableListOf<PrimarySubscriptionsItem>()
        if (this.isNotEmpty()) {
            val headerResId =
                if (active) R.string.primary_subscriptions_active_category else R.string.primary_subscriptions_inactive_category
            result.add(PrimarySubscriptionsItem.HeaderItem(headerResId))
            if (this.size == 1) {
                result.add((PrimarySubscriptionsItem.SubscriptionItem(this.first(), GroupItemLayout.SINGLE, active)))
            } else {
                this.forEachIndexed { index, subscription ->
                    val layout = this.layoutForIndex(index)
                    result.add((PrimarySubscriptionsItem.SubscriptionItem(subscription, layout, active)))
                }
            }
        }
        return result
    }

    private fun List<Subscription>.layoutForIndex(index: Int): GroupItemLayout =
        when (index) {
            0 -> GroupItemLayout.FIRST
            this.lastIndex -> GroupItemLayout.LAST
            else -> GroupItemLayout.CENTER
        }
}