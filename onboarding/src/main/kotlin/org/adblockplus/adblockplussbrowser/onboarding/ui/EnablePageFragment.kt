package org.adblockplus.adblockplussbrowser.onboarding.ui

import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.FragmentOnboardingEnablePageBinding

class EnablePageFragment :
    DataBindingFragment<FragmentOnboardingEnablePageBinding>(R.layout.fragment_onboarding_enable_page) {

    override fun onBindView(binding: FragmentOnboardingEnablePageBinding) {
        val headerInclude = binding.onboardingDefaultPageHeaderInclude
        headerInclude.onboardingHeaderTitle1.setText(R.string.onboarding_enable_header_title1)
        headerInclude.onboardingHeaderTitle2.setText(R.string.onboarding_enable_header_title2)
        headerInclude.onboardingHeaderTitle3.setText(R.string.onboarding_welcome_header_title3)
    }
}