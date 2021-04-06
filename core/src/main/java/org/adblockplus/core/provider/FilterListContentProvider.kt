package org.adblockplus.core.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import java.io.File

class FilterListContentProvider : ContentProvider() {

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun onCreate(): Boolean {
        // TODO - add any initialization code
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        Timber.d("Filter list requested: ${uri.toString()} - $mode...")
        val file = getFilterFile()
        Timber.d("Returning ${file.absolutePath}")
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private fun getFilterFile(): File {
        val file = File(context!!.filesDir, "easylist.txt")
        if (file.exists()) return file

        val ins = context!!.assets.open("easylist.txt")
        ins.source().use { a ->
            file.sink().buffer().use { b -> b.writeAll(a) }
        }

        return file
    }
}