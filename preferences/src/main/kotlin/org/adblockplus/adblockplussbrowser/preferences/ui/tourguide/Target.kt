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

import android.view.View

/**
 * Target represents the spot that Spotlight will cast.
 */
class Target(
    val highlightView: View?,
    val overlay: View?,
    val listener: OnTargetListener?
) {

    /**
     * [Builder] to build a [Target].
     * All parameters should be set in this [Builder].
     */
    class Builder {
        private var overlay: View? = null
        private var listener: OnTargetListener? = null
        private var highlightView: View? = null


        fun setHighlightView(highlightView: View): Builder = apply {
            this.highlightView = highlightView
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
            highlightView = highlightView,
            overlay = overlay,
            listener = listener
        )
    }
}
