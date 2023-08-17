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

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.adblockplus.adblockplussbrowser.telemetry.reporters.HttpReporter
import timber.log.Timber
import javax.inject.Inject

@HiltWorker
internal class TelemetryWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @Inject
    internal lateinit var reporters: HttpReporter

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // if it is a periodic check, force update subscriptions
        return@withContext try {
            Timber.d("TELEMETRY JOB")

            if (reporters.preparePayload().isSuccess) {
                Timber.i("User counted")
                return@withContext Result.success()
            }
            Timber.w("Telemetry report failed, retry scheduled")
            return@withContext Result.retry()
        } catch (ex: Exception) {
            Timber.w("User counting failed, retry scheduled")
            if (ex is CancellationException) Result.success() else Result.retry()
        }
    }
}