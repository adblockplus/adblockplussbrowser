package org.adblockplus.adblockplussbrowser.settings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.adblockplus.adblockplussbrowser.settings.data.DataStoreSettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.datastore.ProtoSettingsSerializer
import org.adblockplus.adblockplussbrowser.settings.data.local.HardcodedSubscriptionsDataSource
import org.adblockplus.adblockplussbrowser.settings.data.local.SubscriptionsDataSource
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoSettings
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SettingsModule {

    @Singleton
    @Provides
    fun provideSubscriptionsDataSource(@ApplicationContext context: Context): SubscriptionsDataSource =
        HardcodedSubscriptionsDataSource(context)

    @Singleton
    @Provides
    fun provideSettingsDataStore(
        @ApplicationContext context: Context,
        subscriptionsDataSource: SubscriptionsDataSource,
    ): DataStore<ProtoSettings> {
        val serializer = ProtoSettingsSerializer(subscriptionsDataSource)
        val corruptionHandler = ReplaceFileCorruptionHandler(serializer::provideDefaultValue)
        return DataStoreFactory.create(serializer,corruptionHandler) {
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