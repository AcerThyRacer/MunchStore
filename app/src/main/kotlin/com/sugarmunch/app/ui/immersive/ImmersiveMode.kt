package com.sugarmunch.app.ui.immersive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════
// Phase 5.6 — Immersive Visual Experiences & Theater Mode
// ═══════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────
// 1. ImmersiveHeroHeader
// ─────────────────────────────────────────────────────────────────

/**
 * Full-width hero area with a vertical gradient and parallax.
 * [scrollOffset] is the current scroll pixel offset — the header
 * translates at 0.5× speed for a parallax effect.
 */
@Composable
fun ImmersiveHeroHeader(
    imageColor: Color,
    title: String,
    subtitle: String,
    scrollOffset: Int = 0,
    modifier: Modifier = Modifier
) {
    val parallaxOffset = (scrollOffset * 0.5f).roundToInt()
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Title fades in and scales as the user scrolls back toward the top
    val titleAlpha = (1f - (scrollOffset / 600f)).coerceIn(0f, 1f)
    val titleScale = (1f - (scrollOffset / 2000f)).coerceIn(0.85f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .offset { IntOffset(0, -parallaxOffset) }
            .background(
                Brush.verticalGradient(
                    colors = listOf(imageColor, surfaceColor)
                )
            ),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .alpha(titleAlpha)
                .scale(titleScale)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 2. ImmersiveDetailScreen
// ─────────────────────────────────────────────────────────────────

/**
 * Full detail-screen template: hero at top with collapsing header,
 * overlapping content card, and sample sections inside a [LazyColumn].
 */
@Composable
fun ImmersiveDetailScreen(
    heroColor: Color,
    title: String,
    subtitle: String,
    description: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Derive the scroll offset from the first visible item
    val scrollOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset
            } else {
                Int.MAX_VALUE
            }
        }
    }

    // Toolbar fades in once the header is mostly scrolled away
    val toolbarAlpha = ((scrollOffset - 200f) / 200f).coerceIn(0f, 1f)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero header item
            item {
                ImmersiveHeroHeader(
                    imageColor = heroColor,
                    title = title,
                    subtitle = subtitle,
                    scrollOffset = scrollOffset
                )
            }

            // Overlapping content card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Description section
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Screenshots row placeholder
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Screenshots",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(5) { index ->
                            Card(
                                modifier = Modifier.size(width = 160.dp, height = 100.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = heroColor.copy(
                                        alpha = 0.2f + index * 0.15f
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text("Shot ${index + 1}", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Stats row
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("4.8", "Rating")
                    StatItem("12K", "Downloads")
                    StatItem("86", "Levels")
                    StatItem("2.1", "Version")
                }
            }

            // Action buttons
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { /* play action */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Now")
                    }
                    OutlinedButton(
                        onClick = { /* share action */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Favorite")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Fade-in toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(toolbarAlpha)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// 3. TheaterMode — full-screen visual showcase
// ─────────────────────────────────────────────────────────────────

private enum class TheaterScene(val label: String) {
    STARFIELD("Starfield"),
    CANDY_RAIN("Candy Rain"),
    AURORA("Aurora"),
    FIREFLY_FOREST("Firefly Forest"),
    NEON_GRID("Neon Grid"),
    OCEAN_WAVES("Ocean Waves")
}

/**
 * Full-screen dark showcase cycling through six Canvas-drawn scenes.
 * Touch to pause/resume; tap reveals the exit button.
 */
@Composable
fun TheaterMode(
    sceneDurationMs: Long = 8_000L,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var paused by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(false) }
    var currentSceneIndex by remember { mutableIntStateOf(0) }
    var timeInScene by remember { mutableLongStateOf(0L) }
    var globalTime by remember { mutableFloatStateOf(0f) }
    var crossFadeAlpha by remember { mutableFloatStateOf(1f) }

    val scenes = TheaterScene.entries
    val currentScene = scenes[currentSceneIndex]
    val nextScene = scenes[(currentSceneIndex + 1) % scenes.size]

    // Stars, particles, etc. — generated once and reused
    val stars = remember { List(120) { Offset(Random.nextFloat(), Random.nextFloat()) } }
    val candies = remember { List(40) { CandyParticle() } }
    val fireflies = remember { List(50) { FireflyParticle() } }
    val oceanPhases = remember { FloatArray(6) { Random.nextFloat() * 2f * PI.toFloat() } }

    // 60 fps animation loop
    LaunchedEffect(paused) {
        if (paused) return@LaunchedEffect
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                val deltaNanos = nanos - lastNanos
                lastNanos = nanos
                val deltaMs = deltaNanos / 1_000_000L
                val deltaSec = deltaMs / 1000f

                globalTime += deltaSec
                timeInScene += deltaMs

                // Cross-fade logic: last 1 000 ms of each scene
                val fadeStart = sceneDurationMs - 1_000L
                crossFadeAlpha = if (timeInScene >= fadeStart) {
                    1f - ((timeInScene - fadeStart).toFloat() / 1_000f).coerceIn(0f, 1f)
                } else {
                    1f
                }

                if (timeInScene >= sceneDurationMs) {
                    currentSceneIndex = (currentSceneIndex + 1) % scenes.size
                    timeInScene = 0L
                    crossFadeAlpha = 1f
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    if (showControls) {
                        paused = !paused
                    }
                    showControls = !showControls
                }
            }
    ) {
        // Current scene at full or fading alpha
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTheaterScene(currentScene, globalTime, crossFadeAlpha, stars, candies, fireflies, oceanPhases)
        }

        // Next scene bleeding in during cross-fade
        if (crossFadeAlpha < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawTheaterScene(nextScene, globalTime, 1f - crossFadeAlpha, stars, candies, fireflies, oceanPhases)
            }
        }

        // Clock in top-right corner
        val timeText = remember(globalTime.toInt()) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }
        Text(
            text = timeText,
            color = Color.White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        // Scene label
        Text(
            text = currentScene.label,
            color = Color.White.copy(alpha = 0.3f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )

        // Controls overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                Text(
                    text = if (paused) "Paused — tap to resume" else "Tap to pause",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exit Theater")
                }
            }
        }
    }
}

