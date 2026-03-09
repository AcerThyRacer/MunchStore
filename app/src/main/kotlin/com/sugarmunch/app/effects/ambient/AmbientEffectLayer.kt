package com.sugarmunch.app.effects.ambient

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.sugarmunch.app.ui.particles.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random

/**
 * Ambient effect types for the background layer
 */
enum class AmbientEffectType {
    NONE,
    FLOATING_PARTICLES,
    RIPPLE_TOUCH,
    EDGE_GLOW,
    BREATHING_PULSE,
    STAR_FIELD,
    BUBBLES,
    CONFETTI_AMBIENT,
    SMOKE_WISPS,
    AURORA,
    RAIN,
    SNOW,
    FIREFLIES,
    NEON_LINES,
    GRADIENT_WAVE
}

/**
 * Configuration for ambient effects
 */
data class AmbientEffectConfig(
    val enabled: Boolean = true,
    val effectType: AmbientEffectType = AmbientEffectType.FLOATING_PARTICLES,
    val intensity: Float = 0.5f,
    val speed: Float = 1.0f,
    val particleCount: Int = 50,
    val primaryColor: Color = Color.White,
    val secondaryColor: Color = Color.Blue,
    val touchReactive: Boolean = true,
    val fadeOnInteraction: Boolean = false,
    val opacity: Float = 0.6f
)

/**
 * AmbientEffectLayer - A composable that adds ambient visual effects across all screens.
 * Features:
 * - Floating particles across all screens
 * - Dynamic backgrounds that react to touch
 * - Screen-edge glow trails
 * - Ripple effects on tap anywhere
 * - Breathing/pulsing UI elements
 */
@Composable
fun AmbientEffectLayer(
    modifier: Modifier = Modifier,
    config: AmbientEffectConfig = AmbientEffectConfig(),
    content: @Composable () -> Unit = {}
) {
    if (!config.enabled || config.effectType == AmbientEffectType.NONE) {
        content()
        return
    }

    // Touch position for reactive effects
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    var isTouching by remember { mutableStateOf(false) }

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                if (config.touchReactive) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press -> {
                                    isTouching = true
                                    event.changes.firstOrNull()?.position?.let {
                                        touchPosition = it
                                    }
                                }
                                PointerEventType.Release -> {
                                    isTouching = false
                                }
                                PointerEventType.Move -> {
                                    event.changes.firstOrNull()?.position?.let {
                                        touchPosition = it
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // Content layer
        content()

        // Effect layer based on type
        when (config.effectType) {
            AmbientEffectType.FLOATING_PARTICLES -> FloatingParticlesEffect(
                config = config,
                touchPosition = touchPosition,
                isTouching = isTouching
            )
            AmbientEffectType.RIPPLE_TOUCH -> RippleTouchEffect(
                config = config,
                touchPosition = touchPosition,
                isTouching = isTouching
            )
            AmbientEffectType.EDGE_GLOW -> EdgeGlowEffect(config, touchPosition)
            AmbientEffectType.BREATHING_PULSE -> BreathingPulseEffect(config)
            AmbientEffectType.STAR_FIELD -> StarFieldEffect(config)
            AmbientEffectType.BUBBLES -> BubblesEffect(config, touchPosition)
            AmbientEffectType.CONFETTI_AMBIENT -> ConfettiAmbientEffect(config)
            AmbientEffectType.SMOKE_WISPS -> SmokeWispsEffect(config, touchPosition)
            AmbientEffectType.AURORA -> AuroraEffect(config)
            AmbientEffectType.RAIN -> RainEffect(config)
            AmbientEffectType.SNOW -> SnowEffect(config)
            AmbientEffectType.FIREFLIES -> FirefliesEffect(config, touchPosition)
            AmbientEffectType.NEON_LINES -> NeonLinesEffect(config, infiniteTransition)
            AmbientEffectType.GRADIENT_WAVE -> GradientWaveEffect(config, infiniteTransition)
            else -> {}
        }
    }
}

/**
 * Floating Particles Effect
 */
@Composable
private fun FloatingParticlesEffect(
    config: AmbientEffectConfig,
    touchPosition: Offset,
    isTouching: Boolean
) {
    val particles = remember {
        List(config.particleCount) {
            AmbientParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                speedX = (Random.nextFloat() - 0.5f) * 0.001f,
                speedY = (Random.nextFloat() - 0.5f) * 0.001f,
                alpha = Random.nextFloat() * 0.5f + 0.3f
            )
        }
    }

    val density = LocalDensity.current
    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(16)
            time += 16
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                particles.forEach { particle ->
                    // Calculate position with time
                    var x = particle.x + particle.speedX * time * config.speed
                    var y = particle.y + particle.speedY * time * config.speed

                    // Wrap around
                    x = (x % 1f + 1f) % 1f
                    y = (y % 1f + 1f) % 1f

                    // React to touch
                    val touchInfluence = if (isTouching) {
                        val dx = x - touchPosition.x / size.width
                        val dy = y - touchPosition.y / size.height
                        1f / (dx * dx + dy * dy + 0.1f) * 0.01f
                    } else 0f

                    val particleX = x * size.width
                    val particleY = y * size.height

                    drawCircle(
                        color = config.primaryColor.copy(
                            alpha = particle.alpha * config.intensity * config.opacity + touchInfluence
                        ),
                        radius = particle.size * density.density,
                        center = Offset(particleX, particleY)
                    )
                }
            }
    )
}

