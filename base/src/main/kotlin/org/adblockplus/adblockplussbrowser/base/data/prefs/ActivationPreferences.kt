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

package org.adblockplus.adblockplussbrowser.base.data.prefs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ActivationPreferences {
    val lastFilterListRequest: Flow<Long>

    suspend fun isAdblockEnabled(): Flow<Boolean> {
        // When filters were requested and they are not expired we assume adblock is enabled
        return lastFilterListRequest.map {
            it != 0L && !isFilterRequestExpired(it)
        }
    }

    suspend fun updateLastFilterRequest(lastFilterListRequest: Long)

    companion object {
        // 30 days to expire filter request 30*24*60*60*1000 = 2592000000
        private const val FILTER_REQUEST_EXPIRE_TIME_SPAN = 2592_000_000
        fun isFilterRequestExpired(lastFilterRequest: Long) =
            System.currentTimeMillis() - lastFilterRequest > FILTER_REQUEST_EXPIRE_TIME_SPAN
    }
}