package org.adblockplus.adblockplussbrowser.app

import android.util.Log.ERROR
import android.util.Log.WARN
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import timber.log.Timber
import javax.inject.Inject


class ReleaseTree : Timber.Tree() {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (priority == ERROR || priority == WARN) {
            throwable?.let {
                analyticsProvider.logException(it)
            }
            message?.let {
                analyticsProvider.logWarning(it)
            }
        }
    }
}
