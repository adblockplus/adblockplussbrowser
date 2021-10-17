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

package org.adblockplus.adblockplussbrowser.preferences.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent.LANGUAGES_CARD_ADD
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent.LANGUAGES_CARD_NO
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class MainPreferencesViewModel @Inject constructor(
    val settingsRepository: SettingsRepository,
) : ViewModel() {
    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    val languagesOnboardingCompleted = settingsRepository.settings.map { settings ->
        settings.languagesOnboardingCompleted
    }.asLiveData()

    val acceptableAdsEnabled = settingsRepository.settings.map { settings ->
        settings.acceptableAdsEnabled
    }.asLiveData()

    val shareEvents = settingsRepository.settings.map { settings ->
        settings.analyticsEnabled
    }.asLiveData()

    fun toggleAnalytics() {
        viewModelScope.launch {
            when (shareEvents.value) {
                true -> {
                    analyticsProvider.disable()
                    settingsRepository.setAnalyticsEnabled(false)
                }
                false -> {
                    analyticsProvider.enable()
                    settingsRepository.setAnalyticsEnabled(true)
                }
            }
        }
    }

    fun markLanguagesOnboardingComplete(wentAdding: Boolean) {
        viewModelScope.launch {
            settingsRepository.markLanguagesOnboardingCompleted()
        }
        analyticsProvider.logEvent(if (wentAdding) LANGUAGES_CARD_ADD else LANGUAGES_CARD_NO)
    }
}