/**
 * Ripple Touch Effect
 */
@Composable
private fun RippleTouchEffect(
    config: AmbientEffectConfig,
    touchPosition: Offset,
    isTouching: Boolean
) {
    val ripples = remember { mutableStateListOf<Ripple>() }

    LaunchedEffect(isTouching) {
        if (isTouching) {
            ripples.add(
                Ripple(
                    center = touchPosition,
                    startTime = System.currentTimeMillis(),
                    maxRadius = 300f
                )
            )
        }
    }

    // Remove old ripples
    LaunchedEffect(ripples.size) {
        val now = System.currentTimeMillis()
        ripples.removeAll { now - it.startTime > 1500 }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                ripples.forEach { ripple ->
                    val progress = ((System.currentTimeMillis() - ripple.startTime) / 1500f)
                        .coerceIn(0f, 1f)

                    val radius = ripple.maxRadius * progress
                    val alpha = (1f - progress) * config.intensity * config.opacity

                    drawCircle(
                        color = config.primaryColor.copy(alpha = alpha),
                        radius = radius,
                        center = ripple.center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.dp.toPx() * (1f - progress)
                        )
                    )
                }
            }
    )
}

/**
 * Edge Glow Effect
 */
@Composable
private fun EdgeGlowEffect(
    config: AmbientEffectConfig,
    touchPosition: Offset
) {
    val infiniteTransition = rememberInfiniteTransition(label = "edge_glow")
    val glowPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_pos"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Top edge glow
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            config.primaryColor.copy(alpha = 0.3f * config.intensity),
                            Color.Transparent
                        ),
                        startX = size.width * (glowPosition - 0.3f),
                        endX = size.width * (glowPosition + 0.3f)
                    ),
                    topLeft = Offset(0f, 0f),
                    size = size.copy(height = 20.dp.toPx())
                )

                // Bottom edge glow
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            config.secondaryColor.copy(alpha = 0.3f * config.intensity),
                            Color.Transparent
                        ),
                        startX = size.width * ((1f - glowPosition) - 0.3f),
                        endX = size.width * ((1f - glowPosition) + 0.3f)
                    ),
                    topLeft = Offset(0f, size.height - 20.dp.toPx()),
                    size = size.copy(height = 20.dp.toPx())
                )
            }
    )
}

/**
 * Breathing Pulse Effect
 */
@Composable
private fun BreathingPulseEffect(config: AmbientEffectConfig) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val maxRadius = minOf(size.width, size.height) / 2

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            config.primaryColor.copy(alpha = 0.1f + pulse * 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = maxRadius * (0.8f + pulse * 0.2f)
                    ),
                    radius = maxRadius
                )
            }
    )
}

/**
 * Star Field Effect
 */
@Composable
private fun StarFieldEffect(config: AmbientEffectConfig) {
    val stars = remember {
        List(config.particleCount * 2) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                twinkleSpeed = Random.nextFloat() * 0.005f + 0.001f,
                twinkleOffset = Random.nextFloat() * (2 * PI).toFloat()
            )
        }
    }

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(16)
            time += 16
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                stars.forEach { star ->
                    val twinkle = sin(time * star.twinkleSpeed + star.twinkleOffset)
                    val alpha = (0.3f + twinkle * 0.4f) * config.intensity * config.opacity

                    drawCircle(
                        color = Color.White.copy(alpha = alpha.coerceIn(0f, 1f)),
                        radius = star.size,
                        center = Offset(star.x * size.width, star.y * size.height)
                    )
                }
            }
    )
}

