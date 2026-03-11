package com.sugarmunch.app.effects.special

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 🎆 SPECIAL EFFECTS SYSTEM
 * Creative candy-themed visual effects for the ultimate sugar rush experience!
 */

@Serializable
data class SpecialEffectConfig(
    val effectId: String,
    val isEnabled: Boolean = false,
    val intensity: Float = 1.0f,
    val speed: Float = 1.0f,
    val customSettings: Map<String, Float> = emptyMap()
)

@Serializable
data class ActiveEffectState(
    val effectId: String,
    val triggerType: EffectTriggerType = EffectTriggerType.MANUAL,
    val durationMs: Long = 5000,
    val startTime: Long = System.currentTimeMillis()
)

enum class EffectTriggerType {
    MANUAL,         // User taps FAB
    AUTO_ALWAYS,    // Always on when enabled
    ON_TOUCH,       // Triggers on screen touch
    ON_INSTALL,     // Triggers when installing apps
    ON_CELEBRATION, // Triggers on achievements
    SCHEDULED       // Time-based activation
}

enum class EffectCategory {
    PARTICLES,      // Particle-based effects
    OVERLAY,        // Screen overlay effects
    TRAIL,          // Trail/following effects
    BURST,          // Explosion/burst effects
    AMBIENT,        // Background ambient effects
    REACTIVE        // Reactive to actions
}

/**
 * Base special effect definition
 */
data class SpecialEffect(
    val id: String,
    val name: String,
    val description: String,
    val category: EffectCategory,
    val icon: String, // Emoji or icon reference
    val defaultColors: List<Color>,
    val minIntensity: Float = 0.1f,
    val maxIntensity: Float = 2.0f,
    val defaultIntensity: Float = 1.0f,
    val supportsSpeed: Boolean = true,
    val supportsColors: Boolean = true,
    val isPremium: Boolean = false,
    val triggerTypes: List<EffectTriggerType> = listOf(EffectTriggerType.MANUAL),
    val customSettings: List<CustomSetting> = emptyList()
) {
    data class CustomSetting(
        val key: String,
        val name: String,
        val minValue: Float,
        val maxValue: Float,
        val defaultValue: Float,
        val step: Float = 0.1f
    )
}

/**
 * 🍭 ALL SPECIAL EFFECTS CATALOG
 */
object SpecialEffectsCatalog {
    
    // ═════════════════════════════════════════════════════════════════
    // PARTICLE EFFECTS
    // ═════════════════════════════════════════════════════════════════
    
    val CANDY_CONFETTI = SpecialEffect(
        id = "candy_confetti",
        name = "Candy Confetti",
        description = "Colorful candy pieces rain down your screen",
        category = EffectCategory.PARTICLES,
        icon = "🎊",
        defaultColors = listOf(
            Color(0xFFFF6B6B), // Red
            Color(0xFF4ECDC4), // Teal
            Color(0xFFFFE66D), // Yellow
            Color(0xFF95E1D3), // Mint
            Color(0xFFF38181), // Pink
            Color(0xFFAA96DA)  // Purple
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("density", "Density", 0.5f, 3f, 1f),
            SpecialEffect.CustomSetting("gravity", "Gravity", 0.5f, 2f, 1f)
        )
    )
    
    val FLOATING_CANDIES = SpecialEffect(
        id = "floating_candies",
        name = "Floating Candies",
        description = "Sweet treats gently float around your screen",
        category = EffectCategory.AMBIENT,
        icon = "🍬",
        defaultColors = listOf(
            Color(0xFFFFB6C1), // Light Pink
            Color(0xFF98D8C8), // Mint
            Color(0xFFF7DC6F), // Yellow
            Color(0xFFBB8FCE), // Lavender
            Color(0xFF85C1E9)  // Light Blue
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("count", "Candy Count", 5f, 30f, 15f, 1f),
            SpecialEffect.CustomSetting("float_speed", "Float Speed", 0.2f, 2f, 0.8f)
        )
    )
    
