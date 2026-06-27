package com.emeth.kernel.spine

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

class AirOsSpineProvider : ContentProvider() {
    private lateinit var database: AirOsSpineDatabase

    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext ?: return false
        database = AirOsSpineDatabase(appContext)
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val table = tableFor(uri)
        val now = System.currentTimeMillis()
        val safeValues = ContentValues(values ?: ContentValues()).apply {
            if (!containsKey(AirOsContract.Columns.ID)) {
                put(AirOsContract.Columns.ID, SharedId.newId(table))
            }
            if (!containsKey(AirOsContract.Columns.VERSION)) put(AirOsContract.Columns.VERSION, 1)
            if (!containsKey(AirOsContract.Columns.CREATED_AT)) put(AirOsContract.Columns.CREATED_AT, now)
            put(AirOsContract.Columns.UPDATED_AT, now)
        }
        val rowId = database.writableDatabase.insertOrThrow(table, null, safeValues)
        context?.contentResolver?.notifyChange(uri, null)
        return ContentUris.withAppendedId(uri, rowId)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val table = tableFor(uri)
        return database.readableDatabase.query(
            table,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder ?: AirOsContract.Columns.UPDATED_AT + " DESC"
        ).apply {
            setNotificationUri(requireNotNull(context).contentResolver, uri)
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val table = tableFor(uri)
        val safeValues = ContentValues(values ?: ContentValues()).apply {
            put(AirOsContract.Columns.UPDATED_AT, System.currentTimeMillis())
        }
        val count = database.writableDatabase.update(table, safeValues, selection, selectionArgs)
        if (count > 0) context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val count = database.writableDatabase.delete(tableFor(uri), selection, selectionArgs)
        if (count > 0) context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String {
        return "vnd.android.cursor.dir/vnd.airos." + tableFor(uri)
    }

    private fun tableFor(uri: Uri): String = when (MATCHER.match(uri)) {
        NODES -> AirOsContract.Paths.NODES
        MEMORIES -> AirOsContract.Paths.MEMORIES
        UI_MAPS -> AirOsContract.Paths.UI_MAPS
        ACCESS -> AirOsContract.Paths.ACCESS
        BEHAVIORS -> AirOsContract.Paths.BEHAVIORS
        else -> throw IllegalArgumentException("Unknown Air OS spine URI: $uri")
    }

    private companion object {
        const val NODES = 1
        const val MEMORIES = 2
        const val UI_MAPS = 3
        const val ACCESS = 4
        const val BEHAVIORS = 5

        val MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AirOsContract.AUTHORITY, AirOsContract.Paths.NODES, NODES)
            addURI(AirOsContract.AUTHORITY, AirOsContract.Paths.MEMORIES, MEMORIES)
            addURI(AirOsContract.AUTHORITY, AirOsContract.Paths.UI_MAPS, UI_MAPS)
            addURI(AirOsContract.AUTHORITY, AirOsContract.Paths.ACCESS, ACCESS)
            addURI(AirOsContract.AUTHORITY, AirOsContract.Paths.BEHAVIORS, BEHAVIORS)
        }
    }
}
