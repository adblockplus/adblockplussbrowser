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

import android.os.Bundle
import android.os.RemoteException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.ReferrerDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.adblockplus.adblockplusbrowser.testutils.FakeAnalyticsProvider
import org.adblockplus.adblockplusbrowser.testutils.FakeSettingsRepository
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.app.ui.helpers.CustomFakeAppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment.getApplication

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class LauncherViewModelTest {

    private lateinit var launcherViewModel: LauncherViewModel
    private lateinit var fakeAppPreferences: CustomFakeAppPreferences
    private val application = getApplication()
    private val fakeSettingsRepository = FakeSettingsRepository("")
    private val fakeAnalyticsProvider = FakeAnalyticsProvider()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        fakeAppPreferences = CustomFakeAppPreferences()
        launcherViewModel = LauncherViewModel(
            fakeAppPreferences,
            application
        )
        launcherViewModel.settingsRepository = fakeSettingsRepository
        launcherViewModel.analyticsProvider = fakeAnalyticsProvider
        launcherViewModel.appPreferences = fakeAppPreferences
    }

    @Test
    fun `test fetchDirection ONBOARDING_LAST_STEP`() {
        launcherViewModel.fetchDirection().observeForever {
            assertEquals(it, LauncherDirection.ONBOARDING_LAST_STEP)
        }
    }

    @Test
    fun `test fetchDirection MAIN`() {
        val fakeAppPreferences = CustomFakeAppPreferences(
            customLastFilterListRequest = System.currentTimeMillis())
        launcherViewModel = LauncherViewModel(fakeAppPreferences, getApplication())
        launcherViewModel.fetchDirection().observeForever {
            assertEquals(it, LauncherDirection.MAIN)
        }
    }

    @Test
    fun `test fetchDirection ONBOARDING`() {
        val fakeAppPreferences = CustomFakeAppPreferences(customOnBoardingCompleted = false)
        launcherViewModel = LauncherViewModel(fakeAppPreferences, getApplication())
        launcherViewModel.fetchDirection().observeForever {
            assertEquals(it, LauncherDirection.ONBOARDING)
        }
    }

    @Test
    fun `test handleInstallReferrerResponse Response OK`() {
        // Set up
        val installReferrerClient = Mockito.mock(InstallReferrerClient::class.java)
        val bundle = Bundle()
        bundle.putString("install_referrer", "utm_source=google-play&utm_medium=organic")
        `when`(installReferrerClient.installReferrer).thenReturn(ReferrerDetails(bundle))
        launcherViewModel.referrerClient = installReferrerClient

        // Run test
        launcherViewModel.handleInstallReferrerResponse(
            InstallReferrerClient.InstallReferrerResponse.OK
        )
        assertEquals(AnalyticsUserProperty.INSTALL_REFERRER, fakeAnalyticsProvider.userPropertyName)
        assertEquals("utm_source=google-play&utm_medium=organic", fakeAnalyticsProvider.userPropertyValue)
        assertTrue(fakeAppPreferences.referrerChecked)
    }

    @Test
    fun `test handleInstallReferrerResponse Response OK Remote Exception`() {
        // Set up
        val installReferrerClient = Mockito.mock(InstallReferrerClient::class.java)
        `when`(installReferrerClient.installReferrer).thenThrow(RemoteException("Error"))
        launcherViewModel.referrerClient = installReferrerClient

        // Run test
        launcherViewModel.handleInstallReferrerResponse(
            InstallReferrerClient.InstallReferrerResponse.OK
        )
        assertNull(fakeAnalyticsProvider.userPropertyName)
        assertNull(fakeAnalyticsProvider.userPropertyValue)
        assertFalse(fakeAppPreferences.referrerChecked)
    }

    @Test
    fun `test handleInstallReferrerResponse Feature Not Supported`() {
        launcherViewModel.handleInstallReferrerResponse(
            InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED
        )
        assertTrue(fakeAppPreferences.referrerChecked)
    }

    @Test
    fun `test checkInstallReferrer exception`() {
        // Set up
        val installReferrerClient = Mockito.mock(InstallReferrerClient::class.java)
        `when`(installReferrerClient.startConnection(any())).thenThrow(
            RuntimeException("Error")
        )
        launcherViewModel.referrerClient = installReferrerClient

        // Run test
        launcherViewModel.checkInstallReferrer()
        assertFalse(fakeAppPreferences.referrerChecked)
        assertEquals("Error", fakeAnalyticsProvider.exception?.message)
    }

    @Test
    fun `test checkInstallReferrer security exception`() {
        // Set up
        val installReferrerClient = Mockito.mock(InstallReferrerClient::class.java)
        `when`(installReferrerClient.startConnection(any())).thenThrow(
            SecurityException("Security Exception")
        )
        launcherViewModel.referrerClient = installReferrerClient

        // Run test
        launcherViewModel.checkInstallReferrer()
        assertTrue(fakeAppPreferences.referrerChecked)
        assertEquals("Security Exception", fakeAnalyticsProvider.exception?.message)
    }
}
