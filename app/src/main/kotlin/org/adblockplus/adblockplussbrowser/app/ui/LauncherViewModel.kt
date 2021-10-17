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

package org.adblockplus.adblockplussbrowser.app.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.data.prefs.AppPreferences
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences.Companion.isFilterRequestExpired
import javax.inject.Inject

@HiltViewModel
internal class LauncherViewModel @Inject constructor(appPreferences: AppPreferences) : ViewModel() {
    private val onBoardingCompletedFlow = appPreferences.onboardingCompleted
    private val lastFilterRequestFlow = appPreferences.lastFilterListRequest

    fun fetchDirection(): MutableLiveData<LauncherDirection> {
        val navigationDirection = MutableLiveData<LauncherDirection>()
        viewModelScope.launch {
            onBoardingCompletedFlow.zip(lastFilterRequestFlow) { onBoardingCompleted, lastFilterRequest ->
                var direction = LauncherDirection.MAIN
                if (!onBoardingCompleted) {
                    direction = LauncherDirection.ONBOARDING
                } else if (onBoardingCompleted && lastFilterRequest == 0L) {
                    direction = LauncherDirection.ONBOARDING_LAST_STEP
                } else if (onBoardingCompleted && isFilterRequestExpired(lastFilterRequest)) {
                    direction = LauncherDirection.ONBOARDING_LAST_STEP
                }
                return@zip direction
            }.flowOn(Dispatchers.IO)
                .collect {
                    navigationDirection.postValue(it)
                }
        }
        return navigationDirection
    }
}