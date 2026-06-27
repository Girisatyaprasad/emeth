package com.emeth.kernel.watchers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class WatcherWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val engine = TriggerEngine(context)
        engine.evaluateAll()
        return Result.success()
    }
}
