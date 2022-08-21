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
import org.adblockplus.adblockplussbrowser.preferences.helpers.DOMParser
import org.adblockplus.adblockplussbrowser.preferences.helpers.getAttribute
import org.adblockplus.adblockplussbrowser.preferences.helpers.getTagContent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.kotlin.doThrow
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class HttpReportIssueRepositoryTest {

    private val httpReportIssueRepository = HttpReportIssueRepository()
    private val fakeReportIssueData = Fakes.fakeReportIssueData
    private var mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start()
        httpReportIssueRepository.setDefaultURL("http://${mockWebServer.hostName}:${mockWebServer.port}")
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
                Result.success(Unit),
                httpReportIssueRepository.sendReport(fakeReportIssueData)
            )
        }
    }

    @Test
    fun `test sendReport no URL returned from server`() {
        val response = MockResponse()
            .setResponseCode(HTTP_OK)
            .setBody("some invalid body")
        mockWebServer.enqueue(response)
        runTest {
            val exception = httpReportIssueRepository.sendReport(fakeReportIssueData).exceptionOrNull()
            assertNotNull(exception)
            assertEquals("Invalid response: some invalid body.", exception?.message)
        }
    }

    @Test
    fun `test sendReport error code != 200`() {
        runTest {
            val response = MockResponse()
                .setResponseCode(HTTP_INTERNAL_ERROR)
            mockWebServer.enqueue(response)
            val exception = httpReportIssueRepository.sendReport(fakeReportIssueData).exceptionOrNull()
            assertNotNull(exception)
            assertEquals("Server replied with 500", exception?.message)
        }
    }

    @Test
    fun `test sendReport XML error`() {
        val xmlSerializerMock = mock(Xml.newSerializer()::class.java)
        doThrow(RuntimeException("Exception")).`when`(xmlSerializerMock).setOutput(any())
        runTest {
            httpReportIssueRepository.serializer = xmlSerializerMock
            val exception = httpReportIssueRepository.sendReport(fakeReportIssueData).exceptionOrNull()
            assertNotNull(exception)
            assertEquals("Exception", exception?.message)
        }
    }

    @Test
    fun `test makeXML success`() {
        val resultXml = httpReportIssueRepository.makeXML(fakeReportIssueData).getOrThrow()
        val document = DOMParser.parse(resultXml)
        assertEquals(fakeReportIssueData.type, document.getAttribute("type"))
        assertEquals(fakeReportIssueData.url, document.getAttribute("url", "window"))
        assertEquals(fakeReportIssueData.comment, document.getTagContent("comment"))
        assertEquals(fakeReportIssueData.email, document.getTagContent("email"))
    }

    @Test
    fun `test makeXML without url`() {
        fakeReportIssueData.url = ""
        val resultXml = httpReportIssueRepository.makeXML(fakeReportIssueData)
        assertFalse(resultXml.getOrThrow().contains("www.example.com"))
    }

    @Test
    fun `test makeXML empty XML`() {
        val xmlSerializerMock = mock(Xml.newSerializer()::class.java)
        `when`(xmlSerializerMock.startTag(anyString(), anyString())).thenThrow(RuntimeException("Exception"))
        httpReportIssueRepository.serializer = xmlSerializerMock
        assertTrue(httpReportIssueRepository.makeXML(fakeReportIssueData).getOrThrow().isEmpty())
    }
}
