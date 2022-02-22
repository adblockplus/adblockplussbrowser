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

package org.adblockplus.adblockplussbrowser.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CoreData(
    val configured: Boolean,
    val lastUpdated: Long,
    val lastState: SavedState,
    val downloadedSubscription: List<DownloadedSubscription>,
    val lastUserCountingResponse: Long,
    val userCountingCount: Int
) : Parcelable

@Parcelize
internal data class SavedState(
    val acceptableAdsEnabled: Boolean,
    val allowedDomains: List<String>,
    val blockedDomains: List<String>,
    val primarySubscriptions: List<String>,
    val otherSubscriptions: List<String>
) : Parcelable