package org.adblockplus.adblockplussbrowser.preferences.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.preferences.helpers.FakeAnalyticsProvider
import org.adblockplus.adblockplussbrowser.preferences.helpers.FakeSettingsRepository
import org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions.OtherSubscriptionsViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

@ExperimentalCoroutinesApi
class OtherSubscriptionsViewModelTest {

    private lateinit var otherSubscriptionsViewModel: OtherSubscriptionsViewModel
    private val analyticsProvider = FakeAnalyticsProvider()
    private val testDispatcher = StandardTestDispatcher()

    // This rule is used to be able to listen to the changes of mutable live data as it
    // runs tasks synchronously
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        otherSubscriptionsViewModel = OtherSubscriptionsViewModel(
            settingsRepository = FakeSettingsRepository(""),
            subscriptionManager = Mockito.mock(SubscriptionsManager::class.java)
        )
        otherSubscriptionsViewModel.analyticsProvider = analyticsProvider
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test Enable AdditionalTracking`() {
        runTest {
            advanceUntilIdle()
            otherSubscriptionsViewModel.toggleAdditionalTracking()
        }
        assertEquals(AnalyticsEvent.DISABLE_TRACKING_OFF, analyticsProvider.event)
    }

    @Test
    fun `test Disable AdditionalTracking`() {
        runTest {
            advanceUntilIdle()
            otherSubscriptionsViewModel.blockAdditionalTracking.value = true
            otherSubscriptionsViewModel.toggleAdditionalTracking()
        }
        assertEquals(AnalyticsEvent.DISABLE_TRACKING_ON, analyticsProvider.event)
    }

    @Test
    fun `test Enable SocialMediaTracking`() {
        runTest {
            advanceUntilIdle()
            otherSubscriptionsViewModel.toggleSocialMediaTracking()
        }
        assertEquals(AnalyticsEvent.SOCIAL_MEDIA_BUTTONS_OFF, analyticsProvider.event)
    }

    @Test
    fun `test Disable SocialMediaTracking`() {
        runTest {
            advanceUntilIdle()
            otherSubscriptionsViewModel.blockSocialMediaTracking.value = true
            otherSubscriptionsViewModel.toggleSocialMediaTracking()
        }
        assertEquals(AnalyticsEvent.SOCIAL_MEDIA_BUTTONS_ON, analyticsProvider.event)
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