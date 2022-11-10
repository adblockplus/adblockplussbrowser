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

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.material.checkbox.MaterialCheckBox
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.view.setDebounceOnClickListener
import org.adblockplus.adblockplussbrowser.base.widget.LockableScrollView
import org.adblockplus.adblockplussbrowser.preferences.BuildConfig
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.tourguide.TargetInfo
import org.adblockplus.adblockplussbrowser.preferences.ui.tourguide.TourGuide
import org.adblockplus.adblockplussbrowser.preferences.ui.tourguide.TourGuideConfiguration
import org.adblockplus.adblockplussbrowser.preferences.ui.tourguide.TourGuideConfiguration.createTargetInfos
import org.adblockplus.adblockplussbrowser.preferences.ui.tourguide.TourGuideListener
import org.adblockplus.adblockplussbrowser.preferences.ui.updates.UpdateSubscriptionsViewModel
import timber.log.Timber


@AndroidEntryPoint
internal class MainPreferencesFragment :
    DataBindingFragment<FragmentMainPreferencesBinding>(R.layout.fragment_main_preferences) {

    private val viewModel: MainPreferencesViewModel by activityViewModels()
    private val updateViewModel: UpdateSubscriptionsViewModel by activityViewModels()
    private lateinit var targetInfos: ArrayList<TargetInfo>
    private lateinit var tourGuide: TourGuide
    private lateinit var popupWindow: PopupWindow

    override fun onBindView(binding: FragmentMainPreferencesBinding) {
        binding.viewModel = viewModel
        lifecycle.addObserver(lifecycleEventObserver)
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
        bindAbout(binding, supportActionBar, lifecycleOwner)

        if (BuildConfig.FLAVOR_product == BuildConfig.FLAVOR_ABP) {
            binding.mainPreferencesShareEventsInclude.mainPreferencesIssueReporterCategory.setDebounceOnClickListener(
                {
                    supportActionBar?.subtitle = null
                    val direction = MainPreferencesFragmentDirections
                        .actionMainPreferencesFragmentToReportIssueFragment()
                    findNavController().navigate(direction)
                },
                lifecycleOwner
            )
        } else {
            binding.mainPreferencesShareEventsInclude.mainPreferencesIssueReporterCategory.visibility =
                View.GONE
            binding.mainPreferencesShareEventsInclude.mainPreferencesDivider1.visibility = View.GONE
        }

        bindGuide(binding, lifecycleOwner)
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
                viewModel.isTourStarted = true
                startGuide(binding)
            },
            lifecycleOwner
        )
    }

    private fun startGuide(binding: FragmentMainPreferencesBinding) {
        viewModel.logStartGuideStarted()

        // Prepare start guide steps
        val overlayRoot = FrameLayout(requireContext())
        val tourDialogLayout = layoutInflater.inflate(R.layout.tour_dialog, overlayRoot)
        val mainPreferencesScroll = binding.mainPreferencesScroll
        targetInfos = createTargetInfos(binding)
        scrollToHighlightedView(mainPreferencesScroll)

        popupWindow = PopupWindow(
            tourDialogLayout,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            TourGuideConfiguration.Constants.POPUP_WINDOW_HEIGHT
        )
        popupWindow.isOutsideTouchable = true

        val target = TourGuideConfiguration.createTarget(
            targetInfos[viewModel.currentTargetIndex],
            requireContext(),
            tourDialogLayout,
            popupWindow
        )

        createTourGuide(target, binding, tourDialogLayout, popupWindow, mainPreferencesScroll)

    }

    private fun createTourGuide(
        target: org.adblockplus.adblockplussbrowser.preferences.ui.tourguide.Target,
        binding: FragmentMainPreferencesBinding,
        tourDialogLayout: View,
        popupWindow: PopupWindow,
        mainPreferencesScroll: LockableScrollView,
    ) {
        tourGuide = TourGuide.Builder(requireActivity())
            .setTarget(target)
            .setBackgroundColorRes(R.color.tour_guide_background)
            .setOnTourGuideListener(object : TourGuideListener {
                override fun onStarted() {
                    Timber.i("Tour guide started")
                    binding.mainPreferencesScroll.setScrollable(false)
                }

                override fun onEnded() {
                    Timber.i("Tour guide ended")
                    binding.mainPreferencesScroll.setScrollable(true)
                }
            }).build()
        setClickListeners(binding, tourGuide, tourDialogLayout, popupWindow, mainPreferencesScroll)
        tourGuide.start()
    }

    private fun setClickListeners(
        binding: FragmentMainPreferencesBinding,
        tourGuide: TourGuide,
        tourDialogLayout: View,
        popupWindow: PopupWindow,
        mainPreferencesScroll: LockableScrollView,
    ) {

        // If the user clicks outside the dialog, we stop the start guide
        popupWindow.setTouchInterceptor { v, event ->
            v.performClick()
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                skipTour()
                tourGuide.finish()
            }
            false
        }

        // Setting clickable to false doesn't effect clickability of those views thus we are swallowing click events
        tourDialogLayout.findViewById<View>(R.id.tour_dialog_text).setOnClickListener {
            Timber.i("Mute on purpose")
        }
        tourDialogLayout.findViewById<View>(R.id.tour_layout).setOnClickListener {
            Timber.i("Mute on purpose")
        }

        tourDialogLayout.findViewById<View>(R.id.tour_next_button).setOnClickListener {
            viewModel.currentTargetIndex++
            Timber.i("viewModel.currentTargetIndex: ${viewModel.currentTargetIndex}")
            popupWindow.dismiss()
            tourGuide.finish()
            scrollToHighlightedView(mainPreferencesScroll)
            val target = TourGuideConfiguration.createTarget(
                targetInfos[viewModel.currentTargetIndex],
                requireContext(),
                tourDialogLayout,
                popupWindow
            )
            createTourGuide(target, binding, tourDialogLayout, popupWindow, mainPreferencesScroll)
        }

        tourDialogLayout.findViewById<View>(R.id.tour_skip_button).setOnClickListener {
            skipTour()
            popupWindow.dismiss()
            tourGuide.finish()
        }
        tourDialogLayout.findViewById<View>(R.id.tour_last_step_done_button).setOnClickListener {
            viewModel.logStartGuideCompleted()
            viewModel.currentTargetIndex = 0
            viewModel.isTourStarted = false
            popupWindow.dismiss()
            tourGuide.finish()
        }
    }

    private fun scrollToHighlightedView(mainPreferencesScroll: LockableScrollView) {
        val highLightView = targetInfos[viewModel.currentTargetIndex].highLightView
        highLightView?.let {
            mainPreferencesScroll.scrollTo(
                0,
                highLightView.y.toInt()
            )
        }
    }

    private fun skipTour() {
        viewModel.currentTargetIndex = 0
        val skippedAt = viewModel.currentTargetIndex + 1
        if (skippedAt == targetInfos.size) {
            /* If the user clicks outside the dialog in the last step he went through the whole guide,
            so we can assume the guide is completed */
            viewModel.logStartGuideCompleted()
        } else {
            viewModel.isTourStarted = false
            viewModel.logStartGuideSkipped(step = skippedAt)
        }
    }

    private val lifecycleEventObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            viewModel.checkLanguagesOnboarding()
            if (viewModel.isTourStarted) {
                startGuide(binding!!)
            }
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            if (viewModel.isTourStarted) {
                popupWindow.dismiss()
                tourGuide.finish()
            }
        }
    }
}
