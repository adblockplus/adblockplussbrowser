package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.preferences.R
import org.apache.commons.validator.routines.DomainValidator

@AndroidEntryPoint
internal class AddDomainDialogFragment : AppCompatDialogFragment() {

    private val viewModel: AllowlistViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialDialog(requireContext()).show {
            title(R.string.allowlist_add_title)
            input(
                hintRes = R.string.allowlist_add_hint,
                waitForPositiveButton = false
            ) { dialog, text ->
                val validDomain = DomainValidator.getInstance().isValid(text.toString())
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, validDomain)
            }
            positiveButton(android.R.string.ok) { dialog ->
                val domain = dialog.getInputField().text.toString()
                viewModel.addDomain(Uri.parse(domain).host ?: domain)
            }
            negativeButton(android.R.string.cancel)
        }
}