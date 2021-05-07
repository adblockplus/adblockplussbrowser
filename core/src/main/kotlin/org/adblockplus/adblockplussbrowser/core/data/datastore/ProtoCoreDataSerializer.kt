package org.adblockplus.adblockplussbrowser.core.data.datastore

import androidx.datastore.core.Serializer
import org.adblockplus.adblockplussbrowser.core.data.proto.ProtoCoreData
import java.io.InputStream
import java.io.OutputStream

internal object ProtoCoreDataSerializer : Serializer<ProtoCoreData> {

    override val defaultValue: ProtoCoreData
        get() = ProtoCoreData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtoCoreData {
        return ProtoCoreData.parseFrom(input)
    }

    override suspend fun writeTo(t: ProtoCoreData, output: OutputStream) {
        t.writeTo(output)
    }
}