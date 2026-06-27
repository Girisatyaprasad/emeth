package com.emeth.kernel.skills.android

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import com.emeth.kernel.health.StepCounterRepository

class StepCountSkill(private val context: Context) : Skill {
    override val id = "android.health.steps"
    override val name = "Step Count"
    override val description = "Checks the number of steps taken"

    override fun canHandle(intent: Intent) = intent == Intent.CHECK_STEPS

    override fun execute(request: SkillRequest): SkillResult {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            return SkillResult.Failure("Step tracking is not available yet. Grant Activity Recognition or connect Health Connect.")
        }

        val steps = StepCounterRepository.read(context)
            ?: return SkillResult.Failure(
                "This phone did not provide a step-counter reading. Confirm Activity Recognition access and sensor support."
            )
        return SkillResult.Success("You have taken $steps steps today.", steps)
    }
}
