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
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.base.view.layoutInflater
import org.adblockplus.adblockplussbrowser.base.view.setDebounceOnClickListener
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData.Companion.REPORT_ISSUE_DATA_TYPE_FALSE_POSITIVE
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData.Companion.REPORT_ISSUE_DATA_TYPE_MISSED_AD
import org.adblockplus.adblockplussbrowser.preferences.data.model.ReportIssueData.Companion.REPORT_ISSUE_DATA_VALID_BLANK
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentReportIssueBinding
import timber.log.Timber

/**
 * Contains UI for issue report and handling of issue screenshot picking.
 */
@AndroidEntryPoint
internal class ReportIssueFragment :
    DataBindingFragment<FragmentReportIssueBinding>(R.layout.fragment_report_issue) {

    private val viewModel: ReportIssueViewModel by viewModels()
    private lateinit var screenshotPreviewViewGroup: ViewGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenshotPreviewViewGroup = view.findViewById(R.id.screenshot_preview)
    }

    override fun onBindView(binding: FragmentReportIssueBinding) {
        binding.viewModel = viewModel
        val lifecycleOwner = this.viewLifecycleOwner

        handleReportStatus()

        handleScreenshot()

        binding.screenshotPreview.root.setDebounceOnClickListener({
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

    private fun handleReportStatus() {
        viewModel.backgroundOperationOutcome.observe(this) {
            hideProgressBar()
            when (viewModel.backgroundOperationOutcome.value) {
                BackgroundOperationOutcome.SCREENSHOT_PROCESSING_FINISHED -> {
                    validateData()
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

    private fun handleScreenshot() {
        viewModel.screenshotLiveData.observe(this) { screenshotBitmap ->
            with(screenshotPreviewViewGroup) {
                removeAllViews()
                if (screenshotBitmap != null) {
                    inflate(R.layout.image_preview_layout)
                    getImageView(R.id.selected_screenshot_preview).setImageBitmap(screenshotBitmap)
                    getTextView(R.id.selected_screenshot_name).text = viewModel.fileName
                } else {
                    inflate(R.layout.image_placeholder_layout)
                }
            }
        }
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
                screenshotPreviewViewGroup.inflate(R.layout.loading_image_ayout)
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
    }

    companion object {
        const val MANDATORY_MARK = " *"
    }
}

private fun ViewGroup.inflate(layout: Int) = layoutInflater.inflate(layout, this)
private fun ViewGroup.getImageView(id: Int) = this.findViewById<ImageView>(id)
private fun ViewGroup.getTextView(id: Int) = this.findViewById<TextView>(id)
private fun MaterialTextView.addMandatoryMark() {
    this.text = buildSpannedString { append(text).color(Color.RED) { append(ReportIssueFragment.MANDATORY_MARK) } }
}
private fun MaterialTextView.removeMandatoryMark() {
    this.text = this.text.removeSuffix(ReportIssueFragment.MANDATORY_MARK)
}
