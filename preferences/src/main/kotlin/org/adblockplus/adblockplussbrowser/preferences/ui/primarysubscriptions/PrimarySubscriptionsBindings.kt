package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("primarySubscriptions")
internal fun bindPrimarySubscriptions(recyclerView: RecyclerView, items: List<PrimarySubscriptionsItem>) {
    (recyclerView.adapter as PrimarySubscriptionsAdapter).submitList(items)
}