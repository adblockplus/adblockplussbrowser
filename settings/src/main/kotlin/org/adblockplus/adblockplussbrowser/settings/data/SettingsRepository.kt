package org.adblockplus.adblockplussbrowser.settings.data

import kotlinx.coroutines.flow.Flow
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings

interface SettingsRepository {

    fun observeSettings(): Flow<Settings>

    fun observeDefaultSubscriptions(): Flow<Subscription>

    fun observeDefaultCustomSubscriptions(): Flow<Subscription>

    suspend fun setAdblockEnabled(enabled: Boolean)

    suspend fun setAcceptableAdsEnabled(enabled: Boolean)

    suspend fun setUpdateConfig(updateConfig: Settings.UpdateConfig)

    suspend fun setAllowedDomains(domains: List<String>)

    suspend fun setBlockedDomains(domains: List<String>)

    suspend fun setDefaultActiveSubscriptions(subscriptions: List<Settings.ActiveSubscription>)

    suspend fun setCustomActiveSubscriptions(subscriptions: List<Settings.ActiveSubscription>)
}