package org.adblockplus.adblockplussbrowser.base.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subscription(
    val url: String,
    val title: String,
    val lastUpdate: Long,
) : Parcelable {
//
//    override fun equals(other: Any?): Boolean =
//        other is Subscription && this.url == other.url
//
//    override fun hashCode(): Int = this.url.hashCode()
}