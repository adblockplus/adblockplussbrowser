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
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.checkbox.MaterialCheckBox
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
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

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    override fun onBindView(binding: FragmentMainPreferencesBinding) {
        binding.viewModel = viewModel
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        supportActionBar?.subtitle = getString(R.string.app_subtitle)

        val lifecycleOwner = this.viewLifecycleOwner

        bindPrimarySubscriptions(binding, supportActionBar, lifecycleOwner)
        bindOtherSubscriptions(binding, supportActionBar, lifecycleOwner)
        bindAllowList(binding, supportActionBar, lifecycleOwner)

        if (BuildConfig.FLAVOR_product != BuildConfig.FLAVOR_CRYSTAL) {
            bindUpdateSubscriptions(binding, supportActionBar, lifecycleOwner)
        } else {
            bindCrystalUpdateTypeSettings(binding)
        }

        bindAdditionalLanguage(binding, supportActionBar, lifecycleOwner)
        bindOnboardingLanguages(binding, lifecycleOwner)
        bindAcceptableAds(binding, supportActionBar, lifecycleOwner)
        bindAbout(binding, supportActionBar, lifecycleOwner)

        if (BuildConfig.FLAVOR_product == BuildConfig.FLAVOR_ABP) {
            binding.mainPreferencesShareEventsInclude.mainPreferencesIssueReporterCategory.setDebounceOnClickListener({
                analyticsProvider.logEvent(AnalyticsEvent.OPEN_ISSUE_REPORTER)
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToReportIssueFragment()
                findNavController().navigate(direction)
            }, lifecycleOwner)
        } else {
            binding.mainPreferencesShareEventsInclude.mainPreferencesIssueReporterCategory.visibility = View.GONE
            binding.mainPreferencesShareEventsInclude.mainPreferencesDivider1.visibility = View.GONE
        }
    }

    private fun bindAbout(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesAboutInclude.mainPreferencesAbout.setDebounceOnClickListener({
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAboutFragment()
            findNavController().navigate(direction)
        }, lifecycleOwner)
    }

    private fun bindAcceptableAds(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesAcceptableAdsInclude.mainPreferencesAcceptableAds.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToAcceptableAdsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
    }

    private fun bindOnboardingLanguages(
        binding: FragmentMainPreferencesBinding,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionSkip
            .setDebounceOnClickListener(
                {
                    viewModel.markLanguagesOnboardingComplete(false)
                },
                lifecycleOwner
            )
    }

    private fun bindAdditionalLanguage(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionAdd
            .setDebounceOnClickListener(
                {
                    supportActionBar?.subtitle = null
                    viewModel.markLanguagesOnboardingComplete(true)
                    val direction = MainPreferencesFragmentDirections
                        .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
                    findNavController().navigate(direction)
                },
                lifecycleOwner
            )
    }

    private fun bindCrystalUpdateTypeSettings(binding: FragmentMainPreferencesBinding) {
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

    private fun bindUpdateSubscriptions(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
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
    }

    private fun bindOtherSubscriptions(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToOtherSubscriptionsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
    }

    private fun bindAllowList(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
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
    }

    private fun bindPrimarySubscriptions(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLanguagesOnboarding()
    }
}
