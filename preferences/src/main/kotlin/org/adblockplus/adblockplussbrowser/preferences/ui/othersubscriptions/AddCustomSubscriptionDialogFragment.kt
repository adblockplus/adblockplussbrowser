package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import android.app.Dialog
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.preferences.R

@AndroidEntryPoint
class AddCustomSubscriptionDialogFragment : AppCompatDialogFragment() {

    private val viewModel: OtherSubscriptionsViewModel by viewModels()

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