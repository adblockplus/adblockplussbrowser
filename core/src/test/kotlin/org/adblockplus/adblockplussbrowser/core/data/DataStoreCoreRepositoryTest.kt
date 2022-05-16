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

package org.adblockplus.adblockplussbrowser.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.adblockplus.adblockplussbrowser.core.data.datastore.ProtoCoreDataSerializer
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState
import org.adblockplus.adblockplussbrowser.core.data.proto.ProtoCoreData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class DataStoreCoreRepositoryTest {

    private val testContext: Context = ApplicationProvider.getApplicationContext()

    private val testDataStore: DataStore<ProtoCoreData> =
        DataStoreFactory.create(
            produceFile = { testContext.dataStoreFile("abp_core_test.pb") },
            serializer = ProtoCoreDataSerializer
        )

    private val dataStoreCoreRepository: DataStoreCoreRepository =
        DataStoreCoreRepository(
            testDataStore,
            testContext.getSharedPreferences("core_prefs_test.xml", Context.MODE_PRIVATE),
        )

    @Test
    fun `test subscriptions path value`() {
        assert(dataStoreCoreRepository.subscriptionsPath.isNullOrEmpty())
        dataStoreCoreRepository.subscriptionsPath = "/test/core"
        assert(dataStoreCoreRepository.subscriptionsPath == "/test/core")
    }

    @Test
    fun `test set configured`() {
        runBlocking {
            assertFalse(dataStoreCoreRepository.getDataSync().configured)
            dataStoreCoreRepository.setConfigured()
            assertTrue(dataStoreCoreRepository.getDataSync().configured)
        }
    }

    @Test
    fun `test update downloaded subscriptions`() {
        val subscriptions = listOf(
            DownloadedSubscription("www.test.com")
        )
        runBlocking {
            // Check current subscriptions are empty
            assert(dataStoreCoreRepository.getDataSync().downloadedSubscription.isEmpty())
            dataStoreCoreRepository.updateDownloadedSubscriptions(subscriptions, false)
            // Subscriptions shouldn't be empty after update
            assert(dataStoreCoreRepository.getDataSync().downloadedSubscription.isNotEmpty())
            assert(dataStoreCoreRepository.getDataSync().lastUpdated == 0L)
        }
    }

    @Test
    fun `test update downloaded subscriptions and updateTimestamp`() {
        val subscriptions = listOf(
            DownloadedSubscription("www.test.com")
        )
        runBlocking {
            assert(dataStoreCoreRepository.getDataSync().downloadedSubscription.isEmpty())
            dataStoreCoreRepository.updateDownloadedSubscriptions(subscriptions, true)
            assert(dataStoreCoreRepository.getDataSync().downloadedSubscription.isNotEmpty())
            assert(dataStoreCoreRepository.getDataSync().lastUpdated != 0L)
        }
    }

    @Test
    fun `test update last update value`() {
        val currentTimestamp = Date().time
        runBlocking {
            assertEquals(0L, dataStoreCoreRepository.getDataSync().lastUpdated)
            dataStoreCoreRepository.updateLastUpdated(currentTimestamp)
            assertEquals(currentTimestamp, dataStoreCoreRepository.getDataSync().lastUpdated)
        }
    }

    @Test
    fun `test update last user counting response`() {
        val currentTimestamp = Date().time
        runBlocking {
            assertEquals(0L, dataStoreCoreRepository.getDataSync().lastUserCountingResponse)
            dataStoreCoreRepository.updateLastUserCountingResponse(currentTimestamp)
            assertEquals(currentTimestamp, dataStoreCoreRepository.getDataSync().lastUserCountingResponse)
        }
    }

    @Test
    fun `test update user counting count`() {
        runBlocking {
            assertEquals(0, dataStoreCoreRepository.getDataSync().userCountingCount)
            dataStoreCoreRepository.updateUserCountingCount(100)
            assertEquals(100, dataStoreCoreRepository.getDataSync().userCountingCount)
        }
    }

    @Test
    fun `test update saved state`() {
        // Set up test values
        val originalSavedState = SavedState (
            acceptableAdsEnabled = false,
            allowedDomains = emptyList(),
            blockedDomains = emptyList(),
            primarySubscriptions = emptyList(),
            otherSubscriptions = emptyList()
        )

        val newState = SavedState (
            acceptableAdsEnabled = true,
            allowedDomains = listOf("www.google.com"),
            blockedDomains = listOf("www.wikipedia.com", "www.stackoverflow.com"),
            primarySubscriptions = listOf("www.test.com"),
            otherSubscriptions = listOf("www.test.com")
        )

        runBlocking {
            // Check that everything is with its default values
            assert(originalSavedState == dataStoreCoreRepository.getDataSync().lastState)
            // Update state with new values
            dataStoreCoreRepository.updateSavedState(newState)
            // Assert new values were set
            assert(newState == dataStoreCoreRepository.getDataSync().lastState)
        }
    }
 }