// ── Theater scene renderers ──────────────────────────────────────

private data class CandyParticle(
    val x: Float = Random.nextFloat(),
    val speed: Float = 0.06f + Random.nextFloat() * 0.12f,
    val size: Float = 6f + Random.nextFloat() * 10f,
    val hueShift: Float = Random.nextFloat() * 360f
)

private data class FireflyParticle(
    val cx: Float = Random.nextFloat(),
    val cy: Float = Random.nextFloat(),
    val radius: Float = 40f + Random.nextFloat() * 80f,
    val speed: Float = 0.3f + Random.nextFloat() * 0.7f,
    val phase: Float = Random.nextFloat() * 2f * PI.toFloat()
)

private fun DrawScope.drawTheaterScene(
    scene: TheaterScene,
    time: Float,
    alpha: Float,
    stars: List<Offset>,
    candies: List<CandyParticle>,
    fireflies: List<FireflyParticle>,
    oceanPhases: FloatArray
) {
    when (scene) {
        TheaterScene.STARFIELD -> drawStarfield(time, alpha, stars)
        TheaterScene.CANDY_RAIN -> drawCandyRain(time, alpha, candies)
        TheaterScene.AURORA -> drawAurora(time, alpha)
        TheaterScene.FIREFLY_FOREST -> drawFireflyForest(time, alpha, fireflies)
        TheaterScene.NEON_GRID -> drawNeonGrid(time, alpha)
        TheaterScene.OCEAN_WAVES -> drawOceanWaves(time, alpha, oceanPhases)
    }
}

