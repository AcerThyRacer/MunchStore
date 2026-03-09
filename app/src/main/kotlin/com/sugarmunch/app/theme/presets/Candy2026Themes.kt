package com.sugarmunch.app.theme.presets

import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.theme.model.*

/**
 * 🍭 2026 CANDY STYLE MEGA THEME COLLECTION
 * 40+ New Ultra-Modern Candy Themes for the Future
 * 
 * Categories:
 * - NEON CANDY (Cyberpunk-inspired glowing treats)
 * - GALACTIC SWEETS (Space-themed cosmic candy)
 * - RETRO WAVE (80s/90s nostalgia)
 * - FRUIT BLAST (Tropical and fresh)
 * - GOURMET (Sophisticated dessert vibes)
 * - KAWAII (Cute Japanese-inspired)
 * - HOLOGRAPHIC (Iridescent dreamy themes)
 * - METALLIC CANDY (Shiny metallic treats)
 */

object Candy2026Themes {
    
    // ═════════════════════════════════════════════════════════════════
    // NEON CANDY - Cyberpunk glowing treats
    // ═════════════════════════════════════════════════════════════════
    
    val NEON_BUBBLEGUM = CandyTheme(
        id = "neon_bubblegum",
        name = "Neon Bubblegum",
        description = "Electric pink cyberpunk pop",
        baseColors = BaseColors(
            primary = Color(0xFFFF10F0),      // Hot Magenta Neon
            secondary = Color(0xFF00FFFF),    // Cyan Electric
            tertiary = Color(0xFFFF0080),     // Neon Pink
            accent = Color(0xFFBF00FF),       // Electric Purple
            surface = Color(0xFF0A0014),
            surfaceVariant = Color(0xFF140029),
            background = Color(0xFF05000A),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFFFE4F3),
            onBackground = Color(0xFFFFE4F3)
        ),
        intensityConfig = IntensityConfig(0.8f, 2f, 1.4f, 0.1f, "Neon Glow"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFF10F0).copy(alpha = 0.15f),
                Color(0xFF00FFFF).copy(alpha = 0.12f),
                Color(0xFFBF00FF).copy(alpha = 0.1f)
            ),
            animationSpeed = 1.8f,
            complexity = 5
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF10F0),
                Color(0xFF00FFFF),
                Color(0xFFFF0080)
            ),
            count = 80..150,
            type = ParticleType.CHAOTIC,
            speed = FloatRange(3f, 8f),
            size = FloatRange(2f, 6f),
            intensityMultiplier = 1.8f
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2.5f,
            backgroundAnimationEnabled = true,
            transitionDuration = 120
        ),
        isDark = true,
        category = ThemeCategory2026.NEON
    )
    
    val NEON_LIME_FIZZ = CandyTheme(
        id = "neon_lime_fizz",
        name = "Neon Lime Fizz",
        description = "Radioactive sour glow",
        baseColors = BaseColors(
            primary = Color(0xFF39FF14),      // Neon Green
            secondary = Color(0xFFFFD700),    // Electric Yellow
            tertiary = Color(0xFF00FF7F),     // Spring Green
            accent = Color(0xFF7FFF00),       // Chartreuse
            surface = Color(0xFF0A1A00),
            surfaceVariant = Color(0xFF142900),
            background = Color(0xFF050D00),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE8FFD6),
            onBackground = Color(0xFFE8FFD6)
        ),
        intensityConfig = IntensityConfig(0.8f, 2f, 1.5f, 0.1f, "Fizz Level"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF050D00),
                Color(0xFF0A1A00),
                Color(0xFF39FF14).copy(alpha = 0.1f)
            ),
            intenseColors = listOf(
                Color(0xFF39FF14).copy(alpha = 0.4f),
                Color(0xFF0A1A00),
                Color(0xFFFFD700).copy(alpha = 0.3f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF39FF14),
                Color(0xFFFFD700),
                Color(0xFF00FF7F)
            ),
            count = 70..130,
            type = ParticleType.EXPLODING,
            speed = FloatRange(2f, 7f),
            size = FloatRange(1f, 5f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2.2f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.NEON
    )
    
    val NEON_GRAPE_BOMB = CandyTheme(
        id = "neon_grape_bomb",
        name = "Neon Grape Bomb",
        description = "Explosive purple energy",
        baseColors = BaseColors(
            primary = Color(0xFF8B00FF),      // Electric Violet
            secondary = Color(0xFFFF00FF),    // Magenta
            tertiary = Color(0xFF9400D3),     // Dark Violet
            accent = Color(0xFFE0B0FF),       // Mauve
            surface = Color(0xFF0D0014),
            surfaceVariant = Color(0xFF1A0029),
            background = Color(0xFF05000A),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFF0E0FF),
            onBackground = Color(0xFFF0E0FF)
        ),
        intensityConfig = IntensityConfig(0.8f, 2f, 1.6f, 0.1f, "Explosion"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFF8B00FF).copy(alpha = 0.2f),
                Color(0xFFFF00FF).copy(alpha = 0.15f),
                Color(0xFF0D0014)
            ),
            animationSpeed = 2f,
            complexity = 6
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF8B00FF),
                Color(0xFFFF00FF),
                Color(0xFFE0B0FF)
            ),
            count = 90..160,
            type = ParticleType.SWIRLING,
            speed = FloatRange(2f, 6f),
            size = FloatRange(2f, 7f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2.8f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.NEON
    )
    
    val NEON_ORANGE_SODA = CandyTheme(
        id = "neon_orange_soda",
        name = "Neon Orange Soda",
        description = "Tangy electric citrus",
        baseColors = BaseColors(
            primary = Color(0xFFFF6600),      // Neon Orange
            secondary = Color(0xFFFFAA00),    // Electric Amber
            tertiary = Color(0xFFFF4500),     // Orange Red
            accent = Color(0xFFFFD700),       // Gold
            surface = Color(0xFF1A0A00),
            surfaceVariant = Color(0xFF291400),
            background = Color(0xFF0D0500),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFFFE8D6),
            onBackground = Color(0xFFFFE8D6)
        ),
        intensityConfig = IntensityConfig(0.8f, 2f, 1.4f, 0.1f, "Fizz"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF0D0500),
                Color(0xFF1A0A00),
                Color(0xFFFF6600).copy(alpha = 0.15f)
            ),
            intenseColors = listOf(
                Color(0xFFFF6600).copy(alpha = 0.5f),
                Color(0xFF1A0A00),
                Color(0xFFFFAA00).copy(alpha = 0.3f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF6600),
                Color(0xFFFFAA00),
                Color(0xFFFFD700)
            ),
            count = 70..120,
            type = ParticleType.RISING,
            speed = FloatRange(1f, 5f),
            size = FloatRange(2f, 6f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.NEON
    )
    
    val NEON_ICE_POP = CandyTheme(
        id = "neon_ice_pop",
        name = "Neon Ice Pop",
        description = "Frozen electric rainbow",
        baseColors = BaseColors(
            primary = Color(0xFF00F5FF),      // Fluorescent Blue
            secondary = Color(0xFFFF1493),    // Deep Pink
            tertiary = Color(0xFF00FF00),     // Electric Lime
            accent = Color(0xFFFFD700),       // Gold
            surface = Color(0xFF000A14),
            surfaceVariant = Color(0xFF001429),
            background = Color(0xFF00050A),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE0F5FF),
            onBackground = Color(0xFFE0F5FF)
        ),
        intensityConfig = IntensityConfig(0.8f, 2f, 1.5f, 0.1f, "Freeze Level"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFF00F5FF).copy(alpha = 0.15f),
                Color(0xFFFF1493).copy(alpha = 0.12f),
                Color(0xFF00FF00).copy(alpha = 0.1f)
            ),
            animationSpeed = 1.5f,
            complexity = 7
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF00F5FF),
                Color(0xFFFF1493),
                Color(0xFF00FF00),
                Color(0xFFFFD700)
            ),
            count = 100..180,
            type = ParticleType.CHAOTIC,
            speed = FloatRange(3f, 10f),
            size = FloatRange(1f, 6f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 3f,
            backgroundAnimationEnabled = true,
            transitionDuration = 100
        ),
        isDark = true,
        category = ThemeCategory2026.NEON
    )
    
    // ═════════════════════════════════════════════════════════════════
    // GALACTIC SWEETS - Cosmic space candy
    // ═════════════════════════════════════════════════════════════════
    
    val GALACTIC_ROCK_CANDY = CandyTheme(
        id = "galactic_rock_candy",
        name = "Galactic Rock Candy",
        description = "Crystallized starlight",
        baseColors = BaseColors(
            primary = Color(0xFFB8E6FF),      // Starlight Blue
            secondary = Color(0xFFE0B0FF),    // Nebula Purple
            tertiary = Color(0xFFFFB6C1),     // Cosmic Pink
            accent = Color(0xFFFFFFE0),       // Moonlight
            surface = Color(0xFF0D0D1A),
            surfaceVariant = Color(0xFF1A1A2E),
            background = Color(0xFF050510),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE8E8FF),
            onBackground = Color(0xFFE8E8FF)
        ),
        intensityConfig = IntensityConfig(0.3f, 2f, 1f, 0.1f, "Cosmic Power"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFB8E6FF).copy(alpha = 0.1f),
                Color(0xFFE0B0FF).copy(alpha = 0.08f),
                Color(0xFF0D0D1A)
            ),
            animationSpeed = 0.4f,
            complexity = 8
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFB8E6FF),
                Color(0xFFE0B0FF)
            ),
            count = 60..120,
            type = ParticleType.SPARKLE,
            speed = FloatRange(0.2f, 1f),
            size = FloatRange(1f, 4f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.8f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.GALACTIC
    )
    
    val NEBULA_GUMMY = CandyTheme(
        id = "nebula_gummy",
        name = "Nebula Gummy",
        description = "Swirling cosmic clouds",
        baseColors = BaseColors(
            primary = Color(0xFF9932CC),      // Dark Orchid
            secondary = Color(0xFFFF69B4),    // Hot Pink
            tertiary = Color(0xFF4169E1),     // Royal Blue
            accent = Color(0xFFFFDAB9),       // Peach Puff
            surface = Color(0xFF0A0A1E),
            surfaceVariant = Color(0xFF141432),
            background = Color(0xFF050514),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFE8E8FF),
            onBackground = Color(0xFFE8E8FF)
        ),
        intensityConfig = IntensityConfig(0.5f, 2f, 1.2f, 0.1f, "Nebula Density"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFF9932CC).copy(alpha = 0.2f),
                Color(0xFFFF69B4).copy(alpha = 0.15f),
                Color(0xFF4169E1).copy(alpha = 0.12f)
            ),
            animationSpeed = 0.6f,
            complexity = 6
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF9932CC).copy(alpha = 0.8f),
                Color(0xFFFF69B4).copy(alpha = 0.7f),
                Color(0xFF4169E1).copy(alpha = 0.6f)
            ),
            count = 50..100,
            type = ParticleType.SWIRLING,
            speed = FloatRange(0.5f, 2f),
            size = FloatRange(3f, 10f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.GALACTIC
    )
    
    val ASTRONAUT_ICE_CREAM = CandyTheme(
        id = "astronaut_ice_cream",
        name = "Astronaut Ice Cream",
        description = "Freeze-dried cosmic treat",
        baseColors = BaseColors(
            primary = Color(0xFFFFF8DC),      // Cornsilk
            secondary = Color(0xFFFFC0CB),    // Pink
            tertiary = Color(0xFFE6E6FA),     // Lavender
            accent = Color(0xFFF0E68C),       // Khaki
            surface = Color(0xFF1A1A2E),
            surfaceVariant = Color(0xFF2A2A3E),
            background = Color(0xFF0D0D1A),
            onPrimary = Color(0xFF1A1A2E),
            onSurface = Color(0xFFF5F5F5),
            onBackground = Color(0xFFF5F5F5)
        ),
        intensityConfig = IntensityConfig(0.3f, 1.8f, 0.8f, 0.1f, "Freeze Factor"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF0D0D1A),
                Color(0xFF1A1A2E),
                Color(0xFF2A2A3E)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFF8DC).copy(alpha = 0.6f),
                Color(0xFFFFC0CB).copy(alpha = 0.5f),
                Color(0xFFE6E6FA).copy(alpha = 0.5f)
            ),
            count = 20..50,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.1f, 0.5f),
            size = FloatRange(4f, 12f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.5f,
            backgroundAnimationEnabled = false
        ),
        isDark = true,
        category = ThemeCategory2026.GALACTIC
    )
    
    val SATURN_RINGS_CANDY = CandyTheme(
        id = "saturn_rings_candy",
        name = "Saturn Rings Candy",
        description = "Planetary caramel swirls",
        baseColors = BaseColors(
            primary = Color(0xFFDAA520),      // Goldenrod
            secondary = Color(0xFFF4A460),    // Sandy Brown
            tertiary = Color(0xFFDEB887),     // Burlywood
            accent = Color(0xFFFFD700),       // Gold
            surface = Color(0xFF1A1410),
            surfaceVariant = Color(0xFF2A2018),
            background = Color(0xFF0D0A08),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFF5E6DC),
            onBackground = Color(0xFFF5E6DC)
        ),
        intensityConfig = IntensityConfig(0.4f, 2f, 1f, 0.1f, "Ring Density"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFDAA520).copy(alpha = 0.12f),
                Color(0xFFF4A460).copy(alpha = 0.1f),
                Color(0xFF1A1410)
            ),
            animationSpeed = 0.3f,
            complexity = 4
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFDAA520),
                Color(0xFFF4A460),
                Color(0xFFFFD700)
            ),
            count = 40..80,
            type = ParticleType.SWIRLING,
            speed = FloatRange(0.5f, 2f),
            size = FloatRange(2f, 8f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.9f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.GALACTIC
    )
    
    val BLACK_HOLE_LICORICE = CandyTheme(
        id = "black_hole_licorice",
        name = "Black Hole Licorice",
        description = "Dark matter candy",
        baseColors = BaseColors(
            primary = Color(0xFF4B0082),      // Indigo
            secondary = Color(0xFF800080),    // Purple
            tertiary = Color(0xFF191970),     // Midnight Blue
            accent = Color(0xFF00FFFF),       // Cyan (event horizon glow)
            surface = Color(0xFF050505),
            surfaceVariant = Color(0xFF0A0A0A),
            background = Color(0xFF000000),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFE0E0E0),
            onBackground = Color(0xFFE0E0E0)
        ),
        intensityConfig = IntensityConfig(0.5f, 2f, 1.3f, 0.1f, "Gravity"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF000000),
                Color(0xFF050505),
                Color(0xFF0A0A0A)
            ),
            intenseColors = listOf(
                Color(0xFF4B0082).copy(alpha = 0.3f),
                Color(0xFF000000),
                Color(0xFF00FFFF).copy(alpha = 0.15f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF4B0082),
                Color(0xFF800080),
                Color(0xFF00FFFF)
            ),
            count = 80..160,
            type = ParticleType.EXPLODING,
            speed = FloatRange(3f, 10f),
            size = FloatRange(1f, 5f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 3f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.GALACTIC
    )
    
    // ═════════════════════════════════════════════════════════════════
    // RETRO WAVE - 80s/90s nostalgia
    // ═════════════════════════════════════════════════════════════════
    
    val RETRO_ARCADE_CANDY = CandyTheme(
        id = "retro_arcade_candy",
        name = "Retro Arcade",
        description = "8-bit sugar rush",
        baseColors = BaseColors(
            primary = Color(0xFFFF00FF),      // Magenta
            secondary = Color(0xFF00FFFF),    // Cyan
            tertiary = Color(0xFFFFFF00),     // Yellow
            accent = Color(0xFF00FF00),       // Lime
            surface = Color(0xFF0D0D0D),
            surfaceVariant = Color(0xFF1A1A1A),
            background = Color(0xFF050505),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE0FFE0),
            onBackground = Color(0xFFE0FFE0)
        ),
        intensityConfig = IntensityConfig(0.8f, 2f, 1.5f, 0.1f, "Retro Level"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF050505),
                Color(0xFF1A0A1A),
                Color(0xFF0A1A1A)
            ),
            intenseColors = listOf(
                Color(0xFFFF00FF).copy(alpha = 0.2f),
                Color(0xFF00FFFF).copy(alpha = 0.15f),
                Color(0xFF050505)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF00FF),
                Color(0xFF00FFFF),
                Color(0xFFFFFF00),
                Color(0xFF00FF00)
            ),
            count = 80..140,
            type = ParticleType.CHAOTIC,
            speed = FloatRange(2f, 6f),
            size = FloatRange(4f, 12f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2f,
            backgroundAnimationEnabled = true,
            transitionDuration = 150
        ),
        isDark = true,
        category = ThemeCategory2026.RETRO
    )
    
    val VHS_CARAMEL = CandyTheme(
        id = "vhs_caramel",
        name = "VHS Caramel",
        description = "Tracking error sweetness",
        baseColors = BaseColors(
            primary = Color(0xFFCD853F),      // Peru
            secondary = Color(0xFFD2691E),    // Chocolate
            tertiary = Color(0xFF8B4513),     // Saddle Brown
            accent = Color(0xFFFFA500),       // Orange
            surface = Color(0xFF1A1410),
            surfaceVariant = Color(0xFF2A2018),
            background = Color(0xFF0D0A08),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFF5E6DC),
            onBackground = Color(0xFFF5E6DC)
        ),
        intensityConfig = IntensityConfig(0.3f, 2f, 1f, 0.1f, "Tracking"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF0D0A08),
                Color(0xFF1A1410),
                Color(0xFF2A2018)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFCD853F),
                Color(0xFFD2691E),
                Color(0xFFFFA500)
            ),
            count = 30..60,
            type = ParticleType.RAINING,
            speed = FloatRange(1f, 3f),
            size = FloatRange(2f, 6f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 1f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.RETRO
    )
    
    val DISCO_BALL_GUM = CandyTheme(
        id = "disco_ball_gum",
        name = "Disco Ball Gum",
        description = "Mirrorball sparkle",
        baseColors = BaseColors(
            primary = Color(0xFFC0C0C0),      // Silver
            secondary = Color(0xFFFFD700),    // Gold
            tertiary = Color(0xFFFF69B4),     // Hot Pink
            accent = Color(0xFF00CED1),       // Dark Turquoise
            surface = Color(0xFF0D0D0D),
            surfaceVariant = Color(0xFF1A1A1A),
            background = Color(0xFF050505),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFF0F0F0),
            onBackground = Color(0xFFF0F0F0)
        ),
        intensityConfig = IntensityConfig(0.5f, 2f, 1.4f, 0.1f, "Disco Fever"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFC0C0C0).copy(alpha = 0.1f),
                Color(0xFFFFD700).copy(alpha = 0.1f),
                Color(0xFF0D0D0D)
            ),
            animationSpeed = 1.5f,
            complexity = 5
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFFFD700),
                Color(0xFFFF69B4),
                Color(0xFF00CED1)
            ),
            count = 100..200,
            type = ParticleType.SPARKLE,
            speed = FloatRange(2f, 8f),
            size = FloatRange(1f, 4f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 3f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.RETRO
    )
    
    val MIXTAPE_ROLL = CandyTheme(
        id = "mixtape_roll",
        name = "Mixtape Roll",
        description = "Sweet analog vibes",
        baseColors = BaseColors(
            primary = Color(0xFF8B0000),      // Dark Red
            secondary = Color(0xFF2F4F4F),    // Dark Slate Gray
            tertiary = Color(0xFFDAA520),     // Goldenrod
            accent = Color(0xFFFF6347),       // Tomato
            surface = Color(0xFF141414),
            surfaceVariant = Color(0xFF1F1F1F),
            background = Color(0xFF0A0A0A),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFFE0E0E0),
            onBackground = Color(0xFFE0E0E0)
        ),
        intensityConfig = IntensityConfig(0.3f, 2f, 0.9f, 0.1f, "Analog"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF0A0A0A),
                Color(0xFF141414),
                Color(0xFF1F1F1F)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = false,
            colors = emptyList()
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.5f,
            backgroundAnimationEnabled = false
        ),
        isDark = true,
        category = ThemeCategory2026.RETRO
    )
    
    val POLAROID_MINT = CandyTheme(
        id = "polaroid_mint",
        name = "Polaroid Mint",
        description = "Instant photo freshness",
        baseColors = BaseColors(
            primary = Color(0xFF98FF98),      // Mint
            secondary = Color(0xFFFFF0F5),    // Lavender Blush
            tertiary = Color(0xFFF0E68C),     // Khaki
            accent = Color(0xFFFFB6C1),       // Light Pink
            surface = Color(0xFFFAFAFA),
            surfaceVariant = Color(0xFFF5F5F5),
            background = Color(0xFFFFFFFF),
            onPrimary = Color(0xFF1A1A1A),
            onSurface = Color(0xFF2D2D2D),
            onBackground = Color(0xFF2D2D2D)
        ),
        intensityConfig = IntensityConfig(0f, 1.5f, 0.7f, 0.1f, "Exposure"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF5F5F5),
                Color(0xFFFAFAFA)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF98FF98).copy(alpha = 0.6f),
                Color(0xFFFFF0F5).copy(alpha = 0.5f)
            ),
            count = 15..30,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.2f, 0.6f),
            size = FloatRange(6f, 14f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.5f,
            backgroundAnimationEnabled = false
        ),
        isDark = false,
        category = ThemeCategory2026.RETRO
    )
    
    // ═════════════════════════════════════════════════════════════════
    // FRUIT BLAST - Tropical and fresh
    // ═════════════════════════════════════════════════════════════════
    
    val MANGO_TANGO = CandyTheme(
        id = "mango_tango",
        name = "Mango Tango",
        description = "Sunset tropical sweetness",
        baseColors = BaseColors(
            primary = Color(0xFFFF8243),      // Mango Orange
            secondary = Color(0xFFFFD700),    // Gold
            tertiary = Color(0xFFFF6347),     // Tomato
            accent = Color(0xFFFFA500),       // Orange
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFFFEDE0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFF2D1B00),
            onBackground = Color(0xFF2D1B00)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.1f, 0.1f, "Tropical"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFFBF7),
                Color(0xFFFFE4C4).copy(alpha = 0.4f),
                Color(0xFFFFDAB9).copy(alpha = 0.3f)
            ),
            intenseColors = listOf(
                Color(0xFFFF8243).copy(alpha = 0.3f),
                Color(0xFFFFD700).copy(alpha = 0.2f),
                Color(0xFFFFFBF7)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF8243),
                Color(0xFFFFD700),
                Color(0xFFFF6347)
            ),
            count = 40..80,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.5f, 2f),
            size = FloatRange(3f, 10f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.2f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory2026.FRUIT
    )
    
    val DRAGON_FRUIT_FANTASY = CandyTheme(
        id = "dragon_fruit_fantasy",
        name = "Dragon Fruit Fantasy",
        description = "Exotic pink paradise",
        baseColors = BaseColors(
            primary = Color(0xFFFF1493),      // Deep Pink
            secondary = Color(0xFF00FF7F),    // Spring Green
            tertiary = Color(0xFFFF69B4),     // Hot Pink
            accent = Color(0xFF98FF98),       // Mint
            surface = Color(0xFFFFF0F5),
            surfaceVariant = Color(0xFFFFE8F0),
            background = Color(0xFFFFFAFD),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF2D1B2E),
            onBackground = Color(0xFF2D1B2E)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.2f, 0.1f, "Exotic"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFF1493).copy(alpha = 0.1f),
                Color(0xFF00FF7F).copy(alpha = 0.08f),
                Color(0xFFFFF0F5)
            ),
            animationSpeed = 0.7f,
            complexity = 4
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF1493),
                Color(0xFF00FF7F),
                Color(0xFFFF69B4)
            ),
            count = 50..100,
            type = ParticleType.SWIRLING,
            speed = FloatRange(0.8f, 2.5f),
            size = FloatRange(2f, 8f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.5f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory2026.FRUIT
    )
    
    val PASSIONFRUIT_PUNCH = CandyTheme(
        id = "passionfruit_punch",
        name = "Passionfruit Punch",
        description = "Tropical energy boost",
        baseColors = BaseColors(
            primary = Color(0xFFFFA500),      // Orange
            secondary = Color(0xFF800080),    // Purple
            tertiary = Color(0xFFFFFF00),     // Yellow
            accent = Color(0xFFFF4500),       // Orange Red
            surface = Color(0xFFFAF0E6),
            surfaceVariant = Color(0xFFF5E6DC),
            background = Color(0xFFFDF5E6),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFF2D1B00),
            onBackground = Color(0xFF2D1B00)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.3f, 0.1f, "Punch Level"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFDF5E6),
                Color(0xFFFAF0E6),
                Color(0xFFFFE4B5).copy(alpha = 0.3f)
            ),
            intenseColors = listOf(
                Color(0xFFFFA500).copy(alpha = 0.3f),
                Color(0xFF800080).copy(alpha = 0.2f),
                Color(0xFFFDF5E6)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFA500),
                Color(0xFF800080),
                Color(0xFFFFFF00)
            ),
            count = 60..120,
            type = ParticleType.EXPLODING,
            speed = FloatRange(2f, 5f),
            size = FloatRange(2f, 7f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.8f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory2026.FRUIT
    )
    
    val KIWI_BURST = CandyTheme(
        id = "kiwi_burst",
        name = "Kiwi Burst",
        description = "Fresh green explosion",
        baseColors = BaseColors(
            primary = Color(0xFF8FBC8F),      // Dark Sea Green
            secondary = Color(0xFF9ACD32),    // Yellow Green
            tertiary = Color(0xFFDAA520),     // Goldenrod
            accent = Color(0xFF556B2F),       // Dark Olive Green
            surface = Color(0xFFF0FFF0),
            surfaceVariant = Color(0xFFE8F5E8),
            background = Color(0xFFF8FFF8),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFF1A2E1A),
            onBackground = Color(0xFF1A2E1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1f, 0.1f, "Freshness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFF8FFF8),
                Color(0xFFF0FFF0),
                Color(0xFFE8F5E8)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF8FBC8F),
                Color(0xFF9ACD32),
                Color(0xFFDAA520)
            ),
            count = 30..60,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.3f, 1.2f),
            size = FloatRange(4f, 10f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.8f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory2026.FRUIT
    )
    
    val COCONTY_COLADA = CandyTheme(
        id = "coconut_colada",
        name = "Coconut Colada",
        description = "Beach vacation vibes",
        baseColors = BaseColors(
            primary = Color(0xFFFFF8DC),      // Cornsilk
            secondary = Color(0xFF87CEEB),    // Sky Blue
            tertiary = Color(0xFFF5DEB3),     // Wheat
            accent = Color(0xFFFFFACD),       // Lemon Chiffon
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFFAFAFA),
            background = Color(0xFFFDFDFD),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFF2D2D2D),
            onBackground = Color(0xFF2D2D2D)
        ),
        intensityConfig = IntensityConfig(0f, 1.5f, 0.6f, 0.1f, "Beach Vibes"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFDFDFD),
                Color(0xFFF0F8FF).copy(alpha = 0.5f),
                Color(0xFFFFFFE0).copy(alpha = 0.3f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFF8DC).copy(alpha = 0.7f),
                Color(0xFF87CEEB).copy(alpha = 0.5f)
            ),
            count = 20..40,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.1f, 0.5f),
            size = FloatRange(6f, 14f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.4f,
            backgroundAnimationEnabled = false
        ),
        isDark = false,
        category = ThemeCategory2026.FRUIT
    )
    
    // ═════════════════════════════════════════════════════════════════
    // GOURMET - Sophisticated dessert vibes
    // ═════════════════════════════════════════════════════════════════
    
    val MACARON_PARIS = CandyTheme(
        id = "macaron_paris",
        name = "Macaron Paris",
        description = "French patisserie elegance",
        baseColors = BaseColors(
            primary = Color(0xFFF8C8DC),      // Pastel Pink
            secondary = Color(0xFFE6E6FA),    // Lavender
            tertiary = Color(0xFFFFF0F5),    // Lavender Blush
            accent = Color(0xFFF5F5DC),       // Beige
            surface = Color(0xFFFAFAFA),
            surfaceVariant = Color(0xFFF5F5F5),
            background = Color(0xFFFDFDFD),
            onPrimary = Color(0xFF2D2D2D),
            onSurface = Color(0xFF2D2D2D),
            onBackground = Color(0xFF2D2D2D)
        ),
        intensityConfig = IntensityConfig(0f, 1.5f, 0.7f, 0.1f, "Elegance"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFDFDFD),
                Color(0xFFF5F5F5),
                Color(0xFFF0F0F0)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFF8C8DC).copy(alpha = 0.5f),
                Color(0xFFE6E6FA).copy(alpha = 0.5f),
                Color(0xFFFFF0F5).copy(alpha = 0.5f)
            ),
            count = 15..30,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.1f, 0.4f),
            size = FloatRange(6f, 16f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.5f,
            backgroundAnimationEnabled = false
        ),
        isDark = false,
        category = ThemeCategory2026.GOURMET
    )
    
    val TIRAMISU_DREAM = CandyTheme(
        id = "tiramisu_dream",
        name = "Tiramisu Dream",
        description = "Italian coffee elegance",
        baseColors = BaseColors(
            primary = Color(0xFFD2B48C),      // Tan
            secondary = Color(0xFF8B4513),    // Saddle Brown
            tertiary = Color(0xFFF5DEB3),     // Wheat
            accent = Color(0xFFFFF8DC),       // Cornsilk
            surface = Color(0xFF2A2018),
            surfaceVariant = Color(0xFF3D2C24),
            background = Color(0xFF1A1410),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFF5E6DC),
            onBackground = Color(0xFFF5E6DC)
        ),
        intensityConfig = IntensityConfig(0.2f, 1.8f, 0.8f, 0.1f, "Richness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF1A1410),
                Color(0xFF2A2018),
                Color(0xFF1A1410)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFD2B48C).copy(alpha = 0.4f),
                Color(0xFF8B4513).copy(alpha = 0.3f)
            ),
            count = 20..40,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.15f, 0.5f),
            size = FloatRange(4f, 10f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.5f,
            backgroundAnimationEnabled = false
        ),
        isDark = true,
        category = ThemeCategory2026.GOURMET
    )
    
    val MATCHA_CEREMONY = CandyTheme(
        id = "matcha_ceremony",
        name = "Matcha Ceremony",
        description = "Japanese green tea zen",
        baseColors = BaseColors(
            primary = Color(0xFF6B8E23),      // Olive Drab
            secondary = Color(0xFF9ACD32),    // Yellow Green
            tertiary = Color(0xFFDDA0DD),     // Plum
            accent = Color(0xFFF5F5DC),       // Beige
            surface = Color(0xFFF5F5DC),
            surfaceVariant = Color(0xFFEBEBC0),
            background = Color(0xFFFAFAF0),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A2E1A),
            onBackground = Color(0xFF1A2E1A)
        ),
        intensityConfig = IntensityConfig(0f, 1.5f, 0.6f, 0.1f, "Zen Level"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFAFAF0),
                Color(0xFFF0F0E0),
                Color(0xFFE8E8D8)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF6B8E23).copy(alpha = 0.4f),
                Color(0xFF9ACD32).copy(alpha = 0.3f)
            ),
            count = 10..25,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.05f, 0.3f),
            size = FloatRange(8f, 18f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.3f,
            backgroundAnimationEnabled = false
        ),
        isDark = false,
        category = ThemeCategory2026.GOURMET
    )
    
    val GOLD_LEAF_TRUFFLE = CandyTheme(
        id = "gold_leaf_truffle",
        name = "Gold Leaf Truffle",
        description = "Luxury chocolate experience",
        baseColors = BaseColors(
            primary = Color(0xFFFFD700),      // Gold
            secondary = Color(0xFFD2691E),    // Chocolate
            tertiary = Color(0xFF8B4513),     // Saddle Brown
            accent = Color(0xFFFFFF00),       // Yellow
            surface = Color(0xFF1A1410),
            surfaceVariant = Color(0xFF2A2018),
            background = Color(0xFF0D0A08),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFF5E6DC),
            onBackground = Color(0xFFF5E6DC)
        ),
        intensityConfig = IntensityConfig(0.4f, 2f, 1.2f, 0.1f, "Luxury"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFFD700).copy(alpha = 0.08f),
                Color(0xFFD2691E).copy(alpha = 0.1f),
                Color(0xFF1A1410)
            ),
            animationSpeed = 0.5f,
            complexity = 4
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFFF00),
                Color(0xFFD2691E)
            ),
            count = 60..120,
            type = ParticleType.SPARKLE,
            speed = FloatRange(0.5f, 2f),
            size = FloatRange(1f, 4f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.2f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.GOURMET
    )
    
    val LAVENDER_HONEY = CandyTheme(
        id = "lavender_honey",
        name = "Lavender Honey",
        description = "Provençal sweetness",
        baseColors = BaseColors(
            primary = Color(0xFFE6E6FA),      // Lavender
            secondary = Color(0xFFDDA0DD),    // Plum
            tertiary = Color(0xFFF0E68C),     // Khaki
            accent = Color(0xFFFFF0F5),       // Lavender Blush
            surface = Color(0xFFFAFAFF),
            surfaceVariant = Color(0xFFF0F0F5),
            background = Color(0xFFFDFDFF),
            onPrimary = Color(0xFF2D2D2D),
            onSurface = Color(0xFF2D2D2D),
            onBackground = Color(0xFF2D2D2D)
        ),
        intensityConfig = IntensityConfig(0f, 1.5f, 0.7f, 0.1f, "Sweetness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFDFDFF),
                Color(0xFFF5F5FA),
                Color(0xFFE6E6FA).copy(alpha = 0.2f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFE6E6FA).copy(alpha = 0.6f),
                Color(0xFFF0E68C).copy(alpha = 0.5f)
            ),
            count = 20..40,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.2f, 0.6f),
            size = FloatRange(5f, 12f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.5f,
            backgroundAnimationEnabled = false
        ),
        isDark = false,
        category = ThemeCategory2026.GOURMET
    )
    
    // ═════════════════════════════════════════════════════════════════
    // KAWAII - Cute Japanese-inspired
    // ═════════════════════════════════════════════════════════════════
    
    val SAKURA_BLOSSOM = CandyTheme(
        id = "sakura_blossom",
        name = "Sakura Blossom",
        description = "Cherry blossom dreams",
        baseColors = BaseColors(
            primary = Color(0xFFFFC0CB),      // Pink
            secondary = Color(0xFFFFE4E1),    // Misty Rose
            tertiary = Color(0xFFFFF0F5),     // Lavender Blush
            accent = Color(0xFFFFB7C5),       // Cherry Blossom
            surface = Color(0xFFFFF5F7),
            surfaceVariant = Color(0xFFFFEBEF),
            background = Color(0xFFFFFAFB),
            onPrimary = Color(0xFF2D1B1F),
            onSurface = Color(0xFF2D1B1F),
            onBackground = Color(0xFF2D1B1F)
        ),
        intensityConfig = IntensityConfig(0f, 1.8f, 0.8f, 0.1f, "Kawaii"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFFC0CB).copy(alpha = 0.15f),
                Color(0xFFFFE4E1).copy(alpha = 0.1f),
                Color(0xFFFFF5F7)
            ),
            animationSpeed = 0.5f,
            complexity = 4
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFC0CB),
                Color(0xFFFFE4E1),
                Color(0xFFFFB7C5)
            ),
            count = 40..80,
            type = ParticleType.RAINING,
            speed = FloatRange(0.5f, 1.5f),
            size = FloatRange(3f, 8f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.9f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory2026.KAWAII
    )
    
    val KAWAII_UNICORN = CandyTheme(
        id = "kawaii_unicorn",
        name = "Kawaii Unicorn",
        description = "Magical rainbow cuteness",
        baseColors = BaseColors(
            primary = Color(0xFFFFB6C1),      // Light Pink
            secondary = Color(0xFFB5DEFF),    // Light Blue
            tertiary = Color(0xFFFFFACD),     // Lemon Chiffon
            accent = Color(0xFFE6E6FA),       // Lavender
            surface = Color(0xFFFAFAFF),
            surfaceVariant = Color(0xFFF5F5FF),
            background = Color(0xFFFDFDFF),
            onPrimary = Color(0xFF2D2D2D),
            onSurface = Color(0xFF2D2D2D),
            onBackground = Color(0xFF2D2D2D)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.2f, 0.1f, "Magic"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFFB6C1).copy(alpha = 0.12f),
                Color(0xFFB5DEFF).copy(alpha = 0.12f),
                Color(0xFFFFFACD).copy(alpha = 0.1f)
            ),
            animationSpeed = 0.8f,
            complexity = 5
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFB6C1),
                Color(0xFFB5DEFF),
                Color(0xFFFFFACD),
                Color(0xFFE6E6FA)
            ),
            count = 60..120,
            type = ParticleType.SWIRLING,
            speed = FloatRange(1f, 3f),
            size = FloatRange(3f, 10f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.5f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory2026.KAWAII
    )
    
    val HAMSTER_CHEEK = CandyTheme(
        id = "hamster_cheek",
        name = "Hamster Cheek",
        description = "Chubby cheek energy",
        baseColors = BaseColors(
            primary = Color(0xFFF4A460),      // Sandy Brown
            secondary = Color(0xFFD2691E),    // Chocolate
            tertiary = Color(0xFFF5DEB3),     // Wheat
            accent = Color(0xFFFFE4B5),       // Moccasin
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFFFEEE0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFF2D1B0A),
            onBackground = Color(0xFF2D1B0A)
        ),
        intensityConfig = IntensityConfig(0f, 1.8f, 0.9f, 0.1f, "Cheeks"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFFBF7),
                Color(0xFFFFF0E0),
                Color(0xFFFFE8D0)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFF4A460).copy(alpha = 0.6f),
                Color(0xFFF5DEB3).copy(alpha = 0.5f)
            ),
            count = 25..50,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.2f, 0.7f),
            size = FloatRange(5f, 14f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.8f,
            backgroundAnimationEnabled = false
        ),
        isDark = false,
        category = ThemeCategory2026.KAWAII
    )
    
    val BUBBLE_TEA_LOVE = CandyTheme(
        id = "bubble_tea_love",
        name = "Bubble Tea Love",
        description = "Milk tea aesthetic",
        baseColors = BaseColors(
            primary = Color(0xFFD2B48C),      // Tan
            secondary = Color(0xFF8B4513),    // Saddle Brown
            tertiary = Color(0xFFFFF8DC),     // Cornsilk
            accent = Color(0xFFDEB887),       // Burlywood
            surface = Color(0xFFFAF5F0),
            surfaceVariant = Color(0xFFF0E8E0),
            background = Color(0xFFFDF9F5),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFF2D2010),
            onBackground = Color(0xFF2D2010)
        ),
        intensityConfig = IntensityConfig(0f, 1.8f, 0.8f, 0.1f, "Boba Level"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFDF9F5),
                Color(0xFFF5EDE5),
                Color(0xFFEDE5DD)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFD2B48C).copy(alpha = 0.6f),
                Color(0xFF8B4513).copy(alpha = 0.5f)
            ),
            count = 30..60,
            type = ParticleType.BUBBLES,
            speed = FloatRange(0.3f, 0.8f),
            size = FloatRange(8f, 16f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.8f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory2026.KAWAII
    )
    
    val PUDDING_PUPPY = CandyTheme(
        id = "pudding_puppy",
        name = "Pudding Puppy",
        description = "Caramel custard cuteness",
        baseColors = BaseColors(
            primary = Color(0xFFE6C229),      // Caramel Gold
            secondary = Color(0xFFD2691E),    // Chocolate
            tertiary = Color(0xFFFFF8DC),     // Cornsilk
            accent = Color(0xFFF4A460),       // Sandy Brown
            surface = Color(0xFFFAF5E6),
            surfaceVariant = Color(0xFFF5EDD6),
            background = Color(0xFFFDF8EE),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFF2D2010),
            onBackground = Color(0xFF2D2010)
        ),
        intensityConfig = IntensityConfig(0f, 1.8f, 0.9f, 0.1f, "Wobble"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFE6C229).copy(alpha = 0.1f),
                Color(0xFFF5EDD6),
                Color(0xFFFDF8EE)
            ),
            animationSpeed = 0.4f,
            complexity = 3
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFE6C229),
                Color(0xFFF4A460),
                Color(0xFFFFF8DC)
            ),
            count = 35..70,
            type = ParticleType.FLOATING,
            speed = FloatRange(0.2f, 0.6f),
            size = FloatRange(4f, 12f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.7f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory2026.KAWAII
    )
    
    // ═════════════════════════════════════════════════════════════════
    // HOLOGRAPHIC - Iridescent dreamy themes
    // ═════════════════════════════════════════════════════════════════
    
    val HOLOGRAPHIC_PRISM = CandyTheme(
        id = "holographic_prism",
        name = "Holographic Prism",
        description = "Rainbow light refraction",
        baseColors = BaseColors(
            primary = Color(0xFFE0B0FF),      // Mauve
            secondary = Color(0xFFB0E0E6),    // Powder Blue
            tertiary = Color(0xFFFFB6C1),     // Light Pink
            accent = Color(0xFFFFFFE0),       // Light Yellow
            surface = Color(0xFF0D0D14),
            surfaceVariant = Color(0xFF1A1A24),
            background = Color(0xFF05050A),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE8E8F0),
            onBackground = Color(0xFFE8E8F0)
        ),
        intensityConfig = IntensityConfig(0.5f, 2f, 1.4f, 0.1f, "Refraction"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFE0B0FF).copy(alpha = 0.15f),
                Color(0xFFB0E0E6).copy(alpha = 0.12f),
                Color(0xFFFFB6C1).copy(alpha = 0.1f)
            ),
            animationSpeed = 1.2f,
            complexity = 7
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFE0B0FF),
                Color(0xFFB0E0E6),
                Color(0xFFFFB6C1),
                Color(0xFFFFFFE0)
            ),
            count = 80..160,
            type = ParticleType.SPARKLE,
            speed = FloatRange(1f, 4f),
            size = FloatRange(1f, 6f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2.5f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.HOLOGRAPHIC
    )
    
    val OPAL_DREAM = CandyTheme(
        id = "opal_dream",
        name = "Opal Dream",
        description = "Precious stone iridescence",
        baseColors = BaseColors(
            primary = Color(0xFFA8E6CF),      // Sea Foam
            secondary = Color(0xFFDCEDC1),    // Pale Green
            tertiary = Color(0xFFFFD3B6),     // Peach
            accent = Color(0xFFFFAAA5),       // Salmon
            surface = Color(0xFF0D1410),
            surfaceVariant = Color(0xFF1A241C),
            background = Color(0xFF050A08),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE8F0EC),
            onBackground = Color(0xFFE8F0EC)
        ),
        intensityConfig = IntensityConfig(0.4f, 2f, 1.2f, 0.1f, "Iridescence"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFA8E6CF).copy(alpha = 0.12f),
                Color(0xFFDCEDC1).copy(alpha = 0.1f),
                Color(0xFFFFD3B6).copy(alpha = 0.1f)
            ),
            animationSpeed = 0.8f,
            complexity = 6
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFA8E6CF),
                Color(0xFFDCEDC1),
                Color(0xFFFFD3B6),
                Color(0xFFFFAAA5)
            ),
            count = 60..120,
            type = ParticleType.SWIRLING,
            speed = FloatRange(0.8f, 2.5f),
            size = FloatRange(2f, 8f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.8f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.HOLOGRAPHIC
    )
    
    val PEARL_SHIMMER = CandyTheme(
        id = "pearl_shimmer",
        name = "Pearl Shimmer",
        description = "Elegant lustrous glow",
        baseColors = BaseColors(
            primary = Color(0xFFF0F8FF),      // Alice Blue
            secondary = Color(0xFFF5F5F5),    // White Smoke
            tertiary = Color(0xFFFFF0F5),     // Lavender Blush
            accent = Color(0xFFF0FFF0),       // Honeydew
            surface = Color(0xFF0D0D0D),
            surfaceVariant = Color(0xFF1A1A1A),
            background = Color(0xFF050505),
            onPrimary = Color(0xFF2D2D2D),
            onSurface = Color(0xFFF0F0F0),
            onBackground = Color(0xFFF0F0F0)
        ),
        intensityConfig = IntensityConfig(0.3f, 2f, 1f, 0.1f, "Luster"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFF0F8FF).copy(alpha = 0.08f),
                Color(0xFFFFF0F5).copy(alpha = 0.06f),
                Color(0xFF0D0D0D)
            ),
            animationSpeed = 0.5f,
            complexity = 5
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFF0F8FF),
                Color(0xFFFFFFFF),
                Color(0xFFFFF0F5)
            ),
            count = 70..140,
            type = ParticleType.SPARKLE,
            speed = FloatRange(0.5f, 2f),
            size = FloatRange(1f, 4f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.5f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.HOLOGRAPHIC
    )
    
    val AURORA_BOREALIS = CandyTheme(
        id = "aurora_borealis",
        name = "Aurora Borealis",
        description = "Northern lights magic",
        baseColors = BaseColors(
            primary = Color(0xFF00FF7F),      // Spring Green
            secondary = Color(0xFF00CED1),    // Dark Turquoise
            tertiary = Color(0xFF9932CC),     // Dark Orchid
            accent = Color(0xFFFF69B4),       // Hot Pink
            surface = Color(0xFF0A0A14),
            surfaceVariant = Color(0xFF141428),
            background = Color(0xFF05050A),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE8E8F0),
            onBackground = Color(0xFFE8E8F0)
        ),
        intensityConfig = IntensityConfig(0.6f, 2f, 1.5f, 0.1f, "Aurora"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFF00FF7F).copy(alpha = 0.12f),
                Color(0xFF00CED1).copy(alpha = 0.1f),
                Color(0xFF9932CC).copy(alpha = 0.1f)
            ),
            animationSpeed = 1f,
            complexity = 8
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF00FF7F),
                Color(0xFF00CED1),
                Color(0xFF9932CC),
                Color(0xFFFF69B4)
            ),
            count = 100..200,
            type = ParticleType.SWIRLING,
            speed = FloatRange(1f, 4f),
            size = FloatRange(2f, 8f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.HOLOGRAPHIC
    )
    
    // ═════════════════════════════════════════════════════════════════
    // METALLIC CANDY - Shiny metallic treats
    // ═════════════════════════════════════════════════════════════════
    
    val CHROME_WRAPPER = CandyTheme(
        id = "chrome_wrapper",
        name = "Chrome Wrapper",
        description = "Futuristic silver shine",
        baseColors = BaseColors(
            primary = Color(0xFFC0C0C0),      // Silver
            secondary = Color(0xFFA9A9A9),    // Dark Gray
            tertiary = Color(0xFFD3D3D3),     // Light Gray
            accent = Color(0xFFFFFFFF),       // White
            surface = Color(0xFF0D0D0D),
            surfaceVariant = Color(0xFF1A1A1A),
            background = Color(0xFF050505),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFF0F0F0),
            onBackground = Color(0xFFF0F0F0)
        ),
        intensityConfig = IntensityConfig(0.4f, 2f, 1.2f, 0.1f, "Shine"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF050505),
                Color(0xFF0D0D0D),
                Color(0xFF1A1A1A)
            ),
            intenseColors = listOf(
                Color(0xFFC0C0C0).copy(alpha = 0.2f),
                Color(0xFF050505),
                Color(0xFFC0C0C0).copy(alpha = 0.15f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFC0C0C0),
                Color(0xFFFFFFFF),
                Color(0xFFD3D3D3)
            ),
            count = 50..100,
            type = ParticleType.SPARKLE,
            speed = FloatRange(1f, 4f),
            size = FloatRange(1f, 5f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.METALLIC
    )
    
    val ROSE_GOLD_FOIL = CandyTheme(
        id = "rose_gold_foil",
        name = "Rose Gold Foil",
        description = "Luxurious pink metal",
        baseColors = BaseColors(
            primary = Color(0xFFB76E79),      // Rose Gold
            secondary = Color(0xFFE6A8B3),    // Light Rose
            tertiary = Color(0xFFD4AF37),     // Gold
            accent = Color(0xFFF4C2C2),       // Tea Rose
            surface = Color(0xFF1A0D0D),
            surfaceVariant = Color(0xFF2A1818),
            background = Color(0xFF0D0808),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFF5E8E8),
            onBackground = Color(0xFFF5E8E8)
        ),
        intensityConfig = IntensityConfig(0.4f, 2f, 1.3f, 0.1f, "Luxury"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFB76E79).copy(alpha = 0.12f),
                Color(0xFFD4AF37).copy(alpha = 0.08f),
                Color(0xFF1A0D0D)
            ),
            animationSpeed = 0.6f,
            complexity = 5
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFB76E79),
                Color(0xFFE6A8B3),
                Color(0xFFD4AF37)
            ),
            count = 60..120,
            type = ParticleType.SPARKLE,
            speed = FloatRange(0.8f, 3f),
            size = FloatRange(1f, 5f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.8f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.METALLIC
    )
    
    val COPPER_PENNY_CANDY = CandyTheme(
        id = "copper_penny_candy",
        name = "Copper Penny Candy",
        description = "Warm metallic glow",
        baseColors = BaseColors(
            primary = Color(0xFFB87333),      // Copper
            secondary = Color(0xFFCD7F32),    // Bronze
            tertiary = Color(0xFFD4AF37),     // Gold
            accent = Color(0xFFE6BE8A),       // Burlywood Light
            surface = Color(0xFF1A1008),
            surfaceVariant = Color(0xFF2A1810),
            background = Color(0xFF0D0804),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFF5E8DC),
            onBackground = Color(0xFFF5E8DC)
        ),
        intensityConfig = IntensityConfig(0.4f, 2f, 1.2f, 0.1f, "Warmth"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF0D0804),
                Color(0xFF1A1008),
                Color(0xFF2A1810)
            ),
            intenseColors = listOf(
                Color(0xFFB87333).copy(alpha = 0.25f),
                Color(0xFF0D0804),
                Color(0xFFCD7F32).copy(alpha = 0.2f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFB87333),
                Color(0xFFCD7F32),
                Color(0xFFD4AF37)
            ),
            count = 50..100,
            type = ParticleType.SPARKLE,
            speed = FloatRange(0.7f, 2.5f),
            size = FloatRange(2f, 6f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.5f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.METALLIC
    )
    
    val TITANIUM_MINT = CandyTheme(
        id = "titanium_mint",
        name = "Titanium Mint",
        description = "Cool metallic freshness",
        baseColors = BaseColors(
            primary = Color(0xFF7DF9FF),      // Electric Blue
            secondary = Color(0xFFC0C0C0),    // Silver
            tertiary = Color(0xFF98FF98),     // Mint
            accent = Color(0xFFB0E0E6),       // Powder Blue
            surface = Color(0xFF0D1414),
            surfaceVariant = Color(0xFF1A2424),
            background = Color(0xFF050A0A),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE0F0F0),
            onBackground = Color(0xFFE0F0F0)
        ),
        intensityConfig = IntensityConfig(0.5f, 2f, 1.4f, 0.1f, "Cool Factor"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFF7DF9FF).copy(alpha = 0.1f),
                Color(0xFFC0C0C0).copy(alpha = 0.08f),
                Color(0xFF0D1414)
            ),
            animationSpeed = 0.8f,
            complexity = 6
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF7DF9FF),
                Color(0xFF98FF98),
                Color(0xFFFFFFFF)
            ),
            count = 70..140,
            type = ParticleType.SPARKLE,
            speed = FloatRange(1f, 4f),
            size = FloatRange(1f, 5f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2.2f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.METALLIC
    )
    
    // ═════════════════════════════════════════════════════════════════
    // SPECIAL 2026 THEMES
    // ═════════════════════════════════════════════════════════════════
    
    val QUANTUM_CANDY = CandyTheme(
        id = "quantum_candy",
        name = "Quantum Candy",
        description = "Schrödinger's sweetness",
        baseColors = BaseColors(
            primary = Color(0xFF00F5FF),      // Fluorescent Blue
            secondary = Color(0xFFFF00FF),    // Magenta
            tertiary = Color(0xFF39FF14),     // Neon Green
            accent = Color(0xFFFFD700),       // Gold
            surface = Color(0xFF050510),
            surfaceVariant = Color(0xFF0A0A20),
            background = Color(0xFF020208),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE8E8FF),
            onBackground = Color(0xFFE8E8FF)
        ),
        intensityConfig = IntensityConfig(1f, 2f, 1.8f, 0.1f, "Quantum"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFF00F5FF).copy(alpha = 0.2f),
                Color(0xFFFF00FF).copy(alpha = 0.18f),
                Color(0xFF39FF14).copy(alpha = 0.15f)
            ),
            animationSpeed = 2.5f,
            complexity = 9
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF00F5FF),
                Color(0xFFFF00FF),
                Color(0xFF39FF14),
                Color(0xFFFFD700)
            ),
            count = 120..240,
            type = ParticleType.CHAOTIC,
            speed = FloatRange(4f, 15f),
            size = FloatRange(1f, 8f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 4f,
            backgroundAnimationEnabled = true,
            transitionDuration = 80
        ),
        isDark = true,
        category = ThemeCategory2026.SPECIAL
    )
    
    val AI_CANDY_CORE = CandyTheme(
        id = "ai_candy_core",
        name = "AI Candy Core",
        description = "Neural network sweetness",
        baseColors = BaseColors(
            primary = Color(0xFF00FF41),      // Matrix Green
            secondary = Color(0xFF008F11),    // Dark Matrix
            tertiary = Color(0xFF003B00),     // Deep Green
            accent = Color(0xFF00FFFF),       // Cyan
            surface = Color(0xFF050505),
            surfaceVariant = Color(0xFF0A0A0A),
            background = Color(0xFF000000),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE0FFE0),
            onBackground = Color(0xFFE0FFE0)
        ),
        intensityConfig = IntensityConfig(0.8f, 2f, 1.6f, 0.1f, "Processing"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFF00FF41).copy(alpha = 0.08f),
                Color(0xFF008F11).copy(alpha = 0.05f),
                Color(0xFF000000)
            ),
            animationSpeed = 1.5f,
            complexity = 10
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF00FF41),
                Color(0xFF00FFFF),
                Color(0xFF008F11)
            ),
            count = 100..200,
            type = ParticleType.RAINING,
            speed = FloatRange(2f, 8f),
            size = FloatRange(1f, 4f)
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 3f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory2026.SPECIAL
    )
    
    // ═════════════════════════════════════════════════════════════════
    // ALL 2026 THEMES LIST
    // ═════════════════════════════════════════════════════════════════
    
    val ALL_2026_THEMES = listOf(
        // NEON (5)
        NEON_BUBBLEGUM, NEON_LIME_FIZZ, NEON_GRAPE_BOMB, NEON_ORANGE_SODA, NEON_ICE_POP,
        
        // GALACTIC (5)
        GALACTIC_ROCK_CANDY, NEBULA_GUMMY, ASTRONAUT_ICE_CREAM, SATURN_RINGS_CANDY, BLACK_HOLE_LICORICE,
        
        // RETRO (5)
        RETRO_ARCADE_CANDY, VHS_CARAMEL, DISCO_BALL_GUM, MIXTAPE_ROLL, POLAROID_MINT,
        
        // FRUIT (5)
        MANGO_TANGO, DRAGON_FRUIT_FANTASY, PASSIONFRUIT_PUNCH, KIWI_BURST, COCONUT_COLADA,
        
        // GOURMET (5)
        MACARON_PARIS, TIRAMISU_DREAM, MATCHA_CEREMONY, GOLD_LEAF_TRUFFLE, LAVENDER_HONEY,
        
        // KAWAII (5)
        SAKURA_BLOSSOM, KAWAII_UNICORN, HAMSTER_CHEEK, BUBBLE_TEA_LOVE, PUDDING_PUPPY,
        
        // HOLOGRAPHIC (4)
        HOLOGRAPHIC_PRISM, OPAL_DREAM, PEARL_SHIMMER, AURORA_BOREALIS,
        
        // METALLIC (4)
        CHROME_WRAPPER, ROSE_GOLD_FOIL, COPPER_PENNY_CANDY, TITANIUM_MINT,
        
        // SPECIAL (2)
        QUANTUM_CANDY, AI_CANDY_CORE
    )
    
    fun getAll() = ALL_2026_THEMES
    fun getByCategory(category: ThemeCategory2026) = ALL_2026_THEMES.filter { it.category == category }
    fun getById(id: String) = ALL_2026_THEMES.find { it.id == id }
    fun getRandom() = ALL_2026_THEMES.random()
}

