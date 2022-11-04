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

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import org.adblockplus.adblockplussbrowser.preferences.BuildConfig
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.spotlight.shape.RoundedRectangle
import timber.log.Timber

class SpotlightConfiguration private constructor() {

    data class TargetInfo(val highLightView: View?, val resId: Int)

    companion object {
        private const val TARGET_CORNER_RADIUS = 32f

        /**
         * Configure and return the Spotlight target for the start guide.
         * @param targetInfo TargetInfo describing view to be anchored
         * @param context Context
         * @param tourDialogLayout View
         * @param popUpWindow PopupWindow
         */
        fun createTarget(
            targetInfo: TargetInfo, context: Context,
            tourDialogLayout: View, popUpWindow: PopupWindow,
        ): Target {
            return if (targetInfo.highLightView != null) {
                createTargetWithHighlight(
                    context,
                    tourDialogLayout,
                    popUpWindow,
                    targetInfo.highLightView,
                    targetInfo.resId
                )
            } else {
                createLastTarget(context, tourDialogLayout, popUpWindow, targetInfo.resId)
            }
        }

        /**
         * Create target info bindings and descriptions for every step.
         * @param binding View FragmentMainPreferencesBinding
         */
        fun createTargetInfos(binding: FragmentMainPreferencesBinding): ArrayList<TargetInfo> {
            val targetInfos = ArrayList<TargetInfo>()
            targetInfos.add(
                TargetInfo(
                    binding.mainPreferencesAdBlockingInclude.mainPreferencesAdBlockingCategory,
                    R.string.tour_dialog_ad_blocking_options_text
                )
            )

            targetInfos.add(
                TargetInfo(
                    binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions,
                    R.string.tour_add_languages
                )
            )

            targetInfos.add(
                TargetInfo(
                    binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions,
                    R.string.tour_disable_social_media_tracking
                )
            )

            if (BuildConfig.FLAVOR_product != BuildConfig.FLAVOR_CRYSTAL) {
                targetInfos.add(
                    TargetInfo(
                        binding.mainPreferencesAdBlockingInclude.mainPreferencesAllowlist,
                        R.string.tour_allowlist,
                    )
                )
            }

            targetInfos.add(
                TargetInfo(
                    null,
                    R.string.tour_last_step_description,
                )
            )
            return targetInfos
        }

        /**
         * Create last target with view
         *
         * @param context Context
         * @param tourDialogLayout View with tour dialog layout
         * @param popUpWindow PopupWindow
         * @param resId Resource id with description of the last step
         */
        private fun createLastTarget(
            context: Context,
            tourDialogLayout: View,
            popUpWindow: PopupWindow,
            resId: Int
        ): Target {
            val root = FrameLayout(context)
            return Target.Builder()
                .setOverlay(root)
                .setShape(RoundedRectangle(0f, 0f, 0f))
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                        tourDialogLayout.findViewById<View>(R.id.tour_next_button).visibility = View.GONE
                        tourDialogLayout.findViewById<View>(R.id.tour_skip_button).visibility = View.GONE
                        tourDialogLayout.findViewById<View>(R.id.tour_last_step_done_button).visibility =
                            View.VISIBLE
                        tourDialogLayout.findViewById<TextView>(R.id.tour_dialog_text).setText(resId)
                        popUpWindow.showAtLocation(root, Gravity.CENTER, 0, 0)
                    }

                    override fun onEnded() {
                        Timber.i("Tour end")
                    }
                })
                .build()
        }

        /**
         * Create target with view to be highlighted
         *
         * @param context Context
         * @param tourDialogLayout View with tour dialog layout
         * @param popUpWindow PopupWindow
         * @param highLightView View that is going to be highlighted
         * @param resId Resource id with description for highlighted view
         */
        private fun createTargetWithHighlight(
            context: Context, tourDialogLayout: View, popUpWindow: PopupWindow,
            highLightView: View, resId: Int
        ): Target {
            val root = FrameLayout(context)
            return Target.Builder()
                .setAnchor(highLightView)
                .setShape(
                    RoundedRectangle(
                        highLightView.height.toFloat(),
                        highLightView.width.toFloat(),
                        TARGET_CORNER_RADIUS
                    )
                ).setOverlay(root)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                        tourDialogLayout.findViewById<TextView>(R.id.tour_dialog_text).setText(resId)
                        popUpWindow.showAsDropDown(
                            highLightView,
                            highLightView.width,
                            Constants.Y_OFFSET,
                        )
                    }

                    override fun onEnded() {
                        // This will be executed either when "Next" or "Skipped"
                        Timber.i("Step ended")
                    }
                })
                .setHighlightView(highLightView)
                .build()
        }
    }

    object Constants {
        const val Y_OFFSET = 10
        const val ANIMATION_DURATION = 300L
        const val POPUP_WINDOW_HEIGHT = 400
    }
}
