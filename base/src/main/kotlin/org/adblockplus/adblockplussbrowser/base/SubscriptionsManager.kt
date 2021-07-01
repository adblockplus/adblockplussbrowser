package org.adblockplus.adblockplussbrowser.base

import kotlinx.coroutines.flow.Flow
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription

interface SubscriptionsManager {

    val status: Flow<Status>
    val lastUpdate: Flow<Long>

    fun initialize()

    fun scheduleImmediate(force: Boolean = false)

    suspend fun validateSubscription(subscription: Subscription): Boolean

    suspend fun updateStatus(status: Status)

    sealed class Status {
        data class Progress(val progress: Int) : Status()
        object Failed : Status()
        object Success : Status()
        object None : Status()
    }
}