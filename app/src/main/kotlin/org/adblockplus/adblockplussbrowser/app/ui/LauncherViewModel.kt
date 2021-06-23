package org.adblockplus.adblockplussbrowser.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import org.adblockplus.adblockplussbrowser.app.data.prefs.AppPreferences
import javax.inject.Inject

@HiltViewModel
internal class LauncherViewModel @Inject constructor(appPreferences: AppPreferences): ViewModel() {

    val direction: LiveData<LauncherDirection> = appPreferences.onboardingCompleted.map { completed ->
        if (completed) {
            LauncherDirection.MAIN
        } else {
            LauncherDirection.ONBOARDING
        }
    }.asLiveData()
}