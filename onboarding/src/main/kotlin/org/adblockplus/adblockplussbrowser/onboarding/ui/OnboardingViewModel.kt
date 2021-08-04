package org.adblockplus.adblockplussbrowser.onboarding.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.data.ValueWrapper
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.data.prefs.OnboardingPreferences
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
                R.string.onboarding_welcome_header_title2,
                R.string.onboarding_welcome_header_title3,
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