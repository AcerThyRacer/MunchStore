package com.sugarmunch.app.ui.customization.utils

import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import com.sugarmunch.app.ui.customization.AnimationCurve
import com.sugarmunch.app.ui.customization.CustomBezier
import com.sugarmunch.app.ui.customization.StaggerPattern
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * EXTREME Animation Utilities for SugarMunch
 * Advanced animation curves, transitions, and choreography
 */
object AnimationUtils {

    // ═══════════════════════════════════════════════════════════════
    // EASING CURVES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get easing function based on AnimationCurve
     */
    fun getEasing(curve: AnimationCurve): Easing {
        return when (curve) {
            AnimationCurve.LINEAR -> Easing.Linear
            AnimationCurve.EASE_IN -> Easing.In
            AnimationCurve.EASE_OUT -> Easing.Out
            AnimationCurve.EASE_IN_OUT -> Easing.InOut
            AnimationCurve.BOUNCE -> Easing.BounceOut
            AnimationCurve.ELASTIC -> createElasticEasing()
            AnimationCurve.SPRING -> createSpringEasing()
            AnimationCurve.CUSTOM -> Easing.Linear // Custom handled separately
        }
    }

    /**
     * Create custom bezier easing
     */
    fun createBezierEasing(bezier: CustomBezier): Easing {
        return Easing { x ->
            bezierInterpolation(x, bezier.x1, bezier.y1, bezier.x2, bezier.y2)
        }
    }

    private fun bezierInterpolation(
        t: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): Float {
        // Cubic bezier interpolation
        val cx = 3 * x1
        val bx = 3 * (x2 - x1) - cx
        val ax = 1 - cx - bx

        val cy = 3 * y1
        val by = 3 * (y2 - y1) - cy
        val ay = 1 - cy - by

        val t2 = t * t
        val t3 = t2 * t

        return ((ay * t3) + (by * t2) + (cy * t))
    }

    /**
     * Create elastic easing function
     */
    fun createElasticEasing(period: Float = 0.4f): Easing {
        return Easing { x ->
            if (x == 0f || x == 1f) return@Easing x
            val s = period / 4
            -sin(2 * PI.toFloat() * x / period) * 2f.pow(-10 * x) + 1
        }
    }

