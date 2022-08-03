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

package org.adblockplus.adblockplussbrowser.settings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.adblockplus.adblockplussbrowser.settings.data.DataStoreSettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.datastore.ProtoSettingsMigration
import org.adblockplus.adblockplussbrowser.settings.data.datastore.ProtoSettingsSerializer
import org.adblockplus.adblockplussbrowser.settings.data.local.HardcodedSubscriptionsDataSource
import org.adblockplus.adblockplussbrowser.settings.data.local.SubscriptionsDataSource
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoSettings
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Singleton
    @Provides
    fun provideSubscriptionsDataSource(@ApplicationContext context: Context): SubscriptionsDataSource =
        HardcodedSubscriptionsDataSource(context)

    @Singleton
    @Provides
    fun provideSettingsDataStore(
        @ApplicationContext context: Context,
        subscriptionsDataSource: SubscriptionsDataSource
    ): DataStore<ProtoSettings> {
        val serializer = ProtoSettingsSerializer()
        val migrations = listOf(ProtoSettingsMigration(context, subscriptionsDataSource))
        return DataStoreFactory.create(serializer = serializer, migrations = migrations) {
            context.dataStoreFile("abp_settings.pb")
        }
    }

    @Singleton
    @Provides
    fun provideSettingsRepository(
        dataStore: DataStore<ProtoSettings>,
        subscriptionsDataSource: SubscriptionsDataSource
    ): SettingsRepository =
        DataStoreSettingsRepository(dataStore, subscriptionsDataSource)
}

