package org.adblockplus.adblockplussbrowser.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsManager
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.FirebaseAnalyticsProvider
import org.adblockplus.adblockplussbrowser.app.data.prefs.AppPreferences
import org.adblockplus.adblockplussbrowser.app.data.prefs.DataStoreAppPreferences
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.core.CoreSubscriptionsManager
import org.adblockplus.adblockplussbrowser.onboarding.data.prefs.OnboardingPreferences
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@ExperimentalTime
@InstallIn(SingletonComponent::class)
@Module
internal object AppModule {

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(DataStoreAppPreferences.PREFS_NAME)
        }

    @Singleton
    @Provides
    fun provideAppPreferences(dataStore: DataStore<Preferences>): AppPreferences =
        DataStoreAppPreferences(dataStore)

    @Singleton
    @Provides
    fun provideOnboardingPreferences(appPreferences: AppPreferences): OnboardingPreferences =
        appPreferences

    @Singleton
    @Provides
    fun provideActivationPreferences(appPreferences: AppPreferences): ActivationPreferences =
        appPreferences

    @Singleton
    @Provides
    fun provideSubscriptionsManager(@ApplicationContext context: Context): SubscriptionsManager =
        CoreSubscriptionsManager(context)

    @Singleton
    @Provides
    fun provideAnalytics(@ApplicationContext context: Context): AnalyticsProvider =
        AnalyticsManager(listOf((FirebaseAnalyticsProvider(context))))
}