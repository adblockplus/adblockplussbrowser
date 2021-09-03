package org.adblockplus.adblockplussbrowser.onboarding.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.onboarding.R
import org.adblockplus.adblockplussbrowser.onboarding.databinding.ActivityOnboardingBinding

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityOnboardingBinding>(this, R.layout.activity_onboarding)

        viewModel.finishedEvent.observe(this) { wrapper ->
            wrapper.get()?.let {
                val targetActivity = intent.getSerializableExtra(TARGET_ACTIVITY_PARAM) as Class<*>
                val intent = Intent(this, targetActivity)
                this.startActivity(intent)
                this.finish()
            }
        }
    }

    companion object {
        const val TARGET_ACTIVITY_PARAM = "target_activity"
    }
}