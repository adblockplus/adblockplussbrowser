package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@HiltViewModel
internal class OtherSubscriptionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val subscriptions: LiveData<List<OtherSubscriptionsItem>> = settingsRepository.settings.map {
        emptyList<OtherSubscriptionsItem>()
    }.asLiveData()


    fun toggleActiveSubscription(defaultItem: OtherSubscriptionsItem.DefaultItem) {

    }

    fun attemptRemoveSubscription(customItem: OtherSubscriptionsItem.CustomItem) {

    }

}