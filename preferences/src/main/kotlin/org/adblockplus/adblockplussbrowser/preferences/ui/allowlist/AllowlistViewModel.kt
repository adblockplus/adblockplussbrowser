package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.preferences.ui.layoutForIndex
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class AllowlistViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val items: LiveData<List<AllowlistItem>> = settingsRepository.settings.map { settings ->
        settings.allowedDomains.sorted().allowlistItems()
    }.asLiveData()

    fun addDomain(domain: String) {
        viewModelScope.launch {
            settingsRepository.addAllowedDomain(domain)
        }
    }

    fun removeItem(item: AllowlistItem) {
        viewModelScope.launch {
            settingsRepository.removeAllowedDomain(item.domain)
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