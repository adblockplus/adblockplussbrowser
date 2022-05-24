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

package org.adblockplus.adblockplussbrowser.preferences.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.checkbox.MaterialCheckBox
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.view.setDebounceOnClickListener
import org.adblockplus.adblockplussbrowser.preferences.BuildConfig
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.updates.UpdateSubscriptionsViewModel

@AndroidEntryPoint
internal class MainPreferencesFragment :
    DataBindingFragment<FragmentMainPreferencesBinding>(R.layout.fragment_main_preferences) {

    private val viewModel: MainPreferencesViewModel by activityViewModels()
    // Lazy loading the UpdateSubscriptionsViewModel so it will only be used for crystal flavor here
    private val updateViewModel: UpdateSubscriptionsViewModel by activityViewModels()

    override fun onBindView(binding: FragmentMainPreferencesBinding) {
        binding.viewModel = viewModel
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        supportActionBar?.subtitle = getString(R.string.app_subtitle)

        val lifecycleOwner = this.viewLifecycleOwner

        binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
        binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToOtherSubscriptionsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
        binding.mainPreferencesAdBlockingInclude.mainPreferencesAllowlist.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToAllowlistFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )

        if (BuildConfig.FLAVOR_product != BuildConfig.FLAVOR_CRYSTAL) {
            binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.visibility = View.VISIBLE
            binding.mainPreferencesAdBlockingInclude.crystalMainPreferencesUpdateSubscriptions.visibility = View.GONE
            binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.setDebounceOnClickListener(
                {
                    supportActionBar?.subtitle = null
                    val direction = MainPreferencesFragmentDirections
                        .actionMainPreferencesFragmentToUpdateSubscriptionsFragment()
                    findNavController().navigate(direction)
                },
                lifecycleOwner
            )
        } else {
            binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.visibility = View.GONE
            binding.mainPreferencesAdBlockingInclude.crystalMainPreferencesUpdateSubscriptions.visibility = View.VISIBLE
            val wifiOnlyCheckbox: MaterialCheckBox =
                binding.mainPreferencesAdBlockingInclude.crystalMainPreferencesUpdateSubscriptions.findViewById(
                    R.id.wifi_only_checkbox
                )

            binding.mainPreferencesAdBlockingInclude.crystalMainPreferencesUpdateSubscriptions.setOnClickListener {
                wifiOnlyCheckbox.isChecked = !wifiOnlyCheckbox.isChecked
                var updateConfigType =
                    UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_ALWAYS
                if (wifiOnlyCheckbox.isChecked) {
                    updateConfigType =
                        UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_WIFI_ONLY
                }

                updateViewModel.setUpdateConfigType(updateConfigType)
            }

            updateViewModel.updateType.observe(this) { updateType ->
                wifiOnlyCheckbox.isChecked =
                    updateType.name == UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_WIFI_ONLY.name
            }
        }

        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionAdd.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                viewModel.markLanguagesOnboardingComplete(true)
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionSkip.setDebounceOnClickListener(
            {
                viewModel.markLanguagesOnboardingComplete(false)
            },
            lifecycleOwner
        )
        binding.mainPreferencesAcceptableAdsInclude.mainPreferencesAcceptableAds.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToAcceptableAdsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
        binding.mainPreferencesAboutInclude.mainPreferencesAbout.setDebounceOnClickListener({
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAboutFragment()
            findNavController().navigate(direction)
        }, lifecycleOwner)
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLanguagesOnboarding()
    }
}
