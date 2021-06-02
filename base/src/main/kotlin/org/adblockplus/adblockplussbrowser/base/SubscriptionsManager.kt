package org.adblockplus.adblockplussbrowser.base

import androidx.lifecycle.LiveData
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription

interface SubscriptionsManager {

    val status: LiveData<Status>

    fun initialize()

    fun scheduleImmediate(force: Boolean = false)

    fun updateStatus(status: Status)

    suspend fun validateSubscription(subscription: Subscription): Boolean

    sealed class Status {
        data class Downloading(val current: Int) : Status()
        object Failed : Status()
        object Success : Status()
    }
}