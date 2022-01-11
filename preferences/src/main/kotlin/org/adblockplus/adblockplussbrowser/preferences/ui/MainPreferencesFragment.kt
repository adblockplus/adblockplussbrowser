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
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding
import timber.log.Timber

@AndroidEntryPoint
internal class MainPreferencesFragment :
    DataBindingFragment<FragmentMainPreferencesBinding>(R.layout.fragment_main_preferences) {

    private val viewModel: MainPreferencesViewModel by viewModels()

    fun View.setDebounceOnClickListener(onClickListener: View.OnClickListener) {
        val thiz = this
        var debounceJob: Job? = null
        val clickWithDebounce: (view: View) -> Unit = {
            if (debounceJob == null) {
                debounceJob = viewModel.viewModelScope.launch {
                    onClickListener.onClick(thiz)
                    delay(1000L)
                    debounceJob = null
                }
            } else {
                Timber.d("Skipping MainPreferencesFragment menu onClick event")
            }
        }
        setOnClickListener(clickWithDebounce)
    }

    override fun onBindView(binding: FragmentMainPreferencesBinding) {
        binding.viewModel = viewModel
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        supportActionBar?.subtitle = getString(R.string.app_subtitle)

        binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions.setDebounceOnClickListener {
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
            findNavController().navigate(direction)
        }
        binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions.setDebounceOnClickListener {
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToOtherSubscriptionsFragment()
            findNavController().navigate(direction)
        }
        binding.mainPreferencesAdBlockingInclude.mainPreferencesAllowlist.setDebounceOnClickListener {
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAllowlistFragment()
            findNavController().navigate(direction)
        }
        binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.setDebounceOnClickListener {
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToUpdateSubscriptionsFragment()
            findNavController().navigate(direction)
        }
        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionAdd.setDebounceOnClickListener {
            supportActionBar?.subtitle = null
            viewModel.markLanguagesOnboardingComplete(true)
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
            findNavController().navigate(direction)
        }
        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionSkip.setDebounceOnClickListener {
            viewModel.markLanguagesOnboardingComplete(false)
        }
        binding.mainPreferencesAcceptableAdsInclude.mainPreferencesAcceptableAds.setDebounceOnClickListener {
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAcceptableAdsFragment()
            findNavController().navigate(direction)
        }
        binding.mainPreferencesAboutInclude.mainPreferencesAbout.setDebounceOnClickListener {
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAboutFragment()
            findNavController().navigate(direction)
        }
    }
}