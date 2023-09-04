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

import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import timber.log.Timber

class PackageHelper private constructor() {
    // TODO refactor to avoid multiple calls to `PackageManager` when retrieving both version and is package installed
    companion object {
        internal const val VERSION_UNKNOWN = "0"

        // TODO refactor to return `Result`
        fun isPackageInstalled(packageManager: PackageManager, packageId: String): Boolean {
            return try {
                // Google might add `PackageManagerCompat` in the future
                // https://issuetracker.google.com/issues/246845196?pli=1
                // For now, we use the deprecated method and suppress the warning
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageId, 0)
                true
            } catch (ex: NameNotFoundException) {
                Timber.i("$packageId not found")
                false
            }
        }
        // TODO refactor to return `Result`
        fun version(packageManager: PackageManager, packageId: String): String {
            return try {
                // Google might add `PackageManagerCompat` in the future
                // https://issuetracker.google.com/issues/246845196?pli=1
                // For now, we use the deprecated method and suppress the warning
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageId, 0).versionName.lowercase()
            } catch (ex: NameNotFoundException) {
                Timber.e(ex,"Error retrieving app version for $packageId")
                VERSION_UNKNOWN
            }
        }

    }
}

fun String.isVersionUnknown() = this == PackageHelper.VERSION_UNKNOWN
