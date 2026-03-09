package com.sugarmunch.app.ui.visual

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.sin

enum class GradientAnimation {
    ROTATION,
    COLOR_CYCLE,
    SHIMMER_SWEEP,
    BREATHING,
    WAVE,
    KALEIDOSCOPE
}

data class AnimatedGradientSpec(
    val baseGradient: GradientSpec,
    val animation: GradientAnimation,
    val speed: Float = 1f,
    val intensity: Float = 1f
)

@Composable
fun AnimatedGradientSurface(
    spec: AnimatedGradientSpec,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val baseDuration = (5000 / spec.speed).toInt().coerceAtLeast(500)
    val infiniteTransition = rememberInfiniteTransition(label = "animGradient")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = baseDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientProgress"
    )

    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (baseDuration * 1.5f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathProgress"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val brush = when (spec.animation) {
                GradientAnimation.ROTATION -> buildRotationBrush(spec, progress, size)
                GradientAnimation.COLOR_CYCLE -> buildColorCycleBrush(spec, progress, size)
                GradientAnimation.SHIMMER_SWEEP -> buildShimmerBrush(spec, progress, size)
                GradientAnimation.BREATHING -> buildBreathingBrush(spec, breathProgress, size)
                GradientAnimation.WAVE -> buildWaveBrush(spec, progress, size)
                GradientAnimation.KALEIDOSCOPE -> buildKaleidoscopeBrush(spec, progress, size)
            }
            drawRect(brush = brush)
        }
        content()
    }
}

private fun buildRotationBrush(spec: AnimatedGradientSpec, progress: Float, size: Size): Brush {
    val angle = spec.baseGradient.angleDegrees + progress * 360f * spec.intensity
    return spec.baseGradient.copy(angleDegrees = angle).toBrush(size)
}

private fun buildColorCycleBrush(spec: AnimatedGradientSpec, progress: Float, size: Size): Brush {
    val stops = spec.baseGradient.stops
    if (stops.isEmpty()) return Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))

    val shift = progress * spec.intensity
    val cycledStops = stops.mapIndexed { index, stop ->
        val sourceIndex = index
        val targetIndex = (index + 1) % stops.size
        val sourceColor = stops[sourceIndex].color
        val targetColor = stops[targetIndex].color
        stop.copy(color = lerp(sourceColor, targetColor, shift))
    }

    return spec.baseGradient.copy(stops = cycledStops).toBrush(size)
}

private fun buildShimmerBrush(spec: AnimatedGradientSpec, progress: Float, size: Size): Brush {
    val shimmerWidth = size.width * 0.3f * spec.intensity
    val shimmerStart = -shimmerWidth + progress * (size.width + shimmerWidth * 2)
    val shimmerEnd = shimmerStart + shimmerWidth

    val baseColors = spec.baseGradient.stops.sortedBy { it.position }.map { it.color }
    val baseColor = if (baseColors.isNotEmpty()) baseColors.first() else Color.Gray

    val shimmerColor = baseColor.copy(
        red = (baseColor.red + 0.3f).coerceAtMost(1f),
        green = (baseColor.green + 0.3f).coerceAtMost(1f),
        blue = (baseColor.blue + 0.3f).coerceAtMost(1f)
    )

    val baseBrush = spec.baseGradient.toBrush(size)

    return Brush.linearGradient(
        colorStops = arrayOf(
            0f to Color.Transparent,
            ((shimmerStart / size.width).coerceIn(0f, 1f)) to Color.Transparent,
            ((shimmerStart + shimmerWidth * 0.5f) / size.width).coerceIn(0f, 1f) to shimmerColor.copy(alpha = 0.4f * spec.intensity),
            ((shimmerEnd / size.width).coerceIn(0f, 1f)) to Color.Transparent,
            1f to Color.Transparent
        ),
        start = Offset.Zero,
        end = Offset(size.width, 0f)
    )
}

private fun buildBreathingBrush(spec: AnimatedGradientSpec, progress: Float, size: Size): Brush {
    val breathFactor = 0.6f + 0.4f * progress * spec.intensity
    val breathedStops = spec.baseGradient.stops.map { stop ->
        stop.copy(color = stop.color.copy(alpha = stop.color.alpha * breathFactor))
    }
    return spec.baseGradient.copy(stops = breathedStops).toBrush(size)
}

private fun buildWaveBrush(spec: AnimatedGradientSpec, progress: Float, size: Size): Brush {
    val waveOffset = sin(progress * 2f * PI.toFloat()) * 0.15f * spec.intensity
    val wavedStops = spec.baseGradient.stops.map { stop ->
        val newPos = (stop.position + waveOffset * sin(stop.position * PI.toFloat() * 4f)).coerceIn(0f, 1f)
        stop.copy(position = newPos)
    }.sortedBy { it.position }
    return spec.baseGradient.copy(stops = wavedStops).toBrush(size)
}

private fun buildKaleidoscopeBrush(spec: AnimatedGradientSpec, progress: Float, size: Size): Brush {
    val angle = progress * 360f * spec.intensity
    val mirroredStops = buildList {
        val sorted = spec.baseGradient.stops.sortedBy { it.position }
        // First half: original order
        sorted.forEach { stop ->
            add(stop.copy(position = stop.position * 0.5f))
        }
        // Second half: reversed (mirror)
        sorted.reversed().forEach { stop ->
            add(stop.copy(position = 0.5f + (1f - stop.position) * 0.5f))
        }
    }

    val cx = size.width * spec.baseGradient.centerX
    val cy = size.height * spec.baseGradient.centerY

    return Brush.sweepGradient(
        colorStops = mirroredStops.map { it.position to it.color }.toTypedArray(),
        center = Offset(cx, cy)
    )
}
