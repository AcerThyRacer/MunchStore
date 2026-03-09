package com.sugarmunch.app.ui.shaders

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class ShaderEffect {
    WATER_RIPPLE,
    CHROMATIC_ABERRATION,
    GLITCH,
    HEAT_HAZE,
    FROSTED_GLASS,
    HOLOGRAPHIC_SWEEP,
    CRT_SCANLINES,
    MATRIX_RAIN,
    LIQUID_CHROME
}

data class ShaderConfig(
    val effect: ShaderEffect,
    val intensity: Float = 1f,
    val speed: Float = 1f,
    val color: Color? = null,
    val touchPoint: Offset? = null
)

// ── Water Ripple state ──────────────────────────────────────────────────────────

private class RippleInstance(
    val center: Offset,
    val startTime: Float
)

private class WaterRippleState {
    val ripples = mutableStateListOf<RippleInstance>()

    fun addRipple(center: Offset, time: Float) {
        ripples.add(RippleInstance(center, time))
        if (ripples.size > 8) ripples.removeAt(0)
    }

    fun pruneOld(time: Float, maxAge: Float = 3f) {
        ripples.removeAll { (time - it.startTime) > maxAge }
    }
}

// ── Glitch state ────────────────────────────────────────────────────────────────

private class GlitchBand(
    val y: Float,
    val height: Float,
    val offsetX: Float,
    val tint: Color
)

private class GlitchState {
    var bands by mutableStateOf(emptyList<GlitchBand>())
    var activeUntil by mutableFloatStateOf(-1f)
    var nextTrigger by mutableFloatStateOf(0f)
}

// ── Matrix Rain state ───────────────────────────────────────────────────────────

private class MatrixColumn(
    val x: Int,
    var headY: Float = 0f,
    var speed: Float = 1f,
    val tailLength: Int = 12
)

private class MatrixRainState(columns: Int, random: Random) {
    val cols = List(columns) { i ->
        MatrixColumn(
            x = i,
            headY = random.nextFloat() * -20f,
            speed = 0.5f + random.nextFloat() * 1.5f,
            tailLength = 8 + random.nextInt(12)
        )
    }
}

// ── Frosted Glass cached dots ───────────────────────────────────────────────────

private class FrostDot(val x: Float, val y: Float, val radius: Float, val alpha: Float)

// ── Main composable ─────────────────────────────────────────────────────────────

@Composable
fun ShaderEffectOverlay(
    config: ShaderConfig,
    modifier: Modifier = Modifier
) {
    var timeSeconds by remember { mutableFloatStateOf(0f) }
    var lastFrameNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(config.speed) {
        while (true) {
            withFrameNanos { nanos ->
                if (lastFrameNanos != 0L) {
                    val deltaSec = (nanos - lastFrameNanos) / 1_000_000_000f
                    timeSeconds += deltaSec * config.speed
                }
                lastFrameNanos = nanos
            }
        }
    }

    when (config.effect) {
        ShaderEffect.WATER_RIPPLE -> WaterRippleOverlay(config, timeSeconds, modifier)
        ShaderEffect.CHROMATIC_ABERRATION -> ChromaticAberrationOverlay(config, timeSeconds, modifier)
        ShaderEffect.GLITCH -> GlitchOverlay(config, timeSeconds, modifier)
        ShaderEffect.HEAT_HAZE -> HeatHazeOverlay(config, timeSeconds, modifier)
        ShaderEffect.FROSTED_GLASS -> FrostedGlassOverlay(config, modifier)
        ShaderEffect.HOLOGRAPHIC_SWEEP -> HolographicSweepOverlay(config, timeSeconds, modifier)
        ShaderEffect.CRT_SCANLINES -> CrtScanlinesOverlay(config, timeSeconds, modifier)
        ShaderEffect.MATRIX_RAIN -> MatrixRainOverlay(config, timeSeconds, modifier)
        ShaderEffect.LIQUID_CHROME -> LiquidChromeOverlay(config, timeSeconds, modifier)
    }
}

// ── WATER_RIPPLE ────────────────────────────────────────────────────────────────

