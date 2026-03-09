package com.sugarmunch.app.ui.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.model.ThemeCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Haptic intensity levels
 */
enum class HapticIntensity {
    CHILL,      // 0.0 - 0.3
    NORMAL,     // 0.3 - 0.7
    SWEET,      // 0.7 - 1.0
    SUGARRUSH,  // 1.0 - 1.5
    MAXIMUM     // 1.5 - 2.0
}

/**
 * Haptic segment for custom patterns
 */
data class HapticSegment(
    val duration: Long = 50,
    val amplitude: Int = 128,
    val delay: Long = 0
)

/**
 * Haptic preset patterns
 */
enum class HapticPreset {
    CLICK,
    TAP,
    SUCCESS,
    ERROR,
    WARNING,
    HEARTBEAT,
    CELEBRATION,
    LEVEL_UP,
    ACHIEVEMENT,
    NOTIFICATION,
    SCROLL_TICK,
    TOGGLE_ON,
    TOGGLE_OFF,
    LONG_PRESS,
    DOUBLE_TAP,
    TRIPLE_TAP
}

/**
 * Theme-aware haptic feedback engine
 * Adjusts haptic patterns based on current theme and intensity settings
 */
class ThemeAwareHapticEngine(private val context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Perform click haptic with theme-aware intensity
     */
    fun performClick(
        themeIntensity: Float,
        theme: CandyTheme,
        intensity: HapticIntensity = getIntensityFromFloat(themeIntensity)
    ) {
        if (!vibrator.hasVibrator()) return

        val (duration, amplitude) = getClickPattern(intensity, theme)
        vibrate(duration, amplitude)
    }

    /**
     * Perform success haptic with theme-aware pattern
     */
    fun performSuccess(
        themeIntensity: Float,
        theme: CandyTheme
    ) {
        if (!vibrator.hasVibrator()) return

        val pattern = getSuccessPattern(theme)
        val intensity = getIntensityFromFloat(themeIntensity)
        vibratePattern(pattern.waveformSpec(intensity))
    }

    /**
     * Perform error haptic
     */
    fun performError(
        themeIntensity: Float,
        theme: CandyTheme
    ) {
        if (!vibrator.hasVibrator()) return

        val pattern = getErrorPattern(theme)
        val intensity = getIntensityFromFloat(themeIntensity)
        vibratePattern(pattern.waveformSpec(intensity))
    }

    /**
     * Perform custom haptic pattern
     */
    fun performPattern(
        preset: HapticPreset,
        themeIntensity: Float,
        theme: CandyTheme
    ) {
        if (!vibrator.hasVibrator()) return

        val intensity = getIntensityFromFloat(themeIntensity)
        val pattern = getPresetPattern(preset, theme)
        vibratePattern(pattern.waveformSpec(intensity))
    }

    /**
     * Perform custom segment pattern
     */
    fun performCustomPattern(
        segments: List<HapticSegment>,
        themeIntensity: Float
    ) {
        if (!vibrator.hasVibrator() || segments.isEmpty()) return

        val intensityMultiplier = themeIntensity.coerceIn(0f, 2f)
        
        val timings = mutableListOf<Long>()
        val amplitudes = mutableListOf<Int>()

        segments.forEachIndexed { index, segment ->
            if (index > 0) {
                timings.add(segment.delay)
            } else {
                timings.add(0)
            }
            timings.add(segment.duration)

            val adjustedAmplitude = (segment.amplitude * intensityMultiplier).toInt().coerceIn(0, 255)
            amplitudes.add(0)
            amplitudes.add(adjustedAmplitude)
        }

        vibratePattern(timings.toLongArray(), amplitudes.toIntArray())
    }

    /**
     * Test a haptic pattern
     */
    fun testPattern(preset: HapticPreset) {
        performPattern(preset, 1.0f, CandyTheme.DEFAULT)
    }

    /**
     * Cancel ongoing vibration
     */
    fun cancel() {
        vibrator.cancel()
    }

    // ========== PRIVATE METHODS ==========

    private fun vibrate(duration: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(duration, amplitude)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, -1)
        }
    }

    private fun getIntensityFromFloat(intensity: Float): HapticIntensity {
        return when {
            intensity <= 0.3f -> HapticIntensity.CHILL
            intensity <= 0.7f -> HapticIntensity.NORMAL
            intensity <= 1.0f -> HapticIntensity.SWEET
            intensity <= 1.5f -> HapticIntensity.SUGARRUSH
            else -> HapticIntensity.MAXIMUM
        }
    }

    private fun getClickPattern(intensity: HapticIntensity, theme: CandyTheme): Pair<Long, Int> {
        val (baseDuration, baseAmplitude) = when (intensity) {
            HapticIntensity.CHILL -> Pair(15L, 40)
            HapticIntensity.NORMAL -> Pair(20L, 80)
            HapticIntensity.SWEET -> Pair(25L, 120)
            HapticIntensity.SUGARRUSH -> Pair(30L, 180)
            HapticIntensity.MAXIMUM -> Pair(40L, 255)
        }

        // Adjust based on theme category
        val adjustedDuration = when (theme.category) {
            ThemeCategory.SUGARRUSH -> (baseDuration * 1.2).toLong()
            ThemeCategory.CHILL -> (baseDuration * 0.8).toLong()
            ThemeCategory.TRIPPY -> (baseDuration * 1.1).toLong()
            else -> baseDuration
        }

        return Pair(adjustedDuration, baseAmplitude)
    }

    private fun getSuccessPattern(theme: CandyTheme): HapticPattern {
        return when (theme.category) {
            ThemeCategory.SUGARRUSH -> HapticPattern.CELEBRATION
            ThemeCategory.CHILL -> HapticPattern.SUCCESS_LIGHT
            ThemeCategory.TRIPPY -> HapticPattern.HEARTBEAT
            else -> HapticPattern.SUCCESS
        }
    }

    private fun getErrorPattern(theme: CandyTheme): HapticPattern {
        return when (theme.category) {
            ThemeCategory.SUGARRUSH -> HapticPattern.ERROR_HEAVY
            ThemeCategory.CHILL -> HapticPattern.ERROR_LIGHT
            else -> HapticPattern.ERROR
        }
    }

    private fun getPresetPattern(preset: HapticPreset, theme: CandyTheme): HapticPattern {
        return when (preset) {
            HapticPreset.CLICK -> HapticPattern.CLICK
            HapticPreset.TAP -> HapticPattern.TAP
            HapticPreset.SUCCESS -> getSuccessPattern(theme)
            HapticPreset.ERROR -> getErrorPattern(theme)
            HapticPreset.WARNING -> HapticPattern.WARNING
            HapticPreset.HEARTBEAT -> HapticPattern.HEARTBEAT
            HapticPreset.CELEBRATION -> HapticPattern.CELEBRATION
            HapticPreset.LEVEL_UP -> HapticPattern.LEVEL_UP
            HapticPreset.ACHIEVEMENT -> HapticPattern.ACHIEVEMENT
            HapticPreset.NOTIFICATION -> HapticPattern.NOTIFICATION
            HapticPreset.SCROLL_TICK -> HapticPattern.SCROLL_TICK
            HapticPreset.TOGGLE_ON -> HapticPattern.TOGGLE_ON
            HapticPreset.TOGGLE_OFF -> HapticPattern.TOGGLE_OFF
            HapticPreset.LONG_PRESS -> HapticPattern.LONG_PRESS
            HapticPreset.DOUBLE_TAP -> HapticPattern.DOUBLE_TAP
            HapticPreset.TRIPLE_TAP -> HapticPattern.TRIPLE_TAP
        }
    }

    companion object {
        @Volatile
        private var instance: ThemeAwareHapticEngine? = null

        fun getInstance(context: Context): ThemeAwareHapticEngine {
            return instance ?: synchronized(this) {
                instance ?: ThemeAwareHapticEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Haptic pattern definitions with intensity support
 */
sealed class HapticPattern {
    abstract fun waveformSpec(intensity: HapticIntensity): Pair<LongArray, IntArray>

    object CLICK : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = when (intensity) {
            HapticIntensity.CHILL -> Pair(longArrayOf(0, 10), intArrayOf(0, 40))
            HapticIntensity.NORMAL -> Pair(longArrayOf(0, 15), intArrayOf(0, 80))
            HapticIntensity.SWEET -> Pair(longArrayOf(0, 20), intArrayOf(0, 120))
            HapticIntensity.SUGARRUSH -> Pair(longArrayOf(0, 25), intArrayOf(0, 180))
            HapticIntensity.MAXIMUM -> Pair(longArrayOf(0, 30), intArrayOf(0, 255))
        }
    }

    object TAP : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = when (intensity) {
            HapticIntensity.CHILL -> Pair(longArrayOf(0, 15), intArrayOf(0, 50))
            HapticIntensity.NORMAL -> Pair(longArrayOf(0, 20), intArrayOf(0, 100))
            HapticIntensity.SWEET -> Pair(longArrayOf(0, 25), intArrayOf(0, 150))
            HapticIntensity.SUGARRUSH -> Pair(longArrayOf(0, 30), intArrayOf(0, 200))
            HapticIntensity.MAXIMUM -> Pair(longArrayOf(0, 40), intArrayOf(0, 255))
        }
    }

    object SUCCESS : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = when (intensity) {
            HapticIntensity.CHILL -> Pair(longArrayOf(0, 20, 60, 20), intArrayOf(0, 80, 0, 120))
            HapticIntensity.NORMAL -> Pair(longArrayOf(0, 25, 80, 25), intArrayOf(0, 120, 0, 180))
            HapticIntensity.SWEET -> Pair(longArrayOf(0, 30, 100, 30), intArrayOf(0, 150, 0, 220))
            HapticIntensity.SUGARRUSH -> Pair(longArrayOf(0, 35, 120, 35), intArrayOf(0, 180, 0, 255))
            HapticIntensity.MAXIMUM -> Pair(longArrayOf(0, 40, 150, 40), intArrayOf(0, 220, 0, 255))
        }
    }

    object SUCCESS_LIGHT : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = Pair(longArrayOf(0, 15, 40, 15), intArrayOf(0, 60, 0, 100))
    }

    object ERROR : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = when (intensity) {
            HapticIntensity.CHILL -> Pair(longArrayOf(0, 15, 40, 15, 40, 15), intArrayOf(0, 120, 0, 120, 0, 120))
            HapticIntensity.NORMAL -> Pair(longArrayOf(0, 20, 50, 20, 50, 20), intArrayOf(0, 180, 0, 180, 0, 180))
            HapticIntensity.SWEET -> Pair(longArrayOf(0, 25, 60, 25, 60, 25), intArrayOf(0, 220, 0, 220, 0, 220))
            HapticIntensity.SUGARRUSH -> Pair(longArrayOf(0, 30, 70, 30, 70, 30), intArrayOf(0, 255, 0, 255, 0, 255))
            HapticIntensity.MAXIMUM -> Pair(longArrayOf(0, 35, 80, 35, 80, 35), intArrayOf(0, 255, 0, 255, 0, 255))
        }
    }

    object ERROR_LIGHT : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = Pair(longArrayOf(0, 10, 30, 10, 30, 10), intArrayOf(0, 80, 0, 80, 0, 80))
    }

    object ERROR_HEAVY : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = Pair(longArrayOf(0, 40, 100, 40, 100, 40, 40), intArrayOf(0, 255, 0, 255, 0, 255, 0, 255))
    }

    object WARNING : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = when (intensity) {
            HapticIntensity.CHILL -> Pair(longArrayOf(0, 80), intArrayOf(0, 100))
            HapticIntensity.NORMAL -> Pair(longArrayOf(0, 100), intArrayOf(0, 150))
            HapticIntensity.SWEET -> Pair(longArrayOf(0, 120), intArrayOf(0, 200))
            HapticIntensity.SUGARRUSH -> Pair(longArrayOf(0, 150), intArrayOf(0, 255))
            HapticIntensity.MAXIMUM -> Pair(longArrayOf(0, 200), intArrayOf(0, 255))
        }
    }

    object HEARTBEAT : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = when (intensity) {
            HapticIntensity.CHILL -> Pair(longArrayOf(0, 30, 80, 30), intArrayOf(0, 120, 0, 100))
            HapticIntensity.NORMAL -> Pair(longArrayOf(0, 40, 100, 40), intArrayOf(0, 180, 0, 140))
            HapticIntensity.SWEET -> Pair(longArrayOf(0, 50, 120, 50), intArrayOf(0, 220, 0, 180))
            HapticIntensity.SUGARRUSH -> Pair(longArrayOf(0, 60, 150, 60), intArrayOf(0, 255, 0, 220))
            HapticIntensity.MAXIMUM -> Pair(longArrayOf(0, 70, 180, 70), intArrayOf(0, 255, 0, 255))
        }
    }

    object CELEBRATION : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = when (intensity) {
            HapticIntensity.CHILL -> Pair(longArrayOf(0, 15, 40, 20, 40, 30), intArrayOf(0, 60, 0, 100, 0, 180))
            HapticIntensity.NORMAL -> Pair(longArrayOf(0, 20, 60, 30, 60, 50), intArrayOf(0, 80, 0, 150, 0, 255))
            HapticIntensity.SWEET -> Pair(longArrayOf(0, 25, 80, 40, 80, 60), intArrayOf(0, 120, 0, 200, 0, 255))
            HapticIntensity.SUGARRUSH -> Pair(longArrayOf(0, 30, 100, 50, 100, 80), intArrayOf(0, 180, 0, 255, 0, 255))
            HapticIntensity.MAXIMUM -> Pair(longArrayOf(0, 35, 120, 60, 120, 100), intArrayOf(0, 255, 0, 255, 0, 255))
        }
    }

    object LEVEL_UP : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = Pair(
            longArrayOf(0, 20, 50, 30, 50, 40, 50, 50, 50, 60),
            intArrayOf(0, 80, 0, 120, 0, 160, 0, 200, 0, 255)
        )
    }

    object ACHIEVEMENT : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = Pair(
            longArrayOf(0, 30, 60, 30, 60, 30, 60, 30, 60, 30, 60, 30, 60),
            intArrayOf(0, 150, 0, 180, 0, 210, 0, 240, 0, 255, 0, 255, 0, 255)
        )
    }

    object NOTIFICATION : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = when (intensity) {
            HapticIntensity.CHILL -> Pair(longArrayOf(0, 10, 100, 10), intArrayOf(0, 60, 0, 60))
            HapticIntensity.NORMAL -> Pair(longArrayOf(0, 15, 150, 15), intArrayOf(0, 100, 0, 100))
            HapticIntensity.SWEET -> Pair(longArrayOf(0, 20, 200, 20), intArrayOf(0, 150, 0, 150))
            HapticIntensity.SUGARRUSH -> Pair(longArrayOf(0, 25, 250, 25), intArrayOf(0, 200, 0, 200))
            HapticIntensity.MAXIMUM -> Pair(longArrayOf(0, 30, 300, 30), intArrayOf(0, 255, 0, 255))
        }
    }

    object SCROLL_TICK : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = when (intensity) {
            HapticIntensity.CHILL -> Pair(longArrayOf(0, 5), intArrayOf(0, 20))
            HapticIntensity.NORMAL -> Pair(longArrayOf(0, 8), intArrayOf(0, 40))
            HapticIntensity.SWEET -> Pair(longArrayOf(0, 10), intArrayOf(0, 60))
            HapticIntensity.SUGARRUSH -> Pair(longArrayOf(0, 12), intArrayOf(0, 80))
            HapticIntensity.MAXIMUM -> Pair(longArrayOf(0, 15), intArrayOf(0, 100))
        }
    }

    object TOGGLE_ON : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = Pair(longArrayOf(0, 15, 20, 25), intArrayOf(0, 80, 0, 180))
    }

    object TOGGLE_OFF : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = Pair(longArrayOf(0, 25, 20, 15), intArrayOf(0, 180, 0, 80))
    }

    object LONG_PRESS : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = when (intensity) {
            HapticIntensity.CHILL -> Pair(longArrayOf(0, 30), intArrayOf(0, 80))
            HapticIntensity.NORMAL -> Pair(longArrayOf(0, 40), intArrayOf(0, 120))
            HapticIntensity.SWEET -> Pair(longArrayOf(0, 50), intArrayOf(0, 180))
            HapticIntensity.SUGARRUSH -> Pair(longArrayOf(0, 60), intArrayOf(0, 220))
            HapticIntensity.MAXIMUM -> Pair(longArrayOf(0, 80), intArrayOf(0, 255))
        }
    }

    object DOUBLE_TAP : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = Pair(
            longArrayOf(0, 15, 50, 15),
            intArrayOf(0, 100, 0, 100)
        )
    }

    object TRIPLE_TAP : HapticPattern() {
        override fun waveformSpec(intensity: HapticIntensity) = Pair(
            longArrayOf(0, 15, 40, 15, 40, 15),
            intArrayOf(0, 100, 0, 100, 0, 100)
        )
    }
}

/**
 * Composable to remember the theme-aware haptic engine
 */
@Composable
fun rememberThemeAwareHaptic(
    themeManager: ThemeManager
): ThemeAwareHapticEngine {
    val context = LocalContext.current
    return remember {
        ThemeAwareHapticEngine.getInstance(context)
    }
}

/**
 * Per-app haptic configuration
 */
data class AppHapticConfig(
    val enabled: Boolean = true,
    val clickIntensity: Float = 0.5f,
    val customPattern: HapticPreset? = null,
    val useThemeDefaults: Boolean = true
)
