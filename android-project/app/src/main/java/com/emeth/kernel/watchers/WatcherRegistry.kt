package com.emeth.kernel.watchers

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class WatcherRegistry(private val context: Context) {
    private val prefs = context.getSharedPreferences("emeth_watchers", Context.MODE_PRIVATE)

    fun addWatcher(watcher: Watcher) {
        val all = getAllWatchers().toMutableList()
        all.removeIf { it.id == watcher.id }
        all.add(watcher)
        saveAll(all)
    }

    fun removeWatcher(id: String) {
        val all = getAllWatchers().toMutableList()
        all.removeIf { it.id == id }
        saveAll(all)
    }

    fun removeSeededDemoWatchers() {
        val all = getAllWatchers().toMutableList()
        val changed = all.removeIf {
            (it.type == WatcherType.BATTERY_LEVEL && it.action.payload == "Battery is running low! Below 20%") ||
                (it.type == WatcherType.STORAGE_LEVEL && it.action.payload == "Storage is running low! Below 5GB")
        }
        if (changed) saveAll(all)
    }

    fun getAllWatchers(): List<Watcher> {
        val jsonStr = prefs.getString("watchers_json", "[]") ?: "[]"
        val array = JSONArray(jsonStr)
        val result = mutableListOf<Watcher>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val condObj = obj.getJSONObject("condition")
            val actObj = obj.getJSONObject("action")
            
            result.add(
                Watcher(
                    id = obj.getString("id"),
                    type = WatcherType.valueOf(obj.getString("type")),
                    condition = WatcherCondition(
                        op = ConditionOp.valueOf(condObj.getString("op")),
                        targetValue = if (condObj.has("targetValue")) condObj.getDouble("targetValue").toFloat() else null
                    ),
                    action = WatcherAction(
                        type = actObj.getString("type"),
                        payload = actObj.getString("payload")
                    ),
                    recurrence = if (obj.has("recurrence")) WatcherRecurrence.valueOf(obj.getString("recurrence")) else WatcherRecurrence.ONCE,
                    selectedDays = if (obj.has("selectedDays")) {
                        val daysArr = obj.getJSONArray("selectedDays")
                        val days = mutableListOf<Int>()
                        for (j in 0 until daysArr.length()) days.add(daysArr.getInt(j))
                        days
                    } else emptyList()
                )
            )
        }
        return result
    }

    private fun saveAll(watchers: List<Watcher>) {
        val array = JSONArray()
        for (w in watchers) {
            val cond = JSONObject().apply {
                put("op", w.condition.op.name)
                w.condition.targetValue?.let { put("targetValue", it.toDouble()) }
            }
            val act = JSONObject().apply {
                put("type", w.action.type)
                put("payload", w.action.payload)
            }
            val obj = JSONObject().apply {
                put("id", w.id)
                put("type", w.type.name)
                put("condition", cond)
                put("action", act)
                put("recurrence", w.recurrence.name)
                val daysArr = JSONArray()
                w.selectedDays.forEach { daysArr.put(it) }
                put("selectedDays", daysArr)
            }
            array.put(obj)
        }
        prefs.edit().putString("watchers_json", array.toString()).apply()
    }
}
