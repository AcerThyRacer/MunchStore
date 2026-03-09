package com.sugarmunch.app.ui.feedback

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class CelebrationType {
    CONFETTI_BURST,
    FIREWORKS,
    CANDY_RAIN,
    STAR_SHOWER,
    LEVEL_UP,
    ACHIEVEMENT
}

private enum class ParticleShape { CIRCLE, RECT, STAR, LOLLIPOP, LINE }

private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var rotation: Float,
    var alpha: Float,
    var color: Color,
    var shape: ParticleShape,
    var life: Float,
    var rotationSpeed: Float = 0f,
    var phase: Float = 0f
)

private val CandyColors = listOf(
    Color(0xFFFFB6C1), // Pink
    Color(0xFF98FF98), // Mint
    Color(0xFFFFFACD), // Yellow
    Color(0xFFDEB887), // Caramel
    Color(0xFFB5DEFF), // Blue
    Color(0xFFE6B3FF), // Purple
    Color(0xFFFF6B6B), // Red
    Color(0xFF4ECDC4), // Teal
    Color(0xFFFFD93D), // Gold
    Color(0xFFFF69B4)  // Hot Pink
)

@Composable
fun CelebrationOverlay(
    type: CelebrationType,
    isActive: Boolean,
    duration: Long = 3000L,
    onComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    when (type) {
        CelebrationType.CONFETTI_BURST -> ConfettiBurstOverlay(duration, onComplete, modifier)
        CelebrationType.FIREWORKS -> FireworksOverlay(duration, onComplete, modifier)
        CelebrationType.CANDY_RAIN -> CandyRainOverlay(duration, onComplete, modifier)
        CelebrationType.STAR_SHOWER -> StarShowerOverlay(duration, onComplete, modifier)
        CelebrationType.LEVEL_UP -> LevelUpOverlay(duration, onComplete, modifier)
        CelebrationType.ACHIEVEMENT -> AchievementOverlay(duration, onComplete, modifier)
    }
}

// ── Particle simulation loop ────────────────────────────────────────────────────

@Composable
private fun rememberParticleLoop(
    duration: Long,
    onComplete: () -> Unit,
    particles: List<Particle>,
    updateParticle: (Particle, Float) -> Unit
): Long {
    var frameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        val startTime = withFrameMillis { it }
        var lastFrame = startTime
        while (true) {
            val now = withFrameMillis { it }
            val dt = ((now - lastFrame) / 1000f).coerceIn(0f, 0.05f)
            lastFrame = now
            frameTime = now

            val elapsed = now - startTime
            if (elapsed > duration) {
                onComplete()
                break
            }

            particles.forEach { p -> updateParticle(p, dt) }
        }
    }
    return frameTime
}

// ── CONFETTI_BURST ──────────────────────────────────────────────────────────────

@Composable
private fun ConfettiBurstOverlay(duration: Long, onComplete: () -> Unit, modifier: Modifier) {
    val density = LocalDensity.current
    val particles = remember {
        List(90) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.3f,
                vx = Random.nextFloat() * 0.4f - 0.2f,
                vy = Random.nextFloat() * 0.3f + 0.15f,
                size = Random.nextFloat() * 8f + 4f,
                rotation = Random.nextFloat() * 360f,
                alpha = 1f,
                color = CandyColors.random(),
                shape = if (Random.nextBoolean()) ParticleShape.RECT else ParticleShape.CIRCLE,
                life = 1f,
                rotationSpeed = Random.nextFloat() * 180f - 90f
            )
        }
    }

    rememberParticleLoop(duration, onComplete, particles) { p, dt ->
        p.vy += 0.5f * dt // gravity
        p.x += p.vx * dt
        p.y += p.vy * dt
        p.rotation += p.rotationSpeed * dt
        p.alpha = (1f - p.y).coerceIn(0f, 1f)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            if (p.alpha > 0f) {
                val px = p.x * size.width
                val py = p.y * size.height
                val s = with(density) { p.size.dp.toPx() }
                rotate(p.rotation, pivot = Offset(px, py)) {
                    when (p.shape) {
                        ParticleShape.CIRCLE -> drawCircle(p.color.copy(alpha = p.alpha), s / 2f, Offset(px, py))
                        ParticleShape.RECT -> drawRect(p.color.copy(alpha = p.alpha), Offset(px - s / 2, py - s / 2), androidx.compose.ui.geometry.Size(s, s * 0.6f))
                        else -> drawCircle(p.color.copy(alpha = p.alpha), s / 2f, Offset(px, py))
                    }
                }
            }
        }
    }
}

