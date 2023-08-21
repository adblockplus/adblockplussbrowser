package org.adblockplus.adblockplussbrowser.telemetry

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.telemetry.reporters.HttpReporter
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class TelemetryService {
    private var reporters: Map<HttpReporter.Configuration, HttpReporter> = mutableMapOf()

    fun addReporter(reporter: HttpReporter) =
        apply { reporters.plus(reporter.configuration to reporter) }

    fun scheduleReporting() {
        reporters.forEach() { (config, reporter) ->
            when (config.repeatable) {
                true -> scheduleRepeatableReporting(config, reporter)
                false -> scheduleOneTimeReporting(config, reporter)
            }
        }
    }

    private fun scheduleOneTimeReporting(
        config: HttpReporter.Configuration,
        reporter: HttpReporter,
    ) {
        OneTimeWorkRequestBuilder<TelemetryWorker>().apply {
            setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                config.backOffDelayMinutes,
                TimeUnit.MINUTES
            )
        }.build()
    }

    private fun scheduleRepeatableReporting(
        config: HttpReporter.Configuration,
        reporter: HttpReporter,
    ) {
        PeriodicWorkRequestBuilder<TelemetryWorker>(config.repeatInterval).apply {
            setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                config.backOffDelayMinutes,
                TimeUnit.MINUTES
            )
        }.build()
    }

}
