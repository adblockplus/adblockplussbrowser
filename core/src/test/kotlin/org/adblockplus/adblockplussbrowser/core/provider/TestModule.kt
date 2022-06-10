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

package org.adblockplus.adblockplussbrowser.core.provider

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.ExperimentalTime
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.BuildConfig
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.downloader.OkHttpDownloader
import org.adblockplus.adblockplussbrowser.core.helpers.Fakes
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounter
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.mockito.Mockito

@Module
@InstallIn(SingletonComponent::class)
@ExperimentalTime
internal class TestModule {
    @Provides
    @Singleton
    fun getCoreRepository(): CoreRepository {
        return Fakes.FakeCoreRepository("")
    }

    @Provides
    @Singleton
    fun getSettingsRepository(): SettingsRepository {
        return Fakes.FakeSettingsRepository("")
    }

    @Provides
    @Singleton
    fun getActivationPreferences(): ActivationPreferences {
        return Fakes.FakeActivationPreferences()
    }

    @Provides
    @Singleton
    fun getAnalyticsProvider(): AnalyticsProvider {
        return Fakes.FakeAnalyticsProvider()
    }

    @Provides
    @Singleton
    fun provideSubscriptionDownloader(): Downloader = Mockito.mock(OkHttpDownloader::class.java)

    @Provides
    @Singleton
    fun provideUserCounter(): UserCounter = Mockito.mock(OkHttpUserCounter::class.java)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = Mockito.mock(OkHttpClient::class.java)


    @Provides
    @Singleton
    fun provideOkHttpClientLogger() =
        HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.HEADERS // The default is Level.NONE
            }
        }
}