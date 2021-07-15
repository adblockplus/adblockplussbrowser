package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.preferences.R

@AndroidEntryPoint
class DeleteCustomSubscriptionDialogFragment : AppCompatDialogFragment() {

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
            title(R.string.other_subscriptions_remove_custom_title)
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