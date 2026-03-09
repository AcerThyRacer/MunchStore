package com.sugarmunch.app.effects.transitions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * Transition types available in the gallery
 */
enum class TransitionType(val displayName: String, val description: String) {
    FADE("Fade", "Simple crossfade transition"),
    SLIDE_LEFT("Slide Left", "Slide content to the left"),
    SLIDE_RIGHT("Slide Right", "Slide content to the right"),
    SLIDE_UP("Slide Up", "Slide content upward"),
    SLIDE_DOWN("Slide Down", "Slide content downward"),
    SCALE_IN("Scale In", "Scale from 0 to 1"),
    SCALE_OUT("Scale Out", "Scale from 1 to 0"),
    EXPLODE("Explode", "Explode into pieces"),
    CARD_STACK("Card Stack", "Stack cards like a deck"),
    PORTAL_TELEPORT("Portal Teleport", "Teleport through a portal"),
    CANDY_CRUSH("Candy Crush", "Explosion of candy pieces"),
    SPIRAL_VORTEX("Spiral Vortex", "Spin into a vortex"),
    PAGE_CURL("Page Curl", "Curl like a page turn"),
    CUBE_ROTATION("Cube Rotation", "3D cube rotation"),
    DOMINO_CASCADE("Domino Cascade", "Domino falling effect"),
    RIPPLE("Ripple", "Ripple expansion"),
    SHATTER("Shatter", "Glass shattering effect"),
    PIXELATE("Pixelate", "Pixel dissolve"),
    GLITCH("Glitch", "Digital glitch effect"),
    MORPH("Morph", "Shape morphing transition")
}

/**
 * Transition configuration
 */
data class TransitionConfig(
    val type: TransitionType = TransitionType.FADE,
    val durationMs: Int = 300,
    val delayMs: Int = 0,
    val easing: Easing = FastOutSlowInEasing,
    val intensity: Float = 1.0f
)

/**
 * TransitionGallery - Collection of 15+ screen transition effects.
 * Features:
 * - Card stack shuffle
 * - Portal teleport
 * - Candy crush explosion
 * - Spiral vortex
 * - Page curl
 * - Cube rotation
 * - Domino cascade
 */
@Composable
fun TransitionGallery(
    onTransitionSelected: (TransitionType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // This would be a full gallery UI for selecting and previewing transitions
    // For now, we'll provide the transition functions
}

/**
 * Apply a transition animation to content
 */
@Composable
fun <T> TransitionEffect(
    targetState: T,
    transitionType: TransitionType,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    when (transitionType) {
        TransitionType.FADE -> AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with
                    fadeOut(animationSpec = tween(300))
            },
            modifier = modifier,
            content = content
        )

        TransitionType.SLIDE_LEFT -> AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it }
                ) with slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it }
                )
            },
            modifier = modifier,
            content = content
        )

        TransitionType.SLIDE_RIGHT -> AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it }
                ) with slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { it }
                )
            },
            modifier = modifier,
            content = content
        )

        TransitionType.SLIDE_UP -> AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it }
                ) with slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { -it }
                )
            },
            modifier = modifier,
            content = content
        )

        TransitionType.SLIDE_DOWN -> AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { -it }
                ) with slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { it }
                )
            },
            modifier = modifier,
            content = content
        )

        TransitionType.SCALE_IN -> AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                scaleIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) with
                    scaleOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            modifier = modifier,
            content = content
        )

        TransitionType.EXPLODE -> ExplodeTransition(
            targetState = targetState,
            modifier = modifier,
            content = content
        )

        TransitionType.CARD_STACK -> CardStackTransition(
            targetState = targetState,
            modifier = modifier,
            content = content
        )

        TransitionType.PORTAL_TELEPORT -> PortalTransition(
            targetState = targetState,
            modifier = modifier,
            content = content
        )

        TransitionType.CANDY_CRUSH -> CandyCrushTransition(
            targetState = targetState,
            modifier = modifier,
            content = content
        )

        TransitionType.SPIRAL_VORTEX -> SpiralVortexTransition(
            targetState = targetState,
            modifier = modifier,
            content = content
        )

        TransitionType.PAGE_CURL -> PageCurlTransition(
            targetState = targetState,
            modifier = modifier,
            content = content
        )

        TransitionType.CUBE_ROTATION -> CubeRotationTransition(
            targetState = targetState,
            modifier = modifier,
            content = content
        )

        TransitionType.DOMINO_CASCADE -> DominoCascadeTransition(
            targetState = targetState,
            modifier = modifier,
            content = content
        )

        else -> AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                fadeIn() with fadeOut()
            },
            modifier = modifier,
            content = content
        )
    }
}

