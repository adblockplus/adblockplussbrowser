package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.preferences.ui.PrimarySubscriptionsItem

internal class PrimarySubscriptionsViewModel: ViewModel() {

    val primarySubscriptions: LiveData<List<PrimarySubscriptionsItem>> = MutableLiveData()


    init {

        val subscriptions = listOf<PrimarySubscriptionsItem>(
            PrimarySubscriptionsItem.HeaderItem("Active for websites in..."),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", false),
            PrimarySubscriptionsItem.HeaderItem("Active for websites in..."),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true),
            PrimarySubscriptionsItem.SubscriptionItem(Subscription("test.com", "Test Sub", 0, listOf()), "updateStr", true)
        )
        (primarySubscriptions as MutableLiveData).value = subscriptions

    }

}