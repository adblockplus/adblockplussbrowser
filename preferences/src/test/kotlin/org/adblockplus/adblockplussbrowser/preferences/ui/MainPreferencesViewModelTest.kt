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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.adblockplus.adblockplusbrowser.testutils.FakeAnalyticsProvider
import org.adblockplus.adblockplusbrowser.testutils.FakeSettingsRepository
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainPreferencesViewModelTest {

    private lateinit var mainPreferencesViewModel: MainPreferencesViewModel
    private val analyticsProvider = FakeAnalyticsProvider()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mainPreferencesViewModel = MainPreferencesViewModel(
            settingsRepository = FakeSettingsRepository("")
        )
        mainPreferencesViewModel.analyticsProvider = analyticsProvider
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
