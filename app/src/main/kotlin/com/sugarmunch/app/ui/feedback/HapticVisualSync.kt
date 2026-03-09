package com.sugarmunch.app.ui.feedback

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import androidx.compose.runtime.*
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * Haptic and Visual Sync for SugarMunch
 * Synchronize haptic feedback with visual effects
 */

/**
 * Haptic feedback type
 */
enum class HapticType {
    CLICK,
    HEAVY,
    SUCCESS,
    ERROR,
    WARNING,
    CELEBRATION,
    LIGHT,
    MEDIUM
}

/**
 * Haptic manager
 */
class HapticManager(private val context: Context) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    
    fun vibrate(type: HapticType) {
        when (type) {
            HapticType.CLICK -> vibrateClick()
            HapticType.HEAVY -> vibrateHeavy()
            HapticType.SUCCESS -> vibrateSuccess()
            HapticType.ERROR -> vibrateError()
            HapticType.WARNING -> vibrateWarning()
            HapticType.CELEBRATION -> vibrateCelebration()
            HapticType.LIGHT -> vibrateLight()
            HapticType.MEDIUM -> vibrateMedium()
        }
    }
    
    private fun vibrateClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
    
    private fun vibrateHeavy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
    
    private fun vibrateSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), 0))
        }
    }
    
    private fun vibrateError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50, 50, 50), 0))
        }
    }
    
    private fun vibrateWarning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
    
    private fun vibrateCelebration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 30, 50, 30, 50, 100), 0))
        }
    }
    
    private fun vibrateLight() {
        vibrateClick()
    }
    
    private fun vibrateMedium() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}

/**
 * Sync haptic with visual celebration
 */
@Composable
fun HapticVisualCelebration(
    triggered: Boolean,
    hapticType: HapticType = HapticType.CELEBRATION,
    celebrationType: CelebrationType = CelebrationType.CONFETTI_CANNON
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hapticManager = remember { HapticManager(context) }
    
    LaunchedEffect(triggered) {
        if (triggered) {
            hapticManager.vibrate(hapticType)
        }
    }
    
    CelebrationManager(
        triggered = triggered,
        config = CelebrationConfig(type = celebrationType)
    )
}

/**
 * Sync haptic with error
 */
@Composable
fun HapticErrorFeedback(
    showError: Boolean,
    hapticType: HapticType = HapticType.ERROR,
    errorType: ErrorEffectType = ErrorEffectType.SHAKE
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hapticManager = remember { HapticManager(context) }
    
    LaunchedEffect(showError) {
        if (showError) {
            hapticManager.vibrate(hapticType)
        }
    }
}
