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
package org.adblockplus.adblockplussbrowser.base.widget

import android.content.Context
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat

/**
 * <h4>A child of a TextView that allows to open links from string resources in a form:
 * `<a https://example.com>Example</a>`</h4>
 *
 * By default, TextView only understands links in an explicit form, eg
 * `<a href=https://example.com>https://example.com</a>`, this seem to be a security measure
 *
 * ATTENTION: when you'd like to use <p> or <br> tags, enclose your HTML into `<![CDATA[ .. ]]>`
 * <br/>For example `<string><![CDATA[ <p>Paragraph</p><br/>Other <b>text</b> ]]></string>`
 */
class HyperlinkTextView(context: Context, attrs: AttributeSet?) :
// class has reduced number of constructor because it supposed to be used only from XML
    AppCompatTextView(context, attrs) {
    /**
     * Set default movement method to [LinkMovementMethod]
     * @return Link movement method as the default movement method
     */
    override fun getDefaultMovementMethod(): MovementMethod {
        return LinkMovementMethod.getInstance()
    }

    override fun onFinishInflate() {
        /*
        A grain of heuristics here:
        TextView supports some html tags out of the box, but not all of them. It looks like it uses
        [HtmlCompat.FROM_HTML_MODE_COMPACT] by default. So we need to call [HtmlCompat.fromHtml]
        when not-supported tags are detected, eg: when text contains <p> or <br>

        There is also no way of detecting if xml from resources contains <![CDATA[ .. ]] thus
        tags detecting is implemented
         */
        if (doesContainExtendedHtml(text.toString()))
            text = HtmlCompat.fromHtml(text.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        super.onFinishInflate()
    }

    companion object {
        // now only has <p> and <br> tags, more could be added
        // not full tag is used because of variations: <p> or <p/>
        private val HTML_EXTENDED_TAGS = Regex("(<p|<br).")

        /**
         * Checks whether the string contains html tags, that TextView does support
         * when inflating from <string> tag and that require [HtmlCompat.fromHtml] to be called
         * @param text a string to be tested
         * @return true if the string contains extended html
         */
        fun doesContainExtendedHtml(text: String) = text.contains(HTML_EXTENDED_TAGS)
    }
}

