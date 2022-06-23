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

package org.adblockplus.adblockplussbrowser.preferences.ui.primarysubscriptions

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentPrimarySubscriptionsBinding

@AndroidEntryPoint
internal class PrimarySubscriptionsFragment :
    DataBindingFragment<FragmentPrimarySubscriptionsBinding>(R.layout.fragment_primary_subscriptions) {

    private val viewModel: PrimarySubscriptionsViewModel by viewModels()

    override fun onBindView(binding: FragmentPrimarySubscriptionsBinding) {
        binding.viewModel = viewModel
        binding.primarySubscriptionsList.adapter = PrimarySubscriptionsAdapter(viewModel, viewLifecycleOwner)
    }
}