// ── FIREWORKS ───────────────────────────────────────────────────────────────────

@Composable
private fun FireworksOverlay(duration: Long, onComplete: () -> Unit, modifier: Modifier) {
    val density = LocalDensity.current
    val rocketCount = 4
    val burstSize = 30

    data class Rocket(
        var x: Float,
        var burstY: Float,
        var launched: Boolean = false,
        var exploded: Boolean = false,
        var rocketY: Float = 1.1f,
        var color: Color,
        var launchDelay: Long
    )

    val rockets = remember {
        List(rocketCount) { i ->
            Rocket(
                x = Random.nextFloat() * 0.6f + 0.2f,
                burstY = Random.nextFloat() * 0.25f + 0.15f,
                color = CandyColors.random(),
                launchDelay = i * 400L
            )
        }
    }

    val particles = remember { mutableListOf<Particle>() }

    LaunchedEffect(Unit) {
        val startTime = withFrameMillis { it }
        var lastFrame = startTime
        while (true) {
            val now = withFrameMillis { it }
            val dt = ((now - lastFrame) / 1000f).coerceIn(0f, 0.05f)
            lastFrame = now
            val elapsed = now - startTime

            if (elapsed > duration) { onComplete(); break }

            rockets.forEach { r ->
                if (!r.launched && elapsed >= r.launchDelay) r.launched = true
                if (r.launched && !r.exploded) {
                    r.rocketY -= 1.2f * dt
                    if (r.rocketY <= r.burstY) {
                        r.exploded = true
                        repeat(burstSize) {
                            val angle = (it.toFloat() / burstSize) * 2f * PI.toFloat()
                            val speed = Random.nextFloat() * 0.4f + 0.2f
                            particles.add(
                                Particle(
                                    x = r.x, y = r.burstY,
                                    vx = cos(angle) * speed, vy = sin(angle) * speed,
                                    size = Random.nextFloat() * 4f + 2f,
                                    rotation = 0f, alpha = 1f,
                                    color = r.color,
                                    shape = ParticleShape.CIRCLE, life = 1f
                                )
                            )
                        }
                    }
                }
            }

            val iter = particles.iterator()
            while (iter.hasNext()) {
                val p = iter.next()
                p.vy += 0.3f * dt
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.life -= dt * 0.8f
                p.alpha = p.life.coerceIn(0f, 1f)
                if (p.life <= 0f) iter.remove()
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw rising rockets
        rockets.forEach { r ->
            if (r.launched && !r.exploded) {
                val px = r.x * size.width
                val py = r.rocketY * size.height
                drawCircle(r.color, 4.dp.toPx(), Offset(px, py))
                drawCircle(Color.White.copy(alpha = 0.6f), 2.dp.toPx(), Offset(px, py))
            }
        }
        // Draw explosion particles
        particles.forEach { p ->
            if (p.alpha > 0f) {
                val px = p.x * size.width
                val py = p.y * size.height
                drawCircle(p.color.copy(alpha = p.alpha), with(density) { p.size.dp.toPx() } / 2f, Offset(px, py))
            }
        }
    }
}

// ── CANDY_RAIN ──────────────────────────────────────────────────────────────────

@Composable
private fun CandyRainOverlay(duration: Long, onComplete: () -> Unit, modifier: Modifier) {
    val density = LocalDensity.current
    val particles = remember {
        List(60) {
            val candyShape = listOf(ParticleShape.CIRCLE, ParticleShape.RECT, ParticleShape.LOLLIPOP).random()
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f - 0.05f,
                vx = 0f,
                vy = Random.nextFloat() * 0.12f + 0.06f,
                size = Random.nextFloat() * 8f + 5f,
                rotation = Random.nextFloat() * 360f,
                alpha = 1f,
                color = CandyColors.random(),
                shape = candyShape,
                life = 1f,
                rotationSpeed = Random.nextFloat() * 60f - 30f,
                phase = Random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }

    rememberParticleLoop(duration, onComplete, particles) { p, dt ->
        p.y += p.vy * dt
        p.x += sin(p.phase + p.y * 4f) * 0.002f // sway
        p.rotation += p.rotationSpeed * dt
        p.alpha = if (p.y > 0.85f) (1f - (p.y - 0.85f) / 0.15f).coerceIn(0f, 1f) else 1f
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            if (p.alpha > 0f && p.y < 1.1f) {
                val px = p.x * size.width
                val py = p.y * size.height
                val s = with(density) { p.size.dp.toPx() }
                rotate(p.rotation, pivot = Offset(px, py)) {
                    when (p.shape) {
                        ParticleShape.CIRCLE -> drawCircle(p.color.copy(alpha = p.alpha), s / 2f, Offset(px, py))
                        ParticleShape.RECT -> drawRect(p.color.copy(alpha = p.alpha), Offset(px - s / 2, py - s * 0.3f), androidx.compose.ui.geometry.Size(s, s * 0.6f))
                        ParticleShape.LOLLIPOP -> {
                            drawCircle(p.color.copy(alpha = p.alpha), s * 0.4f, Offset(px, py - s * 0.3f))
                            drawLine(p.color.copy(alpha = p.alpha * 0.8f), Offset(px, py - s * 0.1f), Offset(px, py + s * 0.5f), strokeWidth = s * 0.12f)
                        }
                        else -> drawCircle(p.color.copy(alpha = p.alpha), s / 2f, Offset(px, py))
                    }
                }
            }
        }
    }
}

// ── STAR_SHOWER ─────────────────────────────────────────────────────────────────

@Composable
private fun StarShowerOverlay(duration: Long, onComplete: () -> Unit, modifier: Modifier) {
    val density = LocalDensity.current
    val goldColors = listOf(Color(0xFFFFD700), Color(0xFFFFC107), Color(0xFFFFEB3B), Color(0xFFFFF176))

    val particles = remember {
        List(50) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.4f - 0.05f,
                vx = Random.nextFloat() * 0.06f - 0.03f,
                vy = Random.nextFloat() * 0.18f + 0.08f,
                size = Random.nextFloat() * 10f + 6f,
                rotation = Random.nextFloat() * 360f,
                alpha = 1f,
                color = goldColors.random(),
                shape = ParticleShape.STAR,
                life = 1f,
                rotationSpeed = Random.nextFloat() * 120f - 60f,
                phase = Random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }

    var frameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        val startTime = withFrameMillis { it }
        var lastFrame = startTime
        while (true) {
            val now = withFrameMillis { it }
            val dt = ((now - lastFrame) / 1000f).coerceIn(0f, 0.05f)
            lastFrame = now
            frameTime = now
            if (now - startTime > duration) { onComplete(); break }

            particles.forEach { p ->
                p.y += p.vy * dt
                p.x += p.vx * dt
                p.rotation += p.rotationSpeed * dt
                // Sparkle: oscillate alpha
                p.alpha = (0.6f + 0.4f * sin(p.phase + (now / 200f))).coerceIn(0f, 1f) *
                    if (p.y > 0.85f) ((1f - p.y) / 0.15f).coerceIn(0f, 1f) else 1f
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            if (p.alpha > 0f && p.y < 1.1f) {
                val px = p.x * size.width
                val py = p.y * size.height
                val s = with(density) { p.size.dp.toPx() }
                rotate(p.rotation, pivot = Offset(px, py)) {
                    drawStar(px, py, s / 2f, p.color.copy(alpha = p.alpha))
                }
            }
        }
    }
}

private fun DrawScope.drawStar(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.4f
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = (i * 36f - 90f) * (PI.toFloat() / 180f)
        val x = cx + r * cos(angle)
        val y = cy + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Fill)
}

// ── LEVEL_UP ────────────────────────────────────────────────────────────────────

@Composable
private fun LevelUpOverlay(duration: Long, onComplete: () -> Unit, modifier: Modifier) {
    val density = LocalDensity.current
    val circleScale = remember { Animatable(0f) }
    val textScale = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }

    val burstParticles = remember {
        List(40) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 0.5f + 0.2f
            Particle(
                x = 0.5f, y = 0.5f,
                vx = cos(angle) * speed, vy = sin(angle) * speed,
                size = Random.nextFloat() * 6f + 3f,
                rotation = 0f, alpha = 1f,
                color = CandyColors.random(),
                shape = ParticleShape.CIRCLE, life = 1f
            )
        }
    }

    LaunchedEffect(Unit) {
        circleScale.animateTo(1.5f, tween(500))
        textScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))

        val start = withFrameMillis { it }
        var last = start
        while (true) {
            val now = withFrameMillis { it }
            val dt = ((now - last) / 1000f).coerceIn(0f, 0.05f)
            last = now
            if (now - start > duration - 500) break
            burstParticles.forEach { p ->
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.life -= dt * 0.6f
                p.alpha = p.life.coerceIn(0f, 1f)
            }
        }

        overlayAlpha.animateTo(0f, tween(400))
        onComplete()
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Expanding circle
            val cR = circleScale.value * size.minDimension
            drawCircle(
                Color(0xFF6C63FF).copy(alpha = overlayAlpha.value * 0.3f),
                radius = cR,
                center = center
            )

            // Burst particles
            burstParticles.forEach { p ->
                if (p.alpha > 0f) {
                    val px = p.x * size.width
                    val py = p.y * size.height
                    val s = with(density) { p.size.dp.toPx() }
                    drawCircle(p.color.copy(alpha = p.alpha * overlayAlpha.value), s / 2f, Offset(px, py))
                }
            }
        }

        Text(
            text = "LEVEL UP!",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp,
                color = Color(0xFFFFD700).copy(alpha = overlayAlpha.value)
            ),
            modifier = Modifier.offset(y = (-20).dp)
        )
    }
}

