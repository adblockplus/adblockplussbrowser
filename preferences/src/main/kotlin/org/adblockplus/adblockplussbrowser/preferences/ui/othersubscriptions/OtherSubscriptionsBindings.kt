package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("otherSubscriptions")
internal fun bindOtherSubscriptions(recyclerView: RecyclerView, items: List<OtherSubscriptionsItem>) {
    (recyclerView.adapter as OtherSubscriptionsAdapter).submitList(items)
}