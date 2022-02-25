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

package org.adblockplus.adblockplussbrowser.preferences.ui.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Spanned
import android.text.style.URLSpan
import android.widget.Toast
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentAboutBinding
import javax.inject.Inject

@AndroidEntryPoint
internal class AboutFragment : DataBindingFragment<FragmentAboutBinding>(R.layout.fragment_about) {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    private val Context.versionName: String?
        get() {
            val packageInfo = this.packageManager?.getPackageInfo(this.packageName, 0)
            return packageInfo?.versionName
        }

    override fun onBindView(binding: FragmentAboutBinding) {

        analyticsProvider.logEvent(AnalyticsEvent.ABOUT_VISITED)

        binding.openSourceLicenses.setOnClickListener {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source_licenses))
            startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
            analyticsProvider.logEvent(AnalyticsEvent.OPEN_SOURCE_LICENSES_VISITED)
        }

        binding.versionNumber.text = context?.versionName

        binding.aboutPrivacyPolicy.setOnClickListener {
            extractUrlAndRedirect(binding.aboutPrivacyPolicyHyperlink.text)
        }

        binding.aboutTermsOfUse.setOnClickListener {
            extractUrlAndRedirect(binding.aboutTermsOfUseHyperlink.text)
        }

    }

    private fun extractUrlAndRedirect(text: CharSequence) {
        try {
            val url = (text as Spanned).getSpans(0, text.length, URLSpan::class.java)[0].url
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(context, getString(R.string.no_browser_found), Toast.LENGTH_LONG).show()
        }
    }
}