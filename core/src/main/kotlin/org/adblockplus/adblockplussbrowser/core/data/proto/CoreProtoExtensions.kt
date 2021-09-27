package org.adblockplus.adblockplussbrowser.core.data.proto

import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.DownloadedSubscription
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings

internal fun ProtoCoreData.toCoreData(): CoreData =
    CoreData(
        configured,
        lastUpdate,
        lastState.toSavedState(),
        downloadedSubscriptionsList.map { it.toDownloadedSubscription() },
        lastVersion
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

internal fun SavedState.toProtoSavedState(): ProtoSavedState =
    ProtoSavedState.newBuilder()
        .setAcceptableAdsEnabled(acceptableAdsEnabled)
        .addAllAllowedDomains(allowedDomains)
        .addAllBlockedDomains(blockedDomains)
        .addAllPrimarySubscriptions(primarySubscriptions)
        .addAllOtherSubscriptions(otherSubscriptions)
        .build()

internal fun ProtoSavedState.toSavedState(): SavedState =
    SavedState(
        acceptableAdsEnabled,
        allowedDomainsList,
        blockedDomainsList,
        primarySubscriptionsList,
        otherSubscriptionsList
    )

internal fun Settings.toSavedState(): SavedState =
    SavedState(
        acceptableAdsEnabled,
        allowedDomains,
        blockedDomains,
        activePrimarySubscriptions.map { it.url },
        activeOtherSubscriptions.map { it.url }
    )