/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    override suspend fun getAdditionalTrackingSubscription(): Subscription =
        hardcodedSubscriptions.additionalTracking.toSubscription()

    override suspend fun getSocialMediaTrackingSubscription(): Subscription =
        hardcodedSubscriptions.socialMediaTracking.toSubscription()
}