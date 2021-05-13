package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.fragment.app.viewModels
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentPrimarySubscriptionsBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.PrimarySubscriptionsAdapter

internal class PrimarySubscriptionsFragment :
    DataBindingFragment<FragmentPrimarySubscriptionsBinding>(R.layout.fragment_primary_subscriptions) {

    private val viewModel: PrimarySubscriptionsViewModel by viewModels()

    override fun onBindView(binding: FragmentPrimarySubscriptionsBinding) {
        binding.viewModel = viewModel
        binding.primarySubscriptionsList.adapter = PrimarySubscriptionsAdapter { subscriptionItem ->

        }
    }
}