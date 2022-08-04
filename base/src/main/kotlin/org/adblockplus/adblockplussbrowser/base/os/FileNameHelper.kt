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

package org.adblockplus.adblockplussbrowser.base.os

import android.app.Activity
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Resolves file name from a given uri.
 *
 * @param uri Uri to local file
 * @return filename extracted from a given uri
 */
fun Activity.resolveFilename(uri: Uri): String {
    val cursor = contentResolver?.query(uri, null, null, null, null)
    var filename: String = uri.path.toString()

    cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)?.let { nameIndex ->
        cursor.moveToFirst()

        filename = cursor.getString(nameIndex)
        cursor.close()
    }

    return filename
}
