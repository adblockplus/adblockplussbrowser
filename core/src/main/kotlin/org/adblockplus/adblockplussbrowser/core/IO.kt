package org.adblockplus.adblockplussbrowser.core

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import timber.log.Timber

const val DEFAULT_RETRY = 3

suspend fun <T> retryIO(
    description: String = "<missing description>",
    times: Int = DEFAULT_RETRY,
    initialDelay: Long = 200, // 0.2 second
    maxDelay: Long = 2000,    // 2 second
    factor: Double = 2.0,
    block: suspend () -> T
): T = coroutineScope {
    var currentDelay = initialDelay
    repeat(times - 1) { currentTry ->
        if (!coroutineContext.isActive) throw Exception("Job canceled when trying to execute retryIO")
        try {
            if (currentTry > 0) Timber.d("Retrying $description")
            return@coroutineScope block()
        } catch (e: Exception) {
            Timber.d(e, "failed call(${currentTry + 1}): $description")
            e.printStackTrace()
        }

        if (!coroutineContext.isActive) throw Exception("Job canceled when trying to execute retryIO")
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }

    if (!coroutineContext.isActive) throw Exception("Job canceled when trying to execute retryIO")
    return@coroutineScope block() // last attempt
}