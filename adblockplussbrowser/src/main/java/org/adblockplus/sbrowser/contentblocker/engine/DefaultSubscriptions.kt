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

package org.adblockplus.sbrowser.contentblocker.engine

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

class DefaultSubscriptions internal constructor (subscriptionInfoList: List<DefaultSubscriptionInfo>) {

    val subscriptions = ArrayList<DefaultSubscriptionInfo>()
    private val urlMap = HashMap<String, DefaultSubscriptionInfo>()

    init {
        subscriptions.addAll(subscriptionInfoList)

        for (sub in subscriptions) {
            val url = sub.url

            if (url.isNotEmpty()) {
                urlMap[url] = sub
            }
        }
    }

    companion object {
        @JvmStatic
        fun fromStream(input: InputStream?): DefaultSubscriptions? {
            try {
                val factory = SAXParserFactory.newInstance()
                factory.isValidating = false
                val parser = factory.newSAXParser()
                val handler = SubscriptionParser()
                parser.parse(input, handler)
                return handler.subscriptions
            } catch (e: ParserConfigurationException) {
                throw IOException("DefaultSubscriptions: " + e.message)
            } catch (e: SAXException) {
                throw IOException("DefaultSubscriptions: " + e.message)
            }

        }
    }

    fun getForUrl(url: String?) = urlMap[url]

    fun getForUrl(url: URL?): DefaultSubscriptionInfo? =
            if (url != null) getForUrl(url.toString()) else null
}

private class SubscriptionParser : DefaultHandler() {

    companion object {
        private const val KEY_SUBSCRIPTION = "subscription"
    }

    val subscriptionInfoList = arrayListOf<DefaultSubscriptionInfo>()
    lateinit var subscriptions: DefaultSubscriptions
    lateinit var subscription: DefaultSubscriptionInfo

    override fun startElement(uri: String?, localName: String?, qualifiedName: String?,
                              attributes: Attributes?) {
        if (KEY_SUBSCRIPTION == qualifiedName) {
            subscription = DefaultSubscriptionInfo()
            for (i in 0 until attributes!!.length) {
                subscription.attributes[attributes.getQName(i)] = attributes.getValue(i)
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qualifiedName: String?) {
        if (KEY_SUBSCRIPTION == qualifiedName) {
            subscriptionInfoList.add(subscription)
        }
    }

    override fun endDocument() {
        super.endDocument()
        subscriptions = DefaultSubscriptions(subscriptionInfoList)
    }
}