package com.sugarmunch.app.ui.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

val PillShape = RoundedCornerShape(50)

/**
 * Organic blob shape using cubic bezier curves.
 * [seed] varies the blob distortion (0f–1f recommended).
 */
class BlobShape(private val seed: Float = 0f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val rx = w / 2f
        val ry = h / 2f

        // Generate 8 control points around the center with seed-based variation
        val points = 8
        val path = Path()
        val angleStep = (2.0 * PI / points).toFloat()
        val offsets = FloatArray(points) { i ->
            val phase = seed * PI.toFloat() * 2f + i * 1.7f
            0.8f + 0.2f * sin(phase)
        }

        val px = FloatArray(points) { i ->
            cx + rx * offsets[i] * cos(angleStep * i)
        }
        val py = FloatArray(points) { i ->
            cy + ry * offsets[i] * sin(angleStep * i)
        }

        path.moveTo(px[0], py[0])
        for (i in 0 until points) {
            val next = (i + 1) % points
            val cpFactor = 0.45f
            val dx = px[next] - px[i]
            val dy = py[next] - py[i]
            val cp1x = px[i] + dx * cpFactor - dy * 0.15f * (1f + seed * 0.5f)
            val cp1y = py[i] + dy * cpFactor + dx * 0.15f * (1f + seed * 0.5f)
            val cp2x = px[next] - dx * cpFactor - dy * 0.15f * (1f + seed * 0.3f)
            val cp2y = py[next] - dy * cpFactor + dx * 0.15f * (1f + seed * 0.3f)
            path.cubicTo(cp1x, cp1y, cp2x, cp2y, px[next], py[next])
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Rectangle with pinched/twisted ends resembling a candy wrapper.
 */
class CandyWrapperShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val pinch = w * 0.15f
        val notch = h * 0.3f

        val path = Path().apply {
            // Top edge: left pinch → straight across → right pinch
            moveTo(0f, 0f)
            lineTo(pinch, notch)
            lineTo(pinch, 0f)
            lineTo(w - pinch, 0f)
            lineTo(w - pinch, notch)
            lineTo(w, 0f)

            // Right edge
            lineTo(w, h)

            // Bottom edge: right pinch → straight across → left pinch
            lineTo(w - pinch, h - notch)
            lineTo(w - pinch, h)
            lineTo(pinch, h)
            lineTo(pinch, h - notch)
            lineTo(0f, h)

            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Diamond / rotated square shape.
 */
class DiamondShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w / 2f, 0f)
            lineTo(w, h / 2f)
            lineTo(w / 2f, h)
            lineTo(0f, h / 2f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Regular hexagon shape.
 */
class HexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = min(w, h) / 2f

        val path = Path()
        for (i in 0 until 6) {
            val angle = (PI / 3.0 * i - PI / 6.0).toFloat()
            val px = cx + r * cos(angle)
            val py = cy + r * sin(angle)
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Circle with scalloped (wavy) edge.
 */
class ScallopedShape(private val scallops: Int = 12) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val outerR = min(w, h) / 2f
        val innerR = outerR * 0.88f
        val count = scallops.coerceAtLeast(4)
        val angleStep = (2.0 * PI / count).toFloat()

        val path = Path()
        for (i in 0 until count) {
            val startAngle = angleStep * i
            val midAngle = startAngle + angleStep / 2f
            val endAngle = startAngle + angleStep

            val sx = cx + outerR * cos(startAngle)
            val sy = cy + outerR * sin(startAngle)
            val mx = cx + innerR * cos(midAngle)
            val my = cy + innerR * sin(midAngle)
            val ex = cx + outerR * cos(endAngle)
            val ey = cy + outerR * sin(endAngle)

            if (i == 0) path.moveTo(sx, sy)
            path.quadraticBezierTo(mx, my, ex, ey)
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Star with configurable points and inner radius ratio.
 */
class StarShape(
    private val points: Int = 5,
    private val innerRadius: Float = 0.4f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val outerR = min(w, h) / 2f
        val innerR = outerR * innerRadius.coerceIn(0.1f, 0.95f)
        val numPoints = points.coerceAtLeast(3)
        val totalVertices = numPoints * 2
        val angleStep = (PI / numPoints).toFloat()
        // Start at top (-PI/2)
        val startOffset = (-PI / 2.0).toFloat()

        val path = Path()
        for (i in 0 until totalVertices) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = startOffset + angleStep * i
            val px = cx + r * cos(angle)
            val py = cy + r * sin(angle)
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Heart shape using cubic bezier curves.
 */
class HeartShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            // Start at bottom center (tip of heart)
            moveTo(w / 2f, h * 0.9f)

            // Left side curve
            cubicTo(
                w * 0.1f, h * 0.6f,   // control point 1
                0f, h * 0.3f,          // control point 2
                w * 0.15f, h * 0.15f   // end of left bump base
            )
            // Left bump top
            cubicTo(
                w * 0.25f, 0f,
                w * 0.45f, 0f,
                w / 2f, h * 0.2f
            )
            // Right bump top
            cubicTo(
                w * 0.55f, 0f,
                w * 0.75f, 0f,
                w * 0.85f, h * 0.15f
            )
            // Right side curve
            cubicTo(
                w * 1f, h * 0.3f,
                w * 0.9f, h * 0.6f,
                w / 2f, h * 0.9f
            )

            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Rectangle with a wavy top edge.
 */
class WavyRectShape(
    private val waveHeight: Dp = 8.dp,
    private val waveCount: Int = 5
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val waveH = with(density) { waveHeight.toPx() }
        val count = waveCount.coerceAtLeast(1)
        val waveWidth = w / count

        val path = Path().apply {
            // Start at top-left
            moveTo(0f, waveH)

            // Wavy top edge
            for (i in 0 until count) {
                val startX = waveWidth * i
                val midX = startX + waveWidth / 2f
                val endX = startX + waveWidth
                quadraticBezierTo(midX, 0f, endX, waveH)
            }

            // Right, bottom, left edges
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Convenience modifier that applies card-style background, shape, and elevation.
 */
@Composable
fun Modifier.sugarSurface(
    style: CardStyle = CardStyle.ELEVATED,
    shape: Shape = RoundedCornerShape(SugarDimens.Radius.md)
): Modifier {
    val colorScheme = MaterialTheme.colorScheme
    return when (style) {
        CardStyle.FLAT -> this
            .clip(shape)
            .shadow(elevation = SugarDimens.Elevation.none, shape = shape)

        CardStyle.ELEVATED -> this
            .shadow(elevation = SugarDimens.Elevation.low, shape = shape)
            .clip(shape)

        CardStyle.OUTLINED -> this
            .clip(shape)

        CardStyle.GLASSMORPHIC -> this
            .shadow(elevation = SugarDimens.Elevation.subtle, shape = shape)
            .clip(shape)

        CardStyle.NEON -> this
            .shadow(
                elevation = SugarDimens.Elevation.medium,
                shape = shape,
                ambientColor = colorScheme.primary.copy(alpha = 0.4f),
                spotColor = colorScheme.primary.copy(alpha = 0.6f)
            )
            .clip(shape)
    }
}
