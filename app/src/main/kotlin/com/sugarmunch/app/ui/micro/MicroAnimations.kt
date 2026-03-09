package com.sugarmunch.app.ui.micro

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ScrollState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// ─── Button Press Animations ─────────────────────────────────────────────────

enum class ButtonPressStyle {
    SPRING, RIPPLE_EXPAND, GLOW, SHAKE, MORPH, INK_SPREAD, NEON_PULSE, CANDY_UNWRAP
}

fun Modifier.animatedPress(
    style: ButtonPressStyle = ButtonPressStyle.SPRING,
    onClick: () -> Unit
): Modifier = composed {
    when (style) {
        ButtonPressStyle.SPRING -> springPress(onClick)
        ButtonPressStyle.RIPPLE_EXPAND -> rippleExpandPress(onClick)
        ButtonPressStyle.GLOW -> glowPress(onClick)
        ButtonPressStyle.SHAKE -> shakePress(onClick)
        ButtonPressStyle.MORPH -> morphPress(onClick)
        ButtonPressStyle.INK_SPREAD -> inkSpreadPress(onClick)
        ButtonPressStyle.NEON_PULSE -> neonPulsePress(onClick)
        ButtonPressStyle.CANDY_UNWRAP -> candyUnwrapPress(onClick)
    }
}

private fun Modifier.springPress(onClick: () -> Unit): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "spring_scale"
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                    onClick()
                }
            )
        }
}

private fun Modifier.rippleExpandPress(onClick: () -> Unit): Modifier = composed {
    var touchCenter by remember { mutableStateOf(Offset.Zero) }
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    this
        .drawBehind {
            if (rippleAlpha.value > 0f) {
                drawCircle(
                    color = Color(0xFF6200EE).copy(alpha = rippleAlpha.value * 0.3f),
                    radius = rippleRadius.value,
                    center = touchCenter
                )
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    touchCenter = offset
                    scope.launch {
                        rippleAlpha.snapTo(1f)
                        rippleRadius.snapTo(0f)
                        launch { rippleRadius.animateTo(size.maxDimension.toFloat(), tween(400)) }
                        launch { rippleAlpha.animateTo(0f, tween(500)) }
                    }
                    tryAwaitRelease()
                    onClick()
                }
            )
        }
}

private fun Modifier.glowPress(onClick: () -> Unit): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (pressed) 12.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "glow_elev"
    )
    this
        .shadow(elevation, RoundedCornerShape(12.dp), ambientColor = Color(0xFF6200EE), spotColor = Color(0xFF6200EE))
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                    onClick()
                }
            )
        }
}

private fun Modifier.shakePress(onClick: () -> Unit): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    this
        .graphicsLayer { translationX = offsetX.value }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    tryAwaitRelease()
                    scope.launch {
                        val shake = 12f
                        repeat(3) {
                            offsetX.animateTo(shake, tween(40, easing = LinearEasing))
                            offsetX.animateTo(-shake, tween(40, easing = LinearEasing))
                        }
                        offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    onClick()
                }
            )
        }
}

private fun Modifier.morphPress(onClick: () -> Unit): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val cornerPercent by animateFloatAsState(
        targetValue = if (pressed) 50f else 12f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "morph_corner"
    )
    this
        .clip(RoundedCornerShape(percent = cornerPercent.toInt()))
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                    onClick()
                }
            )
        }
}

private fun Modifier.inkSpreadPress(onClick: () -> Unit): Modifier = composed {
    var touchCenter by remember { mutableStateOf(Offset.Zero) }
    val clipRadius = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    this
        .drawWithContent {
            drawContent()
            if (clipRadius.value > 0f) {
                drawCircle(
                    color = Color(0xFF6200EE).copy(alpha = 0.2f),
                    radius = clipRadius.value,
                    center = touchCenter
                )
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    touchCenter = offset
                    scope.launch {
                        clipRadius.snapTo(0f)
                        clipRadius.animateTo(size.maxDimension.toFloat(), tween(350))
                        clipRadius.animateTo(0f, tween(200))
                    }
                    tryAwaitRelease()
                    onClick()
                }
            )
        }
}

private fun Modifier.neonPulsePress(onClick: () -> Unit): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val borderAlpha by animateFloatAsState(
        targetValue = if (pressed) 1f else 0.3f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "neon_alpha"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (pressed) 3.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "neon_width"
    )
    this
        .drawBehind {
            drawRoundRect(
                color = Color(0xFF00E5FF).copy(alpha = borderAlpha),
                cornerRadius = CornerRadius(12.dp.toPx()),
                style = Stroke(width = borderWidth.toPx())
            )
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                    onClick()
                }
            )
        }
}

private fun Modifier.candyUnwrapPress(onClick: () -> Unit): Modifier = composed {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    this
        .graphicsLayer { rotationZ = rotation.value }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    scope.launch {
                        rotation.animateTo(5f, tween(80))
                        rotation.animateTo(-5f, tween(80))
                        rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    tryAwaitRelease()
                    onClick()
                }
            )
        }
}

