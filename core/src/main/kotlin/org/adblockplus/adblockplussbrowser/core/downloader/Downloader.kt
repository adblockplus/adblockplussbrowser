package org.adblockplus.adblockplussbrowser.core.downloader

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription

internal interface Downloader {
    suspend fun download(subscription: Subscription): DownloadedSubscription?
}