package org.adblockplus.adblockplussbrowser.settings.data.local

import android.content.Context
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.res.localeCompat
import java.util.*

internal class HardcodedSubscriptionsDataSource(private val context: Context) : SubscriptionsDataSource {

    private val hardcodedSubscriptions = HardcodedSubscriptions()

    override suspend fun getEasylistSubscription(): Subscription = hardcodedSubscriptions.easylist.toSubscription()

    override suspend fun getAcceptableAdsSubscription(): Subscription =
        hardcodedSubscriptions.acceptableAds.toSubscription()

    override suspend fun getDefaultActiveSubscription(): Subscription {
        val currentLanguage = context.resources.configuration.localeCompat.language
        val preloadedSubscription =
            hardcodedSubscriptions.defaultPrimarySubscriptions.find { subscription ->
                // We create a Locale instance to handle language codes that have changed (e.g. iw -> he)
                subscription.languages.any { currentLanguage == Locale(it).language }
            }
                ?: hardcodedSubscriptions.easylist
        return preloadedSubscription.toSubscription()
    }

    override suspend fun getDefaultPrimarySubscriptions(): List<Subscription> =
        hardcodedSubscriptions.defaultPrimarySubscriptions.map { it.toSubscription() }

    override suspend fun getDefaultOtherSubscriptions(): List<Subscription> =
        hardcodedSubscriptions.defaultOtherSubscriptions.map { it.toSubscription() }
}