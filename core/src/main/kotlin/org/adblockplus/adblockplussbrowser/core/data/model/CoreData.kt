package org.adblockplus.adblockplussbrowser.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CoreData(
    val initialized: Boolean,
    val lastUpdated: Long,
    val downloadedSubscription: List<DownloadedSubscription>
) : Parcelable