private fun DrawScope.drawStarfield(time: Float, alpha: Float, stars: List<Offset>) {
    stars.forEachIndexed { i, star ->
        val twinkle = (sin(time * (1.5f + i * 0.05f)) * 0.5f + 0.5f)
        val brightness = twinkle * alpha
        val sz = 1.5f + (i % 4) * 0.8f
        drawCircle(
            color = Color.White.copy(alpha = brightness.coerceIn(0f, 1f)),
            radius = sz,
            center = Offset(star.x * size.width, star.y * size.height)
        )
    }
    // Shooting star
    val shootX = ((time * 200f) % (size.width + 400f)) - 200f
    val shootY = size.height * 0.2f + sin(time * 0.5f) * 60f
    drawLine(
        color = Color.White.copy(alpha = 0.6f * alpha),
        start = Offset(shootX, shootY),
        end = Offset(shootX - 60f, shootY + 20f),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawCandyRain(time: Float, alpha: Float, candies: List<CandyParticle>) {
    val candyColors = listOf(
        Color(0xFFFF69B4), Color(0xFF00CED1), Color(0xFFFFD700),
        Color(0xFFFF6347), Color(0xFF9370DB), Color(0xFF32CD32)
    )
    candies.forEachIndexed { i, candy ->
        val y = ((time * candy.speed * size.height) + candy.hueShift * size.height) % (size.height + candy.size * 2) - candy.size
        val x = candy.x * size.width + sin(time + i.toFloat()) * 20f
        val color = candyColors[i % candyColors.size].copy(alpha = 0.8f * alpha)
        // Draw as small rounded rect (candy shape)
        drawRoundRect(
            color = color,
            topLeft = Offset(x - candy.size / 2, y - candy.size / 2),
            size = Size(candy.size, candy.size * 1.4f),
            cornerRadius = CornerRadius(candy.size * 0.3f)
        )
    }
}

private fun DrawScope.drawAurora(time: Float, alpha: Float) {
    val auroraColors = listOf(
        Color(0xFF00FF88), Color(0xFF00CCFF), Color(0xFF8844FF), Color(0xFFFF44AA)
    )
    for (band in 0..3) {
        val baseY = size.height * (0.2f + band * 0.12f)
        val path = Path().apply {
            moveTo(0f, baseY)
            var x = 0f
            while (x <= size.width) {
                val y = baseY + sin(x * 0.005f + time * (0.5f + band * 0.2f) + band) * (40f + band * 15f)
                lineTo(x, y)
                x += 4f
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = path,
            color = auroraColors[band].copy(alpha = 0.15f * alpha)
        )
    }
}

private fun DrawScope.drawFireflyForest(time: Float, alpha: Float, fireflies: List<FireflyParticle>) {
    // Dark forest ground
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0A1A0A), Color(0xFF1A2E1A)),
            startY = size.height * 0.6f,
            endY = size.height
        ),
        topLeft = Offset(0f, size.height * 0.6f),
        size = Size(size.width, size.height * 0.4f),
        alpha = alpha
    )
    // Simple tree silhouettes
    for (i in 0..7) {
        val tx = size.width * (i.toFloat() / 8f + 0.05f)
        val treeH = size.height * (0.25f + (i % 3) * 0.08f)
        val baseY = size.height * 0.65f
        drawLine(
            color = Color(0xFF0D1F0D).copy(alpha = alpha),
            start = Offset(tx, baseY),
            end = Offset(tx, baseY - treeH),
            strokeWidth = 3f
        )
        // Triangular canopy
        val path = Path().apply {
            moveTo(tx, baseY - treeH)
            lineTo(tx - 25f, baseY - treeH * 0.4f)
            lineTo(tx + 25f, baseY - treeH * 0.4f)
            close()
        }
        drawPath(path = path, color = Color(0xFF143214).copy(alpha = alpha))
    }

    fireflies.forEach { fly ->
        val px = fly.cx * size.width + cos(time * fly.speed + fly.phase) * fly.radius
        val py = fly.cy * size.height + sin(time * fly.speed * 0.7f + fly.phase) * fly.radius * 0.6f
        val glow = (sin(time * 2f + fly.phase) * 0.5f + 0.5f) * alpha
        drawCircle(
            color = Color(0xFFFFFF66).copy(alpha = glow * 0.6f),
            radius = 8f,
            center = Offset(px, py)
        )
        drawCircle(
            color = Color(0xFFFFFF99).copy(alpha = glow),
            radius = 3f,
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawNeonGrid(time: Float, alpha: Float) {
    val gridSpacing = 50f
    val vanishY = size.height * 0.4f
    val neonPink = Color(0xFFFF00FF).copy(alpha = 0.4f * alpha)
    val neonCyan = Color(0xFF00FFFF).copy(alpha = 0.3f * alpha)

    // Horizontal lines moving toward viewer (perspective)
    for (i in 0..20) {
        val rawY = vanishY + i * gridSpacing * 0.8f + (time * 40f) % gridSpacing
        if (rawY > size.height) continue
        val lineAlpha = ((rawY - vanishY) / (size.height - vanishY)).coerceIn(0f, 1f) * alpha
        drawLine(
            color = neonPink.copy(alpha = 0.5f * lineAlpha),
            start = Offset(0f, rawY),
            end = Offset(size.width, rawY),
            strokeWidth = 1.5f
        )
    }

    // Vertical lines converging to center horizon
    val cx = size.width / 2f
    for (i in -10..10) {
        val baseX = cx + i * gridSpacing
        drawLine(
            color = neonCyan,
            start = Offset(baseX, size.height),
            end = Offset(cx + (baseX - cx) * 0.1f, vanishY),
            strokeWidth = 1f
        )
    }

    // Neon sun at vanishing point
    val sunRadius = 30f + sin(time * 0.8f) * 5f
    drawCircle(
        color = Color(0xFFFF6600).copy(alpha = 0.6f * alpha),
        radius = sunRadius,
        center = Offset(cx, vanishY - 10f)
    )
    // Sun glow
    drawCircle(
        color = Color(0xFFFF6600).copy(alpha = 0.1f * alpha),
        radius = sunRadius * 3f,
        center = Offset(cx, vanishY - 10f)
    )
}

private fun DrawScope.drawOceanWaves(time: Float, alpha: Float, phases: FloatArray) {
    val waveColors = listOf(
        Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1976D2),
        Color(0xFF1E88E5), Color(0xFF2196F3), Color(0xFF42A5F5)
    )

    for (layer in waveColors.indices) {
        val baseY = size.height * (0.35f + layer * 0.1f)
        val amp = 20f + layer * 6f
        val freq = 0.008f - layer * 0.0005f
        val speed = 0.8f + layer * 0.15f
        val phase = phases.getOrElse(layer) { 0f }

        val path = Path().apply {
            moveTo(0f, size.height)
            var x = 0f
            while (x <= size.width) {
                val y = baseY + sin(x * freq + time * speed + phase) * amp +
                    cos(x * freq * 1.5f + time * speed * 0.7f) * amp * 0.4f
                lineTo(x, y)
                x += 3f
            }
            lineTo(size.width, size.height)
            close()
        }
        drawPath(
            path = path,
            color = waveColors[layer].copy(alpha = (0.4f + layer * 0.08f) * alpha)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// 4. AmbientMode
// ─────────────────────────────────────────────────────────────────

private data class AmbientParticle(
    val x: Float = Random.nextFloat(),
    val y: Float = Random.nextFloat(),
    val vx: Float = (Random.nextFloat() - 0.5f) * 0.02f,
    val vy: Float = (Random.nextFloat() - 0.5f) * 0.015f,
    val radius: Float = 2f + Random.nextFloat() * 3f,
    val phase: Float = Random.nextFloat() * 2f * PI.toFloat()
)

/**
 * Minimal ambient display for dock/stand usage.
 * Dim elements to preserve OLED. Tap to exit.
 */
@Composable
fun AmbientMode(
    nextRewardText: String = "Next reward in 3h 42m",
    showClock: Boolean = true,
    showReward: Boolean = true,
    particleIntensity: Float = 0.5f,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimAlpha = 0.35f
    var globalTime by remember { mutableFloatStateOf(0f) }
    val particles = remember {
        val count = (20 * particleIntensity).toInt().coerceAtLeast(5)
        List(count) { AmbientParticle() }
    }

    val timeText = remember(globalTime.toInt()) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    val dateText = remember(globalTime.toInt()) {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    // 60 fps animation loop
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                val delta = (nanos - lastNanos) / 1_000_000_000f
                lastNanos = nanos
                globalTime += delta
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onExit() },
        contentAlignment = Alignment.Center
    ) {
        // Particle background
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEachIndexed { i, p ->
                val px = ((p.x + p.vx * globalTime) % 1f + 1f) % 1f
                val py = ((p.y + p.vy * globalTime) % 1f + 1f) % 1f
                val colorCycle = (sin(globalTime * 0.3f + p.phase) * 0.5f + 0.5f)
                val particleColor = Color(
                    red = 0.3f + colorCycle * 0.2f,
                    green = 0.3f + (1f - colorCycle) * 0.2f,
                    blue = 0.5f + colorCycle * 0.1f,
                    alpha = dimAlpha * 0.6f * particleIntensity
                )
                drawCircle(
                    color = particleColor,
                    radius = p.radius,
                    center = Offset(px * size.width, py * size.height)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showClock) {
                Text(
                    text = timeText,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Thin,
                    color = Color.White.copy(alpha = dimAlpha),
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = dimAlpha * 0.7f)
                )
            }

            if (showReward) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = nextRewardText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFD700).copy(alpha = dimAlpha * 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Tap to exit",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = dimAlpha * 0.3f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 5. CinematicTransition
// ─────────────────────────────────────────────────────────────────

/**
 * Reusable transition wrapper: content enters with a "camera zoom"
 * effect — starts at 1.1× scale and settles to 1.0× with
 * deceleration easing, combined with a fade-in.
 */
@Composable
fun CinematicTransition(
    modifier: Modifier = Modifier,
    durationMs: Int = 600,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(1.1f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Run scale and fade in parallel via coroutine scope
        kotlinx.coroutines.launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = durationMs,
                    easing = FastOutSlowInEasing
                )
            )
        }
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = (durationMs * 0.7f).toInt(),
                easing = LinearEasing
            )
        )
    }

    Box(
        modifier = modifier
            .scale(scale.value)
            .alpha(alpha.value)
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────────
// 6. ImmersiveSettingsPanel
// ─────────────────────────────────────────────────────────────────

/**
 * Configuration for immersive features.
 */
data class ImmersiveSettings(
    val sceneOrder: List<String> = TheaterScene.entries.map { it.label },
    val sceneDurationSeconds: Int = 8,
    val showAmbientClock: Boolean = true,
    val showAmbientRewards: Boolean = true,
    val particleIntensity: Float = 0.5f,
    val heroParallaxEnabled: Boolean = true
)

/**
 * Full settings panel for immersive features.
 */
@Composable
fun ImmersiveSettingsPanel(
    settings: ImmersiveSettings,
    onSettingsChange: (ImmersiveSettings) -> Unit,
    onLaunchTheater: () -> Unit,
    onLaunchAmbient: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Immersive Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        // ── Theater Mode Scene Order ──
        Text(
            text = "Theater Mode — Scene Order",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Drag to reorder scenes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        settings.sceneOrder.forEachIndexed { index, sceneName ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DragHandle,
                        contentDescription = "Drag to reorder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        text = sceneName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    SceneThumbnail(sceneName)
                }
            }
        }

        HorizontalDivider()

        // ── Scene Duration Slider ──
        Text(
            text = "Scene Duration: ${settings.sceneDurationSeconds}s",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = settings.sceneDurationSeconds.toFloat(),
            onValueChange = {
                onSettingsChange(settings.copy(sceneDurationSeconds = it.roundToInt()))
            },
            valueRange = 5f..30f,
            steps = 24
        )

        HorizontalDivider()

        // ── Ambient Mode Settings ──
        Text(
            text = "Ambient Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        SettingsToggle(
            title = "Show Clock",
            checked = settings.showAmbientClock,
            onCheckedChange = {
                onSettingsChange(settings.copy(showAmbientClock = it))
            }
        )

        SettingsToggle(
            title = "Show Reward Countdown",
            checked = settings.showAmbientRewards,
            onCheckedChange = {
                onSettingsChange(settings.copy(showAmbientRewards = it))
            }
        )

        Text(
            text = "Particle Intensity: %.0f%%".format(settings.particleIntensity * 100f),
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = settings.particleIntensity,
            onValueChange = {
                onSettingsChange(settings.copy(particleIntensity = it))
            },
            valueRange = 0f..1f
        )

        HorizontalDivider()

        // ── Immersive Detail Pages ──
        SettingsToggle(
            title = "Hero Parallax Effect",
            checked = settings.heroParallaxEnabled,
            onCheckedChange = {
                onSettingsChange(settings.copy(heroParallaxEnabled = it))
            }
        )

        HorizontalDivider()

        // ── Launch Buttons ──
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLaunchTheater,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Movie, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Launch Theater Mode")
        }

        OutlinedButton(
            onClick = onLaunchAmbient,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.NightsStay, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Launch Ambient Mode")
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SceneThumbnail(sceneName: String) {
    val color = when (sceneName) {
        "Starfield" -> Color(0xFF1A237E)
        "Candy Rain" -> Color(0xFFFF69B4)
        "Aurora" -> Color(0xFF00C853)
        "Firefly Forest" -> Color(0xFF1B5E20)
        "Neon Grid" -> Color(0xFFAA00FF)
        "Ocean Waves" -> Color(0xFF0D47A1)
        else -> Color.Gray
    }
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        // Tiny visual hint per scene
        Canvas(modifier = Modifier.size(20.dp)) {
            when (sceneName) {
                "Starfield" -> {
                    for (i in 0..5) {
                        drawCircle(
                            Color.White,
                            radius = 1.5f,
                            center = Offset(
                                size.width * ((i * 37 % 100) / 100f),
                                size.height * ((i * 53 % 100) / 100f)
                            )
                        )
                    }
                }
                "Candy Rain" -> {
                    for (i in 0..3) {
                        drawRoundRect(
                            Color.White.copy(alpha = 0.8f),
                            topLeft = Offset(size.width * i / 4f + 2f, size.height * 0.2f * i),
                            size = Size(4f, 8f),
                            cornerRadius = CornerRadius(2f)
                        )
                    }
                }
                "Aurora" -> {
                    drawLine(Color(0xFF00FF88), Offset(0f, size.height * 0.4f), Offset(size.width, size.height * 0.3f), 2f)
                    drawLine(Color(0xFF00CCFF), Offset(0f, size.height * 0.6f), Offset(size.width, size.height * 0.5f), 2f)
                }
                "Firefly Forest" -> {
                    drawCircle(Color(0xFFFFFF66), 2f, Offset(size.width * 0.3f, size.height * 0.4f))
                    drawCircle(Color(0xFFFFFF66), 2f, Offset(size.width * 0.7f, size.height * 0.6f))
                    drawLine(Color(0xFF2E7D32), Offset(size.width * 0.5f, size.height), Offset(size.width * 0.5f, size.height * 0.2f), 1.5f)
                }
                "Neon Grid" -> {
                    drawLine(Color(0xFFFF00FF), Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), 1f)
                    drawLine(Color(0xFF00FFFF), Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height), 1f)
                }
                "Ocean Waves" -> {
                    val wavePath = Path().apply {
                        moveTo(0f, size.height * 0.6f)
                        cubicTo(size.width * 0.3f, size.height * 0.4f, size.width * 0.7f, size.height * 0.8f, size.width, size.height * 0.5f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(wavePath, Color(0xFF42A5F5).copy(alpha = 0.8f))
                }
            }
        }
    }
}
