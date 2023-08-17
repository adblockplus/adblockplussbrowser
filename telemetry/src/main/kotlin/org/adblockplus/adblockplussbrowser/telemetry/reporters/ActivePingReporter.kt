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

package org.adblockplus.adblockplussbrowser.telemetry.reporters

import androidx.work.Data
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Response
import okio.IOException
import org.adblockplus.adblockplussbrowser.base.BuildConfig
import org.adblockplus.adblockplussbrowser.base.os.AppInfo
import org.adblockplus.adblockplussbrowser.base.os.CallingApp
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.currentSettings
import org.adblockplus.adblockplussbrowser.telemetry.data.TelemetryRepository
import org.adblockplus.adblockplussbrowser.telemetry.schema.ActivePingSchema
import timber.log.Timber
import java.text.ParseException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

internal class ActivePingReporter(
    private val callingApp: CallingApp,
    private val repository: TelemetryRepository,
    private val settings: SettingsRepository,
    private val appInfo: AppInfo,
) : HttpReporter, OkHttpReportResultConvertor {
    override val endpointUrl: String
        get() = "https://test-telemetry.data.eyeo.it/topic/webextension_activeping/version/1"

    override suspend fun preparePayload(): Result<String> = coroutineScope {
        val data = repository.currentData()

        val savedFirstPing = fromLongToOffsetDateTime(data.firstPing)
        val savedLastPing = fromLongToOffsetDateTime(data.lastPing)
        val savedPrevLastPing = fromLongToOffsetDateTime(data.previousLastPing)
        Timber.d(
            "Active ping saved: first ping is `%s`, last ping is `%s`, previous last ping is `%s`",
            savedFirstPing, savedLastPing, savedPrevLastPing
        )
        val acceptableAdsEnabled = settings.currentSettings().acceptableAdsEnabled
        Timber.d("AA enabled status is `%b`", acceptableAdsEnabled)

        ActivePingSchema(
            first_ping = savedFirstPing,
            last_ping = savedLastPing,
            previous_last_ping = savedPrevLastPing,
            last_ping_tag = UUID.randomUUID().toString(),
            aa_active = acceptableAdsEnabled,
            addon_name = appInfo.addonName,
            addon_version = appInfo.addonVersion,
            application = appInfo.application.orEmpty(),
            application_version = appInfo.applicationVersion.orEmpty(),
            platform = appInfo.platform,
            platform_version = appInfo.platformVersion,
            extension_name = callingApp.applicationName,
            extension_version = callingApp.applicationVersion
        ).let {
            Result.success(it.toString())
        }
    }

    override suspend fun processResponse(response: ReportResponse): Result<Unit> =
        response.getString("token").let {
            if (it.isNullOrBlank()) return Result.failure(IOException("The token is empty"))
            Timber.d("Response `token` (date): %s", it)
            val time = parseDateString(it).toEpochSecond()
            with(repository) {
                updateFirstPingIfNotSet(time)
                updateAndShiftLastPingToPreviousLast(time)
            }

            Result.success(Unit)
        }

    @OptIn(ExperimentalSerializationApi::class)
    override fun convert(httpResponse: Response): ReportResponse =
        Data.Builder().apply {
            httpResponse.body?.byteStream()?.use { inputStream ->
                try {
                    val (key, value) = Json.decodeFromStream<Pair<String, String>>(inputStream)
                    putString(key, value)
                } catch (ignore: SerializationException) {
                }
            }
        }.build()


    private fun parseDateString(rawDate: String): OffsetDateTime {
        return try {
            // Expected date format in "token": "2023-05-18T12:50:00Z"
            // from `Instant.parse` docs:
            // obtains an instance of Instant from a text string such as 2007-12-03T10:15:30.00Z.
            // so it should be good to go without a special formatter
            OffsetDateTime.parse(rawDate)
        } catch (ex: ParseException) {
            Timber.e(ex)
            if (BuildConfig.DEBUG) {
                throw ex
            }
            Timber.e("Parsing 'token' failed, using client GMT time")

            // failed to parse, returning device time shifted to UTC (server time)
            // this might prevent errors when the server changed time format
            // thought should be carefully caught during testing
            OffsetDateTime.now(ZoneOffset.UTC)
        }
    }

    private fun fromLongToOffsetDateTime(millisecondsSinceEpoch: Long): OffsetDateTime {
        val instant = Instant.ofEpochMilli(millisecondsSinceEpoch)
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC) // in fact same to GMT
    }
}