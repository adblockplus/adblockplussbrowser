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
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import java.io.File
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.RuntimeEnvironment.getApplication

@RunWith(RobolectricTestRunner::class)
class ReportIssueViewModelTest {

    private val application = Mockito.mock(Application::class.java)
    private val mContext = Mockito.mock(Context::class.java)
    private val mContentResolver = Mockito.mock(ContentResolver::class.java)
    private lateinit var reportIssueViewModel: ReportIssueViewModel

    @Before
    fun setUp() {
        reportIssueViewModel = ReportIssueViewModel(application)
    }


    @Test
    fun test1() {
        val testImageFile = File("mock/test_screenshot.jpg")
        val imageUri = Uri.fromFile(testImageFile)
        runBlocking {
            reportIssueViewModel.processImage(imageUri.toString())
        }
    }
}