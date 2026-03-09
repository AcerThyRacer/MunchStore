package com.sugarmunch.app.ui.motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * Motion Presets for SugarMunch
 * Pre-built motion configurations for consistent animations
 */

/**
 * Spring animation presets
 */
object SpringPresets {
    
    val Bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val VeryBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val Snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    val VerySnappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryHigh
    )
    
    val Gentle = spring<Float>(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val Gummy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
}

/**
 * Tween animation presets
 */
object TweenPresets {
    
    val Fast = tween<Float>(
        durationMillis = 200,
        easing = FastOutSlowInEasing
    )
    
    val Normal = tween<Float>(
        durationMillis = 400,
        easing = FastOutSlowInEasing
    )
    
    val Slow = tween<Float>(
        durationMillis = 800,
        easing = FastOutSlowInEasing
    )
    
    val VerySlow = tween<Float>(
        durationMillis = 1500,
        easing = FastOutSlowInEasing
    )
    
    val Instant = tween<Float>(
        durationMillis = 0
    )
    
    fun custom(
        duration: Int,
        easing: Easing = FastOutSlowInEasing,
        delay: Int = 0
    ): AnimationSpec<Float> {
        return tween(duration, delay, easing)
    }
}

/**
 * Motion configuration data class
 */
data class MotionPreset(
    val enterScale: Float = 0f,
    val exitScale: Float = 1f,
    val enterAlpha: Float = 0f,
    val exitAlpha: Float = 1f,
    val enterOffsetY: Int = 100,
    val exitOffsetY: Int = -100,
    val enterOffsetX: Int = 100,
    val exitOffsetX: Int = -100,
    val duration: Int = 400,
    val easing: Easing = FastOutSlowInEasing,
    val dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    val stiffness: Float = Spring.StiffnessLow,
    val delay: Int = 0
)

/**
 * Pre-built motion presets
 */
object MotionPresets {
    
