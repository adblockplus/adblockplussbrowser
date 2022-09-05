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

package org.adblockplus.adblockplussbrowser.core.usercounter

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.adblockplus.adblockplussbrowser.core.AppInfo
import org.adblockplus.adblockplussbrowser.core.BuildConfig
import org.adblockplus.adblockplussbrowser.core.CallingApp
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes.Companion.HTTP_ERROR_MOCK_500
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter.Companion.HTTP_ERROR_LOG_HEADER_USER_COUNTER
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.time.ExperimentalTime

@ExperimentalTime
class UserCountingTest {

    private val mockWebServer = MockWebServer()
    private lateinit var analyticsProvider : Fakes.FakeAnalyticsProvider
    private lateinit var fakeCoreRepository : Fakes.FakeCoreRepository
    private lateinit var userCounter : OkHttpUserCounter
    private val serverTimeZone: TimeZone = TimeZone.getTimeZone("GMT")
    private val serverDateParser = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
        Locale.ENGLISH)

    @Before
    fun setUp() {
        mockWebServer.start()
        val settings = Fakes.FakeSettingsRepository(mockWebServer.url("").toString())
        val appInfo = AppInfo()
        analyticsProvider = Fakes.FakeAnalyticsProvider()
        fakeCoreRepository = Fakes.FakeCoreRepository(mockWebServer.url("").toString())
        userCounter = OkHttpUserCounter(OkHttpClient(), fakeCoreRepository, settings, appInfo,
            analyticsProvider)
        serverDateParser.timeZone = serverTimeZone
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test counting success`() {
        val response = MockResponse()
            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01 GMT") //202109231731
        mockWebServer.enqueue(response)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Success)
        }
        assertEquals(true.toString(), analyticsProvider.userPropertyValue)
        assertEquals(1, mockWebServer.requestCount)
        assertEquals(202109231731, fakeCoreRepository.lastUserCountingResponse)
        assertEquals(1, fakeCoreRepository.userCountingCount)
        assertNull(analyticsProvider.event)
    }

    @Test
    fun `test counting when Acceptable Ads are disabled`() {
        val response = MockResponse()
            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01 GMT") //202109231731
        mockWebServer.enqueue(response)
        val settings = Fakes.FakeSettingsRepository(mockWebServer.url("").toString())
        val appInfo = AppInfo()
        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            // Given Acceptable Ads are disabled
            settings.acceptableAdsStatus = false
            // When the user is counted
            userCounter = OkHttpUserCounter(OkHttpClient(), fakeCoreRepository, settings, appInfo,
                analyticsProvider)
            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Success)
        }
        // Then User property value is reported to Google Analytics
        assertEquals(false.toString(), analyticsProvider.userPropertyValue)
    }

    @Test
    fun `test counting Http 500`() {
        val response = MockResponse()
            .setResponseCode(HTTP_INTERNAL_ERROR)
        mockWebServer.enqueue(response)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Failed)
        }
        assertEquals(1, mockWebServer.requestCount)
        assertEquals(Fakes.INITIAL_TIMESTAMP,
            fakeCoreRepository.lastUserCountingResponse)
        assertEquals(Fakes.INITIAL_COUNT, fakeCoreRepository.userCountingCount)
        assertNull(analyticsProvider.event)
        assertEquals(analyticsProvider.error, HTTP_ERROR_LOG_HEADER_USER_COUNTER + HTTP_ERROR_MOCK_500)
    }

    @Test
    fun `test counting date wrong format`() {
        val response = MockResponse()
            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01") //No timezone (GMT)
        mockWebServer.enqueue(response)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            try {
                assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Success)
                if (BuildConfig.DEBUG) {
                    fail() // In Debug mode we throw from count()
                }
            } catch (ex: Exception) {
                assert(ex is ParseException)
            }

        }
        assertEquals(1, mockWebServer.requestCount)
        if (BuildConfig.DEBUG) {
            assertEquals(Fakes.INITIAL_TIMESTAMP,
                fakeCoreRepository.lastUserCountingResponse)
            assertEquals(Fakes.INITIAL_COUNT, fakeCoreRepository.userCountingCount)
        } else {
            assert(fakeCoreRepository.userCountingCount == 1)
        }
        assertTrue(analyticsProvider.exception is ParseException)
    }

    @Test
    fun `test counting no header`() {
        val response = MockResponse()
        mockWebServer.enqueue(response)
        if (BuildConfig.DEBUG) {
            assertThrows(ParseException::class.java) {
                runBlocking { userCounter.count(CallingApp("", "")) }
            }
        } else {
            runBlocking { assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Success) }
        }
    }
}
