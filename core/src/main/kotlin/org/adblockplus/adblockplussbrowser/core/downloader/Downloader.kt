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

package org.adblockplus.adblockplussbrowser.core.downloader

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription

internal interface Downloader {
    suspend fun download(
        subscription: Subscription,
        forced: Boolean,
        periodic: Boolean,
        newSubscription: Boolean,
    ): DownloadResult

    suspend fun validate(subscription: Subscription): Boolean
}

internal sealed class DownloadResult(val subscription: DownloadedSubscription?) {
    data class Success(private val sub: DownloadedSubscription) : DownloadResult(sub)
    data class NotModified(private val sub: DownloadedSubscription) : DownloadResult(sub)
    data class Failed(private val sub: DownloadedSubscription?) : DownloadResult(sub)

    fun isSuccessful(): Boolean {
        return when(this) {
            is Success, is NotModified -> true
            is Failed -> false
        }
    }
}

internal fun Collection<DownloadResult>.hasFailedResult(): Boolean =
    this.firstOrNull { !it.isSuccessful() } != null
