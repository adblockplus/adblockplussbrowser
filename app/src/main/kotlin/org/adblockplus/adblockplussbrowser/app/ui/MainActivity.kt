package org.adblockplus.adblockplussbrowser.app.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import org.adblockplus.adblockplussbrowser.app.R
import org.adblockplus.adblockplussbrowser.app.databinding.ActivityMainBinding
import org.adblockplus.adblockplussbrowser.base.navigation.navControllerFromFragmentContainerView
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants.SBROWSER_APP_ID
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants.SBROWSER_APP_ID_BETA
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants.SBROWSER_OLDEST_SAMSUNG_INTERNET_4_VERSIONCODE

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val navController: NavController
        get() = navControllerFromFragmentContainerView(R.id.nav_host_fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.sendAudienceAAEvent()
        setSupportActionBar(binding.toolbar)
        binding.toolbar.subtitle = resources.getString(R.string.app_subtitle)
        setupActionBarWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        if (!hasSamsungInternetVersion5OrNewer() && !hasSamsungInternetBeta()) {
            showInstallSamsungInternetDialog()
        } else {
            checkAdblockActivation()
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    /**
     * Starting with Samsung Internet 5.0, the way to enable ad blocking has changed. As a result, we
     * need to check for the version of Samsung Internet and apply text changes to the first run slide.
     *
     * @return a boolean that indicates, if the user has Samsung Internet version 5.x
     */
    private fun hasSamsungInternetVersion5OrNewer(): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(SBROWSER_APP_ID, 0)
            PackageInfoCompat.getLongVersionCode(packageInfo) >= SBROWSER_OLDEST_SAMSUNG_INTERNET_4_VERSIONCODE
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun hasSamsungInternetBeta(): Boolean {
        var result = true
        try {
            packageManager.getPackageInfo(SBROWSER_APP_ID_BETA, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            result = false
        }
        return result
    }

    private fun showInstallSamsungInternetDialog() {
        MaterialDialog(this).show {
            cancelable(false)
            customView(viewRes = R.layout.dialog_install_si, scrollable = true)
            val installButton = getCustomView().findViewById<View>(R.id.install_si_button)
            installButton.setOnClickListener {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=${SBROWSER_APP_ID}")
                        )
                    )
                } catch (t: Throwable) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=${SBROWSER_APP_ID}")
                        )
                    )
                }
                this.dismiss()
            }
        }
    }

    private fun checkAdblockActivation() {
        viewModel.fetchAdblockActivationStatus().observe(this) {
            if (!it) {
                navigate(LauncherDirection.ONBOARDING_LAST_STEP)
            }
        }
    }

}
