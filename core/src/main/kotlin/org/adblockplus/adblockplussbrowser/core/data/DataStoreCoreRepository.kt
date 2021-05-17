package org.adblockplus.adblockplussbrowser.core.data

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.proto.ProtoCoreData
import org.adblockplus.adblockplussbrowser.core.data.proto.toCoreData
import org.adblockplus.adblockplussbrowser.core.data.proto.toProtoDownloadedSubscription

internal class DataStoreCoreRepository(
    private val dataStore: DataStore<ProtoCoreData>,
    private val sharedPrefs: SharedPreferences
) : CoreRepository {
    override val data: Flow<CoreData> = dataStore.data
        .map { it.toCoreData() }

    override var subscriptionsPath: String?
        get() = sharedPrefs.getString(CoreRepository.KEY_CURRENT_SUBSCRIPTIONS_FILE, null)
        set(value) {
            sharedPrefs.edit().putString(CoreRepository.KEY_CURRENT_SUBSCRIPTIONS_FILE, value).apply()
        }

    override suspend fun getDataSync(): CoreData = data.take(1).single()

    override suspend fun setInitialized() {
        dataStore.updateData { data ->
            data.toBuilder().setConfigured(true).build()
        }
    }

    override suspend fun updateDownloadedSubscriptions(subscriptions: List<DownloadedSubscription>) {
        dataStore.updateData { data ->
            data.toBuilder()
                .clearDownloadedSubscriptions()
                .addAllDownloadedSubscriptions(subscriptions.map { it.toProtoDownloadedSubscription() })
                .setLastUpdate(System.currentTimeMillis())
                .build()
        }
    }

    override suspend fun updateLastUpdated(lastUpdated: Long) {
        dataStore.updateData { data ->
            data.toBuilder().setLastUpdate(lastUpdated).build()
        }
    }
}