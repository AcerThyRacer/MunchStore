package com.sugarmunch.app.ui.feedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.cos
import kotlin.math.sin

/**
 * EXTREME Loading Animations for SugarMunch
 * Advanced spinners, progress indicators, and skeleton loaders
 */

/**
 * Loading type
 */
enum class LoadingType {
    CANDY_SWIRL,
    ORBITING_PLANETS,
    PARTICLE_TRAIL,
    WAVE_LOADING,
    PULSE_RING,
    HELIX_SPINNER,
    RAINBOW_SPINNER,
    SUGAR_DUST,
    LIQUID_FILL,
    NEON_SPINNER
}

/**
 * Main loading indicator
 */
@Composable
fun ExtremeLoadingIndicator(
    modifier: Modifier = Modifier,
    loadingType: LoadingType = LoadingType.CANDY_SWIRL,
    size: Float = 64f,
    colors: List<Color> = listOf(
        SugarDimens.Brand.hotPink,
        SugarDimens.Brand.mint,
        SugarDimens.Brand.yellow,
        SugarDimens.Brand.candyOrange
    ),
    speed: Float = 1.0f
) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(0.5f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            rotation.animateTo(
                rotation.value + 360f,
                animationSpec = tween((1500 / speed).toInt(), easing = LinearEasing)
            )
        }
    }
    
    Canvas(
        modifier = modifier.size(size.dp)
    ) {
        when (loadingType) {
            LoadingType.CANDY_SWIRL -> drawCandySwirl(rotation.value, colors, size)
            LoadingType.ORBITING_PLANETS -> drawOrbitingPlanets(rotation.value, colors, size)
            LoadingType.PARTICLE_TRAIL -> drawParticleTrail(rotation.value, colors, size)
            LoadingType.WAVE_LOADING -> drawWaveLoading(rotation.value, colors, size)
            LoadingType.PULSE_RING -> drawPulseRing(alpha, colors, size)
            LoadingType.HELIX_SPINNER -> drawHelixSpinner(rotation.value, colors, size)
            LoadingType.RAINBOW_SPINNER -> drawRainbowSpinner(rotation.value, colors, size)
            LoadingType.NEON_SPINNER -> drawNeonSpinner(rotation.value, colors, size)
            else -> drawDefaultSpinner(rotation.value, colors, size)
        }
    }
}

/**
 * Draw candy swirl spinner
 */
