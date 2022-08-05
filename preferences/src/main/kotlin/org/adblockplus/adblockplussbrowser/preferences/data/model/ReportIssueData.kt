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

import android.util.Patterns

data class ReportIssueData(
    var type: String = "",
    var screenshot: String = "",
    var email: String = "",
    var comment: String = "",
    var url: String = ""
) {
    fun validate(): Boolean = validateType() && validateEmail() && validateScreenshot()

    fun validateScreenshot() = screenshot.isNotEmpty()

    fun validateEmail() = (email == REPORT_ISSUE_DATA_VALID_BLANK
                || Patterns.EMAIL_ADDRESS.matcher(email).matches())

    fun validateType() = (type == REPORT_ISSUE_DATA_TYPE_FALSE_POSITIVE
                || type == REPORT_ISSUE_DATA_TYPE_MISSED_AD)

    companion object {
        const val REPORT_ISSUE_DATA_VALID_BLANK = " "
        const val REPORT_ISSUE_DATA_TYPE_FALSE_POSITIVE = "false positive"
        const val REPORT_ISSUE_DATA_TYPE_MISSED_AD = "false negative"
    }
}
