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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.BuildConfig
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.view.setDebounceOnClickListener
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentOtherSubscriptionsBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.SwipeToDeleteCallback


@AndroidEntryPoint
internal class OtherSubscriptionsFragment :
    DataBindingFragment<FragmentOtherSubscriptionsBinding>(R.layout.fragment_other_subscriptions) {

    private val viewModel: OtherSubscriptionsViewModel by activityViewModels()

    override fun onBindView(binding: FragmentOtherSubscriptionsBinding) {
        binding.viewModel = viewModel

        if (BuildConfig.FLAVOR_product == BuildConfig.FLAVOR_ABP) {
            val speedDial = binding.speedDial
            speedDial.visibility = View.VISIBLE

            val addWithUrlButton = SpeedDialActionItem.Builder(
                R.id.other_subscriptions_add_from_url_button,
                R.drawable.ic_baseline_link_24
            )
                .setFabBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.background_accent
                    )
                )
                .create()

            val addFromLocalStorageButton = SpeedDialActionItem.Builder(
                R.id.other_subscriptions_add_from_local_button,
                R.drawable.ic_baseline_folder_24
            )
                .setFabBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.background_accent
                    )
                )
                .create()

            speedDial.addAllActionItems(listOf(addWithUrlButton, addFromLocalStorageButton))

            speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.other_subscriptions_add_from_url_button -> {
                        AddCustomSubscriptionDialogFragment().show(parentFragmentManager, null)
                    }
                    R.id.other_subscriptions_add_from_local_button -> {
                        // TODO: finish integration with file explorer
                        Toast.makeText(
                            context,
                            getText(R.string.file_manager_not_found_message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                false
            })
        } else {
            val lifecycleOwner = this.viewLifecycleOwner
            binding.otherSubscriptionsAddButton.visibility = View.VISIBLE
            binding.otherSubscriptionsAddButton.setDebounceOnClickListener({
                AddCustomSubscriptionDialogFragment().show(parentFragmentManager, null)
            }, lifecycleOwner)
        }

        val swipeToDeleteHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val otherSubscriptionsAdapter =
                    binding.otherSubscriptionsList.adapter as OtherSubscriptionsAdapter
                val item = otherSubscriptionsAdapter.getCustomItem(viewHolder.adapterPosition)
                DeleteCustomSubscriptionDialogFragment.newInstance(item)
                    .show(parentFragmentManager, null)
                otherSubscriptionsAdapter.notifyDataSetChanged()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteHandler)
        itemTouchHelper.attachToRecyclerView(binding.otherSubscriptionsList)


        viewModel.uiState.observe(this) { uiState ->
            when (uiState) {
                UiState.Done -> binding.indeterminateBar.visibility = View.INVISIBLE
                UiState.Error -> Toast.makeText(
                    requireContext(),
                    getString(R.string.other_subscriptions_error_add_custom), Toast.LENGTH_LONG
                ).show()
                UiState.Loading -> binding.indeterminateBar.visibility = View.VISIBLE
            }
        }

        viewModel.customSubscriptions.observe(this) { otherSubscriptionsList ->
            binding.otherSubscriptionsList.adapter =
                OtherSubscriptionsAdapter(otherSubscriptionsList)
        }

        viewModel.activeSubscriptions.observe(this) { activeSubscriptions ->
            viewModel.additionalTrackingSubscription.observe(this) { subscription ->
                val result =
                    activeSubscriptions.firstOrNull { active -> active.url == subscription.url }
                viewModel.additionalTrackingLastUpdate.apply { value = result?.lastUpdate }
                viewModel.blockAdditionalTracking.apply {
                    value = result != null
                }
            }

            viewModel.socialMediaTrackingSubscription.observe(this) { subscription ->
                val result =
                    activeSubscriptions.firstOrNull { active -> active.url == subscription.url }
                viewModel.socialMediaIconsTrackingLastUpdate.apply { value = result?.lastUpdate }
                viewModel.blockSocialMediaTracking.apply {
                    value = result != null
                }
            }
        }
    }
}
