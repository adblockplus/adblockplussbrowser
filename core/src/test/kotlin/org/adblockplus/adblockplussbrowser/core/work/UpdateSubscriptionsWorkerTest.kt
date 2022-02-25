package org.adblockplus.adblockplussbrowser.core.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdateSubscriptionsWorkerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context =  ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testCatchExceptionFailureResult() {
        val updateSubscriptionsWorker = TestListenableWorkerBuilder<UpdateSubscriptionsWorker>(context)
            .setTags(mutableListOf(UpdateSubscriptionsWorker.UPDATE_KEY_FORCE_REFRESH))
            .build()
        runBlocking {
            Mockito.`when`(updateSubscriptionsWorker.doWork()).thenThrow(Exception::class.java)
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.failure()))
        }
    }

}
