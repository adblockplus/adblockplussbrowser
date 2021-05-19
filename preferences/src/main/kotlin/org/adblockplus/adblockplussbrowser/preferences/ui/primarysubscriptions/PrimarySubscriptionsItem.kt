package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.annotation.StringRes
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.preferences.ui.GroupItemLayout

internal sealed class PrimarySubscriptionsItem(val id: String) {

    data class HeaderItem(@StringRes val titleResId: Int) : PrimarySubscriptionsItem(titleResId.toString())

    data class SubscriptionItem(
        val subscription: Subscription,
        val layout: GroupItemLayout,
        val active: Boolean,
        val lastUpdate: Long
        ) : PrimarySubscriptionsItem(subscription.url)
}