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

import android.util.Xml
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData
import org.xmlpull.v1.XmlSerializer
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject


class HttpReportIssueRepository @Inject constructor() : ReportIssueRepository {

    override suspend fun sendReport(data: ReportIssueData): String {

        val xml = makeXML(data)
        if (xml.isEmpty()) {
            return XML_ERROR
        }
        Timber.i(xml)

        val url = makeHttpPost(xml)

        return ""
        TODO("Not yet implemented")
    }

    private fun makeHttpPost(xml: String): String {

        var url = URL(DEFAULT_URL)
        url.query
        val con : HttpURLConnection = URL(DEFAULT_URL).openConnection()
        try {
            con.doOutput = true
            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "application/json");
            con.setChunkedStreamingMode(0)
            val os = BufferedOutputStream(con.outputStream)

        }


    }

    private fun makeXML(data: ReportIssueData): String {
        val writer = StringWriter()
        val serializer: XmlSerializer = Xml.newSerializer()
        try {
            serializer.setOutput(writer)
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.startTag(null, "report")
            serializer.attribute(null, "type", data.type)

            serializer.startTag(null, "requests")
            serializer.endTag(null, "requests")

            serializer.startTag(null, "filters")
            serializer.endTag(null, "filters")

            serializer.startTag(null, "platform")
            serializer.attribute(null, "build", "Build")
            serializer.attribute(null, "name", "ABP")
            serializer.attribute(null, "version", "99") // Development environment
            serializer.endTag(null, "platform")

            serializer.startTag(null, "window")
            serializer.endTag(null, "window")

            serializer.startTag(null, "subscriptions")
            serializer.endTag(null, "subscriptions")

            serializer.startTag(null, "adblock-plus")
            serializer.attribute(null, "version", "Build")
            serializer.attribute(null, "locale", "EN-US")
            serializer.endTag(null, "adblock-plus")

            serializer.startTag(null, "application")
            serializer.attribute(null, "name", "Samsung Internet")
            serializer.attribute(null, "version", "Build")
            serializer.attribute(null, "vendor", "")
            serializer.attribute(null, "userAgent", "")
            serializer.endTag(null, "application")

            serializer.startTag(null, "comment")
            serializer.text(data.comment)
            serializer.endTag(null, "comment")

            serializer.startTag(null, "screenshot")
            serializer.attribute(null, "edited", "false")
            serializer.text(data.screenshot)
            serializer.endTag(null, "screenshot")

            serializer.endTag(null, "report")
            serializer.endDocument()
            serializer.flush()
        } catch (e: Exception) {
            return ""
        }
        return writer.toString()
    }

    companion object {
        const val DEFAULT_URL = """https://reports.adblockplus.org/submitReport"""
        const val XML_ERROR = "Error creating XML"
    }
}