    /**
     * Create spring-like easing function
     */
    fun createSpringEasing(): Easing {
        return Easing { x ->
            // Approximate spring behavior with damped oscillation
            val damping = 0.5f
            val frequency = 3f
            val decay = (-damping * x * frequency).toFloat().coerceIn(0f, 1f)
            (1 - decay * cos(2 * PI.toFloat() * frequency * x)).toFloat()
                .coerceIn(0f, 1f)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // BOUNCE EASINGS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Bounce in easing
     */
    val bounceIn: Easing = Easing { x ->
        1 - bounceOut(1 - x)
    }

    /**
     * Bounce out easing
     */
    val bounceOut: Easing = Easing { x ->
        val n1 = 7.5625
        val d1 = 2.75

        when {
            x < 1 / d1 -> n1 * x * x
            x < 2 / d1 -> n1 * (x - 1.5 / d1) * (x - 1.5 / d1) + 0.75
            x < 2.5 / d1 -> n1 * (x - 2.25 / d1) * (x - 2.25 / d1) + 0.9375
            else -> n1 * (x - 2.625 / d1) * (x - 2.625 / d1) + 0.984375
        }
    }

    /**
     * Bounce in-out easing
     */
    val bounceInOut: Easing = Easing { x ->
        if (x < 0.5f) {
            (1 - bounceOut(1 - 2 * x)) / 2
        } else {
            (1 + bounceOut(2 * x - 1)) / 2
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // BACK EASINGS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Back in easing (overshoot at start)
     */
    val backIn: Easing = Easing { x ->
        val s = 1.70158f
        x * x * ((s + 1) * x - s)
    }

    /**
     * Back out easing (overshoot at end)
     */
    val backOut: Easing = Easing { x ->
        val s = 1.70158f
        1 + (x - 1) * x * ((s + 1) * (x - 1) + s)
    }

    /**
     * Back in-out easing
     */
    val backInOut: Easing = Easing { x ->
        val s = 1.70158f * 1.525f
        if (x < 0.5f) {
            (x * x * ((s + 1) * 2 * x - s)) / 2
        } else {
            (2 * (x - 0.5) * ((s + 1) * 2 * (x - 0.5) + s) + 1) / 2
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CIRCULAR EASINGS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Circular in easing
     */
    val circIn: Easing = Easing { x ->
        1 - sqrt(1 - x * x)
    }

    /**
     * Circular out easing
     */
    val circOut: Easing = Easing { x ->
        sqrt(1 - (x - 1) * (x - 1))
    }

    /**
     * Circular in-out easing
     */
    val circInOut: Easing = Easing { x ->
        if (x < 0.5f) {
            (1 - sqrt(1 - (2 * x) * (2 * x))) / 2
        } else {
            (1 + sqrt(1 - (2 * x - 1) * (2 * x - 1))) / 2
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EXPO EASINGS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Exponential in easing
     */
    val expoIn: Easing = Easing { x ->
        if (x == 0f) 0f else 2f.pow(10 * x - 10)
    }

    /**
     * Exponential out easing
     */
    val expoOut: Easing = Easing { x ->
        if (x == 1f) 1f else 1 - 2f.pow(-10 * x)
    }

    /**
     * Exponential in-out easing
     */
    val expoInOut: Easing = Easing { x ->
        when {
            x == 0f -> 0f
            x == 1f -> 1f
            x < 0.5f -> 2f.pow(20 * x - 10) / 2
            else -> (2 - 2f.pow(-20 * x + 10)) / 2
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // STAGGER CALCULATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Calculate stagger delay for an item based on pattern
     * @param index Index of the item
     * @param totalItems Total number of items
     * @param baseDelay Base delay between items in milliseconds
     * @param pattern Stagger pattern to use
     */
    fun calculateStaggerDelay(
        index: Int,
        totalItems: Int,
        baseDelay: Int = 50,
        pattern: StaggerPattern
    ): Int {
        return when (pattern) {
            StaggerPattern.FORWARD -> index * baseDelay
            StaggerPattern.BACKWARD -> (totalItems - index - 1) * baseDelay
            StaggerPattern.CENTER_OUT -> {
                val center = totalItems / 2
                kotlin.math.abs(center - index) * baseDelay
            }
            StaggerPattern.RANDOM -> (Math.random() * baseDelay * 3).toInt()
            StaggerPattern.DOMINO -> (index % 3) * baseDelay * 2
            StaggerPattern.WAVE -> {
                val wave = sin(index * 0.5f) * baseDelay * 2
                wave.toInt().coerceAtLeast(0)
            }
            StaggerPattern.SPIRAL -> {
                val spiral = (index * 0.3f).toInt() * baseDelay
                spiral.coerceAtMost(baseDelay * totalItems / 2)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ANIMATION SPEC BUILDER
    // ═══════════════════════════════════════════════════════════════

    /**
     * Create a spring animation spec
     */
    fun createSpringSpec(
        stiffness: Float = 100f,
        damping: Float = 15f,
        mass: Float = 1f
    ): SpringSpec<Float> {
        return SpringSpec(
            stiffness = stiffness,
            damping = damping,
            mass = mass
        )
    }

    /**
     * Create a keyframes animation spec
     */
    fun createKeyframesSpec(
        durationMillis: Int = 300,
        vararg keyframes: Pair<Float, Float>,
        easing: Easing = Easing.InOut
    ): KeyframesSpec<Float> {
        return keyframes<Float> {
            durationMillis = durationMillis
            keyframes.forEach { (fraction, value) ->
                value at fraction * durationMillis with easing
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TRANSITION UTILITIES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Create a fade transition
     */
    fun fadeTransition(
        durationMillis: Int = 300,
        easing: Easing = Easing.Out
    ): Transition.TransitionSpec<Float> {
        return {
            tween(durationMillis = durationMillis, easing = easing)
        }
    }

    /**
     * Create a slide transition
     */
    fun slideTransition(
        durationMillis: Int = 300,
        easing: Easing = Easing.Out,
        slideDistance: Float = 100f
    ): Transition.TransitionSpec<Offset> {
        return {
            tween(durationMillis = durationMillis, easing = easing)
        }
    }

    /**
     * Create a scale transition
     */
    fun scaleTransition(
        durationMillis: Int = 300,
        easing: Easing = Easing.Out,
        minScale: Float = 0.8f,
        maxScale: Float = 1.2f
    ): Transition.TransitionSpec<Float> {
        return {
            tween(durationMillis = durationMillis, easing = easing)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CHOREOGRAPHY UTILITIES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Calculate animation delays for cascade effect
     */
    fun calculateCascadeDelays(
        itemCount: Int,
        baseDelay: Int = 50,
        cascadeType: CascadeType
    ): List<Int> {
        return when (cascadeType) {
            CascadeType.SEQUENTIAL -> List(itemCount) { it * baseDelay }
            CascadeType.GROUPED -> {
                val groupSize = 4
                List(itemCount) { (it / groupSize) * baseDelay * 2 }
            }
            CascadeType.RANDOM -> List(itemCount) { (Math.random() * baseDelay * 5).toInt() }
            CascadeType.WAVE -> {
                List(itemCount) { index ->
                    (sin(index * 0.3f) * baseDelay * 2 + baseDelay * 2).toInt().coerceAtLeast(0)
                }
            }
        }
    }

    enum class CascadeType {
        SEQUENTIAL,
        GROUPED,
        RANDOM,
        WAVE
    }

    // ═══════════════════════════════════════════════════════════════
    // PERFORMANCE UTILITIES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Check if animation should be reduced based on quality preset
     */
    fun shouldReduceAnimation(
        qualityPreset: String,
        batteryLevel: Int = 100,
        isCharging: Boolean = false
    ): Boolean {
        return when (qualityPreset) {
            "ULTRA" -> false
            "HIGH" -> batteryLevel < 20 && !isCharging
            "MEDIUM" -> batteryLevel < 30 && !isCharging
            "LOW" -> batteryLevel < 50 && !isCharging
            "POWER_SAVER" -> true
            "ACCESSIBILITY" -> true
            else -> false
        }
    }

    /**
     * Get recommended frame duration based on quality preset
     */
    fun getFrameDurationMillis(qualityPreset: String): Int {
        return when (qualityPreset) {
            "ULTRA" -> 16 // 60fps
            "HIGH" -> 16
            "MEDIUM" -> 24 // ~40fps
            "LOW" -> 33 // 30fps
            "POWER_SAVER" -> 50 // 20fps
            "ACCESSIBILITY" -> 100 // 10fps
            else -> 16
        }
    }
}

// Extension function for Float power
private fun Float.pow(exponent: Float): Float {
    return Math.pow(this.toDouble(), exponent.toDouble()).toFloat()
}
