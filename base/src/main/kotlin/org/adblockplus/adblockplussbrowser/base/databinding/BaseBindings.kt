package org.adblockplus.adblockplussbrowser.base.databinding

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("gone")
fun bindGone(view: View, gone: Boolean) {
    view.visibility = if (gone) View.GONE else View.VISIBLE
}