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

package org.adblockplus.adblockplussbrowser.preferences.data

import android.net.Uri
import android.util.Xml
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.adblockplus.adblockplussbrowser.preferences.BuildConfig
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData
import org.xmlpull.v1.XmlSerializer
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.io.IOException
import java.io.StringWriter
import java.net.HttpURLConnection.HTTP_OK
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import org.adblockplus.adblockplussbrowser.base.data.HttpConstants

/**
 * Contains logic of report data conversion into Xml and performing and HTTP post request to the backend.
 */
class HttpReportIssueRepository @Inject constructor() : ReportIssueRepository {

    private val okHttpClient = OkHttpClient()
    private val locale = Locale.getDefault()
    internal var serializer: XmlSerializer = Xml.newSerializer()
    var serverUrl: String = DEFAULT_URL
        internal set

    /**
     * Convert report issue data and send it to the backend.
     *
     * @param data ReportIssueData instance
     * @return the result of the operation
     */
    override suspend fun sendReport(data: ReportIssueData): Result<Unit> =
        makeXML(data).mapCatching { makeHttpPost(it).getOrThrow() }

    private suspend fun makeHttpPost(xml: String): Result<Unit> {
        val url = Uri.parse(serverUrl).buildUpon()
            .appendQueryParameter("version", "1")
            .appendQueryParameter("guid", UUID.randomUUID().toString()) // version 4, variant 1
            .appendQueryParameter("lang", locale.language).build().toString()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "text/xml")
            .addHeader("X-Adblock-Plus", "1")
            .post(xml.toRequestBody("text/xml".toMediaTypeOrNull()))
            .build()

        val response = okHttpClient.newCall(request).await()
        if (response.code != HTTP_OK) {
            return Result.failure(IOException("Server replied with ${response.code}"))
        }

        return runCatching { response.body!!.string() }
            .mapCatching { body ->
                val responseUrls = Regex(A_PATTERN).findAll(body).map { it.value }
                if (responseUrls.any()) {
                    // Just log the result will contain just Unit
                    Timber.d("ReportIssue report sent: ${responseUrls.last()}")
                } else {
                    val bodyLog = body.take(HttpConstants.HTTP_ERROR_MAX_BODY_SIZE)
                    Timber.d("ReportIssue report sent, but no URL received: $bodyLog")
                    // We throw in order to have a failure
                    throw IOException("Invalid response: $bodyLog.")
                }
            }
    }

    @SuppressWarnings("LongMethod")
    internal fun makeXML(data: ReportIssueData): Result<String> {
        val writer = StringWriter()
        return runCatching {
            serializer.setOutput(writer)
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            with(serializer) {
                startTag(null, "report")
                attribute(null, "type", data.type)

                startTag(null, "requests")
                endTag(null, "requests")

                startTag(null, "filters")
                endTag(null, "filters")

                startTag(null, "platform")
                attribute(null, "build", BuildConfig.BUILD_TYPE)
                attribute(null, "name", "ABP")
                attribute(null, "version", BuildConfig.APPLICATION_VERSION)
                endTag(null, "platform")

                startTag(null, "window")
                if (data.url.isNotEmpty()) {
                    attribute(null, "url", data.url)
                }
                endTag(null, "window")

                startTag(null, "subscriptions")
                for (subscription in data.subscriptions) {
                    startTag(null, "subscription")
                    attribute(null, "id", subscription.id)
                    if (subscription.version != null) {
                        attribute(null, "version", subscription.version)
                    }
                    attribute(null, "lastDownloadSuccess", subscription.lastUpdated.toString())
                    attribute(null, "softExpiration", subscription.softExpiration.toString())
                    attribute(null, "hardExpiration", subscription.hardExpiration.toString())
                    endTag(null, "subscription")
                }
                endTag(null, "subscriptions")

                startTag(null, "adblock-plus")
                attribute(null, "version", "Build")
                attribute(null, "locale", locale.toString())
                endTag(null, "adblock-plus")

                startTag(null, "application")
                attribute(null, "name", "Samsung Internet")
                attribute(null, "version", "unknown")
                attribute(null, "vendor", "Samsung Electronics Co.")
                attribute(null, "userAgent", "")
                endTag(null, "application")

                startTag(null, "comment")
                text(data.comment)
                endTag(null, "comment")

                startTag(null, "email")
                text(data.email)
                endTag(null, "email")

                startTag(null, "screenshot")
                attribute(null, "edited", "false")
                text(data.screenshot)
                endTag(null, "screenshot")

                endTag(null, "report")
                endDocument()
                flush()
            }
            writer.toString()
        }
    }

    companion object {
        const val DEFAULT_URL = """https://reports.adblockplus.org/submitReport"""
        const val A_PATTERN = """<a.+</a>"""
    }
}