/**
 * 2026 Theme Categories
 */
enum class ThemeCategory2026 {
    NEON,        // Cyberpunk glowing treats
    GALACTIC,    // Space-themed cosmic candy
    RETRO,       // 80s/90s nostalgia
    FRUIT,       // Tropical and fresh
    GOURMET,     // Sophisticated desserts
    KAWAII,      // Cute Japanese-inspired
    HOLOGRAPHIC, // Iridescent dreamy themes
    METALLIC,    // Shiny metallic treats
    SPECIAL      // Special/unique themes
}

/**
 * Extension to convert 2026 category to legacy category for compatibility
 */
fun ThemeCategory2026.toLegacyCategory(): ThemeCategory = when (this) {
    ThemeCategory2026.NEON -> ThemeCategory.TRIPPY
    ThemeCategory2026.GALACTIC -> ThemeCategory.TRIPPY
    ThemeCategory2026.RETRO -> ThemeCategory.CLASSIC
    ThemeCategory2026.FRUIT -> ThemeCategory.CLASSIC
    ThemeCategory2026.GOURMET -> ThemeCategory.CHILL
    ThemeCategory2026.KAWAII -> ThemeCategory.CLASSIC
    ThemeCategory2026.HOLOGRAPHIC -> ThemeCategory.TRIPPY
    ThemeCategory2026.METALLIC -> ThemeCategory.DARK
    ThemeCategory2026.SPECIAL -> ThemeCategory.SUGARRUSH
}
