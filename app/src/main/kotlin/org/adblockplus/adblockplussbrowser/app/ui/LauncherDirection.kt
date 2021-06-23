package org.adblockplus.adblockplussbrowser.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.adblockplus.adblockplussbrowser.onboarding.ui.OnboardingActivity

internal enum class LauncherDirection(val targetActivity: Class<out Activity>, val extras: Bundle? = null) {
    ONBOARDING(OnboardingActivity::class.java, Bundle().apply {
        putSerializable(OnboardingActivity.TARGET_ACTIVITY_PARAM, MainActivity::class.java)
    }),
    MAIN(MainActivity::class.java)
}

internal fun Activity.navigate(direction: LauncherDirection) {
    val intent = Intent(this, direction.targetActivity)
    direction.extras?.let { intent.putExtras(it) }
    this.startActivity(intent)
    this.finish()
}