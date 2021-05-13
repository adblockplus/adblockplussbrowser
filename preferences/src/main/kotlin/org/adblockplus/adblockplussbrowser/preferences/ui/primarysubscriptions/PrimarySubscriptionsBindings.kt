package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import org.adblockplus.adblockplussbrowser.preferences.ui.PrimarySubscriptionsAdapter
import org.adblockplus.adblockplussbrowser.preferences.ui.PrimarySubscriptionsItem

@BindingAdapter("primarySubscriptions")
internal fun bindPrimarySubscriptions(recyclerView: RecyclerView, items: List<PrimarySubscriptionsItem>?) {
    items?.let {
        (recyclerView.adapter as PrimarySubscriptionsAdapter).submitList(items)
    }
}