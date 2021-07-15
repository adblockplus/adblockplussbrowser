package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.adblockplus.adblockplussbrowser.base.databinding.bindHolder
import org.adblockplus.adblockplussbrowser.base.kotlin.exhaustive
import org.adblockplus.adblockplussbrowser.base.view.layoutInflater
import org.adblockplus.adblockplussbrowser.preferences.databinding.OtherSubscriptionsCustomItemBinding
import org.adblockplus.adblockplussbrowser.preferences.databinding.OtherSubscriptionsDefaultItemBinding
import org.adblockplus.adblockplussbrowser.preferences.databinding.OtherSubscriptionsHeaderItemBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions.OtherSubscriptionsItemType.*
import org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions.OtherSubscriptionsViewHolder.*

internal class OtherSubscriptionsAdapter(
    private val viewModel: OtherSubscriptionsViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<OtherSubscriptionsItem, OtherSubscriptionsViewHolder>(OtherSubscriptionsItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherSubscriptionsViewHolder {
        return when (viewType) {
            HEADER_ITEM.ordinal -> {
                HeaderViewHolder(
                    OtherSubscriptionsHeaderItemBinding.inflate(parent.layoutInflater, parent, false)
                )
            }
            DEFAULT_ITEM.ordinal -> {
                DefaultViewHolder(
                    OtherSubscriptionsDefaultItemBinding.inflate(parent.layoutInflater, parent, false)
                )
            }
            CUSTOM_ITEM.ordinal -> {
                CustomViewHolder(
                    OtherSubscriptionsCustomItemBinding.inflate(parent.layoutInflater, parent, false)
                )
            }
            else -> {
                throw IllegalArgumentException("Unexpected viewType: $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: OtherSubscriptionsViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.binding.bindHolder {
                    item = getItem(position) as OtherSubscriptionsItem.HeaderItem
                }
            }
            is DefaultViewHolder -> {
                holder.binding.bindHolder {
                    item = getItem(position) as OtherSubscriptionsItem.DefaultItem
                    viewModel = this@OtherSubscriptionsAdapter.viewModel
                    lifecycleOwner = this@OtherSubscriptionsAdapter.lifecycleOwner
                }
            }
            is CustomViewHolder -> {
                holder.binding.bindHolder {
                    item = getItem(position) as OtherSubscriptionsItem.CustomItem
                    viewModel = this@OtherSubscriptionsAdapter.viewModel
                    lifecycleOwner = this@OtherSubscriptionsAdapter.lifecycleOwner
                }
            }
        }.exhaustive
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is OtherSubscriptionsItem.HeaderItem -> HEADER_ITEM.ordinal
            is OtherSubscriptionsItem.DefaultItem -> DEFAULT_ITEM.ordinal
            is OtherSubscriptionsItem.CustomItem -> CUSTOM_ITEM.ordinal
        }
    }

    fun getCustomItem(position: Int): OtherSubscriptionsItem.CustomItem {
        val item = getItem(position)
        if (item is OtherSubscriptionsItem.CustomItem) {
            return item
        }
        throw IllegalArgumentException("Unexpected item type at position $position")
    }
}

internal sealed class OtherSubscriptionsViewHolder(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    class HeaderViewHolder(val binding: OtherSubscriptionsHeaderItemBinding) : OtherSubscriptionsViewHolder(binding)

    class DefaultViewHolder(val binding: OtherSubscriptionsDefaultItemBinding) : OtherSubscriptionsViewHolder(binding)

    class CustomViewHolder(val binding: OtherSubscriptionsCustomItemBinding) : OtherSubscriptionsViewHolder(binding)
}

private enum class OtherSubscriptionsItemType {
    HEADER_ITEM,
    DEFAULT_ITEM,
    CUSTOM_ITEM
}

private class OtherSubscriptionsItemDiffCallback : DiffUtil.ItemCallback<OtherSubscriptionsItem>() {

    override fun areItemsTheSame(oldItem: OtherSubscriptionsItem, newItem: OtherSubscriptionsItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: OtherSubscriptionsItem, newItem: OtherSubscriptionsItem): Boolean =
        oldItem == newItem
}