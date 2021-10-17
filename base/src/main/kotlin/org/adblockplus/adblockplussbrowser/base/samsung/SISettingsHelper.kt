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

package org.adblockplus.adblockplussbrowser.base.samsung

import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants
import java.util.concurrent.TimeUnit

class SISettingsHelper private constructor() {
    companion object {
        fun openSISettings(context: Context) {
            var samsungInternetIntentLauncher = getLauncher(SamsungInternetConstants.SBROWSER_APP_ID, context)
            if (samsungInternetIntentLauncher == null) {
                samsungInternetIntentLauncher = getLauncher(SamsungInternetConstants.SBROWSER_APP_ID_BETA, context)
            }

            samsungInternetIntentLauncher?.let {
                // SI has a defect in intent ACTION_OPEN_SETTINGS processing,
                // which is not working if SI receives ACTION_OPEN_SETTINGS in the destroyed state.
                // As a workaround SI is stared before ACTION_OPEN_SETTINGS is sent.
                context.startActivity(it)
                val openSISettingsRequest = OneTimeWorkRequest.Builder(OpenSISettingsWorker::class.java)
                    .setInitialDelay(SamsungInternetConstants.SBROWSER_START_SETTINGS_DELAY, TimeUnit.MILLISECONDS)
                    .build()
                WorkManager.getInstance(context).enqueue(openSISettingsRequest)
            }
        }

        private fun getLauncher(id: String, context: Context): Intent? {
            return context.packageManager?.getLaunchIntentForPackage(id)
        }
    }
}
