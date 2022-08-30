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

// TODO: Document this stuff, why do we need this interface? What is it supposed to represent?
interface AnalyticsProvider {
    fun logEvent(analyticsEvent: AnalyticsEvent)
    fun logException(exception: Exception)
    fun logError(error: String)
    fun setUserProperty(analyticsProperty: AnalyticsUserProperty, analyticsPropertyValue: String)
    fun enable()
    fun disable()
}
