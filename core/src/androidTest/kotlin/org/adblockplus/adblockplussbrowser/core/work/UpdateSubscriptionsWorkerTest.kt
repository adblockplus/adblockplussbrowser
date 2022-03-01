package org.adblockplus.adblockplussbrowser.core.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class UpdateSubscriptionsWorkerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testCatchExceptionFailureResult() {
//        assertThat(true, `is` (true))
//        val updateSubscriptionsWorker = TestListenableWorkerBuilder<UpdateSubscriptionsWorker>(context)
//            .setTags(mutableListOf(UpdateSubscriptionsWorker.UPDATE_KEY_FORCE_REFRESH))
//            .build()
        val updateSubscriptionsWorker = UpdateSubscriptionsWorker(context,Mockito.any(),Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())
        runBlocking {
            `when`(updateSubscriptionsWorker.doWork()).thenThrow(Exception::class.java)
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.failure()))
        }
    }

}