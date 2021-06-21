package org.adblockplus.adblockplussbrowser.core.extensions

import androidx.work.BackoffPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal inline fun <reified W : ListenableWorker> periodicWorkRequestBuilder(
    repeatInterval: Duration,
    flexInterval: Duration
): PeriodicWorkRequest.Builder {
    repeatInterval.inWholeMinutes
    return PeriodicWorkRequest.Builder(W::class.java, repeatInterval.inWholeMinutes, TimeUnit.MINUTES,
        flexInterval.inWholeMinutes, TimeUnit.MINUTES)
}

@ExperimentalTime
internal fun WorkRequest.Builder<PeriodicWorkRequest.Builder, PeriodicWorkRequest>.setInitialDelay(
    duration: Duration
): WorkRequest.Builder<PeriodicWorkRequest.Builder, PeriodicWorkRequest> {
    this.setInitialDelay(duration.inWholeMinutes, TimeUnit.MINUTES)
    return this
}

@ExperimentalTime
internal fun WorkRequest.Builder<PeriodicWorkRequest.Builder, PeriodicWorkRequest>.setBackoffCriteria(
    policy: BackoffPolicy,
    duration: Duration
): WorkRequest.Builder<PeriodicWorkRequest.Builder, PeriodicWorkRequest> {
    this.setBackoffCriteria(policy, duration.inWholeSeconds, TimeUnit.SECONDS)
    return this
}

@ExperimentalTime
internal fun WorkRequest.Builder<OneTimeWorkRequest.Builder, OneTimeWorkRequest>.setBackoffTime(
    duration: Duration
): WorkRequest.Builder<OneTimeWorkRequest.Builder, OneTimeWorkRequest> {
    this.setBackoffCriteria(BackoffPolicy.EXPONENTIAL, duration.inWholeSeconds, TimeUnit.SECONDS)
    return this
}