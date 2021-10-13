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

package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import androidx.annotation.StringRes
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.preferences.ui.GroupItemLayout
import java.io.Serializable

internal sealed class OtherSubscriptionsItem(val id: String) {

    data class HeaderItem(@StringRes val titleResId: Int) : OtherSubscriptionsItem(titleResId.toString())

    data class DefaultItem(val subscription: Subscription, val layout: GroupItemLayout, val active: Boolean) :
        OtherSubscriptionsItem(subscription.url)

    data class CustomItem(val subscription: Subscription, val layout: GroupItemLayout) :
        OtherSubscriptionsItem(subscription.url), Serializable
}