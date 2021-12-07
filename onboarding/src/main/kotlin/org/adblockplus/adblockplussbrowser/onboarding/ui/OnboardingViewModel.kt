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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.data.ValueWrapper
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.base.data.prefs.OnboardingPreferences
import javax.inject.Inject

@HiltViewModel
internal class OnboardingViewModel @Inject constructor(
    private val preferences: OnboardingPreferences,
) : ViewModel() {

    private val _pages = MutableLiveData<List<PageInfo>>()
    val pages: LiveData<List<PageInfo>>
        get() = _pages

    private val _currentPageIndex = MutableLiveData<Int>()
    val currentPageIndex: LiveData<Int>
        get() = _currentPageIndex

    private val _finishedEvent = MutableLiveData<ValueWrapper<Unit>>()
    val finishedEvent: LiveData<ValueWrapper<Unit>>
        get() = _finishedEvent

    init {
        loadPages()
    }

    fun selectPage(index: Int) {
        _currentPageIndex.value = index
    }

    fun nextPage() {
        val index = currentPageIndex.value ?: 0
        _currentPageIndex.value = index + 1
    }

    fun previousPage(): Boolean {
        val index = currentPageIndex.value ?: 0
        if (index > 0) {
            _currentPageIndex.value = index - 1
            return true
        }
        return false
    }

    private fun loadPages() {
        val pageList = mutableListOf<PageInfo>()
        pageList.add(
            PageInfo.Default(
                R.string.onboarding_welcome_header_title1,
                R.string.product_name,
                R.string.app_subtitle,
                R.layout.onboarding_welcome_page
            )
        )

        pageList.add(PageInfo.AcceptableAds)
        pageList.add(PageInfo.Enable)

        _pages.value = pageList
    }

    fun completeOnboarding() {
        _finishedEvent.value = ValueWrapper(Unit)
        viewModelScope.launch {
            preferences.completeOnboarding()
        }
    }

}