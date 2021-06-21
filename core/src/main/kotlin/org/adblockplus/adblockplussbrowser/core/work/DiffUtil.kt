package org.adblockplus.adblockplussbrowser.core.work

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings

data class Changes(
    val changes: List<Type>,
    val newSubscriptions: List<Subscription>,
    val removedSubscriptions: List<String>
) {
    fun hasChanges(): Boolean = changes.isNotEmpty()

    fun acceptableAdsChanged(): Boolean = changes.contains(Type.ACCEPTABLE_ADS)

    enum class Type {
        ACCEPTABLE_ADS,
        ALLOWED_DOMAINS,
        BLOCKED_DOMAINS,
        ADD_SUBSCRIPTIONS,
        REMOVE_SUBSCRIPTIONS
    }
}

internal fun Settings.changes(savedState: SavedState): Changes {
    val list = mutableListOf<Changes.Type>()
    if (this.acceptableAdsEnabled != savedState.acceptableAdsEnabled) {
        list.add(Changes.Type.ACCEPTABLE_ADS)
    }
    if (this.allowedDomains != savedState.allowedDomains) {
        list.add(Changes.Type.ALLOWED_DOMAINS)
    }
    if (this.blockedDomains != savedState.blockedDomains) {
        list.add(Changes.Type.BLOCKED_DOMAINS)
    }

    val additions = newSubscriptions(savedState)
    val deletions = removedSubscriptions(savedState)

    if (additions.isNotEmpty()) {
        list.add(Changes.Type.ADD_SUBSCRIPTIONS)
    }
    if (deletions.isNotEmpty()) {
        list.add(Changes.Type.REMOVE_SUBSCRIPTIONS)
    }

    return Changes(list, additions, deletions)
}

private fun Settings.newSubscriptions(savedState: SavedState): List<Subscription> {
    val urls = savedState.primarySubscriptions + savedState.otherSubscriptions
    return (this.activePrimarySubscriptions + this.activeOtherSubscriptions).filterNot { urls.contains(it.url) }
}

private fun Settings.removedSubscriptions(savedState: SavedState): List<String> {
    val subscriptions =
        (this.activePrimarySubscriptions + this.activeOtherSubscriptions).map { it.url }
    val savedSubscriptions = savedState.primarySubscriptions + savedState.otherSubscriptions

    return savedSubscriptions.filterNot { subscriptions.contains(it) }
}