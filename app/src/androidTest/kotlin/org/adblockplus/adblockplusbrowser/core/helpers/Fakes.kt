package org.adblockplus.adblockplusbrowser.core.helpers

import kotlinx.coroutines.flow.Flow
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences

class Fakes {
    class FakeAnalyticsProvider : AnalyticsProvider {

        var event : AnalyticsEvent? = null
        var exception : Exception? = null
        var userPropertyName : AnalyticsUserProperty? = null
        var userPropertyValue : String? = null

        override fun logEvent(analyticsEvent: AnalyticsEvent) {
            this.event = analyticsEvent
        }

        override fun logException(exception: Exception) {
            this.exception = exception
        }

        override fun setUserProperty(
            analyticsProperty: AnalyticsUserProperty,
            analyticsPropertyValue: String
        ) {
            userPropertyName = analyticsProperty
            userPropertyValue = analyticsPropertyValue
        }

        override fun enable() {}

        override fun disable() {}
    }

    class FakeActivationPreferences : ActivationPreferences {
        override val lastFilterListRequest: Flow<Long>
            get() = TODO("Not yet implemented")

        override suspend fun updateLastFilterRequest(lastFilterListRequest: Long) {
            TODO("Not yet implemented")
        }

    }
}