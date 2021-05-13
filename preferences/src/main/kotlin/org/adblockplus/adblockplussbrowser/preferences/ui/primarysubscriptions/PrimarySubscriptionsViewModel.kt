package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.preferences.ui.PrimarySubscriptionsItem
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class PrimarySubscriptionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val primarySubscriptions: LiveData<List<PrimarySubscriptionsItem>> = MutableLiveData()

    init {

        val subscriptions = listOf<PrimarySubscriptionsItem>(
            PrimarySubscriptionsItem.HeaderItem("Active for websites in..."),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                false
            ),
            PrimarySubscriptionsItem.HeaderItem("Active for websites in..."),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            ),
            PrimarySubscriptionsItem.SubscriptionItem(
                Subscription("test.com", "Test Sub", 0),
                "updateStr",
                true
            )
        )
        (primarySubscriptions as MutableLiveData).value = subscriptions

    }

}