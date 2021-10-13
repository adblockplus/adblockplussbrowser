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

package org.adblockplus.adblockplussbrowser.settings.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription

@Parcelize
data class Settings(
    val adblockEnabled: Boolean,
    val acceptableAdsEnabled: Boolean,
    val updateConfig: UpdateConfig,
    val allowedDomains: List<String>,
    val blockedDomains: List<String>,
    val activePrimarySubscriptions: List<Subscription>,
    val activeOtherSubscriptions: List<Subscription>,
    val analyticsEnabled: Boolean
) : Parcelable

enum class UpdateConfig {
    WIFI_ONLY,
    ALWAYS
}