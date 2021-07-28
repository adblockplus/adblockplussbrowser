package org.adblockplus.adblockplussbrowser.base.data.model

sealed class SubscriptionUpdateStatus {

    data class Progress(val progress: Int) : SubscriptionUpdateStatus()

    object Success : SubscriptionUpdateStatus()

    object Failed : SubscriptionUpdateStatus()

    object None : SubscriptionUpdateStatus()
}