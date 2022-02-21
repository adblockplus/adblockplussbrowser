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

package org.adblockplus.adblockplussbrowser.core

import android.content.Context
import android.net.Uri
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.di.CoreModule
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.downloader.OkHttpDownloader
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.provider.FilterListContentProvider
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounter
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@ExperimentalTime
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@UninstallModules(CoreModule::class)
@RunWith(RobolectricTestRunner::class)
class FilterListContentProviderTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    //    @BindValue
//    @JvmField
//    val analyticsProvider = Fakes.FakeAnalyticsProvider()

//    @Module
//    @InstallIn(SingletonComponent::class)
//    abstract class TestModule {
//
//        @Singleton
//        @Binds
//        abstract fun bindAnalyticsProvider(
//            fakeAnalyticsProvider: Fakes.FakeAnalyticsProvider
//        ): AnalyticsProvider
//    }


    @Module
    @InstallIn(SingletonComponent::class) // 1
//    @TestInstallIn(components = [SingletonComponent::class], replaces = [CoreModule::class])
    object TestAppModule {

//        @Provides
//        fun provideNewsRepository(): NewsRepository { // 2
//            return FakeNewsRepository().apply {
//                insert(News(1, "First Title", "First Body"))
//                insert(News(2, "Second Title", "Second Body"))
//                insert(News(3, "Third Title", "Third Body"))
//            }
//        }
//        fun provideNewsLogger(): RwNewsLogger = FakeNewsLogger() // 2

        @Provides
        @Singleton
        fun getCoreRepository(): CoreRepository = Fakes.FakeCoreRepository("")
        @Provides
        @Singleton
        fun getActivationPreferences(): ActivationPreferences = Fakes.FakeActivationPreferences()
        @Provides
        @Singleton
        fun getAnalyticsProvider(): AnalyticsProvider = Fakes.FakeAnalyticsProvider()

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
        fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor) =
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

        @ExperimentalTime
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

        @ExperimentalTime
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

//        @ExperimentalTime
//        @Provides
//        @Singleton
//        fun provideFakeAnalyticsProvider(): AnalyticsProvider = Fakes.FakeAnalyticsProvider()

    }
    @Inject
    lateinit var analyticsProvider: Fakes.FakeAnalyticsProvider

    @Inject
    lateinit var coreRepository: Fakes.FakeCoreRepository

    @Inject
    lateinit var activationPreferences: Fakes.FakeActivationPreferences

    //    @Inject
    private lateinit var filterListContentProvider: FilterListContentProvider

    @Before
    fun setUp() {
        hiltRule.inject()
//        val settings = Fakes.FakeSettingsRepository("")
//        val appInfo = AppInfo()
//        fakeActivationPreferences = Fakes.FakeActivationPreferences()
//        filterListContentProvider = FilterListContentProvider()

    }

    @After
    fun tearDown() {
    }

    @Test
    fun testUnpack() {
        filterListContentProvider = FilterListContentProvider()
        filterListContentProvider.openFile(Uri.fromParts("http", "", ""), "")
    }

//    fun testCountOK() {
//        val response = MockResponse()
//            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01 GMT") //202109231731
//        mockWebServer.enqueue(response)
//
//        assertEquals(0, mockWebServer.requestCount)
//        runBlocking {
//            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Success)
//        }
//        assertEquals(true.toString(), analyticsProvider.userPropertyValue)
//        assertEquals(1, mockWebServer.requestCount)
//        assertEquals(202109231731, fakeCoreRepository.lastUserCountingResponse)
//        assertEquals(1, fakeCoreRepository.userCountingCount)
//        assertNull(analyticsProvider.event)
//    }
//
//    @Test
//    fun `test count method when Acceptable Ads are disabled`() {
//        val response = MockResponse()
//            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01 GMT") //202109231731
//        mockWebServer.enqueue(response)
//        val settings = Fakes.FakeSettingsRepository(mockWebServer.url("").toString())
//        val appInfo = AppInfo()
//        assertEquals(0, mockWebServer.requestCount)
//        runBlocking {
//            // Given Acceptable Ads are disabled
//            settings.acceptableAdsStatus = false
//            // When the user is counted
//            userCounter = OkHttpUserCounter(OkHttpClient(), fakeCoreRepository, settings, appInfo,
//                analyticsProvider)
//            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Success)
//        }
//        // Then User property value is reported to Google Analytics
//        assertEquals(false.toString(), analyticsProvider.userPropertyValue)
//    }
//
//    @Test
//    fun testCountHttp500() {
//        val response = MockResponse()
//            .setResponseCode(HTTP_INTERNAL_ERROR)
//        mockWebServer.enqueue(response)
//
//        assertEquals(0, mockWebServer.requestCount)
//        runBlocking {
//            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Failed)
//        }
//        assertEquals(1, mockWebServer.requestCount)
//        assertEquals(fakeCoreRepository.INITIAL_TIMESTAMP,
//            fakeCoreRepository.lastUserCountingResponse)
//        assertEquals(fakeCoreRepository.INITIAL_COUNT, fakeCoreRepository.userCountingCount)
//        assertNull(analyticsProvider.event)
//        assertEquals(analyticsProvider.userPropertyName,
//            AnalyticsUserProperty.USER_COUNTING_HTTP_ERROR)
//        assertEquals(analyticsProvider.userPropertyValue, HTTP_INTERNAL_ERROR.toString())
//    }
//
//    @Test
//    fun testCountDateWrongFormat() {
//        val response = MockResponse()
//            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01") //No timezone (GMT)
//        mockWebServer.enqueue(response)
//
//        assertEquals(0, mockWebServer.requestCount)
//        runBlocking {
//            try {
//                assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Success)
//                if (BuildConfig.DEBUG) {
//                    fail() // In Debug mode we throw from count()
//                }
//            } catch (ex: Exception) {
//                assert(ex is ParseException)
//            }
//
//        }
//        assertEquals(1, mockWebServer.requestCount)
//        if (BuildConfig.DEBUG) {
//            assertEquals(fakeCoreRepository.INITIAL_TIMESTAMP,
//                fakeCoreRepository.lastUserCountingResponse)
//            assertEquals(fakeCoreRepository.INITIAL_COUNT, fakeCoreRepository.userCountingCount)
//        } else {
//            assert(fakeCoreRepository.userCountingCount == 1)
//        }
//        assertTrue(analyticsProvider.exception is ParseException)
//    }
}
