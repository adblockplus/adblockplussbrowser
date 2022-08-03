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

package org.adblockplus.adblockplussbrowser.preferences.ui.reporter

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Size
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.preferences.data.ReportIssueRepository
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData
import org.adblockplus.adblockplussbrowser.preferences.ui.reporter.ReportIssueFragment.Companion.REPORT_ISSUE_FRAGMENT_SEND_ERROR
import org.adblockplus.adblockplussbrowser.preferences.ui.reporter.ReportIssueFragment.Companion.REPORT_ISSUE_FRAGMENT_SEND_SUCCESS
import timber.log.Timber


@HiltViewModel
internal class ReportIssueViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {

    val returnedString = MutableLiveData<String>()
    val screenshot = MutableLiveData<Bitmap>()
    var fileName: String = ""
    var data: ReportIssueData = ReportIssueData()

    @Inject
    lateinit var reportIssueRepository: ReportIssueRepository

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    internal fun sendReport() {
        viewModelScope.launch {
            returnedString.value = if (reportIssueRepository.sendReport(data).isEmpty())
                REPORT_ISSUE_FRAGMENT_SEND_SUCCESS
            else REPORT_ISSUE_FRAGMENT_SEND_ERROR
        }
    }

    internal suspend fun processImage(unresolvedUri: String) {
        withContext(Dispatchers.Default) {
            data.screenshot = imageFileToBase64(unresolvedUri)
            val resultString = if (data.screenshot.isEmpty()) {
                // Operation failed, show error message
                "Failed to load image"
            } else {
                Timber.i("ReportIssue: base64 image: ${data.screenshot.subSequence(0, 20)}")
                // Operation successful, validate data
                ""
            }
            returnedString.postValue(resultString)
        }
    }

    private fun imageFileToBase64(unresolvedUri: String): String {
        Timber.d("ReportIssue: unresolvedUri: $unresolvedUri")
        val context = getApplication<Application>().applicationContext
        val cr: ContentResolver = context.contentResolver ?: return ""
        val pic: Uri = Uri.parse(unresolvedUri)
        Timber.d("ReportIssue: image path: $pic")
        fileName = getFileNameFromUri(pic)

        val bs = ByteArrayOutputStream()
        return try {
            val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(cr, pic))
            } else {
                MediaStore.Images.Media.getBitmap(cr, pic)
            }
            processBitmap(imageBitmap).compress(Bitmap.CompressFormat.PNG, 0, bs)
            makePreviewForScreenshot(bs)
            "data:image/png;base64," + Base64.encodeToString(bs.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            Timber.e("ReportIssue: Screenshot decode failed\n" + e.printStackTrace())
            ""
        }
    }

    private fun makePreviewForScreenshot(bs: ByteArrayOutputStream) {
        val byteArray = bs.toByteArray()
        screenshot.postValue(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size))
    }

    private fun getFileNameFromUri(pic: Uri): String{
        // Replace when solving https://jira.eyeo.com/browse/DPC-926
        return "${pic.lastPathSegment.toString()}.png"
    }

    /**
     * Process bitmap to the desired configuration
     *
     * @param imageBitmap original decoded bitmap
     * @return bitmap configured to 16bit and max HD size
     */
    private fun processBitmap(imageBitmap: Bitmap): Bitmap {
        // Calculate smaller size for the sides
        val newSize = calculateImageSize(imageBitmap.width, imageBitmap.height)

        return Bitmap.createScaledBitmap(imageBitmap, newSize.width, newSize.height, true)
    }

    /**
     * Check if the image size is bigger than the max target and reduce it if necessary
     *
     * @param imageWidth
     * @param imageHeight
     * @return a pair containing the new width and height Pair(width, height)
     */
    internal fun calculateImageSize(imageWidth: Int, imageHeight: Int): Size {
        // Determine if image is portrait or landscape and assign "shorter side" and "longer side"
        var orientation = Orientation.PORTRAIT
        var scaledLongerSide = imageHeight
        var scaledShorterSide = imageWidth
        if (imageHeight < imageWidth) {
            orientation = Orientation.LANDSCAPE
            scaledLongerSide = imageWidth
            scaledShorterSide = imageHeight
        }

        // Check if the sizes need conversion and scale them accordingly
        if (scaledShorterSide > REPORT_ISSUE_VIEW_MODEL_IMAGE_MAX_SHORTER_SIDE) {
            val ratio = REPORT_ISSUE_VIEW_MODEL_IMAGE_MAX_SHORTER_SIDE / scaledShorterSide
            scaledShorterSide = (scaledShorterSide * ratio).toInt()
            scaledLongerSide = (scaledLongerSide * ratio).toInt()
        }
        if (scaledLongerSide > REPORT_ISSUE_VIEW_MODEL_IMAGE_MAX_LONGER_SIDE) {
            val ratio = REPORT_ISSUE_VIEW_MODEL_IMAGE_MAX_LONGER_SIDE / scaledLongerSide
            scaledShorterSide = (scaledShorterSide * ratio).toInt()
            scaledLongerSide = (scaledLongerSide * ratio).toInt()
        }

        return if (orientation == Orientation.PORTRAIT) {
            Size(scaledShorterSide, scaledLongerSide)
        } else {
            Size(scaledLongerSide, scaledShorterSide)
        }
    }

    companion object {
        // HD max size
        private const val REPORT_ISSUE_VIEW_MODEL_IMAGE_MAX_LONGER_SIDE = 1280f
        private const val REPORT_ISSUE_VIEW_MODEL_IMAGE_MAX_SHORTER_SIDE = 720f
    }

    private enum class Orientation {
        PORTRAIT,
        LANDSCAPE
    }
}
