package org.adblockplus.adblockplussbrowser.base.databinding

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class DataBindingViewHolder(open val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)