private fun DrawScope.drawCandySwirl(
    rotation: Float,
    colors: List<Color>,
    size: Float
) {
    val center = Offset(size / 2, size / 2)
    val radius = size / 2 * 0.9f
    
    repeat(4) { i ->
        val angle = rotation + (i * 90f)
        val color = colors[i % colors.size]
        
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Draw orbiting planets
 */
private fun DrawScope.drawOrbitingPlanets(
    rotation: Float,
    colors: List<Color>,
    size: Float
) {
    val center = Offset(size / 2, size / 2)
    
    // Draw center sun
    drawCircle(
        color = colors[0],
        radius = size / 8,
        center = center
    )
    
    // Draw orbiting planets
    repeat(3) { i ->
        val orbitRadius = size / 4 * (i + 1)
        val planetAngle = rotation + (i * 120f)
        val planetX = center.x + cos(planetAngle * Math.PI / 180).toFloat() * orbitRadius
        val planetY = center.y + sin(planetAngle * Math.PI / 180).toFloat() * orbitRadius
        
        drawCircle(
            color = colors[(i + 1) % colors.size],
            radius = size / 12,
            center = Offset(planetX, planetY)
        )
        
        // Draw orbit path
        drawCircle(
            color = colors[i % colors.size].copy(alpha = 0.3f),
            radius = orbitRadius,
            center = center,
            style = Stroke(width = 1f)
        )
    }
}

/**
 * Draw particle trail
 */
private fun DrawScope.drawParticleTrail(
    rotation: Float,
    colors: List<Color>,
    size: Float
) {
    val center = Offset(size / 2, size / 2)
    val radius = size / 2 * 0.8f
    
    repeat(12) { i ->
        val angle = rotation + (i * 30f)
        val alpha = 1f - (i / 12f)
        val particleSize = (size / 16) * (1f - i / 12f)
        
        val x = center.x + cos(angle * Math.PI / 180).toFloat() * radius
        val y = center.y + sin(angle * Math.PI / 180).toFloat() * radius
        
        drawCircle(
            color = colors[i % colors.size].copy(alpha = alpha),
            radius = particleSize,
            center = Offset(x, y)
        )
    }
}

/**
 * Draw wave loading
 */
private fun DrawScope.drawWaveLoading(
    rotation: Float,
    colors: List<Color>,
    size: Float
) {
    val center = Offset(size / 2, size / 2)
    val radius = size / 2 * 0.9f
    
    repeat(3) { i ->
        val wavePhase = rotation + (i * 120f)
        val color = colors[i % colors.size]
        
        val path = androidx.compose.ui.graphics.Path()
        var angle = 0f
        
        while (angle <= 360f) {
            val waveOffset = kotlin.math.sin((angle + wavePhase) * Math.PI / 180).toFloat() * 10f
            val x = center.x + cos(angle * Math.PI / 180).toFloat() * (radius + waveOffset)
            val y = center.y + sin(angle * Math.PI / 180).toFloat() * (radius + waveOffset)
            
            if (angle == 0f) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            angle += 10f
        }
        
        path.close()
        drawPath(path, color.copy(alpha = 0.5f), style = Stroke(width = 4f))
    }
}

/**
 * Draw pulse ring
 */
private fun DrawScope.drawPulseRing(
    alpha: State<Float>,
    colors: List<Color>,
    size: Float
) {
    val center = Offset(size / 2, size / 2)
    val baseRadius = size / 4
    
    repeat(3) { i ->
        val ringAlpha = alpha.value * (1f - i / 3f)
        val ringRadius = baseRadius * (i + 1)
        
        drawCircle(
            color = colors[i % colors.size].copy(alpha = ringAlpha),
            radius = ringRadius,
            center = center,
            style = Stroke(width = 3f)
        )
    }
}

/**
 * Draw helix spinner
 */
private fun DrawScope.drawHelixSpinner(
    rotation: Float,
    colors: List<Color>,
    size: Float
) {
    val center = Offset(size / 2, size / 2)
    
    repeat(8) { i ->
        val angle = rotation + (i * 45f)
        val radius = size / 4 * (i / 8f + 0.5f)
        val color = colors[i % colors.size]
        
        val x = center.x + cos(angle * Math.PI / 180).toFloat() * radius
        val y = center.y + sin(angle * Math.PI / 180).toFloat() * radius
        
        drawCircle(
            color = color,
            radius = size / 16,
            center = Offset(x, y)
        )
    }
}

/**
 * Draw rainbow spinner
 */
private fun DrawScope.drawRainbowSpinner(
    rotation: Float,
    colors: List<Color>,
    size: Float
) {
    val center = Offset(size / 2, size / 2)
    val radius = size / 2 * 0.9f
    
    colors.forEachIndexed { i, color ->
        val startAngle = rotation + (i * 360f / colors.size)
        val sweepAngle = 360f / colors.size
        
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
    }
}

/**
 * Draw neon spinner
 */
private fun DrawScope.drawNeonSpinner(
    rotation: Float,
    colors: List<Color>,
    size: Float
) {
    val center = Offset(size / 2, size / 2)
    val radius = size / 2 * 0.8f
    
    // Draw outer glow
    drawCircle(
        color = colors[0].copy(alpha = 0.3f),
        radius = radius + 10f,
        center = center,
        style = Stroke(width = 15f)
    )
    
    // Draw main spinner
    drawArc(
        color = colors[0],
        startAngle = rotation,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 6f, cap = StrokeCap.Round)
    )
}

/**
 * Draw default spinner
 */
private fun DrawScope.drawDefaultSpinner(
    rotation: Float,
    colors: List<Color>,
    size: Float
) {
    drawCandySwirl(rotation, colors, size)
}

/**
 * Progress bar with extreme effects
 */
@Composable
fun ExtremeProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    loadingType: LoadingType = LoadingType.CANDY_SWIRL,
    colors: List<Color> = listOf(
        SugarDimens.Brand.hotPink,
        SugarDimens.Brand.mint,
        SugarDimens.Brand.yellow
    )
) {
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            progress.coerceIn(0f, 1f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .height(8.dp)
                    .background(
                        brush = Brush.horizontalGradient(colors = colors),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
        
        // Loading indicator
        ExtremeLoadingIndicator(
            loadingType = loadingType,
            size = 32f,
            colors = colors,
            speed = animatedProgress.value + 0.5f
        )
    }
}

/**
 * Skeleton loader with shimmer wave
 */
@Composable
fun ExtremeSkeletonLoader(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(SugarDimens.Radius.md)
) {
    val shimmerOffset = remember { Animatable(-1f) }
    
    LaunchedEffect(Unit) {
        shimmerOffset.animateTo(
            2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Gray.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.5f),
                        Color.Gray.copy(alpha = 0.3f)
                    ),
                    startX = shimmerOffset.value * modifier.toString().toFloatOrNull() ?: 0f,
                    endX = (shimmerOffset.value + 1) * 100f
                ),
                shape = shape
            )
    )
}

/**
 * Quick loading preset
 */
@Composable
fun QuickLoadingSpinner(
    modifier: Modifier = Modifier
) {
    ExtremeLoadingIndicator(
        modifier = modifier,
        loadingType = LoadingType.CANDY_SWIRL,
        size = 48f
    )
}
