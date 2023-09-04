import android.content.Context
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

    @ExperimentalSerializationApi
    override suspend fun doWork(): Result {
        return Result.success()
    }
}
