package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.preferences.R

@AndroidEntryPoint
internal class DeleteAllowlistItemDialogFragment : AppCompatDialogFragment() {

    private lateinit var item: AllowlistItem
    private val viewModel: AllowlistViewModel by activityViewModels()

    companion object {
        private const val ITEM_KEY = "item"
        fun newInstance(item: AllowlistItem) : DeleteAllowlistItemDialogFragment {
            val args = Bundle().apply {
                putSerializable(ITEM_KEY, item)
            }
            val dialog = DeleteAllowlistItemDialogFragment()
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it.getSerializable(ITEM_KEY) as AllowlistItem
        }
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialDialog(requireContext()).show {
            title(R.string.allowlist_delete_dialog_title)
            message(0, getString(R.string.allowlist_delete_dialog_message, item.domain))
            positiveButton(android.R.string.ok) {
                viewModel.removeItem(item)
                dismiss()
            }
            negativeButton(android.R.string.cancel)
        }
}