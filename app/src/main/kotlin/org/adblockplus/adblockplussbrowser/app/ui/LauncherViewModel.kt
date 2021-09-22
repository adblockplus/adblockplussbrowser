package org.adblockplus.adblockplussbrowser.app.ui

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.app.data.prefs.AppPreferences
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
internal class LauncherViewModel @Inject constructor(appPreferences: AppPreferences) : ViewModel() {
    private val onBoardingCompletedFlow = appPreferences.onboardingCompleted
    private val lastFilterRequestFlow = appPreferences.lastFilterListRequest

    val navigationDirection = MutableLiveData<LauncherDirection>()

    fun fetchDirection() {
        viewModelScope.launch {
            onBoardingCompletedFlow.zip(lastFilterRequestFlow) { onBoardingCompleted, lastFilterRequiest ->
                var direction = LauncherDirection.MAIN
                if (!onBoardingCompleted) {
                    direction = LauncherDirection.ONBOARDING
                } else if (onBoardingCompleted && lastFilterRequiest == 0L) {
                    direction = LauncherDirection.ONBOARDING_LAST_STEP
                } else if (onBoardingCompleted && isFilterRequestExpired(lastFilterRequiest)
                ) {
                    direction = LauncherDirection.ONBOARDING_LAST_STEP
                }
                return@zip direction
            }.flowOn(Dispatchers.IO)
                .collect {
                    navigationDirection.postValue(it)
                }
        }
    }

    companion object {
        private const val FILTER_REQUEST_DAYS_TO_EXPIRE = 30L
        fun isFilterRequestExpired(lastFilterRequest: Long) =
            System.currentTimeMillis() - lastFilterRequest > TimeUnit.MILLISECONDS.convert(
                FILTER_REQUEST_DAYS_TO_EXPIRE,
                TimeUnit.DAYS
            )
    }
}
