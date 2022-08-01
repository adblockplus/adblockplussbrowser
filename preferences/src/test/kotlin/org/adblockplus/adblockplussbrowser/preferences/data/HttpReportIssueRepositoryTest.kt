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

package org.adblockplus.adblockplussbrowser.preferences.data

import android.util.Xml
import java.io.IOException
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_OK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.adblockplus.adblockplussbrowser.preferences.helpers.Fakes
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class HttpReportIssueRepositoryTest {

    private val httpReportIssueRepository = HttpReportIssueRepository()
    private val fakeReportIssueData = Fakes().fakeReportIssueData
    private var mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start()
        HttpReportIssueRepository.DEFAULT_URL = "http://${mockWebServer.hostName}:${mockWebServer.port}"
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test sendReport success`() {
        val response = MockResponse()
            .setResponseCode(HTTP_OK)
            .setBody("<a>http://www.exampleReport.com</a>")
        mockWebServer.enqueue(response)
        runTest {
            assertEquals(
                "",
                httpReportIssueRepository.sendReport(fakeReportIssueData)
            )
        }
    }

    @Test
    fun `test sendReport no URL returned from server`() {
        val response = MockResponse()
            .setResponseCode(HTTP_OK)
            .setBody("")
        mockWebServer.enqueue(response)
        runTest {
            assertEquals(
                "Send error",
                httpReportIssueRepository.sendReport(fakeReportIssueData)
            )
        }
    }

    @Test
    fun `test sendReport error code != 200`() {
        runTest {
            val response = MockResponse()
                .setResponseCode(HTTP_INTERNAL_ERROR)
                .setBody("<a>http://www.exampleReport.com</a>")
            mockWebServer.enqueue(response)
            assertEquals(
                "HTTP returned $HTTP_INTERNAL_ERROR",
                httpReportIssueRepository.sendReport(fakeReportIssueData)
            )
        }
    }

    @Test
    fun `test sendReport XML error`() {
        val xmlSerializerMock = mock(Xml.newSerializer()::class.java)
        `when`(xmlSerializerMock.startTag(anyString(), anyString())).thenThrow(IOException("Exception"))
        runTest {
            httpReportIssueRepository.serializer = xmlSerializerMock
            assertEquals(
                "Error creating XML",
                httpReportIssueRepository.sendReport(fakeReportIssueData))
        }
    }
}
