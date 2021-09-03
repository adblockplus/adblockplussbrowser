package org.adblockplus.adblockplussbrowser.onboarding.ui

import android.content.res.Configuration
import android.net.Uri
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.media.LocalMediaPlayer
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.FragmentOnboardingEnablePageBinding
import kotlin.math.roundToInt

internal class EnablePageFragment :
    DataBindingFragment<FragmentOnboardingEnablePageBinding>(R.layout.fragment_onboarding_enable_page) {

    private val mediaPlayer: LocalMediaPlayer = LocalMediaPlayer()

    override fun onBindView(binding: FragmentOnboardingEnablePageBinding) {
        val headerInclude = binding.onboardingDefaultPageHeaderInclude
        headerInclude.onboardingHeaderTitle1.setText(R.string.onboarding_enable_header_title1)
        headerInclude.onboardingHeaderTitle2.setText(R.string.onboarding_enable_header_title2)
        headerInclude.onboardingHeaderTitle3.setText(R.string.onboarding_welcome_header_title3)
        val guidesWidth = 2 * resources.getDimension(R.dimen.onboarding_guides_margin)
        val availableWidth = resources.displayMetrics.widthPixels - guidesWidth
        val videoView = binding.videoView
        videoView.layoutParams.width = (availableWidth).roundToInt()
        videoView.layoutParams.height = (availableWidth / 2).roundToInt()
        mediaPlayer.create(videoView)
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer.start(getAnimationUri(), true, MEDIA_PLAYER_RESTART_DELAY)
    }

    override fun onStop() {
        mediaPlayer.stop()
        super.onStop()
    }

    override fun onDestroyView() {
        mediaPlayer.destroy()
        super.onDestroyView()
    }

    private fun getAnimationUri(): Uri {
        val resourcePath = "android.resource://" + activity?.packageName + "/"
        return if (isUsingNightModeResources()) {
            Uri.parse(resourcePath + R.raw.activation_animation_dark)
        } else {
            Uri.parse(resourcePath + R.raw.activation_animation_light)
        }
    }

    private fun isUsingNightModeResources(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    companion object {
        const val MEDIA_PLAYER_RESTART_DELAY = 5000L
    }
}