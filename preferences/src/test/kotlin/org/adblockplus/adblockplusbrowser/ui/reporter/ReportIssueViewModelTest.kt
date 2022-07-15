package org.adblockplus.adblockplusbrowser.ui.reporter

import android.app.Application
import org.adblockplus.adblockplussbrowser.preferences.ui.reporter.ReportIssueViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportIssueViewModelTest {

    @Test
    fun `test validateImageSize Portrait`() {
        val imageSizes = listOf(
            Pair(720, 1280), // HD
            Pair(960, 1280), // Portrait
            Pair(2160, 3840), //UHD
            Pair(2160, 2160), //UHD
            Pair(480, 640) // SD
        )

        for(p in imageSizes) {
            val (scaledWidth, scaledHeight) = ReportIssueViewModel(Application()).validateImageSize(p.first, p.second)
            assertEquals(p.first/p.second, scaledWidth/scaledHeight)
            assertTrue(scaledWidth <= 720)
            assertTrue(scaledHeight <= 1280)
        }

    }
}