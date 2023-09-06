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

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import org.adblockplus.adblockplussbrowser.telemetry.reporters.ActivePingReporter
import org.adblockplus.adblockplussbrowser.telemetry.reporters.ActivePingWorker
import org.adblockplus.adblockplussbrowser.telemetry.reporters.HttpReporter
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * The telemetry service.
 * It is responsible for adding and scheduling the reporters.
 * If you'd like to implement the reporter, please refer to [HttpReporter]
 * or check the implementation of [ActivePingReporter].
 *
 * @property workRequests the list of reporters.
 */
class TelemetryService {
    private var workRequests: MutableMap<HttpReporter.Configuration, WorkRequest> = mutableMapOf()

    // Expose the workRequests for testing purposes
    internal val workRequestsForTests: Collection<WorkRequest>
        get() = workRequests.values

    /**
     * Adds an active ping reporter to the list of reporters.
     * The reporter will be scheduled after calling [scheduleReporting].
     */
    fun addActivePingReporter() = apply {
        // We don't pass any data to the ActivePingWorker
        addReporter<ActivePingWorker>(ActivePingReporter.configuration, Data.EMPTY)
    }

    /**
     * Adds a reporter to the list of reporters.
     * Since the WorkerRequestBuilder is generic, we need to pass the worker type explicitly.
     * The reporter will be scheduled after calling [scheduleReporting].
     *
     * @param W the reporter worker type.
     * @param config the reporter configuration.
     * @param data data to be passed to the reporter.
     */
    internal inline fun <reified W : BaseTelemetryWorker> addReporter(
        config: HttpReporter.Configuration,
        data: Data,
    ) =
        apply {
            when (config.repeatable) {
                true -> PeriodicWorkRequestBuilder<W>(
                    config.repeatInterval.inWholeMinutes,
                    TimeUnit.MINUTES
                )

                false -> OneTimeWorkRequestBuilder<W>()
            }.setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                // setBackoffCriteria with [Duration] is available from API 26
                // so we need to convert it to minutes and use setBackoffCriteria with long
                config.backOffDelay.inWholeMinutes,
                TimeUnit.MINUTES
            ).setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).setInputData(data).build().let {
                workRequests[config] = it
            }
        }

    /**
     * Schedules all reporters.
     * Then cleans everything added by [add*Reporter] methods
     *
     * The default behavior is to replace the existing work if it exists for the non-repeatable reporters
     * and to keep the existing work if it exists for the repeatable reporters.
     *
     * @param workManager the work manager instance.
     * @return the list of work request ids.
     * @throws IllegalStateException if no reporters added.
     */
    fun scheduleReporting(workManager: WorkManager): Collection<UUID> {
        if (workRequests.isEmpty()) {
            // It is fine to throw an exception here since it is a developer error
            // and it should be caught during testing
            throw IllegalStateException(
                "No reporters to schedule. Add a reporter first by calling add*Reporter methods"
            )
        }
        val ids: MutableCollection<UUID> = mutableSetOf()
        workRequests.forEach() { (config, request) ->
            when (config.repeatable) {
                true -> workManager.enqueueUniquePeriodicWork(
                    config.endpointUrl,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request as PeriodicWorkRequest
                )

                false -> workManager.enqueueUniqueWork(
                    config.endpointUrl,
                    ExistingWorkPolicy.REPLACE,
                    request as OneTimeWorkRequest
                )
            }
            ids.add(request.id)
        }
        // Clear the workRequests after scheduling
        workRequests = mutableMapOf()
        // Since the return type is immutable, no need to convert to immutable collection
        // We just do the "typecast"
        return ids
    }

}