// ── ACHIEVEMENT ─────────────────────────────────────────────────────────────────

@Composable
private fun AchievementOverlay(duration: Long, onComplete: () -> Unit, modifier: Modifier) {
    val density = LocalDensity.current
    val overlayAlpha = remember { Animatable(0f) }
    val trophyScale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    val goldenParticles = remember {
        List(30) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 0.25f + 0.05f
            Particle(
                x = 0.5f, y = 0.4f,
                vx = cos(angle) * speed, vy = sin(angle) * speed - 0.1f,
                size = Random.nextFloat() * 5f + 2f,
                rotation = 0f, alpha = 1f,
                color = listOf(Color(0xFFFFD700), Color(0xFFFFC107), Color(0xFFFFF176)).random(),
                shape = ParticleShape.CIRCLE, life = 1f
            )
        }
    }

    LaunchedEffect(Unit) {
        overlayAlpha.animateTo(1f, tween(300))
        trophyScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        textAlpha.animateTo(1f, tween(400))

        val start = withFrameMillis { it }
        var last = start
        while (true) {
            val now = withFrameMillis { it }
            val dt = ((now - last) / 1000f).coerceIn(0f, 0.05f)
            last = now
            if (now - start > duration - 500) break
            goldenParticles.forEach { p ->
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.vy += 0.15f * dt
                p.life -= dt * 0.5f
                p.alpha = p.life.coerceIn(0f, 1f)
            }
        }

        overlayAlpha.animateTo(0f, tween(400))
        onComplete()
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Dark overlay
            drawRect(Color.Black.copy(alpha = overlayAlpha.value * 0.5f))

            // Trophy drawn with Canvas paths
            val trophyCx = size.width / 2f
            val trophyCy = size.height * 0.38f
            val tScale = trophyScale.value
            val tAlpha = overlayAlpha.value

            scale(tScale, pivot = Offset(trophyCx, trophyCy)) {
                val tColor = Color(0xFFFFD700).copy(alpha = tAlpha)
                // Cup body
                val cupWidth = 80.dp.toPx()
                val cupHeight = 60.dp.toPx()
                val path = Path().apply {
                    moveTo(trophyCx - cupWidth / 2, trophyCy - cupHeight / 2)
                    lineTo(trophyCx + cupWidth / 2, trophyCy - cupHeight / 2)
                    lineTo(trophyCx + cupWidth * 0.35f, trophyCy + cupHeight / 2)
                    lineTo(trophyCx - cupWidth * 0.35f, trophyCy + cupHeight / 2)
                    close()
                }
                drawPath(path, tColor)
                // Stem
                val stemW = 12.dp.toPx()
                val stemH = 20.dp.toPx()
                drawRect(tColor, Offset(trophyCx - stemW / 2, trophyCy + cupHeight / 2), androidx.compose.ui.geometry.Size(stemW, stemH))
                // Base
                val baseW = 50.dp.toPx()
                val baseH = 8.dp.toPx()
                drawRect(tColor, Offset(trophyCx - baseW / 2, trophyCy + cupHeight / 2 + stemH), androidx.compose.ui.geometry.Size(baseW, baseH))
                // Star on trophy
                drawStar(trophyCx, trophyCy, 14.dp.toPx(), Color.White.copy(alpha = tAlpha * 0.9f))
            }

            // Golden particles
            goldenParticles.forEach { p ->
                if (p.alpha > 0f) {
                    val px = p.x * size.width
                    val py = p.y * size.height
                    val s = with(density) { p.size.dp.toPx() }
                    drawCircle(p.color.copy(alpha = p.alpha * tAlpha), s / 2f, Offset(px, py))
                }
            }
        }

        Text(
            text = "Achievement Unlocked!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700).copy(alpha = textAlpha.value * overlayAlpha.value)
            ),
            modifier = Modifier.offset(y = 80.dp)
        )
    }
}
