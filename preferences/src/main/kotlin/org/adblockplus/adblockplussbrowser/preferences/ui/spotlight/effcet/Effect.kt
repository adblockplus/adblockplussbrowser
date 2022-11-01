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

package org.adblockplus.adblockplussbrowser.preferences.ui.spotlight.effcet

import android.animation.TimeInterpolator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF

/**
 * Additional effect drawing in loop to Shape.
 */
interface Effect {

    /**
     * [duration] to draw Effect.
     */
    val duration: Long

    /**
     * [interpolator] to draw Effect.
     */
    val interpolator: TimeInterpolator

    /**
     * [repeatMode] to draw Effect.
     */
    val repeatMode: Int

    /**
     * Draw the Effect.
     *
     * @param value the animated value from 0 to 1 and this value is looped until Target finishes.
     */
    fun draw(canvas: Canvas, point: PointF, value: Float, paint: Paint)
}
