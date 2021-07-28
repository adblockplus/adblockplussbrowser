package org.adblockplus.adblockplussbrowser.base

import kotlinx.coroutines.flow.Flow
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus

interface SubscriptionsManager {

    val status: Flow<SubscriptionUpdateStatus>
    val lastUpdate: Flow<Long>

    fun initialize()

    fun scheduleImmediate(force: Boolean = false)

    suspend fun validateSubscription(subscription: Subscription): Boolean

    suspend fun updateStatus(status: SubscriptionUpdateStatus)
}