/**
 * Bubbles Effect
 */
@Composable
private fun BubblesEffect(
    config: AmbientEffectConfig,
    touchPosition: Offset
) {
    val bubbles = remember {
        List(config.particleCount / 2) {
            Bubble(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 30f + 10f,
                speed = Random.nextFloat() * 0.0005f + 0.0002f,
                wobbleSpeed = Random.nextFloat() * 0.003f + 0.001f
            )
        }
    }

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(16)
            time += 16
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                bubbles.forEach { bubble ->
                    var y = (bubble.y - bubble.speed * time * config.speed) % 1f
                    if (y < 0) y += 1f

                    val wobble = sin(time * bubble.wobbleSpeed) * 0.02f
                    val x = bubble.x + wobble

                    drawCircle(
                        color = config.primaryColor.copy(alpha = 0.2f * config.intensity),
                        radius = bubble.size,
                        center = Offset(x * size.width, y * size.height),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                }
            }
    )
}

/**
 * Confetti Ambient Effect
 */
@Composable
private fun ConfettiAmbientEffect(config: AmbientEffectConfig) {
    val confetti = remember {
        List(config.particleCount) {
            ConfettiPiece(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 8f + 4f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 2f - 1f,
                fallSpeed = Random.nextFloat() * 0.0003f + 0.0001f,
                swaySpeed = Random.nextFloat() * 0.002f + 0.001f
            )
        }
    }

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(16)
            time += 16
        }
    }

    val colors = listOf(
        config.primaryColor,
        config.secondaryColor,
        Color.Yellow,
        Color.Green,
        Color.Magenta
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                confetti.forEachIndexed { index, piece ->
                    val y = (piece.y + piece.fallSpeed * time * config.speed) % 1f
                    val sway = sin(time * piece.swaySpeed + index) * 0.05f
                    val x = piece.x + sway

                    val rotation = piece.rotation + piece.rotationSpeed * time

                    drawRect(
                        color = colors[index % colors.size].copy(alpha = 0.6f * config.intensity),
                        topLeft = Offset(x * size.width, y * size.height),
                        size = androidx.compose.ui.geometry.Size(piece.size, piece.size / 2)
                    )
                }
            }
    )
}

/**
 * Smoke Wisps Effect
 */
@Composable
private fun SmokeWispsEffect(
    config: AmbientEffectConfig,
    touchPosition: Offset
) {
    val wisps = remember {
        List(config.particleCount / 4) {
            SmokeWisp(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 100f + 50f,
                speed = Random.nextFloat() * 0.0002f + 0.0001f,
                driftSpeed = Random.nextFloat() * 0.0001f
            )
        }
    }

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(16)
            time += 16
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                wisps.forEach { wisp ->
                    val y = (wisp.y - wisp.speed * time * config.speed) % 1f
                    val x = wisp.x + sin(time * 0.001) * wisp.driftSpeed * time

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                config.primaryColor.copy(alpha = 0.1f * config.intensity),
                                Color.Transparent
                            )
                        ),
                        radius = wisp.size,
                        center = Offset(x * size.width, (y + 1) % 1f * size.height)
                    )
                }
            }
    )
}

/**
 * Aurora Effect
 */
@Composable
private fun AuroraEffect(config: AmbientEffectConfig) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val waveHeight = size.height * 0.3f

                for (i in 0 until 3) {
                    val yOffset = size.height * 0.3f + i * 50.dp.toPx()
                    val alpha = (0.2f - i * 0.05f) * config.intensity

                    val path = Path().apply {
                        moveTo(0f, yOffset)
                        for (x in 0..size.width.toInt() step 10) {
                            val y = yOffset + sin((x / size.width * 4 + waveOffset * 2 + i) * PI).toFloat() * 50.dp.toPx()
                            lineTo(x.toFloat(), y)
                        }
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }

                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                config.primaryColor.copy(alpha = alpha),
                                config.secondaryColor.copy(alpha = alpha)
                            )
                        )
                    )
                }
            }
    )
}

/**
 * Rain Effect
 */
