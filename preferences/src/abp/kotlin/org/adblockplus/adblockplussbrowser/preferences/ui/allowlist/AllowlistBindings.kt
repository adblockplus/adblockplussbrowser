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
