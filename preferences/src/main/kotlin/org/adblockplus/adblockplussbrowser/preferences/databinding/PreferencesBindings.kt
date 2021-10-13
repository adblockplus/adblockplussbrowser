/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.adblockplussbrowser.preferences.databinding

import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.ui.GroupItemLayout

@BindingAdapter("lastUpdate")
internal fun bindLastUpdate(textView: TextView, timestamp: Long) {
    val context = textView.context
    val value = if (timestamp > 0) {
        DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME)
    } else {
        context.getString(R.string.subscription_last_update_never)
    }
    textView.text = context.getString(R.string.subscription_last_update, value)
}

@BindingAdapter("groupItemBackground")
internal fun bindGroupItemBackground(view: View, groupItemLayout: GroupItemLayout) {
    val backgroundResId = when (groupItemLayout) {
        GroupItemLayout.SINGLE -> R.drawable.preferences_group_bg_single
        GroupItemLayout.FIRST -> R.drawable.preferences_group_bg_first
        GroupItemLayout.CENTER -> R.drawable.preferences_group_bg_center
        GroupItemLayout.LAST -> R.drawable.preferences_group_bg_last
    }
    view.setBackgroundResource(backgroundResId)
}

@BindingAdapter("groupItemDividerVisibility")
internal fun bindGroupItemDivider(view: View, groupItemLayout: GroupItemLayout) {
    val visibility = when (groupItemLayout) {
        GroupItemLayout.FIRST, GroupItemLayout.CENTER -> View.VISIBLE
        GroupItemLayout.SINGLE, GroupItemLayout.LAST -> View.GONE
    }
    view.visibility = visibility
}