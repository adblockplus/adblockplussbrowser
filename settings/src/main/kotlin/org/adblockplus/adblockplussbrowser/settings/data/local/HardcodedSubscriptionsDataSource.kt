package org.adblockplus.adblockplussbrowser.settings.data.local

import android.content.Context
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.res.localeCompat

internal class HardcodedSubscriptionsDataSource(private val context: Context) : SubscriptionsDataSource {

    private val hardcodedSubscriptions = HardcodedSubscriptions()

    override suspend fun getEasylistSubscription(): Subscription = hardcodedSubscriptions.easylist.toSubscription()

    override suspend fun getAcceptableAdsSubscription(): Subscription =
        hardcodedSubscriptions.acceptableAds.toSubscription()

    override suspend fun getDefaultActiveSubscription(): Subscription {
        val currentLanguage = context.resources.configuration.localeCompat.toLanguageTag()
        val preloadedSubscription =
            hardcodedSubscriptions.defaultPrimarySubscriptions.find { it.languages.contains(currentLanguage) }
                ?: hardcodedSubscriptions.easylist
        return preloadedSubscription.toSubscription()
    }

    override suspend fun getDefaultPrimarySubscriptions(): List<Subscription> =
        hardcodedSubscriptions.defaultPrimarySubscriptions.map { it.toSubscription() }

    override suspend fun getDefaultOtherSubscriptions(): List<Subscription> =
        hardcodedSubscriptions.defaultOtherSubscriptions.map { it.toSubscription() }
}
