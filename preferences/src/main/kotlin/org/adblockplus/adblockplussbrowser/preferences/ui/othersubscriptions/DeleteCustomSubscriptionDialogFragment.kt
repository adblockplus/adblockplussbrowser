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

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.preferences.R

@AndroidEntryPoint
internal class DeleteCustomSubscriptionDialogFragment : AppCompatDialogFragment() {

    private val viewModel: OtherSubscriptionsViewModel by activityViewModels()
    private lateinit var item: OtherSubscriptionsItem.CustomItem

    companion object {
        private const val ITEM_KEY = "item"
        fun newInstance(item: OtherSubscriptionsItem.CustomItem) : DeleteCustomSubscriptionDialogFragment {
            val args = Bundle().apply {
                putSerializable(ITEM_KEY, item)
            }
            val dialog = DeleteCustomSubscriptionDialogFragment()
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
             item = it.getSerializable(ITEM_KEY) as OtherSubscriptionsItem.CustomItem
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext()).show {
            title(R.string.delete_dialog_title)
            message(0, getString(R.string.other_subscriptions_remove_custom_message, item.subscription.url))
            positiveButton(android.R.string.ok) {
                viewModel.removeSubscription(item)
                dismiss()
            }
            negativeButton(android.R.string.cancel) {
            }
        }
    }
}

