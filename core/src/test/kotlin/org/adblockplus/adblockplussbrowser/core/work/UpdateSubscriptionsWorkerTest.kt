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

package org.adblockplus.adblockplussbrowser.core.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.downloader.DownloadResult
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.helpers.WorkerParameters
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class UpdateSubscriptionsWorkerTest {

    private lateinit var context: Context
    private lateinit var downloader: Downloader
    private val testDispatcher = StandardTestDispatcher()


    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        downloader = Mockito.mock(Downloader::class.java)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
    }

    private suspend fun whenDownload() = Mockito.`when`(downloader.download(any(), any(), any(), any()))

    private fun createWorker(params: WorkerParameters): ListenableWorker {
        val worker = TestListenableWorkerBuilder<UpdateSubscriptionsWorker>(context)
            .setRunAttemptCount(params.runAttemptCount)
            .setTags(params.tags)
            .build()
        worker.subscriptionsManager = Mockito.mock(SubscriptionsManager::class.java)
        worker.coreRepository = Fakes.FakeCoreRepository("")
        worker.downloader = downloader
        worker.settingsRepository = Fakes.FakeSettingsRepository("")
        return worker
    }

    /*
        Mock that the worker is running for the 5th time,
        meaning it's over the  RUN_ATTEMPT_MAX_COUNT, which is 4
        Expected result -> the update should fail
     */
    @Test
    fun `test if worker has reached max attempts should fail`() {
        val updateSubscriptionsWorker = createWorker(WorkerParameters(runAttemptCount = 5)) as UpdateSubscriptionsWorker
        runTest {
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.failure()))
        }
    }

    @Test
    fun `test update should succeed`() {
        val params = WorkerParameters(runAttemptCount = 0)
        val updateSubscriptionsWorker = createWorker(params) as UpdateSubscriptionsWorker
        updateSubscriptionsWorker.settingsRepository = Fakes.FakeSettingsRepositoryNoChanges("")
        runTest {
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun `test downloads failed result should retry`(){
        val updateSubscriptionsWorker = createWorker(WorkerParameters()) as UpdateSubscriptionsWorker
        val directory = File(context.filesDir,"cache")
        directory.mkdirs()
        val file = File.createTempFile("filter", ".txt", directory)
        runTest {
            whenDownload().thenReturn(DownloadResult.Failed(DownloadedSubscription("", file.path)))
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.Retry()))
        }
    }

    @Test
    fun `test downloads Succeed should succeed`(){
        val updateSubscriptionsWorker = createWorker(WorkerParameters()) as UpdateSubscriptionsWorker
        val directory = File(context.filesDir,"cache")
        directory.mkdirs()
        val file = File.createTempFile("filter", ".txt", directory)
        runTest {
            whenDownload().thenReturn(DownloadResult.Success(DownloadedSubscription("", file.path)))
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.Success()))
        }
    }

    @Test
    fun `test worker CancellationException should succeed`() {
        val updateSubscriptionsWorker = createWorker(WorkerParameters()) as UpdateSubscriptionsWorker
        runTest {
            whenDownload().thenReturn(DownloadResult.Success(DownloadedSubscription("")))
            val result = updateSubscriptionsWorker.run {
                updateSubscriptionsWorker.stop()
                updateSubscriptionsWorker.doWork()
            }
            assertThat(result, `is`(ListenableWorker.Result.Success()))
        }
    }

    @Test
    fun `test CatchException should fail`() {
        val updateSubscriptionsWorker = createWorker(WorkerParameters(
            tags = mutableListOf(UpdateSubscriptionsWorker.UPDATE_KEY_FORCE_REFRESH)
        )) as UpdateSubscriptionsWorker
        runTest {
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.failure()))
        }
    }

    @Test
    fun `test catch Exception should retry`() {
        val updateSubscriptionsWorker = createWorker(WorkerParameters()) as UpdateSubscriptionsWorker
        runTest {
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.Retry()))
        }
    }
}

