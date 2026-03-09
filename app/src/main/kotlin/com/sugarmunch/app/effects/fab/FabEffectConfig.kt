package com.sugarmunch.app.effects.fab

import kotlinx.serialization.Serializable

/**
 * 🍭 FAB Special Effects Configuration
 * Allows users to customize which effects appear in the Floating Action Button
 */

@Serializable
data class FabEffectConfig(
    val effectId: String,
    val displayName: String,
    val emoji: String,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
    val triggerMode: FabTriggerMode = FabTriggerMode.TAP,
    val longPressAction: FabLongPressAction = FabLongPressAction.NONE
)

@Serializable
data class FabConfiguration(
    val selectedEffectIds: List<String> = emptyList(),
    val primaryEffectId: String? = null,
    val layoutMode: FabLayoutMode = FabLayoutMode.GRID,
    val showLabels: Boolean = true,
    val animationStyle: FabAnimationStyle = FabAnimationStyle.BOUNCE,
    val triggerOnUnlock: Boolean = false,
    val celebrateOnInstall: Boolean = true,
    val autoRotateEffects: Boolean = false,
    val rotationIntervalSeconds: Int = 30,
    val customColorScheme: FabColorScheme? = null
)

enum class FabTriggerMode {
    TAP,           // Single tap to activate
    DOUBLE_TAP,    // Double tap
    LONG_PRESS,    // Hold to activate
    SWIPE_UP,      // Swipe gesture
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT
}

enum class FabLongPressAction {
    NONE,          // Do nothing
    SHOW_MENU,     // Show full effects menu
    TOGGLE_ALL,    // Toggle all selected effects
    RANDOM_EFFECT  // Trigger random effect
}

enum class FabLayoutMode {
    GRID,          // Grid layout for multiple effects
    CAROUSEL,      // Swipeable carousel
    EXPANDING,     // Expandable FAB with sub-buttons
    COMPACT        // Minimal - just primary effect
}

enum class FabAnimationStyle {
    BOUNCE,        // Bouncy spring animation
    WOBBLE,        // Candy-like wobble
    SPIN,          // Rotation effect
    PULSE,         // Pulsing scale
    FLOAT,         // Gentle floating
    JELLY,         // Jelly wobble effect
    GLITTER        // Sparkle entrance
}

@Serializable
data class FabColorScheme(
    val primaryColor: Long = 0xFFFF69B4,  // Hot Pink
    val secondaryColor: Long = 0xFF00CED1, // Turquoise
    val accentColor: Long = 0xFFFFD700,    // Gold
    val glowColor: Long = 0xFFFFB6C1,      // Light Pink
    val useDynamicColors: Boolean = true
)

/**
 * Predefined FAB configurations for different moods
 */
object FabConfigurationPresets {
    
    val SUGAR_RUSH = FabConfiguration(
        selectedEffectIds = listOf("sugar_rush", "candy_confetti", "star_burst"),
        primaryEffectId = "sugar_rush",
        layoutMode = FabLayoutMode.EXPANDING,
        showLabels = true,
        animationStyle = FabAnimationStyle.JELLY,
        celebrateOnInstall = true,
        customColorScheme = FabColorScheme(
            primaryColor = 0xFFFF1493,
            secondaryColor = 0xFFFF69B4,
            accentColor = 0xFFFFD700,
            glowColor = 0xFFFFB6C1
        )
    )
    
    val CHILL_MODE = FabConfiguration(
        selectedEffectIds = listOf("cotton_candy_clouds", "floating_candies", "vignette_glow"),
        primaryEffectId = "cotton_candy_clouds",
        layoutMode = FabLayoutMode.COMPACT,
        showLabels = false,
        animationStyle = FabAnimationStyle.FLOAT,
        celebrateOnInstall = false,
        customColorScheme = FabColorScheme(
            primaryColor = 0xFFB5DEFF,
            secondaryColor = 0xFFFFB6C1,
            accentColor = 0xFFFFF0F5,
            glowColor = 0xFFE6E6FA
        )
    )
    
    val PARTY_MODE = FabConfiguration(
        selectedEffectIds = listOf("candy_confetti", "fireworks", "lollipop_burst", "gummy_explosion"),
        primaryEffectId = "candy_confetti",
        layoutMode = FabLayoutMode.GRID,
        showLabels = true,
        animationStyle = FabAnimationStyle.GLITTER,
        autoRotateEffects = true,
        rotationIntervalSeconds = 15,
        celebrateOnInstall = true,
        customColorScheme = FabColorScheme(
            primaryColor = 0xFFFF00FF,
            secondaryColor = 0xFF00FFFF,
            accentColor = 0xFFFFFF00,
            glowColor = 0xFFFF69B4
        )
    )
    
