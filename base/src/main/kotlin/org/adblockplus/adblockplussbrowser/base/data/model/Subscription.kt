package org.adblockplus.adblockplussbrowser.base.data.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Subscription(
    val url: String,
    val title: String,
    val lastUpdate: Long,
    val languages: List<String>
) : Parcelable