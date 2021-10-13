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