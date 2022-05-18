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

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
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
    private val updateViewModel: UpdateSubscriptionsViewModel by activityViewModels()

    override fun onBindView(binding: FragmentMainPreferencesBinding) {
        binding.viewModel = viewModel
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        supportActionBar?.subtitle = getString(R.string.app_subtitle)

        val lifecycleOwner = this.viewLifecycleOwner

        binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions.setDebounceOnClickListener ({
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
            findNavController().navigate(direction)
        }, lifecycleOwner)
        binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions.setDebounceOnClickListener ({
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToOtherSubscriptionsFragment()
            findNavController().navigate(direction)
        }, lifecycleOwner)
        binding.mainPreferencesAdBlockingInclude.mainPreferencesAllowlist.setDebounceOnClickListener ({
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAllowlistFragment()
            findNavController().navigate(direction)
        }, lifecycleOwner)

        if (BuildConfig.FLAVOR_product in listOf(BuildConfig.FLAVOR_ABP, BuildConfig.FLAVOR_ADBLOCK)) {
            binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.setDebounceOnClickListener ({
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToUpdateSubscriptionsFragment()
                findNavController().navigate(direction)
            }, lifecycleOwner)
        } else {
            val checkBox = binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.findViewById<MaterialCheckBox>(R.id.wifi_only_checkbox)
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                updateViewModel.setUpdateConfigType(null)
            }
            checkBox.isChecked = updateViewModel.isWifiOnlyEnabled.value!!
            binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
            val updateTypeText = binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.findViewById<MaterialTextView>(R.id.update_type_text)

            updateViewModel.isWifiOnlyEnabled.observe(this) { isWifiOnlyEnabled ->
                if (isWifiOnlyEnabled!!) {
                    updateTypeText.text = getString(R.string.preferences_automatic_updates_wifi_only)
                } else {
                    updateTypeText.text = getString(R.string.preferences_automatic_updates_always)
                }
            }
//            updateViewModel.updateType.observe(this) { updateType ->
//                updateViewModel.isWifiOnlyEnabled.postValue(
//                    updateType == UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_WIFI_ONLY
//                )
//
//                updateTypeText.text = when (updateType) {
//                    UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_WIFI_ONLY -> getString(
//                        R.string.preferences_automatic_updates_wifi_only)
//                    else -> getString()
//                }
//            }
        }

        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionAdd.setDebounceOnClickListener ({
            supportActionBar?.subtitle = null
            viewModel.markLanguagesOnboardingComplete(true)
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
            findNavController().navigate(direction)
        }, lifecycleOwner)
        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionSkip.setDebounceOnClickListener ({
            viewModel.markLanguagesOnboardingComplete(false)
        }, lifecycleOwner)
        binding.mainPreferencesAcceptableAdsInclude.mainPreferencesAcceptableAds.setDebounceOnClickListener ({
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAcceptableAdsFragment()
            findNavController().navigate(direction)
        }, lifecycleOwner)
        binding.mainPreferencesAboutInclude.mainPreferencesAbout.setDebounceOnClickListener ({
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
