package org.adblockplus.adblockplussbrowser.app.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

    private val Context.dataStore by preferencesDataStore(
        name = DataStoreAppPreferences.PREFS_NAME
    )

    @Singleton
    @Provides
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences =
        DataStoreAppPreferences(context.dataStore)

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
}