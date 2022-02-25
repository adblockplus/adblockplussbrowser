package org.adblockplus.adblockplusbrowser.core.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
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
            `when`(updateSubscriptionsWorker.doWork()).thenThrow(Exception::class.java)
            val result = updateSubscriptionsWorker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.failure()))
        }
    }

}
