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

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.view.setDebounceOnClickListener
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentAllowlistBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.SwipeToDeleteCallback

@AndroidEntryPoint
internal class AllowlistFragment : DataBindingFragment<FragmentAllowlistBinding>(R.layout.fragment_allowlist) {

    private val viewModel: AllowlistViewModel by activityViewModels()

    override fun onBindView(binding: FragmentAllowlistBinding) {
        binding.viewModel = viewModel
        binding.allowlistList.adapter = AllowlistAdapter(viewModel, viewLifecycleOwner)

        val lifecycleOwner = this.viewLifecycleOwner
        binding.allowlistAddButton.setDebounceOnClickListener({
            AddDomainDialogFragment().show(parentFragmentManager, null)
        }, lifecycleOwner)

        val swipeToDeleteHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val allowListAdapter = binding.allowlistList.adapter as AllowlistAdapter
                val item = allowListAdapter.getItem(viewHolder.adapterPosition)
                DeleteAllowlistItemDialogFragment.newInstance(item).show(parentFragmentManager, null)
                allowListAdapter.notifyDataSetChanged()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteHandler)
        itemTouchHelper.attachToRecyclerView(binding.allowlistList)

        viewModel.items.observe(this) { items ->
            binding.allowlistHint.visibility = if (items.isEmpty()) View.INVISIBLE else View.VISIBLE
        }
    }
}