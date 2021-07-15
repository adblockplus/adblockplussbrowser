package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentAllowlistBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.SwipeToDeleteCallback

@AndroidEntryPoint
internal class AllowlistFragment : DataBindingFragment<FragmentAllowlistBinding>(R.layout.fragment_allowlist) {

    private val viewModel: AllowlistViewModel by activityViewModels()

    override fun onBindView(binding: FragmentAllowlistBinding) {
        binding.viewModel = viewModel
        binding.allowlistList.adapter = AllowlistAdapter(viewModel, viewLifecycleOwner)

        binding.allowlistAddButton.setOnClickListener {
            AddDomainDialogFragment().show(parentFragmentManager, null)
        }

        val swipeToDeleteHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val allowListAdapter = binding.allowlistList.adapter as AllowlistAdapter
                val item = allowListAdapter.getItem(viewHolder.adapterPosition)
                DeleteAllowlistItemDialogFragment.newInstance(item).show(parentFragmentManager, null)
                allowListAdapter.notifyDataSetChanged()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteHandler)
        itemTouchHelper.attachToRecyclerView(binding.allowlistList)
    }
}