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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.app.R
import org.adblockplus.adblockplussbrowser.app.databinding.ActivityMainBinding
import org.adblockplus.adblockplussbrowser.base.navigation.navControllerFromFragmentContainerView
import org.adblockplus.adblockplussbrowser.base.os.PackageHelper
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants.SBROWSER_APP_ID
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants.SBROWSER_APP_ID_BETA
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants.SBROWSER_OLDEST_SAMSUNG_INTERNET_4_VERSIONCODE
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var analyticsProvider: AnalyticsProvider

    private val navController: NavController
        get() = navControllerFromFragmentContainerView(R.id.nav_host_fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        if (!hasSamsungInternetVersion4OrNewer()
            && !PackageHelper.isPackageInstalled(packageManager, SBROWSER_APP_ID_BETA)
        ) {
            showInstallSamsungInternetDialog()
        } else {
            checkAdblockActivation()
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    /**
     * Starting with Samsung Internet 4.0, the way to enable ad blocking has changed. As a result, we
     * need to check for the version of Samsung Internet and apply text changes to the first run slide.
     *
     * @return a boolean that indicates, if the user has Samsung Internet version 4.x
     */
    private fun hasSamsungInternetVersion4OrNewer(): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(SBROWSER_APP_ID, 0)
            PackageInfoCompat.getLongVersionCode(packageInfo) >= SBROWSER_OLDEST_SAMSUNG_INTERNET_4_VERSIONCODE
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun showInstallSamsungInternetDialog() {
        MaterialDialog(this).show {
            cancelable(true)
            customView(viewRes = R.layout.dialog_install_si, scrollable = true)
            val installButton = getCustomView().findViewById<View>(R.id.install_si_button)
            installButton.setOnClickListener {
                installSamsungInternet(this)
            }
        }
    }

    private fun installSamsungInternet(dialog: MaterialDialog) {
        listOf(PLAY_STORE_PREFIX, SAMSUNG_STORE_PREFIX, PLAY_STORE_WEB_PREFIX).forEach {
            try {
                startStore(it)
                dialog.dismiss()
                return
            } catch (exception: ActivityNotFoundException) {}
        }
        // A device without Play Store, Galaxy store, and a browser
        Timber.e("This device is not supported")
        analyticsProvider.logEvent(AnalyticsEvent.DEVICE_NOT_SUPPORTED)
        Toast.makeText(applicationContext, getString(R.string.device_not_supported), Toast.LENGTH_LONG)
            .show()
    }

    private fun startStore(storePrefix: String) {
        Timber.d("Start store with prefix: $storePrefix")
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("$storePrefix${SBROWSER_APP_ID}")
        )
        startActivity(intent)
    }

    private fun checkAdblockActivation() {
        viewModel.fetchAdblockActivationStatus().observe(this) {
            if (!it) {
                navigate(LauncherDirection.ONBOARDING_LAST_STEP)
            }
        }
    }

    private companion object {
        private const val PLAY_STORE_PREFIX = "market://details?id="
        private const val SAMSUNG_STORE_PREFIX = "samsungapps://ProductDetail/"
        private const val PLAY_STORE_WEB_PREFIX = "https://play.google.com/store/apps/details?id="

    }

}
