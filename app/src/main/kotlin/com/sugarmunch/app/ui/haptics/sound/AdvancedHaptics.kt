package com.sugarmunch.app.ui.haptics.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.sugarmunch.app.crash.GlobalExceptionHandler
import com.sugarmunch.app.crash.Severity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PHASE 14: ENHANCED UX
 * Advanced Haptics & Sound Effects
 */

/**
 * Advanced Haptic Feedback Manager
 */
@Singleton
class AdvancedHapticManager @Inject constructor(
    private val context: Context
) {
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * Click haptic - Light tap
     */
    fun click() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            )
        } else {
            vibrator.vibrate(50)
        }
    }

    /**
     * Heavy click - Stronger feedback
     */
    fun heavyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            )
        } else {
            vibrator.vibrate(100)
        }
    }

    /**
     * Success haptic - Two-tone pattern
     */
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 30, 50),
                    -1
                )
            )
        } else {
            vibrator.vibrate(longArrayOf(0, 50, 30, 50), -1)
        }
    }

    /**
     * Error haptic - Sharp pattern
     */
    fun error() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100, 50, 100),
                    -1
                )
            )
        } else {
            vibrator.vibrate(longArrayOf(0, 100, 50, 100, 50, 100), -1)
        }
    }

    /**
     * Warning haptic - Gentle pulse
     */
    fun warning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(150)
        }
    }

    /**
     * Notification haptic - Subtle tick
     */
    fun notification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            )
        } else {
            vibrator.vibrate(30)
        }
    }

    /**
     * Double tap haptic
     */
    fun doubleTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 40, 30, 40),
                    -1
                )
            )
        } else {
            vibrator.vibrate(longArrayOf(0, 40, 30, 40), -1)
        }
    }

    /**
     * Long press haptic
     */
    fun longPress() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            )
        } else {
            vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1)
        }
    }

    /**
     * Rumble effect - Gaming style
     */
    fun rumble(duration: Long = 500) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(duration)
        }
    }

    /**
     * Pattern haptic - Custom pattern
     */
    fun pattern(timings: LongArray, amplitudes: IntArray? = null, repeat: Int = -1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                if (amplitudes != null) {
                    VibrationEffect.createWaveform(timings, amplitudes, repeat)
                } else {
                    VibrationEffect.createWaveform(timings, repeat)
                }
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, repeat)
        }
    }

    /**
     * Heartbeat pattern
     */
    fun heartbeat() {
        pattern(
            longArrayOf(0, 150, 100, 150, 500, 150, 100, 150),
            intArrayOf(0, 200, 0, 200, 0, 200, 0, 200)
        )
    }

    /**
     * Celebration pattern
     */
    fun celebration() {
        pattern(
            longArrayOf(0, 100, 50, 100, 50, 100, 50, 100, 50, 200),
            intArrayOf(0, 255, 0, 200, 0, 150, 0, 100, 0, 255)
        )
    }

    /**
     * Cancel all vibrations
     */
    fun cancel() {
        vibrator.cancel()
    }
}

/**
 * Sound Effect Manager
 */
@Singleton
class SoundEffectManager @Inject constructor(
    private val context: Context
) {
    private val soundPool: SoundPool

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    private val soundIds = mutableMapOf<SoundType, Int>()
    private var isLoaded = false

    enum class SoundType(val resource: Int) {
        CLICK(R.raw.sound_click),
        SUCCESS(R.raw.sound_success),
        ERROR(R.raw.sound_error),
        POP(R.raw.sound_pop),
        SWOOSH(R.raw.sound_swoosh),
        CHIME(R.raw.sound_chime),
        LEVEL_UP(R.raw.sound_levelup),
        ACHIEVEMENT(R.raw.sound_achievement)
    }

    /**
     * Load all sounds
     */
    fun loadSounds() {
        SoundType.values().forEach { type ->
            try {
                soundIds[type] = soundPool.load(context, type.resource, 1)
            } catch (e: Exception) {
                GlobalExceptionHandler.getInstance().reportCaughtException(e, Severity.DEBUG)
            }
        }
        isLoaded = true
    }

    /**
     * Play sound effect
     */
    fun play(type: SoundType, volume: Float = 1.0f) {
        if (!isLoaded) return

        soundIds[type]?.let { soundId ->
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    /**
     * Play click sound
     */
    fun playClick() = play(SoundType.CLICK)

    /**
     * Play success sound
     */
    fun playSuccess() = play(SoundType.SUCCESS)

    /**
     * Play error sound
     */
    fun playError() = play(SoundType.ERROR)

    /**
     * Play pop sound
     */
    fun playPop() = play(SoundType.POP)

    /**
     * Play swoosh sound
     */
    fun playSwoosh() = play(SoundType.SWOOSH)

    /**
     * Play chime sound
     */
    fun playChime() = play(SoundType.CHIME)

    /**
     * Play level up sound
     */
    fun playLevelUp() = play(SoundType.LEVEL_UP)

    /**
     * Play achievement sound
     */
    fun playAchievement() = play(SoundType.ACHIEVEMENT)

    /**
     * Release resources
     */
    fun release() {
        soundPool.release()
    }
}

/**
 * Combined Haptic + Sound Feedback
 */
@Composable
fun rememberFeedbackManager(): FeedbackManager {
    val context = LocalContext.current
    return remember {
        FeedbackManager(
            hapticManager = AdvancedHapticManager(context),
            soundManager = SoundEffectManager(context)
        )
    }
}

class FeedbackManager(
    private val hapticManager: AdvancedHapticManager,
    private val soundManager: SoundEffectManager
) {
    fun click() {
        hapticManager.click()
        soundManager.playClick()
    }

    fun success() {
        hapticManager.success()
        soundManager.playSuccess()
    }

    fun error() {
        hapticManager.error()
        soundManager.playError()
    }

    fun pop() {
        hapticManager.notification()
        soundManager.playPop()
    }

    fun swoosh() {
        soundManager.playSwoosh()
    }

    fun celebration() {
        hapticManager.celebration()
        soundManager.playAchievement()
    }

    fun levelUp() {
        hapticManager.celebration()
        soundManager.playLevelUp()
    }
}

// Placeholder sound resources (would need actual sound files)
object R {
    object raw {
        const val sound_click = 0
        const val sound_success = 0
        const val sound_error = 0
        const val sound_pop = 0
        const val sound_swoosh = 0
        const val sound_chime = 0
        const val sound_levelup = 0
        const val sound_achievement = 0
    }
}
