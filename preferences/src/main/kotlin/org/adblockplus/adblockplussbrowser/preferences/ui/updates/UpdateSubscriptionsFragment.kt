package org.adblockplus.adblockplussbrowser.preferences.ui.updates

import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentUpdateSubscriptionsBinding
import org.adblockplus.adblockplussbrowser.preferences.ui.updates.UpdateSubscriptionsViewModel.UpdateConfigType
import timber.log.Timber

@AndroidEntryPoint
class UpdateSubscriptionsFragment : DataBindingFragment<FragmentUpdateSubscriptionsBinding>(R.layout.fragment_update_subscriptions) {
    private val viewModel: UpdateSubscriptionsViewModel by viewModels()

    private var isFirstSelection = true

    override fun onBindView(binding: FragmentUpdateSubscriptionsBinding) {
        binding.viewModel = viewModel

        val adapter = UpdateModeSpinnerAdapter.createFromResource(requireContext(),
            R.array.updates_preferences_types, R.layout.update_type_item,
            R.layout.update_type_selection_item, R.id.updates_preferences_automatic_updates_type)
        binding.updatesPreferencesSpinner.adapter = adapter
        binding.updatesPreferencesSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()
                if (!isFirstSelection) {
                    viewModel.setUpdateConfigType(position.toUpdateConfigType())
                }
                isFirstSelection = false
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapter.selectedPosition = -1
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.updateType.observe(viewLifecycleOwner) { configType ->
            binding.updatesPreferencesSpinner.setSelection(configType.toPosition())
        }

        viewModel.updateStatus.observe(this) { value ->
            Timber.d("Update status value: $value")
        }
    }

    private fun Int.toUpdateConfigType(): UpdateConfigType =
        if (this == 0) UpdateConfigType.UPDATE_WIFI_ONLY else UpdateConfigType.UPDATE_ALWAYS

    private fun UpdateConfigType.toPosition(): Int =
        if (this == UpdateConfigType.UPDATE_WIFI_ONLY) 0 else 1
}