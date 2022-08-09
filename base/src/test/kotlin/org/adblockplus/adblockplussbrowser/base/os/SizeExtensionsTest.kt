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

package org.adblockplus.adblockplussbrowser.base.os

import android.util.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [SizeShadow::class])
class SizeExtensionsTest {

    @Test
    fun `should correctly downscale portrait sizes`() {
        // Pair(Width, Height)
        val scaledSizes = listOf(
            Size(720, 1280), // HD
            Size(960, 1280), // Portrait
            Size(2160, 3840), //UHD
        ).map { it.downScaleTo(1280, 720)}
        // assert correct size after conversion
        assertTrue(scaledSizes.all {
            it.width <= 720 && it.height <= 1280
        })
    }

    @Test
    fun `should correctly downscale landscape sizes`() {
        // Pair(Width, Height)
        val scaledSizes = listOf(
            Size(1280, 720), // HD
            Size(1280, 960), // Landscape
            Size(3840, 2160), //UHD
            Size(3840, 2130)
        ).map { it.downScaleTo(1280, 720) }
        // assert correct size after conversion
        assertTrue(scaledSizes.all {
            it.width <= 1280 && it.height <= 720
        })
    }

    @Test
    fun `should keep small portrait sizes (480x640) as they are`() {
        val newSize = Size(480, 640).downScaleTo(1280, 720)
        // assert same ratio after conversion
        assertEquals(3/4, newSize.width/newSize.height)
        // assert correct size after conversion
        assertEquals(480, newSize.width)
        assertEquals(640, newSize.height)
    }

    @Test
    fun `should downscale square sized to square`() {
        Size(2160, 2160).downScaleTo(1280, 720).run {
            // assert same ratio after conversion
            assertEquals(1, width/height)
            // assert correct size after conversion
            assertEquals(720, width)
            assertEquals(720, height)
        }
        Size(640, 640).downScaleTo(1280, 720).run {
            // assert same ratio after conversion
            assertEquals(1, width/height)
            // assert correct size after conversion
            assertEquals(640, width)
            assertEquals(640, height)
        }
    }
}
