package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.ui.PrimarySubscriptionsItem
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class PrimarySubscriptionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val primarySubscriptions: LiveData<List<PrimarySubscriptionsItem>> = settingsRepository.settings.map { settings ->
        val result = mutableListOf<PrimarySubscriptionsItem>()
        val activeSubscriptions = mutableListOf<PrimarySubscriptionsItem.SubscriptionItem>()
        val inactiveSubscriptions = mutableListOf<PrimarySubscriptionsItem.SubscriptionItem>()
        val defaultSubscriptions =  settingsRepository.getDefaultPrimarySubscriptions()
        android.util.Log.i("ABPTEST", "activePrimarySubscriptions ${settings.activePrimarySubscriptions.size} ${settings.acceptableAdsEnabled}")
        settings.activePrimarySubscriptions.forEach { subscription ->
            activeSubscriptions.add(PrimarySubscriptionsItem.SubscriptionItem(subscription, true))
        }
        defaultSubscriptions.forEach { subscription ->
            if(settings.activePrimarySubscriptions.find { it.url == subscription.url } == null) {
                inactiveSubscriptions.add(PrimarySubscriptionsItem.SubscriptionItem(subscription, false))
            }
        }
        result.add(PrimarySubscriptionsItem.HeaderItem(R.string.primary_subscriptions_active_category))
        result.addAll(activeSubscriptions)
        result.add(PrimarySubscriptionsItem.HeaderItem(R.string.primary_subscriptions_inactive_category))
        result.addAll(inactiveSubscriptions)
        result
    }.asLiveData()
}