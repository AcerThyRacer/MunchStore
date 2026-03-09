package com.sugarmunch.app.ui.motion

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.withFrameNanos
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════════════
// Data model
// ═══════════════════════════════════════════════════════════════════════════

enum class ParticleShape(val label: String, val icon: String) {
    CIRCLE("Circle", "●"),
    STAR("Star", "★"),
    HEART("Heart", "♥"),
    DIAMOND("Diamond", "◆"),
    SNOWFLAKE("Snowflake", "❄"),
    SPARKLE("Sparkle", "✦"),
    CANDY("Candy", "🍬"),
    BUBBLE("Bubble", "○")
}

data class ParticleConfig(
    val enabled: Boolean = true,
    val density: Float = 1f,
    val shapes: Set<ParticleShape> = setOf(ParticleShape.CIRCLE, ParticleShape.SPARKLE),
    val colors: List<Color> = DefaultParticleColors,
    val minSize: Float = 3f,
    val maxSize: Float = 10f,
    val speed: Float = 1f,
    val gravity: Float = 0.5f,
    val wind: Float = 0f,
    val turbulence: Float = 0.2f,
    val fadeOut: Boolean = true,
    val rotateParticles: Boolean = true
)

val DefaultParticleColors = listOf(
    Color(0xFFFFB6C1), // pink
    Color(0xFF98FF98), // mint
    Color(0xFFB5DEFF), // sky
    Color(0xFFFFD700), // gold
    Color(0xFFE6A8FF), // lavender
    Color(0xFFFF9966)  // peach
)

// ═══════════════════════════════════════════════════════════════════════════
// ParticleEditor composable
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun ParticleEditor(
    config: ParticleConfig,
    onConfigChanged: (ParticleConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // — Live preview ——————————————————————————————————————————
        SectionTitle("Preview")
        ParticlePreviewCanvas(
            config = config,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        // — Enable toggle —————————————————————————————————————————
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Particles Enabled", style = MaterialTheme.typography.titleSmall)
            Switch(checked = config.enabled, onCheckedChange = { onConfigChanged(config.copy(enabled = it)) })
        }

        // — Shape selector (4 × 2 grid) ——————————————————————————
        SectionTitle("Shapes")
        ShapeSelector(
            selected = config.shapes,
            onToggle = { shape ->
                val next = config.shapes.toMutableSet()
                if (shape in next && next.size > 1) next.remove(shape) else next.add(shape)
                onConfigChanged(config.copy(shapes = next))
            }
        )

        // — Color palette —————————————————————————————————————————
        SectionTitle("Colors")
        ColorPalette(
            selected = config.colors,
            onToggle = { color ->
                val next = config.colors.toMutableList()
                if (color in next && next.size > 1) next.remove(color) else if (color !in next) next.add(color)
                onConfigChanged(config.copy(colors = next))
            }
        )

        // — Sliders ———————————————————————————————————————————————
        LabeledSlider("Density", config.density, 0f..2f) {
            onConfigChanged(config.copy(density = it))
        }
        LabeledSlider("Min Size (dp)", config.minSize, 2f..16f) {
            onConfigChanged(config.copy(minSize = it.coerceAtMost(config.maxSize)))
        }
        LabeledSlider("Max Size (dp)", config.maxSize, 2f..16f) {
            onConfigChanged(config.copy(maxSize = it.coerceAtLeast(config.minSize)))
        }
        LabeledSlider("Speed", config.speed, 0.2f..3f) {
            onConfigChanged(config.copy(speed = it))
        }
        LabeledSlider("Gravity", config.gravity, 0f..1.5f) {
            onConfigChanged(config.copy(gravity = it))
        }
        LabeledSlider("Wind", config.wind, -1f..1f) {
            onConfigChanged(config.copy(wind = it))
        }
        LabeledSlider("Turbulence", config.turbulence, 0f..1f) {
            onConfigChanged(config.copy(turbulence = it))
        }

        // — Toggles ———————————————————————————————————————————————
        ToggleRow("Fade Out", config.fadeOut) { onConfigChanged(config.copy(fadeOut = it)) }
        ToggleRow("Rotate Particles", config.rotateParticles) { onConfigChanged(config.copy(rotateParticles = it)) }

        Spacer(Modifier.height(32.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Live particle canvas
// ═══════════════════════════════════════════════════════════════════════════

private class Particle(
    var x: Float,
    var y: Float,
    val size: Float,
    val shape: ParticleShape,
    val color: Color,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var age: Float = 0f,
    val maxAge: Float
)

@Composable
fun ParticlePreviewCanvas(
    config: ParticleConfig,
    modifier: Modifier = Modifier
) {
    if (!config.enabled) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Particles disabled", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val count = (25 * config.density).toInt().coerceIn(1, 80)
    val shapeList = config.shapes.toList().ifEmpty { listOf(ParticleShape.CIRCLE) }
    val colorList = config.colors.ifEmpty { listOf(Color.White) }

    val particles = remember(count, shapeList, colorList, config.minSize, config.maxSize) {
        List(count) { spawnParticle(shapeList, colorList, config.minSize, config.maxSize) }
            .toMutableList()
    }

    var lastNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(config) {
        lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNanos = nanos
                updateParticles(particles, dt, config, shapeList, colorList)
            }
        }
    }

    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val alpha = if (config.fadeOut) (1f - (p.age / p.maxAge)).coerceIn(0f, 1f) else 1f
            val drawColor = p.color.copy(alpha = alpha * p.color.alpha)
            val sizePx = p.size.dp.toPx()
            val center = Offset(p.x * size.width, p.y * size.height)

            if (config.rotateParticles) {
                rotate(degrees = p.rotation, pivot = center) {
                    drawParticleShape(p.shape, center, sizePx, drawColor)
                }
            } else {
                drawParticleShape(p.shape, center, sizePx, drawColor)
            }
        }
    }
}

