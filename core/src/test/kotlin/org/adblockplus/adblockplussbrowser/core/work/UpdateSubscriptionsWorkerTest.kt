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

    @Test
    fun testHasReachedMaxAttemptsShouldFail() {
        val worker = createWorker(WorkerParameters(runAttemptCount = 5))
        runTest {
            val result = (worker as UpdateSubscriptionsWorker).doWork()
            assertThat(result, `is`(ListenableWorker.Result.failure()))
        }
    }

    @Test
    fun testUpdateShouldSucceed() {
        val params = WorkerParameters(runAttemptCount = 0)
        val updateSubscriptionsWorker = createWorker(params) as UpdateSubscriptionsWorker
        updateSubscriptionsWorker.settingsRepository = Fakes.FakeSettingsRepositoryNoChanges("")
        runTest {
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun testDownloadsFailedResultShouldRetry(){
        val updateSubscriptionsWorker = createWorker(WorkerParameters()) as UpdateSubscriptionsWorker
        val directory = File(context.filesDir,"cache")
        directory.mkdirs()
        val file = File.createTempFile("filter", ".txt", directory)
        runTest {
            Mockito.`when`(downloader.download(any(), any(), any(), any())).thenReturn(DownloadResult.Failed(
                DownloadedSubscription("", file.path)
            ))

            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.Retry()))
        }
    }

    @Test
    fun testDownloadsSucceedShouldSucceed(){
        val updateSubscriptionsWorker = createWorker(WorkerParameters()) as UpdateSubscriptionsWorker
        val directory = File(context.filesDir,"cache")
        directory.mkdirs()
        val file = File.createTempFile("filter", ".txt", directory)
        runTest {
            Mockito.`when`(downloader.download(any(), any(), any(), any())).thenReturn(
                DownloadResult.Success(
                    DownloadedSubscription("", file.path)
                )
            )
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.Success()))
        }
    }

    @Test
    fun testWorkerCancellationExceptionShouldSucceed() {
        val updateSubscriptionsWorker = createWorker(WorkerParameters()) as UpdateSubscriptionsWorker
        runTest {
            Mockito.`when`(downloader.download(any(), any(), any(), any())).thenReturn(DownloadResult.Success(
                DownloadedSubscription("")
            ))
            val result = updateSubscriptionsWorker.run {
                updateSubscriptionsWorker.stop()
                updateSubscriptionsWorker.doWork()
            }
            assertThat(result, `is`(ListenableWorker.Result.Success()))
        }
    }

    @Test
    fun testCatchExceptionShouldFail() {
        val updateSubscriptionsWorker = createWorker(WorkerParameters(
            tags = mutableListOf(UpdateSubscriptionsWorker.UPDATE_KEY_FORCE_REFRESH)
        )) as UpdateSubscriptionsWorker
        runTest {
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.failure()))
        }
    }

    @Test
    fun testCatchExceptionShouldRetry() {
        val updateSubscriptionsWorker = createWorker(WorkerParameters()) as UpdateSubscriptionsWorker
        runTest {
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.Retry()))
        }
    }

    internal class WorkerParameters(
        val tags: MutableList<String> = mutableListOf(),
        var runAttemptCount: Int = 1
    )
}