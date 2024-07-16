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

import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import androidx.core.text.bold
import androidx.core.text.inSpans
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.databinding.bindAAStandardRedirect
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.i18n.R as i18nR
import org.adblockplus.adblockplussbrowser.base.R as baseR
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentAcceptableAdsBinding

@AndroidEntryPoint
internal class AcceptableAdsFragment :
    DataBindingFragment<FragmentAcceptableAdsBinding>(R.layout.fragment_acceptable_ads) {
    private val viewModel: AcceptableAdsViewModel by viewModels()

    override fun onBindView(binding: FragmentAcceptableAdsBinding) {
        binding.viewModel = viewModel
        val textAppearance =
            TextAppearanceSpan(requireActivity(), android.R.style.TextAppearance_Small)
        val formatted = SpannableStringBuilder()
            .bold { append(getString(i18nR.string.preferences_acceptable_ads_action)) }
            .append("\n")
            .inSpans(textAppearance) {
                append(getString(i18nR.string.acceptable_ads_enabled_line2))
            }
        binding.acceptableAdsSelectionBlock.acceptableAdsEnabled.text = formatted

        binding.acceptableAdsSelectionBlock.acceptableAdsDisabled.text = SpannableStringBuilder()
            .bold { append(getString(i18nR.string.acceptable_ads_disabled)) }

        bindAAStandardRedirect(
            binding.acceptableAdsStandardRedirect.findViewById(
                baseR.id.acceptable_ads_standard_redirect_text
            )
        )
    }
}
