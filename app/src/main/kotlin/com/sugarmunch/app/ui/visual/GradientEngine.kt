package com.sugarmunch.app.ui.visual

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

enum class GradientType {
    LINEAR,
    RADIAL,
    SWEEP,
    DIAMOND,
    CONIC
}

data class GradientStop(
    val color: Color,
    val position: Float
)

data class GradientSpec(
    val type: GradientType,
    val stops: List<GradientStop>,
    val angleDegrees: Float = 0f,
    val centerX: Float = 0.5f,
    val centerY: Float = 0.5f,
    val radiusScale: Float = 1f
)

fun GradientSpec.toBrush(size: Size): Brush {
    if (stops.isEmpty()) return Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))

    val colors = stops.sortedBy { it.position }.map { it.color }
    val positions = stops.sortedBy { it.position }.map { it.position }

    return when (type) {
        GradientType.LINEAR -> {
            val angleRad = angleDegrees * (PI / 180.0).toFloat()
            val diagonal = sqrt(size.width * size.width + size.height * size.height)
            val centerXPx = size.width / 2f
            val centerYPx = size.height / 2f
            val halfProj = diagonal / 2f

            val startX = centerXPx - cos(angleRad) * halfProj
            val startY = centerYPx - sin(angleRad) * halfProj
            val endX = centerXPx + cos(angleRad) * halfProj
            val endY = centerYPx + sin(angleRad) * halfProj

            Brush.linearGradient(
                colorStops = colors.zip(positions).map { (c, p) -> p to c }.toTypedArray(),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            )
        }

        GradientType.RADIAL -> {
            val cx = size.width * centerX
            val cy = size.height * centerY
            val maxDim = max(size.width, size.height)
            val radius = (maxDim / 2f) * radiusScale

            Brush.radialGradient(
                colorStops = colors.zip(positions).map { (c, p) -> p to c }.toTypedArray(),
                center = Offset(cx, cy),
                radius = radius,
                tileMode = TileMode.Clamp
            )
        }

        GradientType.SWEEP -> {
            val cx = size.width * centerX
            val cy = size.height * centerY

            Brush.sweepGradient(
                colorStops = colors.zip(positions).map { (c, p) -> p to c }.toTypedArray(),
                center = Offset(cx, cy)
            )
        }

        GradientType.DIAMOND -> {
            // Diamond gradient approximated with a radial gradient
            val cx = size.width * centerX
            val cy = size.height * centerY
            val maxDim = max(size.width, size.height)
            val radius = (maxDim / 2f) * radiusScale

            Brush.radialGradient(
                colorStops = colors.zip(positions).map { (c, p) -> p to c }.toTypedArray(),
                center = Offset(cx, cy),
                radius = radius,
                tileMode = TileMode.Clamp
            )
        }

        GradientType.CONIC -> {
            // Conic is effectively a sweep gradient
            val cx = size.width * centerX
            val cy = size.height * centerY

            Brush.sweepGradient(
                colorStops = colors.zip(positions).map { (c, p) -> p to c }.toTypedArray(),
                center = Offset(cx, cy)
            )
        }
    }
}

fun GradientSpec.toAnimatedBrush(size: Size, time: Float): Brush {
    val animatedAngle = angleDegrees + time * 360f
    val animatedSpec = copy(angleDegrees = animatedAngle)
    return when (type) {
        GradientType.LINEAR -> animatedSpec.toBrush(size)
        GradientType.RADIAL -> {
            val pulseFactor = 0.8f + 0.4f * sin(time * 2f * PI.toFloat())
            animatedSpec.copy(radiusScale = radiusScale * pulseFactor).toBrush(size)
        }
        GradientType.SWEEP, GradientType.CONIC -> {
            val shiftedStops = stops.map { stop ->
                stop.copy(position = (stop.position + time) % 1f)
            }.sortedBy { it.position }
            animatedSpec.copy(stops = shiftedStops).toBrush(size)
        }
        GradientType.DIAMOND -> {
            val pulseFactor = 0.8f + 0.4f * sin(time * 2f * PI.toFloat())
            animatedSpec.copy(radiusScale = radiusScale * pulseFactor).toBrush(size)
        }
    }
}

@Composable
fun GradientBackground(
    spec: GradientSpec,
    animated: Boolean = false,
    animationDuration: Int = 5000,
    modifier: Modifier = Modifier
) {
    if (animated) {
        val infiniteTransition = rememberInfiniteTransition(label = "gradientBg")
        val time by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "gradientTime"
        )

        Box(modifier = modifier) {
            val boxModifier = Modifier.matchParentSize()
            androidx.compose.foundation.Canvas(modifier = boxModifier) {
                drawRect(brush = spec.toAnimatedBrush(size, time))
            }
        }
    } else {
        Box(modifier = modifier) {
            val boxModifier = Modifier.matchParentSize()
            androidx.compose.foundation.Canvas(modifier = boxModifier) {
                drawRect(brush = spec.toBrush(size))
            }
        }
    }
}

fun blendGradients(from: GradientSpec, to: GradientSpec, fraction: Float): GradientSpec {
    val clampedFraction = fraction.coerceIn(0f, 1f)

    val maxStops = max(from.stops.size, to.stops.size)
    val blendedStops = (0 until maxStops).map { i ->
        val fromStop = from.stops.getOrElse(i) { from.stops.last() }
        val toStop = to.stops.getOrElse(i) { to.stops.last() }

        GradientStop(
            color = lerp(fromStop.color, toStop.color, clampedFraction),
            position = fromStop.position + (toStop.position - fromStop.position) * clampedFraction
        )
    }

    val blendedAngle = from.angleDegrees + (to.angleDegrees - from.angleDegrees) * clampedFraction
    val blendedCenterX = from.centerX + (to.centerX - from.centerX) * clampedFraction
    val blendedCenterY = from.centerY + (to.centerY - from.centerY) * clampedFraction
    val blendedRadius = from.radiusScale + (to.radiusScale - from.radiusScale) * clampedFraction

    return GradientSpec(
        type = if (clampedFraction < 0.5f) from.type else to.type,
        stops = blendedStops,
        angleDegrees = blendedAngle,
        centerX = blendedCenterX,
        centerY = blendedCenterY,
        radiusScale = blendedRadius
    )
}
