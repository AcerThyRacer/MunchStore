package com.sugarmunch.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashThemePreview(
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val displayColors = colors.take(6)
    val scales = remember(displayColors) { displayColors.map { Animatable(0f) } }
    val pulses = remember(displayColors) { displayColors.map { Animatable(1f) } }

    LaunchedEffect(displayColors) {
        // Staggered scale-in
        displayColors.forEachIndexed { index, _ ->
            launch {
                delay(index * 50L)
                scales[index].animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
        }

        // Gentle pulsing after initial scale-in
        delay(300)
        displayColors.forEachIndexed { index, _ ->
            launch {
                delay(index * 80L)
                while (true) {
                    pulses[index].animateTo(1.1f, tween(600))
                    pulses[index].animateTo(0.95f, tween(600))
                }
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayColors.forEachIndexed { index, color ->
            val scale = scales[index].value
            val pulse = pulses[index].value
            val combinedScale = scale * pulse

            Canvas(
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        scaleX = combinedScale
                        scaleY = combinedScale
                    }
            ) {
                // Shadow glow
                drawCircle(
                    color = color.copy(alpha = 0.3f),
                    radius = size.minDimension / 2f * 1.4f
                )
                // Main circle
                drawCircle(
                    color = color,
                    radius = size.minDimension / 2f
                )
                // Highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = size.minDimension / 2f * 0.4f,
                    center = Offset(center.x - size.minDimension * 0.1f, center.y - size.minDimension * 0.1f)
                )
            }
        }
    }
}

@Composable
fun ThemeTransitionOverlay(
    fromColors: List<Color>,
    toColors: List<Color>,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val safeFromColors = fromColors.ifEmpty { listOf(Color.White, Color.LightGray) }
    val safeToColors = toColors.ifEmpty { listOf(Color.White, Color.LightGray) }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Blend the two sets of colors based on progress
        val maxCount = maxOf(safeFromColors.size, safeToColors.size).coerceAtLeast(2)
        val blendedColors = (0 until maxCount).map { i ->
            val fromColor = safeFromColors[i % safeFromColors.size]
            val toColor = safeToColors[i % safeToColors.size]
            lerpColor(fromColor, toColor, progress)
        }

        // "From" gradient fading out
        val fromAlpha = (1f - progress).coerceIn(0f, 1f)
        drawRect(
            brush = Brush.radialGradient(
                colors = safeFromColors.map { it.copy(alpha = fromAlpha * 0.8f) } + listOf(Color.Transparent),
                center = center,
                radius = size.maxDimension * 0.7f
            ),
            size = size
        )

        // "To" gradient fading in
        val toAlpha = progress.coerceIn(0f, 1f)
        drawRect(
            brush = Brush.radialGradient(
                colors = safeToColors.map { it.copy(alpha = toAlpha * 0.8f) } + listOf(Color.Transparent),
                center = center,
                radius = size.maxDimension * 0.7f
            ),
            size = size
        )

        // Blended radial gradient on top for a smooth cross-fade
        drawRect(
            brush = Brush.radialGradient(
                colors = blendedColors.map { it.copy(alpha = 0.4f) } + listOf(Color.Transparent),
                center = center,
                radius = size.maxDimension * 0.6f
            ),
            size = size
        )
    }
}

private fun lerpColor(from: Color, to: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = from.red + (to.red - from.red) * f,
        green = from.green + (to.green - from.green) * f,
        blue = from.blue + (to.blue - from.blue) * f,
        alpha = from.alpha + (to.alpha - from.alpha) * f
    )
}

@Composable
fun WelcomeBackSplash(
    userName: String?,
    streakDays: Int = 0,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit
) {
    val greetingAlpha = remember { Animatable(0f) }
    val streakAlpha = remember { Animatable(0f) }
    val streakScale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        // Fade in greeting
        greetingAlpha.animateTo(1f, tween(400))

        // Streak animation after a brief pause
        if (streakDays > 0) {
            delay(300)
            launch { streakAlpha.animateTo(1f, tween(300)) }
            launch {
                streakScale.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
        }

        // Auto-dismiss after 1.5s
        delay(1500)
        onComplete()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Theme-colored background gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            val primaryColor = Color(0xFFFFB6C1)
            val secondaryColor = Color(0xFFFF69B4)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.2f),
                        secondaryColor.copy(alpha = 0.1f),
                        Color.White
                    ),
                    center = center,
                    radius = size.maxDimension * 0.8f
                ),
                size = size
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val displayName = userName ?: "there"
            Text(
                text = "Welcome back, $displayName!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = greetingAlpha.value }
            )

            if (streakDays > 0) {
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

                Text(
                    text = "\uD83D\uDD25 $streakDays day streak!",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = streakAlpha.value
                        scaleX = streakScale.value
                        scaleY = streakScale.value
                    }
                )
            }
        }
    }
}
