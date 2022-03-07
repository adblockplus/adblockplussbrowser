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

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.usercounter.CountUserResult
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import kotlin.time.ExperimentalTime

@ExperimentalTime
class UserCountingTest {

    private val mockWebServer = MockWebServer()
    private lateinit var analyticsProvider : Fakes.FakeAnalyticsProvider
    private lateinit var fakeCoreRepository : Fakes.FakeCoreRepository
    private lateinit var userCounter : OkHttpUserCounter

    @Before
    fun setUp() {
        mockWebServer.start()
        val settings = Fakes.FakeSettingsRepository(mockWebServer.url("").toString())
        val appInfo = AppInfo()
        analyticsProvider = Fakes.FakeAnalyticsProvider()
        fakeCoreRepository = Fakes.FakeCoreRepository(mockWebServer.url("").toString())
        userCounter = OkHttpUserCounter(OkHttpClient(), fakeCoreRepository, settings, appInfo,
            analyticsProvider)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test counting skipped`() {
        val response = MockResponse()
            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01 GMT") //202109231731
        mockWebServer.enqueue(response)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Skipped)
        }
        assertEquals(null, analyticsProvider.userPropertyValue)
        assertEquals(0, mockWebServer.requestCount)
        assertEquals(-1, fakeCoreRepository.lastUserCountingResponse)
        assertEquals(-1, fakeCoreRepository.userCountingCount)
        assertNull(analyticsProvider.event)
    }

    @Test
    fun `test counting skipped when Acceptable Ads are disabled`() {
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
            val countingResult = userCounter.count(CallingApp("", ""))
            assertTrue(countingResult is CountUserResult.Skipped)
        }
        // Then User property value is reported to Google Analytics
        assertEquals(null, analyticsProvider.userPropertyValue)
    }

    @Test
    fun `test counting Http 500`() {
        val response = MockResponse()
            .setResponseCode(HTTP_INTERNAL_ERROR)
        mockWebServer.enqueue(response)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Skipped)
        }
        assertEquals(0, mockWebServer.requestCount)
        assertEquals(fakeCoreRepository.INITIAL_TIMESTAMP,
            fakeCoreRepository.lastUserCountingResponse)
        assertEquals(fakeCoreRepository.INITIAL_COUNT, fakeCoreRepository.userCountingCount)
        assertNull(analyticsProvider.event)
        assertEquals(analyticsProvider.userPropertyName, null)
        assertEquals(analyticsProvider.userPropertyValue, null)
    }

    @Test
    fun `test counting skipped date wrong format`() {
        val response = MockResponse()
            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01") //No timezone (GMT)
        mockWebServer.enqueue(response)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            assertTrue(userCounter.count(CallingApp("", "")) is CountUserResult.Skipped)
        }
        assertEquals(0, mockWebServer.requestCount)
        if (BuildConfig.DEBUG) {
            assertEquals(fakeCoreRepository.INITIAL_TIMESTAMP,
                fakeCoreRepository.lastUserCountingResponse)
            assertEquals(fakeCoreRepository.INITIAL_COUNT, fakeCoreRepository.userCountingCount)
        } else {
            assert(fakeCoreRepository.userCountingCount == 1)
        }
    }
}
