package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.interactor.SubscriptionsInteractor
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.ui.layoutForIndex
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class OtherSubscriptionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val subscriptionsInteractor: SubscriptionsInteractor
) : ViewModel() {

    /*val subscriptions: LiveData<List<OtherSubscriptionsItem>> = settingsRepository.settings.map { settings ->
        val defaultSubscriptions = settingsRepository.getDefaultOtherSubscriptions()
        val activeSubscriptions = settings.activeOtherSubscriptions
        val customSubscriptions = activeSubscriptions.filter { subscription ->
            defaultSubscriptions.none { it.url == subscription.url }
        }
        defaultSubscriptions.defaultItems(activeSubscriptions) + customSubscriptions.customItems()
    }.asLiveData()*/
    val subscriptions: LiveData<List<OtherSubscriptionsItem>> = subscriptionsInteractor.otherSubscriptions.map { subscriptions ->
        val defaultSubscription = subscriptions.filter { it.type == SubscriptionsInteractor.SubscriptionType.OTHER }
        val customSubscription = subscriptions.filter { it.type == SubscriptionsInteractor.SubscriptionType.CUSTOM }

        defaultSubscription.defaultItems() + customSubscription.customItems()
    }.asLiveData()

    fun toggleActiveSubscription(defaultItem: OtherSubscriptionsItem.DefaultItem) {
        viewModelScope.launch {
            if (defaultItem.active) {
                settingsRepository.removeActiveOtherSubscription(defaultItem.subscription)
            } else {
                settingsRepository.addActiveOtherSubscription(defaultItem.subscription)
            }
        }
    }

    fun addCustomUrl(url: String) {
        viewModelScope.launch {
            val subscription = Subscription(url, url, 0L)
            settingsRepository.addActiveOtherSubscription(subscription)
        }
    }

    fun removeSubscription(customItem: OtherSubscriptionsItem.CustomItem) {
        viewModelScope.launch {
            settingsRepository.removeActiveOtherSubscription(customItem.subscription)
        }
    }

    private fun List<SubscriptionsInteractor.SubscriptionInfo>.customItems(): List<OtherSubscriptionsItem> {
        val result = mutableListOf<OtherSubscriptionsItem>()
        if (this.isNotEmpty()) {
            result.add(OtherSubscriptionsItem.HeaderItem(R.string.other_subscriptions_custom_category))
            this.forEachIndexed { index, subscription ->
                val layout = this.layoutForIndex(index)
                result.add(OtherSubscriptionsItem.CustomItem(subscription.subscription, subscription.lastUpdated, layout))
            }
        }
        return result
    }

    private fun List<SubscriptionsInteractor.SubscriptionInfo>.defaultItems(): List<OtherSubscriptionsItem> {
        val result = mutableListOf<OtherSubscriptionsItem>()
        if (this.isNotEmpty()) {
            result.add(OtherSubscriptionsItem.HeaderItem(R.string.other_subscriptions_default_category))
            this.forEachIndexed { index, subscription ->
                val layout = this.layoutForIndex(index)
                result.add(OtherSubscriptionsItem.DefaultItem(subscription.subscription,
                    subscription.lastUpdated, layout, subscription.active))
            }
        }
        return result
    }
}