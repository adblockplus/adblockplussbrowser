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