// ─── List Item Animations ────────────────────────────────────────────────────

@Composable
fun StaggeredAnimatedItem(
    index: Int,
    totalItems: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(200f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(index) {
        delay(index * 50L)
        launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) }
        launch { alpha.animateTo(1f, tween(300)) }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = offsetX.value
                this.alpha = alpha.value
            }
    ) {
        content()
    }
}

fun Modifier.swipeRevealActions(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    leftColor: Color = Color.Red,
    rightColor: Color = Color.Green
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 100.dp.toPx() }

    this
        .drawBehind {
            if (offsetX.value > 0f && onSwipeRight != null) {
                drawRect(
                    color = rightColor,
                    topLeft = Offset.Zero,
                    size = Size(offsetX.value, size.height)
                )
            } else if (offsetX.value < 0f && onSwipeLeft != null) {
                drawRect(
                    color = leftColor,
                    topLeft = Offset(size.width + offsetX.value, 0f),
                    size = Size(-offsetX.value, size.height)
                )
            }
        }
        .graphicsLayer { translationX = offsetX.value }
        .pointerInput(onSwipeLeft, onSwipeRight) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    scope.launch {
                        when {
                            offsetX.value > thresholdPx && onSwipeRight != null -> {
                                offsetX.animateTo(size.width.toFloat(), tween(200))
                                onSwipeRight()
                                offsetX.snapTo(0f)
                            }
                            offsetX.value < -thresholdPx && onSwipeLeft != null -> {
                                offsetX.animateTo(-size.width.toFloat(), tween(200))
                                onSwipeLeft()
                                offsetX.snapTo(0f)
                            }
                            else -> offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                        }
                    }
                },
                onDragCancel = { scope.launch { offsetX.animateTo(0f, spring()) } }
            ) { _, dragAmount ->
                scope.launch {
                    val newValue = offsetX.value + dragAmount
                    val clamped = when {
                        newValue > 0f && onSwipeRight == null -> 0f
                        newValue < 0f && onSwipeLeft == null -> 0f
                        else -> newValue
                    }
                    offsetX.snapTo(clamped)
                }
            }
        }
}

// ─── Toggle Animations ───────────────────────────────────────────────────────

enum class ToggleStyle { CANDY_TOGGLE, LIQUID_FILL, BOUNCE_BALL, NEON_FLICK }

