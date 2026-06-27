package com.emeth.kernel.spine

import android.net.Uri
import java.util.UUID

object AirOsContract {
    const val AUTHORITY = "com.airos.spine"
    const val PERMISSION = "com.airos.permission.ACCESS_SPINE"
    val BASE_URI: Uri = Uri.parse("content://$AUTHORITY")

    object Paths {
        const val NODES = "nodes"
        const val MEMORIES = "memories"
        const val UI_MAPS = "ui_maps"
        const val ACCESS = "access"
        const val BEHAVIORS = "behaviors"
    }

    object Columns {
        const val ID = "id"
        const val NODE_ID = "node_id"
        const val KIND = "kind"
        const val KEY = "key"
        const val VALUE = "value"
        const val PAYLOAD = "payload"
        const val SOURCE = "source"
        const val VERSION = "version"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
    }

    fun uri(path: String): Uri = BASE_URI.buildUpon().appendPath(path).build()
}

object SharedId {
    fun newId(namespace: String): String {
        val safeNamespace = namespace.lowercase().replace(Regex("[^a-z0-9._-]"), "_")
        return "air:$safeNamespace:" + UUID.randomUUID()
    }

    fun stableId(namespace: String, key: String): String {
        val seed = "$namespace:$key".toByteArray(Charsets.UTF_8)
        return "air:$namespace:" + UUID.nameUUIDFromBytes(seed)
    }
}
