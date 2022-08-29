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

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Size
import androidx.annotation.RequiresApi

/**
 * Resolves file name from a given uri.
 *
 * @param uri Uri to local file
 * @return filename extracted from a given uri
 */
fun ContentResolver.resolveFilename(uri: Uri): String {
    return this.query(
        uri, null, null, null, null
    )?.use {
        val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0) {
            it.moveToFirst()
            it.getString(index)
        } else {
            null
        }
    } ?: uri.path.toString()
}

/**
 * Load an image from the given [Uri] and scale it to a given size given as long side and short side values keeping
 * the aspect ratio.
 *
 * @param uri the Uri from which we want to load the image
 * @param longSide the maximum size of the long side
 * @param shortSide the maximum size of the short side
 * @return the decoded and scaled [Bitmap]
 */
fun ContentResolver.loadImage(uri: Uri, longSide: Int, shortSide: Int): Bitmap =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        loadViaImageDecoder(uri, longSide, shortSide)
    else
        legacyLoadImage(uri, longSide, shortSide)

/**
 * Load an image from the given [Uri] and scale it to a given size given as long side and short side values keeping
 * the aspect ratio. This method uses a [BitmapFactory] to decode an InputStream.
 *
 * @param uri the Uri from which we want to load the image
 * @param longSide the maximum size of the long side
 * @param shortSide the maximum size of the short side
 * @return the decoded and scaled [Bitmap]
 */
fun ContentResolver.legacyLoadImage(uri: Uri, longSide: Int, shortSide: Int): Bitmap {
    val boundsDecodingOptions = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
    openInputStream(uri)!!.use { stream ->
        BitmapFactory.decodeStream(stream, null, boundsDecodingOptions)
    }
    val origSize = Size(boundsDecodingOptions.outWidth, boundsDecodingOptions.outHeight)
    // This is a complex logic. There is a full article by google explaining why it must be done this way.
    // See https://developer.android.com/topic/performance/graphics/load-bitmap
    val decodingOptions = BitmapFactory.Options().also {
        @Suppress("MagicNumber") // As the explanation is on the same line
        it.inSampleSize = generateSequence(2) { n -> n * 2}
            .take(9) // Just take the first 9 elements, the max value will be 1024
            .find { n -> origSize.longSide / n < longSide && origSize.shortSide / n < shortSide }!!
            .let { n -> n / 2 } // divide by 2, otherwise the Bitmap will be too small
    }
    // This can be still bigger than the required maximum size, in that case we scale
    val decodedBitmap = openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodingOptions)
    }!!
    val decodedSize = Size(decodedBitmap.width, decodedBitmap.height)
    return if (decodedSize.isContainedIn(longSide, shortSide)) {
        decodedBitmap
    } else decodedSize.downScaleTo(longSide, shortSide).let { (w, h) ->
        Bitmap.createScaledBitmap(decodedBitmap, w, h, true).also { decodedBitmap.recycle() }
    }
}

/**
 * Load an image from the given [Uri] and scale it to a given size given as long side and short side values keeping
 * the aspect ratio. This method requires Android API level 28 (P).
 *
 * @param uri the Uri from which we want to load the image
 * @param longSide the maximum size of the long side
 * @param shortSide the maximum size of the short side
 * @return the decoded and scaled [Bitmap]
 */
@RequiresApi(Build.VERSION_CODES.P)
fun ContentResolver.loadViaImageDecoder(uri: Uri, longSide: Int, shortSide: Int): Bitmap =
    ImageDecoder.createSource(this, uri).let {
        ImageDecoder.decodeBitmap(it) { decoder, info, _ ->
            if (!info.size.isContainedIn(longSide, shortSide)) {
                val newSize = info.size.downScaleTo(longSide, shortSide)
                decoder.setTargetSize(newSize.width, newSize.height)
            }
        }
    }
