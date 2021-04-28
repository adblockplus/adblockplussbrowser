package org.adblockplus.adblockplussbrowser.settings.data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.settings.data.local.SubscriptionsLoader
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import java.io.IOException

class DefaultSettingsRepository(
    private val dataStore: DataStore<Settings>,
    private val subscriptionsLoader: SubscriptionsLoader
) : SettingsRepository {

    override fun observeSettings(): Flow<Settings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(Settings.getDefaultInstance())
            } else {
                throw exception
            }
        }

    override fun observeDefaultSubscriptions(): Flow<Subscription> =
        subscriptionsLoader.defaultSubscriptions

    override fun observeDefaultCustomSubscriptions(): Flow<Subscription> {
        TODO("Not yet implemented")
    }

    override suspend fun setAdblockEnabled(enabled: Boolean) {
        dataStore.updateData { settings ->
            settings.toBuilder().setAdblockEnabled(enabled).build()
        }
    }

    override suspend fun setAcceptableAdsEnabled(enabled: Boolean) {
        dataStore.updateData { settings ->
            settings.toBuilder().setAcceptableAdsEnabled(enabled).build()
        }
    }

    override suspend fun setUpdateConfig(updateConfig: Settings.UpdateConfig) {
        dataStore.updateData { settings ->
            settings.toBuilder().setUpdateConfig(updateConfig).build()
        }
    }

    override suspend fun setAllowedDomains(domains: List<String>) {
        dataStore.updateData { settings ->
            settings.toBuilder().clearAllowedDomains().addAllAllowedDomains(domains).build()
        }
    }

    override suspend fun setBlockedDomains(domains: List<String>) {
        dataStore.updateData { settings ->
            settings.toBuilder().clearBlockedDomains().addAllBlockedDomains(domains).build()
        }
    }

    override suspend fun setDefaultActiveSubscriptions(subscriptions: List<Settings.ActiveSubscription>) {
        dataStore.updateData { settings ->
            settings.toBuilder().clearDefaultActiveSubscriptions()
                .addAllDefaultActiveSubscriptions(subscriptions).build()
        }
    }

    override suspend fun setCustomActiveSubscriptions(subscriptions: List<Settings.ActiveSubscription>) {
        dataStore.updateData { settings ->
            settings.toBuilder().clearCustomActiveSubscriptions()
                .addAllCustomActiveSubscriptions(subscriptions).build()
        }
    }
}