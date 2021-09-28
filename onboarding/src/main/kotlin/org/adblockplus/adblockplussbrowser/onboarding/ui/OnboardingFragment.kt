package org.adblockplus.adblockplussbrowser.onboarding.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.FragmentOnboardingBinding
import timber.log.Timber

@AndroidEntryPoint
internal class OnboardingFragment : DataBindingFragment<FragmentOnboardingBinding>(R.layout.fragment_onboarding) {

    val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!viewModel.previousPage()) {
                requireActivity().finish()
            }
        }
    }

    override fun onBindView(binding: FragmentOnboardingBinding) {
        binding.viewModel = viewModel

        binding.onboardingPager.adapter = OnboardingPagerAdapter(this)

        binding.onboardingButton.setOnClickListener {
            viewModel.nextPage()
        }

        binding.openSiButton.setOnClickListener {
            viewModel.completeOnboarding()
            try {
                val sBrowserIntentLauncher = context?.packageManager?.getLaunchIntentForPackage(SBROWSER_APP_ID)
                if (sBrowserIntentLauncher != null) {
                    // Samsung browser need to be in the background in order to succeed with ACTION_OPEN_SETTINGS
                    startActivity(sBrowserIntentLauncher)
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(ACTION_OPEN_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }, START_SETTINGS_DELAY)
                }
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
            }
        }
    }

    companion object {
        private const val SBROWSER_APP_ID = "com.sec.android.app.sbrowser"
        private const val START_SETTINGS_DELAY = 100L
        private const val ACTION_OPEN_SETTINGS =
            "com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING"
    }
}