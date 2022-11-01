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

import android.graphics.PointF
import android.view.View
import org.adblockplus.adblockplussbrowser.preferences.ui.spotlight.effcet.Effect
import org.adblockplus.adblockplussbrowser.preferences.ui.spotlight.effcet.EmptyEffect
import org.adblockplus.adblockplussbrowser.preferences.ui.spotlight.shape.Circle
import org.adblockplus.adblockplussbrowser.preferences.ui.spotlight.shape.Shape

/**
 * Target represents the spot that Spotlight will cast.
 */
class Target(
    val anchor: PointF,
    val shape: Shape,
    val effect: Effect,
    val overlay: View?,
    val listener: OnTargetListener?
) {

    /**
     * [Builder] to build a [Target].
     * All parameters should be set in this [Builder].
     */
    class Builder {

        private var anchor: PointF = DEFAULT_ANCHOR
        private var shape: Shape = DEFAULT_SHAPE
        private var effect: Effect = DEFAULT_EFFECT
        private var overlay: View? = null
        private var listener: OnTargetListener? = null

        /**
         * Sets a pointer to start a [Target].
         */
        fun setAnchor(view: View): Builder = apply {
            val location = IntArray(2)
            view.getLocationInWindow(location)
            val x = location[0] + view.width / 2f
            val y = location[1] + view.height / 2f
            setAnchor(x, y)
        }

        /**
         * Sets an anchor point to start [Target].
         */
        private fun setAnchor(x: Float, y: Float): Builder = apply {
            setAnchor(PointF(x, y))
        }

        /**
         * Sets an anchor point to start [Target].
         */
        private fun setAnchor(anchor: PointF): Builder = apply {
            this.anchor = anchor
        }

        /**
         * Sets [shape] of the spot of [Target].
         */
        fun setShape(shape: Shape): Builder = apply {
            this.shape = shape
        }

        /**
         * Sets [overlay] to be laid out to describe [Target].
         */
        fun setOverlay(overlay: View): Builder = apply {
            this.overlay = overlay
        }

        /**
         * Sets [OnTargetListener] to notify the state of [Target].
         */
        fun setOnTargetListener(listener: OnTargetListener): Builder = apply {
            this.listener = listener
        }

        fun build() = Target(
            anchor = anchor,
            shape = shape,
            effect = effect,
            overlay = overlay,
            listener = listener
        )

        companion object {

            private val DEFAULT_ANCHOR = PointF(0f, 0f)

            private val DEFAULT_SHAPE = Circle(100f)

            private val DEFAULT_EFFECT = EmptyEffect()
        }
    }
}
