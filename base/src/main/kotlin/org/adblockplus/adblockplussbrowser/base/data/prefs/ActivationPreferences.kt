package org.adblockplus.adblockplussbrowser.base.data.prefs

import kotlinx.coroutines.flow.Flow

interface ActivationPreferences {
    val lastFilterListRequest: Flow<Long>

    suspend fun updateLastFilterRequest(lastFilterListRequest: Long)
}