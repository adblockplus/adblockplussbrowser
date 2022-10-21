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
@file:Suppress("EmptyFunctionBlock")

package org.adblockplus.adblockplussbrowser.app.ui.helpers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus
import org.adblockplus.adblockplussbrowser.base.data.prefs.AppPreferences

internal class CustomFakeAppPreferences(
    private val customOnBoardingCompleted: Boolean = true,
    private val customLastFilterListRequest: Long = 0L,
    private val customReferrerAlreadyChecked: Boolean = false,
    private val customIsAdblockEnabled: Boolean = true
): AppPreferences {
    var referrerChecked = false
    override val referrerAlreadyChecked: Boolean
        get() = customReferrerAlreadyChecked

    override fun referrerChecked() {
        referrerChecked = true
    }

    override val onboardingCompleted: Flow<Boolean>
        get() = flowOf(customOnBoardingCompleted)

    override suspend fun completeOnboarding() {}

    override val lastFilterListRequest: Flow<Long>
        get() = flowOf(customLastFilterListRequest)

    override suspend fun isAdblockEnabled(): Flow<Boolean> {
        return flowOf(customIsAdblockEnabled)
    }

    override suspend fun updateLastFilterRequest(lastFilterListRequest: Long) {}

    override val shouldAddTestPages: Flow<Boolean>
        get() = flowOf(false)

    override fun initialTestPagesConfigurationCompleted() {}
}

internal class FakeSubscriptionsManager(
    private val customStatus: SubscriptionUpdateStatus = SubscriptionUpdateStatus.None,
    private val customLastUpdate: Long = 0L
): SubscriptionsManager {
    var forceSubscriptionsManager = false

    override val status: Flow<SubscriptionUpdateStatus>
        get() = flowOf(customStatus)

    override val lastUpdate: Flow<Long>
        get() = flowOf(customLastUpdate)

    override fun initialize() {}

    override fun scheduleImmediate(force: Boolean) {
        forceSubscriptionsManager = force
    }

    override suspend fun validateSubscription(subscription: Subscription): Boolean = false

    override suspend fun updateStatus(status: SubscriptionUpdateStatus) {}
}
