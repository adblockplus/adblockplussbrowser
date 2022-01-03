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
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding
import timber.log.Timber

@AndroidEntryPoint
internal class MainPreferencesFragment :
    DataBindingFragment<FragmentMainPreferencesBinding>(R.layout.fragment_main_preferences) {

    private val viewModel: MainPreferencesViewModel by viewModels()

    override fun onBindView(binding: FragmentMainPreferencesBinding) {
        binding.viewModel = viewModel
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        supportActionBar?.subtitle = getString(R.string.app_subtitle)

        binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions
            .setOnClickListener(SingleClickListener(supportActionBar, findNavController(),
                MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToPrimarySubscriptionsFragment(), null))

        binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions
            .setOnClickListener(SingleClickListener(supportActionBar, findNavController(),
                MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToOtherSubscriptionsFragment(), null))

        binding.mainPreferencesAdBlockingInclude.mainPreferencesAllowlist
            .setOnClickListener(SingleClickListener(supportActionBar, findNavController(),
                MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAllowlistFragment(), null))

        binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions
            .setOnClickListener(SingleClickListener(supportActionBar, findNavController(),
                MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToUpdateSubscriptionsFragment(), null))

        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionAdd
            .setOnClickListener(SingleClickListener(supportActionBar, findNavController(),
                MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToPrimarySubscriptionsFragment(), viewModel))

        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionSkip
            .setOnClickListener {
            viewModel.markLanguagesOnboardingComplete(false)
        }

        binding.mainPreferencesAcceptableAdsInclude.mainPreferencesAcceptableAds
            .setOnClickListener(SingleClickListener(supportActionBar, findNavController(),
                MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToAcceptableAdsFragment(), null))

        binding.mainPreferencesAboutInclude.mainPreferencesAbout
            .setOnClickListener(SingleClickListener(supportActionBar, findNavController(),
                MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAboutFragment(), null))
    }

    private class SingleClickListener : View.OnClickListener {
        private val controller : NavController
        private val direction : NavDirections
        private val supportActionBar: ActionBar?
        private val viewModel: MainPreferencesViewModel?
        private var previousClickTimeMillis = 0L

        constructor(supportActionBar: ActionBar?, controller : NavController,
                    direction : NavDirections, viewModel: MainPreferencesViewModel?) {
            this.supportActionBar = supportActionBar
            this.controller = controller
            this.direction = direction
            this.viewModel = viewModel
        }

        override fun onClick(v: View) {
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis >= previousClickTimeMillis + DELAY_MILLIS) {
                previousClickTimeMillis = currentTimeMillis
                supportActionBar?.subtitle = null
                viewModel?.markLanguagesOnboardingComplete(true)
                controller.navigate(direction)
            } else {
                Timber.d("Skipping MainPreferencesFragment menu onClick event")
            }
        }

        companion object {
            private const val DELAY_MILLIS = 500L
        }
    }
}
