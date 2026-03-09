package com.sugarmunch.app.ui.adaptive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// ---------------------------------------------------------------------------
// WeatherPalette — colour scheme for a single weather condition
// ---------------------------------------------------------------------------

data class WeatherPalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val accent: Color,
    val particleColor: Color,
    val overlayAlpha: Float,
)

// ---------------------------------------------------------------------------
// WeatherCondition — eight candy-themed weather states
// ---------------------------------------------------------------------------

enum class WeatherCondition(
    val label: String,
    val emoji: String,
    val palette: WeatherPalette,
) {
    SUNNY(
        label = "Sunny",
        emoji = "☀️",
        palette = WeatherPalette(
            primary = Color(0xFFFFC107),
            secondary = Color(0xFFFFEB3B),
            tertiary = Color(0xFFFFF176),
            background = Color(0xFFFFFDE7),
            surface = Color(0xFFFFFFFF),
            accent = Color(0xFFFF8F00),
            particleColor = Color(0xFFFFD54F),
            overlayAlpha = 0.06f,
        ),
    ),
    CLOUDY(
        label = "Cloudy",
        emoji = "☁️",
        palette = WeatherPalette(
            primary = Color(0xFF90A4AE),
            secondary = Color(0xFFB0BEC5),
            tertiary = Color(0xFFCFD8DC),
            background = Color(0xFFECEFF1),
            surface = Color(0xFFF5F5F5),
            accent = Color(0xFF78909C),
            particleColor = Color(0xFFB0BEC5),
            overlayAlpha = 0.10f,
        ),
    ),
    RAIN(
        label = "Rain",
        emoji = "🌧️",
        palette = WeatherPalette(
            primary = Color(0xFF42A5F5),
            secondary = Color(0xFF1E88E5),
            tertiary = Color(0xFF90CAF9),
            background = Color(0xFFE3F2FD),
            surface = Color(0xFFBBDEFB),
            accent = Color(0xFF1565C0),
            particleColor = Color(0xFF64B5F6),
            overlayAlpha = 0.12f,
        ),
    ),
    SNOW(
        label = "Snow",
        emoji = "❄️",
        palette = WeatherPalette(
            primary = Color(0xFFE1F5FE),
            secondary = Color(0xFFB3E5FC),
            tertiary = Color(0xFF81D4FA),
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF),
            accent = Color(0xFF4FC3F7),
            particleColor = Color(0xFFFFFFFF),
            overlayAlpha = 0.08f,
        ),
    ),
    STORM(
        label = "Storm",
        emoji = "⛈️",
        palette = WeatherPalette(
            primary = Color(0xFF37474F),
            secondary = Color(0xFF455A64),
            tertiary = Color(0xFF546E7A),
            background = Color(0xFF263238),
            surface = Color(0xFF37474F),
            accent = Color(0xFFFFEB3B),
            particleColor = Color(0xFF90A4AE),
            overlayAlpha = 0.18f,
        ),
    ),
    FOG(
        label = "Fog",
        emoji = "🌫️",
        palette = WeatherPalette(
            primary = Color(0xFFBDBDBD),
            secondary = Color(0xFFE0E0E0),
            tertiary = Color(0xFFEEEEEE),
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF),
            accent = Color(0xFF9E9E9E),
            particleColor = Color(0xFFE0E0E0),
            overlayAlpha = 0.20f,
        ),
    ),
    RAINBOW(
        label = "Rainbow",
        emoji = "🌈",
        palette = WeatherPalette(
            primary = Color(0xFFE91E63),
            secondary = Color(0xFFFF9800),
            tertiary = Color(0xFF4CAF50),
            background = Color(0xFFF3E5F5),
            surface = Color(0xFFFFFFFF),
            accent = Color(0xFF2196F3),
            particleColor = Color(0xFFFFC107),
            overlayAlpha = 0.07f,
        ),
    ),
    WIND(
        label = "Wind",
        emoji = "💨",
        palette = WeatherPalette(
            primary = Color(0xFF80CBC4),
            secondary = Color(0xFF4DB6AC),
            tertiary = Color(0xFFB2DFDB),
            background = Color(0xFFE0F2F1),
            surface = Color(0xFFFFFFFF),
            accent = Color(0xFF00897B),
            particleColor = Color(0xFF80CBC4),
            overlayAlpha = 0.08f,
        ),
    );
}

// ---------------------------------------------------------------------------
// WeatherParticleConfig — per-condition particle parameters
// ---------------------------------------------------------------------------

data class WeatherParticleConfig(
    val count: Int,
    val speedMin: Float,
    val speedMax: Float,
    val sizeMin: Float,
    val sizeMax: Float,
    val gravity: Float,
    val wind: Float,
)

