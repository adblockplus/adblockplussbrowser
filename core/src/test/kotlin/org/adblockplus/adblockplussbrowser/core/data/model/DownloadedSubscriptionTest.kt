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

package org.adblockplus.adblockplussbrowser.core.data.model

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class DownloadedSubscriptionTest {

    @Test
    fun `test downloaded subscriptions exists`() {
        val downloadFilePath = File("/").path
        val downloadedSubscription = DownloadedSubscription (
            "www.google.com", path = downloadFilePath)
        assertNotNull(downloadedSubscription.ifExists())
    }

    @Test
    fun `test downloaded subscription does not exist`() {
        val downloadedSubscription = DownloadedSubscription ("www.google.com")
        assertNull(downloadedSubscription.ifExists())
    }
}