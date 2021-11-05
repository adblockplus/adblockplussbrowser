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

import android.os.Bundle
import android.os.RemoteException
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import com.android.installreferrer.api.InstallReferrerClient
import org.adblockplus.adblockplussbrowser.base.data.prefs.AppPreferences
import javax.inject.Inject

import com.android.installreferrer.api.InstallReferrerStateListener
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import timber.log.Timber

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.fetchDirection().observe(this) {
            navigate(it)
        }

        checkInstallReferrer();
    }

    fun checkInstallReferrer() {
        if (appPreferences.referrerAlreadyChecked) {
            Timber.d("InstallReferrer already checked")
            return
        }
        // All InstallReferrerClient API needs to be called on UiThread
        val referrerClient = InstallReferrerClient.newBuilder(this).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        try {
                            val response = referrerClient.getInstallReferrer()
                            analyticsProvider.setUserProperty(
                                AnalyticsUserProperty.INSTALL_REFERRER, response.installReferrer
                            )
                            appPreferences.referrerChecked()
                            referrerClient.endConnection()
                        } catch (ex: RemoteException) {
                            Timber.e(ex, "Error processing InstallReferrerResponse")
                        }
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED,
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE,
                    InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR -> {
                        // Call referrerChecked() to not repeat on those failures.
                        // Excluded here is `SERVICE_DISCONNECTED` because on
                        // onInstallReferrerServiceDisconnected() we should retry.
                        appPreferences.referrerChecked()
                        Timber.w("checkInstallReferrer() gets %d", responseCode)
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                checkInstallReferrer()
            }
        })
    }
}