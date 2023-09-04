import android.content.Context
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.OkHttpClient
import org.adblockplus.adblockplussbrowser.telemetry.BaseTelemetryWorker
import org.adblockplus.adblockplussbrowser.telemetry.reporters.HttpReporter
import org.mockito.Mockito

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

internal class FakeHttpWorker constructor(
    context: Context,
    params: WorkerParameters,
) : BaseTelemetryWorker(
    context,
    params,
    Mockito.mock(OkHttpClient::class.java),
    Mockito.mock(HttpReporter::class.java)
) {

    companion object {
        private const val PARAM_RETURN_RESULT = "ReturnResult"
        private const val PARAM_SLEEP_DURATION = "SleepDuration"
        private const val DEFAULT_SLEEP_DURATION = 100 //ms

        fun config(returnResult: Result, sleepDurationMs: Int = DEFAULT_SLEEP_DURATION): Data {
            return Data.Builder()
                .putInt(PARAM_RETURN_RESULT, returnResult.toInt())
                .putInt(PARAM_SLEEP_DURATION, sleepDurationMs)
                .build()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @ExperimentalSerializationApi
    override suspend fun doWork(): Result {
        Thread.sleep(inputData.getLong(PARAM_SLEEP_DURATION, DEFAULT_SLEEP_DURATION.toLong()))
        return inputData.getInt(PARAM_RETURN_RESULT, 0).toResult()
    }
}

private fun ListenableWorker.Result.toInt(): Int {
    return when (this) {
        is ListenableWorker.Result.Success -> 0
        is ListenableWorker.Result.Retry -> 1
        is ListenableWorker.Result.Failure -> 2
        else -> 3
    }
}

private fun Int.toResult(): ListenableWorker.Result {
    return when (this) {
        0 -> ListenableWorker.Result.success()
        1 -> ListenableWorker.Result.retry()
        2 -> ListenableWorker.Result.failure()
        else -> ListenableWorker.Result.success()
    }
}