package org.adblockplus.adblockplussbrowser.preferences.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.preferences.helpers.FakeAnalyticsProvider
import org.adblockplus.adblockplussbrowser.preferences.helpers.FakeSettingsRepository
import org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions.OtherSubscriptionsViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class OtherSubscriptionsViewModelTest {

    private lateinit var otherSubscriptionsViewModel: OtherSubscriptionsViewModel
    private val analyticsProvider = FakeAnalyticsProvider()

    // This rule is used to be able to listen to the changes of mutable live data as it
    // runs tasks synchronously
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        otherSubscriptionsViewModel = OtherSubscriptionsViewModel(
            settingsRepository = FakeSettingsRepository(""),
            subscriptionManager = Mockito.mock(SubscriptionsManager::class.java)
        )
        otherSubscriptionsViewModel.analyticsProvider = analyticsProvider
    }

    @Test
    fun testToggleAdditionalTracking() {
        otherSubscriptionsViewModel.toggleAdditionalTracking()
        otherSubscriptionsViewModel.activeSubscriptions
    }

    @Test
    fun testToggleSocialMediaTracking() {

    }

    @Test
    fun testAddCustomUrl() {

    }

    @Test
    fun testAddCustomFilterFile() {

    }

    @Test
    fun testRemoveSubscription() {

    }

    @Test
    fun loadFileFromStorage() {

    }

    @Test
    fun testHandleFilePickingResult() {

    }

    @Test
    fun testLogCustomFilterListFromUrl() {
        otherSubscriptionsViewModel.logCustomFilterListFromUrl()
        assertEquals(AnalyticsEvent.LOAD_CUSTOM_FILTER_LIST_FROM_URL, analyticsProvider.event)
    }

    @Test
    fun testLogCustomFilterListFromFile() {
        otherSubscriptionsViewModel.logCustomFilterListFromUrl()
        assertEquals(AnalyticsEvent.LOAD_CUSTOM_FILTER_LIST_FROM_URL, analyticsProvider.event)
    }
}