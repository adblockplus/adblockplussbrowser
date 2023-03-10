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

package org.adblockplus.adblockplussbrowser.preferences.ui.acceptableads

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.TextAppearanceSpan
import android.view.View
import androidx.core.text.bold
import androidx.core.text.inSpans
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentAcceptableAdsBinding

@AndroidEntryPoint
internal class AcceptableAdsFragment :
    DataBindingFragment<FragmentAcceptableAdsBinding>(R.layout.fragment_acceptable_ads) {
    private val viewModel: AcceptableAdsViewModel by viewModels()

    override fun onBindView(binding: FragmentAcceptableAdsBinding) {
        binding.viewModel = viewModel
        val textAppearance = TextAppearanceSpan(requireActivity(), R.style.TextAppearance_AppCompat_Small)
        val formatted = SpannableStringBuilder()
            .bold { append(getString(R.string.preferences_acceptable_ads_action)) }
            .append("\n")
            .inSpans(textAppearance) {
                append(getString(R.string.acceptable_ads_enabled_line2))
            }
        binding.acceptableAdsSelectionBlock.acceptableAdsEnabled.text = formatted

        binding.acceptableAdsSelectionBlock.acceptableAdsDisabled.text = SpannableStringBuilder()
            .bold { append(getString(R.string.acceptable_ads_disabled)) }

        val acceptableAdsStandardDescription = getString(R.string.acceptable_ads_standard_description)
        val acceptableAdsStandardLink = SpannableString(getString(R.string.acceptable_ads_standard_link))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://acceptableads.com/standard/"))
                startActivity(intent)
            }
        }

        binding.acceptableAdsStandardRedirect.text = SpannableStringBuilder()
            .append(acceptableAdsStandardDescription)
            .append("\n")
            .inSpans(clickableSpan) {
                append(acceptableAdsStandardLink)
            }

        binding.acceptableAdsStandardRedirect.movementMethod = LinkMovementMethod.getInstance()
    }
}
