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
import java.io.StringWriter
import java.net.HttpURLConnection.HTTP_OK
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Contains logic of report data conversion into Xml and performing and HTTP post request to the backend.
 */
class HttpReportIssueRepository @Inject constructor() : ReportIssueRepository {

    private val okHttpClient = OkHttpClient()
    private val locale = Locale.getDefault()

    /**
     * Convert report issue data and send it to the backend.
     *
     * @param data ReportIssueData instance
     * @return string with state code of the operation
     */
    override suspend fun sendReport(data: ReportIssueData): String {
        val xml = makeXML(data)
        return if (xml.isEmpty()) {
            XML_ERROR
        } else {
            makeHttpPost(xml)
        }
    }

    private suspend fun makeHttpPost(xml: String): String {
        val url = Uri.parse(DEFAULT_URL).buildUpon()
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


        // TODO remove the URL parsing if it's not needed
        val responseBody = kotlin.runCatching { response.body?.string() }
        val responseUrls = Regex(A_PATTERN).findAll(responseBody.toString()).map { it.value }
        if (responseUrls.any()) {
            Timber.d("ReportIssue report sent: ${responseUrls.last()}")
        } else {
            Timber.d("ReportIssue report sent, but no URL received: $responseBody")
            return "Send error"
        }

        return if (response.code == HTTP_OK) {
            ""
        } else {
            "HTTP returned ${response.code}"
        }
    }

    private fun makeXML(data: ReportIssueData): String {
        val writer = StringWriter()
        val serializer: XmlSerializer = Xml.newSerializer()
        // As there are only 3 possible exceptions thrown in this code that are handled the same way, we just
        // suppress the Detekt warning.
        @Suppress("TooGenericExceptionCaught")
        return try {
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
                    serializer.attribute(null, "url", data.url)
                }
                endTag(null, "window")

                startTag(null, "subscriptions")
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
        } catch (e: Exception) {
            Timber.e(e)
            ""
        }
    }

    companion object {
        const val DEFAULT_URL = """https://reports.adblockplus.org/submitReport"""
        const val XML_ERROR = "Error creating XML"
        const val A_PATTERN = """<a.+</a>"""
    }
}
