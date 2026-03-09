package com.sugarmunch.app.ui.scenes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the Candy Factory scene with conveyor belts, gears, and falling candy.
 *
 * This scene features a chocolate-themed factory with three conveyor belts moving
 * candy pieces, rotating gears at belt ends, and candy falling from a chute at the top.
 * Touch interaction spawns additional falling candy.
 *
 * @param t Time in seconds for animation
 * @param scrollOffset Scroll-based parallax offset
 * @param touch Current touch position or null if no active touch
 * @param touchAge Age of current touch in milliseconds
 * @param state Pre-computed scene state with random values
 */
private fun DrawScope.drawCandyFactory(
    t: Float,
    scrollOffset: Float,
    touch: Offset?,
    touchAge: Long,
    state: SceneState
) {
    val w = size.width
    val h = size.height

    // Background gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF5D4037), Color(0xFF3E2723))
        )
    )

    // Conveyor belts
    val beltYs = floatArrayOf(h * 0.35f, h * 0.55f, h * 0.75f)
    val beltHeight = h * 0.03f
    val beltColor = Color(0xFF8D6E63)
    val candyColors = listOf(Color(0xFFE91E63), Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFF2196F3))

    for ((bi, beltY) in beltYs.withIndex()) {
        val parallax = scrollOffset * (0.2f + bi * 0.1f)
        val adjustedY = beltY + parallax

        // Belt
        drawRect(
            color = beltColor,
            topLeft = Offset(0f, adjustedY - beltHeight / 2f),
            size = Size(w, beltHeight)
        )

        // Belt segments
        val segSpacing = w / 12f
        for (s in 0..12) {
            val sx = ((s * segSpacing + t * 80f * (bi + 1)) % (w + segSpacing)) - segSpacing / 2f
            drawLine(
                Color(0xFF6D4C41), Offset(sx, adjustedY - beltHeight / 2f),
                Offset(sx, adjustedY + beltHeight / 2f), strokeWidth = 2f
            )
        }

        // Candy on belt
        for (ci in 0..5) {
            val cx = ((state.floats[bi * 6 + ci] * w + t * 60f * (bi + 1)) % (w + 40f)) - 20f
            val radius = 8f + state.floats[bi * 6 + ci + 18] * 6f
            drawCircle(candyColors[ci % candyColors.size], radius, Offset(cx, adjustedY - beltHeight / 2f - radius))
        }

        // Gears at belt ends
        for (gx in floatArrayOf(20f, w - 20f)) {
            val gearRadius = 14f
            val angle = t * 90f * (if (gx < w / 2f) 1f else -1f)
            rotate(angle, Offset(gx, adjustedY)) {
                drawCircle(Color(0xFF757575), gearRadius, Offset(gx, adjustedY))
                for (tooth in 0 until 6) {
                    val a = (tooth * 60f) * (PI.toFloat() / 180f)
                    drawLine(
                        Color(0xFF616161),
                        Offset(gx + cos(a) * gearRadius * 0.6f, adjustedY + sin(a) * gearRadius * 0.6f),
                        Offset(gx + cos(a) * gearRadius * 1.3f, adjustedY + sin(a) * gearRadius * 1.3f),
                        strokeWidth = 4f
                    )
                }
            }
        }
    }

    // Falling candy from top (chute)
    val chuteX = w * 0.5f
    drawRect(Color(0xFF4E342E), Offset(chuteX - 15f, 0f), Size(30f, h * 0.15f))
    for (fi in 0..3) {
        val fallT = (t * 1.2f + state.floats[fi + 40] * 3f) % 3f
        val fy = fallT / 3f * (beltYs[0] - 20f)
        drawCircle(candyColors[fi], 7f, Offset(chuteX + (fi - 1.5f) * 8f, fy))
    }

    // Touch interaction – spawn candy
    if (touch != null && touchAge < 1500L) {
        val fallProgress = touchAge / 1500f
        val fy = touch.y + fallProgress * (h - touch.y)
        val alpha = (1f - fallProgress).coerceIn(0f, 1f)
        drawCircle(Color(0xFFFF5722).copy(alpha = alpha), 10f, Offset(touch.x, fy))
    }
}
