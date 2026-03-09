package com.sugarmunch.app.effects.v2.presets

import com.sugarmunch.app.effects.v2.model.EffectCategory
import com.sugarmunch.app.effects.v2.model.EffectType
import com.sugarmunch.app.effects.v2.model.EffectV2

/**
 * 10 NEW Premium Effects for Phase 4
 */
object NewEffectPresets {

    // ═════════════════════════════════════════════════════════════
    // PREMIUM VISUAL EFFECTS (4 new)
    // ═════════════════════════════════════════════════════════════

    val DRAGON_BREATH = EffectV2(
        id = "dragon_breath",
        name = "Dragon Breath",
        description = "Fiery particles that follow your touch",
        category = EffectCategory.PARTICLES,
        type = EffectType.INTERACTIVE,
        intensity = 1.0f,
        settings = listOf(
            EffectV2.EffectSetting.Slider("particle_size", "Particle Size", 0.5f, 0.1f, 2.0f),
            EffectV2.EffectSetting.Slider("fire_intensity", "Fire Intensity", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.ColorPicker("fire_color", "Fire Color", 0xFFFF4500)
        ),
        icon = "🐉",
        isPremium = true
    )

    val STARDUST_TRAIL = EffectV2(
        id = "stardust_trail",
        name = "Stardust Trail",
        description = "Leave a trail of glittering stars",
        category = EffectCategory.PARTICLES,
        type = EffectType.INTERACTIVE,
        intensity = 1.0f,
        settings = listOf(
            EffectV2.EffectSetting.Slider("trail_length", "Trail Length", 1.0f, 0.1f, 3.0f),
            EffectV2.EffectSetting.Slider("sparkle_rate", "Sparkle Rate", 1.0f, 0.1f, 2.0f)
        ),
        icon = "✨",
        isPremium = true
    )

    val MATRIX_CODE = EffectV2(
        id = "matrix_code",
        name = "Matrix Code",
        description = "Digital rain effect like the Matrix",
        category = EffectCategory.VISUAL_OVERLAY,
        type = EffectType.ANIMATED,
        intensity = 1.0f,
        settings = listOf(
            EffectV2.EffectSetting.Slider("rain_speed", "Rain Speed", 1.0f, 0.1f, 3.0f),
            EffectV2.EffectSetting.Slider("code_density", "Code Density", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.ColorPicker("code_color", "Code Color", 0xFF00FF00)
        ),
        icon = "💻",
        isPremium = true
    )

    val AURORA_BOREALIS = EffectV2(
        id = "aurora_borealis",
        name = "Aurora Borealis",
        description = "Northern lights dancing across screen",
        category = EffectCategory.VISUAL_OVERLAY,
        type = EffectType.ANIMATED,
        intensity = 1.0f,
        settings = listOf(
            EffectV2.EffectSetting.Slider("wave_speed", "Wave Speed", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.Slider("wave_height", "Wave Height", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.Selection("color_scheme", "Color Scheme", listOf("Green", "Purple", "Rainbow"))
        ),
        icon = "✨",
        isPremium = true
    )

    // ═════════════════════════════════════════════════════════════
    // PREMIUM HAPTIC EFFECTS (3 new)
    // ═════════════════════════════════════════════════════════════

    val THUNDER_RUMBLE = EffectV2(
        id = "thunder_rumble",
        name = "Thunder Rumble",
        description = "Deep rumbling haptic feedback",
        category = EffectCategory.HAPTIC,
        type = EffectType.TRIGGERED,
        intensity = 1.0f,
        settings = listOf(
            EffectV2.EffectSetting.Slider("rumble_depth", "Rumble Depth", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.Slider("duration", "Duration", 1.0f, 0.1f, 3.0f)
        ),
        icon = "⚡",
        isPremium = true
    )

    val BUTTERFLY_WINGS = EffectV2(
        id = "butterfly_wings",
        name = "Butterfly Wings",
        description = "Light, fluttering haptic sensation",
        category = EffectCategory.HAPTIC,
        type = EffectType.CONTINUOUS,
        intensity = 0.5f,
        settings = listOf(
            EffectV2.EffectSetting.Slider("flutter_speed", "Flutter Speed", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.Slider("lightness", "Lightness", 1.0f, 0.1f, 2.0f)
        ),
        icon = "🦋",
        isPremium = true
    )

    val MASSAGE_MODE = EffectV2(
        id = "massage_mode",
        name = "Massage Mode",
        description = "Soothing rhythmic vibration pattern",
        category = EffectCategory.HAPTIC,
        type = EffectType.CONTINUOUS,
        intensity = 0.7f,
        settings = listOf(
            EffectV2.EffectSetting.Slider("intensity", "Intensity", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.Slider("rhythm", "Rhythm", 1.0f, 0.1f, 2.0f)
        ),
        icon = "💆",
        isPremium = true
    )

    // ═════════════════════════════════════════════════════════════
    // PREMIUM ANIMATION EFFECTS (3 new)
    // ═════════════════════════════════════════════════════════════

    val GALAXY_SPIRAL = EffectV2(
        id = "galaxy_spiral",
        name = "Galaxy Spiral",
        description = "Spinning galaxy arms with stars",
        category = EffectCategory.ANIMATIONS,
        type = EffectType.ANIMATED,
        intensity = 1.0f,
        settings = listOf(
            EffectV2.EffectSetting.Slider("rotation_speed", "Rotation Speed", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.Slider("arm_count", "Spiral Arms", 2.0f, 1.0f, 6.0f),
            EffectV2.EffectSetting.ColorPicker("core_color", "Core Color", 0xFFFFD700)
        ),
        icon = "🌀",
        isPremium = true
    )

    val JELLYFISH_FLOAT = EffectV2(
        id = "jellyfish_float",
        name = "Jellyfish Float",
        description = "Bioluminescent jellyfish floating",
        category = EffectCategory.ANIMATIONS,
        type = EffectType.ANIMATED,
        intensity = 0.8f,
        settings = listOf(
            EffectV2.EffectSetting.Slider("float_speed", "Float Speed", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.Slider("glow_intensity", "Glow Intensity", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.ColorPicker("jellyfish_color", "Jellyfish Color", 0xFF00FFFF)
        ),
        icon = "🪼",
        isPremium = true
    )

    val TIME_WARP = EffectV2(
        id = "time_warp",
        name = "Time Warp",
        description = "Reality-bending space distortion",
        category = EffectCategory.ANIMATIONS,
        type = EffectType.ANIMATED,
        intensity = 1.2f,
        settings = listOf(
            EffectV2.EffectSetting.Slider("warp_strength", "Warp Strength", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.Slider("distortion", "Distortion", 1.0f, 0.1f, 2.0f),
            EffectV2.EffectSetting.Selection("warp_type", "Warp Type", listOf("Spiral", "Tunnel", "Ripple"))
        ),
        icon = "🕳️",
        isPremium = true
    )

    /**
     * Get all new premium effects
     */
    fun getAllNewEffects(): List<EffectV2> {
        return listOf(
            DRAGON_BREATH,
            STARDUST_TRAIL,
            MATRIX_CODE,
            AURORA_BOREALIS,
            THUNDER_RUMBLE,
            BUTTERFLY_WINGS,
            MASSAGE_MODE,
            GALAXY_SPIRAL,
            JELLYFISH_FLOAT,
            TIME_WARP
        )
    }

    /**
     * Get effects by category
     */
    fun getEffectsByCategory(category: EffectCategory): List<EffectV2> {
        return getAllNewEffects().filter { it.category == category }
    }

    /**
     * Get premium effects only
     */
    fun getPremiumEffects(): List<EffectV2> {
        return getAllNewEffects().filter { it.isPremium }
    }
}
