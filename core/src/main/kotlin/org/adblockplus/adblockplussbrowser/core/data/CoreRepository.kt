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

package org.adblockplus.adblockplussbrowser.core.data

import kotlinx.coroutines.flow.Flow
import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState

interface CoreRepository {
    val data: Flow<CoreData>
    var subscriptionsPath: String?

    suspend fun getDataSync(): CoreData

    suspend fun setConfigured()

    suspend fun updateDownloadedSubscriptions(subscriptions: List<DownloadedSubscription>,
                                              updateTimestamp: Boolean)

    suspend fun updateLastUpdated(lastUpdated: Long)

    suspend fun updateLastUserCountingResponse(lastUserCountingResponse: Long)

    suspend fun updateUserCountingCount(userCountingCount: Int)

    suspend fun updateSavedState(savedState: SavedState)

    companion object {
        const val KEY_CURRENT_SUBSCRIPTIONS_FILE = "KEY_CURRENT_SUBSCRIPTIONS_FILE"
    }
}