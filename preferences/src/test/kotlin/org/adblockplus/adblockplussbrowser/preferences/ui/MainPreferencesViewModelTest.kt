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

import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.preferences.helpers.FakeAnalyticsProvider
import org.adblockplus.adblockplussbrowser.preferences.helpers.FakeSettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MainPreferencesViewModelTest {

    private lateinit var mainPreferencesViewModel: MainPreferencesViewModel
    private val analyticsProvider = FakeAnalyticsProvider()

    @Before
    fun setUp() {
        mainPreferencesViewModel = MainPreferencesViewModel(
            settingsRepository = FakeSettingsRepository("")
        )
        mainPreferencesViewModel.analyticsProvider = analyticsProvider
    }

    @Test
    fun testStartGuideStarted() {
        mainPreferencesViewModel.logStartGuideStarted()
        assertEquals(AnalyticsEvent.TOUR_STARTED, analyticsProvider.event)
    }

    @Test
    fun testStartGuideCompleted() {
        mainPreferencesViewModel.logStartGuideCompleted()
        assertEquals(AnalyticsEvent.TOUR_COMPLETED, analyticsProvider.event)
    }

    @Test
    fun testStartGuideSkipped() {
        mainPreferencesViewModel.logStartGuideSkipped(1)
        assertEquals(AnalyticsEvent.TOUR_SKIPPED, analyticsProvider.event)
        assertEquals(
            "{ \"skippedAtStep\": 1 }",
            analyticsProvider.event?.data)
    }
}
