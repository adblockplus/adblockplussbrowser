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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import org.adblockplus.adblockplussbrowser.telemetry.data.proto.TelemetryData

/**
 * Storage for last and previous last ping timestamps.
 */
internal interface TelemetryRepository {
    val data: Flow<TelemetryData>

    /**
     * Returns the latest [TelemetryData] value from a [Flow]
     */
    suspend fun currentData(): TelemetryData = data.take(1).single()

    /**
     * Updates [TelemetryData.setFirstPing] if it is not set.
     */
    suspend fun updateFirstPingIfNotSet(firstPing: Long)

    /**
     * Updates [TelemetryData.setLastPing] and shifts the
     * previous last ping to [TelemetryData.setPreviousLastPing].
     */
    suspend fun updateAndShiftLastPingToPreviousLast(newLastPing: Long)
}
