package org.adblockplus.adblockplussbrowser.settings.data.datastore

import androidx.datastore.core.Serializer
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoSettings
import java.io.InputStream
import java.io.OutputStream

internal object ProtoSettingsSerializer : Serializer<ProtoSettings> {

    override val defaultValue: ProtoSettings
        get() = ProtoSettings.getDefaultInstance().toBuilder().apply {
            adblockEnabled = true
            acceptableAdsEnabled = true
        }.build()

    override suspend fun readFrom(input: InputStream): ProtoSettings {
        return ProtoSettings.parseFrom(input)
    }

    override suspend fun writeTo(t: ProtoSettings, output: OutputStream) {
        t.writeTo(output)
    }
}