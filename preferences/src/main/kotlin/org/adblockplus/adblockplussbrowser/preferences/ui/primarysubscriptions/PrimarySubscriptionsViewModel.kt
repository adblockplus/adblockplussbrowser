package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionInfo
import org.adblockplus.adblockplussbrowser.core.interactor.SubscriptionsInteractor
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.ui.layoutForIndex
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class PrimarySubscriptionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    subscriptionsInteractor: SubscriptionsInteractor
) : ViewModel() {

    val subscriptions: LiveData<List<PrimarySubscriptionsItem>> = subscriptionsInteractor.subscriptions.map { subscriptions ->
        subscriptions.subscriptionItems()
    }.asLiveData()

    fun toggleActiveSubscription(subscriptionItem: PrimarySubscriptionsItem.SubscriptionItem) {
        viewModelScope.launch {
            if (subscriptionItem.active) {
                settingsRepository.removeActivePrimarySubscription(subscriptionItem.subscription)
            } else {
                settingsRepository.addActivePrimarySubscription(subscriptionItem.subscription)
            }
        }
    }

    private fun List<SubscriptionInfo>.subscriptionItems(): List<PrimarySubscriptionsItem> {
        val result = mutableListOf<PrimarySubscriptionsItem>()
        val active = this.filter { it.active }
        if (active.isNotEmpty()) {
            result.add(PrimarySubscriptionsItem.HeaderItem(R.string.primary_subscriptions_active_category))
            active.forEachIndexed { index, subscription ->
                val layout = this.layoutForIndex(index)
                result.add((PrimarySubscriptionsItem.SubscriptionItem(subscription.subscription, layout, true, subscription.lastUpdated)))            }
        }
        val inactive = this.filter { !it.active }
        if (inactive.isNotEmpty()) {
            result.add(PrimarySubscriptionsItem.HeaderItem(R.string.primary_subscriptions_inactive_category))
            inactive.forEachIndexed { index, subscription ->
                val layout = this.layoutForIndex(index)
                result.add((PrimarySubscriptionsItem.SubscriptionItem(subscription.subscription, layout, false, subscription.lastUpdated)))
            }
        }
        return result
    }
}