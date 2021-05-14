package org.adblockplus.adblockplussbrowser.settings.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.runBlocking
import org.adblockplus.adblockplussbrowser.settings.data.local.SubscriptionsDataSource
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoSettings
import org.adblockplus.adblockplussbrowser.settings.data.proto.toProtoSubscription
import java.io.InputStream
import java.io.OutputStream

private const val DEFAULT_VALUE_EXCEPTION_MESSAGE = "Workaround exception to provide a default value within a coroutine"

internal class ProtoSettingsSerializer(private val subscriptionsDataSource: SubscriptionsDataSource) :
    Serializer<ProtoSettings> {

    // We rely on readFrom to return our default value
    override val defaultValue: ProtoSettings
        get() = throw CorruptionException(DEFAULT_VALUE_EXCEPTION_MESSAGE)

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun readFrom(input: InputStream): ProtoSettings {
        android.util.Log.i("ABPTEST", "READFROM")
        return ProtoSettings.parseFrom(input)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: ProtoSettings, output: OutputStream) {
        android.util.Log.i("ABPTEST", "WRITETO")
        t.writeTo(output)
    }

    fun provideDefaultValue(exception: CorruptionException): ProtoSettings =
        if (exception.message == DEFAULT_VALUE_EXCEPTION_MESSAGE) {
            android.util.Log.i("ABPTEST", "CREATING DEFAULT 1")
            runBlocking {
                android.util.Log.i("ABPTEST", "CREATING DEFAULT 2 ${subscriptionsDataSource.getDefaultActiveSubscription().url}")
                ProtoSettings.getDefaultInstance().toBuilder().apply {
                    adblockEnabled = true
                    acceptableAdsEnabled = true
                    addActivePrimarySubscriptions(
                        subscriptionsDataSource.getDefaultActiveSubscription().toProtoSubscription()
                    )
                }.build()
            }
        } else {
            android.util.Log.i("ABPTEST", "THROW EXCEPTION")
            throw exception
        }
}
