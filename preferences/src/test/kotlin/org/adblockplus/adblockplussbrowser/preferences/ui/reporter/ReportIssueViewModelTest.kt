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

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.adblockplus.adblockplussbrowser.preferences.data.ReportIssueRepository
import org.adblockplus.adblockplussbrowser.preferences.helpers.FakeAnalyticsProvider
import org.adblockplus.adblockplussbrowser.preferences.helpers.FakeSettingsRepository
import org.adblockplus.adblockplussbrowser.preferences.helpers.Fakes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment.getApplication
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
@ExperimentalCoroutinesApi
class ReportIssueViewModelTest {

    private val reportIssueViewModel = ReportIssueViewModel(getApplication())
    private val mockReportIssueRepository = Mockito.mock(ReportIssueRepository::class.java)

    // This rule is used to be able to listen to the changes of mutable live data as it
    // runs tasks synchronously
    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private suspend fun whenSendReport() = Mockito.`when`(
        mockReportIssueRepository.sendReport(Fakes.fakeReportIssueData))

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        reportIssueViewModel.reportIssueRepository = mockReportIssueRepository
        reportIssueViewModel.analyticsProvider = FakeAnalyticsProvider()
        reportIssueViewModel.settingsRepository = FakeSettingsRepository("")
        val text = ""
        context.openFileOutput("active_subscriptions_version_logs.txt", Context.MODE_PRIVATE)
            .use { it.write(text.toByteArray()) }
    }

    @Test
    fun `test image correctly processed`() {
        runTest {
            reportIssueViewModel.processImage(TEST_URI)
            assertTrue(reportIssueViewModel.data.screenshot.isNotEmpty())
            assertEquals(
                BackgroundOperationOutcome.SCREENSHOT_PROCESSING_FINISHED,
                reportIssueViewModel.backgroundOperationOutcome.value
            )
        }
    }

    @Test
    fun `test image failed to load`() {
        // Prepare mocks to raise exception when loading image
        val application = Mockito.mock(getApplication()::class.java)
        val contentResolver = Mockito.mock(ContentResolver::class.java)
        val fakeUri = Uri.parse("content://empty")
        Mockito.`when`(application.contentResolver).thenReturn(contentResolver)
        // Run
        val reportIssueViewModel = ReportIssueViewModel(application)
        runTest {
            reportIssueViewModel.processImage(fakeUri)
            assertTrue(reportIssueViewModel.data.screenshot.isEmpty())
            assertEquals(
                BackgroundOperationOutcome.SCREENSHOT_PROCESSING_FINISHED,
                reportIssueViewModel.backgroundOperationOutcome.value
            )
        }
    }

    @Test
    fun `test send report successful`() {
        runTest {
            whenSendReport().thenReturn(Result.success(Unit))
            reportIssueViewModel.data = Fakes.fakeReportIssueData
            reportIssueViewModel.sendReport(context)
            assertEquals(
                BackgroundOperationOutcome.REPORT_SEND_SUCCESS,
                reportIssueViewModel.backgroundOperationOutcome.value
            )
        }
    }

    @Test
    fun `test send report failure`() {
        runTest {
            whenSendReport().thenReturn(Result.failure(RuntimeException()))
            reportIssueViewModel.data = Fakes.fakeReportIssueData
            reportIssueViewModel.sendReport(context)
            assertEquals(
                BackgroundOperationOutcome.REPORT_SEND_ERROR,
                reportIssueViewModel.backgroundOperationOutcome.value
            )
        }
    }

    companion object {
        private val TEST_URI = Uri.parse("content://media/screenshot/20")
    }

}
