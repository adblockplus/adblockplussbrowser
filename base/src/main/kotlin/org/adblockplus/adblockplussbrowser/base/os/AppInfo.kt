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

package org.adblockplus.adblockplussbrowser.base.os

import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import org.adblockplus.adblockplussbrowser.base.BuildConfig
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants.SBROWSER_APP_ID
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants.SBROWSER_APP_ID_BETA

/**
 * Data class providing information about the application.
 * Member names are chosen to match the names of the fields in the ActivePing schema.
 *
 * @param addonName Codename of the addon, e.g. "adblockplussbrowser".
 * @param addonVersion Version of the addon. (Adblock Plus, Adblock or Crystal version)
 * @param application Name of the application. (Samsung Internet)
 * @param applicationVersion Version of the application. (Samsung Internet version)
 * @param platform Name of the platform (Android).
 * @param platformVersion Version of the platform (Android SDK version).
 * @param locale Locale of the platform.
 * @param extensionName Name of the extension.
 * @param extensionVersion Version of the extension.
 */
data class AppInfo constructor(
    val addonName: String = addonName(),
    val addonVersion: String? = null, // nullable for tests
    val application: String? = null,
    val applicationVersion: String? = null,
    val platform: String = "android",
    @ChecksSdkIntAtLeast
    val platformVersion: String = Build.VERSION.SDK_INT.toString(),
    val locale: String = "en-US",
    val extensionName: String = addonName(),
    val extensionVersion: String = "",
)

private const val ABP_ADDON_NAME = "adblockplussbrowser"
private const val AB_ADDON_NAME = "adblocksbrowser"
private const val CRYSTAL_ADDON_NAME = "crystalsbrowser"
private const val DEFAULT_ADDON_NAME = ABP_ADDON_NAME

fun Context.buildAppInfo(): AppInfo {
    val (app, ver) = applicationAndVersionForInstalledBrowser(this)
    return AppInfo(
        addonVersion = PackageHelper.version(packageManager, packageName),
        extensionVersion = PackageHelper.version(packageManager, packageName),
        application = app,
        applicationVersion = ver
    )
}

@Suppress("KotlinConstantConditions")
private fun addonName(): String = when (BuildConfig.FLAVOR_product) {
    BuildConfig.FLAVOR_ABP -> ABP_ADDON_NAME
    BuildConfig.FLAVOR_ADBLOCK -> AB_ADDON_NAME
    BuildConfig.FLAVOR_CRYSTAL -> CRYSTAL_ADDON_NAME
    else -> DEFAULT_ADDON_NAME
}

private fun applicationAndVersionForInstalledBrowser(context: Context): Pair<String?, String?> {
    val sbVer = PackageHelper.version(context.packageManager, SBROWSER_APP_ID)
    val sbBetaVer = PackageHelper.version(context.packageManager, SBROWSER_APP_ID_BETA)

    return when {
        !sbVer.isVersionUnknown() -> {
            SBROWSER_APP_ID to sbVer
        }

        !sbBetaVer.isVersionUnknown() -> {
            SBROWSER_APP_ID_BETA to sbBetaVer
        }

        else -> null to null
    }
}
