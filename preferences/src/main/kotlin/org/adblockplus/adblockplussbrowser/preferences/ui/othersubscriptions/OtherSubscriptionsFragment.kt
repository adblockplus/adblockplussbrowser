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

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.leinardi.android.speeddial.SpeedDialActionItem
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.BuildConfig
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.view.setDebounceOnClickListener
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentOtherSubscriptionsBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.SwipeToDeleteCallback
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
internal class OtherSubscriptionsFragment :
    DataBindingFragment<FragmentOtherSubscriptionsBinding>(R.layout.fragment_other_subscriptions) {

    private val viewModel: OtherSubscriptionsViewModel by activityViewModels()

    private lateinit var getTextFile: ActivityResultLauncher<Intent>

    override fun onBindView(binding: FragmentOtherSubscriptionsBinding) {
        binding.viewModel = viewModel

        if (BuildConfig.FLAVOR_product == BuildConfig.FLAVOR_ABP) {
            initSpeedDial(binding)
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
                UiState.Loading -> binding.indeterminateBar.visibility = View.VISIBLE
                else -> binding.indeterminateBar.visibility = View.INVISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.errorFlow.collect {
                Toast.makeText(
                    requireContext(), R.string.other_subscriptions_error_add_custom, Toast.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launch {
            viewModel.activityCancelledFlow.collect {
                Toast.makeText(
                    requireContext(), getText(R.string.file_picking_canceled), Toast.LENGTH_LONG).show()
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
        handleTextFile()
    }

    private fun handleTextFile() {
        getTextFile =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                viewModel.handleFilePickingResult(result, requireContext())
            }
    }

    private fun initSpeedDial(binding: FragmentOtherSubscriptionsBinding) {
        val speedDial = binding.speedDial
        speedDial.visibility = View.VISIBLE

        val addWithUrlButton = SpeedDialActionItem.Builder(
            R.id.other_subscriptions_add_from_url_button,
            R.drawable.ic_baseline_link_24
        )
            .setFabBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.onboarding_icon_foreground_accent
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
                    R.color.onboarding_icon_foreground_accent
                )
            )
            .create()

        speedDial.addAllActionItems(listOf(addFromLocalStorageButton, addWithUrlButton))

        speedDial.setOnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.other_subscriptions_add_from_url_button -> {
                    viewModel.logCustomFilterListFromUrl()
                    AddCustomSubscriptionDialogFragment().show(parentFragmentManager, null)
                }
                R.id.other_subscriptions_add_from_local_button -> {
                    viewModel.logCustomFilterListFromFile()
                    runCatching {
                        viewModel.loadFileFromStorage(getTextFile)
                    }.onFailure {
                        Toast.makeText(
                            requireContext(),
                            getText(R.string.file_manager_not_found_message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            false
        }
    }
}
