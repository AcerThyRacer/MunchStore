package com.sugarmunch.app.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

/**
 * Sugar Effects Engine - 15 Sugar-Specific Visual Effects
 * Complete implementations for all candy-themed effects
 */

// ═════════════════════════════════════════════════════════════════
// EFFECT MODELS
// ═════════════════════════════════════════════════════════════════

enum class SugarEffectType(
    val displayName: String,
    val description: String
) {
    CANDY_RAIN("Candy Rain", "Falling candies with physics"),
    CHOCOLATE_FOUNTAIN("Chocolate Fountain", "Flowing chocolate overlay"),
    CARAMEL_DRIZZLE("Caramel Drizzle", "Dripping caramel animation"),
    SPRINKLES_EXPLOSION("Sprinkles Explosion", "Particle burst on interaction"),
    COTTON_CANDY_CLOUD("Cotton Candy Cloud", "Fluffy cloud background"),
    LOLLIPOP_SPIN("Lollipop Spin", "Rotating spiral overlay"),
    GUMMY_WOBBLE("Gummy Wobble", "Jelly-like screen deformation"),
    SUGAR_RUSH_BLUR("Sugar Rush Blur", "Speed blur effect"),
    CANDY_TRANSFORM("Candy Transform", "Morphing UI elements"),
    SWEET_GLOW("Sweet Glow", "Pulsing glow around cards"),
    BUBBLE_POP("Bubble Pop", "Rising and popping bubbles"),
    RAINBOW_WAVE("Rainbow Wave", "Traveling rainbow gradient"),
    FIZZY_BUBBLES("Fizzy Bubbles", "Soda-like fizzing particles"),
    CANDY_CRUSH("Candy Crush", "Match-3 style tile animation"),
    SUGAR_HIGH("Sugar High", "Maximum intensity overload mode")
}

data class SugarEffectConfig(
    val type: SugarEffectType,
    val intensity: Float = 1f,
    val duration: Int = 5000,
    val enabled: Boolean = true,
    val customParams: Map<String, Float> = emptyMap()
)

// ═════════════════════════════════════════════════════════════════
// MAIN EFFECT COMPOSABLE
// ═════════════════════════════════════════════════════════════════

@Composable
fun SugarEffectOverlay(
    modifier: Modifier = Modifier,
    effect: SugarEffectConfig,
    onAnimationComplete: () -> Unit = {}
) {
    if (!effect.enabled) return

    when (effect.type) {
        SugarEffectType.CANDY_RAIN -> CandyRainEffect(modifier, effect.intensity)
        SugarEffectType.CHOCOLATE_FOUNTAIN -> ChocolateFountainEffect(modifier, effect.intensity)
        SugarEffectType.CARAMEL_DRIZZLE -> CaramelDrizzleEffect(modifier, effect.intensity)
        SugarEffectType.SPRINKLES_EXPLOSION -> SprinklesExplosionEffect(modifier, effect.intensity, onAnimationComplete)
        SugarEffectType.COTTON_CANDY_CLOUD -> CottonCandyCloudEffect(modifier, effect.intensity)
        SugarEffectType.LOLLIPOP_SPIN -> LollipopSpinEffect(modifier, effect.intensity)
        SugarEffectType.GUMMY_WOBBLE -> GummyWobbleEffect(modifier, effect.intensity)
        SugarEffectType.SUGAR_RUSH_BLUR -> SugarRushBlurEffect(modifier, effect.intensity)
        SugarEffectType.CANDY_TRANSFORM -> CandyTransformEffect(modifier, effect.intensity)
        SugarEffectType.SWEET_GLOW -> SweetGlowEffect(modifier, effect.intensity)
        SugarEffectType.BUBBLE_POP -> BubblePopEffect(modifier, effect.intensity)
        SugarEffectType.RAINBOW_WAVE -> RainbowWaveEffect(modifier, effect.intensity)
        SugarEffectType.FIZZY_BUBBLES -> FizzyBubblesEffect(modifier, effect.intensity)
        SugarEffectType.CANDY_CRUSH -> CandyCrushEffect(modifier, effect.intensity)
        SugarEffectType.SUGAR_HIGH -> SugarHighEffect(modifier, effect.intensity, onAnimationComplete)
    }
}

