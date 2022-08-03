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

package org.adblockplus.adblockplussbrowser.settings.data.local

import org.adblockplus.adblockplussbrowser.base.data.model.CustomSubscriptionType
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
        0L,
        CustomSubscriptionType.FROM_URL
    )

    private fun processTitle(): String {
        return if (title.isEmpty() && languages.isNotEmpty()) {
            languages.map { HardcodedSubscriptions.LANGUAGE_DESCRIPTION_MAP[it] ?: "" }
                .distinct().filter { it.isNotEmpty() }.joinToString()
        } else {
            title
        }
    }
}

