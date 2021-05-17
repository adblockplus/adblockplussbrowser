package org.adblockplus.adblockplussbrowser.settings.data.local

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription

internal interface SubscriptionsDataSource {

    suspend fun getEasylistSubscription(): Subscription

    suspend fun getAcceptableAdsSubscription(): Subscription

    suspend fun getDefaultActiveSubscription(): Subscription

    suspend fun getDefaultPrimarySubscriptions(): List<Subscription>

    suspend fun getDefaultOtherSubscriptions(): List<Subscription>
}