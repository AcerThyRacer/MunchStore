package com.sugarmunch.app.ui.icons

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlin.math.*

// ---------------------------------------------------------------------------
// 1. AnimatedDownloadIcon
// ---------------------------------------------------------------------------

@Composable
fun AnimatedDownloadIcon(
    progress: Float,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.08f
        val clampedProgress = progress.coerceIn(0f, 1f)

        // Arrow shaft – slides downward with progress
        val arrowTopY = h * 0.15f + clampedProgress * h * 0.2f
        val arrowBottomY = h * 0.55f + clampedProgress * h * 0.15f
        val centerX = w / 2f

        drawLine(
            color = color,
            start = Offset(centerX, arrowTopY),
            end = Offset(centerX, arrowBottomY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // Arrowhead
        val headSize = w * 0.18f
        val arrowPath = Path().apply {
            moveTo(centerX - headSize, arrowBottomY - headSize * 0.6f)
            lineTo(centerX, arrowBottomY + headSize * 0.3f)
            lineTo(centerX + headSize, arrowBottomY - headSize * 0.6f)
        }
        drawPath(
            path = arrowPath,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Tray at the bottom – fills horizontally with progress
        val trayY = h * 0.85f
        val trayHalfWidth = w * 0.35f
        val trayLeft = centerX - trayHalfWidth
        val trayRight = centerX + trayHalfWidth
        val trayLegHeight = h * 0.1f

        // Left leg
        drawLine(
            color = color,
            start = Offset(trayLeft, trayY - trayLegHeight),
            end = Offset(trayLeft, trayY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Right leg
        drawLine(
            color = color,
            start = Offset(trayRight, trayY - trayLegHeight),
            end = Offset(trayRight, trayY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Bottom shelf
        drawLine(
            color = color,
            start = Offset(trayLeft, trayY),
            end = Offset(trayRight, trayY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // Fill bar inside tray based on progress
        if (clampedProgress > 0f) {
            val fillPadding = stroke
            val fillWidth = (trayRight - trayLeft - fillPadding * 2) * clampedProgress
            val fillHeight = trayLegHeight - fillPadding
            drawRect(
                color = color.copy(alpha = 0.3f),
                topLeft = Offset(trayLeft + fillPadding, trayY - fillHeight),
                size = Size(fillWidth, fillHeight)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 2. AnimatedSuccessIcon
// ---------------------------------------------------------------------------

@Composable
fun AnimatedSuccessIcon(
    visible: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            animatable.snapTo(0f)
            animatable.animateTo(1f, tween(600))
        } else {
            animatable.snapTo(0f)
        }
    }

    val fraction = animatable.value

    Canvas(modifier = modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val radius = minOf(w, h) / 2f * 0.9f

        // Circle background
        drawCircle(
            color = color.copy(alpha = 0.15f * fraction),
            radius = radius,
            center = center
        )
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = w * 0.06f)
        )

        // Checkmark path
        if (fraction > 0f) {
            val checkPath = Path().apply {
                moveTo(w * 0.28f, h * 0.50f)
                lineTo(w * 0.42f, h * 0.64f)
                lineTo(w * 0.72f, h * 0.36f)
            }
            // Total approximate path length for dash effect
            val totalLength = w * 0.60f
            val drawnLength = totalLength * fraction
            drawPath(
                path = checkPath,
                color = color,
                style = Stroke(
                    width = w * 0.07f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(drawnLength, totalLength),
                        0f
                    )
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 3. AnimatedErrorIcon
// ---------------------------------------------------------------------------

@Composable
fun AnimatedErrorIcon(
    visible: Boolean,
    color: Color = MaterialTheme.colorScheme.error,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }
    val drawAlpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            drawAlpha.snapTo(0f)
            drawAlpha.animateTo(1f, tween(200))
            // Decaying shake oscillation
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    12f at 50
                    -10f at 100
                    8f at 150
                    -6f at 200
                    4f at 250
                    -2f at 300
                    0f at 400
                }
            )
        } else {
            drawAlpha.snapTo(0f)
            shakeOffset.snapTo(0f)
        }
    }

    val offsetX = shakeOffset.value
    val alpha = drawAlpha.value

    Canvas(modifier = modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val radius = minOf(w, h) / 2f * 0.9f
        val stroke = w * 0.07f

        translate(left = offsetX) {
            // Circle background
            drawCircle(
                color = color.copy(alpha = 0.15f * alpha),
                radius = radius,
                center = center
            )
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = center,
                style = Stroke(width = w * 0.06f)
            )

            // X lines
            val pad = w * 0.30f
            drawLine(
                color = color.copy(alpha = alpha),
                start = Offset(pad, pad),
                end = Offset(w - pad, h - pad),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color.copy(alpha = alpha),
                start = Offset(w - pad, pad),
                end = Offset(pad, h - pad),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4. AnimatedLoadingIcon
// ---------------------------------------------------------------------------

@Composable
fun AnimatedLoadingIcon(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation"
    )

    Canvas(modifier = modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val orbitRadius = minOf(w, h) * 0.35f
        val dotRadius = w * 0.05f
        val dotCount = 8

        for (i in 0 until dotCount) {
            val dotAngleDeg = i * (360f / dotCount)
            val dotAngleRad = Math.toRadians(dotAngleDeg.toDouble())
            val x = cx + orbitRadius * cos(dotAngleRad).toFloat()
            val y = cy + orbitRadius * sin(dotAngleRad).toFloat()

            // Angular distance from the "active" front of the spinner
            var diff = (dotAngleDeg - angle + 360f) % 360f
            if (diff > 180f) diff = 360f - diff
            val alpha = (1f - diff / 180f).coerceIn(0.15f, 1f)

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = dotRadius,
                center = Offset(x, y)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 5. AnimatedToggleIcon
// ---------------------------------------------------------------------------

@Composable
fun AnimatedToggleIcon(
    isOn: Boolean,
    onColor: Color = MaterialTheme.colorScheme.primary,
    offColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isOn) 1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "toggle_scale"
    )

    val bounceScale = remember { Animatable(1f) }
    LaunchedEffect(isOn) {
        bounceScale.snapTo(0.8f)
        bounceScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    val currentColor by animateColorAsState(
        targetValue = if (isOn) onColor else offColor,
        animationSpec = tween(300),
        label = "toggle_color"
    )

    val checkAlpha by animateFloatAsState(
        targetValue = if (isOn) 1f else 0f,
        animationSpec = tween(300),
        label = "toggle_check_alpha"
    )

    Canvas(modifier = modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val radius = minOf(w, h) / 2f * 0.8f * bounceScale.value

        // Filled circle when on, outlined when off
        val fillAlpha = checkAlpha
        drawCircle(
            color = currentColor.copy(alpha = 0.15f + 0.85f * fillAlpha),
            radius = radius,
            center = center
        )
        drawCircle(
            color = currentColor,
            radius = radius,
            center = center,
            style = Stroke(width = w * 0.06f)
        )

        // Checkmark
        if (checkAlpha > 0f) {
            val checkPath = Path().apply {
                moveTo(w * 0.30f, h * 0.50f)
                lineTo(w * 0.44f, h * 0.64f)
                lineTo(w * 0.70f, h * 0.36f)
            }
            drawPath(
                path = checkPath,
                color = Color.White.copy(alpha = checkAlpha),
                style = Stroke(
                    width = w * 0.08f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 6. AnimatedStarIcon
// ---------------------------------------------------------------------------

@Composable
fun AnimatedStarIcon(
    filled: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val fillFraction by animateFloatAsState(
        targetValue = if (filled) 1f else 0f,
        animationSpec = tween(300),
        label = "star_fill"
    )

    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(filled) {
        scaleAnim.snapTo(if (filled) 1.3f else 0.8f)
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Canvas(modifier = modifier.size(48.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val outerRadius = minOf(w, h) * 0.42f * scaleAnim.value
        val innerRadius = outerRadius * 0.4f
        val points = 5

        val starPath = Path().apply {
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val angleDeg = -90f + i * (360f / (points * 2))
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val x = cx + r * cos(angleRad).toFloat()
                val y = cy + r * sin(angleRad).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        // Filled portion
        if (fillFraction > 0f) {
            drawPath(
                path = starPath,
                color = color.copy(alpha = fillFraction),
                style = Fill
            )
        }

        // Outline
        drawPath(
            path = starPath,
            color = color,
            style = Stroke(
                width = w * 0.04f,
                join = StrokeJoin.Round
            )
        )
    }
}

// ---------------------------------------------------------------------------
// 7. AnimatedSparkle
// ---------------------------------------------------------------------------

@Composable
fun AnimatedSparkle(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")

    // Four sparkles with staggered phase offsets
    data class SparkleConfig(
        val cx: Float, val cy: Float,
        val maxSize: Float, val delayMillis: Int
    )

    val sparkles = remember {
        listOf(
            SparkleConfig(0.25f, 0.30f, 0.18f, 0),
            SparkleConfig(0.70f, 0.20f, 0.12f, 250),
            SparkleConfig(0.55f, 0.65f, 0.15f, 500),
            SparkleConfig(0.20f, 0.75f, 0.10f, 750)
        )
    }

    val animValues = sparkles.map { config ->
        val scale by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1600
                    0f at 0
                    1f at 400
                    0f at 800
                    0f at 1600
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(config.delayMillis)
            ),
            label = "sparkle_scale_${config.delayMillis}"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1600
                    0f at 0
                    1f at 300
                    0.8f at 500
                    0f at 800
                    0f at 1600
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(config.delayMillis)
            ),
            label = "sparkle_alpha_${config.delayMillis}"
        )
        scale to alpha
    }

    Canvas(modifier = modifier.size(48.dp)) {
        val w = size.width
        val h = size.height

        sparkles.forEachIndexed { index, config ->
            val (scale, alpha) = animValues[index]
            if (alpha > 0.01f) {
                val cx = w * config.cx
                val cy = h * config.cy
                val armLength = w * config.maxSize * scale

                // 4-point star sparkle shape
                val sparklePath = Path().apply {
                    moveTo(cx, cy - armLength)
                    lineTo(cx + armLength * 0.25f, cy - armLength * 0.25f)
                    lineTo(cx + armLength, cy)
                    lineTo(cx + armLength * 0.25f, cy + armLength * 0.25f)
                    lineTo(cx, cy + armLength)
                    lineTo(cx - armLength * 0.25f, cy + armLength * 0.25f)
                    lineTo(cx - armLength, cy)
                    lineTo(cx - armLength * 0.25f, cy - armLength * 0.25f)
                    close()
                }

                drawPath(
                    path = sparklePath,
                    color = color.copy(alpha = alpha),
                    style = Fill
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 8. PulsingIcon
// ---------------------------------------------------------------------------

@Composable
fun PulsingIcon(
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(modifier = modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = size.minDimension / 2 * scale
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}
