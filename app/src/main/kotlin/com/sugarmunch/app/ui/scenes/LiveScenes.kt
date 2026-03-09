package com.sugarmunch.app.ui.scenes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

/**
 * Available scene types for the live background animation system.
 *
 * Each scene type provides a unique visual theme with animated elements
 * and touch interaction capabilities.
 */
enum class SceneType {
    /** Chocolate factory with conveyor belts, gears, and falling candy */
    CANDY_FACTORY,
    /** Underwater ocean with waves, bubbles, and floating candy */
    SUGAR_OCEAN,
    /** Cyberpunk cityscape with neon buildings, rain, and lightning */
    NEON_CITY,
    /** Deep space with stars, planets, and nebula clouds */
    SPACE_CANDY,
    /** Magical forest with trees, leaves, and fireflies */
    ENCHANTED_FOREST
}

/**
 * Main composable for rendering animated live scenes.
 *
 * This composable sets up the animation loop and delegates drawing to
 * scene-specific functions based on the selected [SceneType]. It handles
 * frame timing, touch interaction, and scroll-based parallax effects.
 *
 * @param scene The type of scene to render
 * @param scrollOffset Scroll-based offset for parallax effects
 * @param touchPoint Current touch position or null if no active touch
 * @param speed Animation speed multiplier (clamped to 0.1f..5f)
 * @param modifier Compose modifier for the canvas
 */
@Composable
fun LiveScene(
    scene: SceneType,
    scrollOffset: Float = 0f,
    touchPoint: Offset? = null,
    speed: Float = 1f,
    modifier: Modifier = Modifier
) {
    var frameTime by remember { mutableLongStateOf(0L) }
    var elapsed by remember { mutableLongStateOf(0L) }
    var lastTouch by remember { mutableStateOf<Offset?>(null) }
    var touchAge by remember { mutableLongStateOf(0L) }

    LaunchedEffect(touchPoint) {
        if (touchPoint != null) {
            lastTouch = touchPoint
            touchAge = 0L
        }
    }

    LaunchedEffect(Unit) {
        var prev = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - prev) / 1_000_000L).coerceIn(0, 32)
            prev = now
            elapsed += (dt * speed.coerceIn(0.1f, 5f)).toLong()
            frameTime = elapsed
            if (lastTouch != null) {
                touchAge += dt
                if (touchAge > 2000L) lastTouch = null
            }
        }
    }

    val rng = remember { Random(scene.ordinal.toLong() * 7919L) }
    val sceneState = remember(scene) { buildSceneState(scene, rng) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = frameTime / 1000f // seconds
        when (scene) {
            SceneType.CANDY_FACTORY -> drawCandyFactory(t, scrollOffset, lastTouch, touchAge, sceneState)
            SceneType.SUGAR_OCEAN -> drawSugarOcean(t, scrollOffset, lastTouch, touchAge, sceneState)
            SceneType.NEON_CITY -> drawNeonCity(t, scrollOffset, lastTouch, touchAge, sceneState)
            SceneType.SPACE_CANDY -> drawSpaceCandy(t, scrollOffset, lastTouch, touchAge, sceneState)
            SceneType.ENCHANTED_FOREST -> drawEnchantedForest(t, scrollOffset, lastTouch, touchAge, sceneState)
        }
    }
}
