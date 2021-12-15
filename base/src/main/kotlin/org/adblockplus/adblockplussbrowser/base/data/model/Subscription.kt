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
import org.adblockplus.adblockplussbrowser.base.BuildConfig

@Parcelize
data class Subscription(
    val url: String,
    val title: String,
    val lastUpdate: Long,
) : Parcelable {
    /**
     * If the subscription url has easylist-downloads.adblockplus.org as domain, the latter get
     * replaced by a randomized url on eyeo.com.
     */
    val randomizedUrl: String
        get() = when (BuildConfig.FLAVOR_product) {
            "abp" -> url.replace(
                "easylist-downloads.adblockplus.org",
                "${(0..9).random()}.samsung-internet.filter-list-downloads.eyeo.com"
            )
            "adblock" -> url.replace(
                "easylist-downloads.adblockplus.org",
                "${(0..9).random()}.samsung-internet.filter-list-downloads.getadblock.com"
            ).replace("exceptionrules.txt", "samsung_internet_browser.txt")
            "crystal" -> url // no op for crystal so far
            else -> throw NotImplementedError("You forgot to specify a URL override for the " +
                    "flavor you have added")
        }
}