// ---------------------------------------------------------------------------
// WeatherVisualEngine — singleton managing weather visuals
// ---------------------------------------------------------------------------

object WeatherVisualEngine {

    fun getPalette(condition: WeatherCondition): WeatherPalette = condition.palette

    fun blendWithTheme(
        weatherPalette: WeatherPalette,
        themePrimary: Color,
        themeBackground: Color,
        intensity: Float,
    ): WeatherPalette {
        val t = intensity.coerceIn(0f, 1f)
        return weatherPalette.copy(
            primary = lerpColor(themePrimary, weatherPalette.primary, t),
            background = lerpColor(themeBackground, weatherPalette.background, t),
            overlayAlpha = weatherPalette.overlayAlpha * t,
        )
    }

    fun getParticleConfig(condition: WeatherCondition): WeatherParticleConfig = when (condition) {
        WeatherCondition.SUNNY -> WeatherParticleConfig(
            count = 20, speedMin = 15f, speedMax = 35f,
            sizeMin = 2f, sizeMax = 5f, gravity = -0.3f, wind = 0f,
        )
        WeatherCondition.CLOUDY -> WeatherParticleConfig(
            count = 8, speedMin = 8f, speedMax = 18f,
            sizeMin = 30f, sizeMax = 60f, gravity = 0f, wind = 0.4f,
        )
        WeatherCondition.RAIN -> WeatherParticleConfig(
            count = 60, speedMin = 250f, speedMax = 400f,
            sizeMin = 1f, sizeMax = 2.5f, gravity = 1f, wind = 0.15f,
        )
        WeatherCondition.SNOW -> WeatherParticleConfig(
            count = 40, speedMin = 20f, speedMax = 50f,
            sizeMin = 2f, sizeMax = 5f, gravity = 0.5f, wind = 0f,
        )
        WeatherCondition.STORM -> WeatherParticleConfig(
            count = 80, speedMin = 350f, speedMax = 550f,
            sizeMin = 1f, sizeMax = 3f, gravity = 1.2f, wind = 0.35f,
        )
        WeatherCondition.FOG -> WeatherParticleConfig(
            count = 12, speedMin = 5f, speedMax = 12f,
            sizeMin = 50f, sizeMax = 100f, gravity = 0f, wind = 0.3f,
        )
        WeatherCondition.RAINBOW -> WeatherParticleConfig(
            count = 30, speedMin = 10f, speedMax = 25f,
            sizeMin = 2f, sizeMax = 4f, gravity = -0.1f, wind = 0f,
        )
        WeatherCondition.WIND -> WeatherParticleConfig(
            count = 25, speedMin = 150f, speedMax = 300f,
            sizeMin = 1f, sizeMax = 2f, gravity = 0f, wind = 1f,
        )
    }

    private fun lerpColor(a: Color, b: Color, t: Float): Color = Color(
        red = a.red + (b.red - a.red) * t,
        green = a.green + (b.green - a.green) * t,
        blue = a.blue + (b.blue - a.blue) * t,
        alpha = a.alpha + (b.alpha - a.alpha) * t,
    )
}

// ---------------------------------------------------------------------------
// Internal particle state
// ---------------------------------------------------------------------------

private data class WParticle(
    var x: Float,
    var y: Float,
    var size: Float,
    var speed: Float,
    var alpha: Float,
    var phase: Float,
    var length: Float = 0f,
)

private fun createParticles(
    count: Int,
    width: Float,
    height: Float,
    config: WeatherParticleConfig,
): List<WParticle> = List(count) {
    WParticle(
        x = (Math.random() * width).toFloat(),
        y = (Math.random() * height).toFloat(),
        size = config.sizeMin + (Math.random() * (config.sizeMax - config.sizeMin)).toFloat(),
        speed = config.speedMin + (Math.random() * (config.speedMax - config.speedMin)).toFloat(),
        alpha = 0.3f + (Math.random() * 0.5f).toFloat(),
        phase = (Math.random() * 2 * PI).toFloat(),
        length = 6f + (Math.random() * 14f).toFloat(),
    )
}

// ---------------------------------------------------------------------------
// WeatherParticleOverlay — full-screen animated weather layer
// ---------------------------------------------------------------------------

