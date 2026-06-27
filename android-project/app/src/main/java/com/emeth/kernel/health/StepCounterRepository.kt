package com.emeth.kernel.health

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object StepCounterRepository : SensorEventListener {
    private const val PREFS = "air_os_steps"
    private const val RAW = "raw"
    private const val BASELINE = "baseline"
    private const val DATE = "date"

    @Volatile
    private var appContext: Context? = null

    fun start(context: Context): Boolean {
        val safeContext = context.applicationContext
        if (!hasPermission(safeContext)) return false
        val manager = safeContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return false
        appContext = safeContext
        return manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun read(context: Context, timeoutSeconds: Long = 2): Int? {
        val safeContext = context.applicationContext
        if (!hasPermission(safeContext)) return null
        current(safeContext)?.let { return it }

        val manager = safeContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return null
        val latch = CountDownLatch(1)
        var value: Int? = null
        val oneShot = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                store(safeContext, event.values[0])
                value = current(safeContext)
                latch.countDown()
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        manager.registerListener(oneShot, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        latch.await(timeoutSeconds, TimeUnit.SECONDS)
        manager.unregisterListener(oneShot)
        return value ?: current(safeContext)
    }

    override fun onSensorChanged(event: SensorEvent) {
        appContext?.let { store(it, event.values[0]) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun store(context: Context, rawValue: Float) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = LocalDate.now().toString()
        val storedDate = prefs.getString(DATE, null)
        val baseline = when {
            storedDate == today -> prefs.getFloat(BASELINE, 0f)
            storedDate == null -> 0f
            else -> rawValue
        }
        prefs.edit()
            .putString(DATE, today)
            .putFloat(BASELINE, baseline)
            .putFloat(RAW, rawValue)
            .apply()
    }

    private fun current(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(RAW)) return null
        val raw = prefs.getFloat(RAW, 0f)
        val baseline = prefs.getFloat(BASELINE, 0f)
        return (raw - baseline).toInt().coerceAtLeast(0)
    }

    private fun hasPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
