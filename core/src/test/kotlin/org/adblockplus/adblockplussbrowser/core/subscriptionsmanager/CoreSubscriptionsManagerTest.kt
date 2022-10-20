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

package org.adblockplus.adblockplussbrowser.core.subscriptionsmanager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.core.CoreSubscriptionsManager
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.di.CoreModule
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.provider.TestModule
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounter
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.di.SettingsModule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Singleton
import kotlin.time.ExperimentalTime
import org.adblockplus.adblockplussbrowser.analytics.helpers.test.FakeAnalyticsProvider
import org.adblockplus.adblockplussbrowser.settings.helpers.test.FakeSettingsRepository

@ExperimentalTime
@Config(
    application = HiltTestApplication::class,
    sdk = [21],
    manifest = "./src/test/AndroidManifest.xml"
)
@RunWith(RobolectricTestRunner::class)
@UninstallModules(CoreModule::class, SettingsModule::class, TestModule::class)
@HiltAndroidTest
class CoreSubscriptionsManagerTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    lateinit var coreSubscriptionsManager: CoreSubscriptionsManager
    lateinit var mockWorkManager: WorkManager

    @Before
    fun setUp() {
        mockWorkManager = mock()
        val context = ApplicationProvider.getApplicationContext<Context>()
        coreSubscriptionsManager = CoreSubscriptionsManager(context, workManager = mockWorkManager)
        coreSubscriptionsManager.initialize()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    @ExperimentalTime
    internal class CoreSubscriptionsManagerTestModule {
        @Provides
        @Singleton
        fun getCoreRepository(): CoreRepository {
            val coreRepository = Fakes.FakeCoreRepository("")
            // Last user count was done right now
            val lastUserCountingDate = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
                .format(Date(System.currentTimeMillis()))
            coreRepository.lastUserCountingResponse =
                OkHttpUserCounter.parseDateString(lastUserCountingDate, getAnalyticsProvider()).toLong()
            return coreRepository
        }

        @Provides
        @Singleton
        fun getSettingsRepository(): SettingsRepository {
            val settingsRepository = FakeSettingsRepository("")
            settingsRepository.acceptableAdsStatus = false
            return settingsRepository
        }

        @Provides
        @Singleton
        fun provideSubscriptionManager(): SubscriptionsManager = Mockito.mock(SubscriptionsManager::class.java)

        @Provides
        @Singleton
        fun getActivationPreferences(): ActivationPreferences {
            return Fakes.FakeActivationPreferences()
        }

        @Provides
        @Singleton
        fun getAnalyticsProvider(): AnalyticsProvider {
            return FakeAnalyticsProvider()
        }

        @Provides
        @Singleton
        fun provideSubscriptionDownloader(): Downloader = Mockito.mock(Downloader::class.java)

        @Provides
        @Singleton
        fun provideUserCounter(): UserCounter = Mockito.mock(UserCounter::class.java)

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = Mockito.mock(OkHttpClient::class.java)


        @Provides
        @Singleton
        fun provideOkHttpClientLogger() =
            HttpLoggingInterceptor().apply {
                if (org.adblockplus.adblockplussbrowser.analytics.BuildConfig.DEBUG) {
                    level = HttpLoggingInterceptor.Level.HEADERS // The default is Level.NONE
                }
            }
    }

    @Test
    fun `test schedule immediate`() {
        coreSubscriptionsManager.scheduleImmediate(true)

        verify(mockWorkManager).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
    }

    @Test
    fun `test schedule not immediate`() {
        coreSubscriptionsManager.scheduleImmediate(false)

        verify(mockWorkManager).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
    }
}

