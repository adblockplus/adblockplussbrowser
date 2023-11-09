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

package org.adblockplus.adblockplussbrowser.telemetry.data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import org.adblockplus.adblockplussbrowser.telemetry.data.proto.TelemetryData
import timber.log.Timber
import javax.inject.Inject

/**
 * This class is responsible for storing last and previous last ping timestamps.
 *
 * It uses [DataStore] to store the data.
 * @param telemetryDataStore [DataStore] instance, injected by Hilt.
 */
internal class DataStoreTelemetryRepository @Inject constructor(
    private val telemetryDataStore: DataStore<TelemetryData>
) : TelemetryRepository {
    override val data: Flow<TelemetryData> = telemetryDataStore.data

    override suspend fun currentData(): TelemetryData = data.take(1).single()

    override suspend fun updateFirstPingIfNotSet(firstPing: Long) {
        Timber.d("updateFirstPingIfNotSet %s", firstPing)
        if (currentData().firstPing != 0L) return
        telemetryDataStore.updateData { data ->
            data.toBuilder().setFirstPing(firstPing).build()
        }
    }

    override suspend fun updateAndShiftLastPingToPreviousLast(newLastPing: Long) {
        telemetryDataStore.updateData { data ->
            data.toBuilder()
                .setLastPing(newLastPing)
                .setPreviousLastPing(data.lastPing).build()
        }
    }
}
