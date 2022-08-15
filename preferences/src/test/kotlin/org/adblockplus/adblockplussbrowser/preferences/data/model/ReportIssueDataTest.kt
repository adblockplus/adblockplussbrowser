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

package org.adblockplus.adblockplussbrowser.preferences.data.model

import org.adblockplus.adblockplussbrowser.preferences.helpers.Fakes
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReportIssueDataTest {

    private val fakeReportIssueData = Fakes.fakeReportIssueData

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