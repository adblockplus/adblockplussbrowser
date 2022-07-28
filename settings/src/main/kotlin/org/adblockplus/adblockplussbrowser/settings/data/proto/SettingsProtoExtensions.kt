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

package org.adblockplus.adblockplussbrowser.settings.data.proto

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.data.model.CustomSubscriptionType
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig

internal fun ProtoSettings.toSettings(): Settings =
    Settings(
        this.adblockEnabled,
        this.acceptableAdsEnabled,
        this.updateConfig.toUpdateConfig(),
        this.allowedDomainsList,
        this.blockedDomainsList,
        this.activePrimarySubscriptionsList.map { it.toSubscription() },
        this.activeOtherSubscriptionsList.map { it.toSubscription() },
        this.analyticsEnabled,
        this.languagesOnboardingCompleted
    )

internal fun ProtoSubscription.toSubscription(): Subscription =
    Subscription(
        this.url,
        this.title,
        this.lastUpdate,
        this.type.toCustomSubscriptionType()
    )

internal fun Subscription.toProtoSubscription(): ProtoSubscription =
    ProtoSubscription.newBuilder()
        .setUrl(this.url)
        .setTitle(this.title)
        .setLastUpdate(this.lastUpdate)
        .setType(this.type.toProtoCustomSubscriptionType())
        .build()

internal fun ProtoUpdateConfig.toUpdateConfig(): UpdateConfig =
    when (this) {
        ProtoUpdateConfig.ALWAYS -> UpdateConfig.ALWAYS
        else -> UpdateConfig.WIFI_ONLY
    }

internal fun UpdateConfig.toProtoUpdateConfig(): ProtoUpdateConfig =
    when (this) {
        UpdateConfig.WIFI_ONLY -> ProtoUpdateConfig.WIFI_ONLY
        UpdateConfig.ALWAYS -> ProtoUpdateConfig.ALWAYS
    }

internal fun ProtoCustomSubscriptionType.toCustomSubscriptionType(): CustomSubscriptionType =
    when(this) {
        ProtoCustomSubscriptionType.FROM_URL -> CustomSubscriptionType.FROM_URL
        else -> CustomSubscriptionType.LOCAL_FILE
    }

internal fun CustomSubscriptionType.toProtoCustomSubscriptionType(): ProtoCustomSubscriptionType =
    when(this) {
        CustomSubscriptionType.FROM_URL -> ProtoCustomSubscriptionType.FROM_URL
        else -> ProtoCustomSubscriptionType.LOCAL_FILE
    }