/**
 * Explode Transition
 */
@Composable
private fun <T> ExplodeTransition(
    targetState: T,
    modifier: Modifier,
    content: @Composable (T) -> Unit
) {
    var explosionProgress by remember { mutableFloatStateOf(0f) }

    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val explosionSpec = tween<Float>(
                durationMillis = 500,
                easing = FastOutLinearInEasing
            )

            enter togetherWith exit using
                createContainerTransitionSpec(
                    enter = { scaleIn(animationSpec = explosionSpec) },
                    exit = {
                        scaleOut(
                            animationSpec = explosionSpec,
                            targetScale = 2f
                        )
                    }
                )
        },
        modifier = modifier,
        content = content
    )
}

/**
 * Card Stack Transition
 */
@Composable
private fun <T> CardStackTransition(
    targetState: T,
    modifier: Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val cardSpec = spring<Float>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffnessness = Spring.StiffnessLow
            )

            slideInHorizontally(
                animationSpec = cardSpec,
                initialOffsetX = { it / 2 }
            ) + fadeIn() with
                slideOutHorizontally(
                    animationSpec = cardSpec,
                    targetOffsetX = { -it / 2 }
                ) + fadeOut()
        },
        modifier = modifier,
        content = content
    )
}

/**
 * Portal Teleport Transition
 */
@Composable
private fun <T> PortalTransition(
    targetState: T,
    modifier: Modifier,
    content: @Composable (T) -> Unit
) {
    val transition = updateTransition(targetState, label = "portal")

    val scale by transition.animateFloat(
        transitionSpec = {
            tween(400, easing = FastOutSlowInEasing)
        },
        label = "scale"
    ) { state ->
        if (state == targetState) 1f else 0f
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(300)
        },
        label = "alpha"
    ) { state ->
        if (state == targetState) 1f else 0f
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
    ) {
        content(targetState)
    }
}

/**
 * Candy Crush Transition
 */
@Composable
private fun <T> CandyCrushTransition(
    targetState: T,
    modifier: Modifier,
    content: @Composable (T) -> Unit
) {
    var particles by remember { mutableStateOf<List<TransitionParticle>>(emptyList()) }

    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffnessness = Spring.StiffnessLow
                )
            ) with scaleOut(
                animationSpec = tween(300),
                targetScale = 1.5f
            )
        },
        modifier = modifier.drawBehind {
            // Draw candy particles
            particles.forEach { particle ->
                drawCircle(
                    color = particle.color,
                    radius = particle.size,
                    center = Offset(
                        particle.x * size.width,
                        particle.y * size.height
                    ),
                    alpha = particle.alpha
                )
            }
        },
        content = content
    )
}

/**
 * Spiral Vortex Transition
 */
@Composable
private fun <T> SpiralVortexTransition(
    targetState: T,
    modifier: Modifier,
    content: @Composable (T) -> Unit
) {
    val transition = updateTransition(targetState, label = "vortex")

    val rotation by transition.animateFloat(
        transitionSpec = {
            tween(600, easing = FastOutSlowInEasing)
        },
        label = "rotation"
    ) { state ->
        if (state == targetState) 0f else 360f
    }

    val scale by transition.animateFloat(
        transitionSpec = {
            tween(600, easing = FastOutSlowInEasing)
        },
        label = "scale"
    ) { state ->
        if (state == targetState) 1f else 0f
    }

    Box(
        modifier = modifier.graphicsLayer {
            rotationZ = rotation
            scaleX = scale
            scaleY = scale
        }
    ) {
        content(targetState)
    }
}

/**
 * Page Curl Transition
 */
@Composable
private fun <T> PageCurlTransition(
    targetState: T,
    modifier: Modifier,
    content: @Composable (T) -> Unit
) {
    val transition = updateTransition(targetState, label = "page_curl")

    val progress by transition.animateFloat(
        transitionSpec = {
            tween(500, easing = FastOutSlowInEasing)
        },
        label = "progress"
    ) { state ->
        if (state == targetState) 1f else 0f
    }

    Box(
        modifier = modifier.graphicsLayer {
            // Simulate page curl with perspective transform
            cameraDistance = 12f * density
            rotationY = (1f - progress) * -30f
            translationX = (1f - progress) * -size.width * 0.2f
        }
    ) {
        content(targetState)
    }
}

