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
import timber.log.Timber

class PackageHelper private constructor() {
    companion object {
        fun isPackageInstalled(packageManager: PackageManager, packageId: String): Boolean {
            return try {
                packageManager.getPackageInfo(packageId, 0)
                true
            } catch (ex: PackageManager.NameNotFoundException) {
                Timber.i("$packageId not found")
                false
            }
        }

        fun version(packageManager: PackageManager, packageId: String): String {
            return try {
                packageManager.getPackageInfo(packageId, 0).versionName.lowercase()
            } catch (ex: Exception) {
                Timber.e("Error retrieving app version for $packageId")
                "0"
            }
        }
    }
}
