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

    companion object {
        private const val NON_EXISTING_RESOURCE = "not_existing.xml"
        private const val EMPTY_SUBSCRIPTIONS = "subscriptions_empty.xml"
        private const val BROKEN_SUBSCRIPTIONS = "subscriptions_malformed.xml"
        private const val EASYLIST_GERMANY_COMPLETE_URL =
                "https://easylist-downloads.adblockplus.org/easylistgermany+easylist.txt"
    }

    @Test
    fun openResourcesForTesting()
    {
        Assert.assertNull(getTestResourceAsStream(NON_EXISTING_RESOURCE))
        Assert.assertNotNull(getTestResourceAsStream(EMPTY_SUBSCRIPTIONS))
    }

    @Test
    fun parseEmptySubscriptions()
    {
        val subscriptions = DefaultSubscriptions.fromStream(getTestResourceAsStream(EMPTY_SUBSCRIPTIONS))
        Assert.assertNotNull(subscriptions)
        Assert.assertTrue(subscriptions!!.subscriptions.isEmpty())

        Assert.assertNull(subscriptions.getForUrl(EASYLIST_GERMANY_COMPLETE_URL))
    }

    @Test(expected = IOException::class)
    fun parseBrokenSubscriptions()
    {
        DefaultSubscriptions.fromStream(getTestResourceAsStream(BROKEN_SUBSCRIPTIONS))
    }

    @Test
    fun parseValidSubscriptions()
    {
        val subscriptions = DefaultSubscriptions.fromStream(
                RuntimeEnvironment.application.resources.openRawResource(R.raw.subscriptions))
        Assert.assertTrue(subscriptions?.getForUrl(EASYLIST_GERMANY_COMPLETE_URL)!!.title.isNotBlank())
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