package org.adblockplus.adblockplussbrowser.preferences.helpers

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData
import org.mockito.Mockito

class Fakes {

    internal val fakeReportIssueData = ReportIssueData(
        type = "false positive",
        email = "test@email.com",
        comment = "test request",
        url = "www.example.com"
    )
}