package com.sugarmunch.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CandyPink = Color(0xFFFF69B4)
private val CandyMint = Color(0xFF00E676)
private val CandyYellow = Color(0xFFFDD835)
private val CandyBlue = Color(0xFF42A5F5)
private val CandyPurple = Color(0xFFAB47BC)
private val CandyCaramel = Color(0xFFFFC107)

private val BurstColors = listOf(CandyPink, CandyMint, CandyYellow, CandyBlue, CandyPurple, CandyCaramel)

@Composable
fun AnimatedSplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    // Stage 1 — Logo Morph
    val candyScale = remember { Animatable(0f) }
    val leftWrapperRotation = remember { Animatable(0f) }
    val rightWrapperRotation = remember { Animatable(0f) }

    // Stage 2 — Color Burst
    val burstScales = remember { List(6) { Animatable(0f) } }
    val burstAlphas = remember { List(6) { Animatable(1f) } }

    // Stage 3 — Text Reveal
    val titleAlpha = remember { Animatable(0f) }
    val titleScale = remember { Animatable(0.8f) }
    val taglineAlpha = remember { Animatable(0f) }

    // Stage 4 — Transition Out
    val exitScale = remember { Animatable(1f) }
    val exitAlpha = remember { Animatable(1f) }

    // Background pulse
    val bgPulse = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        // Background pulse runs concurrently
        launch {
            repeat(5) {
                bgPulse.animateTo(1.1f, tween(500))
                bgPulse.animateTo(0.9f, tween(500))
            }
        }

        // Stage 1 — Logo Morph (0-600ms)
        launch { candyScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
        launch { leftWrapperRotation.animateTo(-45f, tween(600)) }
        launch { rightWrapperRotation.animateTo(45f, tween(600)) }
        delay(600)

        // Stage 2 — Color Burst (600-1200ms)
        burstScales.forEachIndexed { index, animatable ->
            launch {
                delay(index * 50L)
                launch { animatable.animateTo(1f, tween(500)) }
                launch { burstAlphas[index].animateTo(0f, tween(500)) }
            }
        }
        delay(600)

        // Stage 3 — Text Reveal (1200-1800ms)
        launch { titleAlpha.animateTo(1f, tween(400)) }
        launch { titleScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        launch {
            delay(200)
            taglineAlpha.animateTo(1f, tween(300))
        }
        delay(600)

        // Stage 4 — Transition Out (1800-2400ms)
        launch { exitScale.animateTo(1.05f, tween(500)) }
        launch { exitAlpha.animateTo(0f, tween(500)) }
        delay(600)

        onSplashComplete()
    }

    val primaryColor = colors.primary
    val secondaryColor = colors.secondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = exitScale.value
                scaleY = exitScale.value
                alpha = exitAlpha.value
            }
    ) {
        // Radial gradient background that pulses
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pulse = bgPulse.value
            val radius = size.maxDimension * 0.7f * pulse
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.3f),
                        secondaryColor.copy(alpha = 0.15f),
                        Color.White
                    ),
                    center = center,
                    radius = radius
                ),
                size = size
            )
        }

        // Burst circles (Stage 2)
        Canvas(modifier = Modifier.fillMaxSize()) {
            BurstColors.forEachIndexed { index, color ->
                val scale = burstScales[index].value
                val alpha = burstAlphas[index].value
                if (scale > 0f) {
                    val angle = index * 60f
                    val maxRadius = size.minDimension * 0.4f
                    drawCircle(
                        color = color.copy(alpha = alpha * 0.6f),
                        radius = maxRadius * scale,
                        center = center + Offset(
                            x = kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * maxRadius * scale * 0.3f,
                            y = kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * maxRadius * scale * 0.3f
                        )
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Candy icon (Stage 1)
                Canvas(
                    modifier = Modifier
                        .height(SugarDimens.IconSize.hero)
                        .graphicsLayer {
                            scaleX = candyScale.value
                            scaleY = candyScale.value
                        }
                ) {
                    val candyRadius = size.height / 2.5f
                    drawCandyShape(
                        center = center,
                        radius = candyRadius,
                        bodyColor = primaryColor,
                        wrapperColor = secondaryColor,
                        leftRotation = leftWrapperRotation.value,
                        rightRotation = rightWrapperRotation.value
                    )
                }

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.xxl))

                // Title (Stage 3)
                Text(
                    text = "SugarMunch",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        brush = Brush.linearGradient(listOf(primaryColor, secondaryColor))
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha.value
                        scaleX = titleScale.value
                        scaleY = titleScale.value
                    }
                )

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

                // Tagline (Stage 3, delayed)
                Text(
                    text = "Live, Life, Love ❤",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { alpha = taglineAlpha.value }
                )
            }
        }
    }
}

private fun DrawScope.drawCandyShape(
    center: Offset,
    radius: Float,
    bodyColor: Color,
    wrapperColor: Color,
    leftRotation: Float,
    rightRotation: Float
) {
    // Candy body
    drawCircle(color = bodyColor, radius = radius, center = center)

    // Left wrapper triangle
    rotate(leftRotation, pivot = Offset(center.x - radius, center.y)) {
        val leftPath = Path().apply {
            moveTo(center.x - radius, center.y - radius * 0.5f)
            lineTo(center.x - radius * 1.8f, center.y)
            lineTo(center.x - radius, center.y + radius * 0.5f)
            close()
        }
        drawPath(leftPath, color = wrapperColor)
    }

    // Right wrapper triangle
    rotate(rightRotation, pivot = Offset(center.x + radius, center.y)) {
        val rightPath = Path().apply {
            moveTo(center.x + radius, center.y - radius * 0.5f)
            lineTo(center.x + radius * 1.8f, center.y)
            lineTo(center.x + radius, center.y + radius * 0.5f)
            close()
        }
        drawPath(rightPath, color = wrapperColor)
    }
}
