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
        assertEquals("1", analyticsProvider.event?.flag)
    }


}