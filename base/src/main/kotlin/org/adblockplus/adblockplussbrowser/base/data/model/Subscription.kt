package org.adblockplus.adblockplussbrowser.base.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subscription(
    val url: String,
    val title: String,
    // TODO - remove from here after changing Primary subscriptions fragment
    val lastUpdate: Long,
) : Parcelable