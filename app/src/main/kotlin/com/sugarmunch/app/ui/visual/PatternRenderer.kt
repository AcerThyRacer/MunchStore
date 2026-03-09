package com.sugarmunch.app.ui.visual

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class PatternType {
    CANDY_STRIPES,
    POLKA_DOTS,
    CHEVRONS,
    WAVES,
    CONFETTI_SCATTER,
    DIAGONAL_LINES,
    HONEYCOMB,
    HEARTS,
    STARS,
    CROSSHATCH
}

data class PatternSpec(
    val type: PatternType,
    val primaryColor: Color,
    val secondaryColor: Color = Color.Transparent,
    val scale: Float = 1f,
    val opacity: Float = 0.15f,
    val rotation: Float = 0f
)

fun DrawScope.drawPattern(spec: PatternSpec) {
    val patternColor = spec.primaryColor.copy(alpha = spec.opacity)
    val secondaryPatternColor = spec.secondaryColor.copy(
        alpha = if (spec.secondaryColor == Color.Transparent) 0f else spec.opacity
    )

    if (spec.rotation != 0f) {
        rotate(degrees = spec.rotation) {
            drawPatternContent(spec.type, patternColor, secondaryPatternColor, spec.scale)
        }
    } else {
        drawPatternContent(spec.type, patternColor, secondaryPatternColor, spec.scale)
    }
}

private fun DrawScope.drawPatternContent(
    type: PatternType,
    primaryColor: Color,
    secondaryColor: Color,
    scale: Float
) {
    when (type) {
        PatternType.CANDY_STRIPES -> drawCandyStripes(primaryColor, secondaryColor, scale)
        PatternType.POLKA_DOTS -> drawPolkaDots(primaryColor, scale)
        PatternType.CHEVRONS -> drawChevrons(primaryColor, scale)
        PatternType.WAVES -> drawWaves(primaryColor, scale)
        PatternType.CONFETTI_SCATTER -> drawConfettiScatter(primaryColor, secondaryColor, scale)
        PatternType.DIAGONAL_LINES -> drawDiagonalLines(primaryColor, scale)
        PatternType.HONEYCOMB -> drawHoneycomb(primaryColor, scale)
        PatternType.HEARTS -> drawHearts(primaryColor, scale)
        PatternType.STARS -> drawStars(primaryColor, scale)
        PatternType.CROSSHATCH -> drawCrosshatch(primaryColor, scale)
    }
}

private fun DrawScope.drawCandyStripes(primary: Color, secondary: Color, scale: Float) {
    val stripeWidth = 30f * scale
    val totalWidth = size.width + size.height
    val stripeCount = (totalWidth / stripeWidth).toInt() + 2

    for (i in 0 until stripeCount) {
        if (i % 2 == 0) {
            val x = i * stripeWidth - size.height
            val path = Path().apply {
                moveTo(x, 0f)
                lineTo(x + stripeWidth, 0f)
                lineTo(x + stripeWidth + size.height, size.height)
                lineTo(x + size.height, size.height)
                close()
            }
            drawPath(path, color = primary)
            if (secondary != Color.Transparent) {
                val altPath = Path().apply {
                    moveTo(x + stripeWidth, 0f)
                    lineTo(x + stripeWidth * 2, 0f)
                    lineTo(x + stripeWidth * 2 + size.height, size.height)
                    lineTo(x + stripeWidth + size.height, size.height)
                    close()
                }
                drawPath(altPath, color = secondary)
            }
        }
    }
}

private fun DrawScope.drawPolkaDots(color: Color, scale: Float) {
    val dotRadius = 8f * scale
    val spacing = 40f * scale
    val cols = (size.width / spacing).toInt() + 2
    val rows = (size.height / spacing).toInt() + 2

    for (row in 0..rows) {
        val offsetX = if (row % 2 == 0) 0f else spacing / 2f
        for (col in 0..cols) {
            val cx = col * spacing + offsetX
            val cy = row * spacing
            drawCircle(color = color, radius = dotRadius, center = Offset(cx, cy))
        }
    }
}

private fun DrawScope.drawChevrons(color: Color, scale: Float) {
    val chevronHeight = 30f * scale
    val chevronWidth = 40f * scale
    val strokeWidth = 3f * scale
    val rows = (size.height / chevronHeight).toInt() + 2
    val cols = (size.width / chevronWidth).toInt() + 2

    for (row in 0..rows) {
        for (col in 0..cols) {
            val x = col * chevronWidth
            val y = row * chevronHeight
            val path = Path().apply {
                moveTo(x, y + chevronHeight / 2f)
                lineTo(x + chevronWidth / 2f, y)
                lineTo(x + chevronWidth, y + chevronHeight / 2f)
            }
            drawPath(path, color = color, style = Stroke(width = strokeWidth))
        }
    }
}

private fun DrawScope.drawWaves(color: Color, scale: Float) {
    val waveHeight = 20f * scale
    val waveLength = 60f * scale
    val strokeWidth = 3f * scale
    val rowSpacing = 40f * scale
    val rows = (size.height / rowSpacing).toInt() + 2

    for (row in 0..rows) {
        val baseY = row * rowSpacing
        val path = Path().apply {
            moveTo(0f, baseY)
            var x = 0f
            while (x < size.width + waveLength) {
                val cp1x = x + waveLength / 4f
                val cp1y = baseY - waveHeight
                val cp2x = x + waveLength * 3f / 4f
                val cp2y = baseY + waveHeight
                val endX = x + waveLength
                cubicTo(cp1x, cp1y, cp2x, cp2y, endX, baseY)
                x += waveLength
            }
        }
        drawPath(path, color = color, style = Stroke(width = strokeWidth))
    }
}

