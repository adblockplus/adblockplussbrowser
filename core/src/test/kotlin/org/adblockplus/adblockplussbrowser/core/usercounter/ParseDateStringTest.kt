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

package org.adblockplus.adblockplussbrowser.core.usercounter

import org.adblockplus.adblockplusbrowser.testutils.FakeAnalyticsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.text.ParseException
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ParseDateStringTest {

    private lateinit var analytics: FakeAnalyticsProvider

    @Before
    fun setUp() {
        analytics = FakeAnalyticsProvider()
    }

    @Test
    fun `valid Date header parses to yyyyMMddHHmm`() {
        val result = OkHttpUserCounter.parseDateString(
            rawDate = "Thu, 23 Sep 2021 17:31:01 GMT",
            analyticsProvider = analytics,
            strict = false,
        )
        assertEquals("202109231731", result)
        assertNull(analytics.exception)
    }

    @Test
    fun `strict mode rethrows and logs on empty Date header`() {
        try {
            OkHttpUserCounter.parseDateString(
                rawDate = "",
                analyticsProvider = analytics,
                strict = true,
            )
            fail("Expected ParseException")
        } catch (expected: ParseException) {
            assertTrue(analytics.exception is ParseException)
        }
    }

    @Test
    fun `lenient mode falls back to client time and logs on empty Date header`() {
        val result = OkHttpUserCounter.parseDateString(
            rawDate = "",
            analyticsProvider = analytics,
            strict = false,
        )
        // Format contract: yyyyMMddHHmm — 12 digits, deterministic regardless of wall-clock.
        assertEquals(12, result.length)
        assertTrue(result.all { it.isDigit() })
        assertTrue(analytics.exception is ParseException)
    }
}
