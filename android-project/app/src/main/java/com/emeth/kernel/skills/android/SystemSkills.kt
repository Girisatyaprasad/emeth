package com.emeth.kernel.skills.android

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.content.Intent as AndroidIntent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult

class FlashlightSkill(private val context: Context) : Skill {
    override val id = "android.system.flashlight"
    override val name = "Toggle Flashlight"
    override val description = "Toggles flashlight on/off"

    private var isOn = false

    override fun canHandle(intent: Intent) = intent == Intent.TOGGLE_FLASHLIGHT

    override fun execute(request: SkillRequest): SkillResult {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            isOn = !isOn
            cameraManager.setTorchMode(cameraId, isOn)
            return SkillResult.Success("Flashlight is now ${if (isOn) "ON" else "OFF"}")
        } catch (e: Exception) {
            return SkillResult.Failure("Failed to toggle flashlight", e)
        }
    }
}

class VolumeSkill(private val context: Context) : Skill {
    override val id = "android.system.volume"
    override val name = "Set Volume"
    override val description = "Sets device volume"

    override fun canHandle(intent: Intent) = intent == Intent.SET_VOLUME

    override fun execute(request: SkillRequest): SkillResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Without args, we'll just show the volume UI by adjusting by 0
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
        return SkillResult.Success("Showing volume controls")
    }
}

class MuteSkill(private val context: Context) : Skill {
    override val id = "android.system.mute"
    override val name = "Mute Volume"
    override val description = "Mutes device volume"

    override fun canHandle(intent: Intent) = intent == Intent.MUTE_VOLUME

    override fun execute(request: SkillRequest): SkillResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
        return SkillResult.Success("Muted volume")
    }
}

class BrightnessSkill(private val context: Context) : Skill {
    override val id = "android.system.brightness"
    override val name = "Set Brightness"
    override val description = "Sets screen brightness"

    override fun canHandle(intent: Intent) = intent == Intent.SET_BRIGHTNESS

    override fun execute(request: SkillRequest): SkillResult {
        val i = AndroidIntent(Settings.ACTION_DISPLAY_SETTINGS).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success("Opened Display Settings to adjust brightness")
    }
}

class ReadClipboardSkill(private val context: Context) : Skill {
    override val id = "android.system.clipboard.read"
    override val name = "Read Clipboard"
    override val description = "Reads text from clipboard"

    override fun canHandle(intent: Intent) = intent == Intent.READ_CLIPBOARD

    override fun execute(request: SkillRequest): SkillResult {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip() && clipboard.primaryClip?.itemCount ?: 0 > 0) {
            val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            return SkillResult.Success("Clipboard: $text")
        }
        return SkillResult.Success("Clipboard is empty")
    }
}

class StorageSkill(private val context: Context) : Skill {
    override val id = "android.system.storage"
    override val name = "Check Storage"
    override val description = "Checks available storage"

    override fun canHandle(intent: Intent) = intent == Intent.CHECK_STORAGE

    override fun execute(request: SkillRequest): SkillResult {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalSpace = formatSize(totalBlocks * blockSize)
        val availableSpace = formatSize(availableBlocks * blockSize)
        
        return SkillResult.Success("Storage: $availableSpace available out of $totalSpace")
    }
    
    private fun formatSize(size: Long): String {
        var s = size.toDouble()
        val suffix = arrayOf("B", "KB", "MB", "GB", "TB")
        var index = 0
        while (s >= 1024 && index < suffix.size - 1) {
            s /= 1024
            index++
        }
        return String.format("%.2f %s", s, suffix[index])
    }
}

class RamSkill(private val context: Context) : Skill {
    override val id = "android.system.ram"
    override val name = "Check RAM"
    override val description = "Checks available RAM"

    override fun canHandle(intent: Intent) = intent == Intent.CHECK_RAM

    override fun execute(request: SkillRequest): SkillResult {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val available = formatSize(memoryInfo.availMem)
        val total = formatSize(memoryInfo.totalMem)
        
        return SkillResult.Success("RAM: $available available out of $total")
    }

    private fun formatSize(size: Long): String {
        var s = size.toDouble()
        val suffix = arrayOf("B", "KB", "MB", "GB", "TB")
        var index = 0
        while (s >= 1024 && index < suffix.size - 1) {
            s /= 1024
            index++
        }
        return String.format("%.2f %s", s, suffix[index])
    }
}
