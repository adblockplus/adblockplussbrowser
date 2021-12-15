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
import android.os.Build
import org.adblockplus.adblockplussbrowser.base.os.PackageHelper

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

internal fun Context.buildAppInfo(): AppInfo {
    return AppInfo(
        addonVersion = PackageHelper.version(packageManager, packageName),
        application = applicationForInstalledBrowser(this),
        applicationVersion = applicationVersion(this)
    )
}

private fun applicationForInstalledBrowser(context: Context): String {
    var application = ""
    if (PackageHelper.isPackageInstalled(context.packageManager, SBROWSER_PACKAGE_NAME) ||
        PackageHelper.isPackageInstalled(context.packageManager, SBROWSER_BETA_PACKAGE_NAME)) {
        application += SBROWSER_APP_NAME
    }
    return application
}

private fun applicationVersion(context: Context) = PackageHelper.version(context.packageManager, SBROWSER_PACKAGE_NAME)
