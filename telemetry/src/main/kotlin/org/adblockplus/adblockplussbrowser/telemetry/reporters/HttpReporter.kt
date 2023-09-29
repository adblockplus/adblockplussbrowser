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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.Duration

// Some aliases to make the code more readable
typealias ResultPayload = Result<String>
typealias ReportResponse = Data

/**
 * A reporter that is responsible for sending telemetry data to the server and processing the response.
 *
 * Prepares all necessary data for the request in [preparePayload]
 * and does all the necessary operations (parsing, saving etc) in [processResponse].
 *
 * It also has the configuration of the request, see [Configuration].
 *
 * It is expected to be used in [org.adblockplus.adblockplussbrowser.telemetry.TelemetryService].
 *
 * @see [ActivePingReporter] as an example of implementation.
 * @see [Configuration]
 */
interface HttpReporter {
    val configuration: Configuration

    /**
     * Prepares the payload for the request in the form of [String].
     *
     * @return [ResultPayload] that contains the payload if it was prepared successfully,
     * or [Throwable] if it failed.
     */
    suspend fun preparePayload(): ResultPayload

    /**
     * Does all the necessary operations (parsing, saving etc) with the response.
     * @param response [ReportResponse] that contains the response from the server.
     */
    suspend fun processResponse(response: ReportResponse): Result<Unit>

    /**
     * Converts the response from http response body to [ReportResponse].
     * @param httpResponse [Any] that contains the response from the server.
     * Could be of any type depending on the http client.
     */
    @ExperimentalSerializationApi
    fun convert(httpResponse: Any): ReportResponse

    data class Configuration(
        /**
         * The url of the endpoint.
         */
        val endpointUrl: String,
        /**
         * The interval between the requests.
         */
        val repeatable: Boolean,
        /**
         * The delay before the first request.
         */
        val backOffDelay: Duration,
        /**
         * The interval between the requests.
         */
        val repeatInterval: Duration,
    )
}

