package org.adblockplus.adblockplussbrowser.base.data.prefs

import kotlinx.coroutines.flow.Flow

interface ActivationPreferences {
    val activated: Flow<Boolean>
    val lastFilterListRequest: Flow<Long>

    suspend fun activate()

    suspend fun updateLastFilterRequest(lastFilterListRequest: Long)
}