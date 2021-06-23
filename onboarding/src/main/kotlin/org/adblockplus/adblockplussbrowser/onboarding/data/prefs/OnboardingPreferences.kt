package org.adblockplus.adblockplussbrowser.onboarding.data.prefs

import kotlinx.coroutines.flow.Flow

interface OnboardingPreferences {

    val onboardingCompleted: Flow<Boolean>

    suspend fun completeOnboarding()
}