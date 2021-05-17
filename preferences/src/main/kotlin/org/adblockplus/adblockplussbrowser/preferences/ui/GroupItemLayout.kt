package org.adblockplus.adblockplussbrowser.preferences.ui

import org.adblockplus.adblockplussbrowser.core.interactor.SubscriptionsInteractor

internal enum class GroupItemLayout {
    SINGLE,
    FIRST,
    CENTER,
    LAST
}

/*internal fun List<Subscription>.layoutForIndex(index: Int): GroupItemLayout =
    if(this.size == 1) {
        GroupItemLayout.SINGLE
    } else {
        when (index) {
            0 -> GroupItemLayout.FIRST
            this.lastIndex -> GroupItemLayout.LAST
            else -> GroupItemLayout.CENTER
        }
    }*/

internal fun List<SubscriptionsInteractor.SubscriptionInfo>.layoutForIndex(index: Int): GroupItemLayout =
    if(this.size == 1) {
        GroupItemLayout.SINGLE
    } else {
        when (index) {
            0 -> GroupItemLayout.FIRST
            this.lastIndex -> GroupItemLayout.LAST
            else -> GroupItemLayout.CENTER
        }
    }