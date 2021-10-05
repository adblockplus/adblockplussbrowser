package org.adblockplus.adblockplussbrowser.onboarding.ui

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
import org.adblockplus.adblockplussbrowser.base.SamsungInternetConstants.Companion.ACTION_OPEN_SETTINGS
import org.adblockplus.adblockplussbrowser.base.SamsungInternetConstants.Companion.SBROWSER_APP_ID
import org.adblockplus.adblockplussbrowser.base.SamsungInternetConstants.Companion.SBROWSER_APP_ID_BETA
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.FragmentOnboardingBinding
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
            var samsungInternetIntentLauncher = getLauncher(SBROWSER_APP_ID)
            if (samsungInternetIntentLauncher == null) {
                samsungInternetIntentLauncher = getLauncher(SBROWSER_APP_ID_BETA)
            }

            samsungInternetIntentLauncher?.let {
                // Samsung browser needs to be in the background in order to succeed with ACTION_OPEN_SETTINGS
                startActivity(it)
                val openSISettingsRequest = OneTimeWorkRequest.Builder(OpenSISettingsWorker::class.java)
                    .setInitialDelay(START_SETTINGS_DELAY, TimeUnit.MILLISECONDS)
                    .build()
                WorkManager.getInstance(requireContext()).enqueue(openSISettingsRequest)
            }
        }
    }

    private fun getLauncher(id: String) : Intent? {
        return context?.packageManager?.getLaunchIntentForPackage(id)
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
        private const val START_SETTINGS_DELAY = 500L
    }
}