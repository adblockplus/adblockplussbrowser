package org.adblockplus.adblockplussbrowser.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription

@Parcelize
data class DownloadedSubscription(
    val subscription: Subscription,
    val path: String,
    val lastCheck: Long,
    val downloadCount: Long
): Parcelable