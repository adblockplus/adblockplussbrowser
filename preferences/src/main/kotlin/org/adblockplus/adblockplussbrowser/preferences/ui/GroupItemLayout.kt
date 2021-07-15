package org.adblockplus.adblockplussbrowser.preferences.ui

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription

enum class GroupItemLayout {
    SINGLE,
    FIRST,
    CENTER,
    LAST
}

internal fun List<Any>.layoutForIndex(index: Int): GroupItemLayout =
    if(this.size == 1) {
        GroupItemLayout.SINGLE
    } else {
        when (index) {
            0 -> GroupItemLayout.FIRST
            this.lastIndex -> GroupItemLayout.LAST
            else -> GroupItemLayout.CENTER
        }
    }