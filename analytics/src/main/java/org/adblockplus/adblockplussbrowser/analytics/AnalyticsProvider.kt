package org.adblockplus.adblockplussbrowser.analytics

interface AnalyticsProvider {
    fun logEvent(analyticsEvent: AnalyticsEvent)
    fun enable()
    fun disable()
}