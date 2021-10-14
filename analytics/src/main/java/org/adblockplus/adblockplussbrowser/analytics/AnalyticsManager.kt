package org.adblockplus.adblockplussbrowser.analytics

class AnalyticsManager(private val providers: List<AnalyticsProvider>) : AnalyticsProvider {

    override fun logEvent(analyticsEvent: AnalyticsEvent) {
        for (provider in providers) {
            provider.logEvent(analyticsEvent)
        }
    }

    override fun logException(throwable: Throwable) {
        for (provider in providers) {
            provider.logException(throwable)
        }
    }

    override fun logWarning(warning: String) {
        for (provider in providers) {
            provider.logWarning(warning)
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