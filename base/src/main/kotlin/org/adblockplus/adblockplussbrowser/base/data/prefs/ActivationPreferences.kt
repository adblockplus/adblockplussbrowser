package org.adblockplus.adblockplussbrowser.base.data.prefs

import kotlinx.coroutines.flow.Flow

interface ActivationPreferences {
    val activated: Flow<Boolean>

    suspend fun activate()
}