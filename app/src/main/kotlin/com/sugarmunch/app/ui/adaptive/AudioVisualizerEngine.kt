/**
 * AudioVisualizerEngine.kt — Phase 5.3
 *
 * Music-reactive visualizer engine for SugarMunch.
 * Provides 8 candy-themed visualizer styles driven by simulated audio data,
 * rendered entirely with Canvas drawing primitives (no AGSL/RuntimeShader).
 */
package com.sugarmunch.app.ui.adaptive

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// ─────────────────────────────────────────────────────────────────────────────
// Visualizer Style Enum
// ─────────────────────────────────────────────────────────────────────────────

enum class VisualizerStyle(val description: String) {
    BARS("Classic vertical frequency bars"),
    WAVEFORM("Smooth waveform with bezier curves"),
    CIRCULAR("Radial frequency bars around a circle"),
    GALAXY("Spiral galaxy of frequency-driven dots"),
    LIQUID("Organic blob shape displaced by bass"),
    FLAME("Flickering flame columns with warm glow"),
    AURORA("Layered translucent aurora waves"),
    CANDY_WAVE("Bouncing candy-colored circles in a wave")
}

// ─────────────────────────────────────────────────────────────────────────────
// Audio Data Model
// ─────────────────────────────────────────────────────────────────────────────