private fun DrawScope.drawConfettiScatter(primary: Color, secondary: Color, scale: Float) {
    val random = Random(42)
    val confettiCount = ((size.width * size.height) / (8000f / (scale * scale))).toInt().coerceIn(20, 300)
    val confettiSize = 8f * scale

    for (i in 0 until confettiCount) {
        val x = random.nextFloat() * size.width
        val y = random.nextFloat() * size.height
        val rotation = random.nextFloat() * 360f
        val w = confettiSize * (0.5f + random.nextFloat())
        val h = confettiSize * (0.3f + random.nextFloat() * 0.5f)
        val confettiColor = if (random.nextBoolean() && secondary != Color.Transparent) secondary else primary

        withTransform({
            rotate(degrees = rotation, pivot = Offset(x, y))
        }) {
            drawRect(
                color = confettiColor,
                topLeft = Offset(x - w / 2f, y - h / 2f),
                size = Size(w, h)
            )
        }
    }
}

private fun DrawScope.drawDiagonalLines(color: Color, scale: Float) {
    val spacing = 25f * scale
    val strokeWidth = 1.5f * scale
    val totalSpan = size.width + size.height

    var offset = 0f
    while (offset < totalSpan) {
        drawLine(
            color = color,
            start = Offset(offset, 0f),
            end = Offset(0f, offset),
            strokeWidth = strokeWidth
        )
        offset += spacing
    }
}

private fun DrawScope.drawHoneycomb(color: Color, scale: Float) {
    val hexRadius = 20f * scale
    val strokeWidth = 1.5f * scale
    val hexWidth = hexRadius * 2f
    val hexHeight = hexRadius * sqrt(3f)
    val cols = (size.width / (hexWidth * 0.75f)).toInt() + 2
    val rows = (size.height / hexHeight).toInt() + 2

    for (row in 0..rows) {
        for (col in 0..cols) {
            val cx = col * hexWidth * 0.75f
            val cy = row * hexHeight + if (col % 2 == 1) hexHeight / 2f else 0f
            val path = Path().apply {
                for (i in 0 until 6) {
                    val angle = (PI / 3.0 * i - PI / 6.0).toFloat()
                    val px = cx + hexRadius * cos(angle)
                    val py = cy + hexRadius * sin(angle)
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(path, color = color, style = Stroke(width = strokeWidth))
        }
    }
}

private fun DrawScope.drawHearts(color: Color, scale: Float) {
    val heartSize = 16f * scale
    val spacing = 50f * scale
    val cols = (size.width / spacing).toInt() + 2
    val rows = (size.height / spacing).toInt() + 2

    for (row in 0..rows) {
        val offsetX = if (row % 2 == 0) 0f else spacing / 2f
        for (col in 0..cols) {
            val cx = col * spacing + offsetX
            val cy = row * spacing
            drawHeart(color, Offset(cx, cy), heartSize)
        }
    }
}

private fun DrawScope.drawHeart(color: Color, center: Offset, size: Float) {
    val path = Path().apply {
        val x = center.x
        val y = center.y
        val s = size / 2f
        moveTo(x, y + s * 0.6f)
        // Left curve
        cubicTo(
            x - s * 1.2f, y - s * 0.2f,
            x - s * 0.6f, y - s * 1.0f,
            x, y - s * 0.4f
        )
        // Right curve
        cubicTo(
            x + s * 0.6f, y - s * 1.0f,
            x + s * 1.2f, y - s * 0.2f,
            x, y + s * 0.6f
        )
        close()
    }
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawStars(color: Color, scale: Float) {
    val random = Random(123)
    val starCount = ((size.width * size.height) / (12000f / (scale * scale))).toInt().coerceIn(10, 150)
    val baseStarSize = 10f * scale

    for (i in 0 until starCount) {
        val cx = random.nextFloat() * size.width
        val cy = random.nextFloat() * size.height
        val starSize = baseStarSize * (0.5f + random.nextFloat())
        drawStar(color, Offset(cx, cy), starSize, 5)
    }
}

private fun DrawScope.drawStar(color: Color, center: Offset, radius: Float, points: Int) {
    val path = Path().apply {
        val innerRadius = radius * 0.4f
        for (i in 0 until points * 2) {
            val angle = (PI / points * i - PI / 2).toFloat()
            val r = if (i % 2 == 0) radius else innerRadius
            val px = center.x + r * cos(angle)
            val py = center.y + r * sin(angle)
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawCrosshatch(color: Color, scale: Float) {
    val spacing = 25f * scale
    val strokeWidth = 1f * scale
    val totalSpan = size.width + size.height

    // Lines going top-left to bottom-right
    var offset = 0f
    while (offset < totalSpan) {
        drawLine(
            color = color,
            start = Offset(offset, 0f),
            end = Offset(0f, offset),
            strokeWidth = strokeWidth
        )
        offset += spacing
    }

    // Lines going top-right to bottom-left
    offset = 0f
    while (offset < totalSpan) {
        drawLine(
            color = color,
            start = Offset(size.width - offset, 0f),
            end = Offset(size.width, offset),
            strokeWidth = strokeWidth
        )
        offset += spacing
    }
}

@Composable
fun PatternOverlay(
    spec: PatternSpec,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawPattern(spec)
    }
}

@Composable
fun GradientWithPattern(
    gradient: GradientSpec,
    pattern: PatternSpec?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        GradientBackground(
            spec = gradient,
            modifier = Modifier.fillMaxSize()
        )
        if (pattern != null) {
            PatternOverlay(
                spec = pattern,
                modifier = Modifier.fillMaxSize()
            )
        }
        content()
    }
}
