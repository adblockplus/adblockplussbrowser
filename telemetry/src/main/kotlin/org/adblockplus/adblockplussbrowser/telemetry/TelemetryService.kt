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
 * @property workRequests the list of reporters.
 */
class TelemetryService {
    // It is public because we are using `inline fun <reified W> addReporter()`
    // which can't access private properties of the class.
    var workRequests: Map<HttpReporter.Configuration, WorkRequest> = mutableMapOf()

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
                // setBackoffCriteria with [Duration] is available from API 26
                // so we need to convert it to minutes and use setBackoffCriteria with long
                config.backOffDelay.toMinutes(),
                TimeUnit.MINUTES
            ).setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build().let {
                workRequests = workRequests.plus(config to it)
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
        }
    }


}
