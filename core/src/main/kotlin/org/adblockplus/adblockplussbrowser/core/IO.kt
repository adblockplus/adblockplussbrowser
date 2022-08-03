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

