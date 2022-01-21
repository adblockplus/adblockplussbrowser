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

package org.adblockplus.adblockplussbrowser.settings.data.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataMigration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.settings.data.local.SubscriptionsDataSource
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoSettings
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoSubscription
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoUpdateConfig
import org.adblockplus.adblockplussbrowser.settings.data.proto.toProtoSubscription
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream

internal class ProtoSettingsMigration(
    private val context: Context,
    private val subscriptionsDataSource: SubscriptionsDataSource
) : DataMigration<ProtoSettings> {

    private val sharedPrefs: SharedPreferences by lazy { context.sharedPrefs }

    override suspend fun shouldMigrate(currentData: ProtoSettings): Boolean = !currentData.saved

    override suspend fun migrate(currentData: ProtoSettings): ProtoSettings =
        withContext(Dispatchers.IO) {
            currentData.toBuilder().apply {
                saved = true
                adblockEnabled = true
                acceptableAdsEnabled = sharedPrefs.acceptableAdsEnabled
                updateConfig = sharedPrefs.updateConfig
                addAllAllowedDomains(sharedPrefs.allowedDomains)
                val (primarySubscriptions, otherSubscriptions) = loadActiveSubscriptions()
                addAllActivePrimarySubscriptions(primarySubscriptions)
                addAllActiveOtherSubscriptions(otherSubscriptions)
                languagesOnboardingCompleted = isLanguageOnboardingPresumedComplete(primarySubscriptions)
                analyticsEnabled = true
            }.build()
        }

    private fun isLanguageOnboardingPresumedComplete(primarySubscriptions: List<ProtoSubscription>) =
            primarySubscriptions.size > 1

    override suspend fun cleanUp() {
        // We don't delete the default shared preferences file, only the old subscriptions meta and filter files
        context.subscriptionsDir.listFiles()?.let { files ->
            files.forEach { it.delete() }
        }
    }

    // Based on PreferenceManager.getDefaultSharedPreferencesName()
    private val Context.sharedPrefsName: String
        get() = "${packageName}_preferences"

    private val Context.sharedPrefs: SharedPreferences
        get() = this.getSharedPreferences(this.sharedPrefsName, Context.MODE_PRIVATE)

    private val Context.subscriptionsDir: File
        get() = File(this.filesDir, "subscriptions")

    private val SharedPreferences.applicationActivated: Boolean
        get() = this.getBoolean("application_activated", false)

    private val SharedPreferences.acceptableAdsEnabled: Boolean
        get() = this.getBoolean("acceptable_ads", true)

    private val SharedPreferences.updateConfig: ProtoUpdateConfig
        get() {
            val prefValue = this.getString("automatic_updates", null)
            return if (prefValue == "1") ProtoUpdateConfig.WIFI_ONLY else ProtoUpdateConfig.ALWAYS
        }

    private val SharedPreferences.allowedDomains: Set<String>
        get() = this.getStringSet("whitelisted_websites", emptySet()) ?: emptySet()

    private suspend fun loadActiveSubscriptions(): Pair<List<ProtoSubscription>, List<ProtoSubscription>> {
        val primarySubscriptions = mutableListOf<ProtoSubscription>()
        val otherSubscriptions = mutableListOf<ProtoSubscription>()
        val defaultPrimarySubscriptions = subscriptionsDataSource.getDefaultPrimarySubscriptions()
        val defaultOtherSubscriptions = subscriptionsDataSource.getDefaultOtherSubscriptions()
        loadActiveUrls().forEach { url ->
            val primarySubscription = defaultPrimarySubscriptions.find { it.url == url }
            if (primarySubscription != null) {
                primarySubscriptions.add(primarySubscription.toProtoSubscription())
            } else {
                val otherSubscription = defaultOtherSubscriptions.find { it.url == url }
                if (otherSubscription != null) {
                    otherSubscriptions.add(otherSubscription.toProtoSubscription())
                } else {
                    otherSubscriptions.add(Subscription(url, url, 0L).toProtoSubscription())
                }
            }
        }
        if (primarySubscriptions.isEmpty() && !sharedPrefs.applicationActivated) {
            primarySubscriptions.add(subscriptionsDataSource.getDefaultActiveSubscription().toProtoSubscription())
        }
        return primarySubscriptions to otherSubscriptions
    }

    private fun loadActiveUrls(): List<String> {
        val activeUrls = mutableListOf<String>()
        for (file in context.subscriptionsDir.walkTopDown()) {
            if (file.absolutePath.endsWith(".meta")) {
                retrieveUrlIfActive(file)?.let { url ->
                    url.adjustUrl()?.let { activeUrls.add(it) }
                }
            }
        }
        return activeUrls
    }

    private fun retrieveUrlIfActive(file: File): String? {
        try {
            DataInputStream(BufferedInputStream(GZIPInputStream(FileInputStream(file)))).use { stream ->
                val url = stream.readUTF()
                val numEntries = stream.readInt()
                for (i in 0 until numEntries) {
                    val key = stream.readUTF()
                    val value = stream.readUTF()
                    if (key == "_enabled") {
                        return if (value == "true") url else null
                    }
                }
            }
        } catch (exception: IOException) {
            Timber.e(exception)
            return null
        }
        return null
    }

    private fun String?.adjustUrl(): String? =
        when (this) {
            // We are now using the language subscription lists without embedding easylist to save data, but there are
            // a few lists (liste_ar+liste_fr and ruadlist+easylist) that we still ship with easylist embedded
            "https://easylist-downloads.adblockplus.org/abpindo+easylist.txt" -> "https://easylist-downloads.adblockplus.org/abpindo.txt"
            "https://easylist-downloads.adblockplus.org/abpvn+easylist.txt" -> "https://easylist-downloads.adblockplus.org/abpvn.txt"
            "https://easylist-downloads.adblockplus.org/bulgarian_list+easylist.txt" -> "https://easylist-downloads.adblockplus.org/bulgarian_list.txt"
            "https://easylist-downloads.adblockplus.org/easylistchina+easylist.txt" -> "https://easylist-downloads.adblockplus.org/easylistchina.txt"
            "https://easylist-downloads.adblockplus.org/easylistczechslovak+easylist.txt" -> "https://easylist-downloads.adblockplus.org/easylistczechslovak.txt"
            "https://easylist-downloads.adblockplus.org/easylistdutch+easylist.txt" -> "https://easylist-downloads.adblockplus.org/easylistdutch.txt"
            "https://easylist-downloads.adblockplus.org/easylistgermany+easylist.txt" -> "https://easylist-downloads.adblockplus.org/easylistgermany.txt"
            "https://easylist-downloads.adblockplus.org/israellist+easylist.txt" -> "https://easylist-downloads.adblockplus.org/israellist.txt"
            "https://easylist-downloads.adblockplus.org/easylistitaly+easylist.txt" -> "https://easylist-downloads.adblockplus.org/easylistitaly.txt"
            "https://easylist-downloads.adblockplus.org/easylistlithuania+easylist.txt" -> "https://easylist-downloads.adblockplus.org/easylistlithuania.txt"
            "https://easylist-downloads.adblockplus.org/easylistpolish+easylist.txt" -> "https://easylist-downloads.adblockplus.org/easylistpolish.txt"
            "https://easylist-downloads.adblockplus.org/easylistportuguese+easylist.txt" -> "https://easylist-downloads.adblockplus.org/easylistportuguese.txt"
            "https://easylist-downloads.adblockplus.org/easylistspanish+easylist.txt" -> "https://easylist-downloads.adblockplus.org/easylistspanish.txt"
            "https://easylist-downloads.adblockplus.org/indianlist+easylist.txt" -> "https://easylist-downloads.adblockplus.org/indianlist.txt"
            "https://easylist-downloads.adblockplus.org/koreanlist+easylist.txt" -> "https://easylist-downloads.adblockplus.org/koreanlist.txt"
            "https://easylist-downloads.adblockplus.org/latvianlist+easylist.txt" -> "https://easylist-downloads.adblockplus.org/latvianlist.txt"
            "https://easylist-downloads.adblockplus.org/liste_fr+easylist.txt" -> "https://easylist-downloads.adblockplus.org/liste_fr.txt"
            "https://easylist-downloads.adblockplus.org/rolist+easylist.txt" -> "https://easylist-downloads.adblockplus.org/rolist.txt"
            // We don't migrate AA as a subscription. We have a special field for it
            "https://easylist-downloads.adblockplus.org/exceptionrules.txt" -> null
            // We don't migrate notification.json as a subscription
            "https://notification.adblockplus.org/notification.json" -> null
            else -> this
        }
}