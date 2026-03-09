package com.sugarmunch.app.effects.v2.presets

import com.sugarmunch.app.effects.v2.model.*

/**
 * SugarRush Effect Presets - Curated combinations
 */
object EffectPresets {
    
    // ═════════════════════════════════════════════════════════════════
    // CHILL PRESETS (Relaxed)
    // ═════════════════════════════════════════════════════════════════
    
    val CHILL_MINT = EffectPreset(
        id = "chill_mint",
        name = "Cool Mint",
        description = "Refreshing and calm",
        icon = "🌿",
        category = PresetCategory.CHILL,
        effects = listOf(
            EffectConfig("mint_wash", 0.5f),
            EffectConfig("ice_crystal", 0.4f)
        )
    )
    
    val CHILL_CHOCOLATE = EffectPreset(
        id = "chill_chocolate",
        name = "Cocoa Comfort",
        description = "Warm and comforting",
        icon = "🍫",
        category = PresetCategory.CHILL,
        effects = listOf(
            EffectConfig("chocolate_darkness", 0.6f),
            EffectConfig("chocolate_rain", 0.3f)
        )
    )
    
    val CHILL_VANILLA = EffectPreset(
        id = "chill_vanilla",
        name = "Vanilla Sky",
        description = "Soft and soothing",
        icon = "☁️",
        category = PresetCategory.CHILL,
        effects = listOf(
            EffectConfig("caramel_dim", 0.5f),
            EffectConfig("heartbeat_haptic", 0.4f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // FOCUS PRESETS (Productivity)
    // ═════════════════════════════════════════════════════════════════
    
    val FOCUS_MODE = EffectPreset(
        id = "focus_mode",
        name = "Deep Focus",
        description = "Minimal distractions",
        icon = "🎯",
        category = PresetCategory.FOCUS,
        effects = listOf(
            EffectConfig("chocolate_darkness", 0.4f),
            EffectConfig("mint_wash", 0.2f)
        )
    )
    
    val STUDY_BUDDY = EffectPreset(
        id = "study_buddy",
        name = "Study Buddy",
        description = "Gentle motivation",
        icon = "📚",
        category = PresetCategory.FOCUS,
        effects = listOf(
            EffectConfig("mint_wash", 0.3f),
            EffectConfig("heartbeat_haptic", 0.5f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // PARTY PRESETS (Celebration)
    // ═════════════════════════════════════════════════════════════════
    
    val PARTY_TIME = EffectPreset(
        id = "party_time",
        name = "Party Time!",
        description = "Full celebration mode",
        icon = "🎉",
        category = PresetCategory.PARTY,
        effects = listOf(
            EffectConfig("sugarrush_overlay", 1.5f),
            EffectConfig("candy_confetti", 1.5f),
            EffectConfig("candy_fireworks", 1.2f),
            EffectConfig("pop_rocks", 1f),
            EffectConfig("gummy_bounce", 1.2f)
        )
    )
    
    val BIRTHDAY_BASH = EffectPreset(
        id = "birthday_bash",
        name = "Birthday Bash",
        description = "Make a wish!",
        icon = "🎂",
        category = PresetCategory.PARTY,
        effects = listOf(
            EffectConfig("rainbow_tint", 1.2f),
            EffectConfig("candy_confetti", 1.8f),
            EffectConfig("lollipop_glow", 1.5f),
            EffectConfig("heartbeat_haptic", 1.5f)
        )
    )
    
    val DISCO_CANDY = EffectPreset(
        id = "disco_candy",
        name = "Disco Candy",
        description = "Dance floor vibes",
        icon = "🪩",
        category = PresetCategory.PARTY,
        effects = listOf(
            EffectConfig("unicorn_swirl", 1.8f),
            EffectConfig("lollipop_glow", 1.5f),
            EffectConfig("pop_rocks", 1.5f),
            EffectConfig("fizzy_soda", 1.5f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // TRIPPY PRESETS (Psychedelic)
    // ═════════════════════════════════════════════════════════════════
    
    val TRIPPY_RAINBOW = EffectPreset(
        id = "trippy_rainbow",
        name = "Psychedelic",
        description = "Full rainbow experience",
        icon = "🌈",
        category = PresetCategory.TRIPPY,
        effects = listOf(
            EffectConfig("rainbow_tint", 1.5f),
            EffectConfig("unicorn_swirl", 2f),
            EffectConfig("candy_confetti", 1.5f),
            EffectConfig("gummy_wiggle", 1.2f)
        )
    )
    
    val ACID_TRIP = EffectPreset(
        id = "acid_trip",
        name = "Acid Trip",
        description = "Intense visual experience",
        icon = "🌀",
        category = PresetCategory.TRIPPY,
        effects = listOf(
            EffectConfig("sugarrush_overlay", 2f),
            EffectConfig("unicorn_swirl", 2f),
            EffectConfig("gummy_wiggle", 1.8f),
            EffectConfig("pop_rocks", 1.8f),
            EffectConfig("fizzy_soda", 1.5f)
        )
    )
    
    val COSMIC_VOYAGE = EffectPreset(
        id = "cosmic_voyage",
        name = "Cosmic Voyage",
        description = "Space travel vibes",
        icon = "🚀",
        category = PresetCategory.TRIPPY,
        effects = listOf(
            EffectConfig("unicorn_swirl", 1.2f),
            EffectConfig("candy_fireworks", 0.8f),
            EffectConfig("lollipop_glow", 1f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // GAMING PRESETS (Gaming-focused)
    // ═════════════════════════════════════════════════════════════════
    
    val GAMING_MODE = EffectPreset(
        id = "gaming_mode",
        name = "Gaming Mode",
        description = "Enhanced immersion",
        icon = "🎮",
        category = PresetCategory.GAMING,
        effects = listOf(
            EffectConfig("lollipop_glow", 0.8f),
            EffectConfig("heartbeat_haptic", 0.6f),
            EffectConfig("cinnamon_fire", 0.5f)
        )
    )
    
    val VICTORY_RUSH = EffectPreset(
        id = "victory_rush",
        name = "Victory Rush",
        description = "Winning celebration",
        icon = "🏆",
        category = PresetCategory.GAMING,
        effects = listOf(
            EffectConfig("sugarrush_overlay", 1.5f),
            EffectConfig("candy_fireworks", 2f),
            EffectConfig("candy_confetti", 1.8f),
            EffectConfig("gummy_bounce", 1.5f)
        )
    )
    
    val BOSS_BATTLE = EffectPreset(
        id = "boss_battle",
        name = "Boss Battle",
        description = "Intense encounter",
        icon = "👹",
        category = PresetCategory.GAMING,
        effects = listOf(
            EffectConfig("sugarrush_overlay", 1.8f),
            EffectConfig("cinnamon_fire", 1.5f),
            EffectConfig("heartbeat_haptic", 1.5f),
            EffectConfig("crunch_haptic", 1.2f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // SIGNATURE PRESETS
    // ═════════════════════════════════════════════════════════════════
    
    val SUGARRUSH_MAXIMUM = EffectPreset(
        id = "sugarrush_maximum",
        name = "MAXIMUM OVERDRIVE",
        description = "Everything at maximum!",
        icon = "🔥",
        category = PresetCategory.PARTY,
        effects = listOf(
            EffectConfig("sugarrush_overlay", 2f),
            EffectConfig("rainbow_tint", 2f),
            EffectConfig("candy_confetti", 2f),
            EffectConfig("candy_fireworks", 2f),
            EffectConfig("pop_rocks", 2f),
            EffectConfig("unicorn_swirl", 2f),
            EffectConfig("lollipop_glow", 2f),
            EffectConfig("heartbeat_haptic", 2f),
            EffectConfig("gummy_bounce", 2f),
            EffectConfig("fizzy_soda", 2f)
        )
    )
    
    val CANDY_SHOP = EffectPreset(
        id = "candy_shop",
        name = "Candy Shop",
        description = "Classic SugarMunch experience",
        icon = "🏪",
        category = PresetCategory.CUSTOM,
        effects = listOf(
            EffectConfig("sugarrush_overlay", 1f),
            EffectConfig("candy_confetti", 1f),
            EffectConfig("heartbeat_haptic", 0.8f)
        )
    )
    
    // ═════════════════════════════════════════════════════════════════
    // ALL PRESETS
    // ═════════════════════════════════════════════════════════════════
    
    val ALL_PRESETS = listOf(
        // Chill
        CHILL_MINT,
        CHILL_CHOCOLATE,
        CHILL_VANILLA,
        
        // Focus
        FOCUS_MODE,
        STUDY_BUDDY,
        
        // Party
        PARTY_TIME,
        BIRTHDAY_BASH,
        DISCO_CANDY,
        
        // Trippy
        TRIPPY_RAINBOW,
        ACID_TRIP,
        COSMIC_VOYAGE,
        
        // Gaming
        GAMING_MODE,
        VICTORY_RUSH,
        BOSS_BATTLE,
        
        // Signature
        SUGARRUSH_MAXIMUM,
        CANDY_SHOP
    )
    
    fun getById(id: String): EffectPreset? = ALL_PRESETS.find { it.id == id }
    
    fun getByCategory(category: PresetCategory): List<EffectPreset> = 
        ALL_PRESETS.filter { it.category == category }
    
    fun getQuickPresets(): List<EffectPreset> = listOf(
        CHILL_MINT,
        FOCUS_MODE,
        PARTY_TIME,
        TRIPPY_RAINBOW,
        GAMING_MODE,
        SUGARRUSH_MAXIMUM
    )
}
