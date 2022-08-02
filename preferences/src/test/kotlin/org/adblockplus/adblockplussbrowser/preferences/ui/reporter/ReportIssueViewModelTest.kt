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

import android.net.Uri
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment.getApplication

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class ReportIssueViewModelTest {

    private val reportIssueViewModel = ReportIssueViewModel(getApplication())
    lateinit var tempFile: File
    lateinit var tempFileUri: Uri

    @Before
    fun setUp() {
        tempFile = File("test_screenshot.jpg")
        val result = javaClass.classLoader?.getResourceAsStream("test_screenshot.jpg")
        if (result != null) {
            tempFile.writeBytes(result.readBytes())
        }
        tempFileUri = Uri.fromFile(tempFile)
    }

    @After
    fun tearDown() {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    @Test
    fun `test image correctly processed`() {
        runTest {
            reportIssueViewModel.processImage(tempFileUri.toString())
            assertTrue(reportIssueViewModel.data.screenshot.isNotEmpty())
        }
    }

    @Test
    fun `test image failed to load`() {
        tempFile.delete()
        if (!tempFile.exists()) {
            runTest {
                reportIssueViewModel.processImage(tempFileUri.toString())
//                assertTrue(reportIssueViewModel.data.screenshot.isEmpty())
            }
        }
    }
}