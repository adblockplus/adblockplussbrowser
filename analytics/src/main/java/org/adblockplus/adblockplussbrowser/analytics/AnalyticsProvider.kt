package org.adblockplus.adblockplussbrowser.analytics

interface AnalyticsProvider {
    fun logEvent(analyticsEvent: AnalyticsEvent)
    fun logException(throwable: Throwable)
    fun enable()
    fun disable()
}