package org.adblockplus.adblockplussbrowser.settings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.adblockplus.adblockplussbrowser.settings.data.DefaultSettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.datastore.SettingsSerializer
import org.adblockplus.adblockplussbrowser.settings.data.local.SubscriptionsLoader
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Singleton
    @Provides
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Settings> =
        DataStoreFactory.create(
            SettingsSerializer
        ) {
            context.dataStoreFile("abp_settings.pb")
        }

    @Singleton
    @Provides
    fun provideSubscriptionsLoader(
        @ApplicationContext context: Context,
        moshi: Moshi
    ): SubscriptionsLoader =
        SubscriptionsLoader(context, moshi)

    @Singleton
    @Provides
    fun provideSettingsRepository(
        dataStore: DataStore<Settings>,
        subscriptionsLoader: SubscriptionsLoader
    ): SettingsRepository =
        DefaultSettingsRepository(dataStore, subscriptionsLoader)
}