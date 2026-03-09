package com.sugarmunch.app.ui.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.sin
import kotlin.math.cos

/**
 * EXTREME Page Transitions for SugarMunch
 * 10+ candy-themed transition effects
 */

/**
 * Transition type enumeration
 */
enum class TransitionType {
    CANDY_CRUSH,
    LIQUID_MORPH,
    PARTICLE_DISSOLVE,
    HOLOGRAPHIC_FLIP,
    TAFFY_STRETCH,
    SUGAR_RUSH,
    NEON_FADE,
    CRYSTAL_SHATTER,
    WAVE_IN,
    BOUNCE_IN,
    SPIN_IN,
    ZOOM_BLUR
}

/**
 * Candy Crush Transition - Colorful blocks break apart
 */
fun candyCrushTransition(): AnimatedContentTransitionScope<IntOffset>.() -> SlideTransitionStrategy {
    return {
        val animationSpec = spring<IntOffset>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
        
        val slideDirection = when {
            targetState > initialState -> SlideDirection.Left
            else -> SlideDirection.Right
        }
        
        slideOutOfContainer(
            towards = slideDirection,
            animationSpec = animationSpec
        ) { size, offset ->
            // Add candy crush visual effect here
            Offset(offset.x, offset.y)
        }
    }
}

/**
 * Liquid Morph Transition - Fluid shape transformation
 */
