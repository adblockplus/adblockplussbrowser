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

package org.adblockplus.adblockplussbrowser.core.old_usercounter

import org.junit.Before
import org.junit.runner.RunWith
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.adblockplus.adblockplussbrowser.core.helpers.WorkerParameters
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import java.lang.IndexOutOfBoundsException
import org.adblockplus.adblockplusbrowser.testutils.FakeAnalyticsProvider

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class OldUserCounterWorkerTest {

    private lateinit var context: Context
    private lateinit var oldUserCounter: OldUserCounter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        oldUserCounter = Mockito.mock(OldUserCounter::class.java)
    }

    private fun createWorker (params: WorkerParameters): ListenableWorker {
        val worker = TestListenableWorkerBuilder<OldUserCounterWorker>(context)
            .setRunAttemptCount(params.runAttemptCount)
            .build()
        worker.analyticsProvider = FakeAnalyticsProvider()
        worker.userCounter = oldUserCounter
        return worker
    }

    private suspend fun whenCount() = Mockito.`when`(oldUserCounter.count(any()))

    @Test
    fun `test user counter success`() {
        runTest {
            val worker = createWorker(WorkerParameters()) as OldUserCounterWorker
            whenCount().thenReturn(CountUserResult.Success())
            val result = worker.doWork()
            MatcherAssert.assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Success()))
        }
    }

    @Test
    fun `test user counter failed, worker should retry`() {
        runTest {
            val worker = createWorker(WorkerParameters()) as OldUserCounterWorker
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
        val worker = createWorker(WorkerParameters(runAttemptCount = 9)) as OldUserCounterWorker
        runTest {
            val result = worker.doWork()
            MatcherAssert.assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.failure()))
        }
    }

    @Test
    fun `test worker CancellationException should succeed`() {
        val worker = createWorker(WorkerParameters()) as OldUserCounterWorker
        runTest {
            whenCount().thenThrow(CancellationException())
            val result = worker.doWork()
            MatcherAssert.assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Success()))
        }
    }

    @Test
    fun `test worker catch exception should retry`() {
        val worker = createWorker(WorkerParameters()) as OldUserCounterWorker
        runTest {
            whenCount().thenThrow(IndexOutOfBoundsException())
            val result = worker.doWork()
            MatcherAssert.assertThat(result, CoreMatchers.`is`(ListenableWorker.Result.Retry()))
        }
    }
}

