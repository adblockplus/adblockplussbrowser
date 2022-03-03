package org.adblockplus.adblockplussbrowser.core.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdateSubscriptionsWorkerTest {

    lateinit var context: Context;

    @Before
    fun before() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun doWork() {
        val worker = TestListenableWorkerBuilder<UpdateSubscriptionsWorker>(context).build()
        worker.subscriptionsManager = Mockito.mock(SubscriptionsManager::class.java)
        worker.coreRepository = Mockito.mock(CoreRepository::class.java)
        worker.downloader = Mockito.mock(Downloader::class.java)
        worker.settingsRepository = Mockito.mock(SettingsRepository::class.java)

        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.retry()))
        }
    }
}