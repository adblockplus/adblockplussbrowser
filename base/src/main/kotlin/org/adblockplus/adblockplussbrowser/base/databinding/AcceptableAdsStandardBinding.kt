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
    val context = textView.context
    val acceptableAdsStandardDescription =
        context.getString(R.string.acceptable_ads_standard_description)
    val acceptableAdsStandardLink =
        SpannableString(context.getString(R.string.acceptable_ads_standard_link))
    // Redirect onClick
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(AAStandardConstants.ACCEPTABLE_ADS_STANDARD_LINK))
            context.startActivity(intent)
        }
    }
    // Set text
    textView.text = SpannableStringBuilder()
        .append(acceptableAdsStandardDescription)
        .append("\n")
        .inSpans(clickableSpan) {
            append(acceptableAdsStandardLink)
        }
    textView.movementMethod = LinkMovementMethod.getInstance()
}

object AAStandardConstants{
    const val ACCEPTABLE_ADS_STANDARD_LINK = "https://acceptableads.com/standard/"
}