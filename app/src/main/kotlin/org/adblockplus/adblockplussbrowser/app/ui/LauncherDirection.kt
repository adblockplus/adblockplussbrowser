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

package org.adblockplus.adblockplussbrowser.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.adblockplus.adblockplussbrowser.onboarding.ui.OnboardingActivity

internal enum class LauncherDirection(val targetActivity: Class<out Activity>, val extras: Bundle? = null) {
    ONBOARDING(OnboardingActivity::class.java, Bundle().apply {
        putSerializable(OnboardingActivity.TARGET_ACTIVITY_PARAM, MainActivity::class.java)
    }),
    ONBOARDING_LAST_STEP(OnboardingActivity::class.java, Bundle().apply {
        putSerializable(OnboardingActivity.TARGET_ACTIVITY_PARAM, MainActivity::class.java)
        putBoolean(OnboardingActivity.SHOW_LAST_ONBOARDING_STEP, true)
    }),
    MAIN(MainActivity::class.java)
}

internal fun Activity.navigate(direction: LauncherDirection, showLastStepOnboardingStep: Boolean = false) {
    val intent = Intent(this, direction.targetActivity)
    direction.extras?.let { intent.putExtras(it) }
    if (showLastStepOnboardingStep) {
        intent.putExtra(OnboardingActivity.SHOW_LAST_ONBOARDING_STEP, showLastStepOnboardingStep)
    }
    this.startActivity(intent)
    this.finish()
}

