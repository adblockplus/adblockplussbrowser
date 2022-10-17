package org.adblockplus.adblockplussbrowser.app.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.adblockplus.adblockplussbrowser.app.ui.helpers.Fakes
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment.getApplication

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class LauncherViewModelTest {

    private lateinit var launcherViewModel: LauncherViewModel
    private val fakeSettingsRepository = Fakes.FakeSettingsRepository("")
    private val fakeAnalyticsProvider = Fakes.FakeAnalyticsProvider()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        val fakeAppPreferences = Fakes.CustomFakeAppPreferences()
        launcherViewModel = LauncherViewModel(
            fakeAppPreferences,
            getApplication()
        )
        launcherViewModel.settingsRepository = fakeSettingsRepository
        launcherViewModel.analyticsProvider = fakeAnalyticsProvider
    }

    @Test
    fun `test fetchDirection ONBOARDING_LAST_STEP`() {
        launcherViewModel.fetchDirection().observeForever {
            assertEquals(it, LauncherDirection.ONBOARDING_LAST_STEP)
        }
    }

    @Test
    fun `test fetchDirection MAIN`() {
        val fakeAppPreferences = Fakes.CustomFakeAppPreferences(
            customLastFilterListRequest = System.currentTimeMillis())
        launcherViewModel = LauncherViewModel(fakeAppPreferences, getApplication())
        launcherViewModel.fetchDirection().observeForever {
            assertEquals(it, LauncherDirection.MAIN)
        }
    }

    @Test
    fun `test fetchDirection ONBOARDING`() {
        val fakeAppPreferences = Fakes.CustomFakeAppPreferences(customOnBoardingCompleted = false)
        launcherViewModel = LauncherViewModel(fakeAppPreferences, getApplication())
        launcherViewModel.fetchDirection().observeForever {
            assertEquals(it, LauncherDirection.ONBOARDING)
        }
    }

    @Test
    fun `test checkInstallReferrer`() {

    }
}