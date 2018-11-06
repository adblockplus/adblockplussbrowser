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

class DefaultSubscriptions {

    private val defaultSubscriptions = ArrayList<DefaultSubscriptionInfo>()
    private val urlMap = HashMap<String, DefaultSubscriptionInfo>()

    companion object {
        fun fromStream(input: InputStream?): DefaultSubscriptions {
            try {
                val factory = SAXParserFactory.newInstance()
                factory.isValidating = false
                val parser = factory.newSAXParser()
                val handler = SubscriptionParser()
                parser.parse(input, handler)
                return handler.subscriptions.initialize()
            } catch (e: ParserConfigurationException) {
                throw IOException("DefaultSubscriptions: " + e.message)
            } catch (e: SAXException) {
                throw IOException("DefaultSubscriptions: " + e.message)
            }

        }
    }

    fun initialize(): DefaultSubscriptions {
        for (sub in defaultSubscriptions) {
            val url = sub.url

            if (url.isNotEmpty()) {
                urlMap[url] = sub
            }
        }
        return this
    }

    fun getForUrl(url: String?): DefaultSubscriptionInfo? {
        return urlMap[url]
    }

    fun getForUrl(url: URL?): DefaultSubscriptionInfo? {
        return if (url != null) getForUrl(url.toString()) else null
    }

    fun get(): List<DefaultSubscriptionInfo> {
        return defaultSubscriptions
    }

    class SubscriptionParser : DefaultHandler() {

        private val KEY_SUBSCRIPTION = "subscription"

        val subscriptions = DefaultSubscriptions()
        var subscription: DefaultSubscriptionInfo? = null

        override fun startElement(uri: String?, localName: String?, qualifiedName: String?,
                                  attributes: Attributes?) {
            super.startElement(uri, localName, qualifiedName, attributes)

            if (KEY_SUBSCRIPTION == qualifiedName) {
                subscription = DefaultSubscriptionInfo()
                for (i in 0 until attributes!!.length) {
                    subscription!!.attributes[attributes.getQName(i)] = attributes.getValue(i)
                }
            }
        }

        override fun endElement(uri: String?, localName: String?, qualifiedName: String?) {
            super.endElement(uri, localName, qualifiedName)

            if (KEY_SUBSCRIPTION == qualifiedName) {
                subscriptions.defaultSubscriptions.add(subscription!!)
                subscription = null
            }
        }
    }
}