@Composable
private fun WaterRippleOverlay(
    config: ShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val state = remember { WaterRippleState() }
    val tint = config.color ?: Color(0xFF4FC3F7)

    LaunchedEffect(config.touchPoint, time) {
        val tp = config.touchPoint ?: return@LaunchedEffect
        if (state.ripples.isEmpty() || (time - state.ripples.last().startTime) > 0.3f) {
            state.addRipple(tp, time)
        }
    }

    state.pruneOld(time)

    Canvas(modifier = modifier.fillMaxSize()) {
        for (ripple in state.ripples) {
            drawRipple(ripple, time, config.intensity, tint, size)
        }
    }
}

private fun DrawScope.drawRipple(
    ripple: RippleInstance,
    time: Float,
    intensity: Float,
    tint: Color,
    canvasSize: Size
) {
    val age = time - ripple.startTime
    if (age < 0f) return
    val maxRadius = sqrt(canvasSize.width * canvasSize.width + canvasSize.height * canvasSize.height) / 2f
    val ringCount = (4 * intensity).toInt().coerceIn(2, 8)

    for (i in 0 until ringCount) {
        val phase = age - i * 0.15f
        if (phase < 0f) continue
        val baseRadius = phase * maxRadius * 0.3f
        val sineOffset = sin(phase * 8f * PI.toFloat()) * 4f * intensity
        val radius = baseRadius + sineOffset
        if (radius > maxRadius || radius < 0f) continue

        val alphaFade = (1f - (radius / maxRadius)).coerceIn(0f, 1f) * 0.6f * intensity
        val strokeWidth = max(1f, (3f - phase * 0.8f) * intensity)

        drawCircle(
            color = tint.copy(alpha = alphaFade),
            radius = radius,
            center = ripple.center,
            style = Stroke(width = strokeWidth)
        )
    }
}

// ── CHROMATIC_ABERRATION ────────────────────────────────────────────────────────

@Composable
private fun ChromaticAberrationOverlay(
    config: ShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val maxShift = 6f * config.intensity

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val diag = sqrt(cx * cx + cy * cy)
        val pulse = 1f + sin(time * 2f * PI.toFloat()) * 0.3f

        val bandCount = 6
        val bandH = size.height / bandCount

        for (i in 0 until bandCount) {
            val bandCenterY = (i + 0.5f) * bandH
            val distY = abs(bandCenterY - cy) / cy
            val distFactor = distY * pulse

            val shift = maxShift * distFactor

            val rect = Rect(
                left = 0f,
                top = i * bandH,
                right = size.width,
                bottom = (i + 1) * bandH
            )

            // Red channel shifted left
            drawRect(
                color = Color.Red.copy(alpha = 0.08f * config.intensity),
                topLeft = Offset(rect.left - shift, rect.top),
                size = Size(rect.width, rect.height),
                blendMode = BlendMode.Screen
            )
            // Green channel centered
            drawRect(
                color = Color.Green.copy(alpha = 0.06f * config.intensity),
                topLeft = Offset(rect.left, rect.top),
                size = Size(rect.width, rect.height),
                blendMode = BlendMode.Screen
            )
            // Blue channel shifted right
            drawRect(
                color = Color.Blue.copy(alpha = 0.08f * config.intensity),
                topLeft = Offset(rect.left + shift, rect.top),
                size = Size(rect.width, rect.height),
                blendMode = BlendMode.Screen
            )
        }
    }
}

// ── GLITCH ──────────────────────────────────────────────────────────────────────

