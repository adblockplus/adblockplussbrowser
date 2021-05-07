package org.adblockplus.adblockplussbrowser.core.data

import kotlinx.coroutines.flow.Flow
import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription

internal interface CoreRepository {
    val data: Flow<CoreData>
    var subscriptionsPath: String?

    suspend fun getDataSync(): CoreData

    suspend fun setInitialized()

    suspend fun updateDownloadedSubscriptions(subscriptions: List<DownloadedSubscription>)

    companion object {
        const val KEY_CURRENT_SUBSCRIPTIONS_FILE = "KEY_CURRENT_SUBSCRIPTIONS_FILE"
    }
}