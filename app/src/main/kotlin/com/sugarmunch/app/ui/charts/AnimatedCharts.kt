package com.sugarmunch.app.ui.charts

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.sin
import kotlin.math.cos

/**
 * Advanced animation specifications and presets for charts
 */

/**
 * Animation preset configurations
 */
object ChartAnimationPresets {
    
    val Bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val Snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    val Gentle = tween<Float>(
        durationMillis = 1500,
        easing = FastOutSlowInEasing
    )
    
    val Wave = infiniteRepeatable(
        animation = tween<Float>(durationMillis = 2000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
}

/**
 * Animated chart wrapper with entrance animations
 */
@Composable
fun <T> rememberAnimatedValue(
    initialValue: T,
    targetValue: T,
    animationSpec: AnimationSpec<T> = ChartAnimationPresets.Gentle,
    enabled: Boolean = true
): State<T> {
    val animatable = remember { Animatable(initialValue) }
    
    androidx.compose.runtime.LaunchedEffect(targetValue, enabled) {
        if (enabled && targetValue != animatable.value) {
            animatable.animateTo(targetValue, animationSpec)
        } else {
            animatable.snapTo(targetValue)
        }
    }
    
    return animatable.asState()
}

/**
 * Staggered animation for chart elements
 */
@Composable
fun rememberStaggeredAnimation(
    index: Int,
    totalItems: Int,
    delayPerItem: Int = 100,
    duration: Int = 800
): State<Float> {
    val progress = remember { Animatable(0f) }
    
    androidx.compose.runtime.LaunchedEffect(index, totalItems) {
        progress.snapTo(0f)
        androidx.coroutines.delay((index * delayPerItem).toLong())
        progress.animateTo(
            1f,
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        )
    }
    
    return progress.asState()
}

/**
 * Wave animation effect for charts
 */
@Composable
fun rememberWaveAnimation(
    duration: Int = 2000,
    phase: Float = 0f
): State<Float> {
    val wave = remember { Animatable(0f) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            wave.animateTo(
                360f + phase,
                animationSpec = tween(duration, easing = LinearEasing)
            )
            wave.snapTo(phase)
        }
    }
    
    return wave.asState()
}

/**
 * Pulsing glow animation for chart elements
 */
@Composable
fun rememberPulseAnimation(
    minAlpha: Float = 0.3f,
    maxAlpha: Float = 0.8f,
    duration: Int = 1500
): State<Float> {
    val pulse = remember { Animatable(minAlpha) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            pulse.animateTo(
                maxAlpha,
                animationSpec = tween(duration / 2, easing = FastOutSlowInEasing)
            )
            pulse.animateTo(
                minAlpha,
                animationSpec = tween(duration / 2, easing = FastOutSlowInEasing)
            )
        }
    }
    
    return pulse.asState()
}

/**
 * Rotate animation for pie/radar charts
 */
@Composable
fun rememberRotateAnimation(
    duration: Int = 3000,
    clockwise: Boolean = true
): State<Float> {
    val rotation = remember { Animatable(0f) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            rotation.animateTo(
                if (clockwise) 360f else -360f,
                animationSpec = tween(duration, easing = LinearEasing)
            )
            rotation.snapTo(0f)
        }
    }
    
    return rotation.asState()
}

/**
 * Draw animated sparkle effect on chart
 */
