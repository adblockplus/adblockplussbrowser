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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import okio.buffer
import okio.sink
import okio.source
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.work.UpdateSubscriptionsWorker
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import timber.log.Timber
import java.io.File

internal class FilterListContentProvider : ContentProvider(), CoroutineScope {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FilterListContentProviderEntryPoint {
        fun getSettingsRepository(): SettingsRepository
        fun getCoreRepository(): CoreRepository
    }

    override val coroutineContext = Dispatchers.IO + SupervisorJob()

    lateinit var settingsRepository: SettingsRepository
    lateinit var coreRepository: CoreRepository

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun onCreate(): Boolean {
        val entryPoint = EntryPointAccessors.fromApplication(
            requireContext(this),
            FilterListContentProviderEntryPoint::class.java
        )
        settingsRepository = entryPoint.getSettingsRepository()
        coreRepository = entryPoint.getCoreRepository()

        launch {
            initAsync()

            settingsRepository.settings.onEach { settings ->
                Timber.d("Settings: $settings")
            }.launchIn(this)

            launch {
                coreRepository.data.onEach { coreData ->
                    Timber.d("Core data: $coreData")
                }.launchIn(this)
            }
        }
        return true
    }

    private suspend fun initAsync() = coroutineScope {
            launch {
                val settings = settingsRepository.settings.take(1).single()

                //if (settings.initialized) return@launch

                val subscriptions = settingsRepository.getDefaultPrimarySubscriptions() +
                        settingsRepository.getDefaultOtherSubscriptions()

                setupFilterLists(settings, subscriptions)

                Timber.d("After setup")
            }
            Timber.d("After launch")
        }

    private suspend fun setupFilterLists(settings: Settings, subscriptions: List<Subscription>) {
        val active = subscriptions.filter { subscription ->
            subscription.title == "EasyList" || subscription.title == "Acceptable Ads"
        }

        with(settingsRepository) {
            setAcceptableAdsEnabled(true)
            setAdblockEnabled(true)
            setActivePrimarySubscriptions(active)
        }

        context?.let {
            UpdateSubscriptionsWorker.scheduleOneTime(it)
            UpdateSubscriptionsWorker.schedule(it, settings.updateConfig)
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        return try {
            Timber.d("Filter list requested: $uri - $mode...")
            val file = getFilterFile()
            Timber.d("Returning ${file.absolutePath}")
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private fun getFilterFile(): File {
        val path = coreRepository.subscriptionsPath
        // We have a current file, return it
        if (!path.isNullOrEmpty()) {
            return File(path)
        }

        val context = requireContext(this)
        val directory = File(context.filesDir, "cache")
        directory.mkdirs()
        val file = File(directory, DEFAULT_SUBSCRIPTIONS_FILENAME)
        if (file.exists()) {
            return file
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

        temp.renameTo(file)
        return file
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