    val ROMANTIC = FabConfiguration(
        selectedEffectIds = listOf("heart_bubbles", "cherry_blossoms", "glitter_sparkle"),
        primaryEffectId = "heart_bubbles",
        layoutMode = FabLayoutMode.CAROUSEL,
        showLabels = true,
        animationStyle = FabAnimationStyle.PULSE,
        customColorScheme = FabColorScheme(
            primaryColor = 0xFFFF69B4,
            secondaryColor = 0xFFFFC0CB,
            accentColor = 0xFFFFF0F5,
            glowColor = 0xFFFFB6C1
        )
    )
    
    val GAMING = FabConfiguration(
        selectedEffectIds = listOf("neon_trail", "beat_pulse", "rainbow_trail"),
        primaryEffectId = "neon_trail",
        layoutMode = FabLayoutMode.EXPANDING,
        showLabels = false,
        animationStyle = FabAnimationStyle.PULSE,
        customColorScheme = FabColorScheme(
            primaryColor = 0xFF39FF14,
            secondaryColor = 0xFF00FFFF,
            accentColor = 0xFFFF00FF,
            glowColor = 0xFF00FF00
        )
    )
    
    val MINIMAL = FabConfiguration(
        selectedEffectIds = listOf("ripple_touch"),
        primaryEffectId = "ripple_touch",
        layoutMode = FabLayoutMode.COMPACT,
        showLabels = false,
        animationStyle = FabAnimationStyle.BOUNCE,
        celebrateOnInstall = false
    )
    
    val ALL_PRESETS = listOf(
        "sugar_rush" to SUGAR_RUSH,
        "chill_mode" to CHILL_MODE,
        "party_mode" to PARTY_MODE,
        "romantic" to ROMANTIC,
        "gaming" to GAMING,
        "minimal" to MINIMAL
    )
}

/**
 * Effect category display info for FAB
 */
data class FabEffectDisplayInfo(
    val effectId: String,
    val displayName: String,
    val emoji: String,
    val gradientColors: List<Long>,
    val category: String,
    val isPremium: Boolean = false
)

object FabEffectDisplayCatalog {
    val ALL_DISPLAY_INFO = listOf(
        // Particle Effects
        FabEffectDisplayInfo("candy_confetti", "Confetti", "🎊", 
            listOf(0xFFFF6B6B, 0xFF4ECDC4, 0xFFFFE66D), "Particles"),
        FabEffectDisplayInfo("floating_candies", "Floating", "🍬", 
            listOf(0xFFFFB6C1, 0xFF98D8C8, 0xFFF7DC6F), "Ambient"),
        FabEffectDisplayInfo("glitter_sparkle", "Sparkles", "✨", 
            listOf(0xFFFFFFFF, 0xFFFFF59D, 0xFFFFCC80), "Particles"),
        FabEffectDisplayInfo("heart_bubbles", "Hearts", "💕", 
            listOf(0xFFFF69B4, 0xFFFF1493, 0xFFFFC0CB), "Particles"),
        FabEffectDisplayInfo("star_burst", "Star Burst", "🌟", 
            listOf(0xFFFFD700, 0xFFFFA500, 0xFFFF6347), "Burst"),
        
        // Trail Effects
        FabEffectDisplayInfo("rainbow_trail", "Rainbow", "🌈", 
            listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF), "Trails"),
        FabEffectDisplayInfo("candy_crumbs", "Crumbs", "🍪", 
            listOf(0xFFD2691E, 0xFFF4A460, 0xFFDEB887), "Trails"),
        FabEffectDisplayInfo("neon_trail", "Neon", "⚡", 
            listOf(0xFF39FF14, 0xFF00FFFF, 0xFFFF00FF), "Trails", true),
        
        // Overlay Effects
        FabEffectDisplayInfo("sugar_rush", "Sugar Rush", "🚀", 
            listOf(0xFFFF1493, 0xFF00CED1, 0xFFFFD700), "Overlay"),
        FabEffectDisplayInfo("cotton_candy_clouds", "Clouds", "☁️", 
            listOf(0xFFFFB6C1, 0xFFB5DEFF, 0xFFFFFFE0), "Ambient"),
        FabEffectDisplayInfo("vignette_glow", "Glow", "🌅", 
            listOf(0xFFFF6B6B, 0xFF4ECDC4, 0xFFFFE66D), "Overlay"),
        
