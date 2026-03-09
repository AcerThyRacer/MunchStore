package com.sugarmunch.app.ui.scenes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawArc
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the Space Candy scene with stars, shooting stars, nebula clouds, and candy planets.
 *
 * This scene features a deep space theme with a static star field, twinkling stars,
 * periodic shooting stars, colorful nebula clouds, and candy-themed planets with rings.
 * Touch interaction creates a star burst effect.
 *
 * @param t Time in seconds for animation
 * @param scrollOffset Scroll-based parallax offset
 * @param touch Current touch position or null if no active touch
 * @param touchAge Age of current touch in milliseconds
 * @param state Pre-computed scene state with random values
 */
private fun DrawScope.drawSpaceCandy(
    t: Float,
    scrollOffset: Float,
    touch: Offset?,
    touchAge: Long,
    state: SceneState
) {
    val w = size.width
    val h = size.height

    drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF0D0D2B), Color(0xFF000010))))

    // Static star field
    for (i in 0 until 60) {
        val sx = state.offsets[i % state.offsets.size].x * w + scrollOffset * (0.02f + state.floats[i % state.floats.size] * 0.03f)
        val sy = state.offsets[i % state.offsets.size].y * h
        drawCircle(Color.White.copy(alpha = 0.5f + state.floats[i % state.floats.size] * 0.5f), 1f, Offset(sx % w, sy))
    }

    // Twinkling stars
    for (i in 0 until 10) {
        val sx = state.offsets[i + 60 % state.offsets.size].x * w
        val sy = state.offsets[i + 60 % state.offsets.size].y * h
        val twinkle = (sin((t * 2f + state.floats[i] * 6f).toDouble()).toFloat() + 1f) / 2f
        drawCircle(Color.White.copy(alpha = twinkle * 0.8f), 2f + twinkle, Offset(sx, sy + scrollOffset * 0.01f))
    }

    // Shooting stars
    val shootCycle = (t * 0.4f) % 4f
    if (shootCycle < 0.8f) {
        val prog = shootCycle / 0.8f
        val sx = w * 0.1f + prog * w * 0.7f
        val sy = h * 0.15f + prog * h * 0.25f
        val alpha = (1f - prog).coerceIn(0f, 1f) * 0.8f
        drawLine(Color.White.copy(alpha = alpha), Offset(sx, sy), Offset(sx - 30f, sy - 15f), strokeWidth = 1.5f)
    }

    // Nebula clouds
    val nebulaColors = listOf(Color(0xFF7B1FA2), Color(0xFF1565C0), Color(0xFFC62828))
    for (i in 0 until 3) {
        val nx = state.offsets[i + 10].x * w + scrollOffset * 0.03f
        val ny = state.offsets[i + 10].y * h
        drawCircle(nebulaColors[i].copy(alpha = 0.08f), w * 0.2f, Offset(nx, ny))
    }

    // Candy planets with rings
    for (i in 0 until 3) {
        val px = (state.offsets[i + 30].x * w + t * 3f * (i + 1)) % (w + 60f) - 30f
        val py = state.offsets[i + 30].y * h * 0.8f + h * 0.1f + scrollOffset * 0.04f
        val planetRadius = 14f + state.floats[i + 30] * 10f
        val planetColor = listOf(Color(0xFFE91E63), Color(0xFF8BC34A), Color(0xFFFF9800))[i]
        drawCircle(planetColor, planetRadius, Offset(px, py))
        // Ring
        drawArc(
            color = planetColor.copy(alpha = 0.4f),
            startAngle = 0f, sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(px - planetRadius * 1.6f, py - planetRadius * 0.3f),
            size = Size(planetRadius * 3.2f, planetRadius * 0.6f),
            style = Stroke(2f)
        )
    }

    // Touch – star burst
    if (touch != null && touchAge < 800L) {
        val prog = touchAge / 800f
        val particleCount = 12
        for (p in 0 until particleCount) {
            val angle = (p * 360f / particleCount) * (PI.toFloat() / 180f)
            val dist = prog * 60f
            val px = touch.x + cos(angle) * dist
            val py = touch.y + sin(angle) * dist
            val alpha = (1f - prog).coerceIn(0f, 1f)
            drawCircle(Color.Yellow.copy(alpha = alpha), 2f + (1f - prog) * 2f, Offset(px, py))
        }
    }
}
