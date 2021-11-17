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

package org.adblockplus.adblockplussbrowser.base.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionTest {

    @Test
    fun testUpdateUrl() {
        // Non eyeo domains are not changed
        val someNotEyeoSubscription =
            Subscription(
                "https://some.not.eyeo.domain.com/filter-list.txt",
                "Some List", 0L)
        assertEquals(someNotEyeoSubscription.url,
            someNotEyeoSubscription.randomizedUrl)

        // Empty domains are not changed (wrong input => no op)
        val emptyUrlSubscription = Subscription("", "Wrong URL", 0L)
        assertEquals(emptyUrlSubscription.url, emptyUrlSubscription.randomizedUrl)

        val expectedRegex =
            """https://([0-9]).samsung-internet.filter-list-downloads.eyeo.com/.*""".toRegex()

        // Check prefix was added
        val easylistSubscription = Subscription (
            "https://easylist-downloads.adblockplus.org/easylist.txt",
            "EasyList", 0L)
        assertTrue(expectedRegex.matches(easylistSubscription.randomizedUrl))

        // Check np-op (no change) in a correct url
        val randomizedSubscription = Subscription(
            "3.samsung-internet.filter-list-downloads.eyeo.com",
            "Randomized Subscription", 0L)
        assertEquals(randomizedSubscription.url, randomizedSubscription.randomizedUrl)
    }
}