@Composable
private fun GlitchOverlay(
    config: ShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val state = remember { GlitchState() }
    val random = remember { Random(42) }

    if (time >= state.nextTrigger && time > state.activeUntil) {
        val bandCount = (2 + random.nextInt(4)).coerceAtMost((4 * config.intensity).toInt().coerceAtLeast(1))
        state.bands = List(bandCount) {
            GlitchBand(
                y = random.nextFloat(),
                height = 0.01f + random.nextFloat() * 0.06f,
                offsetX = (random.nextFloat() - 0.5f) * 40f * config.intensity,
                tint = listOf(
                    Color.Red.copy(alpha = 0.3f),
                    Color.Cyan.copy(alpha = 0.3f),
                    Color.Magenta.copy(alpha = 0.25f),
                    Color.Green.copy(alpha = 0.2f)
                ).random(random)
            )
        }
        state.activeUntil = time + 0.2f
        state.nextTrigger = time + 0.5f + random.nextFloat() * 1.5f
    }

    val isActive = time <= state.activeUntil

    Canvas(modifier = modifier.fillMaxSize()) {
        if (!isActive) return@Canvas
        for (band in state.bands) {
            val top = band.y * size.height
            val h = band.height * size.height
            clipRect(left = 0f, top = top, right = size.width, bottom = top + h) {
                drawRect(
                    color = band.tint,
                    topLeft = Offset(band.offsetX, top),
                    size = Size(size.width, h)
                )
                // Secondary corruption stripe
                drawRect(
                    color = Color.White.copy(alpha = 0.1f * config.intensity),
                    topLeft = Offset(band.offsetX * 0.5f, top + h * 0.3f),
                    size = Size(size.width * 0.6f, h * 0.2f)
                )
            }
        }
    }
}

// ── HEAT_HAZE ───────────────────────────────────────────────────────────────────

@Composable
private fun HeatHazeOverlay(
    config: ShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color(0xFFFFAB40)

    Canvas(modifier = modifier.fillMaxSize()) {
        val lineSpacing = 3f
        val lineCount = (size.height / lineSpacing).toInt()
        val baseAlpha = 0.07f * config.intensity

        for (i in 0 until lineCount) {
            val y = i * lineSpacing
            val phase = time * 3f + i * 0.15f
            val displacement = sin(phase.toDouble()).toFloat() * 6f * config.intensity
            val secondaryWave = cos((phase * 0.7f + i * 0.05f).toDouble()).toFloat() * 3f * config.intensity
            val totalDisplacement = displacement + secondaryWave

            val alphaVariation = (sin((phase * 0.5f).toDouble()).toFloat() * 0.5f + 0.5f) * baseAlpha

            drawLine(
                color = tint.copy(alpha = alphaVariation.coerceIn(0f, 1f)),
                start = Offset(totalDisplacement, y),
                end = Offset(size.width + totalDisplacement, y),
                strokeWidth = 1.5f
            )
        }
    }
}

// ── FROSTED_GLASS ───────────────────────────────────────────────────────────────