data class AudioData(
    val waveform: FloatArray,
    val fft: FloatArray,
    val amplitude: Float,
    val bassLevel: Float,
    val midLevel: Float,
    val trebleLevel: Float
) {
    companion object {
        val EMPTY = AudioData(
            waveform = FloatArray(256),
            fft = FloatArray(128),
            amplitude = 0f,
            bassLevel = 0f,
            midLevel = 0f,
            trebleLevel = 0f
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioData) return false
        return waveform.contentEquals(other.waveform) &&
                fft.contentEquals(other.fft) &&
                amplitude == other.amplitude &&
                bassLevel == other.bassLevel &&
                midLevel == other.midLevel &&
                trebleLevel == other.trebleLevel
    }

    override fun hashCode(): Int {
        var result = waveform.contentHashCode()
        result = 31 * result + fft.contentHashCode()
        result = 31 * result + amplitude.hashCode()
        result = 31 * result + bassLevel.hashCode()
        result = 31 * result + midLevel.hashCode()
        result = 31 * result + trebleLevel.hashCode()
        return result
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Simulated Audio Source
// ─────────────────────────────────────────────────────────────────────────────

object SimulatedAudioSource {

    private const val TWO_PI = 2.0 * PI

    /**
     * Generates realistic-looking audio data using layered sine/cosine waves.
     * The time parameter drives smooth continuous evolution of the signal.
     */
    fun generateSimulatedData(timeNanos: Long): AudioData {
        val t = timeNanos / 1_000_000_000.0 // seconds

        // -- Waveform: overlay multiple sine waves at musical frequencies --
        val waveform = FloatArray(256) { i ->
            val phase = i / 256.0
            val fundamental = sin(TWO_PI * 2.0 * phase + t * 3.0).toFloat()
            val harmonic2 = 0.6f * sin(TWO_PI * 4.0 * phase + t * 5.5).toFloat()
            val harmonic3 = 0.35f * sin(TWO_PI * 7.0 * phase + t * 2.2).toFloat()
            val vibrato = 0.2f * sin(TWO_PI * 13.0 * phase + t * 8.0).toFloat()
            val sub = 0.4f * cos(TWO_PI * 1.0 * phase + t * 1.5).toFloat()
            val envelope = (0.6f + 0.4f * sin(TWO_PI * 0.3 * t + phase * PI).toFloat())
            ((fundamental + harmonic2 + harmonic3 + vibrato + sub) / 2.55f * envelope)
                .coerceIn(-1f, 1f)
        }

        // -- FFT: simulated frequency bins with bass emphasis and beat pulse --
        val beatPulse = (0.5 + 0.5 * sin(TWO_PI * 1.8 * t)).toFloat() // ~108 BPM
        val halfBeat = (0.5 + 0.5 * sin(TWO_PI * 0.9 * t)).toFloat()

        val fft = FloatArray(128) { i ->
            val freq = i / 128.0
            // Bass hump (low frequencies)
            val bass = (1.0 - freq).pow(3.0).toFloat() * 0.8f * beatPulse
            // Mid presence
            val mid = (sin(PI * freq * 4.0) * 0.4).toFloat().coerceAtLeast(0f) * halfBeat
            // Treble shimmer
            val treble = (freq.pow(0.5) * 0.3 * sin(TWO_PI * 6.0 * t + i * 0.5)).toFloat()
                .coerceAtLeast(0f)
            // Organic variation
            val variation = 0.15f * sin(TWO_PI * (0.5 * t + i * 0.1)).toFloat()
            (bass + mid + treble + variation).coerceIn(0f, 1f)
        }

        // -- Band levels --
        val bassLevel = fft.take(20).average().toFloat().coerceIn(0f, 1f)
        val midLevel = fft.slice(20..70).average().toFloat().coerceIn(0f, 1f)
        val trebleLevel = fft.drop(70).average().toFloat().coerceIn(0f, 1f)
        val amplitude = (bassLevel * 0.5f + midLevel * 0.3f + trebleLevel * 0.2f)
            .coerceIn(0f, 1f)

        return AudioData(
            waveform = waveform,
            fft = fft,
            amplitude = amplitude,
            bassLevel = bassLevel,
            midLevel = midLevel,
            trebleLevel = trebleLevel
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Color Mode
// ─────────────────────────────────────────────────────────────────────────────

enum class ColorMode(val label: String) {
    THEME_COLORS("Theme Colors"),
    SPECTRUM_RAINBOW("Spectrum Rainbow"),
    MONOCHROME("Monochrome"),
    CANDY_MIX("Candy Mix")
}

// ─────────────────────────────────────────────────────────────────────────────
// Palette helpers
// ─────────────────────────────────────────────────────────────────────────────

private val CandyPalette = listOf(
    Color(0xFFFF6B9D), // pink
    Color(0xFFFFA64D), // orange
    Color(0xFFFFE04D), // yellow
    Color(0xFF7BED9F), // mint
    Color(0xFF70A1FF), // blue
    Color(0xFFCF6EFF), // purple
    Color(0xFFFF4757), // cherry
    Color(0xFF2ED573), // lime
)

private fun rainbowColor(fraction: Float): Color {
    val hue = fraction * 360f
    return Color.hsl(hue, 0.85f, 0.55f)
}

private fun colorForBin(
    index: Int,
    total: Int,
    mode: ColorMode,
    primary: Color,
    secondary: Color
): Color = when (mode) {
    ColorMode.THEME_COLORS -> {
        val t = index / total.toFloat()
        lerp(primary, secondary, t)
    }
    ColorMode.SPECTRUM_RAINBOW -> rainbowColor(index / total.toFloat())
    ColorMode.MONOCHROME -> {
        val l = 0.35f + 0.5f * (index / total.toFloat())
        Color.hsl(0f, 0f, l)
    }
    ColorMode.CANDY_MIX -> CandyPalette[index % CandyPalette.size]
}

private fun lerp(a: Color, b: Color, t: Float): Color {
    val clamped = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * clamped,
        green = a.green + (b.green - a.green) * clamped,
        blue = a.blue + (b.blue - a.blue) * clamped,
        alpha = a.alpha + (b.alpha - a.alpha) * clamped
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// AudioVisualizerCanvas — main composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AudioVisualizerCanvas(
    style: VisualizerStyle,
    audioData: AudioData,
    modifier: Modifier = Modifier,
    colorMode: ColorMode = ColorMode.THEME_COLORS,
    sensitivity: Float = 1f,
    backgroundOpacity: Float = 0.1f,
    respondToBass: Boolean = true,
    respondToMids: Boolean = true,
    respondToTreble: Boolean = true
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier) {
        // Background fill
        drawRect(color = surface.copy(alpha = backgroundOpacity))

        // Apply sensitivity and band filters
        val effectiveFft = audioData.fft.mapIndexed { i, v ->
            val binFraction = i / audioData.fft.size.toFloat()
            val bandMultiplier = when {
                binFraction < 0.16f -> if (respondToBass) 1f else 0.1f
                binFraction < 0.55f -> if (respondToMids) 1f else 0.1f
                else -> if (respondToTreble) 1f else 0.1f
            }
            (v * sensitivity * bandMultiplier).coerceIn(0f, 1f)
        }.toFloatArray()

        val effectiveWaveform = audioData.waveform.map { (it * sensitivity).coerceIn(-1f, 1f) }
            .toFloatArray()

        when (style) {
            VisualizerStyle.BARS -> drawBars(effectiveFft, colorMode, primary, secondary)
            VisualizerStyle.WAVEFORM -> drawWaveform(
                effectiveWaveform, primary, secondary, colorMode
            )
            VisualizerStyle.CIRCULAR -> drawCircular(effectiveFft, colorMode, primary, secondary)
            VisualizerStyle.GALAXY -> drawGalaxy(
                effectiveFft, audioData.amplitude * sensitivity, colorMode, primary, secondary
            )
            VisualizerStyle.LIQUID -> drawLiquid(effectiveFft, primary, secondary, colorMode)
            VisualizerStyle.FLAME -> drawFlame(effectiveFft, colorMode)
            VisualizerStyle.AURORA -> drawAurora(effectiveFft, colorMode, primary, tertiary)
            VisualizerStyle.CANDY_WAVE -> drawCandyWave(
                effectiveFft, audioData.bassLevel * sensitivity, colorMode
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Drawing implementations
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawBars(
    fft: FloatArray,
    colorMode: ColorMode,
    primary: Color,
    secondary: Color
) {
    val barCount = min(fft.size, 64)
    val gap = 2.dp.toPx()
    val totalGap = gap * (barCount - 1)
    val barWidth = (size.width - totalGap) / barCount

    for (i in 0 until barCount) {
        val magnitude = fft[i * fft.size / barCount]
        val barHeight = magnitude * size.height * 0.9f
        val x = i * (barWidth + gap)
        val color = colorForBin(i, barCount, colorMode, primary, secondary)

        // Gradient bar from bottom
        val brush = Brush.verticalGradient(
            colors = listOf(color, color.copy(alpha = 0.3f)),
            startY = size.height,
            endY = size.height - barHeight
        )
        drawRect(
            brush = brush,
            topLeft = Offset(x, size.height - barHeight),
            size = Size(barWidth, barHeight)
        )

        // Bright cap on top
        drawRect(
            color = color,
            topLeft = Offset(x, size.height - barHeight),
            size = Size(barWidth, min(3.dp.toPx(), barHeight))
        )
    }
}

private fun DrawScope.drawWaveform(
    waveform: FloatArray,
    primary: Color,
    secondary: Color,
    colorMode: ColorMode
) {
    val midY = size.height / 2f
    val step = size.width / (waveform.size - 1).toFloat()
    val strokeColor = when (colorMode) {
        ColorMode.THEME_COLORS -> primary
        ColorMode.SPECTRUM_RAINBOW -> Color(0xFF00BFFF)
        ColorMode.MONOCHROME -> Color.White
        ColorMode.CANDY_MIX -> Color(0xFFFF6B9D)
    }

    val path = Path()
    val fillPath = Path()

    // Build bezier curve through samples
    path.moveTo(0f, midY + waveform[0] * midY * 0.8f)
    fillPath.moveTo(0f, size.height)
    fillPath.lineTo(0f, midY + waveform[0] * midY * 0.8f)

    for (i in 1 until waveform.size) {
        val x = i * step
        val y = midY + waveform[i] * midY * 0.8f
        val prevX = (i - 1) * step
        val prevY = midY + waveform[i - 1] * midY * 0.8f
        val cpX = (prevX + x) / 2f
        path.cubicTo(cpX, prevY, cpX, y, x, y)
        fillPath.cubicTo(cpX, prevY, cpX, y, x, y)
    }

    fillPath.lineTo(size.width, size.height)
    fillPath.close()

    // Gradient fill below curve
    val fillBrush = Brush.verticalGradient(
        colors = listOf(strokeColor.copy(alpha = 0.35f), Color.Transparent),
        startY = midY - size.height * 0.3f,
        endY = size.height
    )
    drawPath(fillPath, brush = fillBrush)

    // Stroke
    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawCircular(
    fft: FloatArray,
    colorMode: ColorMode,
    primary: Color,
    secondary: Color
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val baseRadius = min(cx, cy) * 0.35f
    val maxBarLen = min(cx, cy) * 0.5f
    val binCount = min(fft.size, 72)

    // Inner circle glow
    drawCircle(
        color = primary.copy(alpha = 0.12f),
        radius = baseRadius,
        center = Offset(cx, cy)
    )

    for (i in 0 until binCount) {
        val angle = (TWO_PI * i / binCount).toFloat() - (PI / 2f).toFloat()
        val magnitude = fft[i * fft.size / binCount]
        val barLen = magnitude * maxBarLen

        val x0 = cx + cos(angle) * baseRadius
        val y0 = cy + sin(angle) * baseRadius
        val x1 = cx + cos(angle) * (baseRadius + barLen)
        val y1 = cy + sin(angle) * (baseRadius + barLen)

        val color = colorForBin(i, binCount, colorMode, primary, secondary)
        drawLine(
            color = color,
            start = Offset(x0, y0),
            end = Offset(x1, y1),
            strokeWidth = max(2f, (TWO_PI.toFloat() * baseRadius / binCount) * 0.5f),
            cap = StrokeCap.Round
        )
    }

    // Centre dot
    drawCircle(color = primary, radius = 4.dp.toPx(), center = Offset(cx, cy))
}

private fun DrawScope.drawGalaxy(
    fft: FloatArray,
    amplitude: Float,
    colorMode: ColorMode,
    primary: Color,
    secondary: Color
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val maxR = min(cx, cy) * 0.85f
    val dotCount = min(fft.size, 100)

    for (i in 0 until dotCount) {
        val t = i / dotCount.toFloat()
        // Spiral arm
        val spiralAngle = t * 4f * PI.toFloat() + amplitude * 2f
        val spiralR = t * maxR * (0.6f + 0.4f * fft[i * fft.size / dotCount])

        val x = cx + cos(spiralAngle) * spiralR
        val y = cy + sin(spiralAngle) * spiralR

        val mag = fft[i * fft.size / dotCount]
        val dotSize = (1.5f + mag * 6f).dp.toPx()
        val alpha = 0.3f + mag * 0.7f

        val color = colorForBin(i, dotCount, colorMode, primary, secondary)
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = dotSize,
            center = Offset(x, y)
        )
        // Glow halo
        if (mag > 0.4f) {
            drawCircle(
                color = color.copy(alpha = alpha * 0.25f),
                radius = dotSize * 2.2f,
                center = Offset(x, y)
            )
        }
    }
}

private fun DrawScope.drawLiquid(
    fft: FloatArray,
    primary: Color,
    secondary: Color,
    colorMode: ColorMode
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val baseR = min(cx, cy) * 0.35f
    val vertexCount = min(fft.size, 32)

    val path = Path()
    val points = mutableListOf<Offset>()

    for (i in 0 until vertexCount) {
        val angle = (TWO_PI * i / vertexCount).toFloat()
        val displacement = fft[i * fft.size / vertexCount] * baseR * 0.8f
        val r = baseR + displacement
        points.add(Offset(cx + cos(angle) * r, cy + sin(angle) * r))
    }

    // Smooth closed curve through points
    if (points.size >= 3) {
        path.moveTo(points[0].x, points[0].y)
        for (i in points.indices) {
            val p0 = points[i]
            val p1 = points[(i + 1) % points.size]
            val p2 = points[(i + 2) % points.size]
            val cpX = p1.x + (p2.x - p0.x) / 6f
            val cpY = p1.y + (p2.y - p0.y) / 6f
            val cp2X = p1.x - (p2.x - p0.x) / 6f
            val cp2Y = p1.y - (p2.y - p0.y) / 6f
            // Smooth connection
            val midX = (p0.x + p1.x) / 2f
            val midY = (p0.y + p1.y) / 2f
            path.quadraticTo(p0.x, p0.y, midX, midY)
        }
        path.close()
    }

    val fillColor = when (colorMode) {
        ColorMode.THEME_COLORS -> primary
        ColorMode.CANDY_MIX -> Color(0xFFFF6B9D)
        ColorMode.SPECTRUM_RAINBOW -> Color(0xFF7B68EE)
        ColorMode.MONOCHROME -> Color.White
    }

    drawPath(path, color = fillColor.copy(alpha = 0.25f), style = Fill)
    drawPath(
        path, color = fillColor.copy(alpha = 0.7f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Inner glow blob (smaller, brighter)
    val innerPath = Path()
    val innerPoints = points.map { pt ->
        val dx = pt.x - cx
        val dy = pt.y - cy
        Offset(cx + dx * 0.5f, cy + dy * 0.5f)
    }
    if (innerPoints.size >= 3) {
        innerPath.moveTo(innerPoints[0].x, innerPoints[0].y)
        for (i in innerPoints.indices) {
            val p0 = innerPoints[i]
            val p1 = innerPoints[(i + 1) % innerPoints.size]
            val midX = (p0.x + p1.x) / 2f
            val midY = (p0.y + p1.y) / 2f
            innerPath.quadraticTo(p0.x, p0.y, midX, midY)
        }
        innerPath.close()
    }
    drawPath(innerPath, color = fillColor.copy(alpha = 0.15f), style = Fill)
}

private fun DrawScope.drawFlame(fft: FloatArray, colorMode: ColorMode) {
    val columnCount = min(fft.size, 48)
    val colWidth = size.width / columnCount

    val warmColors = listOf(
        Color(0xFFFF4500), // orange-red
        Color(0xFFFF6600), // orange
        Color(0xFFFF8C00), // dark-orange
        Color(0xFFFFD700), // gold
        Color(0xFFFFFF00), // yellow
    )

    for (i in 0 until columnCount) {
        val mag = fft[i * fft.size / columnCount]
        val colHeight = mag * size.height * 0.85f
        val x = i * colWidth
        val baseY = size.height

        // Flickering effect via slight height randomness seeded by index
        val flicker = 1f + 0.08f * sin((i * 7.3f + mag * 20f).toDouble()).toFloat()
        val h = colHeight * flicker

        val topColor = when (colorMode) {
            ColorMode.CANDY_MIX -> CandyPalette[i % CandyPalette.size]
            ColorMode.SPECTRUM_RAINBOW -> rainbowColor(i / columnCount.toFloat())
            ColorMode.MONOCHROME -> Color.White
            ColorMode.THEME_COLORS -> warmColors[(mag * 4).toInt().coerceIn(0, 4)]
        }
        val brush = Brush.verticalGradient(
            colors = listOf(topColor.copy(alpha = 0.9f), Color(0xFFFF4500).copy(alpha = 0.4f), Color.Transparent),
            startY = baseY - h,
            endY = baseY
        )

        drawRect(
            brush = brush,
            topLeft = Offset(x, baseY - h),
            size = Size(colWidth - 1f, h)
        )

        // Bright tip
        if (mag > 0.3f) {
            drawCircle(
                color = topColor.copy(alpha = 0.6f),
                radius = colWidth * 0.6f,
                center = Offset(x + colWidth / 2f, baseY - h)
            )
        }
    }
}

private fun DrawScope.drawAurora(
    fft: FloatArray,
    colorMode: ColorMode,
    primary: Color,
    tertiary: Color
) {
    val layerCount = 5
    val auroraColors = when (colorMode) {
        ColorMode.THEME_COLORS -> listOf(
            primary.copy(alpha = 0.2f),
            tertiary.copy(alpha = 0.15f),
            primary.copy(alpha = 0.1f),
            tertiary.copy(alpha = 0.12f),
            primary.copy(alpha = 0.08f)
        )
        ColorMode.SPECTRUM_RAINBOW -> (0 until layerCount).map {
            rainbowColor(it / layerCount.toFloat()).copy(alpha = 0.18f)
        }
        ColorMode.MONOCHROME -> (0 until layerCount).map {
            Color.White.copy(alpha = 0.06f + it * 0.03f)
        }
        ColorMode.CANDY_MIX -> CandyPalette.take(layerCount).map { it.copy(alpha = 0.18f) }
    }

    for (layer in 0 until layerCount) {
        val path = Path()
        val baseY = size.height * (0.25f + layer * 0.12f)
        val amplitudeScale = 0.12f + layer * 0.04f
        val freqOffset = layer * fft.size / layerCount

        path.moveTo(0f, baseY)
        val step = size.width / 100f
        for (s in 0..100) {
            val x = s * step
            val fftIdx = ((s / 100f) * fft.size).toInt().coerceIn(0, fft.size - 1)
            val fftVal = fft[(fftIdx + freqOffset) % fft.size]
            val sineWave = sin(TWO_PI * s / 50.0 + layer * 1.2).toFloat()
            val y = baseY + (sineWave * size.height * amplitudeScale * (0.5f + fftVal))
            path.lineTo(x, y)
        }

        // Close to bottom
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()

        drawPath(path, color = auroraColors[layer], style = Fill)
    }
}

private fun DrawScope.drawCandyWave(
    fft: FloatArray,
    bassLevel: Float,
    colorMode: ColorMode
) {
    val circleCount = min(fft.size, 24)
    val spacing = size.width / (circleCount + 1)

    for (i in 0 until circleCount) {
        val mag = fft[i * fft.size / circleCount]
        // Wave pattern: sine offset + beat bounce
        val waveY = sin(TWO_PI * i / circleCount.toFloat() + bassLevel * PI).toFloat()
        val bounceOffset = mag * size.height * 0.25f + bassLevel * size.height * 0.08f
        val x = spacing * (i + 1)
        val y = size.height * 0.55f + waveY * size.height * 0.15f - bounceOffset

        val radius = (8f + mag * 24f).dp.toPx() * 0.5f
        val color = when (colorMode) {
            ColorMode.CANDY_MIX -> CandyPalette[i % CandyPalette.size]
            ColorMode.THEME_COLORS -> CandyPalette[i % CandyPalette.size]
            ColorMode.SPECTRUM_RAINBOW -> rainbowColor(i / circleCount.toFloat())
            ColorMode.MONOCHROME -> Color.White
        }

        // Shadow
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = radius * 1.4f,
            center = Offset(x, y + 4.dp.toPx())
        )
        // Main candy circle
        drawCircle(color = color, radius = radius, center = Offset(x, y))
        // Highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = radius * 0.35f,
            center = Offset(x - radius * 0.22f, y - radius * 0.22f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VisualizerSettingsPanel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VisualizerSettingsPanel(
    selectedStyle: VisualizerStyle,
    onStyleSelected: (VisualizerStyle) -> Unit,
    sensitivity: Float,
    onSensitivityChanged: (Float) -> Unit,
    colorMode: ColorMode,
    onColorModeChanged: (ColorMode) -> Unit,
    backgroundOpacity: Float,
    onBackgroundOpacityChanged: (Float) -> Unit,
    respondToBass: Boolean,
    onRespondToBassChanged: (Boolean) -> Unit,
    respondToMids: Boolean,
    onRespondToMidsChanged: (Boolean) -> Unit,
    respondToTreble: Boolean,
    onRespondToTrebleChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var audioData by remember { mutableStateOf(AudioData.EMPTY) }

    // Animate the mini preview
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                audioData = SimulatedAudioSource.generateSimulatedData(frameTimeNanos)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // -- Live Mini Preview --
        Text(
            text = "Preview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            AudioVisualizerCanvas(
                style = selectedStyle,
                audioData = audioData,
                colorMode = colorMode,
                sensitivity = sensitivity,
                backgroundOpacity = backgroundOpacity,
                respondToBass = respondToBass,
                respondToMids = respondToMids,
                respondToTreble = respondToTreble,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(20.dp))

        // -- Style Picker (2x4 grid) --
        Text(
            text = "Style",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(VisualizerStyle.entries.toList()) { style ->
                val isSelected = style == selectedStyle
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                    animationSpec = tween(200),
                    label = "border"
                )
                Card(
                    modifier = Modifier
                        .aspectRatio(0.85f)
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { onStyleSelected(style) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        // Mini icon indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = style.name.lowercase().replaceFirstChar { it.uppercase() }
                                .replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // -- Sensitivity Slider --
        Text("Sensitivity", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("0.5x", style = MaterialTheme.typography.labelSmall)
            Slider(
                value = sensitivity,
                onValueChange = onSensitivityChanged,
                valueRange = 0.5f..2f,
                modifier = Modifier.weight(1f)
            )
            Text("2x", style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(16.dp))

        // -- Color Mode Picker --
        Text("Color Mode", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ColorMode.entries.forEach { mode ->
                FilterChip(
                    selected = mode == colorMode,
                    onClick = { onColorModeChanged(mode) },
                    label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // -- Background Opacity --
        Text("Background Opacity", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Slider(
            value = backgroundOpacity,
            onValueChange = onBackgroundOpacityChanged,
            valueRange = 0f..1f
        )

        Spacer(Modifier.height(16.dp))

        // -- Respond To toggles --
        Text("Respond To", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = respondToBass, onCheckedChange = onRespondToBassChanged)
                Text("Bass", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = respondToMids, onCheckedChange = onRespondToMidsChanged)
                Text("Mids", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = respondToTreble, onCheckedChange = onRespondToTrebleChanged)
                Text("Treble", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VisualizerDemo — Full-screen demo composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VisualizerDemo(modifier: Modifier = Modifier) {
    var selectedStyle by remember { mutableStateOf(VisualizerStyle.BARS) }
    var audioData by remember { mutableStateOf(AudioData.EMPTY) }
    var sensitivity by remember { mutableFloatStateOf(1f) }
    var colorMode by remember { mutableStateOf(ColorMode.CANDY_MIX) }

    // 60fps animation loop using withFrameNanos
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                audioData = SimulatedAudioSource.generateSimulatedData(frameTimeNanos)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Main visualizer area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            AudioVisualizerCanvas(
                style = selectedStyle,
                audioData = audioData,
                colorMode = colorMode,
                sensitivity = sensitivity,
                backgroundOpacity = 0.05f,
                modifier = Modifier.fillMaxSize()
            )

            // Style label overlay
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = selectedStyle.description,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Sensitivity quick-bar
        Surface(tonalElevation = 2.dp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Sensitivity", style = MaterialTheme.typography.labelSmall)
                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    valueRange = 0.5f..2f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                Text(
                    "${String.format("%.1f", sensitivity)}x",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Style switch buttons row
        Surface(tonalElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                VisualizerStyle.entries.forEach { style ->
                    val isSelected = style == selectedStyle
                    val bgAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(200),
                        label = "bg"
                    )
                    FilledTonalButton(
                        onClick = { selectedStyle = style },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = style.name.take(4),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Color mode row
        Surface(tonalElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ColorMode.entries.forEach { mode ->
                    FilterChip(
                        selected = mode == colorMode,
                        onClick = { colorMode = mode },
                        label = {
                            Text(mode.label, style = MaterialTheme.typography.labelSmall)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
