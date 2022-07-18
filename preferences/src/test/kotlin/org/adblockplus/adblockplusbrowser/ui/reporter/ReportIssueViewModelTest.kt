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
import org.adblockplus.adblockplussbrowser.preferences.ui.reporter.ReportIssueViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportIssueViewModelTest {

    private val reportIssueViewModel = ReportIssueViewModel(Application())

    @Test
    fun `test validateImageSize Portrait`() {
        // Pair(Width, Height)
        val scaledSizes = calculateForSizes(listOf(
            Pair(720, 1280), // HD
            Pair(960, 1280), // Portrait
            Pair(2160, 3840), //UHD
        ))
        // assert correct size after conversion
        assertTrue(scaledSizes.all {
            it.first <= 720 && it.second <= 1280
        })
    }

    @Test
    fun `test validateImageSize Landscape`() {
        // Pair(Width, Height)
        val scaledSizes = calculateForSizes(listOf(
            Pair(1280, 720), // HD
            Pair(1280, 960), // Landscape
            Pair(3840, 2160) //UHD
        ))
        // assert correct size after conversion
        assertTrue(scaledSizes.all {
            it.first <= 1280 && it.second <= 720
        })
    }

    @Test
    fun `test validateImageSize SD`() {
        val (scaledWidth, scaledHeight) = reportIssueViewModel.validateImageSize(480, 640)
        // assert same ratio after conversion
        assertEquals(3/4, scaledWidth/scaledHeight)
        // assert correct size after conversion
        assertEquals(480, scaledWidth)
        assertEquals(640, scaledHeight)
    }

    @Test
    fun `test validateImageSize square image`() {
        val (scaledWidth, scaledHeight) = reportIssueViewModel.validateImageSize(2160, 2160)
        // assert same ratio after conversion
        assertEquals(1, scaledWidth/scaledHeight)
        // assert correct size after conversion
        assertEquals(720, scaledWidth)
        assertEquals(720, scaledHeight)
    }

    private fun calculateForSizes(imageSizes: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
        val result: MutableList<Pair<Int, Int>> = mutableListOf()
        for(p in imageSizes) {
            val (scaledWidth, scaledHeight) = reportIssueViewModel.validateImageSize(p.first, p.second)
            // assert same ratio after conversion
            assertEquals(p.first/p.second, scaledWidth/scaledHeight)
            result.add(Pair(scaledWidth, scaledHeight))
        }
        return result
    }
}