package com.sugarmunch.app.ui.feedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.sin

/**
 * EXTREME Error Effects for SugarMunch
 * Shake animations, cracked glass, warning pulses, and error ripples
 */

/**
 * Error effect type
 */
enum class ErrorEffectType {
    SHAKE,
    RED_WAVE,
    CRACKED_GLASS,
    WARNING_PULSE,
    ERROR_RIPPLE,
    GLITCH,
    DISTORTION,
    EARTHQUAKE
}

/**
 * Error state holder
 */
class ErrorState {
    var showError by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf("")
        private set
    
    var errorType by mutableStateOf(ErrorEffectType.SHAKE)
        private set
    
    fun showError(
        message: String,
        type: ErrorEffectType = ErrorEffectType.SHAKE
    ) {
        errorMessage = message
        errorType = type
        showError = true
    }
    
    fun clearError() {
        showError = false
        errorMessage = ""
    }
}

/**
 * Main error effect composable
 */
@Composable
fun ErrorEffect(
    showError: Boolean,
    modifier: Modifier = Modifier,
    errorType: ErrorEffectType = ErrorEffectType.SHAKE,
    intensity: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val shakeOffset = remember { Animatable(0f) }
    val pulseAlpha = remember { Animatable(0f) }
    val crackProgress = remember { Animatable(0f) }
    val glitchOffset = remember { Animatable(Offset.Zero) }
    
    LaunchedEffect(showError) {
        if (showError) {
            when (errorType) {
                ErrorEffectType.SHAKE -> {
                    shakeOffset.snapTo(0f)
                    repeat(6) { i ->
                        shakeOffset.animateTo(
                            if (i % 2 == 0) -15f * intensity else 15f * intensity,
                            animationSpec = tween(100)
                        )
                    }
                    shakeOffset.animateTo(0f, animationSpec = tween(200))
                }
                ErrorEffectType.WARNING_PULSE -> {
                    pulseAlpha.snapTo(0f)
                    pulseAlpha.animateTo(
                        0.8f * intensity,
                        animationSpec = tween(300)
                    )
                    pulseAlpha.animateTo(
                        0f,
                        animationSpec = tween(300)
                    )
                }
                ErrorEffectType.CRACKED_GLASS -> {
                    crackProgress.snapTo(0f)
                    crackProgress.animateTo(
                        1f * intensity,
                        animationSpec = tween(500)
                    )
                }
                ErrorEffectType.GLITCH -> {
                    repeat(10) {
                        glitchOffset.snapTo(
                            Offset(
                                (Math.random().toFloat() - 0.5f) * 20f * intensity,
                                (Math.random().toFloat() - 0.5f) * 20f * intensity
                            )
                        )
                        delay(50)
                    }
                    glitchOffset.snapTo(Offset.Zero)
                }
                else -> {}
            }
        }
    }
    
    Box(modifier = modifier) {
        // Error overlay
        if (showError && errorType == ErrorEffectType.RED_WAVE) {
            RedWaveOverlay(alpha = pulseAlpha.value)
        }
        
        if (showError && errorType == ErrorEffectType.CRACKED_GLASS) {
            CrackedGlassOverlay(progress = crackProgress.value)
        }
        
        // Content with error effects
        Box(
            modifier = Modifier
                .then(
                    when (errorType) {
                        ErrorEffectType.SHAKE -> Modifier.translate(shakeOffset.value, 0f)
                        ErrorEffectType.GLITCH -> Modifier.translate(glitchOffset.value.x, glitchOffset.value.y)
                        ErrorEffectType.EARTHQUAKE -> Modifier.translate(
                            shakeOffset.value * 2,
                            shakeOffset.value
                        )
                        else -> Modifier
                    }
                )
        ) {
            content()
        }
        
        // Warning pulse overlay
        if (showError && errorType == ErrorEffectType.WARNING_PULSE) {
            WarningPulseOverlay(alpha = pulseAlpha.value, intensity = intensity)
        }
    }
}

/**
 * Red wave ripple overlay
 */
@Composable
fun RedWaveOverlay(alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = alpha * 0.3f))
    )
}

/**
 * Cracked glass overlay
 */
@Composable
fun CrackedGlassOverlay(progress: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        if (progress > 0) {
            drawCracks(progress)
        }
    }
}

/**
 * Draw crack lines
 */
