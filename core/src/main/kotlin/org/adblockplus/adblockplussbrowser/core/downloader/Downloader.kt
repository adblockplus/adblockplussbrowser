package org.adblockplus.adblockplussbrowser.core.downloader

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription

internal interface Downloader {
    suspend fun download(
        subscription: Subscription,
        forced: Boolean,
        periodic: Boolean,
        newSubscription: Boolean,
    ): DownloadResult

    suspend fun validate(subscription: Subscription): Boolean
}

internal sealed class DownloadResult(val subscription: DownloadedSubscription?) {
    data class Success(private val sub: DownloadedSubscription) : DownloadResult(sub)
    data class NotModified(private val sub: DownloadedSubscription) : DownloadResult(sub)
    data class Failed(private val sub: DownloadedSubscription?) : DownloadResult(sub)

    fun isSuccessful(): Boolean {
        return when(this) {
            is Success, is NotModified -> true
            is Failed -> false
        }
    }
}

internal fun Collection<DownloadResult>.hasFailedResult(): Boolean =
    this.firstOrNull { !it.isSuccessful() } != null
