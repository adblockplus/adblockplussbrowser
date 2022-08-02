package org.adblockplus.adblockplussbrowser.preferences.data.model

import org.adblockplus.adblockplussbrowser.preferences.helpers.Fakes
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReportIssueDataTest {

    private val fakeReportIssueData = Fakes().fakeReportIssueData

    @Test
    fun `test validate screenshot`() {
        assertFalse(fakeReportIssueData.validateScreenshot())
        fakeReportIssueData.screenshot = "data:image/png;base64,...."
        assert(fakeReportIssueData.validateScreenshot())
    }

    @Test
    fun `test validateEmail`() {
        // Valid email: "test@email.com"
        assert(fakeReportIssueData.validateEmail())
        // Valid empty email
        fakeReportIssueData.email = " "
        assert(fakeReportIssueData.validateEmail())
        // wrong email format
        fakeReportIssueData.email = "myFakeEmail"
        assertFalse(fakeReportIssueData.validateEmail())
    }

    @Test
    fun `test validateType`() {
        // valid type "false positive"
        assert(fakeReportIssueData.validateType())
        // Invalid type
        fakeReportIssueData.type = "wrong type"
        assertFalse(fakeReportIssueData.validateType())
    }

}