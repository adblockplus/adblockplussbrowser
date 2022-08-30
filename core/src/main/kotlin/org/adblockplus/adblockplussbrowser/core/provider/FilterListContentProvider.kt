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

package org.adblockplus.adblockplussbrowser.core.provider

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.sink
import okio.source
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.base.os.PackageHelper
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants
import org.adblockplus.adblockplussbrowser.base.yandex.YandexConstants
import org.adblockplus.adblockplussbrowser.core.BuildConfig
import org.adblockplus.adblockplussbrowser.core.CallingApp
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.extensions.currentData
import org.adblockplus.adblockplussbrowser.core.extensions.currentSettings
import org.adblockplus.adblockplussbrowser.core.extensions.toAllowRule
import org.adblockplus.adblockplussbrowser.core.usercounter.OkHttpUserCounter
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounterWorker
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounterWorker.Companion.BACKOFF_TIME_MINUTES
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounterWorker.Companion.USER_COUNTER_KEY_ONESHOT_WORK
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.tukaani.xz.XZInputStream
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.ParseException
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

// TODO: Documentation
@ExperimentalTime
internal class FilterListContentProvider : ContentProvider(), CoroutineScope {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FilterListContentProviderEntryPoint {
        fun getCoreRepository(): CoreRepository
        fun getSettingsRepository(): SettingsRepository
        fun getActivationPreferences(): ActivationPreferences
        fun getAnalyticsProvider(): AnalyticsProvider
    }

