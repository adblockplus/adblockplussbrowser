package org.adblockplus.adblockplussbrowser.onboarding.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import org.adblockplus.adblockplussbrowser.onboarding.R

class AcceptableAdsDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialDialog(requireContext()).show {
            customView(viewRes = R.layout.acceptable_ads_explanation, scrollable = true)
        }
}