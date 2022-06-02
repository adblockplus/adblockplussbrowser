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
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import kotlin.time.ExperimentalTime
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.BuildConfig
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.core.AppInfo
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.di.CoreModule
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.downloader.OkHttpDownloader
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounter
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.junit.Rule
import org.mockito.Mockito

@ExperimentalTime
@Config(sdk=[21])
@RunWith(RobolectricTestRunner::class)
@UninstallModules(CoreModule::class)
@HiltAndroidTest
internal class FilterListContentProviderTest {
    private var contentResolver : ContentResolver? = null
    private var shadowContentResolver : ShadowContentResolver? = null
    private var filterListContentProvider : FilterListContentProvider? = null

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Module
    @InstallIn(SingletonComponent::class)
    internal class TestModule {
        @Provides
        @Singleton
        fun getCoreRepository(): CoreRepository {
            return Fakes.FakeCoreRepository("")
        }

        @Provides
        @Singleton
        fun getSettingsRepository(): SettingsRepository {
            return Fakes.FakeSettingsRepository("")
        }

        @Provides
        @Singleton
        fun getActivationPreferences(): ActivationPreferences {
            return Fakes.FakeActivationPreferences()
        }

        @Provides
        @Singleton
        fun getAnalyticsProvider(): AnalyticsProvider {
            return Fakes.FakeAnalyticsProvider()
        }

        @Provides
        @Singleton
        fun provideSubscriptionDownloader(
            @ApplicationContext context: Context,
            okHttpClient: OkHttpClient,
            appInfo: AppInfo,
            repository: CoreRepository,
            analyticsProvider: AnalyticsProvider
        ): Downloader =
            OkHttpDownloader(context, okHttpClient, repository, appInfo, analyticsProvider)

        @Provides
        @Singleton
        fun provideUserCounter(
            okHttpClient: OkHttpClient,
            appInfo: AppInfo,
            repository: CoreRepository,
            settings: SettingsRepository,
            analyticsProvider: AnalyticsProvider
        ): UserCounter =
            OkHttpUserCounter(okHttpClient, repository, settings, appInfo, analyticsProvider)

        @Provides
        @Singleton
        fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor) =
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

        @Provides
        @Singleton
        fun provideOkHttpClientLogger() =
            HttpLoggingInterceptor().apply {
                if (BuildConfig.DEBUG) {
                    level = HttpLoggingInterceptor.Level.HEADERS // The default is Level.NONE
                }
            }

        @Provides
        @Singleton
        fun provideAppInfo() = AppInfo()
    }

    @Before
    fun setUp() {
        contentResolver = ApplicationProvider.getApplicationContext<Context>().contentResolver
        val providerInfo = ProviderInfo()
        providerInfo.authority = "org.adblockplus.adblockplussbrowser.contentBlocker.contentProvider"
        providerInfo.grantUriPermissions = true
        val controller = Robolectric.buildContentProvider(FilterListContentProvider::class.java).create(providerInfo)
        shadowContentResolver = shadowOf(contentResolver)
        filterListContentProvider = controller.get()
    }

    @Test
    fun onCreate() {
        val res  = filterListContentProvider?.onCreate()
        assertEquals(res, true)
    }

    @Test
    fun insert() {
        val values = ContentValues()
        val uriSource = Uri.parse("content://org.adblockplus.adblockplussbrowser.contentBlocker.contentProvider")
        val uri = contentResolver?.insert(uriSource, values);
        assertNull(uri)
    }

//    @Test
    fun open() {
        val uriSource = Uri.parse("content://org.adblockplus.adblockplussbrowser.contentBlocker.contentProvider")
        val parcelFileDescriptor = filterListContentProvider?.openFile(uriSource, "r")
        assertNotNull(parcelFileDescriptor)
    }
}