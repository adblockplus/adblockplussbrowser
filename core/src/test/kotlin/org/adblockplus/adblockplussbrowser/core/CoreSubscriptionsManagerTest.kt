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
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.di.CoreModule
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.provider.TestModule
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounter
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.di.SettingsModule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

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

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val mockWebServer = MockWebServer()

    private val serverTimeZone: TimeZone = TimeZone.getTimeZone("GMT")
    private val serverDateParser = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
        Locale.ENGLISH)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
//        WorkManager.initialize(context, myConfig)
        mockWebServer.start()
//        val settings = Fakes.FakeSettingsRepository(mockWebServer.url("").toString())
//        val appInfo = AppInfo()
//        analyticsProvider = Fakes.FakeAnalyticsProvider()
//        fakeCoreRepository = Fakes.FakeCoreRepository(mockWebServer.url("").toString())
//        fakeSettingsRepository = Fakes.FakeSettingsRepository("")
//        userCounter = OkHttpUserCounter(OkHttpClient(), fakeCoreRepository, settings, appInfo,
//            analyticsProvider)
//        serverDateParser.timeZone = serverTimeZone
//        coreSubscriptionsManager = CoreSubscriptionsManager(testContext)
    }


    @After
    fun tearDown() {
        mockWebServer.shutdown()
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
            val settingsRepository =  Fakes.FakeSettingsRepository("")
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
            return Fakes.FakeAnalyticsProvider()
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


//        @Provides
//        @Singleton
//        fun provideOkHttpClientLogger() =
//            HttpLoggingInterceptor().apply {
//                if (org.adblockplus.adblockplussbrowser.analytics.BuildConfig.DEBUG) {
//                    level = HttpLoggingInterceptor.Level.HEADERS // The default is Level.NONE
//                }
//            }
    }



    @Test
    fun `test counting success`() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context,config)
        val manager = WorkManager.getInstance(context)

        assertEquals(manager.getWorkInfosForUniqueWork(UpdateSubscriptionsWorker.UPDATE_KEY_ONESHOT_WORK),"")

        val response = MockResponse()
            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01 GMT") //202109231731
        mockWebServer.enqueue(response)
        assertEquals(0, mockWebServer.requestCount)

//        var coreSubscriptionsManager = CoreSubscriptionsManager(context)
//        coreSubscriptionsManager.initialize()
//        coreSubscriptionsManager.scheduleImmediate(true)
//        assertEquals(manager.getWorkInfosForUniqueWork(UpdateSubscriptionsWorker.UPDATE_KEY_ONESHOT_WORK),"")



//        runBlocking {
//            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Success)
//        }
//        assertEquals(true.toString(), analyticsProvider.userPropertyValue)
//        assertEquals(1, mockWebServer.requestCount)
//        assertEquals(202109231731, fakeCoreRepository.lastUserCountingResponse)
//        assertEquals(1, fakeCoreRepository.userCountingCount)
//        assertNull(analyticsProvider.event)
    }



//
//    @Test
//    fun `test counting success`() {
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
//    fun `test counting when Acceptable Ads are disabled`() {
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
//    fun `test counting Http 500`() {
//        val response = MockResponse()
//            .setResponseCode(HTTP_INTERNAL_ERROR)
//        mockWebServer.enqueue(response)
//
//        assertEquals(0, mockWebServer.requestCount)
//        runBlocking {
//            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Failed)
//        }
//        assertEquals(1, mockWebServer.requestCount)
//        assertEquals(Fakes.INITIAL_TIMESTAMP,
//            fakeCoreRepository.lastUserCountingResponse)
//        assertEquals(Fakes.INITIAL_COUNT, fakeCoreRepository.userCountingCount)
//        assertNull(analyticsProvider.event)
//        assertEquals(analyticsProvider.error, "$HTTP_ERROR_LOG_HEADER_USER_COUNTER $HTTP_ERROR_MOCK_500")
//    }
//
//    @Test
//    fun `test counting date wrong format`() {
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
//            assertEquals(Fakes.INITIAL_TIMESTAMP,
//                fakeCoreRepository.lastUserCountingResponse)
//            assertEquals(Fakes.INITIAL_COUNT, fakeCoreRepository.userCountingCount)
//        } else {
//            assert(fakeCoreRepository.userCountingCount == 1)
//        }
//        assertTrue(analyticsProvider.exception is ParseException)
//    }
//
//    @Test
//    fun `test counting no header`() {
//        val response = MockResponse()
//        mockWebServer.enqueue(response)
//        if (BuildConfig.DEBUG) {
//            assertThrows(ParseException::class.java) {
//                runBlocking { userCounter.count(CallingApp("", "")) }
//            }
//        } else {
//            runBlocking { assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Success) }
//        }
//    }
}

