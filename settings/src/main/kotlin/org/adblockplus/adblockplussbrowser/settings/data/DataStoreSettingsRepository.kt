package org.adblockplus.adblockplussbrowser.settings.data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.settings.data.local.SubscriptionsDataSource
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoSettings
import org.adblockplus.adblockplussbrowser.settings.data.proto.toProtoSubscription
import org.adblockplus.adblockplussbrowser.settings.data.proto.toProtoUpdateConfig
import org.adblockplus.adblockplussbrowser.settings.data.proto.toSettings

internal class DataStoreSettingsRepository(
    private val dataStore: DataStore<ProtoSettings>,
    private val subscriptionsDataSource: SubscriptionsDataSource,
) : SettingsRepository {

    override val settings: Flow<Settings> = dataStore.data
        .map { it.toSettings() }

    override suspend fun getEasylistSubscription(): Subscription =
        subscriptionsDataSource.getEasylistSubscription()

    override suspend fun getAcceptableAdsSubscription(): Subscription =
        subscriptionsDataSource.getAcceptableAdsSubscription()

    override suspend fun getDefaultPrimarySubscriptions(): List<Subscription> =
        subscriptionsDataSource.getDefaultPrimarySubscriptions()

    override suspend fun getDefaultOtherSubscriptions(): List<Subscription> =
        subscriptionsDataSource.getDefaultOtherSubscriptions()

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

    override suspend fun setUpdateConfig(updateConfig: UpdateConfig) {
        dataStore.updateData { settings ->
            settings.toBuilder().setUpdateConfig(updateConfig.toProtoUpdateConfig()).build()
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

    override suspend fun setActivePrimarySubscriptions(subscriptions: List<Subscription>) {
        dataStore.updateData { settings ->
            settings.toBuilder().clearActivePrimarySubscriptions()
                .addAllActivePrimarySubscriptions(subscriptions.map { it.toProtoSubscription() })
                .build()
        }
    }

    override suspend fun setActiveOtherSubscriptions(subscriptions: List<Subscription>) {
        dataStore.updateData { settings ->
            settings.toBuilder().clearActiveOtherSubscriptions()
                .addAllActiveOtherSubscriptions(subscriptions.map { it.toProtoSubscription() })
                .build()
        }
    }
}