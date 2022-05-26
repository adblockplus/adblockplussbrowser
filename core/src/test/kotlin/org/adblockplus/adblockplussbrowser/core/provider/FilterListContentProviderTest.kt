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
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import kotlin.time.ExperimentalTime

@Config(manifest=Config.DEFAULT_MANIFEST_NAME)
@RunWith(RobolectricTestRunner::class)
internal class FilterListContentProviderTest {
    private var contentResolver : ContentResolver? = null
    private var shadowContentResolver : ShadowContentResolver? = null
    @OptIn(ExperimentalTime::class)
    private var filterListContentProvider : FilterListContentProvider? = null

    @OptIn(ExperimentalTime::class)
    @Before
    fun setUp() {
        contentResolver = ApplicationProvider.getApplicationContext<Context>().contentResolver
        val providerInfo = ProviderInfo()
        providerInfo.authority = "org.adblockplus.adblockplussbrowser.contentBlocker.contentProvider"
        providerInfo.grantUriPermissions = true
        val controller = Robolectric.buildContentProvider(FilterListContentProvider::class.java).create(providerInfo)
        shadowContentResolver = shadowOf(contentResolver)
        filterListContentProvider = controller.get()
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun onCreate() {
        val res  = filterListContentProvider?.onCreate()
        assertEquals(res, true)
    }

    @Test
    fun insert() {
        val values = ContentValues()
        val uriSource = Uri.parse("content://org.adblockplus.adblockplussbrowser.contentBlocker.contentProvider")
        val uri = contentResolver?.insert(uriSource, values);
        assertNull(uri)
    }

    @OptIn(ExperimentalTime::class)
    fun open() {
        val uriSource = Uri.parse("content://org.adblockplus.adblockplussbrowser.contentBlocker.contentProvider")
        val parcelFileDescriptor = filterListContentProvider?.openFile(uriSource, "r")
        assertNotNull(parcelFileDescriptor)
    }
}