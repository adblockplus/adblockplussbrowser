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
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.app.Activity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * Holds all of the [Target]s and [TourGuideView] to show/hide [Target], [TourGuideView] properly.
 * [TourGuideView] can be controlled with [start]/[finish].
 *
 * Once you finish the current [TourGuide] with [finish], you can not start the [TourGuide] again
 * unless you create a new [TourGuide] to start again.
 */
class TourGuide private constructor(
    private val tourGuide: TourGuideView,
    private val target: Target,
    private val duration: Long,
    private val interpolator: TimeInterpolator,
    private val container: ViewGroup,
    private val spotlightListener: TourGuideListener?
) {
    init {
        container.addView(tourGuide, MATCH_PARENT, MATCH_PARENT)
    }

    /**
     * Starts [TourGuideView] and show the first [Target].
     */
    fun start() {
        startSpotlight()
    }

    /**
     * Closes Spotlight and [TourGuideView] will remove all children and be removed from the [container].
     */
    fun finish() {
        finishSpotlight()
    }

    /**
     * Starts Spotlight.
     */
    private fun startSpotlight() {
        tourGuide.startSpotlight(duration, interpolator, object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                spotlightListener?.onStarted()
            }

            override fun onAnimationEnd(animation: Animator) {
                tourGuide.startTarget(target)
                target.listener?.onStarted()
            }
        })
    }

    /**
     * Closes Spotlight.
     */
    private fun finishSpotlight() {
        tourGuide.finishSpotlight(duration, interpolator, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                tourGuide.cleanup()
                container.removeView(tourGuide)
                spotlightListener?.onEnded()
            }
        })
    }

    /**
     * Builder to build [TourGuide].
     * All parameters should be set in this [Builder].
     */
    class Builder(private val activity: Activity) {

        private lateinit var target: Target
        private var duration: Long = DEFAULT_DURATION
        private var interpolator: TimeInterpolator = DEFAULT_ANIMATION

        @ColorInt
        private var backgroundColor: Int = DEFAULT_OVERLAY_COLOR
        private var container: ViewGroup? = null
        private var listener: TourGuideListener? = null

        /**
         * Sets [Target] to show on [TourGuide].
         */
        fun setTarget(target: Target): Builder = apply {
            this.target = target
        }

        /**
         * Sets [backgroundColor] resource on [TourGuide].
         */
        fun setBackgroundColorRes(@ColorRes backgroundColorRes: Int): Builder = apply {
            this.backgroundColor = ContextCompat.getColor(activity, backgroundColorRes)
        }

        /**
         * Sets [TourGuideListener] to notify the state of [TourGuide].
         */
        fun setOnSpotlightListener(listener: TourGuideListener): Builder = apply {
            this.listener = listener
        }

        fun build(): TourGuide {

            val spotlight = TourGuideView(activity, backgroundColor)
            val container = container ?: activity.window.decorView as ViewGroup

            return TourGuide(
                tourGuide = spotlight,
                target = target,
                duration = duration,
                interpolator = interpolator,
                container = container,
                spotlightListener = listener
            )
        }

        companion object {
            private const val DEFAULT_DURATION = 300L
            private val DEFAULT_ANIMATION = DecelerateInterpolator(2f)

            @ColorInt
            private val DEFAULT_OVERLAY_COLOR: Int = 0x6000000
        }
    }
}
