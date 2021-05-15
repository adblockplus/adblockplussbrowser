package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions.PrimarySubscriptionsAdapter
import org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions.PrimarySubscriptionsItem

@BindingAdapter("otherSubscriptions")
internal fun bindOtherSubscriptions(recyclerView: RecyclerView, items: List<PrimarySubscriptionsItem>) {
    (recyclerView.adapter as PrimarySubscriptionsAdapter).submitList(items)
}