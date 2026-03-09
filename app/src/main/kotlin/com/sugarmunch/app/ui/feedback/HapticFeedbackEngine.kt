package com.sugarmunch.app.ui.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class HapticPattern {
    LIGHT_TAP,
    MEDIUM_TAP,
    HEAVY_TAP,
    SUCCESS,
    ERROR,
    WARNING,
    CELEBRATION,
    HEARTBEAT,
    COUNTDOWN,
    REWARD_CLAIM,
    TOGGLE_ON,
    TOGGLE_OFF
}

class HapticEngine(private val context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun play(pattern: HapticPattern) {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (timings, amplitudes) = pattern.waveformSpec()
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern.legacyTimings(), -1)
        }
    }

    fun playWithDelay(pattern: HapticPattern, delayMs: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(delayMs)
            play(pattern)
        }
    }

    fun cancel() {
        vibrator.cancel()
    }

    companion object {
        @Volatile
        private var instance: HapticEngine? = null

        fun getInstance(context: Context): HapticEngine {
            return instance ?: synchronized(this) {
                instance ?: HapticEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

// ── Pattern definitions ─────────────────────────────────────────────────────────

private fun HapticPattern.waveformSpec(): Pair<LongArray, IntArray> = when (this) {
    HapticPattern.LIGHT_TAP -> Pair(
        longArrayOf(0, 20),
        intArrayOf(0, 60)
    )
    HapticPattern.MEDIUM_TAP -> Pair(
        longArrayOf(0, 30),
        intArrayOf(0, 128)
    )
    HapticPattern.HEAVY_TAP -> Pair(
        longArrayOf(0, 40),
        intArrayOf(0, 255)
    )
    HapticPattern.SUCCESS -> Pair(
        longArrayOf(0, 25, 80, 25),
        intArrayOf(0, 120, 0, 180)
    )
    HapticPattern.ERROR -> Pair(
        longArrayOf(0, 20, 50, 20, 50, 20),
        intArrayOf(0, 200, 0, 200, 0, 200)
    )
    HapticPattern.WARNING -> Pair(
        longArrayOf(0, 100),
        intArrayOf(0, 180)
    )
    HapticPattern.CELEBRATION -> Pair(
        longArrayOf(0, 20, 60, 30, 60, 50),
        intArrayOf(0, 60, 0, 128, 0, 255)
    )
    HapticPattern.HEARTBEAT -> Pair(
        longArrayOf(0, 40, 100, 40),
        intArrayOf(0, 200, 0, 160)
    )
    HapticPattern.COUNTDOWN -> Pair(
        longArrayOf(0, 30, 400, 30, 400, 30),
        intArrayOf(0, 150, 0, 150, 0, 150)
    )
    HapticPattern.REWARD_CLAIM -> Pair(
        longArrayOf(0, 15, 30, 20, 30, 25, 30, 35, 30, 50),
        intArrayOf(0, 60, 0, 100, 0, 140, 0, 190, 0, 255)
    )
    HapticPattern.TOGGLE_ON -> Pair(
        longArrayOf(0, 15, 20, 25),
        intArrayOf(0, 80, 0, 180)
    )
    HapticPattern.TOGGLE_OFF -> Pair(
        longArrayOf(0, 25, 20, 15),
        intArrayOf(0, 180, 0, 80)
    )
}

@Suppress("DEPRECATION")
private fun HapticPattern.legacyTimings(): LongArray = when (this) {
    HapticPattern.LIGHT_TAP -> longArrayOf(0, 20)
    HapticPattern.MEDIUM_TAP -> longArrayOf(0, 30)
    HapticPattern.HEAVY_TAP -> longArrayOf(0, 50)
    HapticPattern.SUCCESS -> longArrayOf(0, 25, 80, 25)
    HapticPattern.ERROR -> longArrayOf(0, 20, 50, 20, 50, 20)
    HapticPattern.WARNING -> longArrayOf(0, 100)
    HapticPattern.CELEBRATION -> longArrayOf(0, 20, 60, 30, 60, 50)
    HapticPattern.HEARTBEAT -> longArrayOf(0, 40, 100, 40)
    HapticPattern.COUNTDOWN -> longArrayOf(0, 30, 400, 30, 400, 30)
    HapticPattern.REWARD_CLAIM -> longArrayOf(0, 15, 30, 20, 30, 25, 30, 35, 30, 50)
    HapticPattern.TOGGLE_ON -> longArrayOf(0, 15, 20, 25)
    HapticPattern.TOGGLE_OFF -> longArrayOf(0, 25, 20, 15)
}

// ── Compose integration ─────────────────────────────────────────────────────────

@Composable
fun rememberHapticEngine(): HapticEngine {
    val context = LocalContext.current
    return remember { HapticEngine.getInstance(context) }
}

fun Modifier.hapticFeedback(pattern: HapticPattern): Modifier = composed {
    val engine = rememberHapticEngine()
    androidx.compose.foundation.clickable {
        engine.play(pattern)
    }.then(this)
}

@Composable
fun HapticButton(
    onClick: () -> Unit,
    pattern: HapticPattern = HapticPattern.MEDIUM_TAP,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val engine = rememberHapticEngine()
    Button(
        onClick = {
            engine.play(pattern)
            onClick()
        },
        modifier = modifier,
        content = content
    )
}
