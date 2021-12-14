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

package org.adblockplus.adblockplussbrowser.base.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subscription(
    val url: String,
    val title: String,
    val lastUpdate: Long,
) : Parcelable {
    /**
     * If the subscription url has easylist-downloads.adblockplus.org as domain, the latter get
     * replaced by a randomized url on eyeo.com.
     *
     * We do the replacement here because the url gets serialized in settings and we cannot change
     * it for the existing users
     */
    val randomizedUrl: String
        get() = url.replace( // we patch the domain with the new one for ABP flavor
            "easylist-downloads.adblockplus.org",
            "*.samsung-internet.filter-list-downloads.eyeo.com")

            .replace( // then we do the replacement for any samsung internet url
                "*.samsung-internet",
                "${(0..9).random()}.samsung-internet")
}
