package org.adblockplus.adblockplussbrowser.preferences.ui

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingViewHolder
import org.adblockplus.adblockplussbrowser.base.databinding.OnItemClickListener
import org.adblockplus.adblockplussbrowser.base.databinding.ViewHolderBinder
import org.adblockplus.adblockplussbrowser.base.util.exhaustive
import org.adblockplus.adblockplussbrowser.base.view.layoutInflater
import org.adblockplus.adblockplussbrowser.preferences.databinding.PrimarySubscriptionsHeaderItemBinding
import org.adblockplus.adblockplussbrowser.preferences.databinding.PrimarySubscriptionsSubscriptionItemBinding

internal class PrimarySubscriptionsAdapter(
    private val listener: OnItemClickListener<PrimarySubscriptionsItem.SubscriptionItem>
) : ListAdapter<PrimarySubscriptionsItem, PrimarySubscriptionsViewHolder>(PrimarySubscriptionsItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrimarySubscriptionsViewHolder {
        return when (viewType) {
            PrimarySubscriptionsItemType.HEADER_ITEM.ordinal -> {
                PrimarySubscriptionsViewHolder.HeaderViewHolder(
                    PrimarySubscriptionsHeaderItemBinding.inflate(
                        parent.layoutInflater,
                        parent,
                        false
                    )
                )
            }
            PrimarySubscriptionsItemType.SUBSCRIPTION_ITEM.ordinal -> {
                PrimarySubscriptionsViewHolder.SubscriptionViewHolder(
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
        when(holder) {
            is PrimarySubscriptionsViewHolder.HeaderViewHolder -> {
                holder.bind {
                    item = getItem(position) as PrimarySubscriptionsItem.HeaderItem
                }
            }
            is PrimarySubscriptionsViewHolder.SubscriptionViewHolder -> {
                holder.bind {
                    item = getItem(position) as PrimarySubscriptionsItem.SubscriptionItem
                    listener = this@PrimarySubscriptionsAdapter.listener
                }
            }
        }.exhaustive
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PrimarySubscriptionsItem.HeaderItem -> PrimarySubscriptionsItemType.HEADER_ITEM.ordinal
            is PrimarySubscriptionsItem.SubscriptionItem -> PrimarySubscriptionsItemType.SUBSCRIPTION_ITEM.ordinal
        }
    }
}

internal sealed class PrimarySubscriptionsViewHolder(binding: ViewDataBinding) : DataBindingViewHolder(binding) {

    class HeaderViewHolder(override val binding: PrimarySubscriptionsHeaderItemBinding) :
        PrimarySubscriptionsViewHolder(binding), ViewHolderBinder<PrimarySubscriptionsHeaderItemBinding>

    class SubscriptionViewHolder(override val binding: PrimarySubscriptionsSubscriptionItemBinding) :
        PrimarySubscriptionsViewHolder(binding), ViewHolderBinder<PrimarySubscriptionsSubscriptionItemBinding>
}

internal sealed class PrimarySubscriptionsItem(val id: String) {

    data class HeaderItem(val title: String) : PrimarySubscriptionsItem(title)

    data class SubscriptionItem(val subscription: Subscription, val updated: String, val active: Boolean) :
        PrimarySubscriptionsItem(subscription.title)
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