package org.adblockplus.adblockplussbrowser.analytics

class AnalyticsManager(private val providers: List<AnalyticsProvider>) : AnalyticsProvider {

    override fun logEvent(analyticsEvent: AnalyticsEvent) {
        for (provider in providers) {
            provider.logEvent(analyticsEvent)
        }
    }

    override fun setUserProperty(analyticsProperty: AnalyticsUserProperty, analyticsPropertyValue: String) {
        for (provider in providers) {
            provider.setUserProperty(analyticsProperty, analyticsPropertyValue)
        }
    }

    override fun enable() {
        for (provider in providers) {
            provider.enable()
        }

        logEvent(AnalyticsEvent.SHARE_EVENTS_ON)
    }

    override fun disable() {
        logEvent(AnalyticsEvent.SHARE_EVENTS_OFF)

        for (provider in providers) {
            provider.disable()
        }
    }

}
