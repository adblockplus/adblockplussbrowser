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

package org.adblockplus.adblockplussbrowser.app.ui

import android.app.Application
import android.os.RemoteException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences.Companion.isFilterRequestExpired
import org.adblockplus.adblockplussbrowser.base.data.prefs.AppPreferences
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class LauncherViewModel @Inject constructor(
    appPreferences: AppPreferences,
    application: Application
) : AndroidViewModel(application) {

    val context
        get() = getApplication<Application>()

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    private val onBoardingCompletedFlow = appPreferences.onboardingCompleted
    private val lastFilterRequestFlow = appPreferences.lastFilterListRequest

    fun fetchDirection(): MutableLiveData<LauncherDirection> {
        val navigationDirection = MutableLiveData<LauncherDirection>()
        viewModelScope.launch {
            onBoardingCompletedFlow.zip(lastFilterRequestFlow) { onBoardingCompleted, lastFilterRequest ->
                var direction = LauncherDirection.MAIN
                if (!onBoardingCompleted) {
                    direction = LauncherDirection.ONBOARDING
                } else if (onBoardingCompleted && lastFilterRequest == 0L) {
                    direction = LauncherDirection.ONBOARDING_LAST_STEP
                } else if (onBoardingCompleted && isFilterRequestExpired(lastFilterRequest)) {
                    direction = LauncherDirection.ONBOARDING_LAST_STEP
                }
                return@zip direction
            }.flowOn(Dispatchers.IO)
                .collect {
                    navigationDirection.postValue(it)
                }
        }
        return navigationDirection
    }

    fun checkInstallReferrer() {
        if (appPreferences.referrerAlreadyChecked) {
            Timber.d("InstallReferrer already checked")
            return
        }
        Timber.d("Checking InstallReferrer")
        // All InstallReferrerClient API needs to be called on UiThread
        val referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        try {
                            val response = referrerClient.installReferrer
                            var referrer = response.installReferrer
                            analyticsProvider.setUserProperty(
                                AnalyticsUserProperty.INSTALL_REFERRER, referrer)
                            appPreferences.referrerChecked()
                            referrerClient.endConnection()
                            Timber.d("InstallReferrer checked: %s", referrer)
                        } catch (ex: RemoteException) {
                            Timber.e(ex, "Error processing InstallReferrerResponse")
                        }
                    }
                    InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR,
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED,
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED,
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Call referrerChecked() to not repeat on those failures
                        try {
                            appPreferences.referrerChecked()
                            Timber.w("checkInstallReferrer() gets %d", responseCode)
                        } catch (ex: Exception) {
                            Timber.e(ex)
                        }
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                Timber.d("Install referrer service disconnected")
            }
        })
    }
}
