package org.adblockplus.adblockplussbrowser.core.work

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings

internal fun Settings.diff(savedState: SavedState): List<Changes> {
    val list = mutableListOf<Changes>()
    if (this.acceptableAdsEnabled != savedState.acceptableAdsEnabled) {
        list.add(Changes.ACCEPTABLE_ADS)
    }
    if (this.allowedDomains != savedState.allowedDomains) {
        list.add(Changes.ALLOWED_DOMAINS)
    }
    if (this.blockedDomains != savedState.blockedDomains) {
        list.add(Changes.BLOCKED_DOMAINS)
    }

    val subscriptions =
        (this.activePrimarySubscriptions + this.activeOtherSubscriptions).map { it.url }
    val savedSubsriptions = savedState.primarySubscriptions + savedState.otherSubscriptions

    val additions = subscriptions.diff(savedSubsriptions)
    val deletions = savedSubsriptions.diff(subscriptions)

    if (additions.isNotEmpty()) {
        list.add(Changes.ADD_SUBSCRIPTIONS)
    }
    if (deletions.isNotEmpty()) {
        list.add(Changes.REMOVE_SUBSCRIPTIONS)
    }

    return list
}

internal fun Settings.newSubscriptions(savedState: SavedState): List<Subscription> {
    val urls = savedState.primarySubscriptions + savedState.otherSubscriptions
    return (this.activePrimarySubscriptions + this.activeOtherSubscriptions).filterNot { urls.contains(it.url) }
}

internal fun Settings.removedSubscriptions(savedState: SavedState): List<String> {
    val subscriptions =
        (this.activePrimarySubscriptions + this.activeOtherSubscriptions).map { it.url }
    val savedSubsriptions = savedState.primarySubscriptions + savedState.otherSubscriptions

    return savedSubsriptions.diff(subscriptions)
}

private fun List<String>.diff(other: List<String>): List<String> = this.filterNot { other.contains(it) }

internal fun List<Changes>.hasChanges() = this.isNotEmpty()

internal fun List<Changes>.acceptableAdsChanged() = this.contains(Changes.ACCEPTABLE_ADS)

enum class Changes {
    ACCEPTABLE_ADS,
    ALLOWED_DOMAINS,
    BLOCKED_DOMAINS,
    ADD_SUBSCRIPTIONS,
    REMOVE_SUBSCRIPTIONS
}