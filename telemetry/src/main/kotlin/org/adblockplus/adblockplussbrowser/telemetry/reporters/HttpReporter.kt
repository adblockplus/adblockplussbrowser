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
import java.time.Duration

typealias ResultPayload = Result<String>
typealias ReportResponse = Data

interface HttpReporter {
    val configuration: Configuration

    suspend fun preparePayload(): ResultPayload

    suspend fun processResponse(response: ReportResponse): Result<Unit>

    @ExperimentalSerializationApi
    fun convert(httpResponse: Any): ReportResponse

    data class Configuration(
        val endpointUrl: String,
        val repeatable: Boolean,
        val backOffDelay: Duration,
        val repeatInterval: Duration,
    )
}

