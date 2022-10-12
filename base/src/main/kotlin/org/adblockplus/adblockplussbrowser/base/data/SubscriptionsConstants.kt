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

package org.adblockplus.adblockplussbrowser.base.data

object SubscriptionsConstants {
    // On wifi connection we take 24 hours as threshold
    const val UNMETERED_REFRESH_INTERVAL_HOURS = 24
    // On metered connection (3g/4g) we take 3 days as threshold
    const val METERED_REFRESH_INTERVAL_DAYS = 3
    // Filename where we save the current version of the active subscriptions
    const val ACTIVE_SUBSCRIPTIONS_VERSIONS_FILE = "active_subscriptions_version_logs.txt"
}
