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

import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.adblockplus.adblockplussbrowser.base.data.HttpConstants
import org.adblockplus.adblockplussbrowser.base.data.HttpConstants.HTTP_HEADER_AUTHORIZATION
import org.adblockplus.adblockplussbrowser.telemetry.reporters.HttpReporter
import timber.log.Timber
import java.net.HttpRetryException
import java.net.HttpURLConnection

internal class HttpTelemetry(
    private val okHttpClient: OkHttpClient,
) {
    @ExperimentalSerializationApi
    suspend fun report(reporter: HttpReporter): Result<Unit> =
        coroutineScope {
            val url = reporter.configuration.endpointUrl.toHttpUrl()
            val requestBody = reporter.preparePayload().getOrThrow().toRequestBody()
            Timber.d("Sending request to $url")
            val request = Request.Builder().url(url)
                .addHeader(
                    HTTP_HEADER_AUTHORIZATION,
                    "Bearer ".plus(BuildConfig.EYEO_TELEMETRY_ACTIVEPING_AUTH_TOKEN)
                )
                .post(requestBody).build()
            okHttpClient.newCall(request).execute().use { response ->
                when (response.code) {
                    HttpURLConnection.HTTP_CREATED -> {
                        reporter.processResponse(reporter.convert(response))
                    }

                    else -> {
                        val error = getHttpErrorMessage(response)
                        return@use Result.failure(
                            HttpRetryException(
                                error,
                                response.code
                            )
                        )

                    }
                }
            }
        }

    companion object {
        private const val HTTP_ERROR_LOG_HEADER_USER_COUNTER =
            "OkHttpUserCounter HTTP error, return code"

        private fun getHttpErrorMessage(response: Response) =
            ("$HTTP_ERROR_LOG_HEADER_USER_COUNTER ${response.code}"
                    + "\nHeaders:\n${
                response.headers.toString()
                    .take(HttpConstants.HTTP_ERROR_AVERAGE_HEADERS_SIZE)
            }"
                    + "\nBody:\n${
                response.body?.string()
                    ?.take(HttpConstants.HTTP_ERROR_MAX_BODY_SIZE) ?: ""
            }")
    }
}