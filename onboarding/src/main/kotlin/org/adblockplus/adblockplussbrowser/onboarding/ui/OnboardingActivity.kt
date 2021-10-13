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

        if (intent.getBooleanExtra(SHOW_LAST_ONBOARDING_STEP, false)) {
            viewModel.selectPage(LAST_ONBOARDING_STEP_INDEX)
        }

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
        const val SHOW_LAST_ONBOARDING_STEP = "show_last_onboarding_step"
        const val LAST_ONBOARDING_STEP_INDEX = 2
    }
}