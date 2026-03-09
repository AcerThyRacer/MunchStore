package com.sugarmunch.app.ui.scenes

import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

/**
 * Shared data holder for pre-computed random positions used across scene types.
 *
 * This class stores random float values and offset positions that are generated
 * once per scene instance and reused throughout the animation lifecycle for
 * consistent visual elements.
 *
 * @property floats General-purpose random float values in the range 0..1
 * @property offsets Random normalized positions as Offset objects
 */
private class SceneState(
    val floats: FloatArray,
    val offsets: List<Offset>
)

/**
 * Builds a [SceneState] with pre-computed random values for a specific scene type.
 *
 * Generates 80 random float values and 80 random offset positions using the
 * provided random number generator. These values are used by scene drawing
 * functions to create consistent, reproducible visual patterns.
 *
 * @param scene The scene type being initialized (used for potential future customization)
 * @param rng The random number generator seeded for reproducibility
 * @return A new [SceneState] instance with pre-computed random values
 */
private fun buildSceneState(scene: SceneType, rng: Random): SceneState {
    val count = 80
    val floats = FloatArray(count) { rng.nextFloat() }
    val offsets = List(count) { Offset(rng.nextFloat(), rng.nextFloat()) }
    return SceneState(floats, offsets)
}
