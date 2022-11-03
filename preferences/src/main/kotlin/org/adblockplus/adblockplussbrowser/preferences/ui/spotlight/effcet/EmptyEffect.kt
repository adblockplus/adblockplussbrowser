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

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.view.animation.LinearInterpolator
import java.util.concurrent.TimeUnit

/**
 * [Effect] that does not do anything.
 */
class EmptyEffect @JvmOverloads constructor(
    override val duration: Long = DEFAULT_DURATION,
    override val interpolator: TimeInterpolator = DEFAULT_INTERPOLATOR,
    override val repeatMode: Int = DEFAULT_REPEAT_MODE
) : Effect {

    override fun draw(canvas: Canvas, point: PointF, value: Float, paint: Paint) = Unit

    companion object {

        val DEFAULT_DURATION = TimeUnit.MILLISECONDS.toMillis(0)

        val DEFAULT_INTERPOLATOR = LinearInterpolator()

        const val DEFAULT_REPEAT_MODE = ObjectAnimator.REVERSE
    }
}
