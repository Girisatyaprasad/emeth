package com.emeth.kernel.memory

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MemoryDatabase private constructor(context: Context) : 
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION), MemoryDao {

    companion object {
        private const val DATABASE_NAME = "emeth_memory_db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "memory_entries"

        @Volatile
        private var INSTANCE: MemoryDatabase? = null

        fun getDatabase(context: Context): MemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = MemoryDatabase(context)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                id TEXT PRIMARY KEY NOT NULL,
                type TEXT NOT NULL,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                metadata TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun memoryDao(): MemoryDao = this

    override fun insert(entry: MemoryEntry) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", entry.id)
            put("type", entry.type)
            put("content", entry.content)
            put("timestamp", entry.timestamp)
            put("metadata", entry.metadata)
        }
        db.insert(TABLE_NAME, null, values)
    }

    private fun mapCursorToEntries(cursor: android.database.Cursor): List<MemoryEntry> {
        val entries = mutableListOf<MemoryEntry>()
        if (cursor.moveToFirst()) {
            do {
                entries.add(
                    MemoryEntry(
                        id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                        metadata = cursor.getString(cursor.getColumnIndexOrThrow("metadata"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return entries
    }

    override fun getByType(type: String): List<MemoryEntry> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE type = ? ORDER BY timestamp DESC",
            arrayOf(type)
        )
        return mapCursorToEntries(cursor)
    }

    override fun getByTimeRange(startTime: Long, endTime: Long): List<MemoryEntry> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC",
            arrayOf(startTime.toString(), endTime.toString())
        )
        return mapCursorToEntries(cursor)
    }

    override fun searchByKeyword(keyword: String): List<MemoryEntry> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE content LIKE ? ORDER BY timestamp DESC",
            arrayOf("%${keyword}%")
        )
        return mapCursorToEntries(cursor)
    }

    override fun searchByTypeAndTime(type: String, startTime: Long, endTime: Long): List<MemoryEntry> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE type = ? AND timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC",
            arrayOf(type, startTime.toString(), endTime.toString())
        )
        return mapCursorToEntries(cursor)
    }

    override fun pruneOldEntries(type: String, cutoffTime: Long) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "type = ? AND timestamp < ?", arrayOf(type, cutoffTime.toString()))
    }

    override fun clearAll() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
    }
}
