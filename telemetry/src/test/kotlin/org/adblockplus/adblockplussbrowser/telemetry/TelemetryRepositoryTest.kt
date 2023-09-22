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

package org.adblockplus.adblockplussbrowser.telemetry

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.adblockplus.adblockplussbrowser.telemetry.data.DataStoreTelemetryRepository
import org.adblockplus.adblockplussbrowser.telemetry.data.datastore.TelemetryDataSerializer
import org.adblockplus.adblockplussbrowser.telemetry.data.proto.TelemetryData
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class TelemetryRepositoryTest {
    private val testContext: Context = ApplicationProvider.getApplicationContext()

    private val telemetryDataStore: DataStore<TelemetryData> =
        DataStoreFactory.create(
            produceFile = { testContext.dataStoreFile("telemetry.pb") },
            serializer = TelemetryDataSerializer
        )

    private val dataStoreTelemetryRepository: DataStoreTelemetryRepository =
        DataStoreTelemetryRepository(telemetryDataStore)

    @Test
    fun `test data store init`() {
        runBlocking {
            assertTrue(dataStoreTelemetryRepository.currentData().isInitialized)
        }
    }

    @Test
    fun `test update first ping in not set`() {
        runBlocking {
            val testFirstPing = 1692845403742
            dataStoreTelemetryRepository.updateFirstPingIfNotSet(testFirstPing)
            assertEquals(testFirstPing, dataStoreTelemetryRepository.currentData().firstPing)
        }
    }

    @Test
    fun `test update and shift lastPing to previousLast`() {
        runBlocking {
            val testNewLastPing = 1692845403742
            dataStoreTelemetryRepository.updateAndShiftLastPingToPreviousLast(1692845403742)
            val testSecondNewLastPing = 1692845403743
            dataStoreTelemetryRepository.updateAndShiftLastPingToPreviousLast(testSecondNewLastPing)
            assertEquals(testSecondNewLastPing, dataStoreTelemetryRepository.currentData().lastPing)
            assertEquals(testNewLastPing, dataStoreTelemetryRepository.currentData().previousLastPing)
        }
    }
}
