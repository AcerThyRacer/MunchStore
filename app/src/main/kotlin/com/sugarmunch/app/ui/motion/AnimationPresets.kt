package com.sugarmunch.app.ui.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.sugarmunch.app.ui.loading.SugarTransition
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════════════════════
// Motion Presets — user-friendly animation presets for the Control Center
// ═══════════════════════════════════════════════════════════════════════════

enum class MotionPreset(
    val displayName: String,
    val description: String,
    val emoji: String
) {
    SNAPPY("Snappy", "Quick, responsive animations", "⚡"),
    BOUNCY("Bouncy", "Playful spring physics everywhere", "🏀"),
    SMOOTH("Smooth", "Elegant, flowing transitions", "🌊"),
    DRAMATIC("Dramatic", "Bold, cinematic movements", "🎬"),
    SUBTLE("Subtle", "Minimal, refined motion", "✨"),
    NONE("No Animation", "Static UI, maximum performance", "🚫")
}

// ═══════════════════════════════════════════════════════════════════════════
// MotionConfig — runtime configuration produced by a preset or custom tuning
// ═══════════════════════════════════════════════════════════════════════════

data class MotionConfig(
    val preset: MotionPreset = MotionPreset.SMOOTH,
    val speedMultiplier: Float = 1f,
    val particleDensity: Float = 1f,
    val screenTransition: SugarTransition = SugarTransition.SCALE_FADE,
    val reduceMotion: Boolean = false,
    val respectSystemReduceMotion: Boolean = true,

    // Detailed timing knobs (populated by preset → toDetailedConfig)
    val baseDurationMs: Int = 350,
    val easingName: String = "EaseInOut",
    val springDampingRatio: Float = 1f,
    val springStiffness: Float = 300f,
    val overshoot: Float = 0f,
    val buttonPressScale: Float = 0.95f
)

// ═══════════════════════════════════════════════════════════════════════════
// Preset → detailed config mapping
// ═══════════════════════════════════════════════════════════════════════════

fun MotionPreset.toDetailedConfig(): MotionConfig = when (this) {
    MotionPreset.SNAPPY -> MotionConfig(
        preset = this,
        speedMultiplier = 1.5f,
        baseDurationMs = 200,
        easingName = "Linear",
        overshoot = 0f,
        particleDensity = 0.2f,
        screenTransition = SugarTransition.FADE,
        springDampingRatio = 1f,
        springStiffness = 600f,
        buttonPressScale = 0.93f
    )

    MotionPreset.BOUNCY -> MotionConfig(
        preset = this,
        speedMultiplier = 1.0f,
        baseDurationMs = 350,
        easingName = "Spring",
        springDampingRatio = 0.5f,
        springStiffness = 300f,
        overshoot = 0.6f,
        particleDensity = 1.0f,
        screenTransition = SugarTransition.CANDY_POP,
        buttonPressScale = 0.90f
    )

    MotionPreset.SMOOTH -> MotionConfig(
        preset = this,
        speedMultiplier = 1.0f,
        baseDurationMs = 350,
        easingName = "EaseInOut",
        overshoot = 0f,
        particleDensity = 0.5f,
        screenTransition = SugarTransition.FADE,
        springDampingRatio = 0.8f,
        springStiffness = 300f,
        buttonPressScale = 0.95f
    )

    MotionPreset.DRAMATIC -> MotionConfig(
        preset = this,
        speedMultiplier = 0.8f,
        baseDurationMs = 500,
        easingName = "EaseInOut",
        overshoot = 0.15f,
        particleDensity = 1.5f,
        screenTransition = SugarTransition.SCALE_FADE,
        springDampingRatio = 0.65f,
        springStiffness = 250f,
        buttonPressScale = 0.92f
    )

    MotionPreset.SUBTLE -> MotionConfig(
        preset = this,
        speedMultiplier = 1.2f,
        baseDurationMs = 250,
        easingName = "EaseOut",
        overshoot = 0f,
        particleDensity = 0f,
        screenTransition = SugarTransition.FADE,
        springDampingRatio = 1f,
        springStiffness = 400f,
        buttonPressScale = 0.97f
    )

    MotionPreset.NONE -> MotionConfig(
        preset = this,
        speedMultiplier = 0f,
        baseDurationMs = 0,
        easingName = "Linear",
        overshoot = 0f,
        particleDensity = 0f,
        screenTransition = SugarTransition.FADE,
        reduceMotion = true,
        springDampingRatio = 1f,
        springStiffness = 1000f,
        buttonPressScale = 1f
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// CompositionLocal — provides MotionConfig down the tree
// ═══════════════════════════════════════════════════════════════════════════

val LocalMotionConfig = staticCompositionLocalOf { MotionConfig() }

@Composable
fun ProvideMotionConfig(config: MotionConfig, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMotionConfig provides config, content = content)
}

// ═══════════════════════════════════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════════════════════════════════

/** Returns [baseDurationMs] adjusted by the current [LocalMotionConfig] speed multiplier. */
@Composable
fun motionDuration(baseDurationMs: Int): Int {
    val config = LocalMotionConfig.current
    if (config.reduceMotion || config.speedMultiplier <= 0f) return 0
    return (baseDurationMs / config.speedMultiplier).roundToInt().coerceAtLeast(1)
}

/** All presets in display order. */
val AllMotionPresets: List<MotionPreset> = MotionPreset.entries.toList()

/** Clamp helpers for slider ranges. */
fun Float.clampSpeed(): Float = coerceIn(0.5f, 3.0f)
fun Float.clampDensity(): Float = coerceIn(0f, 2.0f)
