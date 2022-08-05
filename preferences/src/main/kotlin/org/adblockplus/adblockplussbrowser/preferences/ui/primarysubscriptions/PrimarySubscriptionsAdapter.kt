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

package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.adblockplus.adblockplussbrowser.base.databinding.bindHolder
import org.adblockplus.adblockplussbrowser.base.kotlin.exhaustive
import org.adblockplus.adblockplussbrowser.base.view.layoutInflater
import org.adblockplus.adblockplussbrowser.preferences.databinding.PrimarySubscriptionsHeaderItemBinding
import org.adblockplus.adblockplussbrowser.preferences.databinding.PrimarySubscriptionsSubscriptionItemBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions.PrimarySubscriptionsItemType.HEADER_ITEM
import org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions.PrimarySubscriptionsItemType.SUBSCRIPTION_ITEM
import org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions.PrimarySubscriptionsViewHolder.HeaderViewHolder
import org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions.PrimarySubscriptionsViewHolder.SubscriptionViewHolder

internal class PrimarySubscriptionsAdapter(
    private val viewModel: PrimarySubscriptionsViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<PrimarySubscriptionsItem, PrimarySubscriptionsViewHolder>(PrimarySubscriptionsItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrimarySubscriptionsViewHolder {
        return when (viewType) {
            HEADER_ITEM.ordinal -> {
                HeaderViewHolder(
                    PrimarySubscriptionsHeaderItemBinding.inflate(parent.layoutInflater, parent, false)
                )
            }
            SUBSCRIPTION_ITEM.ordinal -> {
                SubscriptionViewHolder(
                    PrimarySubscriptionsSubscriptionItemBinding.inflate(parent.layoutInflater, parent, false)
                )
            }
            else -> {
                throw IllegalArgumentException("Unexpected viewType: $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: PrimarySubscriptionsViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.binding.bindHolder {
                    item = getItem(position) as PrimarySubscriptionsItem.HeaderItem
                }
            }
            is SubscriptionViewHolder -> {
                holder.binding.bindHolder {
                    item = getItem(position) as PrimarySubscriptionsItem.SubscriptionItem
                    viewModel = this@PrimarySubscriptionsAdapter.viewModel
                    lifecycleOwner = this@PrimarySubscriptionsAdapter.lifecycleOwner
                }
            }
        }.exhaustive
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PrimarySubscriptionsItem.HeaderItem -> HEADER_ITEM.ordinal
            is PrimarySubscriptionsItem.SubscriptionItem -> SUBSCRIPTION_ITEM.ordinal
        }
    }
}

internal sealed class PrimarySubscriptionsViewHolder(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    class HeaderViewHolder(val binding: PrimarySubscriptionsHeaderItemBinding) : PrimarySubscriptionsViewHolder(binding)

    class SubscriptionViewHolder(val binding: PrimarySubscriptionsSubscriptionItemBinding) :
        PrimarySubscriptionsViewHolder(binding)
}

private enum class PrimarySubscriptionsItemType {
    HEADER_ITEM,
    SUBSCRIPTION_ITEM
}

private class PrimarySubscriptionsItemDiffCallback : DiffUtil.ItemCallback<PrimarySubscriptionsItem>() {

    override fun areItemsTheSame(oldItem: PrimarySubscriptionsItem, newItem: PrimarySubscriptionsItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PrimarySubscriptionsItem, newItem: PrimarySubscriptionsItem): Boolean =
        oldItem == newItem
}