fun DrawScope.drawSparkle(
    center: Offset,
    size: Float,
    color: Color,
    animationProgress: Float
) {
    val sparkleCount = 4
    val sparkleLength = size * 0.3f * animationProgress
    
    repeat(sparkleCount) { i ->
        val angle = (i * 360 / sparkleCount) * Math.PI / 180
        val startX = center.x + cos(angle).toFloat() * size * 0.2f
        val startY = center.y + sin(angle).toFloat() * size * 0.2f
        val endX = center.x + cos(angle).toFloat() * (size * 0.2f + sparkleLength)
        val endY = center.y + sin(angle).toFloat() * (size * 0.2f + sparkleLength)
        
        drawLine(
            color = color.copy(alpha = animationProgress),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draw animated particle trail
 */
fun DrawScope.drawParticleTrail(
    start: Offset,
    end: Offset,
    color: Color,
    particleCount: Int = 5,
    animationProgress: Float
) {
    repeat(particleCount) { i ->
        val progress = (i.toFloat() / particleCount) * animationProgress
        val x = start.x + (end.x - start.x) * progress
        val y = start.y + (end.y - start.y) * progress
        val size = (8f * (1 - progress)) * animationProgress
        val alpha = (0.8f * (1 - progress)) * animationProgress
        
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = size,
            center = Offset(x, y)
        )
    }
}

/**
 * Chart tooltip composable with animation
 */
@Composable
fun AnimatedChartTooltip(
    tooltip: ChartTooltip,
    position: Offset,
    isVisible: Boolean
) {
    val alpha = rememberAnimatedValue(
        initialValue = 0f,
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = ChartAnimationPresets.Bouncy
    )
    
    val scale = rememberAnimatedValue(
        initialValue = 0.8f,
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = ChartAnimationPresets.Bouncy
    )
    
    // Tooltip implementation would go here
    // (simplified for brevity)
}

/**
 * Extreme animation effects for chart celebrations
 */
@Composable
fun ChartCelebrationEffect(
    triggered: Boolean,
    intensity: Float = 1.0f
) {
    val celebrationProgress = remember { Animatable(0f) }
    
    androidx.compose.runtime.LaunchedEffect(triggered) {
        if (triggered) {
            celebrationProgress.snapTo(0f)
            celebrationProgress.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    // Celebration effects would be rendered here
    // Confetti, fireworks, etc.
}

/**
 * SugarMunch extreme chart animations
 * Combines multiple animation effects for maximum visual impact
 */
object ExtremeChartAnimations {
    
    /**
     * Sugar Rush animation - rapid color cycling with scale
     */
    @Composable
    fun sugarRush(): State<SugarRushAnimationState> {
        val colorPhase = rememberWaveAnimation(duration = 1000)
        val scale = rememberPulseAnimation(minAlpha = 1f, maxAlpha = 1.1f, duration = 500)
        val rotation = rememberRotateAnimation(duration = 2000)
        
        return remember {
            derivedStateOf {
                SugarRushAnimationState(
                    colorPhase = colorPhase.value,
                    scale = scale.value,
                    rotation = rotation.value
                )
            }
        }
    }
    
    /**
     * Holographic shimmer animation
     */
    @Composable
    fun holographicShimmer(): State<Float> {
        return rememberWaveAnimation(duration = 1500)
    }
    
    /**
     * Liquid wave animation
     */
    @Composable
    fun liquidWave(): State<Float> {
        return rememberWaveAnimation(duration = 2000, phase = 90f)
    }
    
    /**
     * Neon flicker animation
     */
    @Composable
    fun neonFlicker(): State<Float> {
        val flicker = remember { Animatable(1f) }
        
        androidx.compose.runtime.LaunchedEffect(Unit) {
            while (true) {
                flicker.snapTo(0.7f + kotlin.math.random() * 0.3f)
                androidx.coroutines.delay(50)
            }
        }
        
        return flicker.asState()
    }
}

data class SugarRushAnimationState(
    val colorPhase: Float,
    val scale: Float,
    val rotation: Float
)

/**
 * Chart interaction animations
 */
object ChartInteractions {
    
    /**
     * Hover highlight animation
     */
    @Composable
    fun hoverHighlight(
        isHovered: Boolean
    ): State<Float> {
        return rememberAnimatedValue(
            initialValue = 0f,
            targetValue = if (isHovered) 1f else 0f,
            animationSpec = ChartAnimationPresets.Bouncy
        )
    }
    
    /**
     * Click ripple animation
     */
    @Composable
    fun clickRipple(
        triggered: Boolean
    ): State<Float> {
        val ripple = remember { Animatable(0f) }
        
        androidx.compose.runtime.LaunchedEffect(triggered) {
            if (triggered) {
                ripple.snapTo(0f)
                ripple.animateTo(
                    1f,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            }
        }
        
        return ripple.asState()
    }
}
