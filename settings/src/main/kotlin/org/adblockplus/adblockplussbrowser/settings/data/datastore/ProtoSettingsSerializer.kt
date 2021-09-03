package org.adblockplus.adblockplussbrowser.settings.data.datastore

import androidx.datastore.core.Serializer
import org.adblockplus.adblockplussbrowser.settings.data.proto.ProtoSettings
import java.io.InputStream
import java.io.OutputStream

internal class ProtoSettingsSerializer: Serializer<ProtoSettings> {

    // The actual default values are set by ProtoSettingsMigration
    override val defaultValue: ProtoSettings = ProtoSettings.getDefaultInstance()

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun readFrom(input: InputStream): ProtoSettings = ProtoSettings.parseFrom(input)

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: ProtoSettings, output: OutputStream) = t.writeTo(output)
}