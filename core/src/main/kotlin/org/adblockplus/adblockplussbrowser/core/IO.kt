package org.adblockplus.adblockplussbrowser.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber

const val DEFAULT_RETRY = 3

suspend fun <T> retryIO(
    description: String = "<missing description>",
    times: Int = DEFAULT_RETRY,
    initialDelay: Long = 200, // 0.2 second
    maxDelay: Long = 2000,    // 2 second
    factor: Double = 2.0,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: suspend () -> T
): T = withContext(dispatcher) {
    var currentDelay = initialDelay
    repeat(times - 1) { currentTry ->
        if (!coroutineContext.isActive) throw Exception("Job canceled when trying to execute retryIO")
        try {
            if (currentTry > 0) Timber.d("Retrying $description")
            return@withContext block()
        } catch (e: Exception) {
            Timber.e(e, "failed call(${currentTry + 1}): $description")
        }

        if (!coroutineContext.isActive) throw Exception("Job canceled when trying to execute retryIO")
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }

    if (!coroutineContext.isActive) throw Exception("Job canceled when trying to execute retryIO")
    return@withContext block() // last attempt
}