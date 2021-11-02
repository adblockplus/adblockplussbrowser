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

import java.lang.Exception
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.text.ParseException
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.OkHttpClient
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.usercounter.CountUserResult
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

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
    fun testCountOK() {
        val response = MockResponse()
            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01 GMT") //202109231731
        mockWebServer.enqueue(response)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            assertTrue(userCounter.count() is CountUserResult.Success)
        }
        assertEquals(1, mockWebServer.requestCount)
        assertEquals(202109231731, fakeCoreRepository.lastUserCountingResponse)
        assertEquals(1, fakeCoreRepository.userCountingCount)
        assertNull(analyticsProvider.analyticsEvent)
    }

    @Test
    fun testCountHttp500() {
        val response = MockResponse()
            .setResponseCode(HTTP_INTERNAL_ERROR)
        mockWebServer.enqueue(response)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            assertTrue(userCounter.count() is CountUserResult.Failed)
        }
        assertEquals(1, mockWebServer.requestCount)
        assertEquals(fakeCoreRepository.INITIAL_TIMESTAMP,
            fakeCoreRepository.lastUserCountingResponse)
        assertEquals(fakeCoreRepository.INITIAL_COUNT, fakeCoreRepository.userCountingCount)
        assertNull(analyticsProvider.analyticsEvent)
    }

    @Test
    fun testCountDateWrongFormat() {
        val response = MockResponse()
            .addHeader("Date", "Thu, 23 Sep 2021 17:31:01") //No timezone (GMT)
        mockWebServer.enqueue(response)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            try {
                assertTrue(userCounter.count() is CountUserResult.Success)
                if (BuildConfig.DEBUG) {
                    fail() // In Debug mode we throw from count()
                }
            } catch (ex: Exception) {
                assert(ex is ParseException)
            }

        }
        assertEquals(1, mockWebServer.requestCount)
        if (BuildConfig.DEBUG) {
            assertEquals(fakeCoreRepository.INITIAL_TIMESTAMP,
                fakeCoreRepository.lastUserCountingResponse)
            assertEquals(fakeCoreRepository.INITIAL_COUNT, fakeCoreRepository.userCountingCount)
        } else {
            assert(fakeCoreRepository.userCountingCount == 1)
        }
        assertEquals(analyticsProvider.analyticsEvent,
            AnalyticsEvent.HEAD_RESPONSE_DATA_PARSING_FAILED
        )
    }
}