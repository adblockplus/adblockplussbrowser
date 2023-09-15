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

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.adblockplus.adblockplussbrowser.base.data.takeSingle
import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState
import org.adblockplus.adblockplussbrowser.core.data.proto.ProtoCoreData
import org.adblockplus.adblockplussbrowser.core.data.proto.toCoreData
import org.adblockplus.adblockplussbrowser.core.data.proto.toProtoDownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.proto.toProtoSavedState

internal class DataStoreCoreRepository(
    private val dataStore: DataStore<ProtoCoreData>,
    private val sharedPrefs: SharedPreferences
) : CoreRepository {
    override val data: Flow<CoreData> = dataStore.data
        .map { it.toCoreData() }

    override var subscriptionsPath: String?
        get() = sharedPrefs.getString(CoreRepository.KEY_CURRENT_SUBSCRIPTIONS_FILE, null)
        set(value) {
            sharedPrefs.edit().putString(CoreRepository.KEY_CURRENT_SUBSCRIPTIONS_FILE, value).apply()
        }

    override suspend fun getDataSync(): CoreData = data.takeSingle()

    override suspend fun setConfigured() {
        dataStore.updateData { data ->
            data.toBuilder().setConfigured(true).build()
        }
    }

    override suspend fun updateDownloadedSubscriptions(
        subscriptions: List<DownloadedSubscription>,
        updateTimestamp: Boolean
    ) {
        dataStore.updateData { data ->
            val set = subscriptions.map { it.toProtoDownloadedSubscription() }.toMutableSet()
            set.addAll(data.downloadedSubscriptionsList)
            data.toBuilder().apply {
                clearDownloadedSubscriptions()
                addAllDownloadedSubscriptions(set.toList())
                if (updateTimestamp) {
                    lastUpdate = System.currentTimeMillis()
                }
            }.build()
        }
    }

    override suspend fun updateLastUpdated(lastUpdated: Long) {
        dataStore.updateData { data ->
            data.toBuilder().setLastUpdate(lastUpdated).build()
        }
    }

    override suspend fun updateLastUserCountingResponse(lastUserCountingResponse: Long) {
        dataStore.updateData { data ->
            data.toBuilder().setLastUserCountingResponse(lastUserCountingResponse).build()
        }
    }

    override suspend fun updateUserCountingCount(userCountingCount: Int) {
        dataStore.updateData { data ->
            data.toBuilder().setUserCountingCount(userCountingCount).build()
        }
    }

    override suspend fun updateSavedState(savedState: SavedState) {
        dataStore.updateData { data ->
            data.toBuilder().setLastState(savedState.toProtoSavedState()).build()
        }
    }

}

