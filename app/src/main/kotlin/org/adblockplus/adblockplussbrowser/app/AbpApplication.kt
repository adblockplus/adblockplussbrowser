package org.adblockplus.adblockplussbrowser.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import timber.log.Timber
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

        Timber.plant(Timber.DebugTree())
    }
}