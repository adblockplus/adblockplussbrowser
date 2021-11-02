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

package org.adblockplus.adblockplussbrowser.core.helpers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig

class Fakes {

    internal class FakeCoreRepository : CoreRepository {

        val INITIAL_TIMESTAMP = -1L
        val INITIAL_COUNT = -1
        var lastUserCountingResponse = INITIAL_TIMESTAMP
        var userCountingCount = INITIAL_COUNT

        val AA_URL : String
        val EASYLIST_URL : String

        private val serverUrl : String
        private val coreData : CoreData
        constructor(serverUrl : String) {
            this.serverUrl = serverUrl
            AA_URL = "$serverUrl/easylist.txt"
            EASYLIST_URL = "$serverUrl/exceptionrules.txt"
            coreData = CoreData(
                true,
                0L,
                SavedState(true, listOf(""), listOf(""), listOf(""), listOf("")),
                listOf(),
                0L,
                0);
        }

        override val data: Flow<CoreData>
            get() = flow {
                emit(coreData)
            }

        override var subscriptionsPath: String?
            get() = ""
            @Suppress("UNUSED_PARAMETER")
            set(value) {}

        override suspend fun getDataSync(): CoreData {
            return coreData
        }

        override suspend fun setConfigured() {}

        override suspend fun updateDownloadedSubscriptions(
            subscriptions: List<DownloadedSubscription>,
            updateTimestamp: Boolean
        ) {
            updateTimestamp
        }

        override suspend fun updateLastUpdated(lastUpdated: Long) {}

        override suspend fun updateLastUserCountingResponse(lastUserCountingResponse: Long) {
            this.lastUserCountingResponse = lastUserCountingResponse
        }

        override suspend fun updateUserCountingCount(userCountingCount: Int) {
            this.userCountingCount = userCountingCount
        }

        override suspend fun updateSavedState(savedState: SavedState) {
            savedState
        }
    }

    class FakeSettingsRepository : SettingsRepository {

        private val serverUrl : String
        constructor(serverUrl : String) {
            this.serverUrl = serverUrl
        }

        override val settings: Flow<Settings>
            get() = flow {
                emit(
                    Settings(true,
                    true,
                    UpdateConfig.ALWAYS,
                    listOf(""),
                    listOf(""),
                    listOf(
                        Subscription("$serverUrl/easylist.txt", "", 0L),
                        Subscription("$serverUrl/exceptionrules.txt", "", 0L)
                    ),
                    listOf(Subscription("", "", 0L)),
                    true,
                    true)
                )
            }

        override suspend fun getEasylistSubscription(): Subscription {
            return Subscription("$serverUrl/easylist.txt", "", 0L)
        }

        override suspend fun getAcceptableAdsSubscription(): Subscription {
            return Subscription("$serverUrl/exceptionrules.txt", "", 0L)
        }

        override suspend fun getDefaultPrimarySubscriptions(): List<Subscription> {
            return listOf(
                Subscription("$serverUrl/easylist.txt", "", 0L),
                Subscription("$serverUrl/exceptionrules.txt", "", 0L)
            )
        }

        override suspend fun getDefaultOtherSubscriptions(): List<Subscription> {
            TODO("Not yet implemented")
        }

        override suspend fun setAdblockEnabled(enabled: Boolean) {}

        override suspend fun setAcceptableAdsEnabled(enabled: Boolean) {}

        override suspend fun setUpdateConfig(updateConfig: UpdateConfig) {}

        override suspend fun addAllowedDomain(domain: String) {}

        override suspend fun removeAllowedDomain(domain: String) {}

        override suspend fun setAllowedDomains(domains: List<String>) {}

        override suspend fun addBlockedDomain(domain: String) {}

        override suspend fun removeBlockedDomain(domain: String) {}

        override suspend fun setBlockedDomains(domains: List<String>) {}

        override suspend fun addActivePrimarySubscription(subscription: Subscription) {}

        override suspend fun removeActivePrimarySubscription(subscription: Subscription) {}

        override suspend fun setActivePrimarySubscriptions(subscriptions: List<Subscription>) {}

        override suspend fun addActiveOtherSubscription(subscription: Subscription) {}

        override suspend fun removeActiveOtherSubscription(subscription: Subscription) {}

        override suspend fun setActiveOtherSubscriptions(subscriptions: List<Subscription>) {}

        override suspend fun updatePrimarySubscriptionLastUpdate(url: String, lastUpdate: Long) {}

        override suspend fun updateOtherSubscriptionLastUpdate(url: String, lastUpdate: Long) {}

        override suspend fun updatePrimarySubscriptionsLastUpdate(subscriptions: List<Subscription>) {}

        override suspend fun updateOtherSubscriptionsLastUpdate(subscriptions: List<Subscription>) {}

        override suspend fun setAnalyticsEnabled(enabled: Boolean) {}

        override suspend fun getAdditionalTrackingSubscription(): Subscription {
            TODO("Not yet implemented")
        }

        override suspend fun getSocialMediaTrackingSubscription(): Subscription {
            TODO("Not yet implemented")
        }

        override suspend fun markLanguagesOnboardingCompleted() {}
    }

    class FakeAnalyticsProvider : AnalyticsProvider {

        var analyticsEvent : AnalyticsEvent? = null

        override fun logEvent(analyticsEvent: AnalyticsEvent) {
            this.analyticsEvent = analyticsEvent
        }

        override fun setUserProperty(
            analyticsProperty: AnalyticsUserProperty,
            analyticsPropertyValue: String
        ) {}

        override fun enable() {}

        override fun disable() {}
    }
}