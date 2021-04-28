package org.adblockplus.adblockplussbrowser.base.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Subscription(
    val url: String,
    val title: String,
    val languages: List<String>
) : Parcelable