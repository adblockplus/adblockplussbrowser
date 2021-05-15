package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.databinding.*
import org.adblockplus.adblockplussbrowser.base.kotlin.exhaustive
import org.adblockplus.adblockplussbrowser.base.view.layoutInflater
import org.adblockplus.adblockplussbrowser.preferences.databinding.PrimarySubscriptionsHeaderItemBinding
import org.adblockplus.adblockplussbrowser.preferences.databinding.PrimarySubscriptionsSubscriptionItemBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.GroupItemLayout
import org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions.PrimarySubscriptionsItemType.HEADER_ITEM
import org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions.PrimarySubscriptionsItemType.SUBSCRIPTION_ITEM
import org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions.PrimarySubscriptionsViewHolder.HeaderViewHolder
import org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions.PrimarySubscriptionsViewHolder.SubscriptionViewHolder

internal class PrimarySubscriptionsAdapter(
    private val listener: OnItemClickListener<PrimarySubscriptionsItem.SubscriptionItem>
) : ListAdapter<PrimarySubscriptionsItem, PrimarySubscriptionsViewHolder>(PrimarySubscriptionsItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrimarySubscriptionsViewHolder {
        return when (viewType) {
            HEADER_ITEM.ordinal -> {
                HeaderViewHolder(
                    PrimarySubscriptionsHeaderItemBinding.inflate(
                        parent.layoutInflater,
                        parent,
                        false
                    )
                )
            }
            SUBSCRIPTION_ITEM.ordinal -> {
                SubscriptionViewHolder(
                    PrimarySubscriptionsSubscriptionItemBinding.inflate(
                        parent.layoutInflater,
                        parent,
                        false
                    )
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
                    listener = this@PrimarySubscriptionsAdapter.listener
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

internal sealed class PrimarySubscriptionsItem(val id: String) {

    data class HeaderItem(@StringRes val titleResId: Int) : PrimarySubscriptionsItem(titleResId.toString())

    data class SubscriptionItem(val subscription: Subscription, val layout: GroupItemLayout, val active: Boolean) :
        PrimarySubscriptionsItem(subscription.url)
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