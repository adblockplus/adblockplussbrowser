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
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding
import timber.log.Timber

class SpotlightConfiguration private constructor() {

    data class TargetInfo(val highLightView: View?, val resId: Int)

    companion object {
        private const val TARGET_CORNER_RADIUS = 6f

        /**
         * Configure and return the list of targets for the start guide
         * @param binding FragmentMainPreferencesBinding
         * @param context Context
         * @param tourDialogLayout View
         * @param popUpWindow PopupWindow
         */
        fun prepareStartGuideSteps(
            binding: FragmentMainPreferencesBinding, context: Context,
            tourDialogLayout: View, popUpWindow: PopupWindow,
            currentTargetIndex: Int
        ): ArrayList<Target> {
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

            targetInfos.add(
                TargetInfo(
                    binding.mainPreferencesAdBlockingInclude.mainPreferencesAllowlist,
                    R.string.tour_allowlist,
                )
            )

            targetInfos.add(
                TargetInfo(
                    null,
                    R.string.tour_last_step_description,
                )
            )

            targetInfos.subList(currentTargetIndex, targetInfos.size)

            val targets = ArrayList<Target>()
            targetInfos.forEach {
                if (it.highLightView != null) {
                    targets.add(
                        addTargetToSequence(
                            context,
                            tourDialogLayout,
                            popUpWindow,
                            it.highLightView,
                            it.resId
                        )
                    )
                } else {
                    targets.add(addLastStepToSequence(context, tourDialogLayout, popUpWindow, it.resId))
                }
            }

            return targets
        }

        // Add the last target to the spotlight sequence
        private fun addLastStepToSequence(
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

        // Add a new target to the spotlight sequence
        private fun addTargetToSequence(
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
                .build()
        }
    }

    object Constants {
        const val Y_OFFSET = 10
        const val POPUP_WINDOW_HEIGHT = 400
    }
}
