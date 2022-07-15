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
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.preferences.data.ReportIssueRepository
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData
import org.adblockplus.adblockplussbrowser.preferences.ui.reporter.ReportIssueFragment.Companion.REPORT_ISSUE_FRAGMENT_SEND_ERROR
import org.adblockplus.adblockplussbrowser.preferences.ui.reporter.ReportIssueFragment.Companion.REPORT_ISSUE_FRAGMENT_SEND_SUCCESS
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
internal class ReportIssueViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {

    val returnedString = MutableLiveData<String>()
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

    internal fun processImage(unresolvedUri: String) {
        viewModelScope.launch {
            data.screenshot = imageFileToBase64(unresolvedUri)
            returnedString.value = if (data.screenshot.isEmpty()) {
                // Operation failed, show error message
                "Failed to load image"
            } else {
                Timber.i("ReportIssue: base64 image: ${data.screenshot.subSequence(0, 20)}")
                // Operation successful, validate data
                ""
            }
        }
    }

    private fun imageFileToBase64(unresolvedUri: String): String {
        Timber.d("ReportIssue: unresolvedUri: $unresolvedUri")
        val context = getApplication<Application>().applicationContext
        val cr: ContentResolver = context.contentResolver ?: return ""
        val pic: Uri = Uri.parse(unresolvedUri)

        Timber.d("ReportIssue: image path: $pic")

        val bs = ByteArrayOutputStream()
        lateinit var imageBitmap: Bitmap
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imageBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(cr, pic))
            } else {
                imageBitmap = MediaStore.Images.Media.getBitmap(cr, pic)
            }
            scaleImage(imageBitmap).compress(Bitmap.CompressFormat.PNG, REPORT_ISSUE_VIEW_MODEL_IMAGE_QUALITY, bs)
            "data:image/png;base64," + Base64.encodeToString(bs.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            Timber.e("ReportIssue: Screenshot decode failed\n" + e.printStackTrace())
            ""
        }
    }

    private fun scaleImage(imageBitmap: Bitmap): Bitmap {
        val (width, height) = validateImageSize(imageBitmap.width, imageBitmap.height)
        return Bitmap.createScaledBitmap(imageBitmap, width, height, true)
    }

    internal fun validateImageSize(imageWidth: Int, imageHeight: Int): Pair<Int, Int> {
        var (orientation, scaledLongerSide) = if (imageHeight > imageWidth) Pair("portrait", imageHeight) else Pair("landscape", imageWidth)
        var scaledShorterSide = if (imageWidth > imageHeight) imageHeight else imageWidth

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
        return if (orientation == "portrait") Pair(scaledShorterSide, scaledLongerSide) else Pair(scaledLongerSide, scaledShorterSide)
    }

    companion object {
        private const val REPORT_ISSUE_VIEW_MODEL_IMAGE_QUALITY = 80
        // HD max size
        private const val REPORT_ISSUE_VIEW_MODEL_IMAGE_MAX_LONGER_SIDE = 1280f
        private const val REPORT_ISSUE_VIEW_MODEL_IMAGE_MAX_SHORTER_SIDE = 720f
    }
}
