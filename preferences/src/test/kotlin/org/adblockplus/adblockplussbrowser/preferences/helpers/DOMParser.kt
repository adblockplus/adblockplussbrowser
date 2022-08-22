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

package org.adblockplus.adblockplussbrowser.preferences.helpers

import java.io.StringReader
import java.lang.IllegalArgumentException
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource

/**
 * This object can parse a string into a Document
 */
object DOMParser {

    fun parse(input: String): Document {
        val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder = factory.newDocumentBuilder()
        return builder.parse(InputSource(StringReader(input)))
    }
}

fun Document.getAttribute(attribute: String, tag: String = ""): String {
    return if (tag.isEmpty()) {
        // If no tag is provided then get attribute from root node
        this.documentElement.getAttribute(attribute)
    } else {
        val nodeList = this.documentElement.getElementsByTagName(tag)
        if (nodeList.length > 0) {
            // In our case there is only one element per tag so we can fetch the first Item
            (nodeList.item(0) as Element).getAttribute(attribute)
        } else {
            throw IllegalArgumentException("No nodes with given tag")
        }
    }
}

fun Document.getTagContent(tag: String): String {
    return this.getElementsByTagName(tag).item(0).textContent
}
