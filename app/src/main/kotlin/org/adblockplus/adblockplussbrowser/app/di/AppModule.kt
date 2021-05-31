package org.adblockplus.adblockplussbrowser.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.core.CoreSubscriptionsManager
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal object AppModule {

    @Provides
    @Singleton
    fun provideSubscriptionsManager(
        @ApplicationContext context: Context
    ): SubscriptionsManager =
        CoreSubscriptionsManager(context)
}