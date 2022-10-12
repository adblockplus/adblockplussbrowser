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

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import java.io.FileNotFoundException
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.CustomSubscriptionType
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.preferences.helpers.FakeAnalyticsProvider
import org.adblockplus.adblockplussbrowser.preferences.helpers.FakeSettingsRepository
import org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions.OtherSubscriptionsItem
import org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions.OtherSubscriptionsViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner


@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class OtherSubscriptionsViewModelTest {

    private lateinit var otherSubscriptionsViewModel: OtherSubscriptionsViewModel
    private val analyticsProvider = FakeAnalyticsProvider()
    private val subscriptionsManager = Mockito.mock(SubscriptionsManager::class.java)
    private val testDispatcher = StandardTestDispatcher()
    private val applicationContext: Context = Mockito.mock(
        ApplicationProvider.getApplicationContext<Context>()::class.java)
    private val contentResolver: ContentResolver = Mockito.mock(ContentResolver::class.java)

    // This rule is used to be able to listen to the changes of mutable live data as it
    // runs tasks synchronously
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        otherSubscriptionsViewModel = OtherSubscriptionsViewModel(
            settingsRepository = FakeSettingsRepository(""),
            subscriptionManager = subscriptionsManager
        )
        otherSubscriptionsViewModel.analyticsProvider = analyticsProvider
        `when`(applicationContext.contentResolver).thenReturn(contentResolver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test Enable AdditionalTracking`() {
        runTest {
            // Advances the testScheduler to the point where there are no tasks remaining.
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
    fun `test AddCustomUrl Success`() {
        runTest {
            `when`(subscriptionsManager.validateSubscription(any())).thenReturn(true)
            otherSubscriptionsViewModel.addCustomUrl("www.example.com")
        }
        assertEquals(AnalyticsEvent.CUSTOM_FILTER_LIST_ADDED_FROM_URL, analyticsProvider.event)
    }

    @Test
    fun `test AddCustomUrl Fails`() {
        runTest {
            `when`(subscriptionsManager.validateSubscription(any())).thenReturn(false)
            otherSubscriptionsViewModel.addCustomUrl("www.example.com")
            assertNotNull(otherSubscriptionsViewModel.errorFlow.first())
        }
        assertNull(analyticsProvider.event)
    }

    @Test
    fun `test AddCustomFilterFile Success`() {
        val uri = Uri.parse("/tmp/filesDir/testFile.txt")
        runTest {
            val fileOutputStream = Mockito.mock(FileOutputStream::class.java)
            `when`(applicationContext.openFileOutput(anyString(), anyInt())).thenReturn(fileOutputStream)

            otherSubscriptionsViewModel.addCustomFilterFile(uri, applicationContext)
        }
        assertEquals(AnalyticsEvent.CUSTOM_FILTER_LIST_ADDED_FROM_FILE, analyticsProvider.event)
    }

    @Test
    fun `test AddCustomFilterFile Failure`() {
        val uri = Uri.parse("/tmp/filesDir/testFile.txt")
        runTest {
            `when`(applicationContext.openFileOutput(anyString(), anyInt()))
                .thenThrow(FileNotFoundException::class.java)

            otherSubscriptionsViewModel.addCustomFilterFile(uri, applicationContext)
            assertNotNull(otherSubscriptionsViewModel.errorFlow.first())
        }
        assertNull(analyticsProvider.event)
    }

    @Test
    fun `test RemoveSubscription`() {
        runTest {
            val customItem = OtherSubscriptionsItem.CustomItem(
                Subscription(
                    "/tmp/filesDir/testFile.txt",
                    "/tmp/filesDir/testFile.txt",
                    0L,
                    CustomSubscriptionType.LOCAL_FILE
                ),
                GroupItemLayout.LAST
            )
            otherSubscriptionsViewModel.removeSubscription(customItem, applicationContext)
        }
        assertEquals(AnalyticsEvent.CUSTOM_FILTER_LIST_REMOVED, analyticsProvider.event)
    }

    @Test
    fun `test HandleFilePickingResult Success`() {
        val activityResult = ActivityResult(
            Activity.RESULT_OK,
            Intent().setData(Uri.parse("/tmp/filesDir/testFile.txt"))
        )
        runTest {
            val fileOutputStream = Mockito.mock(FileOutputStream::class.java)
            `when`(applicationContext.openFileOutput(anyString(), anyInt())).thenReturn(fileOutputStream)

            otherSubscriptionsViewModel.handleFilePickingResult(activityResult, applicationContext)
        }
        assertEquals(AnalyticsEvent.CUSTOM_FILTER_LIST_ADDED_FROM_FILE, analyticsProvider.event)
    }

    @Test
    fun `test HandleFilePickingResult Error`() {
        val activityResult = ActivityResult(
            Activity.RESULT_CANCELED,
            Intent().setData(Uri.parse("/tmp/filesDir/testFile.txt"))
        )
        runTest {
            otherSubscriptionsViewModel.handleFilePickingResult(activityResult, applicationContext)
            assertNotNull(otherSubscriptionsViewModel.activityCancelledFlow.first())
        }
        assertEquals(AnalyticsEvent.DEVICE_FILE_MANAGER_NOT_SUPPORTED_OR_CANCELED, analyticsProvider.event)
    }

    @Test
    fun testLogCustomFilterListFromUrl() {
        otherSubscriptionsViewModel.logCustomFilterListFromUrl()
        assertEquals(AnalyticsEvent.LOAD_CUSTOM_FILTER_LIST_FROM_URL, analyticsProvider.event)
    }

    @Test
    fun testLogCustomFilterListFromFile() {
        otherSubscriptionsViewModel.logCustomFilterListFromFile()
        assertEquals(AnalyticsEvent.LOAD_CUSTOM_FILTER_LIST_FROM_FILE, analyticsProvider.event)
    }
}
