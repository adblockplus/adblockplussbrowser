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

package org.adblockplus.adblockplussbrowser.preferences.helpers

import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData

object Fakes {

    val fakeReportIssueData = ReportIssueData(
        type = "false positive",
        email = "test@email.com",
        comment = "test request",
        url = "http://www.example.com"
    )

    // Long response body of 550 characters
    const val longResponseBody = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. " +
            "Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et " +
            "magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec," +
            " pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. " +
            "Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. " +
            "In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis " +
            "eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. " +
            "Aenean vulputate"
}

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

