package com.sugarmunch.app.ui.scenes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

/**
 * Draws the Neon City scene with buildings, scrolling signs, rain, and lightning.
 *
 * This scene features a cyberpunk cityscape with illuminated buildings,
 * a scrolling neon banner, falling rain, periodic lightning flashes,
 * and touch-activated neon glow effects.
 *
 * @param t Time in seconds for animation
 * @param scrollOffset Scroll-based parallax offset
 * @param touch Current touch position or null if no active touch
 * @param touchAge Age of current touch in milliseconds
 * @param state Pre-computed scene state with random values
 */
private fun DrawScope.drawNeonCity(
    t: Float,
    scrollOffset: Float,
    touch: Offset?,
    touchAge: Long,
    state: SceneState
) {
    val w = size.width
    val h = size.height

    drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF4A148C))))

    // Buildings
    val buildingCount = 12
    for (i in 0 until buildingCount) {
        val bw = w / buildingCount * 0.85f
        val bx = i * (w / buildingCount) + scrollOffset * 0.05f
        val bh = h * (0.2f + state.floats[i] * 0.35f)
        val by = h - bh
        drawRect(Color(0xFF1B1B2F), Offset(bx, by), Size(bw, bh))

        // Windows
        val windowRows = (bh / 18f).toInt().coerceAtMost(12)
        val windowCols = (bw / 14f).toInt().coerceAtMost(4)
        for (wr in 0 until windowRows) {
            for (wc in 0 until windowCols) {
                val lit = state.floats[(i * 7 + wr * 3 + wc) % state.floats.size] > 0.4f
                if (lit) {
                    val wx = bx + 4f + wc * (bw - 8f) / windowCols.coerceAtLeast(1)
                    val wy = by + 6f + wr * 16f
                    drawRect(Color(0xFFFFF176).copy(alpha = 0.7f), Offset(wx, wy), Size(6f, 8f))
                }
            }
        }
    }

    // Scrolling sign banner
    val bannerY = h * 0.25f + scrollOffset * 0.08f
    val bannerX = (w - (t * 50f) % (w * 2f)) + w
    drawRect(Color(0xFFFF4081).copy(alpha = 0.8f), Offset(bannerX, bannerY), Size(w * 0.5f, 12f))

    // Rain
    for (i in 0 until 30) {
        val rx = (state.offsets[i % state.offsets.size].x * w + scrollOffset * 0.02f) % w
        val ry = ((state.offsets[i % state.offsets.size].y * h + t * 250f * (0.8f + state.floats[i % state.floats.size] * 0.4f)) % (h + 40f)) - 20f
        drawLine(
            Color.White.copy(alpha = 0.2f),
            Offset(rx, ry), Offset(rx - 1f, ry + 8f),
            strokeWidth = 1f
        )
    }

    // Lightning flash
    val flashCycle = (t * 0.3f) % 5f
    if (flashCycle < 0.06f) {
        drawRect(Color.White.copy(alpha = 0.15f), Offset.Zero, size)
    }

    // Touch – neon glow circle
    if (touch != null && touchAge < 1000L) {
        val alpha = (1f - touchAge / 1000f).coerceIn(0f, 1f)
        val r = 20f + (touchAge / 1000f) * 30f
        drawCircle(Color(0xFF76FF03).copy(alpha = alpha * 0.6f), r, touch)
        drawCircle(Color(0xFF76FF03).copy(alpha = alpha * 0.3f), r * 1.4f, touch)
    }
}
