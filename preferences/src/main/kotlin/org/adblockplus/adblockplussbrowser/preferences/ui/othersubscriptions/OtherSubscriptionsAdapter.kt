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

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.view.layoutInflater
import org.adblockplus.adblockplussbrowser.preferences.databinding.OtherSubscriptionsCustomItemBinding

internal class OtherSubscriptionsAdapter(
    private val customSubscriptions: List<OtherSubscriptionsItem.CustomItem>,
) : RecyclerView.Adapter<CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            OtherSubscriptionsCustomItemBinding.inflate(parent.layoutInflater, parent, false)
        )
    }

    override fun getItemCount(): Int = customSubscriptions.size

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(customSubscriptions[position])
    }

    fun getCustomItem(position: Int): OtherSubscriptionsItem.CustomItem {
        return customSubscriptions[position]
    }
}

internal class CustomViewHolder(val binding: OtherSubscriptionsCustomItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: OtherSubscriptionsItem.CustomItem) {
        binding.item = item
        binding.executePendingBindings()
    }
}