@Composable
fun WeatherParticleOverlay(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    densityMultiplier: Float = 1f,
    intensityMultiplier: Float = 1f,
) {
    val config = remember(condition) { WeatherVisualEngine.getParticleConfig(condition) }
    val palette = remember(condition) { WeatherVisualEngine.getPalette(condition) }
    val effectiveCount = (config.count * densityMultiplier).toInt().coerceAtLeast(1)

    var particles by remember { mutableStateOf(emptyList<WParticle>()) }
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    var lightningAlpha by remember { mutableFloatStateOf(0f) }
    var timeSinceFlash by remember { mutableFloatStateOf(0f) }
    var elapsedTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(condition, effectiveCount) {
        particles = emptyList()
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) lastNanos = nanos
                val dt = (nanos - lastNanos) / 1_000_000_000f
                lastNanos = nanos
                elapsedTime += dt

                if (particles.isEmpty() && canvasWidth > 0f) {
                    particles = createParticles(effectiveCount, canvasWidth, canvasHeight, config)
                }

                // storm lightning logic
                if (condition == WeatherCondition.STORM) {
                    timeSinceFlash += dt
                    if (timeSinceFlash > 2f + Math.random().toFloat() * 3f) {
                        lightningAlpha = 0.7f + (Math.random() * 0.3f).toFloat()
                        timeSinceFlash = 0f
                    } else {
                        lightningAlpha = (lightningAlpha - dt * 4f).coerceAtLeast(0f)
                    }
                }

                particles = particles.map { p ->
                    var nx = p.x
                    var ny = p.y
                    val effectiveSpeed = p.speed * intensityMultiplier

                    when (condition) {
                        WeatherCondition.RAIN, WeatherCondition.STORM -> {
                            ny += effectiveSpeed * dt
                            nx += config.wind * effectiveSpeed * dt
                            if (ny > canvasHeight + 10f) {
                                ny = -10f
                                nx = (Math.random() * canvasWidth).toFloat()
                            }
                        }
                        WeatherCondition.SNOW -> {
                            ny += effectiveSpeed * dt
                            nx += sin(elapsedTime * 1.5f + p.phase) * 25f * dt
                            if (ny > canvasHeight + 10f) {
                                ny = -10f
                                nx = (Math.random() * canvasWidth).toFloat()
                            }
                        }
                        WeatherCondition.WIND -> {
                            nx += effectiveSpeed * dt
                            ny += sin(elapsedTime * 2f + p.phase) * 8f * dt
                            if (nx > canvasWidth + 20f) {
                                nx = -20f
                                ny = (Math.random() * canvasHeight).toFloat()
                            }
                        }
                        WeatherCondition.SUNNY -> {
                            ny -= effectiveSpeed * dt
                            nx += sin(elapsedTime + p.phase) * 10f * dt
                            if (ny < -10f) {
                                ny = canvasHeight + 10f
                                nx = (Math.random() * canvasWidth).toFloat()
                            }
                        }
                        WeatherCondition.FOG -> {
                            nx += effectiveSpeed * config.wind * dt
                            ny += sin(elapsedTime * 0.3f + p.phase) * 3f * dt
                            if (nx > canvasWidth + p.size) {
                                nx = -p.size
                                ny = (Math.random() * canvasHeight).toFloat()
                            }
                        }
                        WeatherCondition.CLOUDY -> {
                            nx += effectiveSpeed * config.wind * dt
                            if (nx > canvasWidth + p.size) {
                                nx = -p.size
                                ny = (Math.random() * canvasHeight * 0.5f).toFloat()
                            }
                        }
                        WeatherCondition.RAINBOW -> {
                            ny -= effectiveSpeed * 0.3f * dt
                            nx += cos(elapsedTime * 0.8f + p.phase) * 15f * dt
                            if (ny < -10f) {
                                ny = canvasHeight * 0.4f
                                nx = (Math.random() * canvasWidth).toFloat()
                            }
                        }
                    }
                    p.copy(x = nx, y = ny)
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        canvasWidth = size.width
        canvasHeight = size.height

        when (condition) {
            WeatherCondition.RAIN -> drawRainParticles(particles, palette.particleColor)
            WeatherCondition.SNOW -> drawSnowParticles(particles, palette.particleColor, elapsedTime)
            WeatherCondition.STORM -> {
                drawRainParticles(particles, palette.particleColor)
                if (lightningAlpha > 0.01f) {
                    drawRect(color = Color.White.copy(alpha = lightningAlpha), size = size)
                }
            }
            WeatherCondition.FOG -> drawFogParticles(particles, palette.particleColor)
            WeatherCondition.WIND -> drawWindParticles(particles, palette.particleColor)
            WeatherCondition.SUNNY -> drawSunnyParticles(particles, palette.particleColor, elapsedTime)
            WeatherCondition.RAINBOW -> drawRainbowShimmer(particles, size, elapsedTime)
            WeatherCondition.CLOUDY -> drawCloudParticles(particles, palette.particleColor)
        }

        // subtle overlay tint
        drawRect(
            color = palette.primary.copy(alpha = palette.overlayAlpha * intensityMultiplier),
            size = size,
        )
    }
}

