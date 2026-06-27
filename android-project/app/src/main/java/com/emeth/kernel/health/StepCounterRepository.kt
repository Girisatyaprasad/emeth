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
    private const val RAW = "raw" // the last raw value seen
    private const val STEPS_TODAY = "steps_today"
    private const val DATE = "date"
    private const val BASELINE = "baseline" // kept for migration

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
        
        var lastRaw = prefs.getFloat(RAW, rawValue)
        var stepsToday = if (prefs.contains(STEPS_TODAY)) {
            prefs.getFloat(STEPS_TODAY, 0f)
        } else {
            // Migration
            val oldBaseline = prefs.getFloat(BASELINE, 0f)
            (lastRaw - oldBaseline).coerceAtLeast(0f)
        }

        if (storedDate != today && storedDate != null) {
            // New day
            if (rawValue >= lastRaw) {
                // Approximate missed steps to today
                stepsToday = rawValue - lastRaw
            } else {
                // Reboot overnight
                stepsToday = rawValue
            }
        } else if (storedDate == null) {
            // First run ever
            stepsToday = 0f
        } else {
            // Same day
            if (rawValue < lastRaw) {
                // Reboot during the day
                stepsToday += rawValue
            } else {
                // Normal accumulation
                stepsToday += (rawValue - lastRaw)
            }
        }

        prefs.edit()
            .putString(DATE, today)
            .putFloat(RAW, rawValue)
            .putFloat(STEPS_TODAY, stepsToday)
            .apply()
    }

    private fun current(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(RAW)) return null
        
        val today = LocalDate.now().toString()
        val storedDate = prefs.getString(DATE, null)
        
        if (storedDate != null && storedDate != today) {
            // New day, sensor hasn't fired yet
            return 0
        }
        
        if (prefs.contains(STEPS_TODAY)) {
            return prefs.getFloat(STEPS_TODAY, 0f).toInt()
        }
        
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