    val GLITTER_SPARKLE = SpecialEffect(
        id = "glitter_sparkle",
        name = "Glitter Sparkle",
        description = "Magical sparkles dance across the screen",
        category = EffectCategory.PARTICLES,
        icon = "✨",
        defaultColors = listOf(
            Color(0xFFFFFFFF), // White
            Color(0xFFFFF59D), // Light Yellow
            Color(0xFFFFCC80), // Orange
            Color(0xFFFFF8E1)  // Cream
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("sparkle_count", "Sparkle Count", 10f, 100f, 50f, 5f),
            SpecialEffect.CustomSetting("twinkle_speed", "Twinkle Speed", 0.5f, 3f, 1.5f)
        )
    )
    
    val HEART_BUBBLES = SpecialEffect(
        id = "heart_bubbles",
        name = "Heart Bubbles",
        description = "Lovely hearts float upward like bubbles",
        category = EffectCategory.PARTICLES,
        icon = "💕",
        defaultColors = listOf(
            Color(0xFFFF69B4), // Hot Pink
            Color(0xFFFF1493), // Deep Pink
            Color(0xFFFFC0CB), // Pink
            Color(0xFFFFB6C1), // Light Pink
            Color(0xFFF8BBD9)  // Pastel Pink
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("heart_size", "Heart Size", 0.5f, 2f, 1f),
            SpecialEffect.CustomSetting("rise_speed", "Rise Speed", 0.3f, 2f, 0.8f)
        )
    )
    
