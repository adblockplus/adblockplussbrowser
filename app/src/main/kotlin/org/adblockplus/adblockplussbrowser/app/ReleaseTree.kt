package org.adblockplus.adblockplussbrowser.app

import android.util.Log.ERROR
import android.util.Log.WARN
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import timber.log.Timber
import javax.inject.Inject


class ReleaseTree : @org.jetbrains.annotations.NotNull Timber.Tree() {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == ERROR || priority == WARN) {
            analyticsProvider.logException(t ?: return)
        }
    }
}
