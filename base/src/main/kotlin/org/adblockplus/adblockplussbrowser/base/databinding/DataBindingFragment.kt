package org.adblockplus.adblockplussbrowser.base.databinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class DataBindingFragment<T : ViewDataBinding>(@LayoutRes private val layoutResId: Int) : Fragment() {

    var binding: T? = null

    protected abstract fun onBindView(binding: T)

    private fun doBindingView(binding: T) {
        this.binding = binding
        onBindView(binding)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<T>(inflater, layoutResId, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        doBindingView(binding)
        return binding.root
    }
}