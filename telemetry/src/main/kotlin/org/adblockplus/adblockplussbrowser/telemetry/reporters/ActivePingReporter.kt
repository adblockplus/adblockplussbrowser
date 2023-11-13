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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.IOException
import org.adblockplus.adblockplussbrowser.base.os.AppInfo
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.currentSettings
import org.adblockplus.adblockplussbrowser.telemetry.BaseTelemetryWorker
import org.adblockplus.adblockplussbrowser.telemetry.BuildConfig
import org.adblockplus.adblockplussbrowser.telemetry.data.TelemetryRepository
import org.adblockplus.adblockplussbrowser.telemetry.schema.ActivePingSchema
import timber.log.Timber
import java.text.ParseException
import java.util.UUID
import javax.inject.Inject
import kotlin.time.DurationUnit.HOURS
import kotlin.time.DurationUnit.MINUTES
import kotlin.time.toDuration

/**
 * Prepares the required payload and interprets the response for the active ping request.
 *
 * This is a repeatable worker, it will be rescheduled after
 * the `ActivePingReporter.configuration.repeatInterval`.
 *
 * Request example:
 * ```
 * {
 *   "payload": {
 *     "first_ping": "2023-05-18T12:50:00Z",
 *     "last_ping": "2023-05-18T12:50:00Z",
 *     "previous_last_ping": "2023-05-18T12:50:00Z",
 *     "last_ping_tag": "string",
 *     "aa_active": true,
 *     "application": "string",
 *     "application_version": "string",
 *     "platform": "string",
 *     "platform_version": "string",
 *     "extension_name": "string",
 *     "extension_version": "string"
 *   }
 * }
 * ```
 * Response example:
 * ```
 * {
 *   "token": "2023-05-18T12:50:00Z"
 * }
 * ```
 * @param repository [TelemetryRepository] instance, injected by Hilt.
 * @param settings [SettingsRepository] instance, injected by Hilt.
 * @param appInfo [AppInfo] instance, injected by Hilt.
 * @see schema [ActivePingSchema]
 */
