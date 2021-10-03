package org.adblockplus.adblockplussbrowser.core.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.ContentProviderCompat.requireContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import okio.buffer
import okio.sink
import okio.source
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.usercounter.CountUserResult
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounter
import timber.log.Timber
import java.io.File
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
        fun getUserCounter(): UserCounter
    }

    override val coroutineContext = Dispatchers.IO + SupervisorJob()

    lateinit var coreRepository: CoreRepository
    lateinit var activationPreferences: ActivationPreferences
    lateinit var userCounter: UserCounter

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun onCreate(): Boolean {
        val entryPoint = EntryPointAccessors.fromApplication(
            requireContext(this),
            FilterListContentProviderEntryPoint::class.java
        )
        coreRepository = entryPoint.getCoreRepository()
        activationPreferences = entryPoint.getActivationPreferences()
        analyticsProvider = entryPoint.getAnalyticsProvider()
        userCounter = entryPoint.getUserCounter()

        return true
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        // Set as Activated... If Samsung Internet is asking for the Filters, it is enabled
        launch {
            activationPreferences.updateLastFilterRequest(System.currentTimeMillis())
            launch {
                val initialBackOffDelay = 1000L
                val backOffFactor = 4
                val maxHeadRetries = 4
                var currentBackOffDelay = initialBackOffDelay
                repeat(maxHeadRetries) {
                    val result = userCounter.count()
                    if (result is CountUserResult.Success) {
                        Timber.i("User counted")
                        return@launch
                    } else if (result is CountUserResult.Skipped) {
                        Timber.i("User counting already done today")
                        return@launch
                    } else {
                        if (it < maxHeadRetries - 1) {
                            Timber.i("User counting failed, retrying with delay of %d ms",
                                currentBackOffDelay
                            )
                            delay(currentBackOffDelay) //backoff
                            currentBackOffDelay = (currentBackOffDelay * backOffFactor)
                        }
                    }
                }
            }
        }
        return try {
            Timber.i("Filter list requested: $uri - $mode...")
            analyticsProvider.logEvent(AnalyticsEvent.FILTER_LIST_REQUESTED)
            val file = getFilterFile()
            Timber.d("Returning ${file.absolutePath}")
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }

    private fun getFilterFile(): File {
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

        if (defaultFile.exists()) {
            return defaultFile
        }

        val temp = File.createTempFile("filters", ".txt", directory)

        var ins = context.assets.open("exceptionrules.txt")
        ins.source().use { a ->
            temp.sink().buffer().use { b -> b.writeAll(a) }
        }

        ins = context.assets.open("easylist.txt")
        ins.source().use { a ->
            temp.sink(append = true).buffer().use { b -> b.writeAll(a) }
        }

        temp.renameTo(defaultFile)
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
    }
}