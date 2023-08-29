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
import java.util.concurrent.TimeUnit

/**
 * The telemetry service.
 * It is responsible for adding and scheduling the reporters.
 * If you'd like to implement the reporter, please refer to [HttpReporter]
 * or check the implementation of [ActivePingReporter].
 *
 * @property reporters the list of reporters.
 */
class TelemetryService {
    // It is public because we are using `inline fun <reified W> addReporter()`
    // which can't access private properties of the class.
    var reporters: Map<HttpReporter.Configuration, WorkRequest> = mutableMapOf()

    /**
     * Adds an active ping reporter to the list of reporters.
     * The reporter will be scheduled after calling [scheduleReporting].
     */
    fun addActivePingReporter() = apply {
        addReporter<ActivePingWorker>(ActivePingReporter.configuration)
    }

    /**
     * Adds a reporter to the list of reporters.
     * Since the WorkerRequestBuilder is generic, we need to pass the worker type explicitly.
     * The reporter will be scheduled after calling [scheduleReporting].
     *
     * @param W the reporter worker type.
     * @param config the reporter configuration.
     */
    private inline fun <reified W: BaseTelemetryWorker>addReporter(config: HttpReporter.Configuration) =
        apply {
            when (config.repeatable) {
                true -> PeriodicWorkRequestBuilder<W>(config.repeatInterval)
                false -> OneTimeWorkRequestBuilder<W>()
            }.setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                config.backOffDelayMinutes,
                TimeUnit.MINUTES
            ).setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build().let {
                reporters = reporters.plus(config to it)
            }
        }

    /**
     * Schedules all reporters.
     * The default behavior is to replace the existing work if it exists for the non-repeatable reporters
     * and to keep the existing work if it exists for the repeatable reporters.
     *
     * @param workManager the work manager instance.
     */
    fun scheduleReporting(workManager: WorkManager) {
        reporters.forEach() { (config, request) ->
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
        }
    }


}
