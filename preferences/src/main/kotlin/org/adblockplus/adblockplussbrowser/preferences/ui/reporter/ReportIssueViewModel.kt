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
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Base64
import android.util.Size
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.os.resolveFilename
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.data.ReportIssueRepository
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject


enum class BackgroundOperationOutcome {
    SCREENSHOT_PROCESSING_FINISHED,
    REPORT_SEND_SUCCESS,
    REPORT_SEND_ERROR
}

/**
 * Contains logic used for issue report screenshot conversion and sending report.
 */
@HiltViewModel
internal class ReportIssueViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {

    val backgroundOperationOutcome = MutableLiveData<BackgroundOperationOutcome>()
    val screenshot = MutableLiveData<Bitmap>()
    var fileName: String = ""
    var data: ReportIssueData = ReportIssueData()

    val displaySnackbarMessage: MutableLiveData<String> = MutableLiveData<String>("")

    @Inject
    lateinit var reportIssueRepository: ReportIssueRepository

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    internal fun sendReport() {
        viewModelScope.launch {
            val context: Context = getApplication<Application>().applicationContext
            backgroundOperationOutcome.postValue(
                if (reportIssueRepository.sendReport(data).isSuccess) {
                    displaySnackbarMessage.postValue(context.getString(R.string.issueReporter_report_sent))
                    BackgroundOperationOutcome.REPORT_SEND_SUCCESS
                } else {
                    displaySnackbarMessage.postValue(context.getString(R.string.issueReporter_report_send_error))
                    BackgroundOperationOutcome.REPORT_SEND_ERROR
                }
            )
        }
    }

    internal suspend fun processImage(unresolvedUri: Uri, activity: FragmentActivity?) {
        withContext(Dispatchers.Default) {
            val context: Context = getApplication<Application>().applicationContext
            val cr: ContentResolver = context.contentResolver

            val fileSizeGood = checkFileSize(unresolvedUri, cr)
            data.screenshot = if (fileSizeGood) {
                imageFileToBase64(unresolvedUri, activity, cr).getOrDefault("")
            } else {
                ""
            }

            if (!fileSizeGood) {
                displaySnackbarMessage.postValue(context.getString(R.string.issueReporter_report_screenshot_too_large))
            } else if (data.screenshot.isEmpty()) {
                displaySnackbarMessage.postValue(context.getString(R.string.issueReporter_report_screenshot_invalid))
            }

            backgroundOperationOutcome.postValue(BackgroundOperationOutcome.SCREENSHOT_PROCESSING_FINISHED)
        }
    }

    private suspend fun checkFileSize(unresolvedUri: Uri, cr: ContentResolver): Boolean {
        var fileLength = -1L
        withContext(Dispatchers.IO) {
            runCatching {
                val pfd: ParcelFileDescriptor? = cr.openFileDescriptor(unresolvedUri, "r")
                fileLength = pfd?.statSize ?: -1L
                pfd?.close()
            }
        }

        return when {
            fileLength < 0L -> {
                // Could happen when the file descriptor is not obtained hence skip the check
                Timber.e("ReportIssue: can't get the file size: $unresolvedUri")
                true
            }
            fileLength > IMAGE_MAX_LENGTH -> {
                Timber.e("The image file is too large: $unresolvedUri ")
                false
            }
            else -> {
                Timber.d("ReportIssue: image size: $fileLength")
                true
            }
        }
    }

    private fun imageFileToBase64(
        unresolvedUri: Uri,
        activity: FragmentActivity?,
        cr: ContentResolver
    ): Result<String> {
        Timber.d("ReportIssue: unresolvedUri: $unresolvedUri")

        activity?.resolveFilename(unresolvedUri)?.let { fileNameString ->
            fileName = fileNameString
        }

        return runCatching {
            val screenshotByteStream = ByteArrayOutputStream()
            val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(cr, unresolvedUri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(cr, unresolvedUri)
            }
            processBitmap(imageBitmap).compress(Bitmap.CompressFormat.PNG, 0, screenshotByteStream)
            makePreviewForScreenshot(screenshotByteStream)
            "data:image/png;base64," + Base64.encodeToString(screenshotByteStream.toByteArray(), Base64.DEFAULT)
        }
    }

    private fun makePreviewForScreenshot(bs: ByteArrayOutputStream) {
        val byteArray = bs.toByteArray()
        screenshot.postValue(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size))
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
        if (scaledShorterSide > IMAGE_MAX_SHORTER_SIDE) {
            val ratio = IMAGE_MAX_SHORTER_SIDE / scaledShorterSide
            scaledShorterSide = (scaledShorterSide * ratio).toInt()
            scaledLongerSide = (scaledLongerSide * ratio).toInt()
        }
        if (scaledLongerSide > IMAGE_MAX_LONGER_SIDE) {
            val ratio = IMAGE_MAX_LONGER_SIDE / scaledLongerSide
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
        private const val IMAGE_MAX_LONGER_SIDE = 1280f
        private const val IMAGE_MAX_SHORTER_SIDE = 720f
        private const val IMAGE_MAX_LENGTH = 5 * 1024 * 1024
    }

    private enum class Orientation {
        PORTRAIT,
        LANDSCAPE
    }
}
