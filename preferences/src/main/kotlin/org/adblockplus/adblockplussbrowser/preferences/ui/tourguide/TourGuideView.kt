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

package org.adblockplus.adblockplussbrowser.preferences.ui.tourguide

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.ColorInt

/**
 * [TourGuideView] starts/finishes [TourGuide], and starts/finishes a current [Target].
 */
internal class TourGuideView constructor(
    context: Context,
    @ColorInt backgroundColor: Int,
) : FrameLayout(context, null, 0) {
    private var rectangle = Rect()

    private val backgroundPaint by lazy {
        Paint().apply { color = backgroundColor }
    }

    private val shapePaint by lazy {
        Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    }

    private var target: Target? = null

    init {
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        target?.highlightView?.let {
            canvas.drawRect(rectangle, shapePaint)
        }
    }

    /**
     * Starts [TourGuide].
     */
    fun startTourGuide(
        duration: Long,
        interpolator: TimeInterpolator,
        listener: Animator.AnimatorListener
    ) {
        val objectAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply {
            setDuration(duration)
            setInterpolator(interpolator)
            addListener(listener)
        }
        objectAnimator.start()
    }

    /**
     * Finishes [TourGuide].
     */
    fun finishTourGuide(
        duration: Long,
        interpolator: TimeInterpolator,
        listener: Animator.AnimatorListener
    ) {
        val objectAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
            setDuration(duration)
            setInterpolator(interpolator)
            addListener(listener)
        }
        objectAnimator.start()
    }

    /**
     * Starts the provided [Target].
     */
    fun startTarget(target: Target) {
        removeAllViews()
        addView(target.overlay, MATCH_PARENT, MATCH_PARENT)
        this.target = target.also {
            // adjust anchor in case where custom container is set.
            val location = IntArray(2)
            target.highlightView?.let {
                it.getLocationInWindow(location)
                rectangle = Rect(
                    location[0],
                    location[1],
                    location[0] + it.width,
                    location[1] + it.height,
                )
            }
        }
    }

    fun cleanup() {
        removeAllViews()
    }
}
