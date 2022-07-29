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
import androidx.fragment.app.FragmentActivity
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
import org.adblockplus.adblockplussbrowser.base.os.FileNameHelper
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

    internal suspend fun processImage(unresolvedUri: String, activity: FragmentActivity?) {
        withContext(Dispatchers.Default) {
            data.screenshot = imageFileToBase64(unresolvedUri, activity)
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

    private fun imageFileToBase64(unresolvedUri: String, activity: FragmentActivity?): String {
        Timber.d("ReportIssue: unresolvedUri: $unresolvedUri")
        val context = getApplication<Application>().applicationContext
        val cr: ContentResolver = context.contentResolver ?: return ""
        val pic: Uri = Uri.parse(unresolvedUri)

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
    }
}
