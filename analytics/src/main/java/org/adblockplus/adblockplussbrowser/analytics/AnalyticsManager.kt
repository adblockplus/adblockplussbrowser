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

package org.adblockplus.adblockplussbrowser.analytics

import java.lang.Exception

class AnalyticsManager(private val providers: List<AnalyticsProvider>) : AnalyticsProvider {

    override fun logEvent(analyticsEvent: AnalyticsEvent) {
        for (provider in providers) {
            provider.logEvent(analyticsEvent)
        }
    }

    override fun logException(exception: Exception) {
        for (provider in providers) {
            provider.logException(exception)
        }
    }

    override fun logError(error: String) {
        for (provider in providers) {
            provider.logError(error)
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
