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

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.app.ui.helpers.Fakes
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class MainViewModelTest {

    private lateinit var mainViewModel: MainViewModel
    private val packageManager = Mockito.mock(PackageManager::class.java)
    private val fakeSubscriptionsManager = Fakes.FakeSubscriptionsManager()
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private fun whenGetPackageInfo(packageName: String) =
        `when`(packageManager.getPackageInfo(packageName, 0))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mainViewModel = MainViewModel(
            fakeSubscriptionsManager,
            Fakes.CustomFakeAppPreferences()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test updateSubscriptions`() {
        mainViewModel.updateSubscriptions()
        assertTrue(fakeSubscriptionsManager.forceSubscriptionsManager)
    }

    @Test
    fun `test fetchAdblockActivationStatus true`() {
        mainViewModel.fetchAdblockActivationStatus().observeForever {
            assertTrue(it)
        }
    }

    @Test
    fun `test fetchAdblockActivationStatus false`() {
        mainViewModel = MainViewModel(
            Fakes.FakeSubscriptionsManager(),
            Fakes.CustomFakeAppPreferences(customIsAdblockEnabled = false)
        )
        mainViewModel.fetchAdblockActivationStatus().observeForever {
            assertFalse(it)
        }
    }

    @Test
    fun `test shouldTriggerSamsungInstallation true`() {
        whenGetPackageInfo(SamsungInternetConstants.SBROWSER_APP_ID).thenThrow(
            PackageManager.NameNotFoundException()
        )
        whenGetPackageInfo(SamsungInternetConstants.SBROWSER_APP_ID_BETA).thenThrow(
            PackageManager.NameNotFoundException()
        )
        assertTrue(
            mainViewModel.shouldTriggerSamsungInstallation(packageManager)
        )
    }

    @Test
    fun `test shouldTriggerSamsungInstallation false because SI installed`() {
        whenGetPackageInfo(SamsungInternetConstants.SBROWSER_APP_ID).thenThrow(
            PackageManager.NameNotFoundException()
        )
        whenGetPackageInfo(SamsungInternetConstants.SBROWSER_APP_ID_BETA).thenReturn(PackageInfo())
        assertFalse(
            mainViewModel.shouldTriggerSamsungInstallation(packageManager)
        )
    }

    @Test
    fun `test logDeviceNotSupported`() {
        val fakeAnalyticsProvider = Fakes.FakeAnalyticsProvider()
        mainViewModel.analyticsProvider = fakeAnalyticsProvider
        mainViewModel.logDeviceNotSupported()
        assertEquals(AnalyticsEvent.DEVICE_NOT_SUPPORTED, fakeAnalyticsProvider.event)
    }
}