    val STAR_BURST = SpecialEffect(
        id = "star_burst",
        name = "Star Burst",
        description = "Explosive stars burst from screen touches",
        category = EffectCategory.BURST,
        icon = "🌟",
        defaultColors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFFA500), // Orange
            Color(0xFFFF6347), // Tomato
            Color(0xFFFF69B4), // Hot Pink
            Color(0xFF00CED1)  // Turquoise
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("burst_size", "Burst Size", 0.5f, 2f, 1f),
            SpecialEffect.CustomSetting("star_count", "Stars per Burst", 5f, 30f, 15f, 1f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // TRAIL EFFECTS
    // ═════════════════════════════════════════════════════════════════
    
    val RAINBOW_TRAIL = SpecialEffect(
        id = "rainbow_trail",
        name = "Rainbow Trail",
        description = "A colorful rainbow follows your finger",
        category = EffectCategory.TRAIL,
        icon = "🌈",
        defaultColors = listOf(
            Color(0xFFFF0000), // Red
            Color(0xFFFF7F00), // Orange
            Color(0xFFFFFF00), // Yellow
            Color(0xFF00FF00), // Green
            Color(0xFF0000FF), // Blue
            Color(0xFF4B0082), // Indigo
            Color(0xFF9400D3)  // Violet
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("trail_length", "Trail Length", 10f, 50f, 25f, 5f),
            SpecialEffect.CustomSetting("trail_width", "Trail Width", 2f, 20f, 8f, 1f)
        )
    )
    
    val CANDY_CRUMBS = SpecialEffect(
        id = "candy_crumbs",
        name = "Candy Crumbs",
        description = "Leave a trail of sugary crumbs",
        category = EffectCategory.TRAIL,
        icon = "🍪",
        defaultColors = listOf(
            Color(0xFFD2691E), // Chocolate
            Color(0xFFF4A460), // Sandy Brown
            Color(0xFFDEB887), // Burlywood
            Color(0xFFFFE4C4)  // Bisque
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("crumb_size", "Crumb Size", 2f, 10f, 5f, 1f),
            SpecialEffect.CustomSetting("fade_time", "Fade Time", 500f, 3000f, 1500f, 100f)
        )
    )
    
    val NEON_TRAIL = SpecialEffect(
        id = "neon_trail",
        name = "Neon Trail",
        description = "Glowing neon light follows your movements",
        category = EffectCategory.TRAIL,
        icon = "⚡",
        defaultColors = listOf(
            Color(0xFF39FF14), // Neon Green
            Color(0xFF00FFFF), // Cyan
            Color(0xFFFF00FF), // Magenta
            Color(0xFFFFFF00), // Yellow
            Color(0xFFFF1493)  // Deep Pink
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("glow_intensity", "Glow Intensity", 0.5f, 2f, 1f),
            SpecialEffect.CustomSetting("trail_fade", "Fade Speed", 100f, 1000f, 500f, 50f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // OVERLAY EFFECTS
    // ═════════════════════════════════════════════════════════════════
    
    val SUGAR_RUSH = SpecialEffect(
        id = "sugar_rush",
        name = "Sugar Rush",
        description = "Intense gradient overlay with haptic feedback",
        category = EffectCategory.OVERLAY,
        icon = "🚀",
        defaultColors = listOf(
            Color(0xFFFF1493).copy(alpha = 0.3f), // Deep Pink
            Color(0xFF00CED1).copy(alpha = 0.2f), // Turquoise
            Color(0xFFFFD700).copy(alpha = 0.2f)  // Gold
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("overlay_opacity", "Opacity", 0.1f, 0.8f, 0.3f),
            SpecialEffect.CustomSetting("pulse_speed", "Pulse Speed", 0.5f, 3f, 1.5f)
        )
    )
    
    val COTTON_CANDY_CLOUDS = SpecialEffect(
        id = "cotton_candy_clouds",
        name = "Cotton Candy Clouds",
        description = "Soft fluffy clouds drift across your screen",
        category = EffectCategory.AMBIENT,
        icon = "☁️",
        defaultColors = listOf(
            Color(0xFFFFB6C1).copy(alpha = 0.4f), // Pink
            Color(0xFFB5DEFF).copy(alpha = 0.4f), // Blue
            Color(0xFFFFFFE0).copy(alpha = 0.3f)  // Light Yellow
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("cloud_count", "Cloud Count", 2f, 10f, 5f, 1f),
            SpecialEffect.CustomSetting("drift_speed", "Drift Speed", 0.1f, 1f, 0.3f)
        )
    )
    
    val VIGNETTE_GLOW = SpecialEffect(
        id = "vignette_glow",
        name = "Vignette Glow",
        description = "Cozy vignette effect with color glow",
        category = EffectCategory.OVERLAY,
        icon = "🌅",
        defaultColors = listOf(
            Color(0xFFFF6B6B).copy(alpha = 0.3f),
            Color(0xFF4ECDC4).copy(alpha = 0.3f),
            Color(0xFFFFE66D).copy(alpha = 0.3f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("vignette_size", "Vignette Size", 0.2f, 0.8f, 0.5f),
            SpecialEffect.CustomSetting("glow_strength", "Glow Strength", 0.2f, 1f, 0.5f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // BURST EFFECTS
    // ═════════════════════════════════════════════════════════════════
    
    val FIREWORKS = SpecialEffect(
        id = "fireworks",
        name = "Candy Fireworks",
        description = "Explosive candy fireworks on screen",
        category = EffectCategory.BURST,
        icon = "🎆",
        defaultColors = listOf(
            Color(0xFFFF0000),
            Color(0xFF00FF00),
            Color(0xFF0000FF),
            Color(0xFFFFFF00),
            Color(0xFFFF00FF),
            Color(0xFF00FFFF)
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION, EffectTriggerType.ON_INSTALL),
        customSettings = listOf(
            SpecialEffect.CustomSetting("explosion_size", "Explosion Size", 0.5f, 2f, 1f),
            SpecialEffect.CustomSetting("particle_count", "Particles", 20f, 100f, 50f, 10f)
        )
    )
    
    val LOLLIPOP_BURST = SpecialEffect(
        id = "lollipop_burst",
        name = "Lollipop Burst",
        description = "Colorful lollipops burst and spin",
        category = EffectCategory.BURST,
        icon = "🍭",
        defaultColors = listOf(
            Color(0xFFFF69B4),
            Color(0xFF00CED1),
            Color(0xFFFFD700),
            Color(0xFF98FB98),
            Color(0xFFDDA0DD)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("lollipop_count", "Lollipop Count", 3f, 20f, 10f, 1f),
            SpecialEffect.CustomSetting("spin_speed", "Spin Speed", 0.5f, 5f, 2f)
        )
    )
    
    val GUMMY_EXPLOSION = SpecialEffect(
        id = "gummy_explosion",
        name = "Gummy Explosion",
        description = "Bouncy gummy bears explode everywhere",
        category = EffectCategory.BURST,
        icon = "🧸",
        defaultColors = listOf(
            Color(0xFFFF0000), // Red
            Color(0xFF00FF00), // Green
            Color(0xFFFFFF00), // Yellow
            Color(0xFF00FFFF), // Cyan
            Color(0xFFFF00FF)  // Magenta
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("gummy_count", "Gummy Count", 5f, 30f, 15f, 1f),
            SpecialEffect.CustomSetting("bounce_height", "Bounce Height", 0.5f, 2f, 1f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // REACTIVE EFFECTS
    // ═════════════════════════════════════════════════════════════════
    
    val MUSIC_REACTIVE = SpecialEffect(
        id = "music_reactive",
        name = "Music Reactive",
        description = "Visuals that react to your music",
        category = EffectCategory.REACTIVE,
        icon = "🎵",
        defaultColors = listOf(
            Color(0xFF9C27B0),
            Color(0xFF673AB7),
            Color(0xFF3F51B5),
            Color(0xFF2196F3)
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        supportsSpeed = false,
        customSettings = listOf(
            SpecialEffect.CustomSetting("sensitivity", "Sensitivity", 0.5f, 3f, 1f),
            SpecialEffect.CustomSetting("bar_count", "Bar Count", 10f, 50f, 30f, 5f)
        )
    )
    
    val RIPPLE_TOUCH = SpecialEffect(
        id = "ripple_touch",
        name = "Ripple Touch",
        description = "Water-like ripples on every touch",
        category = EffectCategory.REACTIVE,
        icon = "💧",
        defaultColors = listOf(
            Color(0xFF4FC3F7).copy(alpha = 0.6f),
            Color(0xFF29B6F6).copy(alpha = 0.5f),
            Color(0xFF03A9F4).copy(alpha = 0.4f)
        ),
        triggerTypes = listOf(EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("ripple_size", "Ripple Size", 50f, 300f, 150f, 10f),
            SpecialEffect.CustomSetting("ripple_duration", "Duration", 300f, 1500f, 800f, 100f)
        )
    )
    
    val BEAT_PULSE = SpecialEffect(
        id = "beat_pulse",
        name = "Beat Pulse",
        description = "Screen pulses to the rhythm",
        category = EffectCategory.REACTIVE,
        icon = "💗",
        defaultColors = listOf(
            Color(0xFFFF1744).copy(alpha = 0.3f),
            Color(0xFFFF4081).copy(alpha = 0.2f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("pulse_intensity", "Pulse Intensity", 0.2f, 1f, 0.5f),
            SpecialEffect.CustomSetting("beat_speed", "Beat Speed", 0.5f, 3f, 1f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // SEASONAL/TIMED EFFECTS
    // ═════════════════════════════════════════════════════════════════
    
    val SNOW_GLOBE = SpecialEffect(
        id = "snow_globe",
        name = "Snow Globe",
        description = "Magical snowflakes falling gently",
        category = EffectCategory.AMBIENT,
        icon = "❄️",
        defaultColors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFE3F2FD),
            Color(0xFFBBDEFB)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS, EffectTriggerType.SCHEDULED),
        customSettings = listOf(
            SpecialEffect.CustomSetting("snow_density", "Snow Density", 10f, 100f, 50f, 5f),
            SpecialEffect.CustomSetting("fall_speed", "Fall Speed", 0.5f, 3f, 1f)
        )
    )
    
    val CHERRY_BLOSSOMS = SpecialEffect(
        id = "cherry_blossoms",
        name = "Cherry Blossoms",
        description = "Delicate sakura petals drift by",
        category = EffectCategory.AMBIENT,
        icon = "🌸",
        defaultColors = listOf(
            Color(0xFFFFC0CB),
            Color(0xFFFFB6C1),
            Color(0xFFFF69B4),
            Color(0xFFFFF0F5)
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("petal_count", "Petal Count", 5f, 40f, 20f, 5f),
            SpecialEffect.CustomSetting("sway_amount", "Sway Amount", 0.2f, 2f, 0.8f)
        )
    )
    
    val GOLDEN_HOUR = SpecialEffect(
        id = "golden_hour",
        name = "Golden Hour",
        description = "Warm sunset glow overlay",
        category = EffectCategory.OVERLAY,
        icon = "🌇",
        defaultColors = listOf(
            Color(0xFFFF6B35).copy(alpha = 0.3f),
            Color(0xFFF7931E).copy(alpha = 0.2f),
            Color(0xFFFFD23F).copy(alpha = 0.2f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS, EffectTriggerType.SCHEDULED),
        customSettings = listOf(
            SpecialEffect.CustomSetting("warmth", "Warmth", 0.3f, 1f, 0.6f),
            SpecialEffect.CustomSetting("intensity", "Intensity", 0.2f, 0.8f, 0.4f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // ALL EFFECTS LIST
    // ═════════════════════════════════════════════════════════════════
    
    val ALL_EFFECTS: List<SpecialEffect> by lazy {
        listOf(
            // Original Particles
            CANDY_CONFETTI,
            FLOATING_CANDIES,
            GLITTER_SPARKLE,
            HEART_BUBBLES,
            STAR_BURST,

            // Original Trails
            RAINBOW_TRAIL,
            CANDY_CRUMBS,
            NEON_TRAIL,

            // Original Overlays
            SUGAR_RUSH,
            COTTON_CANDY_CLOUDS,
            VIGNETTE_GLOW,

            // Original Bursts
            FIREWORKS,
            LOLLIPOP_BURST,
            GUMMY_EXPLOSION,

            // Original Reactive
            MUSIC_REACTIVE,
            RIPPLE_TOUCH,
            BEAT_PULSE,

            // Original Ambient/Seasonal
            SNOW_GLOBE,
            CHERRY_BLOSSOMS,
            GOLDEN_HOUR
        ) + MoreSpecialEffects.ALL_NEW_EFFECTS
    }

    val FREE_EFFECTS: List<SpecialEffect> get() = ALL_EFFECTS.filter { !it.isPremium }
    val PREMIUM_EFFECTS: List<SpecialEffect> get() = ALL_EFFECTS.filter { it.isPremium }

    fun getById(id: String): SpecialEffect? = ALL_EFFECTS.find { it.id == id }
    fun getByCategory(category: EffectCategory) = ALL_EFFECTS.filter { it.category == category }
    fun getRandom() = ALL_EFFECTS.random()
    
    fun getDefaultConfig(effectId: String): SpecialEffectConfig {
        val effect = getById(effectId) ?: return SpecialEffectConfig(effectId)
        return SpecialEffectConfig(
            effectId = effectId,
            intensity = effect.defaultIntensity,
            customSettings = effect.customSettings.associate { it.key to it.defaultValue }
        )
    }
}

// Serialization helpers
fun SpecialEffectConfig.toJson(): String = Json.encodeToString(this)
fun String.toSpecialEffectConfig(): SpecialEffectConfig? = 
    try {
        Json.decodeFromString(this)
    } catch (e: Exception) {
        null
    }
