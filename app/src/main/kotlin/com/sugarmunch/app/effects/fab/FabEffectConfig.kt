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
            listOf(0xFFFF6B35, 0xFFF7931E, 0xFFFFD23F), "Seasonal")
    )
    
    fun getById(id: String): FabEffectDisplayInfo? = ALL_DISPLAY_INFO.find { it.effectId == id }
    fun getByCategory(category: String) = ALL_DISPLAY_INFO.filter { it.category == category }
}
