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
            is PageInfo.AcceptableAds -> AcceptableAdsPageFragment()
            is PageInfo.Enable -> EnablePageFragment()
        }

    private class DiffItemCallback : DiffUtil.ItemCallback<PageInfo>() {

        override fun areItemsTheSame(oldItem: PageInfo, newItem: PageInfo): Boolean =
            oldItem == newItem

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: PageInfo, newItem: PageInfo): Boolean =
            oldItem == newItem
    }
}