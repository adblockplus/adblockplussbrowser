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

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.adblockplus.adblockplussbrowser.telemetry.reporters.HttpReporter
import timber.log.Timber
import javax.inject.Inject
 class TelemetryWorker constructor(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // if it is a periodic check, force update subscriptions
        return@withContext try {
            Timber.d("TELEMETRY JOB")

            if (httpTelemetry.report(this@TelemetryWorker).isSuccess) {
                Timber.i("Telemetry worker success")
                return@withContext Result.success()
            }
            Timber.w("Telemetry report failed, retry scheduled")
            return@withContext Result.retry()
        } catch (ex: Exception) {
            Timber.w("Telemetry report failed, retry scheduled")
            if (ex is CancellationException) Result.success() else Result.retry()
        }
    }
}