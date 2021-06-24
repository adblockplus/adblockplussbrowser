package org.adblockplus.adblockplussbrowser.onboarding.ui

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewpager2.widget.ViewPager2
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

        binding.onboardingFakeFab.setOnClickListener {
            val position = binding.onboardingPager.currentItem
            val pageCount = binding.onboardingPager.adapter!!.itemCount
            val lastPage = pageCount == 0 || position == pageCount - 1
            if (lastPage) {
                viewModel.completeOnboarding()
            } else {
                binding.onboardingPager.currentItem = position + 1
            }
        }

        binding.onboardingPager.registerOnPageChangeCallback(pageChangeCallback)
    }

    override fun onDestroyView() {
        binding?.onboardingPager?.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroyView()
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding?.let {
                val pageCount = it.onboardingPager.adapter!!.itemCount
                if (position == pageCount - 1) {
                    expandFabButton(it)
                    //it.bottomContainer.transitionToEnd()
                } else if (position == pageCount - 2) {
                    colapseFabButton(it)
                    //it.bottomContainer.transitionToStart()
                }
            }
        }
    }

    private fun expandFabButton(binding: FragmentOnboardingBinding) {
        val transition = AutoTransition()
        transition.startDelay = 200
        transition.ordering = TransitionSet.ORDERING_TOGETHER
        //transition.duration = 1000
        TransitionManager.beginDelayedTransition(binding.onboardingBottomContainer, transition)
        binding.onboardingFakeFab.updateLayoutParams {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        binding.onboardingFakeFabIcon.updateLayoutParams<LinearLayout.LayoutParams> {
            marginEnd = resources.getDimensionPixelSize(R.dimen.check_mark_margin)
        }
        binding.onboardingFakeFabText.isVisible = true
        binding.onboardingFakeFabText.alpha = 1f
        binding.onboardingCircularIndicator.isInvisible = true
    }

    private fun colapseFabButton(binding: FragmentOnboardingBinding) {
        val transition = AutoTransition()
        transition.ordering = TransitionSet.ORDERING_TOGETHER
        //transition.duration = 1000
        TransitionManager.beginDelayedTransition(binding.onboardingBottomContainer, transition)
        binding.onboardingFakeFab.updateLayoutParams {
            width = resources.getDimensionPixelSize(R.dimen.fake_fab_width)
        }
        binding.onboardingFakeFabIcon.updateLayoutParams<LinearLayout.LayoutParams> {
            marginEnd = 0
        }
        binding.onboardingFakeFabText.isGone = true
        binding.onboardingFakeFabText.alpha = 0f
        binding.onboardingCircularIndicator.isVisible = true
    }
}