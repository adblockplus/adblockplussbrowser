/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.adblockplusbrowser.testutils

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
