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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import okio.source
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.base.data.prefs.ActivationPreferences
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import timber.log.Timber
import java.io.File


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

        return true
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        // Set as Activated... If Samsung Internet is asking for the Filters, it is enabled
        launch {
            activationPreferences.activate()
        }
        return try {
            Timber.d("Filter list requested: $uri - $mode...")
            val file = getFilterFile()
            Timber.d("Returning ${file.absolutePath}")
            analyticsProvider.logEvent(AnalyticsEvent.FILTER_LIST_REQUESTED)
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (ex: Exception) {
            ex.printStackTrace()
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