package org.adblockplus.adblockplussbrowser.app.databinding

import androidx.databinding.BindingAdapter
import org.adblockplus.adblockplussbrowser.app.ui.widget.StatusSnackbar
import org.adblockplus.adblockplussbrowser.base.data.model.UpdateStatus

@BindingAdapter("currentStatus")
internal fun setCurrentStatus(snackbar: StatusSnackbar, status: UpdateStatus) {
    snackbar.setStatus(status)
}