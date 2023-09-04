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
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.adblockplus.adblockplussbrowser.telemetry.data.proto.TelemetryData
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class DataStoreTelemetryRepositoryTest {

    @Mock
    private lateinit var mockDataStore: DataStore<TelemetryData>

    private lateinit var dataStoreTelemetryRepository: DataStoreTelemetryRepository

    @ExperimentalCoroutinesApi
    @Test
    fun testCurrentDataWhenDataFlowHasItemsThenReturnsFirstItem() = runTest {
        // Arrange
        val expectedTelemetryData = TelemetryData.newBuilder().setFirstPing(123L).build()
        `when`(mockDataStore.data).thenReturn(flowOf(expectedTelemetryData))

        dataStoreTelemetryRepository = DataStoreTelemetryRepository(mockDataStore)

        // Act
        val actualTelemetryData = dataStoreTelemetryRepository.currentData()

        // Assert
        assertEquals(expectedTelemetryData, actualTelemetryData)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testCurrentDataWhenCalledThenTakeAndSingleMethodsAreCalled() = runTest {
        // Arrange
        val mockFlow: Flow<TelemetryData> = mock()
        `when`(mockDataStore.data).thenReturn(mockFlow)

        dataStoreTelemetryRepository = DataStoreTelemetryRepository(mockDataStore)

        // Act
        dataStoreTelemetryRepository.currentData()

        // Assert
        verify(mockFlow).take(1)
        verify(mockFlow).single()
    }
}
