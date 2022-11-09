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
import android.app.Activity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * Holds all of the [Target]s and [TourGuideView] to show/hide [Target], [TourGuideView] properly.
 */
private const val ANIMATION_DURATION = 300L

@ColorInt
private const val DEFAULT_OVERLAY_COLOR: Int = 0x6000000
private const val ANIMATION_FACTOR = 2F

class TourGuide private constructor(
    private val tourGuide: TourGuideView,
    private val target: Target,
    private val container: ViewGroup,
    private val tourGuideListener: TourGuideListener?
) {
    init {
        container.addView(tourGuide, MATCH_PARENT, MATCH_PARENT)
    }

    /**
     * Starts [TourGuideView] and show the first [Target].
     */
    fun start() {
        startTourGuide()
    }

    /**
     * Closes tour guide and [TourGuideView] will remove all children and be removed from the [container].
     */
    fun finish() {
        finishTourGuide()
    }

    /**
     * Starts tour guide.
     */
    private fun startTourGuide() {
        tourGuide.startTourGuide(
            ANIMATION_DURATION,
            AccelerateInterpolator(ANIMATION_FACTOR),
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    tourGuideListener?.onStarted()
                }

                override fun onAnimationEnd(animation: Animator) {
                    tourGuide.startTarget(target)
                    target.listener?.onStarted()
                }
            })
    }

    /**
     * Closes tour guide.
     */
    private fun finishTourGuide() {
        tourGuide.finishTourGuide(
            ANIMATION_DURATION,
            DecelerateInterpolator(ANIMATION_FACTOR),
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    tourGuide.cleanup()
                    container.removeView(tourGuide)
                    tourGuideListener?.onEnded()
                }
            })
    }

    /**
     * Builder to build [TourGuide].
     * All parameters should be set in this [Builder].
     */
    class Builder(private val activity: Activity) {

        private lateinit var target: Target

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
        fun setOnTourGuideListener(listener: TourGuideListener): Builder = apply {
            this.listener = listener
        }

        fun build(): TourGuide {

            val tourGuide = TourGuideView(activity, backgroundColor)
            val container = container ?: activity.window.decorView as ViewGroup

            return TourGuide(
                tourGuide = tourGuide,
                target = target,
                container = container,
                tourGuideListener = listener
            )
        }
    }
}
