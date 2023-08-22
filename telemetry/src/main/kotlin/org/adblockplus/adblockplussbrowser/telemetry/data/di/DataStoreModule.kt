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

package org.adblockplus.adblockplussbrowser.telemetry.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.adblockplus.adblockplussbrowser.telemetry.data.DataStoreTelemetryRepository
import org.adblockplus.adblockplussbrowser.telemetry.data.datastore.TelemetryDataSerializer
import org.adblockplus.adblockplussbrowser.telemetry.data.proto.TelemetryData
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {

    @Provides
    @Singleton
    internal fun provideTelemetryDataStore(
        @ApplicationContext context: Context,
        telemetryDataSerializer: TelemetryDataSerializer,
    ): DataStore<TelemetryData> =
        DataStoreFactory.create(
            serializer = telemetryDataSerializer
        ) {
            context.dataStoreFile("telemetry.pb")
        }

    @Provides
    internal fun provideTelemetryRepository(
        telemetryDataStore: DataStore<TelemetryData>,
    ) = DataStoreTelemetryRepository(telemetryDataStore)


}