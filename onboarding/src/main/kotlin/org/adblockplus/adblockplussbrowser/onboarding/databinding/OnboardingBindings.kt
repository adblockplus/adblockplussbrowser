package org.adblockplus.adblockplussbrowser.onboarding.databinding

import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.BaseDotsIndicator
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.ui.OnboardingPagerAdapter
import org.adblockplus.adblockplussbrowser.onboarding.ui.PageInfo
import kotlin.math.ceil

@BindingAdapter("indicatorPager2")
internal fun bindIndicatorPager2(indicator: BaseDotsIndicator, viewPager2: ViewPager2) {
    indicator.setViewPager2(viewPager2)
}

@BindingAdapter("progressPager2")
internal fun bindProgressPager2(progressBar: ProgressBar, viewPager2: ViewPager2) {
    viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val positionValue = position + positionOffset
            val positionMaxValue = viewPager2.adapter!!.itemCount - 1
            val progress = ceil((positionValue * progressBar.max) / positionMaxValue).toInt()
            progressBar.progress = progress
        }
    })
}

@BindingAdapter("onboardingButtonPager2")
internal fun bindOnboardingButtonPager2(button: ImageButton, viewPager2: ViewPager2) {
    viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (positionOffsetPixels == 0) {
                val lastPage = position == viewPager2.adapter!!.itemCount - 1
                val drawableRes = if (lastPage) R.drawable.ic_round_check_24 else R.drawable.ic_round_arrow_forward_24
                button.setImageResource(drawableRes)
            }
        }
    })
}

@BindingAdapter("onboardingPages")
internal fun bindOnboardingPages(viewPager2: ViewPager2, pageList: List<PageInfo>?) {
    val pageCount = pageList?.size ?: 0
    // Since we have a low number of pages for the onboarding, we load them all beforehand
    // This is to avoid flickering when animating to the next page
    if (pageCount > 0) {
        viewPager2.offscreenPageLimit = pageCount
    }
    (viewPager2.adapter as OnboardingPagerAdapter).submitList(pageList)
}