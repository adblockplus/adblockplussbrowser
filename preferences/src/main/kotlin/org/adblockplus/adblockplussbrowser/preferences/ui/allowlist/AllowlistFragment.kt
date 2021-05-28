package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentAllowlistBinding

@AndroidEntryPoint
internal class AllowlistFragment : DataBindingFragment<FragmentAllowlistBinding>(R.layout.fragment_allowlist) {

    private val viewModel: AllowlistViewModel by activityViewModels()

    override fun onBindView(binding: FragmentAllowlistBinding) {
        binding.viewModel = viewModel
        binding.allowlistList.adapter = AllowlistAdapter(viewModel, viewLifecycleOwner)

        binding.allowlistAddButton.setOnClickListener {
            AddDomainDialogFragment().show(parentFragmentManager, null)
        }
    }
}