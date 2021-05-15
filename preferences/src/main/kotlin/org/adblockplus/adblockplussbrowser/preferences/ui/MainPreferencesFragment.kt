package org.adblockplus.adblockplussbrowser.preferences.ui

import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding

@AndroidEntryPoint
internal class MainPreferencesFragment :
    DataBindingFragment<FragmentMainPreferencesBinding>(R.layout.fragment_main_preferences) {

    override fun onBindView(binding: FragmentMainPreferencesBinding) {
        binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions.setOnClickListener {
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
            findNavController().navigate(direction)
        }
        binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions.setOnClickListener {
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToOtherSubscriptionsFragment()
            findNavController().navigate(direction)
        }
    }
}