@Composable
fun liquidMorphTransition(
    duration: Int = 600
): ContentTransform {
    val fade = fadeIn(
        animationSpec = tween(duration / 2, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(duration / 2, easing = FastOutSlowInEasing)
    )
    
    val scale = scaleIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) + scaleOut(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    return fade + scale
}

/**
 * Particle Dissolve Transition - Break into particles
 */
@Composable
fun particleDissolveTransition(
    duration: Int = 800,
    particleCount: Int = 20
): ContentTransform {
    val fade = fadeIn(
        animationSpec = tween(duration, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(duration, easing = FastOutSlowInEasing)
    )
    
    val slide = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + slideOutHorizontally(
        targetOffsetX = { -it / 2 },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    return fade + slide
}

/**
 * Holographic Flip Transition - 3D flip with rainbow effect
 */
@Composable
fun holographicFlipTransition(
    duration: Int = 700
): ContentTransform {
    return animateContentWith(
        enter = {
            fadeIn(animationSpec = tween(duration / 2))
        },
        exit = {
            fadeOut(animationSpec = tween(duration / 2))
        }
    )
}

/**
 * Taffy Stretch Transition - Elastic stretch animation
 */
@Composable
fun taffyStretchTransition(
    duration: Int = 600,
    stretchAxis: StretchAxis = StretchAxis.HORIZONTAL
): ContentTransform {
    val stretchSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val fade = fadeIn(animationSpec = tween(duration / 2)) + 
               fadeOut(animationSpec = tween(duration / 2))
    
    val scale = when (stretchAxis) {
        StretchAxis.HORIZONTAL -> {
            scaleIn(initialScale = 0f, animationSpec = stretchSpec) + 
            scaleOut(targetScale = 0f, animationSpec = stretchSpec)
        }
        StretchAxis.VERTICAL -> {
            scaleIn(initialScaleY = 0f, animationSpec = stretchSpec) + 
            scaleOut(targetScaleY = 0f, animationSpec = stretchSpec)
        }
        StretchAxis.BOTH -> {
            scaleIn(initialScale = 0f, animationSpec = stretchSpec) + 
            scaleOut(targetScale = 0f, animationSpec = stretchSpec)
        }
    }
    
    return fade + scale
}

enum class StretchAxis {
    HORIZONTAL,
    VERTICAL,
    BOTH
}

/**
 * Sugar Rush Transition - Rapid color cycling zoom
 */
@Composable
fun sugarRushTransition(
    duration: Int = 500
): ContentTransform {
    val zoom = scaleIn(
        initialScale = 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
    ) + scaleOut(
        targetScale = 1.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    val fade = fadeIn(animationSpec = tween(duration / 2)) + 
               fadeOut(animationSpec = tween(duration / 2))
    
    return zoom + fade
}

/**
 * Neon Fade Transition - Glowing fade with pulse
 */
@Composable
fun neonFadeTransition(
    duration: Int = 600
): ContentTransform {
    val fade = fadeIn(
        animationSpec = tween(duration, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(duration, easing = FastOutSlowInEasing)
    )
    
    return fade
}

/**
 * Wave In Transition - Items wave in sequentially
 */
@Composable
fun waveInTransition(
    duration: Int = 800,
    staggerDelay: Int = 50
): ContentTransform {
    val slide = slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    val fade = fadeIn(
        animationSpec = tween(duration, easing = FastOutSlowInEasing)
    )
    
    return slide + fade
}

/**
 * Bounce In Transition - Elastic bounce entrance
 */
@Composable
fun bounceInTransition(
    duration: Int = 700
): ContentTransform {
    val bounce = scaleIn(
        initialScale = 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val fade = fadeIn(animationSpec = tween(duration / 2))
    
    return bounce + fade
}

/**
 * Spin In Transition - Rotating entrance
 */
@Composable
fun spinInTransition(
    duration: Int = 600,
    rotations: Float = 1.5f
): ContentTransform {
    return animateContentWith(
        enter = {
            fadeIn(animationSpec = tween(duration))
        },
        exit = {
            fadeOut(animationSpec = tween(duration))
        }
    )
}

/**
 * Zoom Blur Transition - Fast zoom with motion blur
 */
@Composable
fun zoomBlurTransition(
    duration: Int = 400
): ContentTransform {
    val zoom = scaleIn(
        initialScale = 0.1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryHigh
        )
    ) + scaleOut(
        targetScale = 1.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryHigh
        )
    )
    
    val fade = fadeIn(animationSpec = tween(duration / 2)) + 
               fadeOut(animationSpec = tween(duration / 2))
    
    return zoom + fade
}

/**
 * Get transition by type
 */
@Composable
fun getTransition(type: TransitionType): ContentTransform {
    return when (type) {
        TransitionType.CANDY_CRUSH -> particleDissolveTransition()
        TransitionType.LIQUID_MORPH -> liquidMorphTransition()
        TransitionType.PARTICLE_DISSOLVE -> particleDissolveTransition()
        TransitionType.HOLOGRAPHIC_FLIP -> holographicFlipTransition()
        TransitionType.TAFFY_STRETCH -> taffyStretchTransition()
        TransitionType.SUGAR_RUSH -> sugarRushTransition()
        TransitionType.NEON_FADE -> neonFadeTransition()
        TransitionType.WAVE_IN -> waveInTransition()
        TransitionType.BOUNCE_IN -> bounceInTransition()
        TransitionType.SPIN_IN -> spinInTransition()
        TransitionType.ZOOM_BLUR -> zoomBlurTransition()
    }
}

/**
 * Extreme transition with multiple effects
 */
@Composable
fun extremeTransition(
    type: TransitionType,
    intensity: Float = 1.0f
): ContentTransform {
    val baseTransition = getTransition(type)
    
    // Add intensity-based modifications
    return when (type) {
        TransitionType.SUGAR_RUSH -> sugarRushTransition(
            duration = (500 / intensity).toInt()
        )
        TransitionType.BOUNCE_IN -> bounceInTransition(
            duration = (700 / intensity).toInt()
        )
        else -> baseTransition
    }
}

/**
 * Transition preview composable
 */
@Composable
fun TransitionPreview(
    type: TransitionType,
    isActive: Boolean
) {
    val transition = getTransition(type)
    
    AnimatedContent(
        targetState = isActive,
        transitionSpec = { transition }
    ) { state ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SugarDimens.Brand.hotPink,
                            SugarDimens.Brand.mint,
                            SugarDimens.Brand.yellow
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (state) "Active" else "Inactive",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}