internal class ActivePingReporter @Inject constructor(
    private var repository: TelemetryRepository,
    private var settings: SettingsRepository,
    private var appInfo: AppInfo,
) : HttpReporter {

    companion object {
        val configuration: HttpReporter.Configuration
            get() = HttpReporter.Configuration(
                endpointUrl = if (BuildConfig.DEBUG) BuildConfig.EYEO_TELEMETRY_ACTIVEPING_URL_DEBUG
                else BuildConfig.EYEO_TELEMETRY_ACTIVEPING_URL,
                repeatable = true,
                backOffDelay = 2.toDuration(MINUTES),
                repeatInterval = if (BuildConfig.DEBUG)
                    15.toDuration(MINUTES)
                else 12.toDuration(HOURS) // required by Data team
            )
    }

    override val configuration: HttpReporter.Configuration
        get() = Companion.configuration

    /**
     * Prepares the payload for the active ping request.
     *
     * It uses [ActivePingSchema] for the payload serialization.
     *
     * @return [ResultPayload] that contains the payload _json_ as [String].
     */
    override suspend fun preparePayload(): ResultPayload {
        val data = repository.currentData()

        fun Long.takeIfNotZero() = takeIf { it != 0L }

        val savedFirstPing = data.firstPing.takeIfNotZero()?.toOffsetDateTime()
        val savedLastPing = data.lastPing.takeIfNotZero()?.toOffsetDateTime()
        val savedPrevLastPing = data.previousLastPing.takeIfNotZero()?.toOffsetDateTime()

        Timber.d(
            "Active ping saved: first ping is `%s`, last ping is `%s`, previous last ping is `%s`",
            savedFirstPing, savedLastPing, savedPrevLastPing
        )
        val acceptableAdsEnabled = settings.currentSettings().acceptableAdsEnabled
        Timber.d("AA enabled status is `%b`", acceptableAdsEnabled)

        // if this is the first request, we are not sending the last ping tag
        val lastPingTag = if (savedFirstPing != null) UUID.randomUUID().toString() else null
        val activePingSchema = ActivePingSchema(
            first_ping = savedFirstPing,
            last_ping = savedLastPing,
            previous_last_ping = savedPrevLastPing,
            last_ping_tag = lastPingTag,
            aa_active = acceptableAdsEnabled,
            application = appInfo.application.orEmpty(),
            application_version = appInfo.applicationVersion.orEmpty(),
            platform = appInfo.platform,
            platform_version = appInfo.platformVersion,
            extension_name = appInfo.extensionName.orEmpty(),
            extension_version = appInfo.addonVersion.orEmpty()
        )
        return Result.success(
            Json.encodeToString(
                ActivePingSchema.serializer(),
                activePingSchema
            ).let {
                Timber.d("Active ping payload: %s", it)
                // Wrapping into `payload` to match the server format
                // (documentation is not clear about it)
                "{\"payload\": $it}"
            }
        )
    }

    /**
     * Processes the response for the active ping request.
     *
     * It updates the
     * [org.adblockplus.adblockplussbrowser.telemetry.data.proto.TelemetryData.setFirstPing]
     * and [org.adblockplus.adblockplussbrowser.telemetry.data.proto.TelemetryData.setLastPing]
     * with the received token.
     *
     * @param response [ReportResponse] that contains the response _json_ as [String].
     * @return [Result] that contains [Unit] if the response was processed successfully,
     * or [IOException] if the response processing failed.
     */
    override suspend fun processResponse(response: ReportResponse): Result<Unit> =
        response.getString("token").let {
            if (it.isNullOrBlank()) return Result.failure(IOException("The token is empty"))
            Timber.d("Response `token` (date): %s", it)
            val time = it.toOffsetDateTime().toEpochMilliseconds()
            with(repository) {
                updateFirstPingIfNotSet(time)
                updateAndShiftLastPingToPreviousLast(time)
            }

            Result.success(Unit)
        }

    /**
     * Reads the http response body json and converts it to [ReportResponse].
     *
     * @param httpResponse [Response] instance.
     * @return [ReportResponse] an instance of [Data] that contains the response token.
     * @throws IllegalArgumentException if the response is not [Response].
     * @throws SerializationException if the response cannot be parsed.
     */
    @Throws(IllegalArgumentException::class)
    @ExperimentalSerializationApi
    override fun convert(httpResponse: Any): ReportResponse {
        if (httpResponse !is Response) {
            throw IllegalArgumentException("Expected Response, got ${httpResponse::class.java}")
        }
        val token = httpResponse.body?.byteStream()?.use { inputStream ->
            val jsonToken = Json.decodeFromStream(JsonObject.serializer(), inputStream)["token"]
            jsonToken?.jsonPrimitive?.content
                ?: throw SerializationException("JSON parsing failed: \"token\" field is null or missing")
        }
        return Data.Builder().putString("token", token).build()
    }

    /**
     * Parses [String] to [Instant].
     *
     * In case when the string cannot be parsed, it returns the current time shifted to UTC.
     * @throws ParseException if the string cannot be parsed (only in debug mode).
     */
    private fun String.toOffsetDateTime(): Instant {
        return try {
            // Expected date format in "token": "2023-05-18T12:50:00Z"
            // from `Instant.parse` docs:
            // obtains an instance of Instant from a text string such as 2007-12-03T10:15:30.00Z.
            // so it should be good to go without a special formatter
            Instant.parse(this)
        } catch (ex: ParseException) {
            Timber.e(ex)
            if (BuildConfig.DEBUG) {
                throw ex
            }
            Timber.e("Parsing 'token' failed, using client GMT time")

            // failed to parse, returning device time shifted to UTC (server time)
            // this might prevent errors when the server changed time format
            // thought should be carefully caught during testing
            Clock.System.now().toLocalDateTime(TimeZone.UTC).toInstant(TimeZone.UTC)
        }
    }

    /**
     * Converts epoch milliseconds to [Instant].
     */
    private fun Long.toOffsetDateTime(): Instant = Instant.fromEpochMilliseconds(this)

}

// We need a dedicated worker type for every reporter
// for the [WorkerRequestBuilder] to know how to build the worker
@HiltWorker
internal class ActivePingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    httpClient: OkHttpClient,
    reporter: ActivePingReporter,
) : BaseTelemetryWorker(appContext, params, httpClient, reporter)
