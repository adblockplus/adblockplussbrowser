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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// TODO: Documentation
internal class DebugPreferencesImpl(private val dataStore: DataStore<Preferences>) : DebugPreferences {

    private object Keys {
        val SHOULD_ADD_TEST_PAGES = booleanPreferencesKey("should_add_test_pages")
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
