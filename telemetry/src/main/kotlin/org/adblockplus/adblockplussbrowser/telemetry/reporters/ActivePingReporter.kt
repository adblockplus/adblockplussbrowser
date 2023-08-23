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

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors.*
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Response
import okio.IOException
import org.adblockplus.adblockplussbrowser.base.BuildConfig
import org.adblockplus.adblockplussbrowser.base.os.AppInfo
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.currentSettings
import org.adblockplus.adblockplussbrowser.telemetry.TelemetryWorker
import org.adblockplus.adblockplussbrowser.telemetry.data.TelemetryRepository
import org.adblockplus.adblockplussbrowser.telemetry.schema.ActivePingSchema
import timber.log.Timber
import java.text.ParseException
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@HiltWorker
class ActivePingReporter @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
) : TelemetryWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ActivePingReporterEntryPoint {
        fun telemetryRepository(): TelemetryRepository
        fun settingsRepository(): SettingsRepository
        fun appInfo(): AppInfo
    }

    private var repository: TelemetryRepository

    private var settings: SettingsRepository

    private var appInfo: AppInfo

    init {
        EntryPoints.get(
            appContext,
            ActivePingReporterEntryPoint::class.java
        ).apply {
            repository = telemetryRepository()
            settings = settingsRepository()
            appInfo = appInfo()
        }
    }

    companion object {
        val configuration: HttpReporter.Configuration
            get() = HttpReporter.Configuration(
                endpointUrl = "https://test-telemetry.data.eyeo.it/topic/webextension_activeping/version/1",
                repeatable = false,
                backOffDelayMinutes = 2L,
                repeatInterval = Duration.ofHours(12L)
            )
    }

    override val configuration: HttpReporter.Configuration
        get() = Companion.configuration

    override suspend fun preparePayload(): ResultPayload {
        val data = repository.currentData()

        val savedFirstPing = data.firstPing.toOffsetDateTime()
        val savedLastPing = data.lastPing.toOffsetDateTime()
        val savedPrevLastPing = data.previousLastPing.toOffsetDateTime()
        Timber.d(
            "Active ping saved: first ping is `%s`, last ping is `%s`, previous last ping is `%s`",
            savedFirstPing, savedLastPing, savedPrevLastPing
        )
        val acceptableAdsEnabled = settings.currentSettings().acceptableAdsEnabled
        Timber.d("AA enabled status is `%b`", acceptableAdsEnabled)

        return kotlin.Result.success(
            ActivePingSchema(
                first_ping = savedFirstPing,
                last_ping = savedLastPing,
                previous_last_ping = savedPrevLastPing,
                last_ping_tag = UUID.randomUUID().toString(),
                aa_active = acceptableAdsEnabled,
                addon_name = appInfo.addonName,
                addon_version = appInfo.addonVersion.orEmpty(),
                application = appInfo.application.orEmpty(),
                application_version = appInfo.applicationVersion.orEmpty(),
                platform = appInfo.platform,
                platform_version = appInfo.platformVersion,
                extension_name = appInfo.extensionName,
                extension_version = appInfo.extensionVersion
            ).toString()
        )
    }

    override suspend fun processResponse(response: ReportResponse): kotlin.Result<Unit> =
        response.getString("token").let {
            if (it.isNullOrBlank()) return kotlin.Result.failure(IOException("The token is empty"))
            Timber.d("Response `token` (date): %s", it)
            val time = it.toOffsetDateTime().toEpochSecond()
            with(repository) {
                updateFirstPingIfNotSet(time)
                updateAndShiftLastPingToPreviousLast(time)
            }

            kotlin.Result.success(Unit)
        }

    @ExperimentalSerializationApi
    override fun convert(httpResponse: Any): ReportResponse =
        if (httpResponse is Response) {
            Data.Builder().apply {
                httpResponse.body?.byteStream()?.use { inputStream ->
                    try {
                        val (key, value) = Json.decodeFromStream<Pair<String, String>>(inputStream)
                        putString(key, value)
                    } catch (ignore: SerializationException) {
                    }
                }
            }.build()
        } else {
            throw NotImplementedError("We only support OkHttpResponse in ActivePingReporter")
        }

    private fun String.toOffsetDateTime(): OffsetDateTime {
        return try {
            // Expected date format in "token": "2023-05-18T12:50:00Z"
            // from `Instant.parse` docs:
            // obtains an instance of Instant from a text string such as 2007-12-03T10:15:30.00Z.
            // so it should be good to go without a special formatter
            OffsetDateTime.parse(this)
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

    private fun Long.toOffsetDateTime(): OffsetDateTime {
        val instant = Instant.ofEpochMilli(this)
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC) // in fact same to GMT
    }
}
