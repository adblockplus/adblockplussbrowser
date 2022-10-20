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

package org.adblockplus.adblockplussbrowser.core.provider

import android.content.ContentResolver
import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Singleton
import kotlin.time.ExperimentalTime
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.BuildConfig
import org.adblockplus.adblockplussbrowser.analytics.helpers.test.FakeAnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.di.CoreModule
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.downloader.OkHttpDownloader
import org.adblockplus.adblockplussbrowser.core.helpers.FakeActivationPreferences
import org.adblockplus.adblockplussbrowser.core.helpers.FakeCoreRepository
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounter
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.di.SettingsModule
import org.adblockplus.adblockplussbrowser.settings.helpers.test.FakeSettingsRepository
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver

@ExperimentalTime
@Config(
    application = HiltTestApplication::class,
    sdk = [21],
    manifest = "./src/test/AndroidManifest.xml"
)
@RunWith(RobolectricTestRunner::class)
@UninstallModules(CoreModule::class, SettingsModule::class, TestModule::class)
@HiltAndroidTest
class FilterListContentProviderAADisabledTest {
    private var contentResolver: ContentResolver? = null
    private var shadowContentResolver: ShadowContentResolver? = null
    private var filterListContentProvider: FilterListContentProvider? = null
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val uriSource =
        Uri.parse("content://org.adblockplus.adblockplussbrowser.contentBlocker.contentProvider")

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        contentResolver = context.contentResolver
        val providerInfo = ProviderInfo()
        providerInfo.authority =
            "org.adblockplus.adblockplussbrowser.contentBlocker.contentProvider"
        providerInfo.grantUriPermissions = true
        val controller = Robolectric.buildContentProvider(FilterListContentProvider::class.java)
            .create(providerInfo)
        shadowContentResolver = Shadows.shadowOf(contentResolver)
        filterListContentProvider = controller.get()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    @ExperimentalTime
    internal class CustomTestModule {
        @Provides
        @Singleton
        fun getCoreRepository(): CoreRepository {
            val coreRepository = FakeCoreRepository("")
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
            val settingsRepository =  FakeSettingsRepository("")
            settingsRepository.acceptableAdsStatus = false
            return settingsRepository
        }

        @Provides
        @Singleton
        fun provideSubscriptionManager(): SubscriptionsManager = Mockito.mock(SubscriptionsManager::class.java)

        @Provides
        @Singleton
        fun getActivationPreferences(): ActivationPreferences {
            return FakeActivationPreferences()
        }

        @Provides
        @Singleton
        fun getAnalyticsProvider(): AnalyticsProvider {
            return FakeAnalyticsProvider()
        }

        @Provides
        @Singleton
        fun provideSubscriptionDownloader(): Downloader = Mockito.mock(OkHttpDownloader::class.java)

        @Provides
        @Singleton
        fun provideUserCounter(): UserCounter = Mockito.mock(OkHttpUserCounter::class.java)

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = Mockito.mock(OkHttpClient::class.java)


        @Provides
        @Singleton
        fun provideOkHttpClientLogger() =
            HttpLoggingInterceptor().apply {
                if (BuildConfig.DEBUG) {
                    level = HttpLoggingInterceptor.Level.HEADERS // The default is Level.NONE
                }
            }
    }

    @Test
    fun `open File AA Disabled`() {
        // Open file and check result is not null
        val parcelFileDescriptor = filterListContentProvider?.openFile(uriSource, "r")
        assertNotNull(parcelFileDescriptor)
    }
}