    val CandyEntrance = MotionPreset(
        enterScale = 0.5f,
        enterAlpha = 0f,
        enterOffsetY = 200,
        duration = 600,
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val LiquidMorph = MotionPreset(
        enterScale = 0.8f,
        enterAlpha = 0f,
        duration = 800,
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessVeryLow
    )
    
    val NeonFlash = MotionPreset(
        enterAlpha = 0f,
        duration = 200,
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryHigh
    )
    
    val SugarRushEntrance = MotionPreset(
        enterScale = 0.3f,
        enterAlpha = 0f,
        enterOffsetY = 300,
        duration = 400,
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
        delay = 0
    )
    
    val GentleFade = MotionPreset(
        enterAlpha = 0f,
        duration = 500,
        easing = LinearEasing
    )
    
    val BounceIn = MotionPreset(
        enterScale = 0.5f,
        enterAlpha = 0f,
        duration = 700,
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val SlideIn = MotionPreset(
        enterAlpha = 0f,
        enterOffsetX = 200,
        duration = 400,
        easing = FastOutSlowInEasing
    )
    
    val ZoomIn = MotionPreset(
        enterScale = 0.1f,
        enterAlpha = 0f,
        duration = 300,
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    val FlipIn = MotionPreset(
        enterScale = 0f,
        enterAlpha = 0f,
        duration = 500,
        easing = FastOutSlowInEasing
    )
    
    val SwirlIn = MotionPreset(
        enterScale = 0.5f,
        enterAlpha = 0f,
        enterOffsetY = 100,
        enterOffsetX = 100,
        duration = 600,
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

/**
 * Staggered motion preset
 */
data class StaggeredMotionPreset(
    val baseDelay: Int = 50,
    val delayMultiplier: Float = 1.2f,
    val maxDelay: Int = 500,
    val duration: Int = 400,
    val easing: Easing = FastOutSlowInEasing
)

object StaggeredPresets {
    val Quick = StaggeredMotionPreset(
        baseDelay = 30,
        delayMultiplier = 1.1f,
        maxDelay = 300,
        duration = 300
    )
    
    val Normal = StaggeredMotionPreset(
        baseDelay = 50,
        delayMultiplier = 1.2f,
        maxDelay = 500,
        duration = 400
    )
    
    val Slow = StaggeredMotionPreset(
        baseDelay = 100,
        delayMultiplier = 1.3f,
        maxDelay = 800,
        duration = 600
    )
    
    val Extreme = StaggeredMotionPreset(
        baseDelay = 150,
        delayMultiplier = 1.5f,
        maxDelay = 1000,
        duration = 800
    )
}

/**
 * Particle motion preset
 */
data class ParticleMotionPreset(
    val particleCount: Int = 20,
    val minSpeed: Float = 5f,
    val maxSpeed: Float = 15f,
    val minSize: Float = 4f,
    val maxSize: Float = 12f,
    val gravity: Float = 0.5f,
    val friction: Float = 0.98f,
    val colors: List<Color> = listOf(
        SugarDimens.Brand.hotPink,
        SugarDimens.Brand.mint,
        SugarDimens.Brand.yellow,
        SugarDimens.Brand.candyOrange,
        SugarDimens.Brand.bubblegumBlue
    ),
    val spreadAngle: Float = 360f,
    val explosionForce: Float = 1.0f
)

object ParticlePresets {
    val Confetti = ParticleMotionPreset(
        particleCount = 50,
        minSpeed = 10f,
        maxSpeed = 20f,
        minSize = 6f,
        maxSize = 14f,
        gravity = 0.3f,
        spreadAngle = 360f,
        explosionForce = 1.5f
    )
    
    val Sparkle = ParticleMotionPreset(
        particleCount = 10,
        minSpeed = 5f,
        maxSpeed = 10f,
        minSize = 3f,
        maxSize = 8f,
        gravity = 0.1f,
        spreadAngle = 180f,
        explosionForce = 0.8f
    )
    
    val Explosion = ParticleMotionPreset(
        particleCount = 100,
        minSpeed = 15f,
        maxSpeed = 30f,
        minSize = 5f,
        maxSize = 15f,
        gravity = 0.8f,
        spreadAngle = 360f,
        explosionForce = 2.0f
    )
    
    val Trail = ParticleMotionPreset(
        particleCount = 5,
        minSpeed = 3f,
        maxSpeed = 7f,
        minSize = 2f,
        maxSize = 6f,
        gravity = 0.2f,
        spreadAngle = 90f,
        explosionForce = 0.5f
    )
}

/**
 * Get animation spec from preset
 */
@Composable
fun <T> getAnimationSpec(
    preset: MotionPreset
): AnimationSpec<T> where T : Any {
    return spring(
        dampingRatio = preset.dampingRatio,
        stiffness = preset.stiffness
    ) as AnimationSpec<T>
}

/**
 * Calculate staggered delay for item
 */
fun calculateStaggeredDelay(
    index: Int,
    preset: StaggeredMotionPreset = StaggeredPresets.Normal
): Int {
    val delay = preset.baseDelay * kotlin.math.pow(preset.delayMultiplier, index.toFloat())
    return delay.toInt().coerceAtMost(preset.maxDelay)
}

/**
 * Extreme motion preset - combines multiple effects
 */
data class ExtremeMotionPreset(
    val scale: MotionPreset = MotionPresets.BounceIn,
    val fade: MotionPreset = MotionPresets.GentleFade,
    val slide: MotionPreset = MotionPresets.SlideIn,
    val particle: ParticleMotionPreset = ParticlePresets.Confetti,
    val staggered: StaggeredMotionPreset = StaggeredPresets.Normal,
    val intensity: Float = 1.0f
)

object ExtremePresets {
    val Maximum = ExtremeMotionPreset(
        scale = MotionPresets.BounceIn.copy(duration = 800),
        particle = ParticlePresets.Explosion,
        staggered = StaggeredPresets.Extreme,
        intensity = 2.0f
    )
    
    val Moderate = ExtremeMotionPreset(
        scale = MotionPresets.BounceIn,
        particle = ParticlePresets.Confetti,
        staggered = StaggeredPresets.Normal,
        intensity = 1.0f
    )
    
    val Subtle = ExtremeMotionPreset(
        scale = MotionPresets.GentleFade,
        particle = ParticlePresets.Sparkle,
        staggered = StaggeredPresets.Quick,
        intensity = 0.5f
    )
}
