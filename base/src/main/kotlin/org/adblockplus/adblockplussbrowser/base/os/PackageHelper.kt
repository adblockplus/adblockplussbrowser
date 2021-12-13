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
                Timber.w("$packageId not found")
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