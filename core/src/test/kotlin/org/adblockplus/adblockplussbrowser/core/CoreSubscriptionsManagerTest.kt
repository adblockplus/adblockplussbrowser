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

package org.adblockplus.adblockplussbrowser.core

import kotlin.time.ExperimentalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalTime
class CoreSubscriptionsManagerTest {

    @Test
    fun testUpdateUrl() {
        // Non eyeo domains are not changed
        assertEquals("some.not.eyeo.domain.com",
            CoreSubscriptionsManager.updateUrl("some.not.eyeo.domain.com"))

        // Empty domains are not changed (wrong input => no op)
        assertEquals("", CoreSubscriptionsManager.updateUrl(""))

        val expectedRegex = """samsung-internet-([0-9]).filter-list-downloads.eyeo.com""".toRegex()

        // Check prefix was added
        assertTrue(expectedRegex.matches(CoreSubscriptionsManager.updateUrl("filter-list-downloads.eyeo.com")))

        // Check prefix was added and old domain changed to the new one
        assertTrue(expectedRegex.matches(CoreSubscriptionsManager.updateUrl("easylist-downloads.adblockplus.org")))

        // Check np-op (no change) in a correct url
        assertEquals("samsung-internet-3.filter-list-downloads.eyeo.com",
            CoreSubscriptionsManager.updateUrl("samsung-internet-3.filter-list-downloads.eyeo.com"))

        // Check domains change from old to the new if prefix was already there
        assertEquals("samsung-internet-3.filter-list-downloads.eyeo.com",
            CoreSubscriptionsManager.updateUrl("samsung-internet-3.easylist-downloads.adblockplus.org"))
    }
}