@Composable
fun AnimatedToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    style: ToggleStyle = ToggleStyle.CANDY_TOGGLE,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val thumbPosition by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = when (style) {
            ToggleStyle.BOUNCE_BALL -> spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            ToggleStyle.NEON_FLICK -> spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
            else -> spring(stiffness = Spring.StiffnessMedium)
        },
        label = "toggle_pos"
    )
    val trackColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (checked) activeColor else Color.Gray.copy(alpha = 0.4f),
        animationSpec = tween(250),
        label = "track_color"
    )

    val bounceOffset: Float
    if (style == ToggleStyle.BOUNCE_BALL) {
        val bounceAnim = remember { Animatable(0f) }
        LaunchedEffect(checked) {
            bounceAnim.snapTo(0f)
            bounceAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                initialVelocity = -600f
            )
        }
        bounceOffset = bounceAnim.value
    } else {
        bounceOffset = 0f
    }

    Canvas(
        modifier = modifier
            .size(width = 52.dp, height = 28.dp)
            .pointerInput(Unit) { detectTapGestures { onCheckedChange(!checked) } }
    ) {
        val trackHeight = size.height
        val trackWidth = size.width
        val thumbRadius = trackHeight / 2f - 4.dp.toPx()
        val thumbTravel = trackWidth - trackHeight
        val thumbCx = trackHeight / 2f + thumbTravel * thumbPosition
        val thumbCy = trackHeight / 2f

        when (style) {
            ToggleStyle.CANDY_TOGGLE -> {
                drawRoundRect(
                    color = trackColor,
                    cornerRadius = CornerRadius(trackHeight / 2f),
                    size = Size(trackWidth, trackHeight)
                )
                drawCircle(color = Color.White, radius = thumbRadius, center = Offset(thumbCx, thumbCy))
                if (checked) {
                    val stripeCount = 3
                    for (i in 0 until stripeCount) {
                        val angle = i * 60f
                        val dx = thumbRadius * 0.5f * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
                        val dy = thumbRadius * 0.5f * sin(Math.toRadians(angle.toDouble())).toFloat()
                        drawLine(
                            color = activeColor.copy(alpha = 0.5f),
                            start = Offset(thumbCx - dx, thumbCy - dy),
                            end = Offset(thumbCx + dx, thumbCy + dy),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            ToggleStyle.LIQUID_FILL -> {
                drawRoundRect(
                    color = Color.Gray.copy(alpha = 0.3f),
                    cornerRadius = CornerRadius(trackHeight / 2f),
                    size = Size(trackWidth, trackHeight)
                )
                val fillWidth = thumbCx + thumbRadius
                clipRect(right = fillWidth) {
                    drawRoundRect(
                        color = activeColor,
                        cornerRadius = CornerRadius(trackHeight / 2f),
                        size = Size(trackWidth, trackHeight)
                    )
                }
                val waveCy = thumbCy + sin(thumbPosition.toDouble() * Math.PI * 2).toFloat() * 2.dp.toPx()
                drawCircle(color = Color.White, radius = thumbRadius, center = Offset(thumbCx, waveCy))
            }

            ToggleStyle.BOUNCE_BALL -> {
                drawRoundRect(
                    color = trackColor,
                    cornerRadius = CornerRadius(trackHeight / 2f),
                    size = Size(trackWidth, trackHeight)
                )
                val adjustedCy = (thumbCy + bounceOffset).coerceIn(thumbRadius + 2.dp.toPx(), trackHeight - thumbRadius - 2.dp.toPx())
                drawCircle(color = Color.White, radius = thumbRadius, center = Offset(thumbCx, adjustedCy))
            }

            ToggleStyle.NEON_FLICK -> {
                val glowAlpha = 0.2f + 0.6f * thumbPosition
                drawRoundRect(
                    color = Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.3f),
                    cornerRadius = CornerRadius(trackHeight / 2f),
                    size = Size(trackWidth + 4.dp.toPx(), trackHeight + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
                drawRoundRect(
                    color = Color.DarkGray,
                    cornerRadius = CornerRadius(trackHeight / 2f),
                    size = Size(trackWidth, trackHeight)
                )
                drawRoundRect(
                    color = Color(0xFF00E5FF).copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(trackHeight / 2f),
                    size = Size(trackWidth, trackHeight),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.3f + 0.7f * thumbPosition),
                    radius = thumbRadius + 3.dp.toPx(),
                    center = Offset(thumbCx, thumbCy)
                )
                drawCircle(color = Color.White, radius = thumbRadius, center = Offset(thumbCx, thumbCy))
            }
        }
    }
}

// ─── Scroll Effects ──────────────────────────────────────────────────────────

fun Modifier.overscrollStretch(): Modifier = composed {
    var overscrollY by remember { mutableFloatStateOf(0f) }
    val scale by animateFloatAsState(
        targetValue = 1f + (overscrollY.coerceIn(0f, 1f) * 0.02f),
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "overscroll_scale"
    )
    this
        .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = { overscrollY = 0f },
                onDragCancel = { overscrollY = 0f }
            ) { _, dragAmount ->
                if (dragAmount.y > 0) {
                    overscrollY = (overscrollY + dragAmount.y / size.height).coerceIn(0f, 1f)
                } else {
                    overscrollY = 0f
                }
            }
        }
        .graphicsLayer { scaleY = scale }
}

fun Modifier.parallaxScroll(scrollState: ScrollState, rate: Float = 0.5f): Modifier = composed {
    this.graphicsLayer {
        translationY = scrollState.value * rate
    }
}

// ─── Number Animations ───────────────────────────────────────────────────────

@Composable
fun AnimatedCounter(
    value: Int,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    val digits = remember(value) { value.toString().toList() }

    Row(modifier = modifier) {
        digits.forEachIndexed { index, digit ->
            AnimatedContent(
                targetState = digit,
                transitionSpec = {
                    val direction = if (targetState > initialState) -1 else 1
                    slideInVertically { height -> direction * height } togetherWith
                        slideOutVertically { height -> -direction * height } using
                        SizeTransform(clip = false)
                },
                label = "digit_$index"
            ) { target ->
                Text(
                    text = target.toString(),
                    style = style,
                    color = color
                )
            }
        }
    }
}

@Composable
fun SlotMachineNumber(
    value: Int,
    modifier: Modifier = Modifier
) {
    val style = MaterialTheme.typography.headlineMedium
    val color = MaterialTheme.colorScheme.onSurface
    val targetDigits = remember(value) { value.toString().map { it.digitToInt() } }

    Row(modifier = modifier) {
        targetDigits.forEachIndexed { index, targetDigit ->
            val displayDigit = remember { mutableIntStateOf(0) }
            val alpha = remember { Animatable(1f) }
            val offsetY = remember { Animatable(0f) }

            LaunchedEffect(targetDigit, index) {
                val spinDuration = 80L
                val spinCount = 8 + index * 4
                for (i in 0 until spinCount) {
                    offsetY.snapTo(-20f)
                    displayDigit.intValue = (displayDigit.intValue + 1) % 10
                    offsetY.animateTo(0f, tween((spinDuration).toInt(), easing = LinearEasing))
                }
                displayDigit.intValue = targetDigit
                offsetY.snapTo(-20f)
                offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }

            Text(
                text = displayDigit.intValue.toString(),
                style = style,
                color = color,
                modifier = Modifier.graphicsLayer {
                    translationY = offsetY.value
                    this.alpha = alpha.value
                }
            )
        }
    }
}
