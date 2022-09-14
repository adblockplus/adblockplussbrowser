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

import android.view.View
import android.widget.TextView
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import org.adblockplus.adblockplussbrowser.preferences.BuildConfig
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding
import timber.log.Timber

class SpotlightConfiguration private constructor() {

    companion object {
        private const val TARGET_CORNER_RADIUS = 6f

        /**
         * Configure and return the list of targets for the start guide
         * @param binding FragmentMainPreferencesBinding
         * @param tourDialogLayout View
         */
        fun prepareStartGuideSteps(binding: FragmentMainPreferencesBinding, tourDialogLayout: View): ArrayList<Target> {

            val targets = ArrayList<Target>()

            targets.add(addTargetToSequence(
                binding.mainPreferencesAdBlockingInclude.mainPreferencesAdBlockingCategory,
                tourDialogLayout,
                R.string.tour_dialog_ad_blocking_options_text,
            ))

            targets.add(addTargetToSequence(
                binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions,
                tourDialogLayout,
                R.string.tour_add_languages,
            ))

            targets.add(addTargetToSequence(
                binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions,
                tourDialogLayout,
                R.string.tour_disable_social_media_tracking,
            ))

            if (BuildConfig.FLAVOR_product != BuildConfig.FLAVOR_CRYSTAL) {
                targets.add(addTargetToSequence(
                    binding.mainPreferencesAdBlockingInclude.mainPreferencesAllowlist,
                    tourDialogLayout,
                    R.string.tour_allowlist,
                ))
            }
            targets.add(addLastStepToSequence(tourDialogLayout))
            return targets
        }

        // Add the last target to the spotlight sequence
        private fun addLastStepToSequence(tourDialogLayout: View): Target {
            return Target.Builder()
                .setOverlay(tourDialogLayout)
                .setShape(RoundedRectangle(0f, 0f, 0f))
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                        tourDialogLayout.findViewById<View>(R.id.tour_next_button).visibility = View.GONE
                        tourDialogLayout.findViewById<View>(R.id.tour_skip_button).visibility = View.GONE
                        tourDialogLayout.findViewById<View>(R.id.tour_last_step_done_button).visibility =
                            View.VISIBLE
                        tourDialogLayout.findViewById<TextView>(R.id.tour_dialog_text)
                            .setText(R.string.tour_last_step_description)
                    }

                    override fun onEnded() {
                        Timber.i("Tour end")
                    }
                })
                .build()
        }

        // Add a new target to the spotlight sequence
        private fun addTargetToSequence(highLightView: View, tourDialogLayout: View, resId: Int): Target {
            return Target.Builder()
                .setAnchor(highLightView)
                .setShape(
                    RoundedRectangle(
                        highLightView.height.toFloat(),
                        highLightView.width.toFloat(),
                        TARGET_CORNER_RADIUS
                    )
                ).setOverlay(tourDialogLayout)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                        tourDialogLayout.findViewById<TextView>(R.id.tour_dialog_text).setText(resId)
                    }
                    override fun onEnded() {
                        // This will be executed either when "Next" or "Skipped"
                        Timber.i("Step ended")
                    }
                })
                .build()
        }
    }
}
