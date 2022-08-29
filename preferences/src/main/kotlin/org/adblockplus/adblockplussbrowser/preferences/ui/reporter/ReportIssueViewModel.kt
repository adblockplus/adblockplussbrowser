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
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.os.loadImage
import org.adblockplus.adblockplussbrowser.base.os.resolveFilename
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.data.ReportIssueRepository
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData
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
    val screenshot = MutableLiveData<Bitmap?>()
    var fileName: String = ""
    var data: ReportIssueData = ReportIssueData()

    val displaySnackbarMessage = MutableLiveData<@StringRes Int>(0)

    @Inject
    lateinit var reportIssueRepository: ReportIssueRepository

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    internal fun sendReport() {
        viewModelScope.launch {
            backgroundOperationOutcome.postValue(
                if (reportIssueRepository.sendReport(data).isSuccess) {
                    displaySnackbarMessage.postValue(R.string.issueReporter_report_sent)
                    BackgroundOperationOutcome.REPORT_SEND_SUCCESS
                } else {
                    displaySnackbarMessage.postValue(R.string.issueReporter_report_send_error)
                    BackgroundOperationOutcome.REPORT_SEND_ERROR
                }
            )
        }
    }

    internal suspend fun processImage(uri: Uri) {
        val cr = getApplication<Application>().contentResolver
        withContext(Dispatchers.Default) {
            runCatching {
                cr.loadImage(uri, IMAGE_MAX_LONGER_SIDE, IMAGE_MAX_SHORTER_SIDE)
            }.onSuccess { bitmap ->
                if(bitmap != null) {
                    val base64Bitmap = bitmap.toBase64EncodedPng()
                    if (base64Bitmap.length > IMAGE_MAX_LENGTH) {
                        clearScreenshot()
                        displaySnackbarMessage.postValue(R.string.issueReporter_report_screenshot_too_large)
                    } else {
                        fileName = cr.resolveFilename(uri)
                        data.screenshot = base64Bitmap
                        screenshot.postValue(bitmap)
                    }
                }
            }.onFailure {
                clearScreenshot()
                displaySnackbarMessage.postValue(R.string.issueReporter_report_screenshot_invalid)
            }
            backgroundOperationOutcome.postValue(BackgroundOperationOutcome.SCREENSHOT_PROCESSING_FINISHED)
        }
    }

    private fun clearScreenshot() {
        screenshot.postValue(null)
        data.screenshot = ""
    }

    companion object {
        // HD max size
        private const val IMAGE_MAX_LONGER_SIDE = 1280
        private const val IMAGE_MAX_SHORTER_SIDE = 720
        /* Max length of the base64 encoded string that carries the screenshot data.
           This value is defined in the issue reporter BE
           https://gitlab.com/eyeo/devops/legacy/sitescripts/-/tree/master/sitescripts/reports
         */
        private const val IMAGE_MAX_LENGTH = 1280 * 1280 * 4 + 4096
    }
}

private fun Bitmap.toBase64EncodedPng(): String = ByteArrayOutputStream().use {
    it.write("data:image/png;base64,".toByteArray(Charsets.US_ASCII))
    compress(Bitmap.CompressFormat.PNG, 0, it)
    Base64.encodeToString(it.toByteArray(), Base64.DEFAULT)
}
