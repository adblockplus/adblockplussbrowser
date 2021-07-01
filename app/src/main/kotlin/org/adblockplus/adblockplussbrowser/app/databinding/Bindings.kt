package org.adblockplus.adblockplussbrowser.app.databinding

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager

@BindingAdapter("snackBarVisible")
internal fun bindSnackBarVisible(layout: ConstraintLayout, status: SubscriptionsManager.Status) {
    when (status) {
        is SubscriptionsManager.Status.Progress, SubscriptionsManager.Status.Failed -> layout.isVisible = true
        SubscriptionsManager.Status.None, SubscriptionsManager.Status.Success -> layout.isGone = true
    }
}