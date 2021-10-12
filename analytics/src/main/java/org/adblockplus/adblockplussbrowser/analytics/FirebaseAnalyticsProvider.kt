package org.adblockplus.adblockplussbrowser.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class FirebaseAnalyticsProvider(appContext: Context) : AnalyticsProvider {

    private var firebaseAnalytics = FirebaseAnalytics.getInstance(appContext)

    override fun logEvent(analyticsEvent: AnalyticsEvent) {
        val bundle = Bundle()
        val logEvent = analyticsEvent.eventName
        Timber.i(logEvent)
        firebaseAnalytics.logEvent(logEvent, bundle)
    }

    override fun setUserProperty(analyticsProperty: AnalyticsUserProperty, analyticsPropertyValue: String) {
        Timber.i("set user property $analyticsProperty to $analyticsPropertyValue")
        firebaseAnalytics.setUserProperty(analyticsProperty.propertyName, analyticsPropertyValue)
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
