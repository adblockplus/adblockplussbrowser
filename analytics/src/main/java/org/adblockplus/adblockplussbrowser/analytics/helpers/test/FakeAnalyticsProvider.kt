package org.adblockplus.adblockplussbrowser.analytics.helpers.test

import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty

class FakeAnalyticsProvider : AnalyticsProvider {

    var event : AnalyticsEvent? = null
    var exception : Exception? = null
    var error : String? = null
    var userPropertyName : AnalyticsUserProperty? = null
    var userPropertyValue : String? = null

    override fun logEvent(analyticsEvent: AnalyticsEvent) {
        this.event = analyticsEvent
    }

    override fun logException(exception: Exception) {
        this.exception = exception
    }

    override fun logError(error: String) {
        this.error = error
    }

    override fun setUserProperty(
        analyticsProperty: AnalyticsUserProperty,
        analyticsPropertyValue: String
    ) {
        userPropertyName = analyticsProperty
        userPropertyValue = analyticsPropertyValue
    }

    override fun enable() { return }

    override fun disable() { return }
}