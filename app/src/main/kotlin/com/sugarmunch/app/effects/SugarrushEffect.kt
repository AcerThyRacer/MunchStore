package com.sugarmunch.app.effects

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.sugarmunch.app.R

object SugarrushEffect : Effect {
    override val id = "sugarrush"
    override val name = "Sugarrush"
    override val hasVisual = true
    override val hasSound = true
    override val hasHaptic = true

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    override fun enable(context: Context, windowManager: WindowManager) {
        this.windowManager = windowManager
        val view = View(context).apply {
            setBackgroundResource(R.drawable.sugarrush_overlay)
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }
        windowManager.addView(view, params)
        overlayView = view
        playHaptic(context)
    }

    override fun disable() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        windowManager = null
    }

    override fun isActive(): Boolean = overlayView != null

    private fun playHaptic(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        } else {
            null
        }?.defaultVibrator ?: context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 30, 50), -1))
            } else {
                @Suppress("DEPRECATION") it.vibrate(50)
            }
        }
    }
}
