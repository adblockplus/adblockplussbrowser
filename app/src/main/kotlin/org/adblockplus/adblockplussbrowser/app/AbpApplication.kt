package org.adblockplus.adblockplussbrowser.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import timber.log.Timber
import java.lang.RuntimeException
import javax.inject.Inject

@HiltAndroidApp
class AbpApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var subscriptionsManager: SubscriptionsManager

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        subscriptionsManager.initialize()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        try {
            Timber.i("test")
            throw RuntimeException("example e throw")
        } catch (e: Exception) {
            Timber.e(e, "example e message")
            Timber.w(e, "example w message")
            Timber.w("example w message without throwable")
        }

    }
}
