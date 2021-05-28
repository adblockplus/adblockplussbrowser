package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.adblockplus.adblockplussbrowser.base.databinding.OnItemClickListener
import org.adblockplus.adblockplussbrowser.base.databinding.bindHolder
import org.adblockplus.adblockplussbrowser.base.view.layoutInflater
import org.adblockplus.adblockplussbrowser.preferences.databinding.AllowlistItemBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.allowlist.AllowlistAdapter.ViewHolder

internal class AllowlistAdapter(
    private val viewModel: AllowlistViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<AllowlistItem, ViewHolder>(AllowlistItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(AllowlistItemBinding.inflate(parent.layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.bindHolder {
            item = getItem(position) as AllowlistItem
            viewModel = this@AllowlistAdapter.viewModel
            lifecycleOwner = this@AllowlistAdapter.lifecycleOwner
        }
    }

    internal class ViewHolder(val binding: AllowlistItemBinding) : RecyclerView.ViewHolder(binding.root)
}

private class AllowlistItemDiffCallback : DiffUtil.ItemCallback<AllowlistItem>() {

    override fun areItemsTheSame(oldItem: AllowlistItem, newItem: AllowlistItem): Boolean =
        oldItem.domain == newItem.domain

    override fun areContentsTheSame(oldItem: AllowlistItem, newItem: AllowlistItem): Boolean =
        oldItem == newItem
}