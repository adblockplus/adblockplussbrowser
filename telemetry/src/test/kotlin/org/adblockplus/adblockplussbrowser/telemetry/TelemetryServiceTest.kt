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

import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.adblockplus.adblockplussbrowser.telemetry.reporters.ActivePingReporter
import org.adblockplus.adblockplussbrowser.telemetry.reporters.ActivePingWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class TelemetryServiceTest {

    private lateinit var telemetryService: TelemetryService
    private lateinit var mockWorkManager: WorkManager

    @Before
    fun setUp() {
        telemetryService = TelemetryService()
        mockWorkManager = mock()
    }

    @Test
    fun `test if active ping reporter added`() {
        telemetryService.addActivePingReporter()
        assertEquals(1, telemetryService.workRequestsForTests.size)
        val firstRequest = telemetryService.workRequestsForTests.first()
        assertEquals(ActivePingWorker::class.java.name, firstRequest.workSpec.workerClassName)
    }

    @Test
    fun `test scheduleReporting throws exception if no reporters added`() {
        assertThrows(IllegalStateException::class.java) {
            telemetryService.scheduleReporting(mockWorkManager)
        }
    }

    @Test
    fun `scheduleReporting enqueues unique periodic work for repeatable reporters`() {
        telemetryService.addActivePingReporter() // ActivePingReporter is repeatable

        telemetryService.scheduleReporting(mockWorkManager)

        val requestCaptor = argumentCaptor<PeriodicWorkRequest>()
        verify(mockWorkManager).enqueueUniquePeriodicWork(
            eq(ActivePingReporter.configuration.endpointUrl),
            eq(ExistingPeriodicWorkPolicy.KEEP),
            requestCaptor.capture()
        )
        assertEquals(ActivePingWorker::class.java.name, requestCaptor.firstValue.workSpec.workerClassName)
    }

    @Test
    fun `scheduleReporting enqueues unique work for non-repeatable reporters`() {
        val nonRepeatableConfig = ActivePingReporter.configuration.copy(repeatable = false)
        telemetryService.addReporter<FakeHttpWorker>(nonRepeatableConfig, Data.EMPTY)

        telemetryService.scheduleReporting(mockWorkManager)

        val requestCaptor = argumentCaptor<OneTimeWorkRequest>()
        verify(mockWorkManager).enqueueUniqueWork(
            eq(nonRepeatableConfig.endpointUrl),
            eq(ExistingWorkPolicy.REPLACE),
            requestCaptor.capture()
        )
        assertEquals(FakeHttpWorker::class.java.name, requestCaptor.firstValue.workSpec.workerClassName)
    }

    @Test
    fun `scheduleReporting clears work requests after execution`() {
        telemetryService.addActivePingReporter()
        assertEquals(1, telemetryService.workRequestsForTests.size)
        telemetryService.scheduleReporting(mockWorkManager)

        assertEquals(0, telemetryService.workRequestsForTests.size)
    }
}
