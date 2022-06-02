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

package org.adblockplus.adblockplussbrowser.core

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.base.data.prefs.AppPreferences
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalTime
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [21])
@RunWith(RobolectricTestRunner::class)
class CoreTest {

    @InstallIn(SingletonComponent::class)
    @Module
    object CoreTestModule {
        @Singleton
        @Provides
        fun provideAppPreferences(): AppPreferences = object : AppPreferences {

            private var completed: Boolean = false

            override val referrerAlreadyChecked: Boolean
                get() = TODO("Not yet implemented")

            override fun referrerChecked() {
                TODO("Not yet implemented")
            }

            override val onboardingCompleted: Flow<Boolean>
                get() = flow { emit(completed) }

            override suspend fun completeOnboarding() {
                completed = true
            }

            override val lastFilterListRequest: Flow<Long>
                get() = TODO("Not yet implemented")

            override suspend fun updateLastFilterRequest(lastFilterListRequest: Long) {
                TODO("Not yet implemented")
            }

        }

        @Singleton
        @Provides
        fun provideActivationPreferences(appPreferences: AppPreferences): ActivationPreferences =
            appPreferences

        @Singleton
        @Provides
        fun provideAnalytics(@ApplicationContext context: Context): AnalyticsProvider = object: AnalyticsProvider {
            override fun logEvent(analyticsEvent: AnalyticsEvent) {
                TODO("Not yet implemented")
            }

            override fun logException(exception: Exception) {
                TODO("Not yet implemented")
            }

            override fun setUserProperty(analyticsProperty: AnalyticsUserProperty, analyticsPropertyValue: String) {
                TODO("Not yet implemented")
            }

            override fun enable() {
                TODO("Not yet implemented")
            }

            override fun disable() {
                TODO("Not yet implemented")
            }
        }
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var preferences: AppPreferences

    @Test
    fun test() = runTest {
        val before = preferences.onboardingCompleted.first()
        assert(!before)
        preferences.completeOnboarding()
        val after = preferences.onboardingCompleted.first()
        assert(after)
    }
}