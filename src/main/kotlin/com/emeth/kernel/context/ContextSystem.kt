package com.emeth.kernel.context

data class ContextSnapshot(
    val currentTime: Long,
    val batteryLevel: Int,
    val networkState: String,
    val foregroundApp: String,
    val screenState: String,
    val locationAvailable: Boolean,
    val stepCountAvailable: Boolean
)

interface ContextProvider {
    fun getContextSnapshot(): ContextSnapshot
}
