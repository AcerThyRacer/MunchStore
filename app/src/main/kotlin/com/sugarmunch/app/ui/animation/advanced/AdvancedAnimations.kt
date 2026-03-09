package com.sugarmunch.app.ui.animation.advanced

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.sugarmunch.app.theme.model.BaseColors
import kotlinx.coroutines.delay

/**
 * PHASE 12: ADVANCED ANIMATIONS
 * 2026-Level Animation System
 */

/**
 * Shared Element Transition
 * Smooth transitions between screens
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SharedElementTransition(
    targetState: Any,
    content: @Composable (Any) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) +
                fadeIn(animationSpec = tween(300)) togetherWith
            slideOutHorizontally(targetOffsetX = { -it }) +
                fadeOut(animationSpec = tween(300))
        },
        label = "shared_element"
    ) { target ->
        content(target)
    }
}

/**
 * Staggered List Animation
 * Items animate in sequence with delay
 */
@Composable
fun <T> StaggeredListAnimation(
    items: List<T>,
    key: (T) -> Any,
    content: @Composable (T) -> Unit
) {
    items.forEachIndexed { index, item ->
        val visible = remember { Animatable(0f) }

        LaunchedEffect(items) {
            delay(index * 50L) // Stagger delay
            visible.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    alpha = visible.value
                    translationY = (1f - visible.value) * 50f
                    scaleY = 0.8f + (visible.value * 0.2f)
                }
        ) {
            content(item)
        }
    }
}

/**
 * Page Turn Animation
 * 3D page flip effect
 */
@Composable
fun PageTurnAnimation(
    currentPage: Int,
    totalPages: Int,
    colors: BaseColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val rotationY = remember { Animatable(0f) }

    LaunchedEffect(currentPage) {
        // Animate page turn
        rotationY.animateTo(
            targetValue = 180f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
        rotationY.snapTo(0f)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotationY.value
                cameraDistance = 12f
            }
            .drawWithContent {
                if (rotationY.value < 90f) {
                    drawContent()
                }
            }
    ) {
        content()
    }
}

/**
 * Success Checkmark Animation
 * Animated checkmark for success states
 */
@Composable
fun SuccessCheckmarkAnimation(
    colors: BaseColors,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    val pathProgress = remember { Animatable(0f) }
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        pathProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
        delay(1000)
        onComplete()
    }

    Box(
        modifier = modifier
            .size(80.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    ) {
        // Circle background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = colors.primary.copy(alpha = 0.2f),
                radius = size.minDimension / 2
            )
        }

        // Checkmark
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.5f)
                lineTo(size.width * 0.45f, size.height * 0.75f)
                lineTo(size.width * 0.8f, size.height * 0.25f)
            }

            drawPath(
                path = path,
                color = colors.primary,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 4.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                ),
                pathMeasure = {
                    it.asPathMeasure().let { measure ->
                        val length = measure.length
                        measure.getSegment(0f, length * pathProgress.value)
                    }
                }
            )
        }
    }
}

/**
 * Error Shake Animation
 * Shake effect for error states
 */
@Composable
fun ErrorShakeAnimation(
    modifier: Modifier = Modifier,
    colors: BaseColors,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        repeat(5) { i ->
            offsetX.animateTo(
                targetValue = if (i % 2 == 0) 10f else -10f,
                animationSpec = tween(100, easing = FastOutSlowInEasing)
            )
        }
        offsetX.animateTo(0f, animationSpec = tween(100))
    }

    Box(
        modifier = modifier.graphicsLayer {
            translationX = offsetX.value
        }
    ) {
        content()
    }
}

/**
 * Pulse Glow Effect
 * Pulsing glow around content
 */
@Composable
fun PulseGlowEffect(
    colors: BaseColors,
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    content: @Composable () -> Unit
) {
    val glowAlpha = remember { Animatable(0.3f) }
    val glowScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            glowAlpha.animateTo(
                targetValue = 0.6f * intensity,
                animationSpec = tween(800, easing = LinearEasing)
            )
            glowAlpha.animateTo(
                targetValue = 0.3f * intensity,
                animationSpec = tween(800, easing = LinearEasing)
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            glowScale.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(800, easing = LinearEasing)
            )
            glowScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = LinearEasing)
            )
        }
    }

    Box(modifier = modifier) {
        // Glow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = glowAlpha.value
                    scaleX = glowScale.value
                    scaleY = glowScale.value
                }
                .drawWithContent {
                    drawCircle(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                colors.primary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )

        // Content
        content()
    }
}

/**
 * Shimmer Loading Effect
 * Skeleton loading with shimmer
 */
@Composable
fun ShimmerLoadingEffect(
    colors: BaseColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shimmerOffset = remember { Animatable(-1f) }

    LaunchedEffect(Unit) {
        shimmerOffset.animateTo(
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Box(
        modifier = modifier.drawWithContent {
            // Draw content
            drawContent()

            // Draw shimmer overlay
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        colors.onBackground.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    startX = shimmerOffset.value * size.width,
                    endX = (shimmerOffset.value + 0.5f) * size.width
                )
            )
        }
    ) {
        content()
    }
}

/**
 * Number Counter Animation
 * Animated counting up/down
 */
@Composable
fun AnimatedNumberCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    colors: BaseColors,
    durationMillis: Int = 1000
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            )
        )
    }

    androidx.compose.material3.Text(
        text = animatedValue.value.toInt().toString(),
        color = colors.onBackground
    )
}

/**
 * Progress Bar Animation
 * Smooth animated progress
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    colors: BaseColors,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(colors.background, RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress.value)
                .height(8.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(colors.primary, colors.secondary)
                    ),
                    RoundedCornerShape(4.dp)
                )
        )
    }
}

/**
 * Fade Through Animation
 * Fade out then fade in with scale
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> FadeThroughAnimation(
    targetState: T,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) +
                scaleIn(initialScale = 0.92f, animationSpec = tween(300)) togetherWith
            fadeOut(animationSpec = tween(300)) +
                scaleOut(targetScale = 1.05f, animationSpec = tween(300))
        },
        label = "fade_through"
    ) { target ->
        content(target)
    }
}

/**
 * Slide Up Animation
 * Content slides up with fade
 */
@Composable
fun SlideUpAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val offsetY = remember { Animatable(if (!visible) 50f else 0f) }
    val alpha = remember { Animatable(if (!visible) 0f else 1f) }

    LaunchedEffect(visible) {
        if (visible) {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            alpha.animateTo(1f, animationSpec = tween(300))
        } else {
            offsetY.animateTo(50f, animationSpec = tween(300))
            alpha.animateTo(0f, animationSpec = tween(300))
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            translationY = offsetY.value
            this.alpha = alpha.value
        }
    ) {
        if (visible) content()
    }
}

/**
 * Rotation Animation
 * Continuous or one-time rotation
 */
@Composable
fun RotationAnimation(
    modifier: Modifier = Modifier,
    rotations: Int = 1,
    durationMillis: Int = 1000,
    infinite: Boolean = false,
    content: @Composable () -> Unit
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (infinite) {
            rotation.animateTo(
                targetValue = 360f * rotations,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotation.animateTo(
                targetValue = 360f * rotations,
                animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            rotationZ = rotation.value
        }
    ) {
        content()
    }
}
