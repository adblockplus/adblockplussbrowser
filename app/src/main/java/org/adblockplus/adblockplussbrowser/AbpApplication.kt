package org.adblockplus.adblockplussbrowser

import android.app.Application
import timber.log.Timber

class AbpApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}