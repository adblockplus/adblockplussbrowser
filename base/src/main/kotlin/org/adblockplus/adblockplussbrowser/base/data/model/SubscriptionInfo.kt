package org.adblockplus.adblockplussbrowser.base.data.model

data class SubscriptionInfo(
    val type: SubscriptionType,
    val subscription: Subscription,
    val active: Boolean,
    val lastUpdated: Long
)

enum class SubscriptionType {
    PRIMARY, OTHER, CUSTOM
}