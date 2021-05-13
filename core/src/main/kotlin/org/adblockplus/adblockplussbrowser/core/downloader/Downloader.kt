package org.adblockplus.adblockplussbrowser.core.downloader

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription

internal interface Downloader {
    suspend fun download(subscription: Subscription): Result

    sealed class Result(val subscription: DownloadedSubscription?) {
        data class Success(private val sub: DownloadedSubscription) : Result(sub)
        data class NotModified(private val sub: DownloadedSubscription) : Result(sub)
        data class Failed(private val sub: DownloadedSubscription?) : Result(sub)

        fun isSuccessful(): Boolean {
            return when(this) {
                is Success, is NotModified -> true
                is Failed -> false
            }
        }
    }
}

internal fun Collection<Downloader.Result>.hasFailedResult(): Boolean =
    this.firstOrNull { !it.isSuccessful() } != null
