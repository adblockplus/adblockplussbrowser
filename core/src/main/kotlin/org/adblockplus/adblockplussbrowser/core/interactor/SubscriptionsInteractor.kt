package org.adblockplus.adblockplussbrowser.core.interactor

import kotlinx.coroutines.flow.combine
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository

class SubscriptionsInteractor internal constructor(
    coreRepository: CoreRepository,
    settingsRepository: SettingsRepository
){
    val otherSubscriptions = settingsRepository.settings.combine(coreRepository.data) { settings, coreData ->
        val activeSubscriptions = settings.activeOtherSubscriptions
        val defaultSubscriptions = settingsRepository.getDefaultOtherSubscriptions().map { subscription ->
            val lastUpdated = coreData.downloadedSubscription.lastUpdated(subscription)
            val active = activeSubscriptions.any { it.url == subscription.url }
            SubscriptionInfo(SubscriptionType.OTHER, subscription, active, lastUpdated)
        }
        val customSubscriptions = activeSubscriptions.filter { subscription ->
            defaultSubscriptions.none { it.subscription.url == subscription.url }
        }.map { subscription ->
            val lastUpdated = coreData.downloadedSubscription.lastUpdated(subscription)
            val active = activeSubscriptions.any { it.url == subscription.url }
            SubscriptionInfo(SubscriptionType.CUSTOM, subscription, active, lastUpdated)
        }

        defaultSubscriptions + customSubscriptions
    }

    enum class SubscriptionType {
        PRIMARY, OTHER, CUSTOM
    }

    data class SubscriptionInfo(
        val type: SubscriptionType,
        val subscription: Subscription,
        val active: Boolean,
        val lastUpdated: Long
    )

    internal fun List<DownloadedSubscription>.lastUpdated(subscription: Subscription): Long =
        this.firstOrNull { subscription.url == it.url }?.lastUpdated ?: 0
}