// ═════════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════════

data class Candy(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val color: Color,
    val rotation: Float
)

data class ChocolateStream(
    val x: Float,
    val width: Float,
    val speed: Float,
    val offset: Float
)

data class Bubble(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val wobble: Float
)

data class FizzParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val life: Float
)

data class RainbowSegment(
    val x: Float,
    val y: Float,
    val width: Float,
    val color: Color
)

data class Tile(
    val row: Int,
    val col: Int,
    val color: Color,
    val targetRow: Int,
    val targetCol: Int,
    val progress: Float
)

private val CandyColors = listOf(
    Color(0xFFFF69B4),  // Hot pink
    Color(0xFF00FFA3),  // Mint
    Color(0xFFFFD700),  // Gold
    Color(0xFF00BFFF),  // Deep sky blue
    Color(0xFFFF6347),  // Tomato
    Color(0xFF9370DB)   // Medium purple
)

private val RainbowColors = listOf(
    Color(0xFFFF0000),  // Red
    Color(0xFFFFA500),  // Orange
    Color(0xFFFFFF00),  // Yellow
    Color(0xFF00FF00),  // Green
    Color(0xFF00BFFF),  // Deep sky blue
    Color(0xFF8B00FF)   // Violet
)

// ═════════════════════════════════════════════════════════════════
// EFFECT IMPLEMENTATIONS
// ═════════════════════════════════════════════════════════════════

/**
 * 1. CANDY RAIN - Falling candies with physics
 */
@Composable
private fun CandyRainEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val candyCount = (10 + intensity * 20).toInt()
    val candies = remember {
        List(candyCount) { i ->
            Candy(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.5f - 0.5f,
                size = 10f + Random.nextFloat() * 20f,
                speed = 0.5f + Random.nextFloat() * 0.5f,
                color = CandyColors.random(),
                rotation = Random.nextFloat() * 360f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "candy_rain")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val height = size.height
        val width = size.width

        candies.forEach { candy ->
            val y = ((candy.y + time * 0.001f * candy.speed) % 1.5f) * height
            if (y > 0) {
                drawCandy(
                    center = Offset(candy.x * width, y),
                    size = candy.size,
                    color = candy.color,
                    rotation = candy.rotation + time * 0.1f
                )
            }
        }
    }
}

private fun DrawScope.drawCandy(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float
) {
    rotate(rotation) {
        drawCircle(color = color, radius = size, center = center)
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = size * 0.3f,
            center = Offset(center.x - size * 0.3f, center.y - size * 0.3f)
        )
    }
}

/**
 * 2. CHOCOLATE FOUNTAIN - Flowing chocolate overlay
 */
@Composable
private fun ChocolateFountainEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val streamCount = (5 + intensity * 10).toInt()
    val streams = remember {
        List(streamCount) { i ->
            ChocolateStream(
                x = i.toFloat() / streamCount,
                width = 0.05f + Random.nextFloat() * 0.05f,
                speed = 0.3f + Random.nextFloat() * 0.3f,
                offset = Random.nextFloat() * 100f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "chocolate")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width

        streams.forEach { stream ->
            val streamX = stream.x * width
            val streamWidth = stream.width * width

            repeat(5) { i ->
                val yOffset = (time * 0.001f * stream.speed + i * 0.2f + stream.offset) % 1f
                drawRect(
                    color = Color(0xFF3E2723).copy(alpha = 0.6f),
                    topLeft = Offset(streamX - streamWidth / 2, yOffset * size.height),
                    size = Size(streamWidth, size.height * 0.3f)
                )
            }
        }
    }
}

/**
 * 3. CARAMEL DRIZZLE - Dripping caramel animation
 */
