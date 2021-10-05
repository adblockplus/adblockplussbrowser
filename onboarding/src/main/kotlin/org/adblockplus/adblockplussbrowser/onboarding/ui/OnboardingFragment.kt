package org.adblockplus.adblockplussbrowser.onboarding.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.FragmentOnboardingBinding
import timber.log.Timber
import java.util.concurrent.TimeUnit

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
                context?.packageManager?.getLaunchIntentForPackage(SBROWSER_APP_ID).let {
                    // Samsung browser needs to be in the background in order to succeed with ACTION_OPEN_SETTINGS
                    startActivity(it)
                    val openSISettingsRequest = OneTimeWorkRequest.Builder(OpenSISettingsWorker::class.java)
                        .setInitialDelay(START_SETTINGS_DELAY, TimeUnit.MILLISECONDS)
                        .build()
                    WorkManager.getInstance(requireContext()).enqueue(openSISettingsRequest)
                }
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
            }
        }
    }

    class OpenSISettingsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
        override fun doWork(): Result {
            val intent = Intent(ACTION_OPEN_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)
            return Result.success()
        }
    }

    companion object {
        private const val SBROWSER_APP_ID = "com.sec.android.app.sbrowser"
        private const val START_SETTINGS_DELAY = 50L
        private const val ACTION_OPEN_SETTINGS =
            "com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING"
    }
}