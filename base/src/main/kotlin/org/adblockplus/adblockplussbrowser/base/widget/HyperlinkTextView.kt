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

/*
 A child of a TextView that allows to open links from string resources in a form:
 <a https://example.com>Example</a>
 By default, TextView only understands links in an explicit form, eg
 <a href=https://example.com>https://example.com</a>, this seem to be a security measure
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
}