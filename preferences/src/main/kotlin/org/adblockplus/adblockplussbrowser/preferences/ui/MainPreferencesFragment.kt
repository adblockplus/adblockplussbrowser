package org.adblockplus.adblockplussbrowser.preferences.ui

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding

@AndroidEntryPoint
internal class MainPreferencesFragment :
    DataBindingFragment<FragmentMainPreferencesBinding>(R.layout.fragment_main_preferences) {

    private val viewModel: MainPreferencesViewModel by viewModels()

    override fun onBindView(binding: FragmentMainPreferencesBinding) {
        binding.viewModel = viewModel
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
        binding.mainPreferencesAdBlockingInclude.mainPreferencesAllowlist.setOnClickListener {
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAllowlistFragment()
            findNavController().navigate(direction)
        }
        binding.mainPreferencesAcceptableAdsInclude.mainPreferencesAcceptableAds.setOnClickListener {
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAcceptableAdsFragment()
            findNavController().navigate(direction)
        }
        binding.mainPreferencesAboutInclude.mainPreferencesAbout.setOnClickListener {
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAboutFragment()
            findNavController().navigate(direction)
        }

        viewModel.updates.observe(this) { wrapper ->
            wrapper.get()?.let {
                Toast.makeText(requireContext(), getString(R.string.preferences_updating_message),
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}