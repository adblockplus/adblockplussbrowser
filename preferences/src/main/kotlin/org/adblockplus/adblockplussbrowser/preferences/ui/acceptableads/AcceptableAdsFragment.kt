package org.adblockplus.adblockplussbrowser.preferences.ui.acceptableads

import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import androidx.core.text.bold
import androidx.core.text.inSpans
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentAcceptableAdsBinding

@AndroidEntryPoint
internal class AcceptableAdsFragment : DataBindingFragment<FragmentAcceptableAdsBinding>(R.layout.fragment_acceptable_ads) {
    private val viewModel: AcceptableAdsViewModel by viewModels()

    override fun onBindView(binding: FragmentAcceptableAdsBinding) {
        binding.viewModel = viewModel
        val textAppearance = TextAppearanceSpan(requireActivity(), R.style.TextAppearance_AppCompat_Small)
        val formatted = SpannableStringBuilder()
            .bold { append(getString(R.string.acceptable_ads_enabled_line1)) }
            .append("\n")
            .inSpans(textAppearance) {
                append(getString(R.string.acceptable_ads_enabled_line2))
            }
        binding.acceptableAdsSelectionBlock.acceptableAdsEnabled.text = formatted

        binding.acceptableAdsSelectionBlock.acceptableAdsDisabled.text = SpannableStringBuilder()
            .bold { append(getString(R.string.acceptable_ads_disabled)) }
    }
}