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

package org.adblockplus.adblockplussbrowser.preferences.ui

import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.material.checkbox.MaterialCheckBox
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.view.setDebounceOnClickListener
import org.adblockplus.adblockplussbrowser.preferences.BuildConfig
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.updates.UpdateSubscriptionsViewModel
import timber.log.Timber


@AndroidEntryPoint
internal class MainPreferencesFragment :
    DataBindingFragment<FragmentMainPreferencesBinding>(R.layout.fragment_main_preferences) {

    private val viewModel: MainPreferencesViewModel by activityViewModels()

    // Lazy loading the UpdateSubscriptionsViewModel so it will only be used for crystal flavor here
    private val updateViewModel: UpdateSubscriptionsViewModel by activityViewModels()

    /*
        Use a global targets list to be able to handle at what point the start guide was skipped.
        The list contains pairs of Int and Target.
        The int value represents weather the target was seen (1) or not(0)
    */
    private val startGuideTargets: MutableList<CustomTargetWithStatus> = mutableListOf()

    override fun onBindView(binding: FragmentMainPreferencesBinding) {
        binding.viewModel = viewModel
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        supportActionBar?.subtitle = getString(R.string.app_subtitle)

        val lifecycleOwner = this.viewLifecycleOwner

        bindPrimarySubscriptions(binding, supportActionBar, lifecycleOwner)
        bindOtherSubscriptions(binding, supportActionBar, lifecycleOwner)
        bindAllowList(binding, supportActionBar, lifecycleOwner)

        if (BuildConfig.FLAVOR_product != BuildConfig.FLAVOR_CRYSTAL) {
            bindUpdateSubscriptions(binding, supportActionBar, lifecycleOwner)
        } else {
            bindCrystalUpdateTypeSettings(binding)
        }

        bindAdditionalLanguage(binding, supportActionBar, lifecycleOwner)
        bindOnboardingLanguages(binding, lifecycleOwner)
        bindAcceptableAds(binding, supportActionBar, lifecycleOwner)
        bindGuide(binding, lifecycleOwner)
        bindAbout(binding, supportActionBar, lifecycleOwner)

        if (BuildConfig.FLAVOR_product == BuildConfig.FLAVOR_ABP) {
            binding.mainPreferencesShareEventsInclude.mainPreferencesIssueReporterCategory.setDebounceOnClickListener({
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToReportIssueFragment()
                findNavController().navigate(direction)
            }, lifecycleOwner)
        } else {
            binding.mainPreferencesShareEventsInclude.mainPreferencesIssueReporterCategory.visibility = View.GONE
            binding.mainPreferencesShareEventsInclude.mainPreferencesDivider1.visibility = View.GONE
        }
    }

    private fun bindAbout(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesAboutInclude.mainPreferencesAbout.setDebounceOnClickListener({
            supportActionBar?.subtitle = null
            val direction = MainPreferencesFragmentDirections
                .actionMainPreferencesFragmentToAboutFragment()
            findNavController().navigate(direction)
        }, lifecycleOwner)
    }

    private fun bindAcceptableAds(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesAcceptableAdsInclude.mainPreferencesAcceptableAds.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToAcceptableAdsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
    }

    private fun bindOnboardingLanguages(
        binding: FragmentMainPreferencesBinding,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionSkip
            .setDebounceOnClickListener(
                {
                    viewModel.markLanguagesOnboardingComplete(false)
                },
                lifecycleOwner
            )
    }

    private fun bindAdditionalLanguage(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesLanguagesOnboardingInclude.mainPreferencesLanguagesOnboardingOptionAdd
            .setDebounceOnClickListener(
                {
                    supportActionBar?.subtitle = null
                    viewModel.markLanguagesOnboardingComplete(true)
                    val direction = MainPreferencesFragmentDirections
                        .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
                    findNavController().navigate(direction)
                },
                lifecycleOwner
            )
    }

    private fun bindCrystalUpdateTypeSettings(binding: FragmentMainPreferencesBinding) {
        val rootView = binding.mainPreferencesAdBlockingInclude.root
        val wifiOnlyCheckbox: MaterialCheckBox? = rootView.findViewById(R.id.wifi_only_checkbox)
        val crystalMainPreferencesUpdateSubscriptions =
            rootView.findViewById<ConstraintLayout>(R.id.crystal_main_preferences_update_subscriptions)
        crystalMainPreferencesUpdateSubscriptions?.visibility =
            View.VISIBLE
        crystalMainPreferencesUpdateSubscriptions?.setOnClickListener {
            if (wifiOnlyCheckbox != null) {
                wifiOnlyCheckbox.isChecked = !wifiOnlyCheckbox.isChecked
            }
            val updateConfigType = if (wifiOnlyCheckbox?.isChecked == true) {
                UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_ALWAYS
            } else {
                UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_WIFI_ONLY
            }
            updateViewModel.setUpdateConfigType(updateConfigType)
        }

        updateViewModel.updateType.observe(this) { updateType ->
            if (wifiOnlyCheckbox != null) {
                wifiOnlyCheckbox.isChecked =
                    updateType.name == UpdateSubscriptionsViewModel.UpdateConfigType.UPDATE_ALWAYS.name
            }
        }
    }

    private fun bindUpdateSubscriptions(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.visibility =
            View.VISIBLE
        binding.mainPreferencesAdBlockingInclude.mainPreferencesUpdateSubscriptions.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToUpdateSubscriptionsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
    }

    private fun bindOtherSubscriptions(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToOtherSubscriptionsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
    }

    private fun bindAllowList(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        if (BuildConfig.FLAVOR_product != BuildConfig.FLAVOR_CRYSTAL) {
            binding.mainPreferencesAdBlockingInclude.root.findViewById<LinearLayout>(R.id.main_preferences_allowlist)
                ?.setDebounceOnClickListener(
                    {
                        supportActionBar?.subtitle = null
                        val direction = MainPreferencesFragmentDirections
                            .actionMainPreferencesFragmentToAllowlistFragment()
                        findNavController().navigate(direction)
                    },
                    lifecycleOwner
                )
        }
    }

    private fun bindPrimarySubscriptions(
        binding: FragmentMainPreferencesBinding,
        supportActionBar: ActionBar?,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions.setDebounceOnClickListener(
            {
                supportActionBar?.subtitle = null
                val direction = MainPreferencesFragmentDirections
                    .actionMainPreferencesFragmentToPrimarySubscriptionsFragment()
                findNavController().navigate(direction)
            },
            lifecycleOwner
        )
    }

    private fun bindGuide(
        binding: FragmentMainPreferencesBinding,
        lifecycleOwner: LifecycleOwner
    ) {
        binding.mainPreferencesGuideInclude.mainPreferencesGuideInclude.setDebounceOnClickListener(
            {
                viewModel.logStartGuideStarted()
                /*
                    When starting map all the seen values to 0.
                    Specially for when the user has already started the tour before
                */
//                startGuideTargets.map { it.seen = 0 }

                // Prepare start guide steps
                val overlayRoot = FrameLayout(requireContext())
                val tourDialogLayout = layoutInflater.inflate(R.layout.tour_dialog, overlayRoot)
                val allowlistView = binding.mainPreferencesAdBlockingInclude.preferencesAllowlistTitleText
                val disableSocialMediaView =
                    binding.mainPreferencesAdBlockingInclude.mainPreferencesOtherSubscriptions
                if (BuildConfig.FLAVOR_product == BuildConfig.FLAVOR_CRYSTAL) {
                    binding.mainPreferencesScroll.scrollTo(0, disableSocialMediaView.y.toInt())
                } else {
                    binding.mainPreferencesScroll.scrollTo(0, allowlistView.y.toInt())
                }
                prepareStartGuideSteps(binding, tourDialogLayout, disableSocialMediaView)

                // Create spotlight
                val spotlight = Spotlight.Builder(requireActivity())
                    .setTargets(startGuideTargets.map { it.target })
                    .setBackgroundColorRes(R.color.spotlight_background)
                    .setOnSpotlightListener(object : OnSpotlightListener {
                        override fun onStarted() {
                            Timber.i("Spotlight started")
                        }

                        override fun onEnded() {
                            Timber.i("Spotlight ended")
                        }
                    })
                    .build()
                setClickListeners(spotlight, tourDialogLayout)
                spotlight.start()
            },
            lifecycleOwner
        )
    }

    private fun prepareStartGuideSteps(
        binding: FragmentMainPreferencesBinding,
        tourDialogLayout: View,
        disableSocialMediaView: View) {

        addTargetToSequence(
            binding.mainPreferencesAdBlockingInclude.mainPreferencesAdBlockingCategory,
            tourDialogLayout,
            R.string.tour_dialog_ad_blocking_options_text
        )

        addTargetToSequence(
            binding.mainPreferencesAdBlockingInclude.mainPreferencesPrimarySubscriptions,
            tourDialogLayout,
            R.string.tour_add_languages
        )

        addTargetToSequence(
            disableSocialMediaView,
            tourDialogLayout,
            R.string.tour_disable_social_media_tracking
        )

        if (BuildConfig.FLAVOR_product != BuildConfig.FLAVOR_CRYSTAL) {
            addTargetToSequence(
                binding.mainPreferencesAdBlockingInclude.mainPreferencesAllowlist,
                tourDialogLayout,
                R.string.tour_allowlist
            )
        }
        addLastStepToSequence(tourDialogLayout)
    }

    private fun addLastStepToSequence(tourDialogLayout: View) {
        val target = Target.Builder()
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
                    viewModel.logStartGuideCompleted()
                }
            })
            .build()
        startGuideTargets.add(CustomTargetWithStatus(target))
    }

    private fun setClickListeners(spotlight: Spotlight, tourDialogLayout: View) {
        val nextTarget = View.OnClickListener {
            startGuideTargets.find { it.seen == 0 }?.seen = 1
            spotlight.next()
        }

        // If the user clicks outside the dialog, we stop the start guide
        tourDialogLayout.findViewById<View>(R.id.tour_layout).setOnClickListener {
            skipTour()
            spotlight.close()
        }

        // Setting clickable to false doesn't effect clickability of those views thus we are swallowing click events
        tourDialogLayout.findViewById<View>(R.id.tour_dialog_text).setOnClickListener {
            Timber.i("Mute on purpose")
        }
        tourDialogLayout.findViewById<View>(R.id.tour_dialog_layout).setOnClickListener {
            Timber.i("Mute on purpose")
        }

        tourDialogLayout.findViewById<View>(R.id.tour_next_button).setOnClickListener(nextTarget)
        tourDialogLayout.findViewById<View>(R.id.tour_skip_button).setOnClickListener {
            skipTour()
            spotlight.close()
        }
        tourDialogLayout.findViewById<View>(R.id.tour_last_step_done_button).setOnClickListener{
            spotlight.close()
        }
    }

    private fun addTargetToSequence(
        highLightView: View,
        tourDialogLayout: View,
        resId: Int
    ) {
        val target = Target.Builder()
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
                override fun onEnded() {}
            })
            .build()
        startGuideTargets.add(CustomTargetWithStatus(target))
    }

    private fun skipTour() {
        val skippedAt = startGuideTargets.indexOf(startGuideTargets.find { it.seen == 0 }) + 1
        viewModel.logStartGuideSkipped(step = skippedAt)
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLanguagesOnboarding()
    }

    private companion object {
        private const val TARGET_CORNER_RADIUS = 6f
    }
}

private fun Spotlight.close() = this.finish()

private class CustomTargetWithStatus(
    val target: Target,
    var seen: Int = 0
)
