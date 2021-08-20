package org.adblockplus.adblockplussbrowser.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseAnalyticsProvider(appContext: Context) : AnalyticsProvider {

    private var firebaseAnalytics = FirebaseAnalytics.getInstance(appContext)

    override fun logEvent(analyticsEvent: AnalyticsEvent) {
        val bundle = Bundle()
        firebaseAnalytics.logEvent(analyticsEvent.eventName, bundle)
    }

    override fun enable() {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        firebaseAnalytics.setAnalyticsCollectionEnabled(true)
    }

    override fun disable() {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        firebaseAnalytics.setAnalyticsCollectionEnabled(false)
    }
}