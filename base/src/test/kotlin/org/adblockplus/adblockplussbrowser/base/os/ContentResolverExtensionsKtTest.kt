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
import android.graphics.BitmapFactory
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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val MEDIA_AUTHORITY = "media"
private const val TEST_FILENAME = "test"
private const val TEST_DISPLAY_NAME = "test.png"
private val IMAGE_TEST_URI = Uri.parse("content://media/screenshot/20")
private val TEXT_TEST_URI = Uri.parse("content://media/textFiles/20")
private const val IMG_3680_X_2070 = "3680x2070.jpg"
private const val IMG_2070_X_3680 = "2070x3680.jpg"
private const val IMG_640_X_480 = "640x480.jpg"
private const val IMG_1280_X_960 = "1280x960.jpg"
private const val TEST_TEXT_FILE = "test_text_file.txt"

private const val MAX_RATIO_DELTA = 0.001f

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
        resolver.insert(IMAGE_TEST_URI, values)
        val result = resolver.resolveFilename(IMAGE_TEST_URI)
        assertEquals(TEST_DISPLAY_NAME, result)
    }

    @Test
    fun `should read the text file content`() {
        resolver.mapUriToResource(TEXT_TEST_URI, TEST_TEXT_FILE)
        val result = resolver.readText(TEXT_TEST_URI)
        assert(result.contains("www.google.com"))
    }

    @Test
    fun `the legacy loader should be able to load the default bitmap`() {
        val bitmap = resolver.legacyLoadImage(IMAGE_TEST_URI, 1280, 720)
        assertEquals(100, bitmap.width)
        assertEquals(100, bitmap.height)
    }

    @Test
    fun `the legacy loader should scale landscape image`() =
        loadImageTest(resolver::legacyLoadImage, IMG_3680_X_2070, 1280, 720)

    @Test
    fun `the legacy loader should scale portrait image`()  =
        loadImageTest(resolver::legacyLoadImage, IMG_2070_X_3680, 1280, 720)

    @Test
    fun `the legacy loader should keep the same ratio even if the original one is not 16 over 9`() =
        loadImageTest(resolver::legacyLoadImage, IMG_1280_X_960, 1280, 720)

    @Test
    fun `the legacy loader should not scale small images`() = smallImageTest(resolver::legacyLoadImage)

    @Test
    fun `the ImageDecoder loader should scale landscape image`() =
        loadImageTest(resolver::loadViaImageDecoder, IMG_3680_X_2070, 1280, 720)

    @Test
    fun `the ImageDecoder loader should scale portrait image`() =
        loadImageTest(resolver::loadViaImageDecoder, IMG_2070_X_3680, 1280, 720)

    @Test
    fun `the ImageDecoder loader should keep the same ratio even if the original one is not 16 over 9`() =
        loadImageTest(resolver::loadViaImageDecoder, IMG_1280_X_960, 1280, 720)

    @Test
    fun `the ImageDecoder loader should not scale small images`() = smallImageTest(resolver::loadViaImageDecoder)

    private fun smallImageTest(method: (Uri, Int, Int) -> Bitmap) =
        loadImageTest(method, IMG_640_X_480, 1280, 720)

    @Suppress("SameParameterValue")
    private fun loadImageTest(
        method: (Uri, Int, Int) -> Bitmap,
        resourceName: String,
        targetSide1: Int,
        targetSide2: Int
    ) {
        val originalRatio = originalRatio(resourceName)

        resolver.mapUriToResource(IMAGE_TEST_URI, resourceName)

        val targetLongSide = max(targetSide1, targetSide2)
        val targetShortSide = min(targetSide1, targetSide2)
        val bitmap = method(IMAGE_TEST_URI, targetLongSide, targetShortSide)

        val longSide = max(bitmap.width, bitmap.height)
        val shortSide = min(bitmap.width, bitmap.height)

        val newRatio = bitmap.width / bitmap.height.toFloat()
        val deltaRatio = abs(originalRatio - newRatio)

        assertThat(longSide, isEqualOrLess(targetLongSide))
        assertThat(shortSide, isEqualOrLess(targetShortSide))
        assertThat(deltaRatio, isEqualOrLess(MAX_RATIO_DELTA))
    }

    private fun originalRatio(resourceName: String) = javaClass.classLoader!!.getResourceAsStream(resourceName).use {
        val options = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
        BitmapFactory.decodeStream(it, null, options)
        options.outWidth / options.outHeight.toFloat()
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
