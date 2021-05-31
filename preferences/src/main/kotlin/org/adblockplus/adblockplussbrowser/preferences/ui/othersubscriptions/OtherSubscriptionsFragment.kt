package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import android.app.ProgressDialog
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentOtherSubscriptionsBinding

@AndroidEntryPoint
internal class OtherSubscriptionsFragment :
    DataBindingFragment<FragmentOtherSubscriptionsBinding>(R.layout.fragment_other_subscriptions) {

    private val viewModel: OtherSubscriptionsViewModel by activityViewModels()

    private var progressDialog: ProgressDialog? = null

    override fun onBindView(binding: FragmentOtherSubscriptionsBinding) {
        binding.viewModel = viewModel
        binding.otherSubscriptionsList.adapter = OtherSubscriptionsAdapter(viewModel, viewLifecycleOwner)

        binding.otherSubscriptionsAddButton.setOnClickListener {
            AddCustomSubscriptionDialogFragment().show(parentFragmentManager, null)
        }

        viewModel.uiState.observe(this) { uiState ->
            when (uiState) {
                UiState.Done -> progressDialog?.hide()
                UiState.Error -> Toast.makeText(requireContext(),
                    getString(R.string.other_subscriptions_error_add_custom), Toast.LENGTH_LONG).show()
                UiState.Loading -> {
                    progressDialog = ProgressDialog.show(requireContext(),
                        getString(R.string.other_subscriptions_dialog_title),
                        getString(R.string.other_subscriptions_dialog_message))
                }
            }
        }
    }
}