// ---------------------------------------------------------------------------
// Canvas drawing helpers — one per weather type
// ---------------------------------------------------------------------------

private fun DrawScope.drawRainParticles(particles: List<WParticle>, color: Color) {
    particles.forEach { p ->
        drawLine(
            color = color.copy(alpha = p.alpha),
            start = Offset(p.x, p.y),
            end = Offset(p.x - 1f, p.y + p.length),
            strokeWidth = p.size,
        )
        // splash at bottom
        if (p.y > size.height - 30f) {
            val splash = (1f - (size.height - p.y) / 30f).coerceIn(0f, 1f)
            drawCircle(
                color = color.copy(alpha = p.alpha * splash * 0.4f),
                radius = p.size * 3f * splash,
                center = Offset(p.x, size.height - 2f),
            )
        }
    }
}

private fun DrawScope.drawSnowParticles(particles: List<WParticle>, color: Color, time: Float) {
    particles.forEach { p ->
        val flicker = 0.7f + 0.3f * sin(time * 2f + p.phase)
        drawCircle(
            color = color.copy(alpha = p.alpha * flicker),
            radius = p.size,
            center = Offset(p.x, p.y),
        )
        // inner highlight
        drawCircle(
            color = Color.White.copy(alpha = p.alpha * flicker * 0.5f),
            radius = p.size * 0.4f,
            center = Offset(p.x - p.size * 0.2f, p.y - p.size * 0.2f),
        )
    }
}

private fun DrawScope.drawFogParticles(particles: List<WParticle>, color: Color) {
    particles.forEach { p ->
        drawCircle(
            color = color.copy(alpha = p.alpha * 0.15f),
            radius = p.size,
            center = Offset(p.x, p.y),
        )
        drawCircle(
            color = color.copy(alpha = p.alpha * 0.08f),
            radius = p.size * 1.5f,
            center = Offset(p.x + p.size * 0.3f, p.y - p.size * 0.2f),
        )
    }
}

private fun DrawScope.drawWindParticles(particles: List<WParticle>, color: Color) {
    particles.forEach { p ->
        drawLine(
            color = color.copy(alpha = p.alpha * 0.6f),
            start = Offset(p.x, p.y),
            end = Offset(p.x + p.length * 3f, p.y),
            strokeWidth = p.size,
        )
    }
}

private fun DrawScope.drawSunnyParticles(particles: List<WParticle>, color: Color, time: Float) {
    particles.forEach { p ->
        val twinkle = 0.5f + 0.5f * sin(time * 3f + p.phase)
        val starSize = p.size * twinkle
        // four-point sparkle using two crossing lines
        drawLine(
            color = color.copy(alpha = p.alpha * twinkle),
            start = Offset(p.x - starSize, p.y),
            end = Offset(p.x + starSize, p.y),
            strokeWidth = 1.5f,
        )
        drawLine(
            color = color.copy(alpha = p.alpha * twinkle),
            start = Offset(p.x, p.y - starSize),
            end = Offset(p.x, p.y + starSize),
            strokeWidth = 1.5f,
        )
        // centre dot
        drawCircle(
            color = color.copy(alpha = p.alpha * twinkle * 0.8f),
            radius = starSize * 0.35f,
            center = Offset(p.x, p.y),
        )
    }
}

private fun DrawScope.drawRainbowShimmer(particles: List<WParticle>, canvasSize: Size, time: Float) {
    val rainbowColors = listOf(
        Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00),
        Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF4B0082), Color(0xFF9400D3),
    )
    val bandHeight = canvasSize.height * 0.25f
    rainbowColors.forEachIndexed { i, color ->
        val yOffset = (i.toFloat() / rainbowColors.size) * bandHeight
        val shimmer = 0.04f + 0.03f * sin(time * 1.2f + i * 0.8f)
        drawRect(
            color = color.copy(alpha = shimmer),
            topLeft = Offset(0f, yOffset),
            size = Size(canvasSize.width, bandHeight / rainbowColors.size),
        )
    }
    // floating prismatic particles
    particles.forEach { p ->
        val colorIndex = (p.phase * rainbowColors.size / (2 * PI).toFloat()).toInt()
            .coerceIn(0, rainbowColors.lastIndex)
        val sparkle = 0.4f + 0.4f * sin(time * 2f + p.phase)
        drawCircle(
            color = rainbowColors[colorIndex].copy(alpha = p.alpha * sparkle * 0.5f),
            radius = p.size,
            center = Offset(p.x, p.y),
        )
    }
}

