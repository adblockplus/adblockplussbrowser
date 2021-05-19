package org.adblockplus.adblockplussbrowser.preferences.ui

import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionInfo

internal enum class GroupItemLayout {
    SINGLE,
    FIRST,
    CENTER,
    LAST
}

internal fun List<SubscriptionInfo>.layoutForIndex(index: Int): GroupItemLayout =
    if(this.size == 1) {
        GroupItemLayout.SINGLE
    } else {
        when (index) {
            0 -> GroupItemLayout.FIRST
            this.lastIndex -> GroupItemLayout.LAST
            else -> GroupItemLayout.CENTER
        }
    }