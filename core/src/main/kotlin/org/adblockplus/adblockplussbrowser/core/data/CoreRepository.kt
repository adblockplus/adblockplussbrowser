package org.adblockplus.adblockplussbrowser.core.data

import kotlinx.coroutines.flow.Flow
import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState

internal interface CoreRepository {
    val data: Flow<CoreData>
    var subscriptionsPath: String?

    suspend fun getDataSync(): CoreData

    suspend fun setConfigured()

    suspend fun updateDownloadedSubscriptions(subscriptions: List<DownloadedSubscription>,
                                              updateTimestamp: Boolean)

    suspend fun updateLastUpdated(lastUpdated: Long)

    suspend fun updateLastVersion(lastUpdated: Long)

    suspend fun updateSavedState(savedState: SavedState)

    companion object {
        const val KEY_CURRENT_SUBSCRIPTIONS_FILE = "KEY_CURRENT_SUBSCRIPTIONS_FILE"
    }
}