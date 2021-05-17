package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentOtherSubscriptionsBinding

@AndroidEntryPoint
internal class OtherSubscriptionsFragment :
    DataBindingFragment<FragmentOtherSubscriptionsBinding>(R.layout.fragment_other_subscriptions) {

    private val viewModel: OtherSubscriptionsViewModel by viewModels()

    override fun onBindView(binding: FragmentOtherSubscriptionsBinding) {
        binding.viewModel = viewModel
        binding.otherSubscriptionsList.adapter = OtherSubscriptionsAdapter(viewModel, viewLifecycleOwner)

        binding.otherSubscriptionsAddButton.setOnClickListener {
            AddCustomSubscriptionDialogFragment().show(parentFragmentManager, null)
        }
    }
}