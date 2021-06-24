package org.adblockplus.adblockplussbrowser.onboarding.ui

import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.FragmentOnboardingAcceptableAdsPageBinding

class AcceptableAdsPageFragment :
    DataBindingFragment<FragmentOnboardingAcceptableAdsPageBinding>(R.layout.fragment_onboarding_acceptable_ads_page) {

    override fun onBindView(binding: FragmentOnboardingAcceptableAdsPageBinding) {
        val headerInclude = binding.onboardingDefaultPageHeaderInclude
        headerInclude.onboardingHeaderTitle1.setText(R.string.onboarding_acceptable_ads_header_title1)
        headerInclude.onboardingHeaderTitle2.setText(R.string.onboarding_acceptable_ads_header_title2)
        headerInclude.onboardingHeaderTitle3.setText(R.string.onboarding_acceptable_ads_header_title3)
        binding.onboardingAcceptableAdsNonintrusiveAdsExample.setOnClickListener {
            AcceptableAdsDialogFragment().show(parentFragmentManager, null)
        }
    }
}