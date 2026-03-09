package com.sugarmunch.app.effects.special

import androidx.compose.ui.graphics.Color

/**
 * 🎨 MORE SPECIAL EFFECTS — 56 Additional Animations
 * Creative candy-themed and beyond visual effects expanding the catalog.
 */
object MoreSpecialEffects {

    // ═════════════════════════════════════════════════════════════════
    // PARTICLE EFFECTS (NEW)
    // ═════════════════════════════════════════════════════════════════

    val PIXEL_RAIN = SpecialEffect(
        id = "pixel_rain",
        name = "Pixel Rain",
        description = "Retro pixel blocks cascade down the screen",
        category = EffectCategory.PARTICLES,
        icon = "🎮",
        defaultColors = listOf(
            Color(0xFF00FF41), // Matrix green
            Color(0xFF00CC34),
            Color(0xFF009928),
            Color(0xFF00FF88)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("pixel_size", "Pixel Size", 4f, 24f, 10f, 2f),
            SpecialEffect.CustomSetting("density", "Density", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("fall_speed", "Fall Speed", 0.5f, 4f, 2f)
        )
    )

    val BUBBLE_POP = SpecialEffect(
        id = "bubble_pop",
        name = "Bubble Pop",
        description = "Iridescent soap bubbles float up and pop with satisfying splashes",
        category = EffectCategory.PARTICLES,
        icon = "🫧",
        defaultColors = listOf(
            Color(0xFFB5DEFF).copy(alpha = 0.7f),
            Color(0xFFE8F4FD).copy(alpha = 0.6f),
            Color(0xFFFFFFFF).copy(alpha = 0.5f),
            Color(0xFFCCEEFF).copy(alpha = 0.7f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("bubble_count", "Bubble Count", 5f, 40f, 15f, 1f),
            SpecialEffect.CustomSetting("bubble_size", "Bubble Size", 20f, 120f, 50f, 5f),
            SpecialEffect.CustomSetting("pop_radius", "Pop Splash Radius", 30f, 150f, 60f, 5f)
        )
    )

    val LIGHTNING_STRIKE = SpecialEffect(
        id = "lightning_strike",
        name = "Lightning Strike",
        description = "Electric lightning bolts crackle across the screen",
        category = EffectCategory.PARTICLES,
        icon = "⚡",
        defaultColors = listOf(
            Color(0xFFFFFF00), // Yellow
            Color(0xFFFFFFFF), // White
            Color(0xFF87CEEB), // Sky blue
            Color(0xFFE0E0FF)  // Lavender white
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("bolt_count", "Bolt Count", 1f, 10f, 3f, 1f),
            SpecialEffect.CustomSetting("branch_depth", "Branch Depth", 1f, 5f, 3f, 1f),
            SpecialEffect.CustomSetting("flicker_speed", "Flicker Speed", 0.5f, 3f, 1.5f)
        )
    )

    val FIRE_SPARKS = SpecialEffect(
        id = "fire_sparks",
        name = "Fire Sparks",
        description = "Hot embers and sparks rise like a campfire",
        category = EffectCategory.PARTICLES,
        icon = "🔥",
        defaultColors = listOf(
            Color(0xFFFF4500), // OrangeRed
            Color(0xFFFF6347), // Tomato
            Color(0xFFFFD700), // Gold
            Color(0xFFFFA500), // Orange
            Color(0xFFFF8C00)  // DarkOrange
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("spark_count", "Spark Count", 10f, 80f, 40f, 5f),
            SpecialEffect.CustomSetting("heat_intensity", "Heat Intensity", 0.5f, 2f, 1f),
            SpecialEffect.CustomSetting("rise_speed", "Rise Speed", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("spread_width", "Spread Width", 50f, 400f, 200f, 10f)
        )
    )

    val AURORA_PARTICLES = SpecialEffect(
        id = "aurora_particles",
        name = "Aurora Borealis",
        description = "Shimmering Northern Lights dance across the screen",
        category = EffectCategory.PARTICLES,
        icon = "🌌",
        defaultColors = listOf(
            Color(0xFF00FF88), // Aurora green
            Color(0xFF00CCFF), // Aurora blue
            Color(0xFFAA00FF), // Aurora purple
            Color(0xFF00FFCC), // Teal
            Color(0xFF0066FF)  // Blue
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("wave_count", "Wave Count", 2f, 8f, 4f, 1f),
            SpecialEffect.CustomSetting("shimmer_speed", "Shimmer Speed", 0.3f, 2f, 0.8f),
            SpecialEffect.CustomSetting("wave_height", "Wave Height", 50f, 300f, 150f, 10f)
        )
    )

    val CRYSTAL_SHARDS = SpecialEffect(
        id = "crystal_shards",
        name = "Crystal Shards",
        description = "Glittering crystal fragments scatter and reflect light",
        category = EffectCategory.PARTICLES,
        icon = "💎",
        defaultColors = listOf(
            Color(0xFF00FFFF), // Cyan
            Color(0xFFE0FFFF), // LightCyan
            Color(0xFFAFEEEE), // PaleTurquoise
            Color(0xFFB0E0E6), // PowderBlue
            Color(0xFFFFFFFF)  // White
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("shard_count", "Shard Count", 10f, 60f, 25f, 5f),
            SpecialEffect.CustomSetting("shard_size", "Shard Size", 5f, 30f, 12f, 1f),
            SpecialEffect.CustomSetting("reflection_speed", "Reflection Speed", 0.5f, 3f, 1.5f)
        )
    )

    val FLOWER_PETALS = SpecialEffect(
        id = "flower_petals",
        name = "Flower Petals",
        description = "Colorful flower petals swirl and drift gently",
        category = EffectCategory.PARTICLES,
        icon = "🌺",
        defaultColors = listOf(
            Color(0xFFFF69B4), // HotPink
            Color(0xFFFF85C2), // Light pink
            Color(0xFFFFAE42), // Orange-yellow
            Color(0xFFFFF44F), // Lemon yellow
            Color(0xFF9370DB)  // MediumPurple
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("petal_count", "Petal Count", 5f, 50f, 20f, 5f),
            SpecialEffect.CustomSetting("swirl_strength", "Swirl Strength", 0.1f, 2f, 0.7f),
            SpecialEffect.CustomSetting("petal_size", "Petal Size", 8f, 40f, 18f, 2f)
        )
    )

    val EMOJI_RAIN = SpecialEffect(
        id = "emoji_rain",
        name = "Emoji Rain",
        description = "A downpour of random emojis rains from the sky",
        category = EffectCategory.PARTICLES,
        icon = "😂",
        defaultColors = listOf(
            Color(0xFFFFD700), // Yellow (emoji skin)
            Color(0xFFFFA07A), // LightSalmon
            Color(0xFFFF69B4), // HotPink
            Color(0xFF00CED1)  // DarkTurquoise
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("emoji_count", "Emoji Count", 5f, 50f, 20f, 5f),
            SpecialEffect.CustomSetting("fall_speed", "Fall Speed", 0.5f, 4f, 1.5f),
            SpecialEffect.CustomSetting("emoji_size", "Emoji Size", 16f, 64f, 28f, 4f),
            SpecialEffect.CustomSetting("spin_speed", "Spin Speed", 0f, 3f, 1f)
        )
    )

    val DNA_HELIX = SpecialEffect(
        id = "dna_helix",
        name = "DNA Helix",
        description = "Rotating DNA double helix streams up the screen",
        category = EffectCategory.PARTICLES,
        icon = "🧬",
        defaultColors = listOf(
            Color(0xFF00FF7F), // SpringGreen
            Color(0xFF1E90FF), // DodgerBlue
            Color(0xFFFF69B4), // HotPink
            Color(0xFFFFFF00)  // Yellow
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("helix_count", "Helix Streams", 1f, 5f, 2f, 1f),
            SpecialEffect.CustomSetting("rotation_speed", "Rotation Speed", 0.5f, 4f, 1.5f),
            SpecialEffect.CustomSetting("node_size", "Node Size", 4f, 20f, 8f, 1f)
        )
    )

    val COMET_SHOWER = SpecialEffect(
        id = "comet_shower",
        name = "Comet Shower",
        description = "Blazing comets streak across the screen with glowing tails",
        category = EffectCategory.PARTICLES,
        icon = "☄️",
        defaultColors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFF8C00), // DarkOrange
            Color(0xFFFFFFFF), // White
            Color(0xFFFFA500)  // Orange
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("comet_count", "Comet Count", 2f, 20f, 8f, 1f),
            SpecialEffect.CustomSetting("tail_length", "Tail Length", 50f, 300f, 120f, 10f),
            SpecialEffect.CustomSetting("comet_speed", "Comet Speed", 1f, 5f, 2.5f)
        )
    )

    val NEON_DOTS = SpecialEffect(
        id = "neon_dots",
        name = "Neon Dots",
        description = "Bouncy neon dots scatter around with electric energy",
        category = EffectCategory.PARTICLES,
        icon = "🔴",
        defaultColors = listOf(
            Color(0xFFFF00FF), // Magenta
            Color(0xFF00FFFF), // Cyan
            Color(0xFFFFFF00), // Yellow
            Color(0xFF00FF00), // Lime
            Color(0xFFFF6600)  // Orange
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("dot_count", "Dot Count", 10f, 80f, 30f, 5f),
            SpecialEffect.CustomSetting("bounce_energy", "Bounce Energy", 0.5f, 2f, 1f),
            SpecialEffect.CustomSetting("dot_size", "Dot Size", 4f, 20f, 8f, 1f),
            SpecialEffect.CustomSetting("glow_radius", "Glow Radius", 2f, 20f, 8f, 1f)
        )
    )

    // ═════════════════════════════════════════════════════════════════
    // TRAIL EFFECTS (NEW)
    // ═════════════════════════════════════════════════════════════════

    val INK_TRAIL = SpecialEffect(
        id = "ink_trail",
        name = "Ink Trail",
        description = "Leave an artistic ink brush trail as you swipe",
        category = EffectCategory.TRAIL,
        icon = "🖊️",
        defaultColors = listOf(
            Color(0xFF1A1A2E), // Dark blue-black
            Color(0xFF16213E), // Navy
            Color(0xFF0F3460), // Dark blue
            Color(0xFF533483)  // Purple
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("brush_width", "Brush Width", 2f, 20f, 8f, 1f),
            SpecialEffect.CustomSetting("ink_spread", "Ink Spread", 0f, 2f, 0.5f),
            SpecialEffect.CustomSetting("fade_time", "Fade Time", 200f, 3000f, 1000f, 100f),
            SpecialEffect.CustomSetting("drip_chance", "Ink Drip Chance", 0f, 1f, 0.2f)
        )
    )

    val FIRE_TRAIL = SpecialEffect(
        id = "fire_trail",
        name = "Fire Trail",
        description = "A blazing fire trail scorches behind your touch",
        category = EffectCategory.TRAIL,
        icon = "🔥",
        defaultColors = listOf(
            Color(0xFFFF4500), // OrangeRed
            Color(0xFFFF6347), // Tomato
            Color(0xFFFFD700), // Gold
            Color(0xFFFF8C00)  // DarkOrange
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("flame_height", "Flame Height", 10f, 80f, 30f, 5f),
            SpecialEffect.CustomSetting("fire_width", "Fire Width", 5f, 30f, 12f, 1f),
            SpecialEffect.CustomSetting("ember_density", "Ember Density", 0.2f, 2f, 0.8f)
        )
    )

    val GALAXY_TRAIL = SpecialEffect(
        id = "galaxy_trail",
        name = "Galaxy Trail",
        description = "Cosmic stardust and nebula wisps follow your finger",
        category = EffectCategory.TRAIL,
        icon = "🌌",
        defaultColors = listOf(
            Color(0xFF6A0DAD), // Purple
            Color(0xFF1B2A6B), // Deep blue
            Color(0xFFE8C5F5), // Lavender
            Color(0xFFF5D78E), // Star yellow
            Color(0xFF2B6CB0)  // Blue
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("star_density", "Star Density", 5f, 40f, 15f, 1f),
            SpecialEffect.CustomSetting("nebula_opacity", "Nebula Opacity", 0.1f, 0.8f, 0.4f),
            SpecialEffect.CustomSetting("trail_width", "Trail Width", 10f, 60f, 25f, 5f)
        )
    )

    val WATER_RIPPLE_TRAIL = SpecialEffect(
        id = "water_ripple_trail",
        name = "Water Ripples",
        description = "Every movement creates expanding water ripples",
        category = EffectCategory.TRAIL,
        icon = "💧",
        defaultColors = listOf(
            Color(0xFF4FC3F7).copy(alpha = 0.6f),
            Color(0xFF81D4FA).copy(alpha = 0.5f),
            Color(0xFFB3E5FC).copy(alpha = 0.4f),
            Color(0xFFE1F5FE).copy(alpha = 0.3f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("ripple_speed", "Ripple Speed", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("ripple_size", "Max Ripple Size", 50f, 250f, 120f, 10f),
            SpecialEffect.CustomSetting("ripple_count", "Rings per Touch", 1f, 6f, 3f, 1f),
            SpecialEffect.CustomSetting("surface_tension", "Surface Tension", 0.2f, 1f, 0.6f)
        )
    )

    val BUTTERFLY_TRAIL = SpecialEffect(
        id = "butterfly_trail",
        name = "Butterfly Trail",
        description = "Delicate butterflies flutter and follow in your wake",
        category = EffectCategory.TRAIL,
        icon = "🦋",
        defaultColors = listOf(
            Color(0xFFFF69B4), // HotPink
            Color(0xFF9370DB), // MediumPurple
            Color(0xFF00CED1), // DarkTurquoise
            Color(0xFFFFD700), // Gold
            Color(0xFF32CD32)  // LimeGreen
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("butterfly_count", "Butterfly Count", 3f, 20f, 8f, 1f),
            SpecialEffect.CustomSetting("flutter_speed", "Flutter Speed", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("follow_delay", "Follow Delay", 100f, 800f, 300f, 50f)
        )
    )

    val MAGIC_WAND_TRAIL = SpecialEffect(
        id = "magic_wand_trail",
        name = "Magic Wand",
        description = "Enchanted sparkles and stars conjure behind your touch",
        category = EffectCategory.TRAIL,
        icon = "🪄",
        defaultColors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFFFFFF), // White
            Color(0xFFFFB6C1), // LightPink
            Color(0xFF9B59B6), // Purple
            Color(0xFF3498DB)  // Blue
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("sparkle_density", "Sparkle Density", 5f, 40f, 15f, 1f),
            SpecialEffect.CustomSetting("star_size", "Star Size", 4f, 24f, 10f, 1f),
            SpecialEffect.CustomSetting("magic_fade", "Magic Fade", 300f, 2000f, 800f, 100f),
            SpecialEffect.CustomSetting("swirl_intensity", "Swirl Intensity", 0f, 2f, 0.8f)
        )
    )

    val LASER_TRAIL = SpecialEffect(
        id = "laser_trail",
        name = "Laser Beam",
        description = "Sci-fi laser beams cut through the air with electric edges",
        category = EffectCategory.TRAIL,
        icon = "🔴",
        defaultColors = listOf(
            Color(0xFFFF0000), // Red
            Color(0xFF00FF00), // Green
            Color(0xFF0000FF), // Blue
            Color(0xFFFF00FF)  // Magenta
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("beam_width", "Beam Width", 1f, 10f, 3f, 0.5f),
            SpecialEffect.CustomSetting("glow_intensity", "Glow Intensity", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("scatter_chance", "Scatter Chance", 0f, 1f, 0.3f)
        )
    )

    // ═════════════════════════════════════════════════════════════════
    // OVERLAY EFFECTS (NEW)
    // ═════════════════════════════════════════════════════════════════

    val KALEIDOSCOPE = SpecialEffect(
        id = "kaleidoscope",
        name = "Kaleidoscope",
        description = "Mesmerizing kaleidoscope patterns rotate and shift",
        category = EffectCategory.OVERLAY,
        icon = "🔮",
        defaultColors = listOf(
            Color(0xFFFF69B4).copy(alpha = 0.3f),
            Color(0xFF00CED1).copy(alpha = 0.3f),
            Color(0xFFFFD700).copy(alpha = 0.3f),
            Color(0xFF9B59B6).copy(alpha = 0.25f)
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("segments", "Segments", 4f, 16f, 8f, 2f),
            SpecialEffect.CustomSetting("rotation_speed", "Rotation Speed", 0.1f, 2f, 0.5f),
            SpecialEffect.CustomSetting("zoom_pulse", "Zoom Pulse", 0f, 1f, 0.3f),
            SpecialEffect.CustomSetting("opacity", "Opacity", 0.1f, 0.6f, 0.3f)
        )
    )

    val NEON_GRID = SpecialEffect(
        id = "neon_grid",
        name = "Neon Grid",
        description = "Tron-style glowing grid lines overlay the screen",
        category = EffectCategory.OVERLAY,
        icon = "🕸️",
        defaultColors = listOf(
            Color(0xFF00FFFF).copy(alpha = 0.4f), // Cyan
            Color(0xFF39FF14).copy(alpha = 0.3f), // Neon Green
            Color(0xFFFF00FF).copy(alpha = 0.3f)  // Magenta
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("grid_spacing", "Grid Spacing", 30f, 150f, 60f, 10f),
            SpecialEffect.CustomSetting("line_width", "Line Width", 0.5f, 3f, 1f),
            SpecialEffect.CustomSetting("pulse_speed", "Pulse Speed", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("perspective_depth", "3D Depth", 0f, 1f, 0.5f)
        )
    )

    val STARFIELD = SpecialEffect(
        id = "starfield",
        name = "Starfield",
        description = "Deep space star field flies past at warp speed",
        category = EffectCategory.OVERLAY,
        icon = "🌠",
        defaultColors = listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.9f),
            Color(0xFFFFFFE0).copy(alpha = 0.7f),
            Color(0xFFE0E0FF).copy(alpha = 0.8f),
            Color(0xFFFFCCCC).copy(alpha = 0.6f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("star_count", "Star Count", 50f, 500f, 200f, 25f),
            SpecialEffect.CustomSetting("warp_speed", "Warp Speed", 0.1f, 5f, 1f),
            SpecialEffect.CustomSetting("star_size_range", "Star Size Range", 0.5f, 4f, 2f),
            SpecialEffect.CustomSetting("parallax_layers", "Parallax Layers", 1f, 5f, 3f, 1f)
        )
    )

    val MATRIX_RAIN = SpecialEffect(
        id = "matrix_rain",
        name = "Matrix Rain",
        description = "The iconic digital rain from the Matrix movie",
        category = EffectCategory.OVERLAY,
        icon = "💻",
        defaultColors = listOf(
            Color(0xFF00FF41), // Matrix green
            Color(0xFF009B18),
            Color(0xFF00CC2F),
            Color(0xFF00FF41).copy(alpha = 0.5f)
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("column_density", "Column Density", 0.2f, 1f, 0.5f),
            SpecialEffect.CustomSetting("drop_speed", "Drop Speed", 0.5f, 4f, 2f),
            SpecialEffect.CustomSetting("char_size", "Character Size", 10f, 30f, 16f, 2f),
            SpecialEffect.CustomSetting("opacity", "Opacity", 0.2f, 0.8f, 0.5f)
        )
    )

    val VINTAGE_FILM = SpecialEffect(
        id = "vintage_film",
        name = "Vintage Film",
        description = "Retro film grain, scratches, and vignette overlay",
        category = EffectCategory.OVERLAY,
        icon = "🎞️",
        defaultColors = listOf(
            Color(0xFFD2B48C).copy(alpha = 0.3f), // Tan
            Color(0xFFD8B870).copy(alpha = 0.2f), // Sandy
            Color(0xFF808080).copy(alpha = 0.2f)  // Gray
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("grain_intensity", "Film Grain", 0.1f, 1f, 0.4f),
            SpecialEffect.CustomSetting("scratch_density", "Scratches", 0f, 1f, 0.2f),
            SpecialEffect.CustomSetting("vignette_size", "Vignette", 0.1f, 0.8f, 0.4f),
            SpecialEffect.CustomSetting("flicker_rate", "Flicker", 0f, 0.5f, 0.1f)
        )
    )

    val PRISM_LIGHT = SpecialEffect(
        id = "prism_light",
        name = "Prism Light",
        description = "Rainbow prism light bands refract across the display",
        category = EffectCategory.OVERLAY,
        icon = "🌈",
        defaultColors = listOf(
            Color(0x33FF0000), // Transparent red
            Color(0x33FF7F00), // Transparent orange
            Color(0x33FFFF00), // Transparent yellow
            Color(0x3300FF00), // Transparent green
            Color(0x330000FF), // Transparent blue
            Color(0x339400D3)  // Transparent violet
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("band_count", "Light Bands", 2f, 10f, 5f, 1f),
            SpecialEffect.CustomSetting("sweep_speed", "Sweep Speed", 0.1f, 2f, 0.5f),
            SpecialEffect.CustomSetting("band_width", "Band Width", 20f, 200f, 80f, 10f),
            SpecialEffect.CustomSetting("opacity", "Opacity", 0.1f, 0.5f, 0.25f)
        )
    )

    val HOLOGRAPHIC_SHIMMER = SpecialEffect(
        id = "holographic_shimmer",
        name = "Holographic Shimmer",
        description = "Futuristic holographic shimmer waves across everything",
        category = EffectCategory.OVERLAY,
        icon = "🔵",
        defaultColors = listOf(
            Color(0x2200FFFF), // Transparent cyan
            Color(0x22FF00FF), // Transparent magenta
            Color(0x2200FF00), // Transparent green
            Color(0x22FFFFFF)  // Transparent white
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("shimmer_speed", "Shimmer Speed", 0.2f, 2f, 0.8f),
            SpecialEffect.CustomSetting("tilt_intensity", "Tilt Intensity", 0f, 1f, 0.5f),
            SpecialEffect.CustomSetting("opacity", "Opacity", 0.05f, 0.4f, 0.15f),
            SpecialEffect.CustomSetting("wave_count", "Wave Count", 2f, 8f, 4f, 1f)
        )
    )

    // ═════════════════════════════════════════════════════════════════
    // BURST EFFECTS (NEW)
    // ═════════════════════════════════════════════════════════════════

    val DIAMOND_BURST = SpecialEffect(
        id = "diamond_burst",
        name = "Diamond Burst",
        description = "Jewel-like diamonds explode outward in a dazzling burst",
        category = EffectCategory.BURST,
        icon = "💎",
        defaultColors = listOf(
            Color(0xFF00FFFF), // Cyan
            Color(0xFFFFFFFF), // White
            Color(0xFFB0E0E6), // PowderBlue
            Color(0xFFE0FFFF), // LightCyan
            Color(0xFF87CEEB)  // SkyBlue
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("diamond_count", "Diamond Count", 5f, 30f, 12f, 1f),
            SpecialEffect.CustomSetting("burst_radius", "Burst Radius", 100f, 400f, 200f, 20f),
            SpecialEffect.CustomSetting("spin_speed", "Spin Speed", 1f, 10f, 4f, 0.5f)
        )
    )

    val SUPERNOVA = SpecialEffect(
        id = "supernova",
        name = "Supernova",
        description = "A massive stellar explosion of light and color",
        category = EffectCategory.BURST,
        icon = "💥",
        defaultColors = listOf(
            Color(0xFFFFFFFF), // White core
            Color(0xFFFFFF00), // Yellow
            Color(0xFFFF8C00), // DarkOrange
            Color(0xFFFF4500), // OrangeRed
            Color(0xFFFF0000)  // Red
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("blast_radius", "Blast Radius", 200f, 600f, 350f, 20f),
            SpecialEffect.CustomSetting("debris_count", "Debris Count", 20f, 100f, 50f, 5f),
            SpecialEffect.CustomSetting("shockwave_count", "Shockwaves", 1f, 5f, 3f, 1f),
            SpecialEffect.CustomSetting("duration", "Duration", 1000f, 5000f, 2500f, 250f)
        )
    )

    val COLOR_BOMB = SpecialEffect(
        id = "color_bomb",
        name = "Color Bomb",
        description = "A paint bomb explodes, splashing vibrant colors everywhere",
        category = EffectCategory.BURST,
        icon = "🎨",
        defaultColors = listOf(
            Color(0xFFFF0000), // Red
            Color(0xFFFF7F00), // Orange
            Color(0xFFFFFF00), // Yellow
            Color(0xFF00FF00), // Green
            Color(0xFF0000FF), // Blue
            Color(0xFF8B00FF)  // Violet
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("splash_count", "Splash Count", 10f, 50f, 25f, 5f),
            SpecialEffect.CustomSetting("drip_length", "Drip Length", 20f, 200f, 80f, 10f),
            SpecialEffect.CustomSetting("splat_size", "Splat Size", 20f, 100f, 50f, 5f)
        )
    )

    val SHOCKWAVE = SpecialEffect(
        id = "shockwave",
        name = "Shockwave",
        description = "Concentric shockwave rings propagate from the touch point",
        category = EffectCategory.BURST,
        icon = "🌀",
        defaultColors = listOf(
            Color(0xFF00CED1).copy(alpha = 0.5f),
            Color(0xFF48D1CC).copy(alpha = 0.4f),
            Color(0xFF40E0D0).copy(alpha = 0.3f),
            Color(0xFF7FFFD4).copy(alpha = 0.2f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("wave_count", "Wave Count", 1f, 8f, 4f, 1f),
            SpecialEffect.CustomSetting("max_radius", "Max Radius", 100f, 500f, 250f, 25f),
            SpecialEffect.CustomSetting("expand_speed", "Expand Speed", 0.5f, 4f, 2f),
            SpecialEffect.CustomSetting("distortion", "Distortion", 0f, 1f, 0.3f)
        )
    )

    val GLITCH_BURST = SpecialEffect(
        id = "glitch_burst",
        name = "Glitch Burst",
        description = "Digital glitch fragments scatter in a corrupted data explosion",
        category = EffectCategory.BURST,
        icon = "📺",
        defaultColors = listOf(
            Color(0xFF00FFFF), // Cyan
            Color(0xFFFF00FF), // Magenta
            Color(0xFFFF0000), // Red
            Color(0xFF00FF00), // Green
            Color(0xFFFFFF00)  // Yellow
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION, EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("fragment_count", "Fragments", 10f, 50f, 20f, 5f),
            SpecialEffect.CustomSetting("glitch_intensity", "Glitch Intensity", 0.5f, 2f, 1f),
            SpecialEffect.CustomSetting("scatter_radius", "Scatter Radius", 50f, 300f, 150f, 10f)
        )
    )

    val RAINBOW_BLOOM = SpecialEffect(
        id = "rainbow_bloom",
        name = "Rainbow Bloom",
        description = "A flower of rainbow petals blooms outward gloriously",
        category = EffectCategory.BURST,
        icon = "🌸",
        defaultColors = listOf(
            Color(0xFFFF0000),
            Color(0xFFFF7F00),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFF0000FF),
            Color(0xFF8B00FF)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("petal_count", "Petal Count", 5f, 20f, 10f, 1f),
            SpecialEffect.CustomSetting("bloom_size", "Bloom Size", 100f, 400f, 200f, 20f),
            SpecialEffect.CustomSetting("bloom_speed", "Bloom Speed", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("rotation_speed", "Rotation Speed", 0f, 3f, 0.5f)
        )
    )

    val SOAP_BUBBLE_BURST = SpecialEffect(
        id = "soap_bubble_burst",
        name = "Bubble Pop Burst",
        description = "A giant soap bubble dramatically pops into tiny droplets",
        category = EffectCategory.BURST,
        icon = "🫧",
        defaultColors = listOf(
            Color(0xFFB5DEFF).copy(alpha = 0.8f),
            Color(0xFFE8F4FD).copy(alpha = 0.7f),
            Color(0xFFFFB6C1).copy(alpha = 0.5f),
            Color(0xFFDDA0DD).copy(alpha = 0.5f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("bubble_size", "Bubble Size", 50f, 250f, 120f, 10f),
            SpecialEffect.CustomSetting("droplet_count", "Droplet Count", 20f, 80f, 40f, 5f),
            SpecialEffect.CustomSetting("iridescence", "Iridescence", 0.3f, 1f, 0.7f)
        )
    )

    // ═════════════════════════════════════════════════════════════════
    // AMBIENT EFFECTS (NEW)
    // ═════════════════════════════════════════════════════════════════

    val LAVA_LAMP = SpecialEffect(
        id = "lava_lamp",
        name = "Lava Lamp",
        description = "Groovy lava lamp blobs slowly rise and fall",
        category = EffectCategory.AMBIENT,
        icon = "🫙",
        defaultColors = listOf(
            Color(0xFFFF4500).copy(alpha = 0.7f), // OrangeRed
            Color(0xFFFF69B4).copy(alpha = 0.6f), // HotPink
            Color(0xFFFFD700).copy(alpha = 0.5f), // Gold
            Color(0xFF8B008B).copy(alpha = 0.4f)  // DarkMagenta
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("blob_count", "Blob Count", 3f, 12f, 6f, 1f),
            SpecialEffect.CustomSetting("rise_speed", "Rise Speed", 0.1f, 0.8f, 0.3f),
            SpecialEffect.CustomSetting("blob_size", "Blob Size", 40f, 150f, 80f, 10f),
            SpecialEffect.CustomSetting("viscosity", "Viscosity", 0.2f, 1f, 0.6f)
        )
    )

    val PLASMA_WAVE = SpecialEffect(
        id = "plasma_wave",
        name = "Plasma Wave",
        description = "Flowing plasma energy waves pulse with electric color",
        category = EffectCategory.AMBIENT,
        icon = "🌊",
        defaultColors = listOf(
            Color(0xFF9400D3).copy(alpha = 0.5f), // DarkViolet
            Color(0xFF4169E1).copy(alpha = 0.4f), // RoyalBlue
            Color(0xFF00CED1).copy(alpha = 0.4f), // DarkTurquoise
            Color(0xFF32CD32).copy(alpha = 0.3f)  // LimeGreen
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("wave_frequency", "Wave Frequency", 0.5f, 4f, 2f),
            SpecialEffect.CustomSetting("wave_amplitude", "Wave Amplitude", 20f, 150f, 60f, 10f),
            SpecialEffect.CustomSetting("plasma_speed", "Plasma Speed", 0.2f, 2f, 0.8f),
            SpecialEffect.CustomSetting("opacity", "Opacity", 0.2f, 0.7f, 0.4f)
        )
    )

    val FIREFLIES = SpecialEffect(
        id = "fireflies",
        name = "Fireflies",
        description = "Magical fireflies blink and drift in the dark",
        category = EffectCategory.AMBIENT,
        icon = "✨",
        defaultColors = listOf(
            Color(0xFFFFFF44), // Bright yellow
            Color(0xFF88FF00), // Yellow-green
            Color(0xFFCCFF00), // Lime
            Color(0xFFFFEE00)  // Yellow
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("firefly_count", "Firefly Count", 10f, 80f, 35f, 5f),
            SpecialEffect.CustomSetting("blink_rate", "Blink Rate", 0.3f, 3f, 1f),
            SpecialEffect.CustomSetting("wander_speed", "Wander Speed", 0.2f, 1.5f, 0.6f),
            SpecialEffect.CustomSetting("glow_radius", "Glow Radius", 5f, 30f, 12f, 1f)
        )
    )

    val OCEAN_WAVES = SpecialEffect(
        id = "ocean_waves",
        name = "Ocean Waves",
        description = "Serene ocean waves undulate gently across the screen",
        category = EffectCategory.AMBIENT,
        icon = "🌊",
        defaultColors = listOf(
            Color(0xFF006994).copy(alpha = 0.5f), // Dark blue
            Color(0xFF0099CC).copy(alpha = 0.4f), // Blue
            Color(0xFF00B4D8).copy(alpha = 0.35f), // Light blue
            Color(0xFF90E0EF).copy(alpha = 0.3f)  // Lightest blue
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("wave_layers", "Wave Layers", 2f, 6f, 3f, 1f),
            SpecialEffect.CustomSetting("wave_height", "Wave Height", 10f, 80f, 30f, 5f),
            SpecialEffect.CustomSetting("wave_speed", "Wave Speed", 0.2f, 2f, 0.7f),
            SpecialEffect.CustomSetting("foam_intensity", "Foam Intensity", 0f, 1f, 0.4f)
        )
    )

    val GALAXY_DRIFT = SpecialEffect(
        id = "galaxy_drift",
        name = "Galaxy Drift",
        description = "A spiral galaxy slowly rotates in the background",
        category = EffectCategory.AMBIENT,
        icon = "🌌",
        defaultColors = listOf(
            Color(0xFF2E004F).copy(alpha = 0.6f), // Deep purple
            Color(0xFF1A0030).copy(alpha = 0.7f), // Very dark purple
            Color(0xFFF5D78E).copy(alpha = 0.3f), // Star gold
            Color(0xFFFFFFFF).copy(alpha = 0.2f)  // Stars
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("arm_count", "Spiral Arms", 2f, 6f, 3f, 1f),
            SpecialEffect.CustomSetting("rotation_speed", "Rotation Speed", 0.05f, 0.5f, 0.1f),
            SpecialEffect.CustomSetting("star_density", "Star Density", 50f, 500f, 200f, 25f),
            SpecialEffect.CustomSetting("nebula_opacity", "Nebula Opacity", 0.1f, 0.6f, 0.3f)
        )
    )

    val BIOLUMINESCENCE = SpecialEffect(
        id = "bioluminescence",
        name = "Bioluminescence",
        description = "Deep-sea glowing organisms pulse with ethereal light",
        category = EffectCategory.AMBIENT,
        icon = "🐟",
        defaultColors = listOf(
            Color(0xFF00FFBF).copy(alpha = 0.6f), // Mint green glow
            Color(0xFF00BFFF).copy(alpha = 0.5f), // DeepSkyBlue
            Color(0xFF9370DB).copy(alpha = 0.4f), // MediumPurple
            Color(0xFF00FF7F).copy(alpha = 0.4f)  // SpringGreen
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("organism_count", "Organism Count", 15f, 80f, 35f, 5f),
            SpecialEffect.CustomSetting("pulse_rhythm", "Pulse Rhythm", 0.3f, 3f, 1f),
            SpecialEffect.CustomSetting("drift_speed", "Drift Speed", 0.1f, 1f, 0.4f),
            SpecialEffect.CustomSetting("glow_radius", "Glow Radius", 10f, 60f, 25f, 5f)
        )
    )

    val CITY_LIGHTS = SpecialEffect(
        id = "city_lights",
        name = "City Lights",
        description = "Twinkling city lights shimmer like a skyline at night",
        category = EffectCategory.AMBIENT,
        icon = "🌃",
        defaultColors = listOf(
            Color(0xFFFFD700).copy(alpha = 0.7f), // Gold
            Color(0xFFFF6347).copy(alpha = 0.6f), // Tomato
            Color(0xFF87CEEB).copy(alpha = 0.5f), // SkyBlue
            Color(0xFFFFFFFF).copy(alpha = 0.8f), // White
            Color(0xFFFF69B4).copy(alpha = 0.5f)  // HotPink
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS, EffectTriggerType.SCHEDULED),
        customSettings = listOf(
            SpecialEffect.CustomSetting("light_count", "Light Count", 20f, 200f, 80f, 10f),
            SpecialEffect.CustomSetting("twinkle_speed", "Twinkle Speed", 0.3f, 3f, 1f),
            SpecialEffect.CustomSetting("light_size", "Light Size", 2f, 12f, 5f, 1f)
        )
    )

    // ═════════════════════════════════════════════════════════════════
    // REACTIVE EFFECTS (NEW)
    // ═════════════════════════════════════════════════════════════════

    val GYROSCOPE_WAVE = SpecialEffect(
        id = "gyroscope_wave",
        name = "Gyroscope Wave",
        description = "Particles shift and flow as you tilt your device",
        category = EffectCategory.REACTIVE,
        icon = "🌀",
        defaultColors = listOf(
            Color(0xFF4FC3F7).copy(alpha = 0.7f),
            Color(0xFF81D4FA).copy(alpha = 0.6f),
            Color(0xFFE1F5FE).copy(alpha = 0.5f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("sensitivity", "Tilt Sensitivity", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("particle_count", "Particle Count", 20f, 150f, 60f, 10f),
            SpecialEffect.CustomSetting("fluid_viscosity", "Fluid Viscosity", 0.1f, 1f, 0.5f)
        )
    )

    val SHAKE_CONFETTI = SpecialEffect(
        id = "shake_confetti",
        name = "Shake Confetti",
        description = "Shake your device to spray confetti everywhere",
        category = EffectCategory.REACTIVE,
        icon = "🎉",
        defaultColors = listOf(
            Color(0xFFFF6B6B),
            Color(0xFF4ECDC4),
            Color(0xFFFFE66D),
            Color(0xFF95E1D3),
            Color(0xFFF38181),
            Color(0xFFAA96DA)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL),
        customSettings = listOf(
            SpecialEffect.CustomSetting("shake_threshold", "Shake Sensitivity", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("confetti_count", "Confetti Count", 20f, 150f, 60f, 10f),
            SpecialEffect.CustomSetting("burst_force", "Burst Force", 0.5f, 3f, 1.5f)
        )
    )

    val MULTI_TOUCH_FIREWORKS = SpecialEffect(
        id = "multi_touch_fireworks",
        name = "Multi-Touch Fireworks",
        description = "Each finger tap ignites a separate firework",
        category = EffectCategory.REACTIVE,
        icon = "🎆",
        defaultColors = listOf(
            Color(0xFFFF0000),
            Color(0xFF00FF00),
            Color(0xFF0000FF),
            Color(0xFFFFFF00),
            Color(0xFFFF00FF),
            Color(0xFF00FFFF)
        ),
        triggerTypes = listOf(EffectTriggerType.ON_TOUCH),
        customSettings = listOf(
            SpecialEffect.CustomSetting("spark_count", "Sparks per Rocket", 20f, 80f, 40f, 5f),
            SpecialEffect.CustomSetting("rocket_speed", "Rocket Speed", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("explosion_radius", "Explosion Radius", 50f, 300f, 150f, 10f)
        )
    )

    val VOLUME_VISUALIZER = SpecialEffect(
        id = "volume_visualizer",
        name = "Volume Visualizer",
        description = "Animated bars and waves dance to your device volume level",
        category = EffectCategory.REACTIVE,
        icon = "🔊",
        defaultColors = listOf(
            Color(0xFF1DB954), // Spotify green
            Color(0xFF0099FF), // Blue
            Color(0xFFFF3366), // Pink-red
            Color(0xFFFFCC00)  // Yellow
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("bar_count", "Bar Count", 8f, 48f, 24f, 4f),
            SpecialEffect.CustomSetting("sensitivity", "Sensitivity", 0.5f, 3f, 1.5f),
            SpecialEffect.CustomSetting("bar_style", "Bar Style (0=rect, 1=round, 2=circle)", 0f, 2f, 0f, 1f)
        )
    )

    val SCROLL_PARALLAX_EFFECT = SpecialEffect(
        id = "scroll_parallax_effect",
        name = "Scroll Parallax",
        description = "Background layers shift at different speeds as you scroll",
        category = EffectCategory.REACTIVE,
        icon = "📜",
        defaultColors = listOf(
            Color(0xFF87CEEB).copy(alpha = 0.3f),
            Color(0xFF98FB98).copy(alpha = 0.25f),
            Color(0xFFDDA0DD).copy(alpha = 0.2f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("layer_count", "Parallax Layers", 2f, 5f, 3f, 1f),
            SpecialEffect.CustomSetting("depth_scale", "Depth Scale", 0.2f, 1f, 0.5f),
            SpecialEffect.CustomSetting("particle_layer_density", "Particle Density", 0.2f, 1f, 0.5f)
        )
    )

    // ═════════════════════════════════════════════════════════════════
    // SPECIAL / ARTISTIC EFFECTS (NEW)
    // ═════════════════════════════════════════════════════════════════

    val GLITCH_ART = SpecialEffect(
        id = "glitch_art",
        name = "Glitch Art",
        description = "Artistic digital glitch errors slice and shift the display",
        category = EffectCategory.OVERLAY,
        icon = "📺",
        defaultColors = listOf(
            Color(0xFF00FFFF).copy(alpha = 0.4f),
            Color(0xFFFF00FF).copy(alpha = 0.3f),
            Color(0xFFFF0000).copy(alpha = 0.2f)
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("glitch_frequency", "Glitch Frequency", 0.1f, 2f, 0.5f),
            SpecialEffect.CustomSetting("slice_count", "Slice Count", 2f, 20f, 8f, 1f),
            SpecialEffect.CustomSetting("chromatic_shift", "Chromatic Shift", 0f, 30f, 8f, 1f),
            SpecialEffect.CustomSetting("scan_lines", "Scan Lines", 0f, 1f, 0.5f)
        )
    )

    val LIQUID_METAL = SpecialEffect(
        id = "liquid_metal",
        name = "Liquid Metal",
        description = "Shiny liquid mercury-like blobs morph and flow",
        category = EffectCategory.AMBIENT,
        icon = "🪨",
        defaultColors = listOf(
            Color(0xFFD3D3D3).copy(alpha = 0.7f), // LightGray
            Color(0xFFC0C0C0).copy(alpha = 0.8f), // Silver
            Color(0xFF808080).copy(alpha = 0.6f), // Gray
            Color(0xFFFFFFFF).copy(alpha = 0.5f)  // White highlights
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("blob_count", "Blob Count", 3f, 10f, 5f, 1f),
            SpecialEffect.CustomSetting("fluidity", "Fluidity", 0.3f, 1f, 0.7f),
            SpecialEffect.CustomSetting("reflectivity", "Reflectivity", 0.3f, 1f, 0.8f),
            SpecialEffect.CustomSetting("viscosity", "Viscosity", 0.2f, 1f, 0.5f)
        )
    )

    val CHROMATIC_ABERRATION = SpecialEffect(
        id = "chromatic_aberration",
        name = "Chromatic Aberration",
        description = "RGB color channels split apart for a lens distortion effect",
        category = EffectCategory.OVERLAY,
        icon = "🎨",
        defaultColors = listOf(
            Color(0xFFFF0000).copy(alpha = 0.3f), // Red channel
            Color(0xFF00FF00).copy(alpha = 0.3f), // Green channel
            Color(0xFF0000FF).copy(alpha = 0.3f)  // Blue channel
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("split_amount", "Channel Split", 2f, 20f, 6f, 1f),
            SpecialEffect.CustomSetting("pulse_speed", "Pulse Speed", 0f, 2f, 0.5f),
            SpecialEffect.CustomSetting("edge_only", "Edge Only", 0f, 1f, 0f, 1f)
        )
    )

    val VHS_EFFECT = SpecialEffect(
        id = "vhs_effect",
        name = "VHS Effect",
        description = "Retro VHS tape artifacts: tracking lines, static, and color bleed",
        category = EffectCategory.OVERLAY,
        icon = "📼",
        defaultColors = listOf(
            Color(0x22FFFFFF), // Static white
            Color(0x22FF0000), // Red color bleed
            Color(0x22000000)  // Shadow
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("tracking_error", "Tracking Error", 0f, 1f, 0.3f),
            SpecialEffect.CustomSetting("static_noise", "Static Noise", 0f, 0.5f, 0.15f),
            SpecialEffect.CustomSetting("color_bleed", "Color Bleed", 0f, 0.5f, 0.2f),
            SpecialEffect.CustomSetting("scanlines", "Scan Lines", 0f, 1f, 0.5f)
        )
    )

    val PIXEL_SORT = SpecialEffect(
        id = "pixel_sort",
        name = "Pixel Sort",
        description = "Pixels sort themselves into streaking color bands",
        category = EffectCategory.OVERLAY,
        icon = "🌈",
        defaultColors = listOf(
            Color(0xFFFF6B6B).copy(alpha = 0.5f),
            Color(0xFF4ECDC4).copy(alpha = 0.5f),
            Color(0xFFFFE66D).copy(alpha = 0.5f)
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("sort_direction", "Sort Direction (0=H, 1=V)", 0f, 1f, 0f, 1f),
            SpecialEffect.CustomSetting("sort_threshold", "Sort Threshold", 0.1f, 0.9f, 0.4f),
            SpecialEffect.CustomSetting("sort_speed", "Sort Speed", 0.2f, 2f, 0.8f)
        )
    )

    val NEON_OUTLINE = SpecialEffect(
        id = "neon_outline",
        name = "Neon Outline",
        description = "Glowing neon outlines trace the edges of UI elements",
        category = EffectCategory.OVERLAY,
        icon = "💡",
        defaultColors = listOf(
            Color(0xFF39FF14), // Neon green
            Color(0xFFFF00FF), // Neon magenta
            Color(0xFF00FFFF), // Neon cyan
            Color(0xFFFF6600)  // Neon orange
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("outline_width", "Outline Width", 1f, 8f, 3f, 0.5f),
            SpecialEffect.CustomSetting("glow_spread", "Glow Spread", 2f, 20f, 8f, 1f),
            SpecialEffect.CustomSetting("flicker_speed", "Flicker Speed", 0f, 2f, 0.5f),
            SpecialEffect.CustomSetting("cycle_colors", "Color Cycling Speed", 0f, 2f, 0.5f)
        )
    )

    val CANDY_SPIRAL = SpecialEffect(
        id = "candy_spiral",
        name = "Candy Spiral",
        description = "A mesmerizing candy-stripe spiral hypnotizes from the center",
        category = EffectCategory.OVERLAY,
        icon = "🍭",
        defaultColors = listOf(
            Color(0xFFFF69B4).copy(alpha = 0.4f), // HotPink
            Color(0xFFFFFFFF).copy(alpha = 0.35f), // White
            Color(0xFF00CED1).copy(alpha = 0.3f)  // DarkTurquoise
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("spiral_arms", "Spiral Arms", 2f, 8f, 4f, 1f),
            SpecialEffect.CustomSetting("rotation_speed", "Rotation Speed", 0.2f, 3f, 0.8f),
            SpecialEffect.CustomSetting("zoom_speed", "Zoom Speed", 0.1f, 1f, 0.3f),
            SpecialEffect.CustomSetting("opacity", "Opacity", 0.1f, 0.6f, 0.3f)
        )
    )

    val DEPTH_OF_FIELD = SpecialEffect(
        id = "depth_of_field",
        name = "Depth of Field",
        description = "A realistic bokeh blur effect with glowing light circles",
        category = EffectCategory.OVERLAY,
        icon = "📷",
        defaultColors = listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.6f),
            Color(0xFFFFF9C4).copy(alpha = 0.5f),
            Color(0xFFB3E5FC).copy(alpha = 0.4f)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("bokeh_count", "Bokeh Count", 10f, 60f, 25f, 5f),
            SpecialEffect.CustomSetting("bokeh_size", "Bokeh Size", 20f, 100f, 50f, 5f),
            SpecialEffect.CustomSetting("blur_intensity", "Blur Intensity", 0.2f, 1f, 0.5f),
            SpecialEffect.CustomSetting("drift_speed", "Drift Speed", 0.1f, 1f, 0.3f)
        )
    )

    val NORTHERN_LIGHTS_AMBIENT = SpecialEffect(
        id = "northern_lights_ambient",
        name = "Northern Lights",
        description = "Sweeping curtains of aurora light in greens and purples",
        category = EffectCategory.AMBIENT,
        icon = "🌠",
        defaultColors = listOf(
            Color(0xFF00FF88).copy(alpha = 0.4f),
            Color(0xFF00CCFF).copy(alpha = 0.35f),
            Color(0xFFAA00FF).copy(alpha = 0.3f),
            Color(0xFF3366FF).copy(alpha = 0.35f)
        ),
        isPremium = true,
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS, EffectTriggerType.SCHEDULED),
        customSettings = listOf(
            SpecialEffect.CustomSetting("curtain_count", "Curtain Count", 2f, 6f, 3f, 1f),
            SpecialEffect.CustomSetting("sway_speed", "Sway Speed", 0.1f, 1f, 0.3f),
            SpecialEffect.CustomSetting("intensity", "Intensity", 0.2f, 0.8f, 0.4f),
            SpecialEffect.CustomSetting("altitude", "Altitude", 0.1f, 0.9f, 0.5f)
        )
    )

    val CONFETTI_CANNON = SpecialEffect(
        id = "confetti_cannon",
        name = "Confetti Cannon",
        description = "Party confetti cannons blast streams from the screen corners",
        category = EffectCategory.BURST,
        icon = "🎉",
        defaultColors = listOf(
            Color(0xFFFF6B6B),
            Color(0xFF4ECDC4),
            Color(0xFFFFE66D),
            Color(0xFF95E1D3),
            Color(0xFFF38181),
            Color(0xFFAA96DA),
            Color(0xFFFF9A9E)
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("cannon_count", "Cannons", 1f, 4f, 2f, 1f),
            SpecialEffect.CustomSetting("piece_count", "Piece Count", 50f, 300f, 150f, 25f),
            SpecialEffect.CustomSetting("launch_angle", "Launch Spread", 10f, 60f, 30f, 5f),
            SpecialEffect.CustomSetting("gravity", "Gravity", 0.5f, 2f, 1f)
        )
    )

    val METEOR_STRIKE = SpecialEffect(
        id = "meteor_strike",
        name = "Meteor Strike",
        description = "Fiery meteors crash down from the top of the screen",
        category = EffectCategory.BURST,
        icon = "🌑",
        defaultColors = listOf(
            Color(0xFFFF4500), // OrangeRed
            Color(0xFFFF8C00), // DarkOrange
            Color(0xFFFFD700), // Gold
            Color(0xFFFF6347)  // Tomato
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.ON_TOUCH, EffectTriggerType.ON_CELEBRATION),
        customSettings = listOf(
            SpecialEffect.CustomSetting("meteor_count", "Meteor Count", 1f, 10f, 4f, 1f),
            SpecialEffect.CustomSetting("impact_radius", "Impact Radius", 50f, 250f, 100f, 10f),
            SpecialEffect.CustomSetting("tail_length", "Tail Length", 50f, 300f, 120f, 10f),
            SpecialEffect.CustomSetting("fall_speed", "Fall Speed", 1f, 5f, 2.5f)
        )
    )

    val GLOWING_RUNES = SpecialEffect(
        id = "glowing_runes",
        name = "Glowing Runes",
        description = "Mystical glowing runes and symbols drift across the screen",
        category = EffectCategory.AMBIENT,
        icon = "🔮",
        defaultColors = listOf(
            Color(0xFF9B59B6).copy(alpha = 0.7f), // Purple
            Color(0xFF3498DB).copy(alpha = 0.6f), // Blue
            Color(0xFF1ABC9C).copy(alpha = 0.6f), // Emerald
            Color(0xFFE74C3C).copy(alpha = 0.5f)  // Red
        ),
        triggerTypes = listOf(EffectTriggerType.MANUAL, EffectTriggerType.AUTO_ALWAYS),
        customSettings = listOf(
            SpecialEffect.CustomSetting("rune_count", "Rune Count", 5f, 30f, 12f, 1f),
            SpecialEffect.CustomSetting("float_speed", "Float Speed", 0.1f, 1f, 0.4f),
            SpecialEffect.CustomSetting("glow_intensity", "Glow Intensity", 0.3f, 1f, 0.7f),
            SpecialEffect.CustomSetting("rune_size", "Rune Size", 12f, 48f, 24f, 4f)
        )
    )

    // ═════════════════════════════════════════════════════════════════
    // ALL NEW EFFECTS LIST
    // ═════════════════════════════════════════════════════════════════

    val ALL_NEW_EFFECTS = listOf(
        // New Particle Effects
        PIXEL_RAIN,
        BUBBLE_POP,
        LIGHTNING_STRIKE,
        FIRE_SPARKS,
        AURORA_PARTICLES,
        CRYSTAL_SHARDS,
        FLOWER_PETALS,
        EMOJI_RAIN,
        DNA_HELIX,
        COMET_SHOWER,
        NEON_DOTS,

        // New Trail Effects
        INK_TRAIL,
        FIRE_TRAIL,
        GALAXY_TRAIL,
        WATER_RIPPLE_TRAIL,
        BUTTERFLY_TRAIL,
        MAGIC_WAND_TRAIL,
        LASER_TRAIL,

        // New Overlay Effects
        KALEIDOSCOPE,
        NEON_GRID,
        STARFIELD,
        MATRIX_RAIN,
        VINTAGE_FILM,
        PRISM_LIGHT,
        HOLOGRAPHIC_SHIMMER,

        // New Burst Effects
        DIAMOND_BURST,
        SUPERNOVA,
        COLOR_BOMB,
        SHOCKWAVE,
        GLITCH_BURST,
        RAINBOW_BLOOM,
        SOAP_BUBBLE_BURST,
        CONFETTI_CANNON,
        METEOR_STRIKE,

        // New Ambient Effects
        LAVA_LAMP,
        PLASMA_WAVE,
        FIREFLIES,
        OCEAN_WAVES,
        GALAXY_DRIFT,
        BIOLUMINESCENCE,
        CITY_LIGHTS,
        NORTHERN_LIGHTS_AMBIENT,
        LIQUID_METAL,
        GLOWING_RUNES,

        // New Reactive Effects
        GYROSCOPE_WAVE,
        SHAKE_CONFETTI,
        MULTI_TOUCH_FIREWORKS,
        VOLUME_VISUALIZER,
        SCROLL_PARALLAX_EFFECT,

        // New Artistic/Special Effects
        GLITCH_ART,
        CHROMATIC_ABERRATION,
        VHS_EFFECT,
        PIXEL_SORT,
        NEON_OUTLINE,
        CANDY_SPIRAL,
        DEPTH_OF_FIELD
    )

    val NEW_FREE_EFFECTS = ALL_NEW_EFFECTS.filter { !it.isPremium }
    val NEW_PREMIUM_EFFECTS = ALL_NEW_EFFECTS.filter { it.isPremium }
}
