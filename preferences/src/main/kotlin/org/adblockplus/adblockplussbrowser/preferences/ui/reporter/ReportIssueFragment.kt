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

package org.adblockplus.adblockplussbrowser.preferences.ui.reporter

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.opengl.Visibility
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.view.setDebounceOnClickListener
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData.Companion.REPORT_ISSUE_DATA_TYPE_FALSE_POSITIVE
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData.Companion.REPORT_ISSUE_DATA_TYPE_MISSED_AD
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData.Companion.REPORT_ISSUE_DATA_VALID_BLANK
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentReportIssueBinding
import org.adblockplus.adblockplussbrowser.preferences.databinding.ImagePreviewLayoutBinding
import timber.log.Timber

/**
 * Contains UI for issue report and handling of issue screenshot picking.
 */
@AndroidEntryPoint
@Suppress("TooManyFunctions")
internal class ReportIssueFragment :
    DataBindingFragment<FragmentReportIssueBinding>(R.layout.fragment_report_issue) {

    private val viewModel: ReportIssueViewModel by viewModels()

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            viewModel.logCancelIssueReporter()
        }
    }

    @Suppress("LongMethod")
    override fun onBindView(binding: FragmentReportIssueBinding) {
        binding.viewModel = viewModel
        val lifecycleOwner = this.viewLifecycleOwner

        handleReportStatus()

        viewModel.screenshot.observe(this) {
            with(binding.screenshotPreview) {
                screenshot.setImageBitmap(it)
                selectedScreenshotName.text = viewModel.fileName
                selectedScreenshotName.visibility = View.VISIBLE
                setProcessingImageIndicatorVisibility(View.GONE)
                screenshotSelectionDescription.visibility = View.GONE
                imagePlaceholderContainer.visibility = View.VISIBLE
                screenshotReselect.visibility = View.VISIBLE
            }
        }

        binding.screenshotPreview.imagePlaceholderContainer.setDebounceOnClickListener({
            pickImageFromGallery()
        }, lifecycleOwner)

        binding.anonymousSubmissionCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.anonymousSubmissionWarning.visibility = View.VISIBLE
                binding.editTextBoxEmailAddress.isEnabled = false
                binding.editTextBoxEmailAddress.setText("")
                viewModel.data.email = REPORT_ISSUE_DATA_VALID_BLANK
            } else {
                binding.anonymousSubmissionWarning.visibility = View.GONE
                binding.editTextBoxEmailAddress.isEnabled = true
                viewModel.data.email = binding.editTextBoxEmailAddress.text.toString()
            }
            validateData()
        }

        binding.issueTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.blockingTooHigh.id -> viewModel.data.type =
                    REPORT_ISSUE_DATA_TYPE_FALSE_POSITIVE
                binding.blockingTooLow.id -> viewModel.data.type = REPORT_ISSUE_DATA_TYPE_MISSED_AD
            }
            validateData()
        }

        binding.editTextBoxComment.addTextChangedListener {
            viewModel.data.comment = binding.editTextBoxComment.text.toString()
        }

        binding.editTextBoxEmailAddress.addTextChangedListener {
            viewModel.data.email = binding.editTextBoxEmailAddress.text.toString()
            validateData()
        }

        binding.editTextBoxUrl.addTextChangedListener {
            viewModel.data.url = binding.editTextBoxUrl.text.toString()
        }

        binding.cancel.setDebounceOnClickListener({
            viewModel.logCancelIssueReporter()
            val direction = ReportIssueFragmentDirections.actionReportIssueFragmentToMainPreferencesFragment()
            findNavController().navigate(direction)
        }, lifecycleOwner)

        binding.sendReport.setDebounceOnClickListener({
            showProgressBar()
            viewModel.sendReport()
        }, lifecycleOwner)

        // Sets the Mandatory Marks for empty default field values
        validateData()
    }

    private fun ImagePreviewLayoutBinding.setProcessingImageIndicatorVisibility(visibility: Int) {
        processingImageIndicator.visibility = visibility
        processingImageBar.visibility = visibility
        loading.visibility = visibility
    }

    private fun handleReportStatus() {
        viewModel.backgroundOperationOutcome.observe(this) {
            hideProgressBar()
            when (viewModel.backgroundOperationOutcome.value) {
                BackgroundOperationOutcome.SCREENSHOT_READ_SUCCESS -> {
                    validateData()
                    Timber.d("ReportIssue Screenshot read success")
                }
                BackgroundOperationOutcome.SCREENSHOT_READ_ERROR -> {
                    validateData()
                    Timber.d("ReportIssue Screenshot read error")
                }
                BackgroundOperationOutcome.SEND_SUCCESS -> {
                    viewModel.displaySnackbarMessage.value = context?.getString(R.string.issueReporter_report_sent)
                    val direction =
                        ReportIssueFragmentDirections.actionReportIssueFragmentToMainPreferencesFragment()
                    findNavController().navigate(direction)
                    Timber.d("ReportIssueFragment: Send success")
                }
                BackgroundOperationOutcome.SEND_ERROR -> {
                    viewModel.displaySnackbarMessage.value =
                        context?.getString(R.string.issueReporter_report_send_error)
                    Timber.d("ReportIssueFragment: Send error")
                }
            }
        }
    }

    private fun MaterialTextView.addMandatoryMark() {
        this.text = buildSpannedString { append(text).color(Color.RED) { append(MANDATORY_MARK) } }
    }

    private fun MaterialTextView.removeMandatoryMark() {
        this.text = this.text.removeSuffix(MANDATORY_MARK)
    }

    private fun markMandatoryField(textView: MaterialTextView, enabled: Boolean) {
        val hasAsterisk = textView.text.endsWith(MANDATORY_MARK)
        when {
            !hasAsterisk && enabled -> textView.addMandatoryMark()
            hasAsterisk && !enabled -> textView.removeMandatoryMark()
        }
    }

    private val pickImageFromGalleryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding?.screenshotPreview?.setProcessingImageIndicatorVisibility(View.VISIBLE)
                binding?.screenshotPreview?.imagePlaceholderContainer?.visibility = View.GONE
                val intent = result.data
                val unresolvedUri = intent?.data
                if (unresolvedUri != null) {
                    lifecycleScope.launch {
                        viewModel.processImage(unresolvedUri, activity)
                    }
                } else {
                    viewModel.data.screenshot = ""
                    validateData()
                }
            }
        }

    private fun pickImageFromGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        pickImageFromGalleryForResult.launch(pickIntent)
    }

    private fun validateData() {
        binding?.let {
            it.sendReport.isEnabled = viewModel.data.validate()
            markMandatoryField(it.enterEmailTitle, !viewModel.data.validateEmail())
            markMandatoryField(it.selectIssueType, !viewModel.data.validateType())
            markMandatoryField(it.pickScreenshotDescription, !viewModel.data.validateScreenshot())
        }
    }

    private fun showProgressBar() {
        binding?.indeterminateBar?.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding?.indeterminateBar?.visibility = View.GONE
        binding?.screenshotPreview?.processingImageBar?.visibility = View.GONE
    }

    companion object {
        const val MANDATORY_MARK = " *"
    }
}
