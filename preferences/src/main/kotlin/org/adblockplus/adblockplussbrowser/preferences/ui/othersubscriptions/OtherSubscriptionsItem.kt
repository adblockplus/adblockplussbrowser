package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import androidx.annotation.StringRes
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.preferences.ui.GroupItemLayout

internal sealed class OtherSubscriptionsItem(val id: String) {

    data class HeaderItem(@StringRes val titleResId: Int) : OtherSubscriptionsItem(titleResId.toString())

    data class DefaultItem(val subscription: Subscription, val layout: GroupItemLayout, val active: Boolean) :
        OtherSubscriptionsItem(subscription.url)

    data class CustomItem(val subscription: Subscription, val layout: GroupItemLayout) :
        OtherSubscriptionsItem(subscription.url)
}