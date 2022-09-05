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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.leinardi.android.speeddial.SpeedDialActionItem
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.BuildConfig
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.view.setDebounceOnClickListener
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentOtherSubscriptionsBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.SwipeToDeleteCallback
import javax.inject.Inject

@AndroidEntryPoint
internal class OtherSubscriptionsFragment :
    DataBindingFragment<FragmentOtherSubscriptionsBinding>(R.layout.fragment_other_subscriptions) {

    private val viewModel: OtherSubscriptionsViewModel by activityViewModels()

    private lateinit var getTextFile: ActivityResultLauncher<Intent>

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

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

        getTextFile =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                handleFilePickingResult(result)
            }
    }

    private fun handleFilePickingResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { filePath ->
                viewModel.addCustomFilterFile(
                    filePath,
                    requireContext()
                )
            }
        } else {
            analyticsProvider.logEvent(AnalyticsEvent.DEVICE_FILE_MANAGER_NOT_SUPPORTED_OR_CANCELED)
            Toast.makeText(
                context,
                getText(R.string.file_picking_canceled),
                Toast.LENGTH_LONG
            ).show()
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

        speedDial.addAllActionItems(listOf(addWithUrlButton, addFromLocalStorageButton))

        speedDial.setOnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.other_subscriptions_add_from_url_button -> {
                    analyticsProvider.logEvent(AnalyticsEvent.LOAD_CUSTOM_FILTER_LIST_FROM_URL)
                    AddCustomSubscriptionDialogFragment().show(parentFragmentManager, null)
                }
                R.id.other_subscriptions_add_from_local_button -> {
                    analyticsProvider.logEvent(AnalyticsEvent.LOAD_CUSTOM_FILTER_LIST_FROM_FILE)
                    loadFileFromStorage()
                }
            }
            false
        }
    }

    private fun loadFileFromStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(intent, "Open file from...")
        try {
            getTextFile.launch(chooser)
        } catch (ex: ActivityNotFoundException) {
            analyticsProvider.logException(ex)
            Toast.makeText(
                context,
                getText(R.string.file_manager_not_found_message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

}
