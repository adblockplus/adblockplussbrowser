/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        lastUserCountingResponse,
        userCountingCount
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

