package com.sugarmunch.app.ui.effects.advanced

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.model.BaseColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * PHASE 11: ADVANCED VISUAL EFFECTS
 * 2026-Level Touch Animations & Visual Effects
 */

/**
 * Advanced Touch Ripple Effect
 * Creates beautiful ripples that adapt to current theme
 */
@Composable
fun AdvancedTouchRipple(
    modifier: Modifier = Modifier,
    colors: BaseColors,
    rippleCount: Int = 3,
    content: @Composable () -> Unit
) {
    var ripples by remember { mutableStateOf(listOf<RippleState>()) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.pointerInput(Unit) {
        detectTapGestures(
            onTap = { offset ->
                scope.launch {
                    val newRipples = List(rippleCount) { i ->
                        RippleState(
                            offset = offset,
                            color = colors.primary.copy(alpha = 0.3f - (i * 0.05f)),
                            radius = 0f,
                            targetRadius = 100f + (i * 50f),
                            opacity = 1f,
                            delay = i * 100
                        )
                    }
                    ripples = ripples + newRipples
                }
            },
            onDoubleTap = { offset ->
                scope.launch {
                    // Double tap creates bigger ripple
                    val newRipple = RippleState(
                        offset = offset,
                        color = colors.secondary.copy(alpha = 0.5f),
                        radius = 0f,
                        targetRadius = 200f,
                        opacity = 1f,
                        delay = 0
                    )
                    ripples = ripples + listOf(newRipple)
                }
            }
        )
    }) {
        content()

        // Draw ripples
        ripples.forEach { ripple ->
            AnimatedRipple(ripple = ripple, onComplete = {
                ripples = ripples.filter { it != ripple }
            })
        }
    }
}

@Composable
private fun AnimatedRipple(
    ripple: RippleState,
    onComplete: () -> Unit
) {
    val radius = remember { Animatable(0f) }
    val opacity = remember { Animatable(1f) }

    LaunchedEffect(ripple) {
        launch {
            delay(ripple.delay)
            radius.animateTo(
                targetValue = ripple.targetRadius,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            delay(ripple.delay + 300)
            opacity.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300)
            )
        }
        awaitCompletion()
        onComplete()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ripple.color.copy(alpha = opacity.value),
                    ripple.color.copy(alpha = 0f)
                ),
                center = ripple.offset,
                radius = radius.value
            ),
            radius = radius.value,
            center = ripple.offset
        )
    }
}

/**
 * Drag Trail Effect
 * Creates particle trail when dragging
 */
@Composable
fun DragTrailEffect(
    modifier: Modifier = Modifier,
    colors: BaseColors,
    particleCount: Int = 5,
    content: @Composable () -> Unit
) {
    var trailPoints by remember { mutableStateOf(listOf<TrailPoint>()) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDrag = { change, dragAmount ->
                change.consume()
                val point = TrailPoint(
                    offset = change.position,
                    color = colors.tertiary,
                    size = 20f,
                    opacity = 1f
                )
                trailPoints = trailPoints + point
                
                // Auto-remove old points
                if (trailPoints.size > 50) {
                    trailPoints = trailPoints.drop(10)
                }
            },
            onDragEnd = {
                scope.launch {
                    // Fade out remaining points
                    kotlinx.coroutines.delay(500)
                    trailPoints = emptyList()
                }
            }
        )
    }) {
        content()

        // Draw trail
        trailPoints.forEach { point ->
            AnimatedTrailPoint(point = point)
        }
    }
}

@Composable
private fun AnimatedTrailPoint(point: TrailPoint) {
    val size = remember { Animatable(point.size) }
    val opacity = remember { Animatable(point.opacity) }

    LaunchedEffect(point) {
        launch {
            size.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400)
            )
        }
        launch {
            opacity.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400)
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = point.color.copy(alpha = opacity.value),
            radius = size.value,
            center = point.offset
        )
    }
}

/**
 * Gesture-Based Effects
 * Swipe, pinch, rotate gestures trigger visual effects
 */
@Composable
fun GestureVisualEffects(
    modifier: Modifier = Modifier,
    colors: BaseColors,
    onSwipeUp: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onDoubleTap: (Offset) -> Unit = {},
    onLongPress: (Offset) -> Unit = {},
    content: @Composable () -> Unit
) {
    var effectState by remember { mutableStateOf<GestureEffect?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = { offset ->
                effectState = BurstEffect(offset, colors)
                onDoubleTap(offset)
                scope.launch {
                    kotlinx.coroutines.delay(1000)
                    effectState = null
                }
            },
            onLongPress = { offset ->
                effectState = PulseEffect(offset, colors)
                onLongPress(offset)
            },
            onPress = { offset ->
                // Press effect
                effectState = PressEffect(offset, colors)
            }
        )
    }) {
        content()

        effectState?.let { effect ->
            when (effect) {
                is BurstEffect -> AnimatedBurstEffect(effect)
                is PulseEffect -> AnimatedPulseEffect(effect)
                is PressEffect -> AnimatedPressEffect(effect)
            }
        }
    }
}

