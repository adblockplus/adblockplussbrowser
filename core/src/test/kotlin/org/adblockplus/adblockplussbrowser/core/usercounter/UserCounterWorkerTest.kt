package org.adblockplus.adblockplussbrowser.core.usercounter

import org.junit.Before
import org.junit.runner.RunWith
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.helpers.WorkerParameters
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import java.lang.IndexOutOfBoundsException

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class UserCounterWorkerTest {

    private lateinit var context: Context
    private lateinit var userCounter: UserCounter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        userCounter = Mockito.mock(UserCounter::class.java)
    }

    private fun createWorker (params: WorkerParameters): ListenableWorker {
        val worker = TestListenableWorkerBuilder<UserCounterWorker>(context)
            .setRunAttemptCount(params.runAttemptCount)
            .build()
        worker.analyticsProvider = Fakes.FakeAnalyticsProvider()
        worker.userCounter = userCounter
        return worker
    }

    private suspend fun whenCount() = Mockito.`when`(userCounter.count(any()))

    @Test
    fun `test user counter success`() {
        runTest {
            val worker = createWorker(WorkerParameters()) as UserCounterWorker
            whenCount().thenReturn(CountUserResult.Success())
            val result = worker.doWork()
            MatcherAssert.assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Success()))
        }
    }

    @Test
    fun `test user counter failed, worker should retry`() {
        runTest {
            val worker = createWorker(WorkerParameters()) as UserCounterWorker
            whenCount().thenReturn(CountUserResult.Failed())
            val result = worker.doWork()
            MatcherAssert.assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Retry()))
        }
    }

    /*
    Mock that the worker is running for the 9th time,
    meaning it's over the  RUN_ATTEMPT_MAX_COUNT, which is 8
    Expected result -> the user count should fail
 */
    @Test
    fun `test if worker has reached max attempts should fail`() {
        val worker = createWorker(WorkerParameters(runAttemptCount = 9)) as UserCounterWorker
        runTest {
            val result = worker.doWork()
            MatcherAssert.assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.failure()))
        }
    }

    @Test
    fun `test worker CancellationException should succeed`() {
        val worker = createWorker(WorkerParameters()) as UserCounterWorker
        runTest {
            whenCount().thenThrow(CancellationException())
            val result = worker.doWork()
            MatcherAssert.assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Success()))
        }
    }

    @Test
    fun `test worker catch exception should retry`() {
        val worker = createWorker(WorkerParameters()) as UserCounterWorker
        runTest {
            whenCount().thenThrow(IndexOutOfBoundsException())
            val result = worker.doWork()
            MatcherAssert.assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Retry()))
        }
    }
}