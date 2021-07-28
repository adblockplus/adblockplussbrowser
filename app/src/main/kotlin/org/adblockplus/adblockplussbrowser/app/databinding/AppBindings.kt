package org.adblockplus.adblockplussbrowser.app.databinding

import android.view.View
import androidx.databinding.BindingAdapter
import org.adblockplus.adblockplussbrowser.app.R
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus
import org.adblockplus.adblockplussbrowser.base.widget.SnackbarContainer

@BindingAdapter("updateStatus")
internal fun bindUpdateStatus(snackbarContainer: SnackbarContainer, status: SubscriptionUpdateStatus) {
    when (status) {
        is SubscriptionUpdateStatus.Progress -> snackbarContainer.showProgressStatus(status.progress)
        SubscriptionUpdateStatus.Failed -> snackbarContainer.showErrorStatus()
        SubscriptionUpdateStatus.Success -> snackbarContainer.dismiss(SnackbarContainer.HIDE_DELAY_LONG)
        SubscriptionUpdateStatus.None -> snackbarContainer.dismiss()
    }
}

@BindingAdapter("retryAction")
internal fun bindRetryAction(snackbarContainer: SnackbarContainer, listener: View.OnClickListener) {
    snackbarContainer.setActionText(R.string.update_status_retry)
    snackbarContainer.setActionListener(listener)
}

private fun SnackbarContainer.showProgressStatus(progressValue: Int) {
    showProgress()
    progress = progressValue
    setText(R.string.update_status_progress_message)
    setTextDrawableStart(null)
    hideAction()
    show()
}

private fun SnackbarContainer.showErrorStatus() {
    hideProgress()
    setText(R.string.update_status_error_message)
    setTextDrawableStart(R.drawable.ic_baseline_error_outline_24)
    showAction()
    show()
    dismiss(SnackbarContainer.HIDE_DELAY_LONG)
}