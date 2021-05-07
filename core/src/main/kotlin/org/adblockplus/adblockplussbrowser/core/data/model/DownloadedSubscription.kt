package org.adblockplus.adblockplussbrowser.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.adblockplus.adblockplussbrowser.core.data.proto.ProtoDownloadedSubscription

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