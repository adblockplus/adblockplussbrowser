package org.adblockplus.adblockplussbrowser.settings.data.datastore

import androidx.datastore.core.Serializer
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import java.io.InputStream
import java.io.OutputStream

object SettingsSerializer : Serializer<Settings> {

    override val defaultValue: Settings
        get() = Settings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Settings {
        return Settings.parseFrom(input)
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        t.writeTo(output)
    }
}