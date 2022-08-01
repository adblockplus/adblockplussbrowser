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

package org.adblockplus.adblockplussbrowser.base.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class DataStoreAppPreferences(private val dataStore: DataStore<Preferences>) :
    AppPreferences {

    companion object {
        const val PREFS_NAME = "abp_app_prefs"
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_FILTER_REQUEST = longPreferencesKey("last_filter_request")
        val REFERRER_CHECKED = booleanPreferencesKey("referrer_checked")
        val SHOULD_ADD_TEST_PAGES = booleanPreferencesKey("should_add_test_pages")
    }

    override val referrerAlreadyChecked: Boolean =
        runBlocking {
            dataStore.data.map { it[Keys.REFERRER_CHECKED] ?: false }.firstOrNull() == true
        }

    override fun referrerChecked() {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[Keys.REFERRER_CHECKED] = true
            }
        }
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

    override val shouldAddTestPages: Flow<Boolean> =
        runBlocking {
            dataStore.data.map { it[Keys.SHOULD_ADD_TEST_PAGES] ?: true}
        }

    override fun initialTestPagesConfigurationCompleted() {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[Keys.SHOULD_ADD_TEST_PAGES] = false
            }
        }
    }
}
