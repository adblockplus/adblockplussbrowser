package org.adblockplus.adblockplussbrowser.onboarding.ui

import android.os.Bundle
import android.widget.TextView
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.view.layoutInflater
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.FragmentOnboardingDefaultPageBinding

internal class DefaultPageFragment :
    DataBindingFragment<FragmentOnboardingDefaultPageBinding>(R.layout.fragment_onboarding_default_page) {

    override fun onBindView(binding: FragmentOnboardingDefaultPageBinding) {
        val arguments = requireNotNull(arguments)

        val headerInclude = binding.onboardingDefaultPageHeaderInclude
        bindTextViewStringRes(headerInclude.onboardingHeaderTitle1, arguments, TITLE1_RES_PARAM)
        bindTextViewStringRes(headerInclude.onboardingHeaderTitle2, arguments, TITLE2_RES_PARAM)
        bindTextViewStringRes(headerInclude.onboardingHeaderTitle3, arguments, TITLE3_RES_PARAM)

        val contentLayoutRes = requireNotNull(arguments.getInt(CONTENT_RES_PARAM))
        binding.root.layoutInflater.inflate(contentLayoutRes, binding.onboardingDefaultPageContainer, true)
    }

    private fun bindTextViewStringRes(textView: TextView, arguments: Bundle, argumentId: String) {
        val stringRes = requireNotNull(arguments.getInt(argumentId))
        textView.setText(stringRes)
    }

    companion object {

        private const val TITLE1_RES_PARAM = "title1_res"
        private const val TITLE2_RES_PARAM = "title2_res"
        private const val TITLE3_RES_PARAM = "title3_res"
        private const val CONTENT_RES_PARAM = "content_res"

        fun newInstance(info: PageInfo.Default): DefaultPageFragment {
            val bundle = Bundle().apply {
                putInt(TITLE1_RES_PARAM, info.title1StringRes)
                putInt(TITLE2_RES_PARAM, info.title2StringRes)
                putInt(TITLE3_RES_PARAM, info.title3StringRes)
                putInt(CONTENT_RES_PARAM, info.contentLayoutRes)
            }
            return DefaultPageFragment().apply { arguments = bundle }
        }
    }
}