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

package org.adblockplus.adblockplussbrowser.core.provider

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import kotlin.time.ExperimentalTime
import org.adblockplus.adblockplussbrowser.core.di.CoreModule
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounterWorker.Companion.USER_COUNTER_KEY_ONESHOT_WORK
import org.adblockplus.adblockplussbrowser.settings.di.SettingsModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver

@ExperimentalTime
@Config(
    application = HiltTestApplication::class,
    sdk = [21],
    manifest = "src/test/AndroidManifest.xml"
)
@RunWith(RobolectricTestRunner::class)
@UninstallModules(CoreModule::class, SettingsModule::class)
@HiltAndroidTest
internal class FilterListContentProviderTest {
    private var contentResolver: ContentResolver? = null
    private var shadowContentResolver: ShadowContentResolver? = null
    private var filterListContentProvider: FilterListContentProvider? = null
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val uriSource =
        Uri.parse("content://org.adblockplus.adblockplussbrowser.contentBlocker.contentProvider")

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        contentResolver = context.contentResolver
        val providerInfo = ProviderInfo()
        providerInfo.authority =
            "org.adblockplus.adblockplussbrowser.contentBlocker.contentProvider"
        providerInfo.grantUriPermissions = true
        val controller = Robolectric.buildContentProvider(FilterListContentProvider::class.java)
            .create(providerInfo)
        shadowContentResolver = shadowOf(contentResolver)
        filterListContentProvider = controller.get()
    }

    @Test
    fun onCreate() {
        val res = filterListContentProvider?.onCreate()
        assertEquals(true, res)
    }

    @Test
    fun delete() {
        assertEquals(0, filterListContentProvider?.delete(uriSource, null, null))
    }

    @Test
    fun getType() {
        assertNull(filterListContentProvider?.getType(uriSource))
    }

    @Test
    fun insert() {
        val values = ContentValues()
        assertNull(filterListContentProvider?.insert(uriSource, values))
    }

    @Test
    fun `open File AA Enabled`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        val myConfig = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
        WorkManager.initialize(context, myConfig)
        // Open file and check result is not null
        val parcelFileDescriptor = filterListContentProvider?.openFile(uriSource, "r")
        assertNotNull(parcelFileDescriptor)
        // User counter work was enqueued
        val userCounterWork = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(USER_COUNTER_KEY_ONESHOT_WORK)
        assertEquals(1, userCounterWork.get().size)
    }

    @Test
    fun query() {
        assertNull(filterListContentProvider?.query(uriSource, null, null, null, null))
    }

    @Test
    fun update() {
        assertEquals(0, filterListContentProvider?.update(uriSource, null, null, null))
    }
}