private fun DrawScope.drawCracks(progress: Float) {
    val crackColor = Color.Gray.copy(alpha = 0.5f * progress)
    
    // Draw multiple crack lines from center
    repeat(8) { i ->
        val angle = (i * 45).toFloat() * Math.PI / 180
        val length = size.minDimension * 0.4f * progress
        
        drawLine(
            color = crackColor,
            start = center,
            end = Offset(
                center.x + kotlin.math.cos(angle) * length,
                center.y + kotlin.math.sin(angle) * length
            ),
            strokeWidth = 2f * progress,
            cap = StrokeCap.Round
        )
        
        // Draw secondary cracks
        if (progress > 0.5f) {
            drawSecondaryCracks(
                center = Offset(
                    center.x + kotlin.math.cos(angle) * length * 0.5f,
                    center.y + kotlin.math.sin(angle) * length * 0.5f
                ),
                angle = angle,
                progress = progress,
                color = crackColor
            )
        }
    }
}

/**
 * Draw secondary crack branches
 */
private fun DrawScope.drawSecondaryCracks(
    center: Offset,
    angle: Float,
    progress: Float,
    color: Color
) {
    val secondaryLength = 50f * progress
    
    repeat(2) { i ->
        val secondaryAngle = angle + (if (i == 0) 0.5f else -0.5f)
        drawLine(
            color = color,
            start = center,
            end = Offset(
                center.x + kotlin.math.cos(secondaryAngle) * secondaryLength,
                center.y + kotlin.math.sin(secondaryAngle) * secondaryLength
            ),
            strokeWidth = 1.5f * progress
        )
    }
}

/**
 * Warning pulse overlay
 */
@Composable
fun WarningPulseOverlay(alpha: Float, intensity: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = (4f * intensity).dp,
                color = Color(0xFFFF0000).copy(alpha = alpha),
                shape = RoundedCornerShape(16.dp)
            )
    )
}

/**
 * Error message display with animation
 */
@Composable
fun ErrorMessageDisplay(
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(visible) {
        if (visible) {
            scale.snapTo(0.8f)
            alpha.snapTo(0f)
            
            scale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            alpha.animateTo(1f, animationSpec = tween(300))
        } else {
            alpha.animateTo(0f, animationSpec = tween(200))
        }
    }
    
    if (visible) {
        Card(
            modifier = modifier
                .scale(scale.value)
                .alpha(alpha.value),
            colors = CardDefaults.cardColors(
                containerColor = SugarDimens.Brand.hotPink.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(SugarDimens.Radius.md)
        ) {
            Row(
                modifier = Modifier.padding(SugarDimens.Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️ $message",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
        }
    }
}

/**
 * Shake animation modifier
 */
fun Modifier.shake(
    triggered: Boolean,
    intensity: Float = 1.0f
): Modifier {
    val shakeOffset = remember { Animatable(0f) }
    
    LaunchedEffect(triggered) {
        if (triggered) {
            shakeOffset.snapTo(0f)
            repeat(6) { i ->
                shakeOffset.animateTo(
                    if (i % 2 == 0) -15f * intensity else 15f * intensity,
                    animationSpec = tween(100)
                )
            }
            shakeOffset.animateTo(0f, animationSpec = tween(200))
        }
    }
    
    return this.translate(shakeOffset.value, 0f)
}

/**
 * Glitch effect modifier
 */
fun Modifier.glitch(
    triggered: Boolean,
    intensity: Float = 1.0f
): Modifier {
    val glitchOffsetX = remember { Animatable(0f) }
    val glitchOffsetY = remember { Animatable(0f) }
    
    LaunchedEffect(triggered) {
        if (triggered) {
            repeat(10) {
                glitchOffsetX.snapTo((Math.random().toFloat() - 0.5f) * 20f * intensity)
                glitchOffsetY.snapTo((Math.random().toFloat() - 0.5f) * 20f * intensity)
                delay(50)
            }
            glitchOffsetX.snapTo(0f)
            glitchOffsetY.snapTo(0f)
        }
    }
    
    return this.translate(glitchOffsetX.value, glitchOffsetY.value)
}

/**
 * Quick error shake
 */
@Composable
fun QuickErrorShake(
    showError: Boolean,
    content: @Composable () -> Unit
) {
    ErrorEffect(
        showError = showError,
        errorType = ErrorEffectType.SHAKE,
        content = content
    )
}