@Composable
private fun CaramelDrizzleEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val dripCount = (8 + intensity * 12).toInt()

    val infiniteTransition = rememberInfiniteTransition(label = "caramel")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width

        repeat(dripCount) { i ->
            val dripX = (i + 0.5f) / dripCount * width
            val dripLength = (sin(time * 0.001f + i) * 0.3f + 0.5f) * size.height * 0.4f

            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(dripX - 10f, 0f)
                quadraticBezierTo(dripX, 0f, dripX, dripLength)
                quadraticBezierTo(dripX + 10f, 0f, dripX + 20f, 0f)
                close()
            }
            drawPath(path, Color(0xFFC68E59).copy(alpha = 0.7f))
        }
    }
}

/**
 * 4. SPRINKLES EXPLOSION - Particle burst on interaction
 */
@Composable
private fun SprinklesExplosionEffect(
    modifier: Modifier = Modifier,
    intensity: Float,
    onAnimationComplete: () -> Unit
) {
    val sprinkleCount = (30 + intensity * 50).toInt()

    LaunchedEffect(Unit) {
        delay(3000L)
        onAnimationComplete()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sprinkles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = tween(3000, easing = FastOutSlowInEasing),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        repeat(sprinkleCount) { i ->
            val angle = (i * 360f / sprinkleCount)
            val distance = time * size.minDimension * 0.4f
            val x = centerX + cos(angle * Math.PI / 180f).toFloat() * distance
            val y = centerY + sin(angle * Math.PI / 180f).toFloat() * distance

            drawSprinkle(
                center = Offset(x, y),
                length = 10f + intensity * 10f,
                width = 4f,
                color = CandyColors[i % CandyColors.size],
                rotation = angle
            )
        }
    }
}

private fun DrawScope.drawSprinkle(
    center: Offset,
    length: Float,
    width: Float,
    color: Color,
    rotation: Float
) {
    rotate(rotation) {
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - length / 2, center.y - width / 2),
            size = Size(length, width),
            cornerRadius = CornerRadius(width / 2)
        )
    }
}

/**
 * 5. COTTON CANDY CLOUD - Fluffy cloud background
 */
@Composable
private fun CottonCandyCloudEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cloud")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        repeat(5) { i ->
            val cloudX = (time * 0.0001f + i * 0.2f) % 1.2f * size.width - size.width * 0.1f
            val cloudY = size.height * (0.2f + i * 0.15f)
            val cloudSize = size.minDimension * (0.3f + intensity * 0.2f)

            drawCloud(
                center = Offset(cloudX, cloudY),
                size = cloudSize,
                color = Color(0xFFFFB6C1).copy(alpha = 0.3f + intensity * 0.2f)
            )
        }
    }
}

private fun DrawScope.drawCloud(
    center: Offset,
    size: Float,
    color: Color
) {
    drawCircle(color, size * 0.8f, Offset(center.x - size * 0.5f, center.y))
    drawCircle(color, size, center)
    drawCircle(color, size * 0.7f, Offset(center.x + size * 0.5f, center.y))
}

/**
 * 6. LOLLIPOP SPIN - Rotating spiral overlay
 */
@Composable
private fun LollipopSpinEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lollipop")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension * 0.5f

        rotate(angle) {
            repeat(12) { i ->
                val sweepAngle = 360f / 12
                drawArc(
                    color = RainbowColors[i % RainbowColors.size].copy(alpha = 0.3f * intensity),
                    startAngle = i * sweepAngle,
                    sweepAngle = sweepAngle - 2f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
        }
    }
}

/**
 * 7. GUMMY WOBBLE - Jelly-like screen deformation
 */
@Composable
private fun GummyWobbleEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wobble")
    val wobble by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val segments = 20
        val segmentHeight = size.height / segments

        repeat(segments) { i ->
            val offset = sin(wobble * Math.PI.toFloat() + i * 0.3f) * 10f * intensity
            drawRect(
                color = Color(0xFFFF1493).copy(alpha = 0.05f),
                topLeft = Offset(offset, i * segmentHeight),
                size = Size(size.width - abs(offset), segmentHeight + 1f)
            )
        }
    }
}

