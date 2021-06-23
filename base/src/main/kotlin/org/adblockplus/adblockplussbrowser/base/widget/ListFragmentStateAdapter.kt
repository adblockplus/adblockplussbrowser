package org.adblockplus.adblockplussbrowser.base.widget

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class ListFragmentStateAdapter<T>(
    diffCallback: DiffUtil.ItemCallback<T>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    val differ: AsyncListDiffer<T>

    constructor(diffCallback: DiffUtil.ItemCallback<T>, activity: FragmentActivity) : this(
        diffCallback,
        activity.supportFragmentManager,
        activity.lifecycle
    )

    constructor(diffCallback: DiffUtil.ItemCallback<T>, fragment: Fragment) : this(
        diffCallback,
        fragment.childFragmentManager,
        fragment.lifecycle
    )

    init {
        val config = AsyncDifferConfig.Builder(diffCallback).build()
        @Suppress("LeakingThis")
        differ = AsyncListDiffer(AdapterListUpdateCallback(this), config)
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun getItem(position: Int): T = differ.currentList[position]

    fun submitList(list: List<T>?) {
        differ.submitList(list)
    }
}