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

package org.adblockplus.adblockplussbrowser.base.os

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import org.hamcrest.CustomMatcher
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.android.controller.ContentProviderController
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowContentResolver
import java.io.File
import kotlin.math.max
import kotlin.math.min

private const val MEDIA_AUTHORITY = "media"
private const val TEST_FILENAME = "test"
private const val TEST_DISPLAY_NAME = "test.png"
private val TEST_URI = Uri.parse("content://media/screenshot/20")


@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowSize::class, ShadowImageDecoder::class])
class ContentResolverExtensionsKtTest {

    private lateinit var contentProviderController: ContentProviderController<BaseContentProvider>
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var resolver: ContentResolver

    @Before
    fun setUp() {
        activityController = Robolectric.buildActivity(Activity::class.java)
        contentProviderController = Robolectric.buildContentProvider(BaseContentProvider::class.java)
            .create(ProviderInfo().apply { authority = MEDIA_AUTHORITY })
        resolver = activityController.get().contentResolver
    }

    @After
    fun shutDown() {
        contentProviderController.shutdown()
        activityController.destroy()
    }

    @Test
    fun `should resolve to the absolute path`() {
        val file = File(TEST_FILENAME)
        val uri = Uri.fromFile(file)
        val result = resolver.resolveFilename(uri)
        assertEquals(file.absolutePath, result)
    }

    @Test
    fun `should resolve to a proper name`() {
        val values = ContentValues().apply { put(OpenableColumns.DISPLAY_NAME, TEST_DISPLAY_NAME) }
        resolver.insert(TEST_URI, values)
        val result = resolver.resolveFilename(TEST_URI)
        assertEquals(TEST_DISPLAY_NAME, result)
    }

    @Test
    fun `the legacy loader should be able to load the default bitmap`() {
        val bitmap = resolver.legacyLoadImage(TEST_URI, 1280, 720)
        assertEquals(100, bitmap.width)
        assertEquals(100, bitmap.height)
    }

    @Test
    fun `the legacy loader should scale landscape image`() =
        loadImageTest(resolver::legacyLoadImage, "3680x2070.jpg", 1280, 720)

    @Test
    fun `the legacy loader should scale portrait image`()  =
        loadImageTest(resolver::legacyLoadImage, "2070x3680.jpg", 1280, 720)

    @Test
    fun `the legacy loader should not scale small images`() {
        resolver.mapUriToResource(TEST_URI, "640x480.jpg")

        val bitmap = resolver.legacyLoadImage(TEST_URI, 1280, 720)
        assertEquals(640, bitmap.width)
        assertEquals(480, bitmap.height)
    }

    @Test
    fun `the ImageDecoder loader should scale landscape image`() =
        loadImageTest(resolver::loadViaImageDecoder, "3680x2070.jpg", 1280, 720)

    @Test
    fun `the ImageDecoder loader should scale portrait image`() =
        loadImageTest(resolver::loadViaImageDecoder, "2070x3680.jpg", 1280, 720)

    @Test
    fun `the ImageDecoder loader should not scale small images`() {
        resolver.mapUriToResource(TEST_URI, "640x480.jpg")

        val bitmap = resolver.loadViaImageDecoder(TEST_URI, 1280, 720)
        assertEquals(640, bitmap.width)
        assertEquals(480, bitmap.height)
    }

    @Suppress("SameParameterValue")
    private fun loadImageTest(
        method: (Uri, Int, Int) -> Bitmap,
        resourceName: String,
        targetSide1: Int,
        targetSide2: Int
    ) {
        resolver.mapUriToResource(TEST_URI, resourceName)

        val targetLongSide = max(targetSide1, targetSide2)
        val targetShortSide = min(targetSide1, targetSide2)
        val bitmap = method(TEST_URI, targetLongSide, targetShortSide)

        val longSide = max(bitmap.width, bitmap.height)
        val shortSide = min(bitmap.width, bitmap.height)

        assertThat(longSide, isEqualOrLess(targetLongSide))
        assertThat(shortSide, isEqualOrLess(targetShortSide))
    }


}

private fun ContentResolver.mapUriToResource(uri: Uri, resourceName: String) =
    Shadow.extract<ShadowContentResolver>(this).registerInputStreamSupplier(uri) {
        javaClass.classLoader!!.getResourceAsStream(resourceName)
    }

@Suppress("UNCHECKED_CAST")
private fun <T: Comparable<T>> isEqualOrLess(value: T): Matcher<T> {
    return object: CustomMatcher<T>("The value should be equal to or less than $value") {
        override fun matches(item: Any?): Boolean = (item as T) <= value
    }
}
