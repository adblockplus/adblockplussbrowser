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
import androidx.work.Data
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.adblockplus.adblockplusbrowser.testutils.FakeSettingsRepository
import org.adblockplus.adblockplussbrowser.base.os.AppInfo
import org.adblockplus.adblockplussbrowser.telemetry.PingConstants.FIRST_PING_TIME
import org.adblockplus.adblockplussbrowser.telemetry.data.DataStoreTelemetryRepository
import org.adblockplus.adblockplussbrowser.telemetry.data.datastore.TelemetryDataSerializer
import org.adblockplus.adblockplussbrowser.telemetry.data.proto.TelemetryData
import org.adblockplus.adblockplussbrowser.telemetry.reporters.ActivePingReporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@Serializable
data class Payload(
    @SerialName("last_ping_tag") val lastPingTag: String,
    @SerialName("first_ping") val firstPing: String,
    @SerialName("application") val application: String,
    @SerialName("application_version") val applicationVersion: String,
    @SerialName("aa_active") val aaActive: Boolean,
    @SerialName("platform") val platform: String,
    @SerialName("platform_version") val platformVersion: String,
    @SerialName("extension_name") val extensionName: String,
    @SerialName("extension_version") val extensionVersion: String
)

@Serializable
data class ActivePing(
    val payload: Payload
)

@RunWith(RobolectricTestRunner::class)
class ActivePingReporterTest {
    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val telemetryDataStore: DataStore<TelemetryData> =
        DataStoreFactory.create(
            produceFile = { testContext.dataStoreFile("telemetry.pb") },
            serializer = TelemetryDataSerializer
        )
    private val dataStoreTelemetryRepository: DataStoreTelemetryRepository =
        DataStoreTelemetryRepository(telemetryDataStore)
    private val settingsRepository = FakeSettingsRepository("")

    private lateinit var activePingReporter: ActivePingReporter

    @Before
    fun setUp() {
        runBlocking {
            dataStoreTelemetryRepository.updateFirstPingIfNotSet(FIRST_PING_TIME)
        }
        activePingReporter = ActivePingReporter(
            dataStoreTelemetryRepository,
            settingsRepository,
            AppInfo(),
        )
    }

    @Test
    fun `test prepare payload`() {
        runBlocking {
            val result = activePingReporter.preparePayload()
            assertTrue(result.isSuccess)
            result.onSuccess { activePingPayload ->
                val json = Json { ignoreUnknownKeys = true }
                val activePing = json.decodeFromString<ActivePing>(activePingPayload)
                assertTrue(activePing.payload.aaActive)
                assertEquals("2023-08-24T02:50:03.742Z", activePing.payload.firstPing)
                assertEquals("android", activePing.payload.platform)
            }
        }
    }

    @Test
    fun `test process response`() {
        runBlocking {
            val response = Data.Builder().putString("token", "2023-09-26T14:30:00Z").build()
            val processedResponse = activePingReporter.processResponse(response)
            assertTrue(processedResponse.isSuccess)
        }
    }
}
