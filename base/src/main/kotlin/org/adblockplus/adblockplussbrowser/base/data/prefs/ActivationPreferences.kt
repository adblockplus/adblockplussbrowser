package org.adblockplus.adblockplussbrowser.base.data.prefs

import kotlinx.coroutines.flow.Flow

interface ActivationPreferences {
    val lastFilterListRequest: Flow<Long>

    suspend fun updateLastFilterRequest(lastFilterListRequest: Long)

    companion object {
        // 30 days to expire filter request 30*24*60*60*1000 = 2592000000
        private const val FILTER_REQUEST_EXPIRE_TIME_SPAN = 2592_000_000
        fun isFilterRequestExpired(lastFilterRequest: Long) =
            System.currentTimeMillis() - lastFilterRequest > FILTER_REQUEST_EXPIRE_TIME_SPAN
    }
}