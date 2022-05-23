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

import android.content.ContentResolver
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
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

        val lifecycleOwner = this.viewLifecycleOwner
        binding.otherSubscriptionsAddFromUrlButton.setDebounceOnClickListener({
            AddCustomSubscriptionDialogFragment().show(parentFragmentManager, null)
        }, lifecycleOwner)

        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the returned Uri
            if (uri != null) {
                val inputStream: InputStream = FileInputStream(uri.toString())
                print("hello")
            }
        }

        binding.otherSubscriptionsAddFromLocalButton.setOnClickListener{
            getContent.launch("text/plain")
//            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "text/plain"
//            }
//            try {
//                startActivity(intent)
//            } catch (e: ActivityNotFoundException) {
//                Toast.makeText(context, getString(R.string.file_explorer_not_found_message), Toast.LENGTH_LONG)
//                    .show()
//            }
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