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

package org.adblockplus.adblockplussbrowser.app.ui.helpers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.base.data.model.CustomSubscriptionType
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.data.prefs.AppPreferences
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig

@SuppressWarnings("EmptyFunctionBlock")
class Fakes {

    internal open class CustomFakeAppPreferences(
        private val customOnBoardingCompleted: Boolean = true,
        private val customLastFilterListRequest: Long = 0L,
        private val customReferrerAlreadyChecked: Boolean = false
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
            return super.isAdblockEnabled()
        }

        override suspend fun updateLastFilterRequest(lastFilterListRequest: Long) {}

        override val shouldAddTestPages: Flow<Boolean>
            get() = flowOf(false)

        override fun initialTestPagesConfigurationCompleted() {}
    }
}
