package org.adblockplus.adblockplussbrowser.onboarding.ui

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import org.adblockplus.adblockplussbrowser.base.widget.ListFragmentStateAdapter

internal class OnboardingPagerAdapter(fragment: Fragment) :
    ListFragmentStateAdapter<PageInfo>(DiffItemCallback(), fragment) {

    override fun createFragment(position: Int): Fragment {
        return getItem(position).createFragment()
    }

    private fun PageInfo.createFragment(): Fragment =
        when (this) {
            is PageInfo.Default -> DefaultPageFragment.newInstance(this)
        }

    private class DiffItemCallback : DiffUtil.ItemCallback<PageInfo>() {

        override fun areItemsTheSame(oldItem: PageInfo, newItem: PageInfo): Boolean =
            oldItem == newItem

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: PageInfo, newItem: PageInfo): Boolean =
            oldItem == newItem
    }
}