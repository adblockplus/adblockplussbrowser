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
import kotlin.math.max
import kotlin.math.min

/**
 * The maximum between [Size.getWidth] and [Size.getHeight]
 */
val Size.longSide: Int
    get() = max(width, height)

/**
 * The minimum between [Size.getWidth] and [Size.getHeight]
 */
val Size.shortSide: Int
    get() = min(width, height)

/**
 * Checks is a rectangle with the given sides can contain another one of this size
 *
 * @param side1 the size of the first side
 * @param side2 the size of the second side
 * @return true if the requirements are satisfied, false otherwise
 */
fun Size.isContainedIn(side1: Int, side2: Int) = this.isContainedIn(Size(side1, side2))

/**
 * Checks if the size is contained in the other one independently of its rotation
 *
 * @param other the Size we want to check if this fit in
 * @return true if the requirements are satisfied, false otherwise
 */
fun Size.isContainedIn(other: Size) = longSide <= other.longSide && shortSide <= other.shortSide

/**
 * Down scales the size to the given boundaries keeping the aspect ratio
 *
 * @param maxLongSide the maximum size for the long side
 * @param maxShortSide the maximum size for the short side
 * @return the scaled [Size], it can have the same width and height if this was already smaller or equal
 */
fun Size.downScaleTo(maxLongSide: Int, maxShortSide: Int): Size {
    // Determine if image is portrait or landscape and assign "shorter side" and "longer side"
    val orientation = if (height > width) Orientation.PORTRAIT else Orientation.LANDSCAPE
    var scaledLongerSide = longSide
    var scaledShorterSide = shortSide

    // Check if the sizes need conversion and scale them accordingly
    if (scaledShorterSide > maxShortSide) {
        val ratio = maxShortSide / scaledShorterSide.toFloat()
        scaledShorterSide = (scaledShorterSide * ratio).toInt()
        scaledLongerSide = (scaledLongerSide * ratio).toInt()
    }
    if (scaledLongerSide > maxLongSide) {
        val ratio = maxLongSide / scaledLongerSide.toFloat()
        scaledShorterSide = (scaledShorterSide * ratio).toInt()
        scaledLongerSide = (scaledLongerSide * ratio).toInt()
    }

    return if (orientation == Orientation.PORTRAIT) {
        Size(scaledShorterSide, scaledLongerSide)
    } else {
        Size(scaledLongerSide, scaledShorterSide)
    }
}

// Destructuring operators to write stuff like val (width, height) = Size(1280, 720)
operator fun Size.component1() = width

operator fun Size.component2() = height

// Just to keep the orientation information
private enum class Orientation {
    PORTRAIT,
    LANDSCAPE
}
