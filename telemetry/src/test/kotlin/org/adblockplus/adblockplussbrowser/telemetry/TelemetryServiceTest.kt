/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-2023 eyeo GmbH
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

import FakeHttpWorker
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.core.app.ApplicationProvider.*
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import org.adblockplus.adblockplussbrowser.telemetry.reporters.ActivePingReporter
import org.adblockplus.adblockplussbrowser.telemetry.reporters.ActivePingWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class TelemetryServiceTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context

    private lateinit var workManager: WorkManager

    private lateinit var testDriver: TestDriver

    private lateinit var telemetryService: TelemetryService

    private lateinit var testLifecycleOwner: TestLifecycleOwner

    @Before
    fun setUp() {
        context = getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)
        testDriver = WorkManagerTestInitHelper.getTestDriver(context)!! // if null, throw NPE
        telemetryService = TelemetryService()
        testLifecycleOwner = TestLifecycleOwner()
    }

    private fun getWorkInfo(id: UUID): WorkInfo {
        return workManager.getWorkInfoById(id).get()
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
            // addActivePingReporter() has not been called
            telemetryService.scheduleReporting(workManager)
        }
    }

    @Test
    fun `test work request correctly re-scheduled after calling scheduleReporting twice`() {
        val config = ActivePingReporter.configuration

        // Schedule the work request
        val ids = telemetryService.addFakeReporter(ListenableWorker.Result.success())
            .scheduleReporting(workManager)

        // Make sure it is only one entry
        assertEquals(1, ids.size)

        // Check if the work request is correctly scheduled
        val workInfo = getWorkInfo(ids.first())
        assertEquals(WorkInfo.State.ENQUEUED, workInfo.state)

        // Check if the work request is correctly scheduled after reschedule
        telemetryService.addFakeReporter(ListenableWorker.Result.success())
        // Make sure it is still one entry
        assertEquals(1, telemetryService.workRequestsForTests.size)
        telemetryService.scheduleReporting(workManager)

        // Collect all [WorkInfo] based on the name (endpointUrl) and make sure there is only
        // one job enqueued
        val workInfosAfterReschedule =
            workManager.getWorkInfosForUniqueWork(config.endpointUrl).get()
        assertEquals(1, workInfosAfterReschedule.size)
        // Check the status
        val workInfoAfterReschedule = workInfosAfterReschedule.first()
        assertEquals(WorkInfo.State.ENQUEUED, workInfoAfterReschedule.state)
    }

    private fun maybeScheduleWorkAndAssertSuccess(id: UUID, scheduleWork: Boolean) {
        val latch = CountDownLatch(3)

        // Before scheduling the work request, set observers
        workManager.getWorkInfoByIdLiveData(id).observe(testLifecycleOwner) {
            latch.countDown()
            when (latch.count) {
                // Check if the work request succeeds
                2L -> {
                    assertEquals(WorkInfo.State.ENQUEUED, it.state)
                    println("Work $id request enqueued")

                    // Allowing the worker to run
                    testDriver.setAllConstraintsMet(id)
                }
                // Check if the work request is running
                1L -> {
                    assertEquals(WorkInfo.State.RUNNING, it.state)
                    println("Work $id request is running")
                }
                // Check if the work request is enqueued
                0L -> {
                    assertEquals(WorkInfo.State.SUCCEEDED, it.state)
                    println("Work $id request succeeded")
                }
            }
        }
        if (scheduleWork) {
            // Schedule the work request
            telemetryService.scheduleReporting(workManager)
        }

        latch.await(5, TimeUnit.SECONDS)
        assertEquals(
            "Worker did not succeed, the status didn't reach WorkInfo.State.SUCCEEDED",
            0,
            latch.count
        )
    }

    @Test
    fun `test work request enqueued and succeeded`() {
        // Schedule the work request and retrieve work ids
        val id =
            telemetryService.addFakeReporter(ListenableWorker.Result.success()).workRequestsForTests.map { it.id }
                .first()

        // We have to put the assert first before scheduling the work request
        maybeScheduleWorkAndAssertSuccess(id, true)

        // Fake constraints met
        testDriver.setInitialDelayMet(id)
    }

    @Test
    fun `test work manager re-schedules after the repeat duration`() {
        // Schedule the work request and retrieve work ids
        val id =
            telemetryService.addFakeReporter(ListenableWorker.Result.success()).workRequestsForTests.map { it.id }
                .first()

        maybeScheduleWorkAndAssertSuccess(id, true)
        // Fake constraints met
        testDriver.setInitialDelayMet(id)

        // Wait for everything to settle down
        // without this, the WorkManager isn't able to re-schedule the work request
        Thread.sleep(100)

        // Check if the work request is correctly scheduled after reschedule
        testDriver.setPeriodDelayMet(id)
        maybeScheduleWorkAndAssertSuccess(id, false)
    }

    @Test
    fun `test work manager re-schedules after failure`() {
        // Schedule the work request and retrieve work ids
        val id =
            telemetryService.addFakeReporter(ListenableWorker.Result.retry())
                .scheduleReporting(workManager)
                .first()

        // Check if the work request is correctly scheduled after reschedule
        testDriver.setPeriodDelayMet(id)

        assertEquals(workManager.getWorkInfoById(id).get().state, WorkInfo.State.ENQUEUED)
    }
}

fun TelemetryService.addFakeReporter(result: ListenableWorker.Result) = apply {
    addReporter<FakeHttpWorker>(
        ActivePingReporter.configuration,
        FakeHttpWorker.config(result)
    )
}
