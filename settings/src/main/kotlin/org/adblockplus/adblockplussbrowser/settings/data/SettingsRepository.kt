package org.adblockplus.adblockplussbrowser.settings.data

import kotlinx.coroutines.flow.Flow
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig

interface SettingsRepository {

    val settings: Flow<Settings>

    suspend fun getEasylistSubscription(): Subscription

    suspend fun getAcceptableAdsSubscription(): Subscription

    suspend fun getDefaultPrimarySubscriptions(): List<Subscription>

    suspend fun getDefaultOtherSubscriptions(): List<Subscription>

    suspend fun setAdblockEnabled(enabled: Boolean)

    suspend fun setAcceptableAdsEnabled(enabled: Boolean)

    suspend fun setUpdateConfig(updateConfig: UpdateConfig)

    suspend fun addAllowedDomain(domain: String)

    suspend fun removeAllowedDomain(domain: String)

    suspend fun setAllowedDomains(domains: List<String>)

    suspend fun addBlockedDomain(domain: String)

    suspend fun removeBlockedDomain(domain: String)

    suspend fun setBlockedDomains(domains: List<String>)

    suspend fun addActivePrimarySubscription(subscription: Subscription)

    suspend fun removeActivePrimarySubscription(subscription: Subscription)

    suspend fun setActivePrimarySubscriptions(subscriptions: List<Subscription>)

    suspend fun addActiveOtherSubscription(subscription: Subscription)

    suspend fun removeActiveOtherSubscription(subscription: Subscription)

    suspend fun setActiveOtherSubscriptions(subscriptions: List<Subscription>)

    suspend fun updatePrimarySubscriptionLastUpdate(url: String, lastUpdate: Long)

    suspend fun updateOtherSubscriptionLastUpdate(url: String, lastUpdate: Long)

    suspend fun updatePrimarySubscriptionsLastUpdate(subscriptions: List<Subscription>)

    suspend fun updateOtherSubscriptionsLastUpdate(subscriptions: List<Subscription>)

    suspend fun setAnalyticsEnabled(enabled: Boolean)

    suspend fun getAdditionalTrackingSubscription(): Subscription

    suspend fun getSocialMediaTrackingSubscription(): Subscription

    suspend fun markLanguagesOnboardingCompleted()

}