    private val entrypoint: FilterListContentProviderEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            requireContext(this@FilterListContentProvider),
            FilterListContentProviderEntryPoint::class.java
        )
    }
    private val coreRepository: CoreRepository by lazy {
        entrypoint.getCoreRepository()
    }

    private val settingsRepository: SettingsRepository by lazy {
        entrypoint.getSettingsRepository()
    }

    private val activationPreferences: ActivationPreferences by lazy {
        entrypoint.getActivationPreferences()
    }

    val analyticsProvider: AnalyticsProvider by lazy {
        entrypoint.getAnalyticsProvider()
    }

    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(requireContext(this@FilterListContentProvider))
    }

    private val defaultSubscriptionDir: File by lazy {
        val context = requireContext(this@FilterListContentProvider)
        val directory = File(context.filesDir, "cache")
        directory.mkdirs()
        directory
    }

    private val defaultSubscriptionFile: File by lazy {
        File(defaultSubscriptionDir, DEFAULT_SUBSCRIPTIONS_FILENAME)
    }

    override val coroutineContext = Dispatchers.IO + SupervisorJob()

    override fun onCreate() = true

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    private fun triggerUserCountingRequest(callingApp: CallingApp) {
        val request = OneTimeWorkRequestBuilder<UserCounterWorker>().apply {
            setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_TIME_MINUTES, TimeUnit.MINUTES)
            setInputData(callingApp())
        }.build()

        // REPLACE old enqueued works
        workManager.enqueueUniqueWork(
            USER_COUNTER_KEY_ONESHOT_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
        Timber.d("USER COUNTER JOB SCHEDULED")
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        // Set as Activated... If Samsung Internet is asking for the Filters, it is enabled
        val callingApp = getCallingApp(callingPackage, context?.packageManager)
        launch {
            activationPreferences.updateLastFilterRequest(System.currentTimeMillis())
            val savedLastUserCountingResponse = coreRepository.currentData().lastUserCountingResponse
            if (!isUserCountedInCurrentCycle(savedLastUserCountingResponse)) {
                Timber.d("User count lastUserCountingResponse saved is `%d`", savedLastUserCountingResponse)
                triggerUserCountingRequest(callingApp)
            } else {
                Timber.d("Skip user counting")
            }
        }
        return try {
            Timber.i("Filter list requested: $uri - $mode...")
            analyticsProvider.logEvent(AnalyticsEvent.FILTER_LIST_REQUESTED)
            val file = getFilterFile()
            Timber.d("Open File file size ${file.length()}")
            Timber.d("Returning ${file.absolutePath}")
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (ex: Exception) {
            Timber.e(ex)
            analyticsProvider.logException(ex)
            null
        }
    }

    private fun getCallingApp(callingPackageName: String?, packageManager: PackageManager?): CallingApp {
        var application = DEFAULT_CALLING_APP_NAME
        var applicationVersion = DEFAULT_CALLING_APP_VERSION
        if (callingPackageName != null && packageManager != null) {
            Timber.i("User count callingPackageName $callingPackageName")
            application = when (callingPackageName) {
                SamsungInternetConstants.SBROWSER_APP_ID,
                SamsungInternetConstants.SBROWSER_APP_ID_BETA -> SamsungInternetConstants.SBROWSER_APP_NAME
                YandexConstants.YANDEX_PACKAGE_NAME,
                YandexConstants.YANDEX_BETA_PACKAGE_NAME,
                YandexConstants.YANDEX_ALPHA_PACKAGE_NAME -> YandexConstants.YANDEX_APP_NAME
                else -> DEFAULT_CALLING_APP_NAME
            }
            applicationVersion = PackageHelper.version(packageManager, callingPackageName)
        }
        return CallingApp(application, applicationVersion)
    }

    @SuppressLint("BinaryOperationInTimber")
    private fun unpackDefaultSubscriptions() {
        val context = requireContext(this)
        val temp = File.createTempFile("filters", ".txt", defaultSubscriptionDir)

        var acceptableAdsEnabled: Boolean
        var allowedDomains: List<String> = emptyList()
        runBlocking {
            acceptableAdsEnabled = settingsRepository.currentSettings().acceptableAdsEnabled
            if (BuildConfig.FLAVOR_product != BuildConfig.FLAVOR_CRYSTAL) {
                allowedDomains = settingsRepository.currentSettings().allowedDomains
                Timber.d("Adding allowedDomains (not Crystal): $allowedDomains")
            }
        }
        Timber.i("Is AA enabled: $acceptableAdsEnabled")

        try {
            createDefaultFilterFile(acceptableAdsEnabled, context, temp, allowedDomains)
        } catch (ex: IOException) {
            Timber.e(ex)
            defaultSubscriptionFile.delete()
            temp.delete()
        }
    }

    private fun createDefaultFilterFile(
        acceptableAdsEnabled: Boolean,
        context: Context,
        temp: File,
        allowedDomains: List<String>
    ) {
        var ins: InputStream
        if (acceptableAdsEnabled) {
            Timber.d("getFilterFile: unpacking AA")
            val start = Duration.milliseconds(System.currentTimeMillis())
            ins = context.assets.open("exceptionrules.txt.xz")
            /*
                    XZInputStream params:
                        - Input stream
                        - memory limit expressed in kilobytes (KiB)
                            The worst-case memory usage of XZInputStream is currently 1.5 GiB.
                            Still, very few files will require more than about 65 MiB.
                            To calculate, multiply the digital storage value by 1024. E.g.: 65 MiB * 1024
                 */
            val xzInputStream = XZInputStream(ins, XZ_MEMORY_LIMIT_KB)
            xzInputStream.source().use { a ->
                temp.sink().buffer().use { b -> b.writeAll(a) }
            }
            Timber.d(
                "getFilterFile: unpacked AA, elapsed: %s",
                (Duration.milliseconds(System.currentTimeMillis()) - start).toString()
            )
        }

        ins = context.assets.open("easylist.txt")
        ins.source().use { a ->
            temp.sink(append = true).buffer().use { b -> b.writeAll(a) }
        }

        allowedDomains.forEach { domain ->
            Timber.d("allowedDomain: $domain")
            temp.sink(append = true).buffer().use { sink ->
                sink.writeUtf8("\n")
                sink.writeUtf8(domain.toAllowRule())
            }
        }

        temp.sink(append = true).buffer().use { sink ->
            sink.writeUtf8("\n")
        }

        temp.renameTo(defaultSubscriptionFile)
    }

    private fun getFilterFile(): File {
        Timber.i("getFilterFile")
        defaultSubscriptionFile.delete()
        val path = coreRepository.subscriptionsPath

        // We have a current file, return it
        if (!path.isNullOrEmpty() && File(path).exists()) {
            return File(path)
        }

        unpackDefaultSubscriptions()

        return defaultSubscriptionFile
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    companion object {
        const val DEFAULT_SUBSCRIPTIONS_FILENAME = "default_subscriptions.txt"
        const val DEFAULT_CALLING_APP_NAME = "other"
        const val DEFAULT_CALLING_APP_VERSION = "0"

        /*
            Memory limit was calculated by using a 5x times bigger file and
            limiting the memory until the unpacking wouldn't work. That value 3x bigger.
            The result was it wouldn't work with less than 9 * 1024 so rounded up to 10 and
            multiplied it by 3.
         */
        const val XZ_MEMORY_LIMIT_KB = 30 * 1024

        private fun convertToTimestamp(stringToFormat: String): Long {
            return try {
                // TODO: Solve this warning, maybe a runCatching?
                val date: Date = OkHttpUserCounter.lastUserCountingResponseFormat.parse(stringToFormat)
                date.time
            } catch (e: ParseException) {
                0
            }
        }

        // There should be one user count request per 24h = 24*60*60*1000 ms = 86400000 ms
        // We are comparing device time and server time
        // subtract 15 min to compensate possible clock synchronization issues
        // 23h 45min = 86400000 - 15*60*1000 = 85500000 ms
        private const val USER_COUNTING_CYCLE = 85_500_000
        private fun isUserCountedInCurrentCycle(lastUserCount: Long): Boolean {
            val lastUserCountTimeStamp = convertToTimestamp(lastUserCount.toString())
            val periodSinceLastUserCount = System.currentTimeMillis() - lastUserCountTimeStamp
            Timber.i("User has been counted %d ms ago", periodSinceLastUserCount)
            return periodSinceLastUserCount < USER_COUNTING_CYCLE
        }
    }
}
