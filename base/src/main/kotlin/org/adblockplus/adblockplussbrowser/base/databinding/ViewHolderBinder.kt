package org.adblockplus.adblockplussbrowser.base.databinding

import androidx.databinding.ViewDataBinding

interface ViewHolderBinder<T : ViewDataBinding> {

    val binding: T

    fun bind(config: T.() -> Unit) {
        binding.config()
        binding.executePendingBindings()
    }
}