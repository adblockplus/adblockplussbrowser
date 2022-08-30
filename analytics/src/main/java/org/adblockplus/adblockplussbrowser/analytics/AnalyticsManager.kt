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

    private inline fun providersApply(f: AnalyticsProvider.() -> Unit) = providers.forEach { it.apply(f) }

    private inline fun providersApplyAndLog(log: AnalyticsEvent, f: AnalyticsProvider.() -> Unit) {
        providersApply(f)
        logEvent(log)
    }

    override fun logEvent(analyticsEvent: AnalyticsEvent) = providersApply { logEvent(analyticsEvent) }

    override fun logException(exception: Exception) = providersApply { logException(exception) }

    override fun logError(error: String) = providersApply { logError(error) }

    override fun setUserProperty(analyticsProperty: AnalyticsUserProperty, analyticsPropertyValue: String) =
        providersApply { setUserProperty(analyticsProperty, analyticsPropertyValue) }

    override fun enable() = providersApplyAndLog(AnalyticsEvent.SHARE_EVENTS_ON) { enable() }

    override fun disable() = providersApplyAndLog(AnalyticsEvent.SHARE_EVENTS_OFF) { disable() }
}
