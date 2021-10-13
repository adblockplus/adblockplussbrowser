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

import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.FragmentOnboardingAcceptableAdsPageBinding
import javax.inject.Inject

@AndroidEntryPoint
class AcceptableAdsPageFragment :
    DataBindingFragment<FragmentOnboardingAcceptableAdsPageBinding>(R.layout.fragment_onboarding_acceptable_ads_page) {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    override fun onBindView(binding: FragmentOnboardingAcceptableAdsPageBinding) {
        val headerInclude = binding.onboardingAaHeaderInclude
        headerInclude.onboardingHeaderTitle1.setText(R.string.onboarding_acceptable_ads_header_title1)
        headerInclude.onboardingHeaderTitle2.setText(R.string.onboarding_acceptable_ads_header_title2)
        headerInclude.onboardingHeaderTitle3.setText(R.string.onboarding_acceptable_ads_header_title3)
        binding.onboardingAaNonintrusiveAdsExample.setOnClickListener {
            AcceptableAdsDialogFragment().show(parentFragmentManager, null)
            analyticsProvider.logEvent(AnalyticsEvent.ONBOARDING_AA_SHOW_ME_EXAMPLES)
        }
    }
}