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

package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.preferences.ui.layoutForIndex
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class AllowlistViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    val items: LiveData<List<AllowlistItem>> = settingsRepository.settings.map { settings ->
        settings.allowedDomains.sorted().allowlistItems()
    }.asLiveData()

    fun addDomain(domain: String) {
        viewModelScope.launch {
            settingsRepository.addAllowedDomain(domain)
            analyticsProvider.logEvent(AnalyticsEvent.URL_ALLOWLIST_ADDED)
        }
    }

    fun removeItem(item: AllowlistItem) {
        viewModelScope.launch {
            settingsRepository.removeAllowedDomain(item.domain)
            analyticsProvider.logEvent(AnalyticsEvent.URL_ALLOWLIST_REMOVED)
        }
    }

    private fun List<String>.allowlistItems(): List<AllowlistItem> {
        val result = mutableListOf<AllowlistItem>()
        this.forEachIndexed { index, domain ->
            val layout = this.layoutForIndex(index)
            result.add(AllowlistItem(domain, layout))
        }
        return result
    }
}
