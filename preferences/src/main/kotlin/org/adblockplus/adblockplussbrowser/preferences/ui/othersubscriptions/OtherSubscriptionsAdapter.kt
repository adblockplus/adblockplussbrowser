package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

internal class OtherSubscriptionsAdapter(
    private val viewModel: OtherSubscriptionsViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<OtherSubscriptionsItem, OtherSubscriptionsViewHolder>(OtherSubscriptionsItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherSubscriptionsViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: OtherSubscriptionsViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}

internal sealed class OtherSubscriptionsViewHolder(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
}

private class OtherSubscriptionsItemDiffCallback : DiffUtil.ItemCallback<OtherSubscriptionsItem>() {

    override fun areItemsTheSame(oldItem: OtherSubscriptionsItem, newItem: OtherSubscriptionsItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: OtherSubscriptionsItem, newItem: OtherSubscriptionsItem): Boolean =
        oldItem == newItem
}