@Composable
private fun FrostedGlassOverlay(
    config: ShaderConfig,
    modifier: Modifier
) {
    val tint = config.color ?: Color.White

    // Pre-generate dots once so we don't regenerate every frame
    val dots = remember(config.intensity) {
        val rng = Random(1337)
        val count = (2000 * config.intensity).toInt().coerceIn(500, 5000)
        List(count) {
            FrostDot(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                radius = 1f + rng.nextFloat() * 2.5f,
                alpha = 0.03f + rng.nextFloat() * 0.08f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Grain texture dots
        for (dot in dots) {
            drawCircle(
                color = tint.copy(alpha = (dot.alpha * config.intensity).coerceIn(0f, 1f)),
                radius = dot.radius,
                center = Offset(dot.x * size.width, dot.y * size.height)
            )
        }
        // Frosted overlay
        drawRect(
            color = tint.copy(alpha = 0.12f * config.intensity),
            size = size
        )
    }
}

// ── HOLOGRAPHIC_SWEEP ───────────────────────────────────────────────────────────

@Composable
private fun HolographicSweepOverlay(
    config: ShaderConfig,
    time: Float,
    modifier: Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val bandWidth = size.width * 0.35f
        val totalTravel = size.width + size.height + bandWidth
        val cyclePos = ((time * 0.4f) % 1f) * totalTravel

        val rainbowColors = listOf(
            Color(0x40FF0000),
            Color(0x40FF8800),
            Color(0x40FFFF00),
            Color(0x4000FF00),
            Color(0x4000FFFF),
            Color(0x400088FF),
            Color(0x40AA00FF),
            Color(0x40FF00AA)
        ).map { it.copy(alpha = it.alpha * config.intensity.coerceIn(0f, 1f)) }

        val brush = Brush.linearGradient(
            colors = rainbowColors,
            start = Offset(cyclePos - bandWidth, 0f),
            end = Offset(cyclePos, size.height)
        )

        drawRect(
            brush = brush,
            size = size,
            blendMode = BlendMode.Screen
        )

        // Secondary sweep offset for richness
        val cyclePos2 = (((time * 0.4f) + 0.5f) % 1f) * totalTravel
        val brush2 = Brush.linearGradient(
            colors = rainbowColors.reversed(),
            start = Offset(cyclePos2 - bandWidth * 0.6f, size.height * 0.3f),
            end = Offset(cyclePos2, size.height * 0.7f)
        )
        drawRect(
            brush = brush2,
            size = size,
            alpha = 0.5f,
            blendMode = BlendMode.Screen
        )
    }
}

// ── CRT_SCANLINES ───────────────────────────────────────────────────────────────

@Composable
private fun CrtScanlinesOverlay(
    config: ShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val tint = config.color ?: Color(0xFF00FF41) // Classic green phosphor

    Canvas(modifier = modifier.fillMaxSize()) {
        // Alternating scanlines
        val lineSpacing = 2f
        val lineCount = (size.height / lineSpacing).toInt()
        val scanAlpha = 0.12f * config.intensity
        val scrollOffset = (time * 30f) % lineSpacing

        for (i in 0 until lineCount) {
            val y = i * lineSpacing + scrollOffset
            if (i % 2 == 0) {
                drawLine(
                    color = Color.Black.copy(alpha = scanAlpha.coerceIn(0f, 1f)),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
        }

        // Phosphor tint overlay
        drawRect(
            color = tint.copy(alpha = 0.04f * config.intensity),
            size = size,
            blendMode = BlendMode.Screen
        )

        // Vignette / edge curvature illusion with arcs
        val vignetteAlpha = 0.08f * config.intensity
        val arcStroke = Stroke(width = size.width * 0.15f)

        // Top edge darkening
        drawArc(
            color = Color.Black.copy(alpha = vignetteAlpha.coerceIn(0f, 1f)),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.1f, -size.height * 0.45f),
            size = Size(size.width * 1.2f, size.height),
            style = arcStroke
        )
        // Bottom edge darkening
        drawArc(
            color = Color.Black.copy(alpha = vignetteAlpha.coerceIn(0f, 1f)),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.1f, size.height * 0.45f),
            size = Size(size.width * 1.2f, size.height),
            style = arcStroke
        )

        // Flickering brightness band
        val flickerY = ((time * 0.8f) % 1f) * size.height * 1.4f - size.height * 0.2f
        drawRect(
            color = Color.White.copy(alpha = 0.02f * config.intensity),
            topLeft = Offset(0f, flickerY),
            size = Size(size.width, size.height * 0.08f)
        )
    }
}

// ── MATRIX_RAIN ─────────────────────────────────────────────────────────────────

@Composable
private fun MatrixRainOverlay(
    config: ShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val cellSize = 14f
    val tint = config.color ?: Color(0xFF00FF41)

    val state = remember { mutableStateOf<MatrixRainState?>(null) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cols = (size.width / cellSize).toInt().coerceAtLeast(1)
        val rows = (size.height / cellSize).toInt().coerceAtLeast(1)

        // Lazy-init with correct column count
        val rainState = state.value.let { s ->
            if (s == null || s.cols.size != cols) {
                MatrixRainState(cols, Random(99)).also { state.value = it }
            } else s
        }

        // Dark background wash
        drawRect(color = Color.Black.copy(alpha = 0.15f * config.intensity), size = size)

        for (col in rainState.cols) {
            col.headY += col.speed * config.speed * 0.6f
            if (col.headY > rows + col.tailLength) {
                col.headY = -col.tailLength.toFloat()
            }

            val headRow = col.headY.toInt()
            for (t in 0 until col.tailLength) {
                val row = headRow - t
                if (row < 0 || row >= rows) continue

                val fade = 1f - (t.toFloat() / col.tailLength)
                val alpha = fade * 0.7f * config.intensity
                val brightness = if (t == 0) 1f else fade * 0.7f

                val charColor = tint.copy(
                    alpha = alpha.coerceIn(0f, 1f),
                    red = tint.red * brightness + (1f - brightness) * 0.1f,
                    green = tint.green * brightness,
                    blue = tint.blue * brightness
                )

                // Draw small rect as character glyph placeholder
                val cx = col.x * cellSize
                val cy = row * cellSize
                val glyphPad = cellSize * 0.2f
                drawRect(
                    color = charColor,
                    topLeft = Offset(cx + glyphPad, cy + glyphPad),
                    size = Size(cellSize - glyphPad * 2, cellSize - glyphPad * 2)
                )
            }
        }
    }
}

// ── LIQUID_CHROME ───────────────────────────────────────────────────────────────

@Composable
private fun LiquidChromeOverlay(
    config: ShaderConfig,
    time: Float,
    modifier: Modifier
) {
    val blobCount = (6 * config.intensity).toInt().coerceIn(3, 12)
    val blobSeeds = remember(blobCount) {
        val rng = Random(77)
        List(blobCount) {
            floatArrayOf(
                rng.nextFloat() * 2f * PI.toFloat(),  // phase X
                rng.nextFloat() * 2f * PI.toFloat(),  // phase Y
                0.15f + rng.nextFloat() * 0.35f,       // freq X
                0.15f + rng.nextFloat() * 0.35f,       // freq Y
                0.08f + rng.nextFloat() * 0.15f,       // radius ratio
                0.6f + rng.nextFloat() * 0.4f           // base brightness
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        for ((idx, seed) in blobSeeds.withIndex()) {
            val phaseX = seed[0]
            val phaseY = seed[1]
            val freqX = seed[2]
            val freqY = seed[3]
            val radiusRatio = seed[4]
            val brightness = seed[5]

            val cx = size.width * (0.5f + 0.4f * sin((time * freqX + phaseX).toDouble()).toFloat())
            val cy = size.height * (0.5f + 0.4f * cos((time * freqY + phaseY).toDouble()).toFloat())
            val baseRadius = min(size.width, size.height) * radiusRatio

            // Pulsating radius
            val pulse = 1f + 0.15f * sin((time * 1.5f + idx).toDouble()).toFloat()
            val radius = baseRadius * pulse

            // Chrome gradient: bright center, darker edge
            val gray = (brightness * 255).toInt().coerceIn(0, 255)
            val highlight = Color(gray, gray, gray, (90 * config.intensity).toInt().coerceIn(0, 255))
            val edge = Color(gray / 2, gray / 2, gray / 2, (40 * config.intensity).toInt().coerceIn(0, 255))

            val chromeGradient = Brush.radialGradient(
                colors = listOf(highlight, edge, Color.Transparent),
                center = Offset(cx, cy),
                radius = radius
            )
            drawCircle(
                brush = chromeGradient,
                radius = radius,
                center = Offset(cx, cy),
                blendMode = BlendMode.Screen
            )

            // Specular highlight
            val specX = cx - radius * 0.2f
            val specY = cy - radius * 0.2f
            drawCircle(
                color = Color.White.copy(alpha = (0.12f * config.intensity * brightness).coerceIn(0f, 1f)),
                radius = radius * 0.25f,
                center = Offset(specX, specY)
            )
        }

        // Connecting metallic threads between nearby blobs
        for (i in blobSeeds.indices) {
            for (j in i + 1 until blobSeeds.size) {
                val s1 = blobSeeds[i]
                val s2 = blobSeeds[j]
                val x1 = size.width * (0.5f + 0.4f * sin((time * s1[2] + s1[0]).toDouble()).toFloat())
                val y1 = size.height * (0.5f + 0.4f * cos((time * s1[3] + s1[1]).toDouble()).toFloat())
                val x2 = size.width * (0.5f + 0.4f * sin((time * s2[2] + s2[0]).toDouble()).toFloat())
                val y2 = size.height * (0.5f + 0.4f * cos((time * s2[3] + s2[1]).toDouble()).toFloat())

                val dist = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
                val maxDist = min(size.width, size.height) * 0.45f
                if (dist < maxDist) {
                    val lineAlpha = (1f - dist / maxDist) * 0.1f * config.intensity
                    drawLine(
                        color = Color.LightGray.copy(alpha = lineAlpha.coerceIn(0f, 1f)),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 1.5f,
                        blendMode = BlendMode.Screen
                    )
                }
            }
        }
    }
}
