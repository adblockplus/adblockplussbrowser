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

import android.app.ProgressDialog
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentOtherSubscriptionsBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.SwipeToDeleteCallback

@AndroidEntryPoint
internal class OtherSubscriptionsFragment :
    DataBindingFragment<FragmentOtherSubscriptionsBinding>(R.layout.fragment_other_subscriptions) {

    private val viewModel: OtherSubscriptionsViewModel by activityViewModels()

    private var progressDialog: ProgressDialog? = null

    override fun onBindView(binding: FragmentOtherSubscriptionsBinding) {
        binding.viewModel = viewModel
        binding.otherSubscriptionsList.adapter = OtherSubscriptionsAdapter(viewModel, viewLifecycleOwner)

        binding.otherSubscriptionsAddButton.setOnClickListener {
            AddCustomSubscriptionDialogFragment().show(parentFragmentManager, null)
        }

        val swipeToDeleteHandler = object : SwipeToDeleteCallback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                if (viewHolder.adapterPosition < viewModel.nonRemovableItemCount) return 0
                return super.getMovementFlags(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val otherSubscriptionsAdapter = binding.otherSubscriptionsList.adapter as OtherSubscriptionsAdapter
                val item = otherSubscriptionsAdapter.getCustomItem(viewHolder.adapterPosition)
                DeleteCustomSubscriptionDialogFragment.newInstance(item).show(parentFragmentManager, null)
                otherSubscriptionsAdapter.notifyDataSetChanged()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteHandler)
        itemTouchHelper.attachToRecyclerView(binding.otherSubscriptionsList)


        viewModel.uiState.observe(this) { uiState ->
            when (uiState) {
                UiState.Done -> progressDialog?.dismiss()
                UiState.Error -> Toast.makeText(requireContext(),
                    getString(R.string.other_subscriptions_error_add_custom), Toast.LENGTH_LONG).show()
                UiState.Loading -> {
                    progressDialog = ProgressDialog.show(requireContext(),
                        getString(R.string.other_subscriptions_dialog_title),
                        getString(R.string.other_subscriptions_dialog_message))
                }
            }
        }

        viewModel.subscriptions.observe(this) { otherSubscriptionsList ->
            val areCustomSubscriptionsEmpty = otherSubscriptionsList.filterIsInstance(OtherSubscriptionsItem.CustomItem::class.java).isEmpty()
            binding.otherSubscriptionsHint.visibility = if (areCustomSubscriptionsEmpty) View.INVISIBLE else View.VISIBLE
        }

    }
}