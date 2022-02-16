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

import android.net.Uri
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.provider.FilterListContentProvider
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.time.ExperimentalTime

//@UninstallModules(AnalyticsProvider::class)

@ExperimentalTime
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class FilterListContentProviderTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var analyticsProvider: Fakes.FakeAnalyticsProvider

//    @BindValue @JvmField
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
