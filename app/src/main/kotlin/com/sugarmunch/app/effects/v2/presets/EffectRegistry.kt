package com.sugarmunch.app.effects.v2.presets

import com.sugarmunch.app.effects.v2.model.EffectV2

/**
 * Complete Effect Registry - 16+ SugarRush Effects
 */
object EffectRegistry {
    
    // ═════════════════════════════════════════════════════════════════
    // VISUAL OVERLAY EFFECTS
    // ═════════════════════════════════════════════════════════════════
    
    val SUGARRUSH_OVERLAY = SugarrushOverlayEffect()
    val RAINBOW_TINT = RainbowTintEffect()
    val MINT_WASH = MintWashEffect()
    val CARAMEL_DIM = CaramelDimEffect()
    val CHOCOLATE_DARKNESS = ChocolateDarknessEffect()
    val LOLLIPOP_GLOW = LollipopGlowEffect()
    
    // ═════════════════════════════════════════════════════════════════
    // PARTICLE EFFECTS
    // ═════════════════════════════════════════════════════════════════
    
    val CANDY_CONFETTI = CandyConfettiEffect()
    val CHOCOLATE_RAIN = ChocolateRainEffect()
    val GUMMY_WIGGLE = GummyWiggleEffect()
    val POP_ROCKS = PopRocksEffect()
    val CANDY_FIREWORKS = CandyFireworksEffect()
    
    // ═════════════════════════════════════════════════════════════════
    // ANIMATION EFFECTS
    // ═════════════════════════════════════════════════════════════════
    
    val UNICORN_SWIRL = UnicornSwirlEffect()
    val ICE_CRYSTAL = IceCrystalEffect()
    val CINNAMON_FIRE = CinnamonFireEffect()
    
    // ═════════════════════════════════════════════════════════════════
    // HAPTIC EFFECTS
    // ═════════════════════════════════════════════════════════════════
    
    val HEARTBEAT_HAPTIC = HeartbeatHapticEffect()
    val GUMMY_BOUNCE = GummyBounceEffect()
    val CRUNCH_HAPTIC = CrunchHapticEffect()
    val FIZZY_SODA = FizzySodaEffect()
    
    // ═════════════════════════════════════════════════════════════════
    // ALL EFFECTS LIST
    // ═════════════════════════════════════════════════════════════════
    
    val ALL_EFFECTS: List<EffectV2> = listOf(
        // Visual overlays (6)
        SUGARRUSH_OVERLAY,
        RAINBOW_TINT,
        MINT_WASH,
        CARAMEL_DIM,
        CHOCOLATE_DARKNESS,
        LOLLIPOP_GLOW,
        
        // Particles (5)
        CANDY_CONFETTI,
        CHOCOLATE_RAIN,
        GUMMY_WIGGLE,
        POP_ROCKS,
        CANDY_FIREWORKS,
        
        // Animations (3)
        UNICORN_SWIRL,
        ICE_CRYSTAL,
        CINNAMON_FIRE,
        
        // Haptic (4)
        HEARTBEAT_HAPTIC,
        GUMMY_BOUNCE,
        CRUNCH_HAPTIC,
        FIZZY_SODA
    )
    
    fun getById(id: String): EffectV2? = ALL_EFFECTS.find { it.id == id }
    
    fun getByCategory(category: com.sugarmunch.app.effects.v2.model.EffectCategory): List<EffectV2> =
        ALL_EFFECTS.filter { it.category == category }
}
