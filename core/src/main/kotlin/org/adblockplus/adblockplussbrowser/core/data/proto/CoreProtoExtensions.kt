package org.adblockplus.adblockplussbrowser.core.data.proto

import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription

internal fun ProtoCoreData.toCoreData(): CoreData =
    CoreData(
        isInitialized,
        lastUpdate,
        downloadedSubscriptionsList.map { it.toDownloadedSubscription() }
    )

internal fun ProtoDownloadedSubscription.toDownloadedSubscription(): DownloadedSubscription =
    DownloadedSubscription(
        url,
        path,
        lastUpdate,
        lastModified,
        version,
        etag,
        downloadCount
    )

internal fun DownloadedSubscription.toProtoDownloadedSubscription(): ProtoDownloadedSubscription =
    ProtoDownloadedSubscription.newBuilder()
        .setUrl(url)
        .setPath(path)
        .setLastUpdate(lastUpdated)
        .setLastModified(lastModified)
        .setVersion(version)
        .setEtag(etag)
        .setDownloadCount(downloadCount)
        .build()