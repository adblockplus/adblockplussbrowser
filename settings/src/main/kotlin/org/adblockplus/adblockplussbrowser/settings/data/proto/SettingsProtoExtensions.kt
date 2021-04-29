package org.adblockplus.adblockplussbrowser.settings.data.proto

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig

internal fun ProtoSettings.toSettings(): Settings =
    Settings(
        this.adblockEnabled,
        this.acceptableAdsEnabled,
        this.updateConfig.toUpdateConfig(),
        this.allowedDomainsList,
        this.blockedDomainsList,
        this.activeAdsSubscriptionsList.map { it.toSubscription() },
        this.activeOtherSubscriptionsList.map { it.toSubscription() }
    )

internal fun Settings.toProtoSettings(): ProtoSettings =
    ProtoSettings.newBuilder()
        .setAdblockEnabled(this.adblockEnabled)
        .setAcceptableAdsEnabled(this.acceptableAdsEnabled)
        .setUpdateConfig(this.updateConfig.toProtoUpdateConfig())
        .addAllAllowedDomains(this.allowedDomains)
        .addAllBlockedDomains(this.blockedDomains)
        .addAllActiveAdsSubscriptions(this.activeAdsSubscriptions.map { it.toProtoSubscription() })
        .addAllActiveOtherSubscriptions(this.activeOtherSubscriptions.map { it.toProtoSubscription() })
        .build()

internal fun ProtoSubscription.toSubscription(): Subscription =
    Subscription(
        this.url,
        this.title,
        this.languagesList
    )

internal fun Subscription.toProtoSubscription(): ProtoSubscription =
    ProtoSubscription.newBuilder()
        .setUrl(this.url)
        .setTitle(this.title)
        .addAllLanguages(this.languages)
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