package com.emeth.kernel.watchers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.net.wifi.WifiManager
import android.bluetooth.BluetoothAdapter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.util.Calendar

class ConditionEvaluator(private val context: Context) {

    fun evaluate(watcher: Watcher): Boolean {
        return when (watcher.type) {
            WatcherType.TIME_OF_DAY -> evaluateTimeOfDay(watcher.condition)
            WatcherType.BATTERY_LEVEL -> evaluateBattery(watcher.condition)
            WatcherType.STORAGE_LEVEL -> evaluateStorage(watcher.condition)
            WatcherType.WIFI_DISCONNECT -> !isWifiConnected()
            WatcherType.BLUETOOTH_DISCONNECT -> !isBluetoothConnected()
            // Step count would require persistent tracking, simplified check
            WatcherType.STEP_COUNT -> evaluateSteps(watcher.condition)
            else -> false // Other complex listeners are handled via BroadcastReceivers ideally
        }
    }

    private fun evaluateTimeOfDay(condition: WatcherCondition): Boolean {
        val targetMinute = condition.targetValue?.toInt() ?: return false
        val now = Calendar.getInstance()
        val currentMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return currentMinute >= targetMinute && currentMinute < targetMinute + 15
    }

    private fun evaluateBattery(condition: WatcherCondition): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale.toFloat()

        return evaluateOp(batteryPct, condition)
    }

    private fun evaluateStorage(condition: WatcherCondition): Boolean {
        val stat = StatFs(Environment.getDataDirectory().path)
        val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
        val freeGb = freeBytes / (1024f * 1024f * 1024f)
        
        return evaluateOp(freeGb, condition)
    }

    private fun isWifiConnected(): Boolean {
        val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiMgr.isWifiEnabled // simplified
    }

    private fun isBluetoothConnected(): Boolean {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        return btAdapter?.isEnabled == true
    }

    private fun evaluateSteps(condition: WatcherCondition): Boolean {
        // Mocked or persistent reading would go here.
        // For actual steps, we need TYPE_STEP_COUNTER sensor and background tracking.
        return false // Requires permission ACTIVITY_RECOGNITION
    }

    private fun evaluateOp(current: Float, condition: WatcherCondition): Boolean {
        val target = condition.targetValue ?: return false
        return when (condition.op) {
            ConditionOp.GREATER_THAN_EQUAL -> current >= target
            ConditionOp.LESS_THAN_EQUAL -> current <= target
            ConditionOp.EQUAL -> current == target
            ConditionOp.TRIGGERED -> true
        }
    }
}
