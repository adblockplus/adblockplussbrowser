package org.adblockplus.adblockplussbrowser.base.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreAppPreferences(private val dataStore: DataStore<Preferences>) :
    AppPreferences {

    companion object {
        const val PREFS_NAME = "abp_app_prefs"
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_FILTER_REQUEST = longPreferencesKey("last_filter_request")
    }

    override val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] ?: false }

    override suspend fun completeOnboarding() {
        dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    override val lastFilterListRequest: Flow<Long> = dataStore.data.map { preferences ->
        preferences[Keys.LAST_FILTER_REQUEST] ?: 0
    }

    override suspend fun updateLastFilterRequest(lastFilterListRequest: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_FILTER_REQUEST] = lastFilterListRequest
        }
    }

}