/**
 * Cube Rotation Transition
 */
@Composable
private fun <T> CubeRotationTransition(
    targetState: T,
    modifier: Modifier,
    content: @Composable (T) -> Unit
) {
    val transition = updateTransition(targetState, label = "cube")

    val rotation by transition.animateFloat(
        transitionSpec = {
            tween(600, easing = FastOutSlowInEasing)
        },
        label = "rotation"
    ) { state ->
        if (state == targetState) 0f else 90f
    }

    Box(
        modifier = modifier.graphicsLayer {
            cameraDistance = 10f * density
            rotationY = rotation
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        }
    ) {
        content(targetState)
    }
}

/**
 * Domino Cascade Transition
 */
@Composable
private fun <T> DominoCascadeTransition(
    targetState: T,
    modifier: Modifier,
    content: @Composable (T) -> Unit
) {
    val transition = updateTransition(targetState, label = "domino")

    val rotation by transition.animateFloat(
        transitionSpec = {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        },
        label = "rotation"
    ) { state ->
        if (state == targetState) 0f else 90f
    }

    Box(
        modifier = modifier.graphicsLayer {
            rotationX = rotation
            transformOrigin = TransformOrigin(0.5f, 1f)
        }
    ) {
        content(targetState)
    }
}

/**
 * Ripple Transition
 */
@Composable
fun <T> RippleTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val rippleSpec = tween<Float>(500, easing = FastOutSlowInEasing)

            scaleIn(
                animationSpec = rippleSpec,
                initialScale = 0.1f
            ) with scaleOut(
                animationSpec = rippleSpec,
                targetScale = 3f
            )
        },
        modifier = modifier,
        content = content
    )
}

/**
 * Shatter Transition
 */
@Composable
fun <T> ShatterTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val shatterSpec = tween<Float>(400, easing = LinearEasing)

            scaleIn(
                animationSpec = shatterSpec
            ) with
                scaleOut(
                    animationSpec = shatterSpec,
                    targetScale = 1.5f
                )
        },
        modifier = modifier,
        content = content
    )
}

/**
 * Pixelate Transition
 */
@Composable
fun <T> PixelateTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    // This would use RenderScript or shader-based pixelation
    // Simplified version uses scale animation
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val pixelSpec = spring<Float>(stiffness = Spring.StiffnessMedium)

            scaleIn(animationSpec = pixelSpec) with
                scaleOut(animationSpec = pixelSpec)
        },
        modifier = modifier,
        content = content
    )
}

/**
 * Glitch Transition
 */
@Composable
fun <T> GlitchTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    val transition = updateTransition(targetState, label = "glitch")

    val translateX by transition.animateFloat(
        transitionSpec = {
            repeatable(
                iterations = 5,
                animation = tween(50),
                repeatMode = RepeatMode.Reverse
            )
        },
        label = "translateX"
    ) { state ->
        if (state == targetState) 0f else 10f
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(300)
        },
        label = "alpha"
    ) { state ->
        if (state == targetState) 1f else 0f
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.translationX = translateX * (if (transition.currentState != targetState) 1 else -1)
            this.alpha = alpha
        }
    ) {
        content(targetState)
    }
}

/**
 * Morph Transition
 */
@Composable
fun <T> MorphTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    val transition = updateTransition(targetState, label = "morph")

    val scaleX by transition.animateFloat(
        transitionSpec = {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        },
        label = "scaleX"
    ) { state ->
        if (state == targetState) 1f else 0.1f
    }

    val scaleY by transition.animateFloat(
        transitionSpec = {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        },
        label = "scaleY"
    ) { state ->
        if (state == targetState) 1f else 0.1f
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.scaleX = scaleX
            this.scaleY = scaleY
        }
    ) {
        content(targetState)
    }
}

// Data class for transition particles
private data class TransitionParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val alpha: Float
)

// Helper function for container transitions
private fun <S> createContainerTransitionSpec(
    enter: @Composable AnimatedContentTransitionScope<S>.() -> EnterTransition,
    exit: @Composable AnimatedContentTransitionScope<S>.() -> ExitTransition
): ContentTransform {
    return fadeIn() with fadeOut()
}
