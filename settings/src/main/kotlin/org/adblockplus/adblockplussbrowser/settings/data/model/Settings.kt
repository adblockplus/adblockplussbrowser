package org.adblockplus.adblockplussbrowser.settings.data.model

import android.os.Parcelable
import androidx.work.NetworkType
import kotlinx.parcelize.Parcelize
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription

@Parcelize
data class Settings(
    val adblockEnabled: Boolean,
    val acceptableAdsEnabled: Boolean,
    val updateConfig: UpdateConfig,
    val allowedDomains: List<String>,
    val blockedDomains: List<String>,
    val activeAdsSubscriptions: List<Subscription>,
    val activeOtherSubscriptions: List<Subscription>
) : Parcelable

enum class UpdateConfig {
    WIFI_ONLY,
    ALWAYS
}