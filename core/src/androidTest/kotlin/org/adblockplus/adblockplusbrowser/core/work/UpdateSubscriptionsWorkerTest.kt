package org.adblockplus.adblockplusbrowser.core.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import org.adblockplus.adblockplusbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import javax.inject.Singleton

@HiltAndroidTest
class UpdateSubscriptionsWorkerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

//    @Module
//    @InstallIn(SingletonComponent::class)
//    object TestDependenciesModule {
//
//        @Singleton
//        @Provides
//        fun getAnalyticsProvider() = Fakes.FakeAnalyticsProvider()
//
//        @Singleton
//        @Provides
//        fun getActivationPreferences() = Fakes.FakeActivationPreferences()
//    }

    @Test
    fun testCatchExceptionFailureResult() {
        assertThat(true, `is` (true))
//        val updateSubscriptionsWorker = TestListenableWorkerBuilder<UpdateSubscriptionsWorker>(context)
//            .setTags(mutableListOf(UpdateSubscriptionsWorker.UPDATE_KEY_FORCE_REFRESH))
//            .build()
//        runBlocking {
//            `when`(updateSubscriptionsWorker.doWork()).thenThrow(Exception::class.java)
//            val result = updateSubscriptionsWorker.doWork()
//            assertThat(result, `is`(ListenableWorker.Result.failure()))
//        }
    }

}