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

package org.adblockplus.adblockplussbrowser.base.databinding

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.text.inSpans
import org.adblockplus.adblockplussbrowser.base.R

fun bindAAStandardRedirect(textView: TextView) {
    // AA Standard Link
    val acceptableAdsStandardLink = "https://acceptableads.com/standard/"
    // Prepare String
    val context = textView.context
    val acceptableAdsStandardDescription =
        context.getString(R.string.acceptable_ads_standard_description)
    val acceptableAdsStandardLinkSpan =
        SpannableString(context.getString(R.string.acceptable_ads_standard_link))
    // Redirect onClick
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(acceptableAdsStandardLink)
                )
            context.startActivity(intent)
        }
    }
    // Set text
    textView.text = SpannableStringBuilder()
        .append(acceptableAdsStandardDescription)
        .append("\n")
        .inSpans(clickableSpan) {
            append(acceptableAdsStandardLinkSpan)
        }
    textView.movementMethod = LinkMovementMethod.getInstance()
}
