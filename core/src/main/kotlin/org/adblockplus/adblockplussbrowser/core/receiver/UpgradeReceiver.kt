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

package org.adblockplus.adblockplussbrowser.core.receiver

import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.SubscriptionsManager
import org.adblockplus.adblockplussbrowser.core.BuildConfig
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class UpgradeReceiver : HiltBroadcastReceiver() {
    @Inject
    internal lateinit var coreRepository: CoreRepository
    @Inject
    internal lateinit var subscriptionsManager: SubscriptionsManager

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (BuildConfig.FLAVOR_product == BuildConfig.FLAVOR_CRYSTAL) {
            val path = coreRepository.subscriptionsPath
            if (!path.isNullOrEmpty()) {
                File(path).let {
                    Timber.d("UpgradeReceiver: path=$path exists=${it.exists()}, deleting...")
                    it.exists() && it.delete()
                    Timber.d("UpgradeReceiver after delete: exists=${it.exists()}")
                }
            }
            subscriptionsManager.scheduleImmediate(force = true)
            Timber.d("UpgradeReceiver scheduleImmediate done")
        }
    }
}
