package org.adblockplus.adblockplussbrowser.telemetry.reporters

import okhttp3.Response
internal interface OkHttpReportResultConvertor {
    fun convert(httpResponse: Response): ReportResponse
}