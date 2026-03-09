package com.sugarmunch.app.ui.scenes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

/**
 * Draws the Sugar Ocean scene with layered waves, bubbles, and floating candy.
 *
 * This scene features an underwater theme with three parallax wave layers,
 * rising bubbles, candy floating on the surface, and touch-activated ripples.
 *
 * @param t Time in seconds for animation
 * @param scrollOffset Scroll-based parallax offset
 * @param touch Current touch position or null if no active touch
 * @param touchAge Age of current touch in milliseconds
 * @param state Pre-computed scene state with random values
 */
private fun DrawScope.drawSugarOcean(
    t: Float,
    scrollOffset: Float,
    touch: Offset?,
    touchAge: Long,
    state: SceneState
) {
    val w = size.width
    val h = size.height

    drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFF01579B))))

    // Waves – 3 layers
    val waveColors = listOf(
        Color(0xFF4FC3F7).copy(alpha = 0.35f),
        Color(0xFF29B6F6).copy(alpha = 0.45f),
        Color(0xFF0288D1).copy(alpha = 0.55f)
    )
    for (layer in 0..2) {
        val amplitude = 12f + layer * 8f
        val freq = 0.008f + layer * 0.003f
        val speedMul = 0.6f + layer * 0.4f
        val baseY = h * (0.45f + layer * 0.12f) + scrollOffset * (0.1f + layer * 0.05f)
        val path = Path().apply {
            moveTo(0f, h)
            lineTo(0f, baseY)
            var x = 0f
            while (x <= w) {
                val y = baseY + sin((x * freq + t * speedMul).toDouble()).toFloat() * amplitude
                lineTo(x, y)
                x += 4f
            }
            lineTo(w, h)
            close()
        }
        drawPath(path, waveColors[layer])
    }

    // Bubbles
    for (i in 0 until 15) {
        val bx = state.offsets[i].x * w + sin((t * 0.5f + state.floats[i] * 6f).toDouble()).toFloat() * 10f
        val by = h - ((t * 30f * (0.5f + state.floats[i]) + state.offsets[i].y * h) % (h * 1.2f))
        val radius = 3f + state.floats[i] * 5f
        val alpha = if (by < 0f || by > h) 0f else 0.6f
        drawCircle(Color.White.copy(alpha = alpha), radius, Offset(bx, by))
    }

    // Floating candy on surface
    for (i in 0 until 5) {
        val cx = (state.offsets[i + 20].x * w + t * 15f) % (w + 40f) - 20f
        val surfaceY = h * 0.45f + sin((cx * 0.008f + t * 0.6f).toDouble()).toFloat() * 12f
        drawCircle(Color(0xFFE91E63), 8f, Offset(cx, surfaceY - 10f + scrollOffset * 0.1f))
    }

    // Touch ripple
    if (touch != null && touchAge < 1200L) {
        val rippleProgress = touchAge / 1200f
        val radius = rippleProgress * 80f
        val alpha = (1f - rippleProgress).coerceIn(0f, 1f) * 0.5f
        drawCircle(Color.White.copy(alpha = alpha), radius, touch, style = Stroke(2f))
        if (rippleProgress > 0.3f) {
            val r2 = (rippleProgress - 0.3f) / 0.7f * 80f
            drawCircle(Color.White.copy(alpha = alpha * 0.6f), r2, touch, style = Stroke(1.5f))
        }
    }
}
