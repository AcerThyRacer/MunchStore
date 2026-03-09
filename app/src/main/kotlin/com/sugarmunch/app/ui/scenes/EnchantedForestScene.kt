package com.sugarmunch.app.ui.scenes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the Enchanted Forest scene with trees, falling leaves, fireflies, and ground.
 *
 * This scene features a magical forest theme with tree silhouettes,
 * gently falling leaves, glowing fireflies, and a wavy ground line.
 * Touch interaction spawns a cluster of fireflies.
 *
 * @param t Time in seconds for animation
 * @param scrollOffset Scroll-based parallax offset
 * @param touch Current touch position or null if no active touch
 * @param touchAge Age of current touch in milliseconds
 * @param state Pre-computed scene state with random values
 */
private fun DrawScope.drawEnchantedForest(
    t: Float,
    scrollOffset: Float,
    touch: Offset?,
    touchAge: Long,
    state: SceneState
) {
    val w = size.width
    val h = size.height

    drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF0D3B0F))))

    // Ground – wavy line at bottom
    val groundPath = Path().apply {
        moveTo(0f, h)
        var x = 0f
        val groundY = h * 0.88f
        while (x <= w) {
            val gy = groundY + sin((x * 0.015f).toDouble()).toFloat() * 8f + scrollOffset * 0.05f
            lineTo(x, gy)
            x += 4f
        }
        lineTo(w, h)
        close()
    }
    drawPath(groundPath, Color(0xFF2E7D32))

    // Tree silhouettes
    for (i in 0 until 8) {
        val tx = state.offsets[i].x * w + scrollOffset * (0.03f + i * 0.01f)
        val groundBase = h * 0.88f + sin((tx * 0.015f).toDouble()).toFloat() * 8f
        val trunkH = h * (0.08f + state.floats[i] * 0.12f)
        val trunkW = 10f + state.floats[i] * 6f
        val trunkTop = groundBase - trunkH
        // Trunk
        drawRect(Color(0xFF4E342E), Offset(tx - trunkW / 2f, trunkTop), Size(trunkW, trunkH))
        // Canopy – triangle
        val canopyH = trunkH * 1.2f
        val canopyW = trunkW * 4f
        val treePath = Path().apply {
            moveTo(tx, trunkTop - canopyH)
            lineTo(tx - canopyW / 2f, trunkTop)
            lineTo(tx + canopyW / 2f, trunkTop)
            close()
        }
        drawPath(treePath, Color(0xFF388E3C).copy(alpha = 0.85f))
    }

    // Falling leaves
    for (i in 0 until 15) {
        val leafPhase = state.floats[i + 20] * 10f
        val fallSpeed = 25f + state.floats[i + 20] * 15f
        val lx = (state.offsets[i + 20].x * w + sin((t * 0.8f + leafPhase).toDouble()).toFloat() * 20f) % w
        val ly = ((t * fallSpeed + state.offsets[i + 20].y * h) % (h * 1.1f)) - h * 0.05f
        val rotation = t * 60f + leafPhase * 40f
        val alpha = if (ly > h * 0.9f) (1f - (ly - h * 0.9f) / (h * 0.1f)).coerceIn(0f, 1f) else 0.7f

        rotate(rotation, Offset(lx, ly + scrollOffset * 0.08f)) {
            drawOval(
                Color(0xFFFF8F00).copy(alpha = alpha),
                Offset(lx - 4f, ly - 2f + scrollOffset * 0.08f),
                Size(8f, 4f)
            )
        }
    }

    // Fireflies
    for (i in 0 until 12) {
        val fx = state.offsets[i + 50 % state.offsets.size].x * w +
            sin((t * 0.4f + state.floats[i + 50 % state.floats.size] * 8f).toDouble()).toFloat() * 30f
        val fy = state.offsets[i + 50 % state.offsets.size].y * h * 0.7f + h * 0.15f +
            cos((t * 0.3f + state.floats[i + 50 % state.floats.size] * 5f).toDouble()).toFloat() * 20f +
            scrollOffset * 0.04f
        val glow = (sin((t * 3f + state.floats[i + 50 % state.floats.size] * 6f).toDouble()).toFloat() + 1f) / 2f
        drawCircle(Color(0xFFFFEB3B).copy(alpha = glow * 0.7f), 5f + glow * 3f, Offset(fx, fy))
        drawCircle(Color(0xFFFFEB3B).copy(alpha = glow * 0.2f), 10f + glow * 6f, Offset(fx, fy))
    }

    // Touch – spawn firefly cluster
    if (touch != null && touchAge < 1500L) {
        val prog = touchAge / 1500f
        for (fi in 0 until 6) {
            val angle = (fi * 60f + prog * 120f) * (PI.toFloat() / 180f)
            val dist = prog * 40f + 5f
            val fx = touch.x + cos(angle) * dist
            val fy = touch.y + sin(angle) * dist
            val alpha = (1f - prog).coerceIn(0f, 1f) * 0.8f
            val glowSize = 4f + sin((t * 4f + fi).toDouble()).toFloat() * 2f
            drawCircle(Color(0xFFFFEB3B).copy(alpha = alpha), glowSize, Offset(fx, fy))
        }
    }
}
