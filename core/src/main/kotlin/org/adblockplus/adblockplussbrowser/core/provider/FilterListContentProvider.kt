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

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.ContentProviderCompat.requireContext
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
import okio.buffer
import okio.sink
import okio.source
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.base.os.PackageHelper
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants
import org.adblockplus.adblockplussbrowser.base.yandex.YandexConstants
import org.adblockplus.adblockplussbrowser.core.CallingApp
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.extensions.setBackoffTime
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounterWorker
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounterWorker.Companion.BACKOFF_TIME_S
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounterWorker.Companion.USER_COUNTER_KEY_ONESHOT_WORK
import org.tukaani.xz.XZIOException
import org.tukaani.xz.XZInputStream
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class FilterListContentProvider : ContentProvider(), CoroutineScope {

    lateinit var analyticsProvider: AnalyticsProvider

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FilterListContentProviderEntryPoint {
        fun getCoreRepository(): CoreRepository
        fun getActivationPreferences(): ActivationPreferences
        fun getAnalyticsProvider(): AnalyticsProvider
    }

    override val coroutineContext = Dispatchers.IO + SupervisorJob()

    lateinit var coreRepository: CoreRepository
    lateinit var activationPreferences: ActivationPreferences
    lateinit var workManager: WorkManager

    var defaultSubscriptionsUnpackingDone: CountDownLatch = CountDownLatch(1)

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun onCreate(): Boolean {
        val context = requireContext(this)
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            FilterListContentProviderEntryPoint::class.java
        )
        coreRepository = entryPoint.getCoreRepository()
        activationPreferences = entryPoint.getActivationPreferences()
        analyticsProvider = entryPoint.getAnalyticsProvider()
        workManager = WorkManager.getInstance(context)
        prepareDefaultSubscriptions()
        return true
    }

    private fun triggerUserCountingRequest(callingApp: CallingApp) {
        val request = OneTimeWorkRequestBuilder<UserCounterWorker>().apply {
            setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            setBackoffTime(Duration.seconds(BACKOFF_TIME_S))
            setInputData(callingApp())
        }.build()

        // REPLACE old enqueued works
        workManager.enqueueUniqueWork(
            USER_COUNTER_KEY_ONESHOT_WORK,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
        Timber.d("USER COUNTER JOB SCHEDULED")
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        // Set as Activated... If Samsung Internet is asking for the Filters, it is enabled
        val callingApp = getCallingApp(callingPackage, context?.packageManager)
        launch {
            activationPreferences.updateLastFilterRequest(System.currentTimeMillis())
            triggerUserCountingRequest(callingApp)
        }
        return try {
            Timber.i("Filter list requested: $uri - $mode...")
            analyticsProvider.logEvent(AnalyticsEvent.FILTER_LIST_REQUESTED)
            val file = getFilterFile()
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

    private fun getDefaultSubscriptionsDir(): File {
        val context = requireContext(this)
        val directory = File(context.filesDir, "cache")
        directory.mkdirs()
        return directory
    }

    private fun unpackDefaultSubscriptions()
    {
        val context = requireContext(this)
        val directory = getDefaultSubscriptionsDir()
        val defaultFile = File(directory, DEFAULT_SUBSCRIPTIONS_FILENAME)
        val temp = File.createTempFile("filters", ".txt", directory)
        val start = Duration.milliseconds(System.currentTimeMillis())

        Timber.d("getFilterFile: unpacking")
        try {
            var ins = context.assets.open("exceptionrules.txt.xz")
            /*
                XZInputStream params:
                    - Input stream
                    - memory limit expressed in kibibytes (KiB)
                        The worst-case memory usage of XZInputStream is currently 1.5 GiB.
                        Still, very few files will require more than about 65 MiB.
                        To calculate KiB multiply the digital storage value by 1024. E.g.: 65 MiB * 1024
             */
            val xzInputStream = XZInputStream(ins, 100 * 1024) // We use 100 to not be in the limit
            xzInputStream.source().use { a ->
                temp.sink().buffer().use { b -> b.writeAll(a) }
            }

            ins = context.assets.open("easylist.txt")
            ins.source().use { a ->
                temp.sink(append = true).buffer().use { b -> b.writeAll(a) }
            }
            temp.renameTo(defaultFile)
            Timber.d("getFilterFile: unpacked, elapsed: ${Duration.milliseconds(System.currentTimeMillis()) - start}")
        } catch (ex: Exception) {
            when (ex) {
                is IOException, is XZIOException -> {
                    Timber.e(ex)
                    defaultFile.delete()
                    temp.delete()
                }
            }
        }
        defaultSubscriptionsUnpackingDone.countDown()
    }

    private fun prepareDefaultSubscriptions() {
        val directory = getDefaultSubscriptionsDir()
        val defaultFile = File(directory, DEFAULT_SUBSCRIPTIONS_FILENAME)
        val path = coreRepository.subscriptionsPath

        // We have a current file with downloaded subscriptions, return it
        if (!path.isNullOrEmpty() && File(path).exists()) {
            defaultFile.delete()
        } else if (!defaultFile.exists()) {
            launch {
                unpackDefaultSubscriptions()
            }
            return
        }
        defaultSubscriptionsUnpackingDone.countDown()
    }

    private fun getFilterFile(): File {
        Timber.i("getFilterFile")
        val context = requireContext(this)
        val directory = File(context.filesDir, "cache")
        directory.mkdirs()
        val defaultFile = File(directory, DEFAULT_SUBSCRIPTIONS_FILENAME)
        val path = coreRepository.subscriptionsPath
        // We have a current file, return it
        if (!path.isNullOrEmpty() && File(path).exists()) {
            defaultFile.delete()
            return File(path)
        }

        defaultSubscriptionsUnpackingDone.await()

        if (!defaultFile.exists()) {
            defaultFile.createNewFile()
        }
        return defaultFile
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
    }
}