/**
 * 8. SUGAR RUSH BLUR - Speed blur effect
 */
@Composable
private fun SugarRushBlurEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blur")
    val blurOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f * intensity,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blur"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        repeat(10) { i ->
            val alpha = (1f - i / 10f) * 0.1f * intensity
            drawRect(
                color = Color(0xFFFFFF00).copy(alpha = alpha),
                topLeft = Offset(-blurOffset * (i / 10f), 0f),
                size = Size(size.width, size.height)
            )
        }
    }
}

/**
 * 9. CANDY TRANSFORM - Morphing UI elements
 */
@Composable
private fun CandyTransformEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "transform")
    val morph by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "morph"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val corners = listOf(
            Offset(0f, 0f),
            Offset(size.width, 0f),
            Offset(size.width, size.height),
            Offset(0f, size.height)
        )

        corners.forEachIndexed { i, corner ->
            val targetX = size.width / 2 + (corner.x - size.width / 2) * (1f - morph * 0.3f)
            val targetY = size.height / 2 + (corner.y - size.height / 2) * (1f - morph * 0.3f)
            drawCircle(
                color = CandyColors[i % CandyColors.size].copy(alpha = 0.2f * intensity),
                radius = 50f * intensity,
                center = Offset(targetX, targetY)
            )
        }
    }
}

/**
 * 10. SWEET GLOW - Pulsing glow around cards
 */
@Composable
private fun SweetGlowEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF69B4).copy(alpha = glow * 0.3f * intensity),
                    Color(0xFFFF69B4).copy(alpha = 0f)
                ),
                center = Offset(size.width / 2, size.height / 2),
                radius = size.minDimension * 0.7f
            )
        )
    }
}

/**
 * 11. BUBBLE POP - Rising and popping bubbles
 */
@Composable
private fun BubblePopEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val bubbleCount = (15 + intensity * 20).toInt()
    val bubbles = remember {
        List(bubbleCount) { i ->
            Bubble(
                x = Random.nextFloat(),
                y = Random.nextFloat() + 0.5f,
                size = 15f + Random.nextFloat() * 20f,
                speed = 0.3f + Random.nextFloat() * 0.4f,
                wobble = Random.nextFloat() * 100f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        bubbles.forEach { bubble ->
            val y = (1f - ((bubble.y + time * 0.001f * bubble.speed) % 1.5f)) * size.height
            val x = (bubble.x + sin(time * 0.002f + bubble.wobble) * 0.05f) * size.width

            if (y < size.height) {
                drawBubble(
                    center = Offset(x, y),
                    size = bubble.size,
                    color = Color(0xFF00BFFF).copy(alpha = 0.4f)
                )
            }
        }
    }
}

private fun DrawScope.drawBubble(
    center: Offset,
    size: Float,
    color: Color
) {
    drawCircle(
        color = color,
        radius = size,
        center = center,
        style = Stroke(width = 2f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = size * 0.3f,
        center = Offset(center.x - size * 0.3f, center.y - size * 0.3f)
    )
}

/**
 * 12. RAINBOW WAVE - Traveling rainbow gradient
 */
@Composable
private fun RainbowWaveEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val waveWidth = size.width * 0.3f
        val waveX = (time * size.width * 1.3f) - waveWidth

        repeat(RainbowColors.size) { i ->
            val offset = i * waveWidth * 0.3f
            drawRect(
                color = RainbowColors[i].copy(alpha = 0.3f * intensity),
                topLeft = Offset(waveX + offset - size.width * 0.1f, 0f),
                size = Size(waveWidth, size.height)
            )
        }
    }
}

/**
 * 13. FIZZY BUBBLES - Soda-like fizzing particles
 */
