package org.adblockplus.adblockplussbrowser.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CoreData(
    val configured: Boolean,
    val lastUpdated: Long,
    val lastState: SavedState,
    val downloadedSubscription: List<DownloadedSubscription>,
    val lastUserCountingResponse: Long
) : Parcelable

@Parcelize
internal data class SavedState(
    val acceptableAdsEnabled: Boolean,
    val allowedDomains: List<String>,
    val blockedDomains: List<String>,
    val primarySubscriptions: List<String>,
    val otherSubscriptions: List<String>
) : Parcelable