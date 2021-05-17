package org.adblockplus.adblockplussbrowser.base.databinding

import androidx.databinding.ViewDataBinding

inline fun <reified T : ViewDataBinding> T.bindHolder(config: T.() -> Unit) {
    this.config()
    this.executePendingBindings()
}