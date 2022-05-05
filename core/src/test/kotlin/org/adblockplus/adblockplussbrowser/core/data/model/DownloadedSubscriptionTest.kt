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