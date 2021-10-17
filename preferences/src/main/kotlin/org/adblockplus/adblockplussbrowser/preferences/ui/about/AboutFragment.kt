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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import androidx.core.text.HtmlCompat
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
            openUrl(getString(R.string.url_privacy_policy))
            analyticsProvider.logEvent(AnalyticsEvent.PRIVACY_POLICY_VISITED)
        }

        binding.aboutTermsOfUse.setOnClickListener {
            openUrl(getString(R.string.url_terms_of_use))
            analyticsProvider.logEvent(AnalyticsEvent.TERMS_OF_USE_VISITED)
        }

        binding.aboutImprintText.movementMethod = LinkMovementMethod.getInstance()
        binding.aboutImprintText.text = HtmlCompat.fromHtml(
            getString(R.string.about_imprint_text), HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun openUrl(url: String) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        startActivity(openURL)
    }
}