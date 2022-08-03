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

package org.adblockplus.adblockplusbrowser.ui.reporter

import android.app.Application
import android.util.Size
import org.adblockplus.adblockplussbrowser.preferences.ui.reporter.ReportIssueViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class ReportIssueViewModelTest {

    private val reportIssueViewModel = ReportIssueViewModel(Application())

    @Test
    fun `test validateImageSize Portrait`() {
        // Pair(Width, Height)
        val scaledSizes = calculateForSizes(listOf(
            Size(720, 1280), // HD
            Size(960, 1280), // Portrait
            Size(2160, 3840), //UHD
        ))
        // assert correct size after conversion
        assertTrue(scaledSizes.all {
            it.width <= 720 && it.height <= 1280
        })
    }

    @Test
    fun `test validateImageSize Landscape`() {
        // Pair(Width, Height)
        val scaledSizes = calculateForSizes(listOf(
            Size(1280, 720), // HD
            Size(1280, 960), // Landscape
            Size(3840, 2160), //UHD
            Size(3840, 2130)
        ))
        // assert correct size after conversion
        assertTrue(scaledSizes.all {
            it.width <= 1280 && it.height <= 720
        })
    }

    @Test
    fun `test validateImageSize SD`() {
        val newSize = reportIssueViewModel.calculateImageSize(480, 640)
        // assert same ratio after conversion
        assertEquals(3/4, newSize.width/newSize.height)
        // assert correct size after conversion
        assertEquals(480, newSize.width)
        assertEquals(640, newSize.height)
    }

    @Test
    fun `test validateImageSize square image`() {
        val newSize = reportIssueViewModel.calculateImageSize(2160, 2160)
        // assert same ratio after conversion
        assertEquals(1, newSize.width/newSize.height)
        // assert correct size after conversion
        assertEquals(720, newSize.width)
        assertEquals(720, newSize.height)
    }

    private fun calculateForSizes(imageSizes: List<Size>): List<Size> {
        val result: MutableList<Size> = mutableListOf()
        for(p in imageSizes) {
            val newSize = reportIssueViewModel.calculateImageSize(p.width, p.height)
            // assert same ratio after conversion
            assertEquals(p.width/p.height, newSize.width/newSize.height)
            result.add(Size(newSize.width, newSize.height))
        }
        return result
    }
}
