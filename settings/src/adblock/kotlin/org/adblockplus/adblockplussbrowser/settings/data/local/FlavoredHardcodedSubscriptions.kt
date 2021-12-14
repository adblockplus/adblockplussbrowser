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
package org.adblockplus.adblockplussbrowser.settings.data.local

/**
 * A flavored list of Hardcoded subscriptions for `Adblock` app flavor
 */
internal class FlavoredHardcodedSubscriptions: HardcodedSubscriptionsBase() {
    // we override AA list for `Adblock`
    override val acceptableAds = subscription {
        title = "Acceptable Ads"
        // `*.samsung-internet` will be replaced with [0-9].samsung-internet, eg `3.samsung-internet`
        url = "https://*.samsung-internet.filter-list-downloads.getadblock.com/samsung_internet_browser.txt"
    }

    val defaultPrimarySubscriptions = listOf(easylist) + regionalSubscriptions
}