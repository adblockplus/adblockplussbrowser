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

package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
            item = getItem(position)
            viewModel = this@AllowlistAdapter.viewModel
            lifecycleOwner = this@AllowlistAdapter.lifecycleOwner
        }
    }

    public override fun getItem(position: Int): AllowlistItem = super.getItem(position)

    internal class ViewHolder(val binding: AllowlistItemBinding) : RecyclerView.ViewHolder(binding.root)
}

private class AllowlistItemDiffCallback : DiffUtil.ItemCallback<AllowlistItem>() {

    override fun areItemsTheSame(oldItem: AllowlistItem, newItem: AllowlistItem): Boolean =
        oldItem.domain == newItem.domain

    override fun areContentsTheSame(oldItem: AllowlistItem, newItem: AllowlistItem): Boolean =
        oldItem == newItem
}