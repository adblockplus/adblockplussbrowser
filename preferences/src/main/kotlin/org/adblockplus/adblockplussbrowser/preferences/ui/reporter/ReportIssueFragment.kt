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
import android.provider.MediaStore
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
internal class ReportIssueFragment :
    DataBindingFragment<FragmentReportIssueBinding>(R.layout.fragment_report_issue) {

    private val viewModel: ReportIssueViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // log issue reporter was opened
        viewModel.logOpenIssueReporter()
        // log issue reporter was canceled
        // Back press on toolbar
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { cancelIssueReporter() }
        // Back press from phone
        requireActivity().onBackPressedDispatcher.addCallback(this) { cancelIssueReporter() }
    }

    @Suppress("LongMethod")
    override fun onBindView(binding: FragmentReportIssueBinding) {
        binding.viewModel = viewModel
        val lifecycleOwner = this.viewLifecycleOwner

        handleReportStatus()

        viewModel.screenshot.observe(this) { bitmap ->
            with(binding.screenshotPreview) {
                screenshot.setImageBitmap(bitmap)
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
            binding.validateData()
        }

        binding.issueTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.blockingTooHigh.id -> viewModel.data.type =
                    REPORT_ISSUE_DATA_TYPE_FALSE_POSITIVE
                binding.blockingTooLow.id -> viewModel.data.type = REPORT_ISSUE_DATA_TYPE_MISSED_AD
            }
            binding.validateData()
        }

        binding.editTextBoxComment.addTextChangedListener {
            viewModel.data.comment = binding.editTextBoxComment.text.toString()
        }

        binding.editTextBoxEmailAddress.addTextChangedListener {
            viewModel.data.email = binding.editTextBoxEmailAddress.text.toString()
            binding.validateData()
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
            // Show progress bar
            binding.indeterminateBar.visibility = View.VISIBLE
            viewModel.sendReport()
        }, lifecycleOwner)

        // Sets the Mandatory Marks for empty default field values
        binding.validateData()
    }

    private fun ImagePreviewLayoutBinding.setProcessingImageIndicatorVisibility(visibility: Int) {
        processingImageIndicator.visibility = visibility
        processingImageBar.visibility = visibility
        loading.visibility = visibility
    }

    private fun handleReportStatus() {
        viewModel.backgroundOperationOutcome.observe(this) {
            // Hide the progress bar
            binding?.indeterminateBar?.visibility = View.GONE
            binding?.screenshotPreview?.processingImageBar?.visibility = View.GONE

            when (viewModel.backgroundOperationOutcome.value) {
                BackgroundOperationOutcome.SCREENSHOT_PROCESSING_FINISHED -> {
                    binding.validateData()
                }
                BackgroundOperationOutcome.REPORT_SEND_SUCCESS -> {
                    val direction =
                        ReportIssueFragmentDirections.actionReportIssueFragmentToMainPreferencesFragment()
                    findNavController().navigate(direction)
                    Timber.d("ReportIssueFragment: Send success")
                }
                BackgroundOperationOutcome.REPORT_SEND_ERROR -> {
                    Timber.d("ReportIssueFragment: Send error")
                }
            }
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
                        viewModel.processImage(unresolvedUri)
                    }
                } else {
                    viewModel.data.screenshot = ""
                    binding.validateData()
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

    private fun cancelIssueReporter() {
        viewModel.logCancelIssueReporter()
        findNavController().popBackStack()
    }

    companion object {
        const val MANDATORY_MARK = " *"
    }
}

private fun FragmentReportIssueBinding?.validateData() {
    this?.run {
        viewModel?.data?.let { data ->
            sendReport.isEnabled = data.validate()
            enterEmailTitle.markMandatoryField(!data.validateEmail())
            selectIssueType.markMandatoryField(!data.validateType())
            pickScreenshotDescription.markMandatoryField(!data.validateScreenshot())
        }
    }
}

private fun MaterialTextView.markMandatoryField(enabled: Boolean) {
    val hasAsterisk = text.endsWith(ReportIssueFragment.MANDATORY_MARK)
    when {
        !hasAsterisk && enabled -> this.addMandatoryMark()
        hasAsterisk && !enabled -> this.removeMandatoryMark()
    }
}

private fun MaterialTextView.addMandatoryMark() {
    text = buildSpannedString { append(text).color(Color.RED) { append(ReportIssueFragment.MANDATORY_MARK) } }
}

private fun MaterialTextView.removeMandatoryMark() {
    text = text.removeSuffix(ReportIssueFragment.MANDATORY_MARK)
}

