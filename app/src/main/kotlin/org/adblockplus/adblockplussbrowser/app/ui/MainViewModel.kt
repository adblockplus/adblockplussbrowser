package org.adblockplus.adblockplussbrowser.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.UpdateStatus
import org.adblockplus.adblockplussbrowser.preferences.ui.updates.UpdateSubscriptionsViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val subscriptionsManager: SubscriptionsManager
) : ViewModel() {
    val updateStatus = subscriptionsManager.status.map { status ->
        when (status) {
            is SubscriptionsManager.Status.Success -> UpdateStatus.Completed
            is SubscriptionsManager.Status.Failed -> UpdateStatus.Error
            is SubscriptionsManager.Status.Progress -> UpdateStatus.Progress(status.progress)
            is SubscriptionsManager.Status.None -> UpdateStatus.None
        }
    }.asLiveData()

    fun update() {
        subscriptionsManager.scheduleImmediate(force = true)
    }
}