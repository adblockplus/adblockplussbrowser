package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("allowlistItems")
internal fun bindAllowlistItems(recyclerView: RecyclerView, items: List<AllowlistItem>) {
    (recyclerView.adapter as AllowlistAdapter).submitList(items)
}