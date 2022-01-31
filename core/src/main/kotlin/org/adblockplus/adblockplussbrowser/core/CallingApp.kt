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

package org.adblockplus.adblockplussbrowser.core

import androidx.work.Data

data class CallingApp(val applicationName: String, val applicationVersion: String) {
    constructor(data: Data) : this(
        data.getString(DATA_APP_NAME_TAG).orEmpty(),
        data.getString(DATA_APP_VERSION_TAG).orEmpty()
    )

    operator fun invoke(): Data = Data.Builder()
        .putString(DATA_APP_NAME_TAG, applicationName)
        .putString(DATA_APP_VERSION_TAG, applicationVersion)
        .build()

    companion object {
        internal const val DATA_APP_NAME_TAG = "APP_NAME"
        internal const val DATA_APP_VERSION_TAG = "APP_VERSION"
    }
}
