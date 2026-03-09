package com.sugarmunch.app.theme.presets

import com.sugarmunch.app.theme.model.*

/**
 * 10 NEW Premium Themes for Phase 4
 */
object NewThemePresets {

    // ═════════════════════════════════════════════════════════════
    // PREMIUM EXCLUSIVE THEMES
    // ═════════════════════════════════════════════════════════════

    val GOLDEN_CANDY = CandyTheme(
        id = "golden_candy",
        name = "Golden Candy",
        description = "Pure gold and caramel luxury",
        category = ThemeCategory.PREMIUM,
        baseColors = BaseColors(
            primary = 0xFFFFD700,
            secondary = 0xFFFFE4B5,
            tertiary = 0xFFFFA500,
            background = 0xFF2C1810,
            onPrimary = 0xFF000000,
            onSecondary = 0xFF000000,
            onBackground = 0xFFFFF8DC
        ),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(0xFF2C1810, 0xFF8B4513, 0xFFDAA520),
            angle = 135f,
            isIntense = true
        ),
        particleConfig = ParticleConfig(
            type = ParticleType.RISING,
            count = 80,
            speed = 0.8f,
            size = 8.0f,
            color = 0xFFFFD700
        ),
        animationConfig = AnimationConfig(
            cardPulseSpeed = 2.0f,
            transitionSpeed = 1.5f,
            shimmerIntensity = 1.5f
        ),
        intensityLevels = IntensityLevels(
            themeIntensity = 1.5f,
            backgroundIntensity = 1.3f,
            particleIntensity = 1.2f,
            animationIntensity = 1.4f
        ),
        icon = "🌟",
        isPremium = true
    )

    val DIAMOND_DUST = CandyTheme(
        id = "diamond_dust",
        name = "Diamond Dust",
        description = "Sparkling ice crystal elegance",
        category = ThemeCategory.PREMIUM,
        baseColors = BaseColors(
            primary = 0xB9F2FF,
            secondary = 0xE0FFFF,
            tertiary = 0xF0FFFF,
            background = 0xFF0A1628,
            onPrimary = 0xFF000000,
            onSecondary = 0xFF000000,
            onBackground = 0xFFE0FFFF
        ),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            meshComplexity = 8,
            animationSpeed = 0.8f,
            color1 = 0xFF0A1628,
            color2 = 0xFF1E3A5F,
            color3 = 0xFF4682B4
        ),
        particleConfig = ParticleConfig(
            type = ParticleType.FLOATING,
            count = 100,
            speed = 0.5f,
            size = 4.0f,
            color = 0xFFFFFFFF
        ),
        animationConfig = AnimationConfig(
            cardPulseSpeed = 1.5f,
            transitionSpeed = 1.2f,
            shimmerIntensity = 2.0f
        ),
        intensityLevels = IntensityLevels(
            themeIntensity = 1.3f,
            backgroundIntensity = 1.5f,
            particleIntensity = 1.8f,
            animationIntensity = 1.6f
        ),
        icon = "💎",
        isPremium = true
    )

    val CYBER_PUNK = CandyTheme(
        id = "cyber_punk",
        name = "Cyber Punk",
        description = "Neon-lit futuristic cityscape",
        category = ThemeCategory.PREMIUM,
        baseColors = BaseColors(
            primary = 0xFFFF00FF,
            secondary = 0xFF00FFFF,
            tertiary = 0xFFFF1493,
            background = 0xFF0D0221,
            onPrimary = 0xFFFFFFFF,
            onSecondary = 0xFF000000,
            onBackground = 0xFF00FFFF
        ),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(0xFF0D0221, 0xFF2B1055, 0xFF7F00FF),
            angle = 90f,
            isIntense = true
        ),
        particleConfig = ParticleConfig(
            type = ParticleType.RAINING,
            count = 150,
            speed = 2.0f,
            size = 3.0f,
            color = 0xFF00FF00
        ),
        animationConfig = AnimationConfig(
            cardPulseSpeed = 2.5f,
            transitionSpeed = 2.0f,
            shimmerIntensity = 1.8f
        ),
        intensityLevels = IntensityLevels(
            themeIntensity = 1.8f,
            backgroundIntensity = 1.6f,
            particleIntensity = 1.5f,
            animationIntensity = 2.0f
        ),
        icon = "🌆",
        isPremium = true
    )

    val SAKURA_DREAMS = CandyTheme(
        id = "sakura_dreams",
        name = "Sakura Dreams",
        description = "Cherry blossom serenity",
        category = ThemeCategory.PREMIUM,
        baseColors = BaseColors(
            primary = 0xFFFFB7C5,
            secondary = 0xFFFFC0CB,
            tertiary = 0xFFFF69B4,
            background = 0xFF2D1B2E,
            onPrimary = 0xFF000000,
            onSecondary = 0xFF000000,
            onBackground = 0xFFFFF0F5
        ),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(0xFF2D1B2E, 0xFF4B2C5F, 0xFFFFB7C5),
            angle = 45f,
            isIntense = false
        ),
        particleConfig = ParticleConfig(
            type = ParticleType.RAINING,
            count = 60,
            speed = 0.6f,
            size = 10.0f,
            color = 0xFFFFB7C5
        ),
        animationConfig = AnimationConfig(
            cardPulseSpeed = 1.2f,
            transitionSpeed = 1.0f,
            shimmerIntensity = 1.0f
        ),
        intensityLevels = IntensityLevels(
            themeIntensity = 1.0f,
            backgroundIntensity = 0.8f,
            particleIntensity = 1.0f,
            animationIntensity = 0.9f
        ),
        icon = "🌸",
        isPremium = true,
        isSeasonal = true,
        season = "Spring"
    )

    val VOLCANIC_HEAT = CandyTheme(
        id = "volcanic_heat",
        name = "Volcanic Heat",
        description = "Molten lava and embers",
        category = ThemeCategory.PREMIUM,
        baseColors = BaseColors(
            primary = 0xFFFF4500,
            secondary = 0xFFFF6347,
            tertiary = 0xFFFFD700,
            background = 0xFF1A0A00,
            onPrimary = 0xFFFFFFFF,
            onSecondary = 0xFFFFFFFF,
            onBackground = 0xFFFFD700
        ),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            meshComplexity = 6,
            animationSpeed = 1.5f,
            color1 = 0xFF1A0A00,
            color2 = 0xFF4B0000,
            color3 = 0xFFFF4500
        ),
        particleConfig = ParticleConfig(
            type = ParticleType.RISING,
            count = 70,
            speed = 1.5f,
            size = 12.0f,
            color = 0xFFFF4500
        ),
        animationConfig = AnimationConfig(
            cardPulseSpeed = 1.8f,
            transitionSpeed = 1.5f,
            shimmerIntensity = 1.6f
        ),
        intensityLevels = IntensityLevels(
            themeIntensity = 1.6f,
            backgroundIntensity = 1.8f,
            particleIntensity = 1.4f,
            animationIntensity = 1.5f
        ),
        icon = "🌋",
        isPremium = true
    )

    val OCEAN_DEPTHS = CandyTheme(
        id = "ocean_depths",
        name = "Ocean Depths",
        description = "Deep sea mystery and wonder",
        category = ThemeCategory.PREMIUM,
        baseColors = BaseColors(
            primary = 0xFF00CED1,
            secondary = 0xFF20B2AA,
            tertiary = 0xFF48D1CC,
            background = 0xFF001219,
            onPrimary = 0xFF000000,
            onSecondary = 0xFF000000,
            onBackground = 0xFFE0FFFF
        ),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(0xFF001219, 0xFF003554, 0xFF007EA7),
            angle = 180f,
            isIntense = true
        ),
        particleConfig = ParticleConfig(
            type = ParticleType.FLOATING,
            count = 90,
            speed = 0.7f,
            size = 6.0f,
            color = 0xFF7FFFD4
        ),
        animationConfig = AnimationConfig(
            cardPulseSpeed = 1.3f,
            transitionSpeed = 1.1f,
            shimmerIntensity = 1.4f
        ),
        intensityLevels = IntensityLevels(
            themeIntensity = 1.2f,
            backgroundIntensity = 1.4f,
            particleIntensity = 1.3f,
            animationIntensity = 1.2f
        ),
        icon = "🌊",
        isPremium = true
    )

    val FOREST_SPIRIT = CandyTheme(
        id = "forest_spirit",
        name = "Forest Spirit",
        description = "Enchanted woodland magic",
        category = ThemeCategory.PREMIUM,
        baseColors = BaseColors(
            primary = 0xFF32CD32,
            secondary = 0xFF228B22,
            tertiary = 0xFF90EE90,
            background = 0xFF0D1B0D,
            onPrimary = 0xFF000000,
            onSecondary = 0xFFFFFFFF,
            onBackground = 0xFF90EE90
        ),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            meshComplexity = 7,
            animationSpeed = 0.6f,
            color1 = 0xFF0D1B0D,
            color2 = 0xFF1B4332,
            color3 = 0xFF2D6A4F
        ),
        particleConfig = ParticleConfig(
            type = ParticleType.RISING,
            count = 50,
            speed = 0.5f,
            size = 8.0f,
            color = 0xFF98FB98
        ),
        animationConfig = AnimationConfig(
            cardPulseSpeed = 1.0f,
            transitionSpeed = 0.9f,
            shimmerIntensity = 1.1f
        ),
        intensityLevels = IntensityLevels(
            themeIntensity = 1.0f,
            backgroundIntensity = 1.1f,
            particleIntensity = 0.9f,
            animationIntensity = 1.0f
        ),
        icon = "🌲",
        isPremium = true
    )

    val COSMIC_VOYAGER = CandyTheme(
        id = "cosmic_voyager",
        name = "Cosmic Voyager",
        description = "Journey through the cosmos",
        category = ThemeCategory.PREMIUM,
        baseColors = BaseColors(
            primary = 0xFF9400D3,
            secondary = 0xFF8A2BE2,
            tertiary = 0xFFBA55D3,
            background = 0xFF0B0014,
            onPrimary = 0xFFFFFFFF,
            onSecondary = 0xFFFFFFFF,
            onBackground = 0xFFE6E6FA
        ),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(0xFF0B0014, 0xFF1E003E, 0xFF4B0082, 0xFF9400D3),
            angle = 270f,
            isIntense = true
        ),
        particleConfig = ParticleConfig(
            type = ParticleType.CHAOTIC,
            count = 120,
            speed = 1.2f,
            size = 5.0f,
            color = 0xFFFFFFFF
        ),
        animationConfig = AnimationConfig(
            cardPulseSpeed = 1.6f,
            transitionSpeed = 1.4f,
            shimmerIntensity = 1.7f
        ),
        intensityLevels = IntensityLevels(
            themeIntensity = 1.7f,
            backgroundIntensity = 1.9f,
            particleIntensity = 1.6f,
            animationIntensity = 1.8f
        ),
        icon = "🚀",
        isPremium = true
    )

    val DESERT_ROSE = CandyTheme(
        id = "desert_rose",
        name = "Desert Rose",
        description = "Warm sands and sunset glow",
        category = ThemeCategory.PREMIUM,
        baseColors = BaseColors(
            primary = 0xFFC19A6B,
            secondary = 0xFFE6BE8A,
            tertiary = 0xFFFF7F50,
            background = 0xFF1A0F0A,
            onPrimary = 0xFF000000,
            onSecondary = 0xFF000000,
            onBackground = 0xFFFFF8DC
        ),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(0xFF1A0F0A, 0xFF8B4513, 0xFFDEB887),
            angle = 60f,
            isIntense = false
        ),
        particleConfig = ParticleConfig(
            type = ParticleType.FLOATING,
            count = 40,
            speed = 0.4f,
            size = 7.0f,
            color = 0xFFFFE4B5
        ),
        animationConfig = AnimationConfig(
            cardPulseSpeed = 1.1f,
            transitionSpeed = 1.0f,
            shimmerIntensity = 1.2f
        ),
        intensityLevels = IntensityLevels(
            themeIntensity = 1.1f,
            backgroundIntensity = 1.0f,
            particleIntensity = 0.8f,
            animationIntensity = 1.0f
        ),
        icon = "🌹",
        isPremium = true
    )

    val RAINBOW_UNICORN = CandyTheme(
        id = "rainbow_unicorn",
        name = "Rainbow Unicorn",
        description = "Maximum sparkle power!",
        category = ThemeCategory.PREMIUM,
        baseColors = BaseColors(
            primary = 0xFFFF69B4,
            secondary = 0xFF9370DB,
            tertiary = 0xFF00CED1,
            background = 0xFF1A0033,
            onPrimary = 0xFFFFFFFF,
            onSecondary = 0xFFFFFFFF,
            onBackground = 0xFFFFF0F5
        ),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            meshComplexity = 10,
            animationSpeed = 2.0f,
            color1 = 0xFF1A0033,
            color2 = 0xFF4B0082,
            color3 = 0xFFFF1493
        ),
        particleConfig = ParticleConfig(
            type = ParticleType.EXPLODING,
            count = 200,
            speed = 1.8f,
            size = 10.0f,
            color = 0xFFFFFFFF
        ),
        animationConfig = AnimationConfig(
            cardPulseSpeed = 2.5f,
            transitionSpeed = 2.2f,
            shimmerIntensity = 2.0f
        ),
        intensityLevels = IntensityLevels(
            themeIntensity = 2.0f,
            backgroundIntensity = 2.0f,
            particleIntensity = 2.0f,
            animationIntensity = 2.0f
        ),
        icon = "🦄",
        isPremium = true,
        isExclusive = true
    )

    /**
     * Get all new premium themes
     */
    fun getAllNewThemes(): List<CandyTheme> {
        return listOf(
            GOLDEN_CANDY,
            DIAMOND_DUST,
            CYBER_PUNK,
            SAKURA_DREAMS,
            VOLCANIC_HEAT,
            OCEAN_DEPTHS,
            FOREST_SPIRIT,
            COSMIC_VOYAGER,
            DESERT_ROSE,
            RAINBOW_UNICORN
        )
    }

    /**
     * Get premium themes only
     */
    fun getPremiumThemes(): List<CandyTheme> {
        return getAllNewThemes().filter { it.isPremium }
    }

    /**
     * Get seasonal themes
     */
    fun getSeasonalThemes(): List<CandyTheme> {
        return getAllNewThemes().filter { it.isSeasonal }
    }

    /**
     * Get exclusive themes
     */
    fun getExclusiveThemes(): List<CandyTheme> {
        return getAllNewThemes().filter { it.isExclusive }
    }
}
