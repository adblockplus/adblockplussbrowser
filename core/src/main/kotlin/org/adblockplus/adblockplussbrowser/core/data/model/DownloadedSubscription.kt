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

package org.adblockplus.adblockplussbrowser.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
internal data class DownloadedSubscription(
    val url: String,
    val path: String = "",
    val lastUpdated: Long = 0L,
    val lastModified: String = "",
    val version: String = "0",
    val etag: String = "",
    val downloadCount: Int = 0
): Parcelable

internal fun DownloadedSubscription.exists(): Boolean = File(path).exists()

internal fun DownloadedSubscription.ifExists(): DownloadedSubscription? =
    if (this.exists()) this else null

