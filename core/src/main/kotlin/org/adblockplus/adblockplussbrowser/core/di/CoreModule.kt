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
import org.adblockplus.adblockplussbrowser.core.AppInfo
import org.adblockplus.adblockplussbrowser.core.buildAppInfo
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.DataStoreCoreRepository
import org.adblockplus.adblockplussbrowser.core.data.datastore.ProtoCoreDataSerializer
import org.adblockplus.adblockplussbrowser.core.data.proto.ProtoCoreData
import org.adblockplus.adblockplussbrowser.core.downloader.Downloader
import org.adblockplus.adblockplussbrowser.core.downloader.OkHttpDownloader
import javax.inject.Qualifier
import javax.inject.Singleton

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
            level = HttpLoggingInterceptor.Level.HEADERS
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor) =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideSubscriptionDownloader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        appInfo: AppInfo,
        repository: CoreRepository
    ): Downloader =
        OkHttpDownloader(context, okHttpClient, repository, appInfo)

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