        // Burst Effects
        FabEffectDisplayInfo("fireworks", "Fireworks", "🎆", 
            listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF), "Burst", true),
        FabEffectDisplayInfo("lollipop_burst", "Lollipops", "🍭", 
            listOf(0xFFFF69B4, 0xFF00CED1, 0xFFFFD700), "Burst"),
        FabEffectDisplayInfo("gummy_explosion", "Gummies", "🧸", 
            listOf(0xFFFF0000, 0xFF00FF00, 0xFFFFFF00), "Burst"),
        
        // Reactive Effects
        FabEffectDisplayInfo("music_reactive", "Music", "🎵", 
            listOf(0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5), "Reactive", true),
        FabEffectDisplayInfo("ripple_touch", "Ripples", "💧", 
            listOf(0xFF4FC3F7, 0xFF29B6F6, 0xFF03A9F4), "Reactive"),
        FabEffectDisplayInfo("beat_pulse", "Pulse", "💗", 
            listOf(0xFFFF1744, 0xFFFF4081), "Reactive"),
        
        // Seasonal Effects
        FabEffectDisplayInfo("snow_globe", "Snow", "❄️", 
            listOf(0xFFFFFFFF, 0xFFE3F2FD, 0xFFBBDEFB), "Seasonal"),
        FabEffectDisplayInfo("cherry_blossoms", "Sakura", "🌸", 
            listOf(0xFFFFC0CB, 0xFFFFB6C1, 0xFFFF69B4), "Seasonal", true),
        FabEffectDisplayInfo("golden_hour", "Sunset", "🌇", 
            listOf(0xFFFF6B35, 0xFFF7931E, 0xFFFFD23F), "Seasonal"),

        // ── NEW PARTICLE EFFECTS ───────────────────────────────────────
        FabEffectDisplayInfo("pixel_rain", "Pixel Rain", "🎮",
            listOf(0xFF00FF41, 0xFF00CC34, 0xFF009928), "Particles"),
        FabEffectDisplayInfo("bubble_pop", "Bubbles", "🫧",
            listOf(0xFFB5DEFF, 0xFFE8F4FD, 0xFFFFFFFF), "Particles"),
        FabEffectDisplayInfo("lightning_strike", "Lightning", "⚡",
            listOf(0xFFFFFF00, 0xFFFFFFFF, 0xFF87CEEB), "Particles", true),
        FabEffectDisplayInfo("fire_sparks", "Sparks", "🔥",
            listOf(0xFFFF4500, 0xFFFF6347, 0xFFFFD700), "Particles"),
        FabEffectDisplayInfo("aurora_particles", "Aurora", "🌌",
            listOf(0xFF00FF88, 0xFF00CCFF, 0xFFAA00FF), "Particles", true),
        FabEffectDisplayInfo("crystal_shards", "Crystals", "💎",
            listOf(0xFF00FFFF, 0xFFE0FFFF, 0xFFFFFFFF), "Particles"),
        FabEffectDisplayInfo("flower_petals", "Petals", "🌺",
            listOf(0xFFFF69B4, 0xFFFFAE42, 0xFF9370DB), "Particles"),
        FabEffectDisplayInfo("emoji_rain", "Emoji Rain", "😂",
            listOf(0xFFFFD700, 0xFFFFA07A, 0xFFFF69B4), "Particles"),
        FabEffectDisplayInfo("dna_helix", "DNA Helix", "🧬",
            listOf(0xFF00FF7F, 0xFF1E90FF, 0xFFFF69B4), "Particles", true),
        FabEffectDisplayInfo("comet_shower", "Comets", "☄️",
            listOf(0xFFFFD700, 0xFFFF8C00, 0xFFFFFFFF), "Particles"),
        FabEffectDisplayInfo("neon_dots", "Neon Dots", "🔴",
            listOf(0xFFFF00FF, 0xFF00FFFF, 0xFFFFFF00), "Particles"),

        // ── NEW TRAIL EFFECTS ──────────────────────────────────────────
        FabEffectDisplayInfo("ink_trail", "Ink Trail", "🖊️",
            listOf(0xFF1A1A2E, 0xFF16213E, 0xFF533483), "Trails"),
        FabEffectDisplayInfo("fire_trail", "Fire Trail", "🔥",
            listOf(0xFFFF4500, 0xFFFF6347, 0xFFFFD700), "Trails", true),
        FabEffectDisplayInfo("galaxy_trail", "Galaxy Trail", "🌌",
            listOf(0xFF6A0DAD, 0xFF1B2A6B, 0xFFE8C5F5), "Trails", true),
        FabEffectDisplayInfo("water_ripple_trail", "Water Trail", "💧",
            listOf(0xFF4FC3F7, 0xFF81D4FA, 0xFFB3E5FC), "Trails"),
        FabEffectDisplayInfo("butterfly_trail", "Butterflies", "🦋",
            listOf(0xFFFF69B4, 0xFF9370DB, 0xFF00CED1), "Trails"),
        FabEffectDisplayInfo("magic_wand_trail", "Magic Wand", "🪄",
            listOf(0xFFFFD700, 0xFFFFFFFF, 0xFF9B59B6), "Trails"),
        FabEffectDisplayInfo("laser_trail", "Laser", "🔴",
            listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF), "Trails", true),

        // ── NEW OVERLAY EFFECTS ────────────────────────────────────────
        FabEffectDisplayInfo("kaleidoscope", "Kaleidoscope", "🔮",
            listOf(0xFFFF69B4, 0xFF00CED1, 0xFFFFD700), "Overlay", true),
        FabEffectDisplayInfo("neon_grid", "Neon Grid", "🕸️",
            listOf(0xFF00FFFF, 0xFF39FF14, 0xFFFF00FF), "Overlay", true),
        FabEffectDisplayInfo("starfield", "Starfield", "🌠",
            listOf(0xFFFFFFFF, 0xFFFFFFE0, 0xFFE0E0FF), "Overlay"),
        FabEffectDisplayInfo("matrix_rain", "Matrix", "💻",
            listOf(0xFF00FF41, 0xFF009B18, 0xFF00CC2F), "Overlay", true),
        FabEffectDisplayInfo("vintage_film", "Vintage Film", "🎞️",
            listOf(0xFFD2B48C, 0xFFD8B870, 0xFF808080), "Overlay"),
        FabEffectDisplayInfo("prism_light", "Prism", "🌈",
            listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF), "Overlay"),
        FabEffectDisplayInfo("holographic_shimmer", "Hologram", "🔵",
            listOf(0xFF00FFFF, 0xFFFF00FF, 0xFF00FF00), "Overlay", true),
        FabEffectDisplayInfo("glitch_art", "Glitch Art", "📺",
            listOf(0xFF00FFFF, 0xFFFF00FF, 0xFFFF0000), "Overlay", true),
        FabEffectDisplayInfo("chromatic_aberration", "Chromatic", "🎨",
            listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF), "Overlay"),
        FabEffectDisplayInfo("vhs_effect", "VHS", "📼",
            listOf(0xFFFFFFFF, 0xFFFF0000, 0xFF000000), "Overlay"),
        FabEffectDisplayInfo("pixel_sort", "Pixel Sort", "🌈",
            listOf(0xFFFF6B6B, 0xFF4ECDC4, 0xFFFFE66D), "Overlay", true),
        FabEffectDisplayInfo("neon_outline", "Neon Outline", "💡",
            listOf(0xFF39FF14, 0xFFFF00FF, 0xFF00FFFF), "Overlay"),
        FabEffectDisplayInfo("candy_spiral", "Candy Spiral", "🍭",
            listOf(0xFFFF69B4, 0xFFFFFFFF, 0xFF00CED1), "Overlay"),
        FabEffectDisplayInfo("depth_of_field", "Bokeh", "📷",
            listOf(0xFFFFFFFF, 0xFFFFF9C4, 0xFFB3E5FC), "Overlay"),

        // ── NEW BURST EFFECTS ──────────────────────────────────────────
        FabEffectDisplayInfo("diamond_burst", "Diamonds", "💎",
            listOf(0xFF00FFFF, 0xFFFFFFFF, 0xFFB0E0E6), "Burst"),
        FabEffectDisplayInfo("supernova", "Supernova", "💥",
            listOf(0xFFFFFFFF, 0xFFFFFF00, 0xFFFF4500), "Burst", true),
        FabEffectDisplayInfo("color_bomb", "Color Bomb", "🎨",
            listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF), "Burst"),
        FabEffectDisplayInfo("shockwave", "Shockwave", "🌀",
            listOf(0xFF00CED1, 0xFF48D1CC, 0xFF7FFFD4), "Burst"),
        FabEffectDisplayInfo("glitch_burst", "Glitch Burst", "📺",
            listOf(0xFF00FFFF, 0xFFFF00FF, 0xFFFF0000), "Burst", true),
        FabEffectDisplayInfo("rainbow_bloom", "Rainbow Bloom", "🌸",
            listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF), "Burst"),
        FabEffectDisplayInfo("soap_bubble_burst", "Bubble Burst", "🫧",
            listOf(0xFFB5DEFF, 0xFFE8F4FD, 0xFFFFB6C1), "Burst"),
        FabEffectDisplayInfo("confetti_cannon", "Cannon", "🎉",
            listOf(0xFFFF6B6B, 0xFF4ECDC4, 0xFFFFE66D), "Burst"),
        FabEffectDisplayInfo("meteor_strike", "Meteor", "🌑",
            listOf(0xFFFF4500, 0xFFFF8C00, 0xFFFFD700), "Burst"),

        // ── NEW AMBIENT EFFECTS ────────────────────────────────────────
        FabEffectDisplayInfo("lava_lamp", "Lava Lamp", "🫙",
            listOf(0xFFFF4500, 0xFFFF69B4, 0xFFFFD700), "Ambient"),
        FabEffectDisplayInfo("plasma_wave", "Plasma Wave", "🌊",
            listOf(0xFF9400D3, 0xFF4169E1, 0xFF00CED1), "Ambient", true),
        FabEffectDisplayInfo("fireflies", "Fireflies", "✨",
            listOf(0xFFFFFF44, 0xFF88FF00, 0xFFCCFF00), "Ambient"),
        FabEffectDisplayInfo("ocean_waves", "Ocean Waves", "🌊",
            listOf(0xFF006994, 0xFF0099CC, 0xFF90E0EF), "Ambient"),
        FabEffectDisplayInfo("galaxy_drift", "Galaxy Drift", "🌌",
            listOf(0xFF2E004F, 0xFF1A0030, 0xFFF5D78E), "Ambient", true),
        FabEffectDisplayInfo("bioluminescence", "Bioluminescence", "🐟",
            listOf(0xFF00FFBF, 0xFF00BFFF, 0xFF9370DB), "Ambient"),
        FabEffectDisplayInfo("city_lights", "City Lights", "🌃",
            listOf(0xFFFFD700, 0xFFFF6347, 0xFF87CEEB), "Ambient"),
        FabEffectDisplayInfo("northern_lights_ambient", "N. Lights", "🌠",
            listOf(0xFF00FF88, 0xFF00CCFF, 0xFFAA00FF), "Ambient", true),
        FabEffectDisplayInfo("liquid_metal", "Liquid Metal", "🪨",
            listOf(0xFFD3D3D3, 0xFFC0C0C0, 0xFFFFFFFF), "Ambient", true),
        FabEffectDisplayInfo("glowing_runes", "Runes", "🔮",
            listOf(0xFF9B59B6, 0xFF3498DB, 0xFF1ABC9C), "Ambient"),

        // ── NEW REACTIVE EFFECTS ───────────────────────────────────────
        FabEffectDisplayInfo("gyroscope_wave", "Gyro Wave", "🌀",
            listOf(0xFF4FC3F7, 0xFF81D4FA, 0xFFE1F5FE), "Reactive"),
        FabEffectDisplayInfo("shake_confetti", "Shake!", "🎉",
            listOf(0xFFFF6B6B, 0xFF4ECDC4, 0xFFFFE66D), "Reactive"),
        FabEffectDisplayInfo("multi_touch_fireworks", "Multi-Touch", "🎆",
            listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF), "Reactive"),
        FabEffectDisplayInfo("volume_visualizer", "Volume Viz", "🔊",
            listOf(0xFF1DB954, 0xFF0099FF, 0xFFFF3366), "Reactive", true),
        FabEffectDisplayInfo("scroll_parallax_effect", "Parallax", "📜",
            listOf(0xFF87CEEB, 0xFF98FB98, 0xFFDDA0DD), "Reactive")
    )

    fun getById(id: String): FabEffectDisplayInfo? = ALL_DISPLAY_INFO.find { it.effectId == id }
    fun getByCategory(category: String) = ALL_DISPLAY_INFO.filter { it.category == category }
}
