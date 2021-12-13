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
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_OK
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.OkHttpClient
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.downloader.DownloadResult
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.downloader.OkHttpDownloader
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.io.File

@RunWith(MockitoJUnitRunner::class)
@ExperimentalTime
class DownloaderTest {

    private val mockContext = Mockito.mock(Context::class.java)
    private val cacheDir = "/tmp/cacheDir/"
    private val filesDir = "/tmp/filesDir/"
    private val mockWebServer = MockWebServer()
    private lateinit var fakeCoreRepository : Fakes.FakeCoreRepository
    private lateinit var downloader : Downloader
    private lateinit var analyticsProvider : Fakes.FakeAnalyticsProvider

    @Before
    fun setUp() {
        mockWebServer.start()
        val appInfo = AppInfo()
        fakeCoreRepository = Fakes.FakeCoreRepository(mockWebServer.url("").toString())
        analyticsProvider = Fakes.FakeAnalyticsProvider()
        downloader = OkHttpDownloader(mockContext, OkHttpClient(), fakeCoreRepository, appInfo,
            analyticsProvider)
        `when`(mockContext.applicationContext).thenReturn(mockContext)
        File(cacheDir).mkdirs()
        File(filesDir).mkdirs()
        `when`(mockContext.filesDir).thenReturn(File(filesDir))
        `when`(mockContext.cacheDir).thenReturn(File(cacheDir))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        File(filesDir).deleteRecursively()
        File(cacheDir).deleteRecursively()
    }


    @Test
    fun testDownloadOK200_ThenOK304() {
        val version = "202109091143"
        val etag = "1234567890"
        val lastModified = "Thu, 23 Sep 2021 17:31:01 GMT"
        val downloadFileContent = "[Adblock Plus 2.0]\n" +
            "! Checksum: PRVPDDw+HOO0AQNjsGwCLg\n" +
            "! Version: $version\n" +
            "! Title: Allow nonintrusive advertising\n" +
            "! Expires: 1 days\n" +
            "! Homepage: https://acceptableads.com/"
        val response = MockResponse()
            .setResponseCode(HTTP_OK)
            .setHeader("ETag", etag)
            .setHeader("Last-Modified", lastModified)
            .setBody(downloadFileContent)
        mockWebServer.enqueue(response)
        mockWebServer.enqueue(MockResponse().setResponseCode(HTTP_NOT_MODIFIED))

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            var downloadResult = downloader.download(
                Subscription(fakeCoreRepository.AA_URL, "", 0L),false, true, true)
            assertTrue(downloadResult is DownloadResult.Success)
            assertEquals(version, downloadResult.subscription?.version)
            assertEquals(etag, downloadResult.subscription?.etag)
            assertEquals(lastModified, downloadResult.subscription?.lastModified)
            assertEquals(1, downloadResult.subscription?.downloadCount)
            // We cannot test more that download status because saving subscription from
            // a previous download is done by the caller - UpdateSubscriptionsWorker.
            downloadResult = downloader.download(
                Subscription(fakeCoreRepository.AA_URL, "", 0L),false, true, true)
            assertTrue(downloadResult is DownloadResult.NotModified)
        }
        assertEquals(2, mockWebServer.requestCount)

        var filesCount = 0
        File(filesDir).walk().forEach {
            if (it.isFile) {
                ++filesCount
                assertEquals(downloadFileContent, it.readText())
            }
        }
        assertEquals(1, filesCount)
    }

    @Test
    fun testDownloadNOK500() {
        val response = MockResponse()
            .setResponseCode(HTTP_INTERNAL_ERROR)
        mockWebServer.enqueue(response)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            val downloadResult = downloader.download(
                Subscription(fakeCoreRepository.AA_URL, "", 0L),false, true, true)
            assertTrue(downloadResult is DownloadResult.Failed)
            assertNull(downloadResult.subscription)
        }
        assertEquals(1, mockWebServer.requestCount)

        var filesCount = 0
        File(filesDir).walk().forEach {
            if (it.isFile) {
                ++filesCount
            }
        }
        assertEquals(0, filesCount)
        assertEquals(analyticsProvider.userPropertyName,
            AnalyticsUserProperty.DOWNLOAD_HTTP_ERROR)
        assertEquals(analyticsProvider.userPropertyValue, HTTP_INTERNAL_ERROR.toString())
    }
}
