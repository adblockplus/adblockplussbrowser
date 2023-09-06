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

package org.adblockplus.adblockplussbrowser.core.downloader

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.adblockplus.adblockplusbrowser.testutils.FakeAnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.data.model.CustomSubscriptionType
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.os.AppInfo
import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState
import org.adblockplus.adblockplussbrowser.core.downloader.OkHttpDownloader.Companion.HTTP_ERROR_LOG_HEADER_DOWNLOADER
import org.adblockplus.adblockplussbrowser.core.helpers.FakeCoreRepository
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes.HTTP_ERROR_MOCK_500
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_OK
import kotlin.time.ExperimentalTime

@RunWith(MockitoJUnitRunner::class)
@ExperimentalTime
@ExperimentalCoroutinesApi
class DownloaderTest {

    private val mockContext = Mockito.mock(Context::class.java)
    private val cacheDir = "/tmp/cacheDir/"
    private val filesDir = "/tmp/filesDir/"
    private val mockWebServer = MockWebServer()
    private lateinit var fakeCoreRepository: FakeCoreRepository
    private lateinit var downloader: Downloader
    private lateinit var analyticsProvider: FakeAnalyticsProvider
    private val testDispatcher = StandardTestDispatcher()

    private val version = "202109231731"
    private val etag = "1234567890"
    private val lastModified = "Thu, 23 Sep 2021 17:31:01 GMT"
    private val lastModifiedInt = 202109231731L
    private val downloadFileContent = "[Adblock Plus 2.0]\n" +
            "! Checksum: PRVPDDw+HOO0AQNjsGwCLg\n" +
            "! Title: Allow nonintrusive advertising\n" +
            "! Expires: 1 days\n" +
            "! Homepage: https://acceptableads.com/"
    private val badResponse = MockResponse().setResponseCode(HTTP_INTERNAL_ERROR)
    private val goodResponse = MockResponse()
        .setResponseCode(HTTP_OK)
        .setHeader("ETag", etag)
        .setHeader("Last-Modified", lastModified)
        .setHeader("Date", "Thu, 23 Sep 2021 17:31:01 GMT") //202109231731
        .setBody(downloadFileContent)
    private val fakeSubscription by lazy {
        Subscription(
            fakeCoreRepository.aaUrl,
            "",
            0L,
            CustomSubscriptionType.FROM_URL
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockWebServer.start()
        val appInfo = AppInfo()
        fakeCoreRepository = FakeCoreRepository(mockWebServer.url("").toString())
        analyticsProvider = FakeAnalyticsProvider()
        downloader = OkHttpDownloader(
            mockContext, OkHttpClient(), fakeCoreRepository, appInfo,
            analyticsProvider
        )
        `when`(mockContext.applicationContext).thenReturn(mockContext)
        File(cacheDir).mkdirs()
        File(filesDir).mkdirs()
        `when`(mockContext.filesDir).thenReturn(File(filesDir))
        `when`(mockContext.cacheDir).thenReturn(File(cacheDir))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockWebServer.shutdown()
        File(filesDir).deleteRecursively()
        File(cacheDir).deleteRecursively()
    }


    @Test
    fun `test successful download with code 200 and then again with code 304`() {
        mockWebServer.enqueue(goodResponse)
        mockWebServer.enqueue(MockResponse().setResponseCode(HTTP_NOT_MODIFIED))

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            var downloadResult =
                downloader.download(
                    fakeSubscription,
                    forced = false,
                    periodic = true,
                    newSubscription = true
                )
            assertTrue(downloadResult is DownloadResult.Success)
            assertEquals(version, downloadResult.subscription?.version)
            assertEquals(etag, downloadResult.subscription?.etag)
            assertEquals(lastModified, downloadResult.subscription?.lastModified)
            assertEquals(1, downloadResult.subscription?.downloadCount)
            // We cannot test more that download status because saving subscription from
            // a previous download is done by the caller - UpdateSubscriptionsWorker.
            downloadResult =
                downloader.download(
                    fakeSubscription,
                    forced = false,
                    periodic = true,
                    newSubscription = true
                )
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
    fun `test download failure with code 500`() {
        mockWebServer.enqueue(badResponse)

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            val downloadResult =
                downloader.download(
                    fakeSubscription,
                    forced = false,
                    periodic = true,
                    newSubscription = true
                )
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
        assertEquals(
            analyticsProvider.error,
            "$HTTP_ERROR_LOG_HEADER_DOWNLOADER $HTTP_ERROR_MOCK_500"
        )
    }

    @Test
    fun `test download failure due to absence of Date header`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(HTTP_OK).setBody(downloadFileContent))
        assertEquals(0, mockWebServer.requestCount)
        assertThat(
            downloader.download(
                fakeSubscription,
                forced = false,
                periodic = true,
                newSubscription = true
            ), instanceOf(DownloadResult.Failed::class.java)
        )
    }


