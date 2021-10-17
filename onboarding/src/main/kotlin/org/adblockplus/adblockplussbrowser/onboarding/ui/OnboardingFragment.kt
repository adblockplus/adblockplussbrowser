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

package org.adblockplus.adblockplussbrowser.onboarding.ui

import android.os.Bundle
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.samsung.SISettingsHelper
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.FragmentOnboardingBinding

@AndroidEntryPoint
internal class OnboardingFragment : DataBindingFragment<FragmentOnboardingBinding>(R.layout.fragment_onboarding) {

    val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!viewModel.previousPage()) {
                requireActivity().finish()
            }
        }
    }

    override fun onBindView(binding: FragmentOnboardingBinding) {
        binding.viewModel = viewModel

        binding.onboardingPager.adapter = OnboardingPagerAdapter(this)

        binding.onboardingButton.setOnClickListener {
            viewModel.nextPage()
        }

        binding.openSiButton.setOnClickListener {
            viewModel.completeOnboarding()
            SISettingsHelper.openSISettings(requireContext())
        }
    }
}
