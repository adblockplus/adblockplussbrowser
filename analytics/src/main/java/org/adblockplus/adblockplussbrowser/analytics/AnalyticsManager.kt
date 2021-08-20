package org.adblockplus.adblockplussbrowser.analytics

class AnalyticsManager(private val providers: List<AnalyticsProvider>) : AnalyticsProvider {

    override fun logEvent(analyticsEvent: AnalyticsEvent) {
        for (provider in providers) {
            provider.logEvent(analyticsEvent)
        }
    }

    override fun enable() {
        logEvent(AnalyticsEvent.SHARE_EVENTS_ON)

        for (provider in providers) {
            provider.enable()
        }
    }

    override fun disable() {
        logEvent(AnalyticsEvent.SHARE_EVENTS_OFF)

        for (provider in providers) {
            provider.disable()
        }
    }

}