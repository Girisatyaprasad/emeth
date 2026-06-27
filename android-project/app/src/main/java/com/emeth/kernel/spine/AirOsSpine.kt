package com.emeth.kernel.spine

import android.content.ContentValues
import android.content.Context
import org.json.JSONObject

class AirOsSpine(context: Context) {
    private val resolver = context.applicationContext.contentResolver
    val nodeId: String = SharedId.stableId("node", context.packageName)
    private val source = context.packageName

    fun registerNode(kind: String, label: String, version: Int) {
        val values = ContentValues().apply {
            put(AirOsContract.Columns.ID, nodeId)
            put(AirOsContract.Columns.KIND, kind)
            put(AirOsContract.Columns.KEY, source)
            put(AirOsContract.Columns.VALUE, label)
            put(AirOsContract.Columns.VERSION, version)
        }
        val updated = resolver.update(
            AirOsContract.uri(AirOsContract.Paths.NODES),
            values,
            AirOsContract.Columns.ID + " = ?",
            arrayOf(nodeId)
        )
        if (updated == 0) resolver.insert(AirOsContract.uri(AirOsContract.Paths.NODES), values)
    }

    fun remember(kind: String, value: String, payload: JSONObject = JSONObject()): String {
        return append(AirOsContract.Paths.MEMORIES, kind, null, value, payload)
    }

    fun publishUiMap(key: String, payload: JSONObject): String {
        return upsertMap(AirOsContract.Paths.UI_MAPS, "ui_map", key, null, payload)
    }

    fun publishAccess(key: String, value: String, payload: JSONObject = JSONObject()): String {
        return upsertMap(AirOsContract.Paths.ACCESS, "access_mechanism", key, value, payload)
    }

    fun emitBehavior(kind: String, payload: JSONObject): String {
        return append(AirOsContract.Paths.BEHAVIORS, kind, null, null, payload)
    }

    private fun append(
        path: String,
        kind: String,
        key: String?,
        value: String?,
        payload: JSONObject
    ): String {
        val id = SharedId.newId(path)
        val values = ContentValues().apply {
            put(AirOsContract.Columns.ID, id)
            put(AirOsContract.Columns.NODE_ID, nodeId)
            put(AirOsContract.Columns.KIND, kind)
            key?.let { put(AirOsContract.Columns.KEY, it) }
            value?.let { put(AirOsContract.Columns.VALUE, it) }
            put(AirOsContract.Columns.PAYLOAD, payload.toString())
            put(AirOsContract.Columns.SOURCE, source)
            put(AirOsContract.Columns.VERSION, 1)
        }
        resolver.insert(AirOsContract.uri(path), values)
        return id
    }

    private fun upsertMap(
        path: String,
        kind: String,
        key: String,
        value: String?,
        payload: JSONObject
    ): String {
        val id = SharedId.stableId(path, "$nodeId:$key")
        val values = ContentValues().apply {
            put(AirOsContract.Columns.ID, id)
            put(AirOsContract.Columns.NODE_ID, nodeId)
            put(AirOsContract.Columns.KIND, kind)
            put(AirOsContract.Columns.KEY, key)
            value?.let { put(AirOsContract.Columns.VALUE, it) }
            put(AirOsContract.Columns.PAYLOAD, payload.toString())
            put(AirOsContract.Columns.SOURCE, source)
            put(AirOsContract.Columns.VERSION, 1)
        }
        val updated = resolver.update(
            AirOsContract.uri(path),
            values,
            AirOsContract.Columns.ID + " = ?",
            arrayOf(id)
        )
        if (updated == 0) resolver.insert(AirOsContract.uri(path), values)
        return id
    }
}