@Composable
private fun FizzyBubblesEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val fizzCount = (50 + intensity * 100).toInt()
    val fizzParticles = remember {
        List(fizzCount) { i ->
            FizzParticle(
                x = Random.nextFloat(),
                y = 1f,
                vx = (Random.nextFloat() - 0.5f) * 0.01f,
                vy = -(0.5f + Random.nextFloat() * 0.5f),
                size = 2f + Random.nextFloat() * 4f,
                life = Random.nextFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fizz")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        fizzParticles.forEach { particle ->
            val newX = (particle.x + particle.vx * time * 0.01f) % 1f
            val newY = (particle.y + particle.vy * time * 0.001f + particle.life) % 1.2f

            if (newY > 0 && newY < 1f) {
                drawCircle(
                    color = Color(0xFF00FFA3).copy(alpha = 0.5f),
                    radius = particle.size,
                    center = Offset(newX * size.width, newY * size.height)
                )
            }
        }
    }
}

/**
 * 14. CANDY CRUSH - Match-3 style tile animation
 */
@Composable
private fun CandyCrushEffect(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    val tileSize = 60f * intensity
    val cols = (size.width / tileSize).toInt().coerceAtMost(8)
    val rows = (size.height / tileSize).toInt().coerceAtMost(8)

    val infiniteTransition = rememberInfiniteTransition(label = "crush")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        repeat(rows) { row ->
            repeat(cols) { col ->
                val colorIndex = (row + col + (time * 0.001f).toInt()) % CandyColors.size
                val x = col * tileSize + (size.width - cols * tileSize) / 2
                val y = row * tileSize + (size.height - rows * tileSize) / 2

                drawRoundRect(
                    color = CandyColors[colorIndex].copy(alpha = 0.6f),
                    topLeft = Offset(x + 2f, y + 2f),
                    size = Size(tileSize - 4f, tileSize - 4f),
                    cornerRadius = CornerRadius(10f)
                )
            }
        }
    }
}

/**
 * 15. SUGAR HIGH - Maximum intensity overload mode
 */
@Composable
private fun SugarHighEffect(
    modifier: Modifier = Modifier,
    intensity: Float,
    onAnimationComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(5000L)
        onAnimationComplete()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sugar_high")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "time"
    )

    val hue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hue"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Rainbow background
        drawRect(
            color = Color.hsl(hue, 1f, 0.5f).copy(alpha = 0.2f * intensity)
        )

        // Pulsing circles
        repeat(5) { i ->
            val radius = (time * 0.05f + i * 100f) % size.minDimension
            drawCircle(
                color = RainbowColors[i % RainbowColors.size].copy(alpha = 0.1f * intensity),
                radius = radius,
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(width = 5f)
            )
        }

        // Sparkles
        repeat(20) { i ->
            val x = (sin(time * 0.001f + i) * 0.5f + 0.5f) * size.width
            val y = (cos(time * 0.0015f + i * 1.5f) * 0.5f + 0.5f) * size.height
            drawCircle(
                color = Color.White,
                radius = 3f * intensity,
                center = Offset(x, y)
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// EFFECT MANAGER
// ═════════════════════════════════════════════════════════════════

class SugarEffectManager private constructor() {
    companion object {
        @Volatile
        private var instance: SugarEffectManager? = null

        fun getInstance(): SugarEffectManager {
            return instance ?: synchronized(this) {
                instance ?: SugarEffectManager().also { instance = it }
            }
        }
    }

    private val activeEffects = mutableStateListOf<SugarEffectConfig>()

    fun addEffect(effect: SugarEffectConfig) {
        activeEffects.add(effect)
    }

    fun removeEffect(type: SugarEffectType) {
        activeEffects.removeAll { it.type == type }
    }

    fun getActiveEffects(): List<SugarEffectConfig> = activeEffects.toList()

    fun clearAllEffects() {
        activeEffects.clear()
    }

    fun isEffectActive(type: SugarEffectType): Boolean {
        return activeEffects.any { it.type == type && it.enabled }
    }
}
