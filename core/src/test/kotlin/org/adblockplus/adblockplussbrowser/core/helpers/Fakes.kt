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

// This file contains fake implementations used in tests (unit tests), it's fine if the functions are not implemented
// and kept empty.
@file:Suppress("EmptyFunctionBlock")

package org.adblockplus.adblockplussbrowser.core.helpers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.adblockplus.adblockplussbrowser.base.data.model.CustomSubscriptionType
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.base.data.prefs.DebugPreferences
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig
import org.adblockplus.adblockplussbrowser.settings.helpers.test.FakeSettingsRepository

class Fakes {

    internal companion object {
        const val INITIAL_TIMESTAMP = -1L
        const val INITIAL_COUNT = -1
        const val HTTP_ERROR_MOCK_500 = "500\n" +
                "Headers:\n" +
                "Content-Length: 0\n" +
                "\n" +
                "Body:\n"

    }

    internal class FakeCoreRepository(serverUrl: String) : CoreRepository {

        var lastUserCountingResponse = INITIAL_TIMESTAMP
        var userCountingCount = INITIAL_COUNT

        val aaUrl : String
        val easylistUrl : String

        internal var coreData : CoreData

        init {
            aaUrl = "$serverUrl/exceptionrules.txt"
            easylistUrl = "$serverUrl/easylist.txt"
            coreData = CoreData(
                true,
                0L,
                SavedState(true, listOf(""), listOf(""), listOf(""), listOf("")),
                listOf(),
                0L,
                0)
        }

        override val data: Flow<CoreData>
            get() = flow {
                emit(coreData.copy(lastUserCountingResponse = this@FakeCoreRepository.lastUserCountingResponse))
            }

        override var subscriptionsPath: String?
            get() = ""
            @Suppress("UNUSED_PARAMETER")
            set(value) {}

        override suspend fun getDataSync(): CoreData {
            return coreData.copy(lastUserCountingResponse = this.lastUserCountingResponse)
        }

        override suspend fun setConfigured() {}

        override suspend fun updateDownloadedSubscriptions(
            subscriptions: List<DownloadedSubscription>,
            updateTimestamp: Boolean
        ) {}

        override suspend fun updateLastUpdated(lastUpdated: Long) {}

        override suspend fun updateLastUserCountingResponse(lastUserCountingResponse: Long) {
            this.lastUserCountingResponse = lastUserCountingResponse
        }

        override suspend fun updateUserCountingCount(userCountingCount: Int) {
            this.userCountingCount = userCountingCount
        }

        override suspend fun updateSavedState(savedState: SavedState) {  }
    }

    class FakeSettingsRepositoryNoChanges(serverUrl: String) :
        FakeSettingsRepository(serverUrl) {
        override val settings: Flow<Settings>
            get() = flow {
                emit(
                    Settings(
                        true,
                        acceptableAdsStatus,
                        UpdateConfig.ALWAYS,
                        listOf(""),
                        listOf(""),
                        listOf(Subscription("", "", 0L, CustomSubscriptionType.FROM_URL)),
                        listOf(Subscription("", "", 0L, CustomSubscriptionType.FROM_URL)),
                        analyticsEnabled = true,
                        languagesOnboardingCompleted = true
                    )
                )
            }
    }

    class FakeActivationPreferences : ActivationPreferences {
        override val lastFilterListRequest: Flow<Long>
            get() = flowOf(System.currentTimeMillis())

        override suspend fun updateLastFilterRequest(lastFilterListRequest: Long) {
            // NOP
        }
    }

    class FakeDebugPreferences: DebugPreferences {
        override val shouldAddTestPages: Flow<Boolean>
            get() = flowOf(false)

        override fun initialTestPagesConfigurationCompleted() {
            TODO("Not yet implemented")
        }
    }
}