private fun spawnParticle(
    shapes: List<ParticleShape>,
    colors: List<Color>,
    minSize: Float,
    maxSize: Float
): Particle {
    return Particle(
        x = Random.nextFloat(),
        y = Random.nextFloat() * -0.1f,  // start near top
        size = Random.nextFloat() * (maxSize - minSize) + minSize,
        shape = shapes.random(),
        color = colors.random().copy(alpha = Random.nextFloat() * 0.4f + 0.5f),
        vx = (Random.nextFloat() - 0.5f) * 0.1f,
        vy = Random.nextFloat() * 0.05f + 0.02f,
        rotation = Random.nextFloat() * 360f,
        maxAge = Random.nextFloat() * 3f + 2f
    )
}

private fun updateParticles(
    particles: MutableList<Particle>,
    dt: Float,
    config: ParticleConfig,
    shapes: List<ParticleShape>,
    colors: List<Color>
) {
    for (i in particles.indices) {
        val p = particles[i]
        p.age += dt
        p.vy += config.gravity * dt * 0.15f
        p.vx += config.wind * dt * 0.08f
        p.vx += (Random.nextFloat() - 0.5f) * config.turbulence * dt * 0.5f
        p.x += p.vx * config.speed * dt
        p.y += p.vy * config.speed * dt
        if (config.rotateParticles) p.rotation += config.speed * 30f * dt

        // Respawn when off-screen or expired
        if (p.y > 1.1f || p.x < -0.1f || p.x > 1.1f || p.age > p.maxAge) {
            particles[i] = spawnParticle(shapes, colors, config.minSize, config.maxSize)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Shape drawing
// ═══════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawParticleShape(
    shape: ParticleShape,
    center: Offset,
    sizePx: Float,
    color: Color
) {
    when (shape) {
        ParticleShape.CIRCLE -> drawCircle(color = color, radius = sizePx / 2f, center = center)

        ParticleShape.STAR -> {
            val path = starPath(center, sizePx / 2f, sizePx / 4.5f, 5)
            drawPath(path, color = color, style = Fill)
        }

        ParticleShape.HEART -> {
            val path = heartPath(center, sizePx)
            drawPath(path, color = color, style = Fill)
        }

        ParticleShape.DIAMOND -> {
            val half = sizePx / 2f
            val path = Path().apply {
                moveTo(center.x, center.y - half)
                lineTo(center.x + half, center.y)
                lineTo(center.x, center.y + half)
                lineTo(center.x - half, center.y)
                close()
            }
            drawPath(path, color = color, style = Fill)
        }

        ParticleShape.SNOWFLAKE -> {
            val r = sizePx / 2f
            for (i in 0 until 6) {
                val angle = (i * 60f) * (PI.toFloat() / 180f)
                drawLine(
                    color = color,
                    start = center,
                    end = Offset(center.x + cos(angle) * r, center.y + sin(angle) * r),
                    strokeWidth = sizePx * 0.12f
                )
            }
        }

        ParticleShape.SPARKLE -> {
            val path = starPath(center, sizePx / 2f, sizePx / 6f, 4)
            drawPath(path, color = color, style = Fill)
        }

        ParticleShape.CANDY -> {
            // Circle body with a small wrapper rectangle
            drawCircle(color = color, radius = sizePx / 2.5f, center = center)
            val wrapW = sizePx * 0.15f
            val wrapH = sizePx * 0.5f
            drawRect(
                color = color.copy(alpha = color.alpha * 0.6f),
                topLeft = Offset(center.x - wrapW / 2f, center.y - wrapH),
                size = androidx.compose.ui.geometry.Size(wrapW, wrapH * 2f)
            )
        }

        ParticleShape.BUBBLE -> {
            val r = sizePx / 2f
            drawCircle(color = color.copy(alpha = color.alpha * 0.35f), radius = r, center = center)
            drawCircle(color = color, radius = r, center = center, style = Stroke(width = sizePx * 0.08f))
            // Highlight arc
            drawArc(
                color = Color.White.copy(alpha = color.alpha * 0.4f),
                startAngle = 210f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(center.x - r * 0.55f, center.y - r * 0.7f),
                size = androidx.compose.ui.geometry.Size(r * 0.7f, r * 0.5f),
                style = Stroke(width = sizePx * 0.1f)
            )
        }
    }
}

private fun starPath(center: Offset, outerR: Float, innerR: Float, points: Int): Path {
    val path = Path()
    val angleStep = PI.toFloat() / points
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = -PI.toFloat() / 2f + i * angleStep
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun heartPath(center: Offset, size: Float): Path {
    val s = size / 2f
    return Path().apply {
        moveTo(center.x, center.y + s * 0.6f)
        cubicTo(
            center.x - s * 1.1f, center.y - s * 0.1f,
            center.x - s * 0.6f, center.y - s * 0.9f,
            center.x, center.y - s * 0.3f
        )
        cubicTo(
            center.x + s * 0.6f, center.y - s * 0.9f,
            center.x + s * 1.1f, center.y - s * 0.1f,
            center.x, center.y + s * 0.6f
        )
        close()
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// UI sub-components
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun ShapeSelector(
    selected: Set<ParticleShape>,
    onToggle: (ParticleShape) -> Unit
) {
    // 4 columns × 2 rows
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ParticleShape.entries.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { shape ->
                    val isSelected = shape in selected
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        animationSpec = spring(),
                        label = "shape_border"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else Color.Transparent
                            )
                            .clickable { onToggle(shape) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(shape.icon, style = MaterialTheme.typography.titleMedium)
                            Text(shape.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                // Pad last row if < 4 items
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ColorPalette(
    selected: List<Color>,
    onToggle: (Color) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DefaultParticleColors.forEach { color ->
            val isSelected = color in selected
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.border(BorderStroke(2.5.dp, MaterialTheme.colorScheme.onSurface), CircleShape)
                        else Modifier
                    )
                    .clickable { onToggle(color) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "%.2f".format(value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
