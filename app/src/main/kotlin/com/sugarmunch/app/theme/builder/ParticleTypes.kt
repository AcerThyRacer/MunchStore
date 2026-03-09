package com.sugarmunch.app.theme.builder

import kotlinx.serialization.Serializable

/**
 * Particle type options for theme background effects.
 *
 * Defines the available particle shapes that can be rendered in the
 * theme background when particle effects are enabled.
 */
@Serializable
enum class ParticleType(val displayName: String) {
    /** Circular particles */
    CIRCLES("Circles"),

    /** Square particles */
    SQUARES("Squares"),

    /** Star-shaped particles */
    STARS("Stars"),

    /** Heart-shaped particles */
    HEARTS("Hearts"),

    /** Diamond-shaped particles */
    DIAMONDS("Diamonds"),
    
    // NEW: Candy-specific particle types
    CANDY_CANE("Candy Cane"),
    GUMMY_BEAR("Gummy Bear"),
    SPRINKLES("Sprinkles"),
    LOLLIPOP("Lollipop"),
    SUGAR_CRYSTAL("Sugar Crystal")
}
