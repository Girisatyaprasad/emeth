package com.emeth.kernel.spine

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AirOsSpineDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    "air_os_spine.db",
    null,
    2
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE nodes (
                id TEXT PRIMARY KEY,
                kind TEXT NOT NULL,
                key TEXT NOT NULL,
                value TEXT,
                version INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        createSharedTable(db, AirOsContract.Paths.MEMORIES)
        createSharedTable(db, AirOsContract.Paths.UI_MAPS)
        createSharedTable(db, AirOsContract.Paths.ACCESS)
        createSharedTable(db, AirOsContract.Paths.BEHAVIORS)
        createSharedTable(db, AirOsContract.Paths.IDENTITY)
        db.execSQL("CREATE INDEX memories_kind_time ON memories(kind, created_at DESC)")
        db.execSQL("CREATE INDEX ui_maps_node_key ON ui_maps(node_id, key)")
        db.execSQL("CREATE INDEX access_node_key ON access(node_id, key)")
        db.execSQL("CREATE INDEX behaviors_kind_time ON behaviors(kind, created_at DESC)")
        db.execSQL("CREATE INDEX identity_node_key ON identity(node_id, key)")
    }

    private fun createSharedTable(db: SQLiteDatabase, table: String) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $table (
                id TEXT PRIMARY KEY,
                node_id TEXT NOT NULL,
                kind TEXT NOT NULL,
                key TEXT,
                value TEXT,
                payload TEXT,
                source TEXT NOT NULL,
                version INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createSharedTable(db, AirOsContract.Paths.IDENTITY)
            db.execSQL("CREATE INDEX IF NOT EXISTS identity_node_key ON identity(node_id, key)")
        }
    }
}
