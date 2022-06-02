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

import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
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

        // Primary Subscriptions
        binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
        // Other Subscriptions
        binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToOtherSubscriptionsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
        // Allowlist
        binding.mainPreferencesAdBlockingInclude.mainPreferencesAllowlist.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToAllowlistFragment()
                findNavController().navigate(direction)
                if (BuildConfig.FLAVOR_product == BuildConfig.FLAVOR_CRYSTAL) {
                    MaterialDialog((activity as AppCompatActivity).window.context).show {
                        cancelable(true)
                        customView(R.layout.dialog_disabled_whitelist, scrollable = true)
                        val textView: TextView = findViewById(R.id.install_si_dialog_summary)
                        textView.movementMethod = LinkMovementMethod.getInstance()
                    }
                }
            },
            lifecycleOwner
        )
        // Update Subscriptions
        if (BuildConfig.FLAVOR_product != BuildConfig.FLAVOR_CRYSTAL) {
            binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.visibility =
                View.VISIBLE
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
            // Binding and update configuration type logic
            binding.mainPreferencesAdBlockingInclude.crystalMainPreferencesUpdateSubscriptions.visibility =
                View.VISIBLE
            val wifiOnlyCheckbox: MaterialCheckBox =
                binding.mainPreferencesAdBlockingInclude.wifiOnlyCheckbox
            binding.mainPreferencesAdBlockingInclude.crystalMainPreferencesUpdateSubscriptions.setOnClickListener {
                wifiOnlyCheckbox.isChecked = !wifiOnlyCheckbox.isChecked
                val updateConfigType = if (wifiOnlyCheckbox.isChecked) {
                    UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_ALWAYS
                } else {
                    UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_WIFI_ONLY
                }
                updateViewModel.setUpdateConfigType(updateConfigType)
            }

            updateViewModel.updateType.observe(this) { updateType ->
                wifiOnlyCheckbox.isChecked =
                    updateType.name == UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_ALWAYS.name
            }
        }
        // Languages "Add additional language"
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
        // Languages "Only browse in one language"
        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionSkip.setDebounceOnClickListener(
            {
                viewModel.markLanguagesOnboardingComplete(false)
            },
            lifecycleOwner
        )
        // Acceptable Ads
        binding.mainPreferencesAcceptableAdsInclude.mainPreferencesAcceptableAds.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToAcceptableAdsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
        // About
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
