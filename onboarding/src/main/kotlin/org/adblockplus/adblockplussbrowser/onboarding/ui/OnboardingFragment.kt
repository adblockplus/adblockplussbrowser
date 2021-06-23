package org.adblockplus.adblockplussbrowser.onboarding.ui

import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.FragmentOnboardingBinding

@AndroidEntryPoint
internal class OnboardingFragment : DataBindingFragment<FragmentOnboardingBinding>(R.layout.fragment_onboarding) {

    val viewModel: OnboardingViewModel by activityViewModels()

    override fun onBindView(binding: FragmentOnboardingBinding) {
        binding.viewModel = viewModel

        binding.onboardingPager.adapter = OnboardingPagerAdapter(this)

        binding.onboardingButton.setOnClickListener {
            val position = binding.onboardingPager.currentItem
            val pageCount = binding.onboardingPager.adapter!!.itemCount
            val lastPage = pageCount == 0 || position == pageCount - 1
            if (lastPage) {
                viewModel.completeOnboarding()
            } else {
                binding.onboardingPager.currentItem = position + 1
            }
        }
    }
}