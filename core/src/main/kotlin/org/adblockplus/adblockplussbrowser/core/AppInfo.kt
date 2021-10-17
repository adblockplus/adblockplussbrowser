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

package org.adblockplus.adblockplussbrowser.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import timber.log.Timber

internal data class AppInfo(
    val addonName: String? = DEFAULT_ADDON_NAME,
    val addonVersion: String? = null,
    val application: String? = null,
    val applicationVersion: String? = null,
    val platform: String? = "android",
    val platformVersion: String? = Build.VERSION.SDK_INT.toString(),
    val locale: String? = "en-US"
)

private const val DEFAULT_ADDON_NAME = "adblockplussbrowser"
private const val SBROWSER_PACKAGE_NAME = "com.sec.android.app.sbrowser"
private const val SBROWSER_BETA_PACKAGE_NAME = "com.sec.android.app.sbrowser.beta"
private const val SBROWSER_APP_NAME = "sbrowser"
private const val YANDEX_PACKAGE_NAME = "com.yandex.browser"
private const val YANDEX_ALPHA_PACKAGE_NAME = "com.yandex.browser.alpha"
private const val YANDEX_BETA_PACKAGE_NAME = "com.yandex.browser.beta"
private const val YANDEX_APP_NAME = "yandex"

internal fun Context.buildAppInfo(): AppInfo {
    return AppInfo(
        addonVersion = version(packageName),
        application = applicationForInstalledBrowser(this),
        applicationVersion = applicationVersion(this)
    )
}

private fun applicationForInstalledBrowser(context: Context): String {
    var application = ""
    if (context.isPackageInstalled(SBROWSER_PACKAGE_NAME) || context.isPackageInstalled(
            SBROWSER_BETA_PACKAGE_NAME)) {
        application += SBROWSER_APP_NAME
    }
    if (context.isPackageInstalled(YANDEX_PACKAGE_NAME)
        || context.isPackageInstalled(YANDEX_ALPHA_PACKAGE_NAME)
        || context.isPackageInstalled(YANDEX_BETA_PACKAGE_NAME)) {
        application += YANDEX_APP_NAME
    }

    return application
}

private fun applicationVersion(context: Context) = context.version(SBROWSER_PACKAGE_NAME)

private fun Context.version(packageId: String): String {
    return try {
        packageManager.getPackageInfo(packageId, 0).versionName.toLowerCase()
    } catch (ex: Exception) {
        Timber.e("Error retrieving app version for $packageId")
        "0"
    }
}

private fun Context.isPackageInstalled(packageId: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageId, 0)
        true
    } catch (ex: PackageManager.NameNotFoundException) {
        Timber.w("$packageId not found")
        false
    }
}