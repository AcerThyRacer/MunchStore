package com.sugarmunch.app.effects

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.core.content.getSystemService

object HeartbeatHapticEffect : Effect {
    override val id = "heartbeat_haptic"
    override val name = "Heartbeat"
    override val hasVisual = false
    override val hasSound = false
    override val hasHaptic = true

    private var active = false

    override fun enable(context: Context, windowManager: WindowManager) {
        active = true
        playHeartbeat(context)
    }

    override fun disable() {
        active = false
    }

    override fun isActive(): Boolean = active

    private fun playHeartbeat(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        } else {
            null
        }?.defaultVibrator ?: context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 100, 80), -1))
            } else {
                @Suppress("DEPRECATION") it.vibrate(80)
            }
        }
    }
}
