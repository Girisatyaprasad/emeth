package com.emeth.kernel.skills.android

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class StepCountSkill(private val context: Context) : Skill {
    override val id = "android.health.steps"
    override val name = "Step Count"
    override val description = "Checks the number of steps taken"

    override fun canHandle(intent: Intent) = intent == Intent.CHECK_STEPS

    override fun execute(request: SkillRequest): SkillResult {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            return SkillResult.Failure("Step tracking is not available yet. Grant Activity Recognition or connect Health Connect.")
        }

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        if (stepSensor == null) {
            return SkillResult.Failure("Step tracking is not available yet. Grant Activity Recognition or connect Health Connect.")
        }

        var steps = -1f
        val latch = CountDownLatch(1)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                steps = event.values[0]
                latch.countDown()
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
        
        // Wait up to 1 second for a sensor reading
        val gotReading = latch.await(1, TimeUnit.SECONDS)
        sensorManager.unregisterListener(listener)

        return if (gotReading && steps >= 0) {
            SkillResult.Success("You have taken ${steps.toInt()} steps.")
        } else {
            // The TYPE_STEP_COUNTER only pushes an event when a step is taken, 
            // so if we didn't get one immediately we don't know the exact count right now without caching.
            // But since this is a simple implementation:
            SkillResult.Failure("Could not read step count. Try walking a few steps and asking again.")
        }
    }
}
