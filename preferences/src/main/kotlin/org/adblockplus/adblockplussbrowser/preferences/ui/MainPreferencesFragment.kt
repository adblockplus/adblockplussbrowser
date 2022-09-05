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
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import dagger.hilt.android.AndroidEntryPoint
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
                Timber.i("start guide")
                val targets = ArrayList<Target>()
                val adBlockingOptions = binding.mainPreferencesAdBlockingInclude.mainPreferencesAdBlockingCategory
                binding.mainPreferencesScroll.scrollTo(0, adBlockingOptions.y.toInt())
                val tourDialogLayout = layoutInflater.inflate(R.layout.tour_dialog, FrameLayout(requireContext()))

                val firstTarget =
                    createTourTarget(adBlockingOptions, tourDialogLayout, R.string.tour_dialog_ad_blocking_options_text)
                targets.add(firstTarget)
                // create spotlight
                val spotlight = Spotlight.Builder(requireActivity())
                    .setTargets(targets)
                    .setBackgroundColorRes(R.color.spotlight_background)
                    .setDuration(TOUR_ANIMATION_DURATION)
                    .setAnimation(DecelerateInterpolator(TOUR_ANIMATION_DECELERATE_FACTOR))
                    .setOnSpotlightListener(object : OnSpotlightListener {
                        override fun onStarted() {
                            Timber.i("Spotlight started")
                        }

                        override fun onEnded() {
                            Timber.i("Spotlight ended")
                        }
                    })
                    .build()

                spotlight.start()

                val nextTarget = View.OnClickListener { spotlight.next() }

                val closeSpotlight = View.OnClickListener { spotlight.finish() }
                tourDialogLayout.findViewById<View>(R.id.tour_next_button).setOnClickListener(nextTarget)
                tourDialogLayout.findViewById<View>(R.id.tour_skip_button).setOnClickListener(closeSpotlight)
            },
            lifecycleOwner
        )
    }

    private fun createTourTarget(
        adBlockingOptions: MaterialTextView,
        tourDialogLayout: View,
        resId: Int
    ): Target {
        val firstTarget = Target.Builder()
            .setAnchor(adBlockingOptions)
            .setShape(
                RoundedRectangle(
                    adBlockingOptions.height.toFloat(),
                    adBlockingOptions.width.toFloat(),
                    TARGET_CORNER_RADIUS
                )
            ).setOverlay(tourDialogLayout)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    tourDialogLayout.findViewById<TextView>(R.id.tour_dialog_text).setText(resId)
                }

                override fun onEnded() {
                    Toast.makeText(
                        requireContext(),
                        "first target is passed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .build()
        return firstTarget
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLanguagesOnboarding()
    }

    private companion object {
        private const val TARGET_CORNER_RADIUS = 6f
        private const val TOUR_ANIMATION_DECELERATE_FACTOR = 2F
        private const val TOUR_ANIMATION_DURATION = 500L
    }
}