@Composable
private fun RainEffect(config: AmbientEffectConfig) {
    val raindrops = remember {
        List(config.particleCount * 3) {
            Raindrop(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                length = Random.nextFloat() * 20f + 10f,
                speed = Random.nextFloat() * 0.002f + 0.001f
            )
        }
    }

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(16)
            time += 16
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                raindrops.forEach { drop ->
                    val y = (drop.y + drop.speed * time * config.speed) % 1f

                    drawLine(
                        color = config.primaryColor.copy(alpha = 0.3f * config.intensity),
                        start = Offset(drop.x * size.width, y * size.height),
                        end = Offset(drop.x * size.width, y * size.height + drop.length),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
    )
}

/**
 * Snow Effect
 */
@Composable
private fun SnowEffect(config: AmbientEffectConfig) {
    val snowflakes = remember {
        List(config.particleCount * 2) {
            Snowflake(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 6f + 2f,
                speed = Random.nextFloat() * 0.0005f + 0.0002f,
                wobbleSpeed = Random.nextFloat() * 0.002f + 0.001f
            )
        }
    }

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(16)
            time += 16
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                snowflakes.forEach { flake ->
                    val y = (flake.y + flake.speed * time * config.speed) % 1f
                    val wobble = sin(time * flake.wobbleSpeed) * 0.03f
                    val x = flake.x + wobble

                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f * config.intensity),
                        radius = flake.size,
                        center = Offset(x * size.width, y * size.height)
                    )
                }
            }
    )
}

/**
 * Fireflies Effect
 */
@Composable
private fun FirefliesEffect(
    config: AmbientEffectConfig,
    touchPosition: Offset
) {
    val fireflies = remember {
        List(config.particleCount / 2) {
            Firefly(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                glowSpeed = Random.nextFloat() * 0.003f + 0.001f,
                moveSpeed = Random.nextFloat() * 0.0002f + 0.0001f
            )
        }
    }

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(16)
            time += 16
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                fireflies.forEach { firefly ->
                    val glow = sin(time * firefly.glowSpeed)
                    val alpha = (0.3f + glow * 0.7f) * config.intensity

                    // Random movement
                    val x = (firefly.x + sin(time * 0.001) * 0.05f) % 1f
                    val y = (firefly.y + cos(time * 0.0015) * 0.03f) % 1f

                    // Draw glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                config.primaryColor.copy(alpha = alpha),
                                Color.Transparent
                            )
                        ),
                        radius = firefly.size * 3,
                        center = Offset(x * size.width, y * size.height)
                    )

                    // Draw core
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = firefly.size,
                        center = Offset(x * size.width, y * size.height)
                    )
                }
            }
    )
}

/**
 * Neon Lines Effect
 */
@Composable
private fun NeonLinesEffect(
    config: AmbientEffectConfig,
    infiniteTransition: InfiniteTransition
) {
    val lineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "line_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                for (i in 0 until 5) {
                    val angle = (lineOffset + i * 72) * PI / 180
                    val startX = size.width / 2 + cos(angle) * size.width * 0.3f
                    val startY = size.height / 2 + sin(angle) * size.height * 0.3f
                    val endX = size.width / 2 + cos(angle) * size.width
                    val endY = size.height / 2 + sin(angle) * size.height

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                config.primaryColor.copy(alpha = 0.5f * config.intensity),
                                Color.Transparent
                            )
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
    )
}

/**
 * Gradient Wave Effect
 */
@Composable
private fun GradientWaveEffect(
    config: AmbientEffectConfig,
    infiniteTransition: InfiniteTransition
) {
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            config.primaryColor.copy(alpha = 0.1f * config.intensity),
                            config.secondaryColor.copy(alpha = 0.05f * config.intensity),
                            config.primaryColor.copy(alpha = 0.1f * config.intensity)
                        ),
                        start = Offset(wavePhase * size.width, 0f),
                        end = Offset((wavePhase + 1) * size.width, size.height)
                    )
                )
            }
    )
}

// Data classes for particles
private data class AmbientParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val alpha: Float
)

private data class Ripple(
    val center: Offset,
    val startTime: Long,
    val maxRadius: Float
)

private data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val twinkleSpeed: Float,
    val twinkleOffset: Float
)

private data class Bubble(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val wobbleSpeed: Float
)

private data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val fallSpeed: Float,
    val swaySpeed: Float
)

private data class SmokeWisp(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val driftSpeed: Float
)

private data class Raindrop(
    val x: Float,
    val y: Float,
    val length: Float,
    val speed: Float
)

private data class Snowflake(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val wobbleSpeed: Float
)

private data class Firefly(
    val x: Float,
    val y: Float,
    val size: Float,
    val glowSpeed: Float,
    val moveSpeed: Float
)
