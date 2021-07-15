package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import android.graphics.drawable.Drawable
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("allowlistItems")
internal fun bindAllowlistItems(recyclerView: RecyclerView, items: List<AllowlistItem>) {
    (recyclerView.adapter as AllowlistAdapter).submitList(items)
}

@BindingAdapter("divider")
internal fun bindDivider(recyclerView: RecyclerView, drawable: Drawable) {
    val context = recyclerView.context
    val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
    drawable.let { decoration.setDrawable(it) }
    recyclerView.addItemDecoration(decoration)
}

@BindingAdapter("allowlistHeaderVisibility")
internal fun bindAllowlistHeaderVisibility(view: View, items: List<AllowlistItem>) {
    view.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
}
