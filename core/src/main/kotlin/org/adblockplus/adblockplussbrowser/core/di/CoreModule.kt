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

package org.adblockplus.adblockplussbrowser.core.di

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.core.AppInfo
import org.adblockplus.adblockplussbrowser.core.BuildConfig
import org.adblockplus.adblockplussbrowser.core.buildAppInfo
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.DataStoreCoreRepository
import org.adblockplus.adblockplussbrowser.core.data.datastore.ProtoCoreDataSerializer
import org.adblockplus.adblockplussbrowser.core.data.proto.ProtoCoreData
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.downloader.OkHttpDownloader
import org.adblockplus.adblockplussbrowser.core.old_usercounter.OkHttpOldUserCounter
import org.adblockplus.adblockplussbrowser.core.old_usercounter.OldUserCounter
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Module
@InstallIn(SingletonComponent::class)
internal object CoreModule {

    @Provides
    fun provideAppInfo(@ApplicationContext context: Context): AppInfo {
        return context.buildAppInfo()
    }

    @Provides
    @Singleton
    fun provideOkHttpClientLogger() =
        HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.HEADERS // The default is Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor) =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    @ExperimentalTime
    @Provides
    @Singleton
    fun provideSubscriptionDownloader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        appInfo: AppInfo,
        repository: CoreRepository,
        analyticsProvider: AnalyticsProvider
    ): Downloader =
        OkHttpDownloader(context, okHttpClient, repository, appInfo, analyticsProvider)

    @ExperimentalTime
    @Provides
    @Singleton
    fun provideUserCounter(
        okHttpClient: OkHttpClient,
        appInfo: AppInfo,
        repository: CoreRepository,
        settings: SettingsRepository,
        analyticsProvider: AnalyticsProvider
    ): OldUserCounter =
        OkHttpOldUserCounter(okHttpClient, repository, settings, appInfo, analyticsProvider)

    @Provides
    @CorePreferences
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("core_prefs.xml", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideCoreDataStore(@ApplicationContext context: Context): DataStore<ProtoCoreData> =
        DataStoreFactory.create(ProtoCoreDataSerializer) {
            context.dataStoreFile("abp_core.pb")
        }

    @Provides
    @Singleton
    fun provideCoreRepository(
        dataStore: DataStore<ProtoCoreData>,
        @CorePreferences sharedPreferences: SharedPreferences
    ): CoreRepository {
        return DataStoreCoreRepository(dataStore, sharedPreferences)
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CorePreferences