    @Test
    fun `test download success with only the Date header`() {
        mockWebServer.enqueue(
            MockResponse()
                .setHeader("Date", "Thu, 23 Sep 2021 17:31:01 GMT") //202109231731
                .setResponseCode(HTTP_OK)
                .setBody(downloadFileContent)
        )

        assertEquals(0, mockWebServer.requestCount)
        runBlocking {
            val downloadResult =
                downloader.download(
                    fakeSubscription,
                    forced = false,
                    periodic = true,
                    newSubscription = true
                )
            assertTrue(downloadResult is DownloadResult.Success)
            assertEquals(lastModifiedInt.toString(), downloadResult.subscription?.version)
            assertEquals("", downloadResult.subscription?.etag)
            assertEquals("", downloadResult.subscription?.lastModified)
            assertEquals(1, downloadResult.subscription?.downloadCount)
        }
    }

    @Test
    fun `test validation success on HTTP fetch`() {
        mockWebServer.enqueue(goodResponse)
        runBlocking { assertTrue(downloader.validate(fakeSubscription)) }
    }

    @Test
    fun `test validation failure on HTTP fetch error`() {
        mockWebServer.enqueue(badResponse)
        runBlocking { assertTrue(!downloader.validate(fakeSubscription)) }
    }

    @Test
    fun `test GetDownloadedSubscription when the subscription is absent in coreData`() {
        val downloadedSubscription =
            runBlocking {
                (downloader as OkHttpDownloader).getDownloadedSubscription(
                    fakeSubscription
                )
            }
        assertEquals(fakeCoreRepository.aaUrl, downloadedSubscription.url)
        assertTrue(downloadedSubscription.path.contains(Regex("tmp.filesDir.downloads")))
        assertEquals(0, downloadedSubscription.lastUpdated)
        assertEquals("", downloadedSubscription.lastModified)
        assertEquals("0", downloadedSubscription.version)
        assertEquals("", downloadedSubscription.etag)
        assertEquals(0, downloadedSubscription.downloadCount)
    }

    @Test
    fun `test GetDownloadedSubscription when the subscription is present in coreData`() {
        val testSubscriptionUrl = "http://test.url"
        val testDownloadedSubscription =
            DownloadedSubscription(
                testSubscriptionUrl,
                "",
                lastModifiedInt,
                "lastModified",
                "version"
            )
        fakeCoreRepository.coreData = CoreData(
            true, 0,
            SavedState(true, listOf(""), listOf(""), listOf(""), listOf("")),
            listOf(testDownloadedSubscription), 0L, 0
        )
        val downloadedSubscription = runBlocking {
            (downloader as OkHttpDownloader).getDownloadedSubscription(
                Subscription(testSubscriptionUrl, "", 0, CustomSubscriptionType.FROM_URL)
            )
        }
        assertEquals(testDownloadedSubscription, downloadedSubscription)
    }
}
