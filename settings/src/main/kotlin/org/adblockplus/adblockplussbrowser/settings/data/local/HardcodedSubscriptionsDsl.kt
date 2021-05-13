package org.adblockplus.adblockplussbrowser.settings.data.local

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.kotlin.asMutable

internal fun subscriptions(initializer: HardcodedSubscriptionsDsl.() -> Unit): MutableList<HardcodedSubscriptionDsl> {
    val preloadedSubscriptions = HardcodedSubscriptionsDsl()
    preloadedSubscriptions.initializer()
    return preloadedSubscriptions.subscriptions
}

internal fun subscription(initializer: HardcodedSubscriptionDsl.() -> Unit): HardcodedSubscriptionDsl {
    val preloadedSubscription = HardcodedSubscriptionDsl()
    preloadedSubscription.initializer()
    return preloadedSubscription
}

internal class HardcodedSubscriptionsDsl {
    var subscriptions = mutableListOf<HardcodedSubscriptionDsl>()

    fun subscription(initializer: HardcodedSubscriptionDsl.() -> Unit): HardcodedSubscriptionsDsl {
        val preloadedSubscription = HardcodedSubscriptionDsl()
        preloadedSubscription.initializer()
        subscriptions.add(preloadedSubscription)
        return this
    }
}

internal class HardcodedSubscriptionDsl {

    var url = ""
    var title = ""
    val languages: List<String> = mutableListOf()

    fun languages(vararg languages: String) {
        this.languages.asMutable().addAll(languages)
    }

    fun toSubscription(): Subscription = Subscription(
        this.url,
        processTitle(),
        0L
    )

    private fun processTitle(): String {
        return if (title.isEmpty() && languages.isNotEmpty()) {
            languages.map { HardcodedSubscriptions.LANGUAGE_DESCRIPTION_MAP[it] ?: "" }.filter { it.isNotEmpty() }
                .joinToString()
        } else {
            title
        }
    }
}