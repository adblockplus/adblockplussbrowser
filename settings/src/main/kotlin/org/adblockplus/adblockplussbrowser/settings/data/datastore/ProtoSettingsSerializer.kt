package org.adblockplus.adblockplussbrowser.settings.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.runBlocking
import org.adblockplus.adblockplussbrowser.settings.data.local.SubscriptionsDataSource
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoSettings
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoUpdateConfig
import org.adblockplus.adblockplussbrowser.settings.data.proto.toProtoSubscription
import java.io.InputStream
import java.io.OutputStream

private const val DEFAULT_VALUE_EXCEPTION_MESSAGE = "Workaround exception to provide a default value within a coroutine"

internal class ProtoSettingsSerializer(private val subscriptionsDataSource: SubscriptionsDataSource) :
    Serializer<ProtoSettings> {

    // We rely on provideDefaultValue to return the default value within a coroutine scope
    // We can remove the workaround if the following feature request is incorporated to DataStore in the future:
    // https://issuetracker.google.com/issues/188096915
    override val defaultValue: ProtoSettings
        get() = throw CorruptionException(DEFAULT_VALUE_EXCEPTION_MESSAGE)

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun readFrom(input: InputStream): ProtoSettings = ProtoSettings.parseFrom(input)

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: ProtoSettings, output: OutputStream) = t.writeTo(output)

    fun provideDefaultValue(exception: CorruptionException): ProtoSettings =
        if (exception.message == DEFAULT_VALUE_EXCEPTION_MESSAGE) {
            runBlocking {
                ProtoSettings.getDefaultInstance().toBuilder().apply {
                    adblockEnabled = true
                    acceptableAdsEnabled = true
                    addActivePrimarySubscriptions(
                        subscriptionsDataSource.getDefaultActiveSubscription().toProtoSubscription()
                    )
                    updateConfig = ProtoUpdateConfig.ALWAYS
                    analyticsEnabled = true
                }.build()
            }
        } else {
            throw exception
        }
}
