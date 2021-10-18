package org.adblockplus.adblockplussbrowser.analytics

interface AnalyticsProvider {
    fun logEvent(analyticsEvent: AnalyticsEvent)
    fun setUserProperty(analyticsProperty: AnalyticsUserProperty, analyticsPropertyValue: String)
    fun enable()
    fun disable()
}
