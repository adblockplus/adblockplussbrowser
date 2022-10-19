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

import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.base.data.model.SubscriptionUpdateStatus
import org.adblockplus.adblockplussbrowser.base.data.prefs.AppPreferences
import org.adblockplus.adblockplussbrowser.base.os.PackageHelper
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants
import timber.log.Timber

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val subscriptionsManager: SubscriptionsManager,
    private val appPreferences: AppPreferences
) : ViewModel() {

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    val updateStatus: LiveData<SubscriptionUpdateStatus> = subscriptionsManager.status.asLiveData()

    fun updateSubscriptions() {
        subscriptionsManager.scheduleImmediate(force = true)
    }

    fun fetchAdblockActivationStatus(): MutableLiveData<Boolean> {
        val isAdblockActivated = MutableLiveData<Boolean>()
        viewModelScope.launch {
            appPreferences.isAdblockEnabled().collect {
                isAdblockActivated.postValue(it)
            }
        }
        return isAdblockActivated
    }

    /**
     * Starting with Samsung Internet 4.0, the way to enable ad blocking has changed. As a result, we
     * need to check for the version of Samsung Internet and apply text changes to the first run slide.
     *
     * @return a boolean that indicates, if the user has Samsung Internet version 4.x
     */
    private fun hasSamsungInternetVersion4OrNewer(packageManager: PackageManager): Boolean {
        val packageId = SamsungInternetConstants.SBROWSER_APP_ID
        return try {
            val packageInfo = packageManager.getPackageInfo(packageId, 0)
            PackageInfoCompat.getLongVersionCode(packageInfo) >=
                    SamsungInternetConstants.SBROWSER_OLDEST_SAMSUNG_INTERNET_4_VERSIONCODE
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.d("${e.message}") // Logging the exception message because of detekt
            false
        }
    }

    fun shouldTriggerSamsungInstallation(packageManager: PackageManager): Boolean =
        !hasSamsungInternetVersion4OrNewer(packageManager) &&
                !PackageHelper.isPackageInstalled(
                    packageManager, SamsungInternetConstants.SBROWSER_APP_ID_BETA)

    fun logDeviceNotSupported() {
        // A device without Play Store, Galaxy store, and a browser
        Timber.e("This device is not supported")
        analyticsProvider.logEvent(AnalyticsEvent.DEVICE_NOT_SUPPORTED)
    }

    internal companion object {
        const val PLAY_STORE_PREFIX = "market://details?id="
        const val SAMSUNG_STORE_PREFIX = "samsungapps://ProductDetail/"
        const val PLAY_STORE_WEB_PREFIX = "https://play.google.com/store/apps/details?id="
    }
}
