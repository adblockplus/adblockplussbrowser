package org.adblockplus.adblockplussbrowser.settings.data

import kotlinx.coroutines.flow.Flow
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig

interface SettingsRepository {

    fun observeSettings(): Flow<Settings>

    fun observeDefaultAdsSubscriptions(): Flow<List<Subscription>>

    fun observeDefaultOtherSubscriptions(): Flow<List<Subscription>>

    suspend fun setAdblockEnabled(enabled: Boolean)

    suspend fun setAcceptableAdsEnabled(enabled: Boolean)

    suspend fun setUpdateConfig(updateConfig: UpdateConfig)

    suspend fun setAllowedDomains(domains: List<String>)

    suspend fun setBlockedDomains(domains: List<String>)

    suspend fun setActiveAdsSubscriptions(subscriptions: List<Subscription>)

    suspend fun setActiveOtherSubscriptions(subscriptions: List<Subscription>)
}