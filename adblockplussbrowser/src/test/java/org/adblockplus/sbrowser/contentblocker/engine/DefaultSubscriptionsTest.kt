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

package org.adblockplus.sbrowser.contentblocker.engine


import org.adblockplus.adblockplussbrowser.R
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.IOException
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
class DefaultSubscriptionsTest {

    private val nonExistingResource = "not_existing.xml"
    private val emptySubscriptions = "subscriptions_empty.xml"
    private val brokenSubscriptions = "subscriptions_malformed.xml"

    private val EASYLIST_GERMANY_COMPLETE_URL =
            "https://easylist-downloads.adblockplus.org/easylistgermany+easylist.txt"

    @Test
    fun openResourcesForTesting()
    {
        Assert.assertNull(getTestResourceAsStream(nonExistingResource))
        Assert.assertNotNull(getTestResourceAsStream(emptySubscriptions))
    }

    @Test
    fun parseEmptySubscriptions()
    {
        val subscriptions = DefaultSubscriptions.fromStream(getTestResourceAsStream(emptySubscriptions))
        Assert.assertNotNull(subscriptions)
        Assert.assertTrue(subscriptions.get().isEmpty())

        Assert.assertNull(subscriptions.getForUrl(EASYLIST_GERMANY_COMPLETE_URL))
    }

    @Test(expected = IOException::class)
    fun parseBrokenSubscriptions()
    {
        DefaultSubscriptions.fromStream(getTestResourceAsStream(brokenSubscriptions))
    }

    @Test
    fun parseValidSubscriptions()
    {
        val subscriptions = DefaultSubscriptions.fromStream(
                RuntimeEnvironment.application.resources.openRawResource(R.raw.subscriptions))
        Assert.assertTrue(subscriptions.getForUrl(EASYLIST_GERMANY_COMPLETE_URL)!!.title.isNotBlank())
        Assert.assertTrue(subscriptions.getForUrl(EASYLIST_GERMANY_COMPLETE_URL)!!.url.isNotBlank())
        Assert.assertTrue(subscriptions.getForUrl(EASYLIST_GERMANY_COMPLETE_URL)!!.author.isNotBlank())
        Assert.assertTrue(subscriptions.getForUrl(EASYLIST_GERMANY_COMPLETE_URL)!!.prefixes.isNotBlank())
        Assert.assertTrue(subscriptions.getForUrl(EASYLIST_GERMANY_COMPLETE_URL)!!.specialization.isNotBlank())
        Assert.assertTrue(subscriptions.getForUrl(EASYLIST_GERMANY_COMPLETE_URL)!!.homepage.isNotBlank())
        Assert.assertTrue(subscriptions.getForUrl(EASYLIST_GERMANY_COMPLETE_URL)!!.type.isNotBlank())
    }

    private fun getTestResourceAsStream(path: String): InputStream? {
        return javaClass.getResourceAsStream(path)
    }
}