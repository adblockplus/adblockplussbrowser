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

package org.adblockplus.adblockplussbrowser.app.databinding

import android.view.View
import androidx.databinding.BindingAdapter
import org.adblockplus.adblockplussbrowser.app.R
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus
import org.adblockplus.adblockplussbrowser.base.widget.SnackbarContainer

@BindingAdapter("updateStatus")
internal fun bindUpdateStatus(snackbarContainer: SnackbarContainer, status: SubscriptionUpdateStatus) {
    when (status) {
        SubscriptionUpdateStatus.Failed -> snackbarContainer.showErrorStatus()
        SubscriptionUpdateStatus.None, SubscriptionUpdateStatus.Success -> snackbarContainer.dismiss()
    }
}

@BindingAdapter("retryAction")
internal fun bindRetryAction(snackbarContainer: SnackbarContainer, listener: View.OnClickListener) {
    snackbarContainer.setActionText(R.string.update_status_retry)
    snackbarContainer.setActionListener(listener)
}

private fun SnackbarContainer.showErrorStatus() {
    setText(R.string.update_status_error_message)
    setTextDrawableStart(R.drawable.ic_baseline_error_outline_24)
    showAction()
    show()
    dismiss(SnackbarContainer.HIDE_DELAY_LONG)
}