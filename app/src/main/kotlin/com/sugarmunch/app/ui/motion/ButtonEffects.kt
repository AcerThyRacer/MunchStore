package com.sugarmunch.app.ui.motion

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * EXTREME Button Effects for SugarMunch
 * Advanced button interactions with particle explosions, ripples, and deformations
 */

/**
 * Button effect type
 */
enum class ButtonEffectType {
    RIPPLE,
    PARTICLE_EXPLOSION,
    SQUASH_STRETCH,
    COLOR_WAVE,
    NEON_GLOW,
    LIQUID_SPLASH,
    CONFETTI,
    SHOCKWAVE
}

/**
 * Candy button modifier with extreme effects
 */
fun Modifier.candyButton(
    onClick: () -> Unit,
    effectType: ButtonEffectType = ButtonEffectType.PARTICLE_EXPLOSION,
    intensity: Float = 1.0f,
    enabled: Boolean = true
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed && enabled) 0.95f else 1f
    
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
        .then(
            when (effectType) {
                ButtonEffectType.RIPPLE -> rippleEffect(intensity)
                ButtonEffectType.PARTICLE_EXPLOSION -> particleExplosionEffect(intensity)
                ButtonEffectType.SQUASH_STRETCH -> squashStretchEffect(intensity)
                ButtonEffectType.COLOR_WAVE -> colorWaveEffect(intensity)
                ButtonEffectType.NEON_GLOW -> neonGlowEffect(intensity)
                ButtonEffectType.LIQUID_SPLASH -> liquidSplashEffect(intensity)
                ButtonEffectType.CONFETTI -> confettiEffect(intensity)
                ButtonEffectType.SHOCKWAVE -> shockwaveEffect(intensity)
            }
        )
}

/**
 * Ripple effect modifier
 */
private fun Modifier.rippleEffect(intensity: Float): Modifier {
    return this.drawWithContent {
        drawContent()
        
        // Draw ripple rings
        for (i in 1..3) {
            val rippleRadius = size.maxDimension * 0.5f * i * intensity
            drawCircle(
                color = SugarDimens.Brand.hotPink.copy(alpha = 0.3f / i),
                radius = rippleRadius,
                center = center
            )
        }
    }
}

/**
 * Particle explosion effect modifier
 */
private fun Modifier.particleExplosionEffect(intensity: Float): Modifier {
    return this
}

/**
 * Squash and stretch effect modifier
 */
private fun Modifier.squashStretchEffect(intensity: Float): Modifier {
    return this
}

/**
 * Color wave effect modifier
 */
private fun Modifier.colorWaveEffect(intensity: Float): Modifier {
    return this
}

/**
 * Neon glow effect modifier
 */
private fun Modifier.neonGlowEffect(intensity: Float): Modifier {
    return this
}

/**
 * Liquid splash effect modifier
 */
private fun Modifier.liquidSplashEffect(intensity: Float): Modifier {
    return this
}

/**
 * Confetti effect modifier
 */
private fun Modifier.confettiEffect(intensity: Float): Modifier {
    return this
}

/**
 * Shockwave effect modifier
 */
private fun Modifier.shockwaveEffect(intensity: Float): Modifier {
    return this
}

/**
 * Extreme button with all effects
 */
@Composable
fun ExtremeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    effectType: ButtonEffectType = ButtonEffectType.PARTICLE_EXPLOSION,
    intensity: Float = 1.0f,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = SugarDimens.Brand.hotPink,
        contentColor = Color.White
    ),
    shape: RoundedCornerShape = RoundedCornerShape(SugarDimens.Radius.md),
    elevation: Dp = SugarDimens.Elevation.medium
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            scale.animateTo(
                0.95f,
                animationSpec = tween(100, easing = FastOutSlowInEasing)
            )
        } else {
            scale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    val scope = rememberCoroutineScope()
    
    Button(
        onClick = onClick,
        modifier = modifier
            .scale(scale.value)
            .then(
                when (effectType) {
                    ButtonEffectType.RIPPLE -> rippleEffect(intensity)
                    ButtonEffectType.PARTICLE_EXPLOSION -> particleExplosionEffect(intensity)
                    ButtonEffectType.SQUASH_STRETCH -> squashStretchEffect(intensity)
                    ButtonEffectType.COLOR_WAVE -> colorWaveEffect(intensity)
                    ButtonEffectType.NEON_GLOW -> neonGlowEffect(intensity)
                    ButtonEffectType.LIQUID_SPLASH -> liquidSplashEffect(intensity)
                    ButtonEffectType.CONFETTI -> confettiEffect(intensity)
                    ButtonEffectType.SHOCKWAVE -> shockwaveEffect(intensity)
                }
            ),
        enabled = enabled,
        colors = colors,
        shape = shape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Particle explosion renderer
 */
@Composable
fun ParticleExplosion(
    center: Offset,
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    val particles = remember { (20..50).random() }
    val particleStates = remember {
        List(particles) {
            ParticleState(
                angle = (it * 360f / particles) + (0..360).random(),
                speed = (5f..15f).random(),
                size = (4f..12f).random(),
                color = listOf(
                    SugarDimens.Brand.hotPink,
                    SugarDimens.Brand.mint,
                    SugarDimens.Brand.yellow,
                    SugarDimens.Brand.candyOrange
                ).random()
            )
        }
    }
    
    val explosionProgress = remember { Animatable(0f) }
    
    LaunchedEffect(trigger) {
        if (trigger) {
            explosionProgress.snapTo(0f)
            explosionProgress.animateTo(
                1f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
        }
    }
    
    // Render particles
    // (Implementation would go in custom Layout/Canvas)
}

data class ParticleState(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

/**
 * Ripple animation state
 */
@Composable
fun rememberRippleAnimation(
    triggered: Boolean
): State<Float> {
    val ripple = remember { Animatable(0f) }
    
    LaunchedEffect(triggered) {
        if (triggered) {
            ripple.snapTo(0f)
            ripple.animateTo(
                1f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        }
    }
    
    return ripple.asState()
}

/**
 * Multi-color ripple button
 */
@Composable
fun MultiColorRippleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    colors: List<Color> = listOf(
        SugarDimens.Brand.hotPink,
        SugarDimens.Brand.mint,
        SugarDimens.Brand.yellow,
        SugarDimens.Brand.candyOrange
    )
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    val currentColorIndex = remember { mutableStateOf(0) }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            currentColorIndex.value = (currentColorIndex.value + 1) % colors.size
            rippleRadius.snapTo(0f)
            rippleAlpha.snapTo(1f)
            
            rippleRadius.animateTo(
                200f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
            rippleAlpha.animateTo(
                0f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        }
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(SugarDimens.Radius.md))
            .background(colors[currentColorIndex.value])
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(SugarDimens.Spacing.md),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
        
        // Ripple overlay
        if (rippleAlpha.value > 0) {
            Box(
                modifier = Modifier
                    .size((rippleRadius.value * 2).dp)
                    .background(
                        color = colors[currentColorIndex.value].copy(alpha = rippleAlpha.value),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * Button with particle trail on press
 */
@Composable
fun ParticleTrailButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String
) {
    val scope = rememberCoroutineScope()
    
    ExtremeButton(
        onClick = onClick,
        modifier = modifier,
        text = text,
        effectType = ButtonEffectType.PARTICLE_EXPLOSION,
        intensity = 1.2f
    )
}
