package org.adblockplus.adblockplussbrowser.onboarding.databinding

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.annotation.IdRes
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.ListenerUtil
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import com.tbuonomo.viewpagerdotsindicator.BaseDotsIndicator
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.ui.OnboardingPagerAdapter
import org.adblockplus.adblockplussbrowser.onboarding.ui.PageInfo
import kotlin.math.ceil

@BindingAdapter("onPageScrollSelected")
internal fun bindOnPageSelected(viewPager2: ViewPager2, onPageSelectedListener: OnPageScrollSelectedListener) {
    val listener = object : ViewPager2.OnPageChangeCallback() {
        var scrolling = false

        override fun onPageSelected(position: Int) {
            if (scrolling) {
                onPageSelectedListener.onPageScrollSelected(position)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            scrolling = state != ViewPager2.SCROLL_STATE_IDLE
        }
    }
    viewPager2.updateOnPageChangeCallback(listener, R.id.onPageSelectedListener)
}

@BindingAdapter("indicatorPager2")
internal fun bindIndicatorPager2(indicator: BaseDotsIndicator, viewPager2: ViewPager2) {
    indicator.setViewPager2(viewPager2)
}

@BindingAdapter("progressPager2")
internal fun bindProgressPager2(progressBar: ProgressBar, viewPager2: ViewPager2) {
    val listener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val positionValue = position + positionOffset
            val positionMaxValue = viewPager2.adapter!!.itemCount - 1
            val progress = ceil((positionValue * progressBar.max) / positionMaxValue).toInt()
            progressBar.progress = progress
            if (position == positionMaxValue) {
                progressBar.visibility = View.INVISIBLE
            } else {
                progressBar.visibility = View.VISIBLE
            }
        }
    }
    viewPager2.updateOnPageChangeCallback(listener, R.id.progressPager2Listener)
}

@BindingAdapter("onboardingButtonPager2")
internal fun bindOnboardingButtonPager2(button: ImageButton, viewPager2: ViewPager2) {
    val listener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (positionOffsetPixels == 0) {
                val lastPage = position == viewPager2.adapter!!.itemCount - 1
                if (lastPage) {
                    button.visibility = View.INVISIBLE
                } else {
                    button.visibility = View.VISIBLE
                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            if (state == SCROLL_STATE_DRAGGING) {
                button.visibility = View.VISIBLE
            }
        }
    }
    viewPager2.updateOnPageChangeCallback(listener, R.id.onboardingButtonPager2Listener)
}

@BindingAdapter("onboardingPages", "currentItem")
internal fun bindOnboardingPages(viewPager2: ViewPager2, pageList: List<PageInfo>?, currentItem: Int) {
    // Since we have a low number of pages for the onboarding, we load them all beforehand
    // This is to avoid flickering when animating to the next page
    val pageCount = pageList?.size ?: 0
    if (pageCount > 0) {
        viewPager2.offscreenPageLimit = pageCount
    }
    (viewPager2.adapter as OnboardingPagerAdapter).submitList(pageList)
    // Once we update the items, we also want to set the currentItem
    // This is to keep the correct item selected in the case of a configuration change
    viewPager2.currentItem = currentItem
}

@BindingAdapter("onboardingOpenSamsungButton")
internal fun bindOnboardingOpenSamsungButton(button: Button, viewPager2: ViewPager2) {
    val listener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            if (positionOffsetPixels == 0) {
                val lastPage = position == viewPager2.adapter!!.itemCount - 1
                if (lastPage) {
                    button.visibility = View.VISIBLE
                } else {
                    button.visibility = View.INVISIBLE
                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            if (state == SCROLL_STATE_DRAGGING) {
                button.visibility = View.INVISIBLE
            }
        }
    }
    viewPager2.updateOnPageChangeCallback(listener, R.id.onboardingButtonOpenSamsungListener)
}

private fun ViewPager2.updateOnPageChangeCallback(newListener: ViewPager2.OnPageChangeCallback, @IdRes idRes: Int) {
    val oldListener = ListenerUtil.trackListener(this, newListener, idRes)
    oldListener?.let { this.unregisterOnPageChangeCallback(oldListener) }
    this.registerOnPageChangeCallback(newListener)
}

internal interface OnPageScrollSelectedListener {
    fun onPageScrollSelected(position: Int)
}