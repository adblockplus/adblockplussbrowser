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

package org.adblockplus.adblockplussbrowser.preferences.ui.spotlight

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
 * Holds all of the [Target]s and [SpotlightView] to show/hide [Target], [SpotlightView] properly.
 * [SpotlightView] can be controlled with [start]/[finish].
 *
 * Once you finish the current [Spotlight] with [finish], you can not start the [Spotlight] again
 * unless you create a new [Spotlight] to start again.
 */
class Spotlight private constructor(
    private val spotlight: SpotlightView,
    private val target: Target,
    private val duration: Long,
    private val interpolator: TimeInterpolator,
    private val container: ViewGroup,
    private val spotlightListener: OnSpotlightListener?
) {
    init {
        container.addView(spotlight, MATCH_PARENT, MATCH_PARENT)
    }

    /**
     * Starts [SpotlightView] and show the first [Target].
     */
    fun start() {
        startSpotlight()
    }

    /**
     * Closes Spotlight and [SpotlightView] will remove all children and be removed from the [container].
     */
    fun finish() {
        finishSpotlight()
    }

    /**
     * Starts Spotlight.
     */
    private fun startSpotlight() {
        spotlight.startSpotlight(duration, interpolator, object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                spotlightListener?.onStarted()
            }

            override fun onAnimationEnd(animation: Animator) {
                spotlight.startTarget(target)
                target.listener?.onStarted()
            }
        })
    }

    /**
     * Closes Spotlight.
     */
    private fun finishSpotlight() {
        spotlight.finishSpotlight(duration, interpolator, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                spotlight.cleanup()
                container.removeView(spotlight)
                spotlightListener?.onEnded()
            }
        })
    }

    /**
     * Builder to build [Spotlight].
     * All parameters should be set in this [Builder].
     */
    class Builder(private val activity: Activity) {

        private lateinit var target: Target
        private var duration: Long = DEFAULT_DURATION
        private var interpolator: TimeInterpolator = DEFAULT_ANIMATION

        @ColorInt
        private var backgroundColor: Int = DEFAULT_OVERLAY_COLOR
        private var container: ViewGroup? = null
        private var listener: OnSpotlightListener? = null

        /**
         * Sets [Target] to show on [Spotlight].
         */
        fun setTarget(target: Target): Builder = apply {
            this.target = target
        }

        /**
         * Sets [backgroundColor] resource on [Spotlight].
         */
        fun setBackgroundColorRes(@ColorRes backgroundColorRes: Int): Builder = apply {
            this.backgroundColor = ContextCompat.getColor(activity, backgroundColorRes)
        }

        /**
         * Sets [OnSpotlightListener] to notify the state of [Spotlight].
         */
        fun setOnSpotlightListener(listener: OnSpotlightListener): Builder = apply {
            this.listener = listener
        }

        fun build(): Spotlight {

            val spotlight = SpotlightView(activity, backgroundColor)
            val container = container ?: activity.window.decorView as ViewGroup

            return Spotlight(
                spotlight = spotlight,
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
