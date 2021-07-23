package org.adblockplus.adblockplussbrowser.preferences.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import androidx.core.text.HtmlCompat
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentAboutBinding

@AndroidEntryPoint
internal class AboutFragment : DataBindingFragment<FragmentAboutBinding>(R.layout.fragment_about) {

    private val Context.versionName: String?
        get() {
            val packageInfo = this.packageManager?.getPackageInfo(this.packageName, 0)
            return packageInfo?.versionName
        }

    override fun onBindView(binding: FragmentAboutBinding) {
        binding.versionNumber.text = context?.versionName

        binding.aboutPrivacyPolicy.setOnClickListener {
            openUrl(getString(R.string.url_privacy_policy))
        }

        binding.aboutTermsOfUse.setOnClickListener {
            openUrl(getString(R.string.url_terms_of_use))
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