/**
 * Screen Edge Glow Effect
 * Glowing edges when scrolling near boundaries
 */
@Composable
fun ScreenEdgeGlow(
    modifier: Modifier = Modifier,
    colors: BaseColors,
    scrollOffset: Float,
    maxScroll: Float,
    content: @Composable () -> Unit
) {
    val topGlowAlpha = remember { Animatable(0f) }
    val bottomGlowAlpha = remember { Animatable(0f) }

    LaunchedEffect(scrollOffset, maxScroll) {
        // Top glow when scrolled to top
        if (scrollOffset < 100) {
            topGlowAlpha.animateTo(
                targetValue = 1f - (scrollOffset / 100),
                animationSpec = tween(150)
            )
        } else {
            topGlowAlpha.animateTo(0f, animationSpec = tween(150))
        }

        // Bottom glow when scrolled to bottom
        val distanceFromBottom = maxScroll - scrollOffset
        if (distanceFromBottom < 100) {
            bottomGlowAlpha.animateTo(
                targetValue = 1f - (distanceFromBottom / 100),
                animationSpec = tween(150)
            )
        } else {
            bottomGlowAlpha.animateTo(0f, animationSpec = tween(150))
        }
    }

    Box(modifier = modifier) {
        content()

        // Top glow
        if (topGlowAlpha.value > 0.01f) {
            Canvas(modifier = Modifier.fillMaxWidth().height(50.dp)) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = topGlowAlpha.value * 0.3f),
                            Color.Transparent
                        )
                    ),
                    size = size
                )
            }
        }

        // Bottom glow
        if (bottomGlowAlpha.value > 0.01f) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .align(Alignment.BottomCenter)) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            colors.secondary.copy(alpha = bottomGlowAlpha.value * 0.3f)
                        )
                    ),
                    size = size
                )
            }
        }
    }
}

/**
 * Icon Bounce Animation on Tap
 */
@Composable
fun BouncyIcon(
    icon: String,
    colors: BaseColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .size(80.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        scope.launch {
                            // Squash down
                            scale.animateTo(
                                targetValue = 0.8f,
                                animationSpec = tween(100, easing = FastOutSlowInEasing)
                            )
                            // Bounce up with overshoot
                            scale.animateTo(
                                targetValue = 1.1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                            // Settle
                            scale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                        onClick()
                    }
                )
            }
    ) {
        Text(
            text = icon,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 40.sp
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                }
        )
    }
}

/**
 * Loading Spinner with Theme Colors
 */
@Composable
fun ThemedLoadingSpinner(
    colors: BaseColors,
    modifier: Modifier = Modifier,
    size: Float = 1f
) {
    val rotation = remember { Animatable(0f) }
    val sweep = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            sweep.animateTo(
                targetValue = 270f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
            sweep.animateTo(
                targetValue = 0f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        }
    }

    Canvas(modifier = modifier.size(48.dp * size)) {
        rotate(rotation.value) {
            drawArc(
                color = colors.primary,
                startAngle = 0f,
                sweepAngle = sweep.value,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = colors.secondary,
                startAngle = 180f,
                sweepAngle = sweep.value,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

// Data classes
data class RippleState(
    val offset: Offset,
    val color: Color,
    val radius: Float,
    val targetRadius: Float,
    val opacity: Float,
    val delay: Long
)

data class TrailPoint(
    val offset: Offset,
    val color: Color,
    val size: Float,
    val opacity: Float
)

sealed class GestureEffect
data class BurstEffect(val offset: Offset, val colors: BaseColors) : GestureEffect()
data class PulseEffect(val offset: Offset, val colors: BaseColors) : GestureEffect()
data class PressEffect(val offset: Offset, val colors: BaseColors) : GestureEffect()

@Composable
fun AnimatedBurstEffect(effect: BurstEffect) {
    val radius = remember { Animatable(0f) }
    val particleCount = 12

    LaunchedEffect(Unit) {
        radius.animateTo(
            targetValue = 150f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(particleCount) { i ->
            val angle = (i * 360f / particleCount) + radius.value
            val x = effect.offset.x + kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * radius.value * 0.5f
            val y = effect.offset.y + kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * radius.value * 0.5f
            
            drawCircle(
                color = effect.colors.primary.copy(alpha = 1f - (radius.value / 150f)),
                radius = 8f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun AnimatedPulseEffect(effect: PulseEffect) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        repeat(3) { i ->
            scale.animateTo(
                targetValue = 1.5f,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = effect.colors.secondary.copy(alpha = 0.3f),
            radius = 50f * scale.value,
            center = effect.offset
        )
    }
}

@Composable
fun AnimatedPressEffect(effect: PressEffect) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 0.95f,
            animationSpec = tween(100)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = effect.colors.tertiary.copy(alpha = 0.2f),
            radius = 40f * scale.value,
            center = effect.offset
        )
    }
}
