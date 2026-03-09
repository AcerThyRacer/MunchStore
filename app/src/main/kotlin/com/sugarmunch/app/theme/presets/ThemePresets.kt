package com.sugarmunch.app.theme.presets

import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.theme.model.*

/**
 * MASSIVE Theme Presets Collection - SugarRush Intensity
 */
object ThemePresets {
    
    // ═════════════════════════════════════════════════════════════════
    // CLASSIC CANDY THEMES
    // ═════════════════════════════════════════════════════════════════
    
    val CLASSIC_CANDY = CandyTheme(
        id = "classic_candy",
        name = "Classic Candy",
        description = "The original SugarMunch experience",
        baseColors = BaseColors(
            primary = Color(0xFFFFB6C1),      // Candy Pink
            secondary = Color(0xFF98FF98),    // Mint
            tertiary = Color(0xFFFFFACD),     // Lemon
            accent = Color(0xFFB5DEFF),       // Cotton Candy Blue
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFFFEDE0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFF1A1A1A),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A),
            error = Color(0xFFFF6B6B),
            success = Color(0xFF51CF66)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1f, 0.1f, "Sweetness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFFBF7),
                Color(0xFFFFE4EC).copy(alpha = 0.3f),
                Color(0xFFE8F8FF).copy(alpha = 0.2f)
            ),
            intenseColors = listOf(
                Color(0xFFFFB6C1),
                Color(0xFFFFE4EC),
                Color(0xFFB5DEFF)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFB6C1),
                Color(0xFF98FF98),
                Color(0xFFFFFACD),
                Color(0xFFB5DEFF)
            ),
            count = 30..50,
            type = ParticleType.FLOATING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.CLASSIC
    )
    
    val COTTON_CANDY = CandyTheme(
        id = "cotton_candy",
        name = "Cotton Candy",
        description = "Dreamy pink and blue swirls",
        baseColors = BaseColors(
            primary = Color(0xFFFF9ECD),
            secondary = Color(0xFFB5DEFF),
            tertiary = Color(0xFFE8C5FF),
            accent = Color(0xFFFFD6E8),
            surface = Color(0xFFFFF0F7),
            surfaceVariant = Color(0xFFFFE8F3),
            background = Color(0xFFFFFAFD),
            onPrimary = Color(0xFF2D1B2E),
            onSurface = Color(0xFF2D1B2E),
            onBackground = Color(0xFF2D1B2E)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1f, 0.1f, "Fluffiness"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFF9ECD).copy(alpha = 0.2f),
                Color(0xFFB5DEFF).copy(alpha = 0.2f),
                Color(0xFFE8C5FF).copy(alpha = 0.15f)
            ),
            animationSpeed = 0.5f,
            complexity = 4
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF9ECD).copy(alpha = 0.6f),
                Color(0xFFB5DEFF).copy(alpha = 0.6f)
            ),
            count = 40..80,
            type = ParticleType.SWIRLING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.8f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.CLASSIC
    )
    
    val SOUR_PATCH = CandyTheme(
        id = "sour_patch",
        name = "Sour Patch",
        description = "Tangy green and yellow burst",
        baseColors = BaseColors(
            primary = Color(0xFF7FFF00),      // Chartreuse
            secondary = Color(0xFFFFEA00),    // Sour Yellow
            tertiary = Color(0xFF00FF7F),     // Spring Green
            accent = Color(0xFFADFF2F),       // Green Yellow
            surface = Color(0xFFF8FFF0),
            surfaceVariant = Color(0xFFEFFFDC),
            background = Color(0xFFFCFFF8),
            onPrimary = Color(0xFF0A1A00),
            onSurface = Color(0xFF1A2E00),
            onBackground = Color(0xFF1A2E00)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.2f, 0.1f, "Sourness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFCFFF8),
                Color(0xFFDFFFAC).copy(alpha = 0.3f),
                Color(0xFFFFFDD0).copy(alpha = 0.2f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF7FFF00),
                Color(0xFFFFEA00),
                Color(0xFF00FF7F)
            ),
            count = 35..60,
            type = ParticleType.CHAOTIC,
            speed = FloatRange(1f, 3f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.3f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.CLASSIC
    )
    
    // ═════════════════════════════════════════════════════════════════
    // SUGARRUSH THEMES (High Intensity)
    // ═════════════════════════════════════════════════════════════════
    
    val SUGARRUSH_CLASSIC = CandyTheme(
        id = "sugarrush_classic",
        name = "SugarRush",
        description = "Maximum candy energy!",
        baseColors = BaseColors(
            primary = Color(0xFFFF1493),      // Deep Pink
            secondary = Color(0xFF00CED1),    // Dark Turquoise
            tertiary = Color(0xFFFFD700),     // Gold
            accent = Color(0xFFFF4500),       // Orange Red
            surface = Color(0xFFFFE4F3),
            surfaceVariant = Color(0xFFFFD6EC),
            background = Color(0xFFFFF0F7),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A0A14),
            onBackground = Color(0xFF1A0A14)
        ),
        intensityConfig = IntensityConfig(0.5f, 2f, 1.5f, 0.1f, "Rush Level"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFF0F7),
                Color(0xFFFFB6C1).copy(alpha = 0.4f),
                Color(0xFFB5DEFF).copy(alpha = 0.3f),
                Color(0xFFFFFACD).copy(alpha = 0.3f)
            ),
            intenseColors = listOf(
                Color(0xFFFF1493).copy(alpha = 0.6f),
                Color(0xFF00CED1).copy(alpha = 0.5f),
                Color(0xFFFFD700).copy(alpha = 0.5f),
                Color(0xFFFF4500).copy(alpha = 0.4f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF1493),
                Color(0xFF00CED1),
                Color(0xFFFFD700),
                Color(0xFFFF4500),
                Color(0xFF7B68EE)
            ),
            count = 60..100,
            type = ParticleType.EXPLODING,
            speed = FloatRange(2f, 5f),
            intensityMultiplier = 1.5f
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2f,
            backgroundAnimationEnabled = true,
            transitionDuration = 200
        ),
        isDark = false,
        category = ThemeCategory.SUGARRUSH
    )
    
    val SUGARRUSH_NUCLEAR = CandyTheme(
        id = "sugarrush_nuclear",
        name = "Nuclear Rush",
        description = "Radioactive candy glow",
        baseColors = BaseColors(
            primary = Color(0xFF39FF14),      // Neon Green
            secondary = Color(0xFF00FFFF),    // Cyan
            tertiary = Color(0xFFFF00FF),     // Magenta
            accent = Color(0xFFFFFF00),       // Yellow
            surface = Color(0xFF0A1A00),
            surfaceVariant = Color(0xFF142900),
            background = Color(0xFF051000),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE8FFE0),
            onBackground = Color(0xFFE8FFE0)
        ),
        intensityConfig = IntensityConfig(0.8f, 2f, 1.8f, 0.1f, "Radiation"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF051000),
                Color(0xFF0A1A00),
                Color(0xFF143300).copy(alpha = 0.8f)
            ),
            intenseColors = listOf(
                Color(0xFF39FF14).copy(alpha = 0.3f),
                Color(0xFF00FFFF).copy(alpha = 0.2f),
                Color(0xFF000000)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF39FF14),
                Color(0xFF00FFFF),
                Color(0xFFFF00FF)
            ),
            count = 80..150,
            type = ParticleType.CHAOTIC,
            speed = FloatRange(3f, 8f),
            size = FloatRange(1f, 5f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 3f,
            backgroundAnimationEnabled = true,
            transitionDuration = 150
        ),
        isDark = true,
        category = ThemeCategory.SUGARRUSH
    )
    
    val SUGARRUSH_VOLCANO = CandyTheme(
        id = "sugarrush_volcano",
        name = "Volcano Pop",
        description = "Explosive cinnamon and heat",
        baseColors = BaseColors(
            primary = Color(0xFFFF2400),      // Scarlet
            secondary = Color(0xFFFF8C00),    // Dark Orange
            tertiary = Color(0xFFFFD700),     // Gold
            accent = Color(0xFFDC143C),       // Crimson
            surface = Color(0xFF1A0505),
            surfaceVariant = Color(0xFF2A0A0A),
            background = Color(0xFF0D0202),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFFFE4E1),
            onBackground = Color(0xFFFFE4E1)
        ),
        intensityConfig = IntensityConfig(0.7f, 2f, 1.6f, 0.1f, "Heat Level"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF0D0202),
                Color(0xFF1A0505),
                Color(0xFF2A0A0A)
            ),
            intenseColors = listOf(
                Color(0xFFFF2400).copy(alpha = 0.4f),
                Color(0xFFFF8C00).copy(alpha = 0.3f),
                Color(0xFF0D0202)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF2400),
                Color(0xFFFF8C00),
                Color(0xFFFFD700),
                Color(0xFFFF4500)
            ),
            count = 70..120,
            type = ParticleType.RISING,
            speed = FloatRange(2f, 6f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2.5f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory.SUGARRUSH
    )
    
    // ═════════════════════════════════════════════════════════════════
    // TRIPPY / PSYCHEDELIC THEMES
    // ═════════════════════════════════════════════════════════════════
    
    val TRIPPY_RAINBOW = CandyTheme(
        id = "trippy_rainbow",
        name = "Psychedelic Rainbow",
        description = "All colors, all the time",
        baseColors = BaseColors(
            primary = Color(0xFFFF0080),      // Hot Pink
            secondary = Color(0xFF00FF80),    // Spring Green
            tertiary = Color(0xFF8000FF),     // Violet
            accent = Color(0xFFFF8000),       // Orange
            surface = Color(0xFF1A0A2E),
            surfaceVariant = Color(0xFF2A1540),
            background = Color(0xFF0D0518),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFF0E0FF),
            onBackground = Color(0xFFF0E0FF)
        ),
        intensityConfig = IntensityConfig(1f, 2f, 1.5f, 0.1f, "Trip Level"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFF0080).copy(alpha = 0.3f),
                Color(0xFF00FF80).copy(alpha = 0.3f),
                Color(0xFF8000FF).copy(alpha = 0.3f),
                Color(0xFFFF8000).copy(alpha = 0.3f)
            ),
            animationSpeed = 2f,
            complexity = 6
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF0000),
                Color(0xFFFF7F00),
                Color(0xFFFFFF00),
                Color(0xFF00FF00),
                Color(0xFF0000FF),
                Color(0xFF4B0082),
                Color(0xFF9400D3)
            ),
            count = 100..200,
            type = ParticleType.SWIRLING,
            speed = FloatRange(2f, 8f),
            intensityMultiplier = 2f
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 3f,
            backgroundAnimationEnabled = true,
            transitionDuration = 100
        ),
        isDark = true,
        category = ThemeCategory.TRIPPY
    )
    
    val TRIPPY_LIQUID = CandyTheme(
        id = "trippy_liquid",
        name = "Liquid Dreams",
        description = "Flowing liquid colors",
        baseColors = BaseColors(
            primary = Color(0xFF00CED1),      // Dark Turquoise
            secondary = Color(0xFFFF69B4),    // Hot Pink
            tertiary = Color(0xFF7B68EE),     // Medium Slate Blue
            accent = Color(0xFF00FA9A),       // Medium Spring Green
            surface = Color(0xFF0A1A2E),
            surfaceVariant = Color(0xFF142A40),
            background = Color(0xFF050D18),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFE0F0FF),
            onBackground = Color(0xFFE0F0FF)
        ),
        intensityConfig = IntensityConfig(0.8f, 2f, 1.4f, 0.1f, "Fluidity"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFF00CED1).copy(alpha = 0.25f),
                Color(0xFFFF69B4).copy(alpha = 0.25f),
                Color(0xFF7B68EE).copy(alpha = 0.25f)
            ),
            animationSpeed = 1.5f,
            complexity = 5
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF00CED1).copy(alpha = 0.8f),
                Color(0xFFFF69B4).copy(alpha = 0.8f),
                Color(0xFF7B68EE).copy(alpha = 0.8f)
            ),
            count = 60..120,
            type = ParticleType.SWIRLING,
            speed = FloatRange(1f, 4f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.5f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory.TRIPPY
    )
    
    val TRIPPY_GALAXY = CandyTheme(
        id = "trippy_galaxy",
        name = "Candy Galaxy",
        description = "Cosmic candy nebulas",
        baseColors = BaseColors(
            primary = Color(0xFFE0B0FF),      // Mauve
            secondary = Color(0xFF87CEEB),    // Sky Blue
            tertiary = Color(0xFFFFDAB9),     // Peach Puff
            accent = Color(0xFF98FB98),       // Pale Green
            surface = Color(0xFF0D0D1A),
            surfaceVariant = Color(0xFF1A1A2E),
            background = Color(0xFF050510),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE8E8FF),
            onBackground = Color(0xFFE8E8FF)
        ),
        intensityConfig = IntensityConfig(0.5f, 2f, 1.2f, 0.1f, "Cosmic Power"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFE0B0FF).copy(alpha = 0.15f),
                Color(0xFF87CEEB).copy(alpha = 0.15f),
                Color(0xFF4B0082).copy(alpha = 0.2f),
                Color(0xFF000033).copy(alpha = 0.3f)
            ),
            animationSpeed = 0.3f,
            complexity = 8
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFE0B0FF),
                Color(0xFF87CEEB),
                Color(0xFFFFDAB9)
            ),
            count = 50..150,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.2f, 1f),
            size = FloatRange(0.5f, 3f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.5f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory.TRIPPY
    )
    
    val TRIPPY_ACID = CandyTheme(
        id = "trippy_acid",
        name = "Acid Trip",
        description = "Neon wasteland",
        baseColors = BaseColors(
            primary = Color(0xFFCCFF00),      // Electric Lime
            secondary = Color(0xFF00FFCC),    // Caribbean Green
            tertiary = Color(0xFFFF00CC),     // Hot Magenta
            accent = Color(0xFFFFCC00),       // Electric Yellow
            surface = Color(0xFF0A1400),
            surfaceVariant = Color(0xFF142100),
            background = Color(0xFF050A00),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE8FFD6),
            onBackground = Color(0xFFE8FFD6)
        ),
        intensityConfig = IntensityConfig(1.2f, 2f, 1.8f, 0.1f, "Acidity"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF050A00),
                Color(0xFF0A1400),
                Color(0xFF142900)
            ),
            intenseColors = listOf(
                Color(0xFFCCFF00).copy(alpha = 0.2f),
                Color(0xFF00FFCC).copy(alpha = 0.15f),
                Color(0xFFFF00CC).copy(alpha = 0.1f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFCCFF00),
                Color(0xFF00FFCC),
                Color(0xFFFF00CC)
            ),
            count = 100..180,
            type = ParticleType.CHAOTIC,
            speed = FloatRange(4f, 12f),
            size = FloatRange(1f, 6f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 4f,
            backgroundAnimationEnabled = true,
            transitionDuration = 80
        ),
        isDark = true,
        category = ThemeCategory.TRIPPY
    )
    
    // ═════════════════════════════════════════════════════════════════
    // CHILL THEMES (Relaxed)
    // ═════════════════════════════════════════════════════════════════
    
    val CHILL_MINT = CandyTheme(
        id = "chill_mint",
        name = "Cool Mint",
        description = "Refreshing and calm",
        baseColors = BaseColors(
            primary = Color(0xFF98D8C8),      // Pale Teal
            secondary = Color(0xFFB8E0D2),    // Mint Cream
            tertiary = Color(0xFFD4F1F4),     // Light Cyan
            accent = Color(0xFFA8E6CF),       // Sea Foam
            surface = Color(0xFFF5FBF9),
            surfaceVariant = Color(0xFFE8F5F2),
            background = Color(0xFFFAFDFC),
            onPrimary = Color(0xFF0A1A16),
            onSurface = Color(0xFF1A2E2A),
            onBackground = Color(0xFF1A2E2A)
        ),
        intensityConfig = IntensityConfig(0f, 1.5f, 0.6f, 0.1f, "Coolness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFAFDFC),
                Color(0xFFE8F5F2).copy(alpha = 0.5f),
                Color(0xFFD4F1F4).copy(alpha = 0.3f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF98D8C8).copy(alpha = 0.7f),
                Color(0xFFB8E0D2).copy(alpha = 0.7f)
            ),
            count = 12..25,
            type = ParticleType.BUBBLES,
            speed = FloatRange(0.15f, 0.4f),
            size = FloatRange(6f, 14f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.5f,
            backgroundAnimationEnabled = false
        ),
        isDark = false,
        category = ThemeCategory.CHILL
    )
    
    val CHILL_CHOCOLATE = CandyTheme(
        id = "chill_chocolate",
        name = "Dark Chocolate",
        description = "Rich and comforting",
        baseColors = BaseColors(
            primary = Color(0xFFD2691E),      // Chocolate
            secondary = Color(0xFF8B4513),    // Saddle Brown
            tertiary = Color(0xFFCD853F),     // Peru
            accent = Color(0xFFF4A460),       // Sandy Brown
            surface = Color(0xFF2A1F1A),
            surfaceVariant = Color(0xFF3D2C24),
            background = Color(0xFF1A1410),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFF5E6DC),
            onBackground = Color(0xFFF5E6DC)
        ),
        intensityConfig = IntensityConfig(0f, 1.5f, 0.5f, 0.1f, "Richness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF1A1410),
                Color(0xFF2A1F1A),
                Color(0xFF1A1410)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = false,
            colors = emptyList()
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.3f,
            backgroundAnimationEnabled = false
        ),
        isDark = true,
        category = ThemeCategory.CHILL
    )
    
    // ═════════════════════════════════════════════════════════════════
    // DARK THEMES
    // ═════════════════════════════════════════════════════════════════
    
    val DARK_BERRY = CandyTheme(
        id = "dark_berry",
        name = "Midnight Berry",
        description = "Deep purple darkness",
        baseColors = BaseColors(
            primary = Color(0xFF9932CC),      // Dark Orchid
            secondary = Color(0xFF8A2BE2),    // Blue Violet
            tertiary = Color(0xFFDA70D6),     // Orchid
            accent = Color(0xFFBA55D3),       // Medium Orchid
            surface = Color(0xFF1A0A2E),
            surfaceVariant = Color(0xFF2A1540),
            background = Color(0xFF0D0518),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFE8D5F2),
            onBackground = Color(0xFFE8D5F2)
        ),
        intensityConfig = IntensityConfig(0.3f, 2f, 0.8f, 0.1f, "Berry Power"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF0D0518),
                Color(0xFF1A0A2E),
                Color(0xFF0D0518)
            ),
            intenseColors = listOf(
                Color(0xFF9932CC).copy(alpha = 0.2f),
                Color(0xFF0D0518)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF9932CC).copy(alpha = 0.6f),
                Color(0xFFDA70D6).copy(alpha = 0.4f)
            ),
            count = 30..60,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.5f, 1.5f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory.DARK
    )
    
    val DARK_COCOA = CandyTheme(
        id = "dark_cocoa",
        name = "Black Cocoa",
        description = "Pure dark chocolate",
        baseColors = BaseColors(
            primary = Color(0xFFA0522D),      // Sienna
            secondary = Color(0xFF8B7355),    // Burlywood Dark
            tertiary = Color(0xFFBC8F8F),     // Rosy Brown
            accent = Color(0xFFD2B48C),       // Tan
            surface = Color(0xFF14100D),
            surfaceVariant = Color(0xFF1F1A14),
            background = Color(0xFF0A0806),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFE8E0D8),
            onBackground = Color(0xFFE8E0D8)
        ),
        intensityConfig = IntensityConfig(0f, 1.5f, 0.4f, 0.1f, "Darkness"),
        backgroundStyle = BackgroundStyle.Solid(Color(0xFF0A0806)),
        particleConfig = ParticleConfig(enabled = false, colors = emptyList()),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.2f,
            backgroundAnimationEnabled = false
        ),
        isDark = true,
        category = ThemeCategory.DARK
    )
    
    // ═════════════════════════════════════════════════════════════════
    // DYNAMIC TIME-OF-DAY THEMES (used by ThemeManager auto-switch)
    // ═════════════════════════════════════════════════════════════════
    
    val SUNRISE_SHERBET = CandyTheme(
        id = "sunrise_sherbet",
        name = "Sunrise Sherbet",
        description = "Soft morning light – light mode",
        baseColors = BaseColors(
            primary = Color(0xFFFFB088),      // Peach
            secondary = Color(0xFFFFD4A3),     // Apricot
            tertiary = Color(0xFFFFE4C4),      // Cream
            accent = Color(0xFF87CEEB),        // Sky blue
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFFFF0E0),
            background = Color(0xFFFFFBF5),
            onPrimary = Color(0xFF2D1A0A),
            onSurface = Color(0xFF2D1A0A),
            onBackground = Color(0xFF2D1A0A)
        ),
        intensityConfig = IntensityConfig(0.5f, 1.5f, 1f, 0.1f, "Dawn"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFFBF5),
                Color(0xFFFFE4C4).copy(alpha = 0.4f),
                Color(0xFF87CEEB).copy(alpha = 0.15f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFB088).copy(alpha = 0.6f),
                Color(0xFFFFD4A3).copy(alpha = 0.5f)
            ),
            count = 20..45,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.3f, 1f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.7f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.CLASSIC
    )
    
    val SUNSET_SWIRL = CandyTheme(
        id = "sunset_swirl",
        name = "Sunset Swirl",
        description = "Warm evening glow",
        baseColors = BaseColors(
            primary = Color(0xFFFF6B4A),      // Coral
            secondary = Color(0xFFFF8C69),      // Salmon
            tertiary = Color(0xFFFFB088),      // Peach
            accent = Color(0xFFE07C50),        // Terracotta
            surface = Color(0xFFFFF0E8),
            surfaceVariant = Color(0xFFFFE4D8),
            background = Color(0xFFFFF5EE),
            onPrimary = Color(0xFF2D1510),
            onSurface = Color(0xFF2D1510),
            onBackground = Color(0xFF2D1510)
        ),
        intensityConfig = IntensityConfig(0.6f, 1.6f, 1.1f, 0.1f, "Dusk"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFF5EE),
                Color(0xFFFFB088).copy(alpha = 0.35f),
                Color(0xFFFF6B4A).copy(alpha = 0.2f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF6B4A).copy(alpha = 0.6f),
                Color(0xFFFF8C69).copy(alpha = 0.5f)
            ),
            count = 25..55,
            type = ParticleType.SWIRLING,
            speed = FloatRange(0.4f, 1.2f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.9f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.CLASSIC
    )
    
    val MIDNIGHT_MINT = CandyTheme(
        id = "midnight_mint",
        name = "Midnight Mint",
        description = "Cool dark mode for night",
        baseColors = BaseColors(
            primary = Color(0xFF98E6C9),      // Mint
            secondary = Color(0xFF7DD3B0),    // Darker mint
            tertiary = Color(0xFFB8F0DC),     // Light mint
            accent = Color(0xFF5EC4A1),        // Teal mint
            surface = Color(0xFF1A2520),
            surfaceVariant = Color(0xFF24302A),
            background = Color(0xFF0F1614),
            onPrimary = Color(0xFF0A1410),
            onSurface = Color(0xFFE0F5EC),
            onBackground = Color(0xFFE0F5EC)
        ),
        intensityConfig = IntensityConfig(0.5f, 1.8f, 1f, 0.1f, "Night"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF0F1614),
                Color(0xFF1A2520),
                Color(0xFF0F1614)
            ),
            intenseColors = listOf(
                Color(0xFF98E6C9).copy(alpha = 0.12f),
                Color(0xFF5EC4A1).copy(alpha = 0.08f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF98E6C9).copy(alpha = 0.5f),
                Color(0xFF7DD3B0).copy(alpha = 0.4f)
            ),
            count = 30..60,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.2f, 0.8f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.6f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory.DARK
    )
    
    // ═════════════════════════════════════════════════════════════════
    // SEASONAL THEMES
    // ═════════════════════════════════════════════════════════════════
    
    val HALLOWEEN_CANDY = CandyTheme(
        id = "halloween_candy",
        name = "Spooky Sweet",
        description = "Trick or treat!",
        baseColors = BaseColors(
            primary = Color(0xFFFF6600),      // Halloween Orange
            secondary = Color(0xFF660099),    // Witch Purple
            tertiary = Color(0xFF00FF00),     // Slime Green
            accent = Color(0xFF000000),       // Black
            surface = Color(0xFF1A0A1A),
            surfaceVariant = Color(0xFF2A152A),
            background = Color(0xFF0D050D),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFFFE4C4),
            onBackground = Color(0xFFFFE4C4)
        ),
        intensityConfig = IntensityConfig(0.5f, 2f, 1.2f, 0.1f, "Spookiness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF0D050D),
                Color(0xFF1A0A1A),
                Color(0xFF0D050D)
            ),
            intenseColors = listOf(
                Color(0xFFFF6600).copy(alpha = 0.3f),
                Color(0xFF660099).copy(alpha = 0.2f),
                Color(0xFF0D050D)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF6600),
                Color(0xFF660099),
                Color(0xFF00FF00)
            ),
            count = 40..80,
            type = ParticleType.RAINING,
            speed = FloatRange(1f, 3f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.5f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory.SEASONAL
    )
    
    val CHRISTMAS_PEPPERMINT = CandyTheme(
        id = "christmas_peppermint",
        name = "Peppermint Twist",
        description = "Holiday cheer!",
        baseColors = BaseColors(
            primary = Color(0xFFDC143C),      // Crimson
            secondary = Color(0xFF228B22),    // Forest Green
            tertiary = Color(0xFFFFFFFF),     // White
            accent = Color(0xFFFFD700),       // Gold
            surface = Color(0xFFF0FFF0),
            surfaceVariant = Color(0xFFE0F0E0),
            background = Color(0xFFF8FFF8),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0A1A0A),
            onBackground = Color(0xFF0A1A0A)
        ),
        intensityConfig = IntensityConfig(0.5f, 2f, 1.1f, 0.1f, "Holiday Spirit"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFF8FFF8),
                Color(0xFFE0F0E0).copy(alpha = 0.5f),
                Color(0xFFF0F8FF).copy(alpha = 0.3f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFFFD700),
                Color(0xFFDC143C).copy(alpha = 0.6f)
            ),
            count = 60..120,
            type = ParticleType.SPARKLE,
            speed = FloatRange(2f, 5f),
            size = FloatRange(1f, 4f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.SEASONAL
    )
    
    // ═════════════════════════════════════════════════════════════════
    // ALL THEMES LIST
    // ═════════════════════════════════════════════════════════════════
    
    val ALL_THEMES = listOf(
        // Classic
        CLASSIC_CANDY,
        COTTON_CANDY,
        SOUR_PATCH,
        
        // SugarRush
        SUGARRUSH_CLASSIC,
        SUGARRUSH_NUCLEAR,
        SUGARRUSH_VOLCANO,
        
        // Trippy
        TRIPPY_RAINBOW,
        TRIPPY_LIQUID,
        TRIPPY_GALAXY,
        TRIPPY_ACID,
        
        // Chill
        CHILL_MINT,
        CHILL_CHOCOLATE,
        
        // Dark
        DARK_BERRY,
        DARK_COCOA,
        
        // Dynamic time-of-day
        SUNRISE_SHERBET,
        SUNSET_SWIRL,
        MIDNIGHT_MINT,
        
        // Seasonal
        HALLOWEEN_CANDY,
        CHRISTMAS_PEPPERMINT
    )
    
    // Include 2026 themes and Phase 1 utility themes
    val ALL_THEMES_2026 = ALL_THEMES + Candy2026Themes.ALL_2026_THEMES + PhaseOneUtilityThemes.ALL
    
    /**
     * Get default theme
     */
    fun getDefault(): CandyTheme = CLASSIC_CANDY
    
    /**
     * Get theme by ID (searches both legacy and 2026 themes)
     */
    fun getById(id: String): CandyTheme? = ALL_THEMES_2026.find { it.id == id }
    
    /**
     * Get themes for a category
     */
    fun getByCategory(category: ThemeCategory): List<CandyTheme> = 
        ALL_THEMES_2026.filter { it.category == category }
    
    /**
     * Get random theme
     */
    fun getRandom(): CandyTheme = ALL_THEMES_2026.random()
    
    /**
     * Get random SugarRush theme for intense moments
     */
    fun getRandomSugarRush(): CandyTheme = 
        ALL_THEMES.filter { it.category == ThemeCategory.SUGARRUSH }.random()
    
    /**
     * Get random Trippy theme
     */
    fun getRandomTrippy(): CandyTheme = 
        ALL_THEMES.filter { it.category == ThemeCategory.TRIPPY }.random()
}
