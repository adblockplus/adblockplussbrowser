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
import android.util.Patterns
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.preferences.R

@AndroidEntryPoint
internal class AddCustomSubscriptionDialogFragment : AppCompatDialogFragment() {

    private val viewModel: OtherSubscriptionsViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialDialog(requireContext()).show {
            title(R.string.other_subscriptions_add_custom_title)
            input(
                hintRes = R.string.other_subscriptions_add_custom_hint,
                waitForPositiveButton = false
            ) { dialog, text ->
                val validUrl = Patterns.WEB_URL.matcher(text).matches()
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, validUrl)
            }
            positiveButton(android.R.string.ok) { dialog ->
                val url = dialog.getInputField().text.toString()
                viewModel.addCustomUrl(url)
            }
            negativeButton(android.R.string.cancel)
        }
}