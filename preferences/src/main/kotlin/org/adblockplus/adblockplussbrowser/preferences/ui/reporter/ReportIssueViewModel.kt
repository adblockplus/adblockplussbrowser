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
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.os.FileNameHelper
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus
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
    val screenshot = MutableLiveData<Bitmap>()
    var fileName: String = ""
    var data: ReportIssueData = ReportIssueData()

    private val _status: MutableStateFlow<String> = MutableStateFlow(String())
    val sendReportStatus: LiveData<String>
        get() = _status.asLiveData()


    @Inject
    lateinit var reportIssueRepository: ReportIssueRepository

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    internal fun sendReport() {
        viewModelScope.launch {
            val sendResult = reportIssueRepository.sendReport(data);
            if (sendResult.isEmpty()) {
                _status.value = "Thanks for reporting the issue!"
                returnedString.value = REPORT_ISSUE_FRAGMENT_SEND_SUCCESS
            } else {
                _status.value = "Error: $sendResult"
                returnedString.value = REPORT_ISSUE_FRAGMENT_SEND_ERROR
            }
        }
    }

    internal suspend fun processImage(unresolvedUri: String, activity: FragmentActivity?) {
        withContext(Dispatchers.Default) {
            val cr: ContentResolver? = getApplication<Application>().applicationContext.contentResolver
            if (cr == null) {
                returnedString.postValue("Internal error")
                return@withContext
            }
            val pic: Uri = Uri.parse(unresolvedUri)

            var fileLength = -1L;
            try {
                val pfd: ParcelFileDescriptor? = cr.openFileDescriptor(pic, "r")
                fileLength = pfd?.statSize ?: -1L
                pfd?.close()
            } catch (ex: java.io.FileNotFoundException) {
            }
            if (fileLength == -1L) {
                returnedString.postValue("Cannot open the image file")
                return@withContext
            }

            Timber.i("ReportIssue: image size: $fileLength")

            if (fileLength > REPORT_ISSUE_VIEW_MODEL_IMAGE_MAX_LENGTH) {
                returnedString.postValue("The image file is too large. Please pick another one.")
                return@withContext
            }

            data.screenshot = imageFileToBase64(unresolvedUri)
            val resultString = if (data.screenshot.isEmpty()) {
                // Operation failed, show error message
                "Failed to load image"
            } else {
                // Operation successful, validate data
                ""
            }
            returnedString.postValue(resultString)
        }
    }

    private fun imageFileToBase64(unresolvedUri: String): String {
        Timber.d("ReportIssue: unresolvedUri: ${pic.toString()}")
        val cr: ContentResolver = contentResolver ?: return ""

        fileName = FileNameHelper.getFilename(activity, pic)
        Timber.d("ReportIssue: image path: $pic")

        val bs = ByteArrayOutputStream()
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(cr, pic))
                    .compress(Bitmap.CompressFormat.PNG, REPORT_ISSUE_VIEW_MODEL_IMAGE_QUALITY, bs)
            } else {
                MediaStore.Images.Media.getBitmap(cr, pic)
                    .compress(Bitmap.CompressFormat.PNG, REPORT_ISSUE_VIEW_MODEL_IMAGE_QUALITY, bs)
            }
            val screenshotByteArray = bs.toByteArray()
            screenshot.postValue(BitmapFactory.decodeByteArray(screenshotByteArray, 0, screenshotByteArray.size))
            "data:image/png;base64," + Base64.encodeToString(screenshotByteArray, Base64.DEFAULT)
        } catch (e: OutOfMemoryError) {
            Timber.e(e, "ReportIssue: Screenshot decode failed\n");
            ""
        }
    }

    companion object {
        private const val REPORT_ISSUE_VIEW_MODEL_IMAGE_QUALITY = 100
        private const val REPORT_ISSUE_VIEW_MODEL_IMAGE_MAX_LENGTH = 1 * 1024 * 1024
    }
}