private fun DrawScope.drawCloudParticles(particles: List<WParticle>, color: Color) {
    particles.forEach { p ->
        // main blob
        drawCircle(
            color = color.copy(alpha = p.alpha * 0.18f),
            radius = p.size,
            center = Offset(p.x, p.y),
        )
        // secondary lobe
        drawCircle(
            color = color.copy(alpha = p.alpha * 0.14f),
            radius = p.size * 0.7f,
            center = Offset(p.x + p.size * 0.5f, p.y - p.size * 0.15f),
        )
        // tertiary lobe
        drawCircle(
            color = color.copy(alpha = p.alpha * 0.12f),
            radius = p.size * 0.55f,
            center = Offset(p.x - p.size * 0.4f, p.y - p.size * 0.1f),
        )
    }
}

// ---------------------------------------------------------------------------
// WeatherSettingsPanel — user-facing settings UI
// ---------------------------------------------------------------------------

@Composable
fun WeatherSettingsPanel(
    modifier: Modifier = Modifier,
    selectedCondition: WeatherCondition = WeatherCondition.SUNNY,
    onConditionChange: (WeatherCondition) -> Unit = {},
    intensity: Float = 0.5f,
    onIntensityChange: (Float) -> Unit = {},
    particleDensity: Float = 1f,
    onParticleDensityChange: (Float) -> Unit = {},
    soundEnabled: Boolean = false,
    onSoundToggle: (Boolean) -> Unit = {},
    demoMode: Boolean = false,
    onDemoModeToggle: (Boolean) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // -- header -------------------------------------------------------
        Text(
            text = "🍭 Weather Effects",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        // -- condition selector (2 × 4 grid) -----------------------------
        Text("Weather Condition", fontWeight = FontWeight.SemiBold)

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            items(WeatherCondition.entries.toList()) { condition ->
                val isSelected = condition == selectedCondition
                val palette = condition.palette
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onConditionChange(condition) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) palette.primary.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(condition.emoji, fontSize = 22.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            condition.label,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        // -- intensity slider ---------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Theme Intensity", fontWeight = FontWeight.SemiBold)
                    Text(
                        "${(intensity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    "How much weather affects theme colours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = intensity,
                    onValueChange = onIntensityChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = selectedCondition.palette.primary,
                        activeTrackColor = selectedCondition.palette.accent,
                    ),
                )
            }
        }

        // -- particle density slider --------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Particle Density", fontWeight = FontWeight.SemiBold)
                    Text(
                        "${(particleDensity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    "Number of on-screen weather particles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = particleDensity,
                    onValueChange = onParticleDensityChange,
                    valueRange = 0.1f..2f,
                    colors = SliderDefaults.colors(
                        thumbColor = selectedCondition.palette.primary,
                        activeTrackColor = selectedCondition.palette.accent,
                    ),
                )
            }
        }

        // -- toggles (sound + demo mode) ----------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Sound Effects", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Play ambient weather sounds",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onSoundToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Demo Mode", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Force selected weather for testing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = demoMode,
                        onCheckedChange = onDemoModeToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        }

        // -- live preview card --------------------------------------------
        Text("Live Preview", fontWeight = FontWeight.SemiBold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = selectedCondition.palette.background,
            ),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                WeatherParticleOverlay(
                    condition = selectedCondition,
                    densityMultiplier = particleDensity,
                    intensityMultiplier = intensity.coerceAtLeast(0.3f),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(
                            selectedCondition.palette.surface.copy(alpha = 0.85f),
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${selectedCondition.emoji} ${selectedCondition.label}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = selectedCondition.palette.primary,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// WeatherScenePreview — 2×4 grid showing all eight weather conditions
// ---------------------------------------------------------------------------

@Composable
fun WeatherScenePreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "🍬 All Weather Scenes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(720.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            items(WeatherCondition.entries.toList()) { condition ->
                WeatherMiniPreviewCard(condition = condition)
            }
        }
    }
}

@Composable
private fun WeatherMiniPreviewCard(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
) {
    val palette = condition.palette

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.background),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            WeatherParticleOverlay(
                condition = condition,
                densityMultiplier = 0.5f,
                intensityMultiplier = 0.8f,
            )

            // label overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(
                        palette.surface.copy(alpha = 0.85f),
                        RoundedCornerShape(6.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "${condition.emoji} ${condition.label}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.primary,
                )
            }

            // accent dot in top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(palette.accent),
            )
        }
    }
}
