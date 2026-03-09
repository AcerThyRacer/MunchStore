package com.sugarmunch.app.theme.builder

import kotlinx.serialization.Serializable

/**
 * Gradient type options for theme backgrounds.
 *
 * Defines the available gradient rendering styles that can be applied
 * to theme backgrounds. Each type produces a different visual effect.
 */
@Serializable
enum class GradientType(val displayName: String) {
    /** Linear gradient - colors blend in a straight line */
    LINEAR("Linear"),

    /** Radial gradient - colors blend outward from a center point */
    RADIAL("Radial"),

    /** Sweep gradient - colors blend in a circular sweep pattern */
    SWEEP("Sweep")
}
