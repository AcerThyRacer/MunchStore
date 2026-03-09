package com.sugarmunch.app.theme.presets

import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.theme.model.*

/**
 * SugarMunch Extreme - 20+ New Candy-Themed Presets
 * Organized in 5 collections: Chocolate, Caramel, Gummy, Lollipop, and Extreme
 */
object SugarExtremeThemes {

    // ═════════════════════════════════════════════════════════════════
    // CHOCOLATE COLLECTION (4 themes)
    // ═════════════════════════════════════════════════════════════════

    val DARK_CHOCOLATE = CandyTheme(
        id = "dark_chocolate",
        name = "Dark Chocolate",
        description = "Rich dark chocolate with gold accents",
        baseColors = BaseColors(
            primary = Color(0xFF3E2723),      // Dark chocolate brown
            secondary = Color(0xFF5D4037),    // Milk chocolate
            tertiary = Color(0xFFD4AF37),     // Gold
            accent = Color(0xFFFFD700),       // Bright gold
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFFFF3E0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFFFFF8E1),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A),
            error = Color(0xFFFF6B6B),
            success = Color(0xFF51CF66)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1f, 0.1f, "Richness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF3E2723),
                Color(0xFF5D4037),
                Color(0xFF8D6E63)
            ),
            intenseColors = listOf(
                Color(0xFF3E2723),
                Color(0xFF6D4C41),
                Color(0xFFD4AF37)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFD4AF37),
                Color(0xFFFFD700),
                Color(0xFF8D6E63)
            ),
            count = 20..40,
            type = ParticleType.SPARKLE
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.8f,
            backgroundAnimationEnabled = true
        ),
        isDark = true,
        category = ThemeCategory.CHOCOLATE
    )

    val MILK_CHOCOLATE = CandyTheme(
        id = "milk_chocolate",
        name = "Milk Chocolate",
        description = "Creamy milk chocolate with caramel swirls",
        baseColors = BaseColors(
            primary = Color(0xFF8D6E63),      // Milk chocolate
            secondary = Color(0xFFA1887F),    // Light chocolate
            tertiary = Color(0xFFFFD54F),     // Caramel
            accent = Color(0xFFFFB74D),       // Orange caramel
            surface = Color(0xFFFFFBF7),
            surfaceVariant = Color(0xFFFFF3E0),
            background = Color(0xFFFFF8F0),
            onPrimary = Color(0xFFFFF8E1),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 0.8f, 0.1f, "Creaminess"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFF8D6E63).copy(alpha = 0.3f),
                Color(0xFFFFD54F).copy(alpha = 0.2f),
                Color(0xFFA1887F).copy(alpha = 0.25f)
            ),
            animationSpeed = 0.4f,
            complexity = 4
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFD54F),
                Color(0xFFFFB74D),
                Color(0xFFD7CCC8)
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
        category = ThemeCategory.CHOCOLATE
    )

    val WHITE_CHOCOLATE = CandyTheme(
        id = "white_chocolate",
        name = "White Chocolate",
        description = "Ivory white with pink highlights",
        baseColors = BaseColors(
            primary = Color(0xFFFFF8E1),      // Ivory
            secondary = Color(0xFFFFECB3),    // Cream
            tertiary = Color(0xFFFFB6C1),     // Pink
            accent = Color(0xFFFFC0CB),       // Light pink
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFFFF9F0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFF3E2723),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 0.7f, 0.1f, "Sweetness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFF8E1),
                Color(0xFFFFECB3).copy(alpha = 0.5f),
                Color(0xFFFFB6C1).copy(alpha = 0.3f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFB6C1),
                Color(0xFFFFC0CB),
                Color(0xFFFFF8E1)
            ),
            count = 40..60,
            type = ParticleType.BUBBLES
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.2f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.CHOCOLATE
    )

    val RUBY_CHOCOLATE = CandyTheme(
        id = "ruby_chocolate",
        name = "Ruby Chocolate",
        description = "Rare pink-brown ruby chocolate",
        baseColors = BaseColors(
            primary = Color(0xFFB76E79),      // Ruby pink
            secondary = Color(0xFFD48792),    // Light ruby
            tertiary = Color(0xFF8D6E63),     // Chocolate brown
            accent = Color(0xFFFF69B4),       // Hot pink
            surface = Color(0xFFFFF0F5),
            surfaceVariant = Color(0xFFFFE4E1),
            background = Color(0xFFFFF8F0),
            onPrimary = Color(0xFFFFF0F5),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.2f, 0.1f, "Rarity"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFB76E79).copy(alpha = 0.4f),
                Color(0xFF8D6E63).copy(alpha = 0.3f),
                Color(0xFFFF69B4).copy(alpha = 0.2f)
            ),
            animationSpeed = 0.6f,
            complexity = 5
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFB76E79),
                Color(0xFFFF69B4),
                Color(0xFFD48792)
            ),
            count = 35..55,
            type = ParticleType.SPARKLE
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.1f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.CHOCOLATE
    )

    // ═════════════════════════════════════════════════════════════════
    // CARAMEL COLLECTION (3 themes)
    // ═════════════════════════════════════════════════════════════════

    val SALTED_CARAMEL = CandyTheme(
        id = "salted_caramel",
        name = "Salted Caramel",
        description = "Golden caramel with sea salt sparkle",
        baseColors = BaseColors(
            primary = Color(0xFFC68E59),      // Caramel
            secondary = Color(0xFFD4A574),    // Light caramel
            tertiary = Color(0xFFFFF8DC),     // Cornsilk
            accent = Color(0xFFF0E68C),       // Khaki (salt)
            surface = Color(0xFFFFFBF7),
            surfaceVariant = Color(0xFFFFF3E0),
            background = Color(0xFFFFFAF0),
            onPrimary = Color(0xFFFFF8E1),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1f, 0.1f, "Saltiness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFFAF0),
                Color(0xFFC68E59).copy(alpha = 0.4f),
                Color(0xFFD4A574).copy(alpha = 0.3f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFF8DC),
                Color(0xFFF0E68C),
                Color(0xFFFFD700)
            ),
            count = 25..45,
            type = ParticleType.SPARKLE
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.9f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.CARAMEL
    )

    val CARAMEL_DELIGHT = CandyTheme(
        id = "caramel_delight",
        name = "Caramel Delight",
        description = "Swirled caramel gradient paradise",
        baseColors = BaseColors(
            primary = Color(0xFFB87333),      // Copper caramel
            secondary = Color(0xFFD2691E),    // Chocolate
            tertiary = Color(0xFFFFDAB9),     // Peach puff
            accent = Color(0xFFFF8C00),       // Dark orange
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFFFE4C4),
            background = Color(0xFFFFF0E0),
            onPrimary = Color(0xFFFFF8E1),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.3f, 0.1f, "Swirl"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFB87333).copy(alpha = 0.5f),
                Color(0xFFFFDAB9).copy(alpha = 0.3f),
                Color(0xFFFF8C00).copy(alpha = 0.25f)
            ),
            animationSpeed = 0.7f,
            complexity = 6
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFDAB9),
                Color(0xFFFF8C00),
                Color(0xFFD2691E)
            ),
            count = 40..70,
            type = ParticleType.SWIRLING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.2f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.CARAMEL
    )

    val BUTTERSCOTCH = CandyTheme(
        id = "butterscotch",
        name = "Butterscotch",
        description = "Golden yellow butterscotch warmth",
        baseColors = BaseColors(
            primary = Color(0xFFFFD700),      // Gold
            secondary = Color(0xFFFFE4B5),    // Moccasin
            tertiary = Color(0xFFFFA500),     // Orange
            accent = Color(0xFFFF8C00),       // Dark orange
            surface = Color(0xFFFFFBF7),
            surfaceVariant = Color(0xFFFFF3E0),
            background = Color(0xFFFFFAF0),
            onPrimary = Color(0xFF1A1A1A),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.1f, 0.1f, "Warmth"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFFAF0),
                Color(0xFFFFD700).copy(alpha = 0.5f),
                Color(0xFFFFA500).copy(alpha = 0.3f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFFA500),
                Color(0xFFFFE4B5)
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
        category = ThemeCategory.CARAMEL
    )

    // ═════════════════════════════════════════════════════════════════
    // GUMMY COLLECTION (4 themes)
    // ═════════════════════════════════════════════════════════════════

    val GUMMY_BEAR = CandyTheme(
        id = "gummy_bear",
        name = "Gummy Bear",
        description = "Translucent multicolor gummy bears",
        baseColors = BaseColors(
            primary = Color(0xFFFF1493),      // Deep pink
            secondary = Color(0xFF32CD32),    // Lime green
            tertiary = Color(0xFFFFD700),     // Gold
            accent = Color(0xFF00BFFF),       // Deep sky blue
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFF0FFF0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.5f, 0.1f, "Chewiness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFF8F0),
                Color(0xFFFF1493).copy(alpha = 0.2f),
                Color(0xFF32CD32).copy(alpha = 0.15f),
                Color(0xFF00BFFF).copy(alpha = 0.15f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF1493).copy(alpha = 0.6f),
                Color(0xFF32CD32).copy(alpha = 0.6f),
                Color(0xFFFFD700).copy(alpha = 0.6f),
                Color(0xFF00BFFF).copy(alpha = 0.6f)
            ),
            count = 50..80,
            type = ParticleType.BUBBLES
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.3f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.GUMMY
    )

    val GUMMY_WORM = CandyTheme(
        id = "gummy_worm",
        name = "Gummy Worm",
        description = "Rainbow gradient gummy worms",
        baseColors = BaseColors(
            primary = Color(0xFFFF0000),      // Red
            secondary = Color(0xFFFFA500),    // Orange
            tertiary = Color(0xFFFFFF00),     // Yellow
            accent = Color(0xFF00FF00),       // Green
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFF0FFF0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.6f, 0.1f, "Rainbow"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFF0000).copy(alpha = 0.3f),
                Color(0xFFFFA500).copy(alpha = 0.3f),
                Color(0xFF00FF00).copy(alpha = 0.3f),
                Color(0xFF00BFFF).copy(alpha = 0.3f)
            ),
            animationSpeed = 0.8f,
            complexity = 5
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF0000).copy(alpha = 0.5f),
                Color(0xFFFFA500).copy(alpha = 0.5f),
                Color(0xFFFFFF00).copy(alpha = 0.5f),
                Color(0xFF00FF00).copy(alpha = 0.5f),
                Color(0xFF00BFFF).copy(alpha = 0.5f)
            ),
            count = 60..100,
            type = ParticleType.RISING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.4f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.GUMMY
    )

    val SOUR_GUMMY = CandyTheme(
        id = "sour_gummy",
        name = "Sour Gummy",
        description = "Neon sour candy explosion",
        baseColors = BaseColors(
            primary = Color(0xFF39FF14),      // Neon green
            secondary = Color(0xFFFF3F34),    // Neon red
            tertiary = Color(0xFFFFFF00),     // Yellow
            accent = Color(0xFFFF00FF),       // Magenta
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFF0FFF0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFF1A1A1A),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.8f, 0.1f, "Sourness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFF8F0),
                Color(0xFF39FF14).copy(alpha = 0.4f),
                Color(0xFFFF3F34).copy(alpha = 0.3f)
            ),
            intenseColors = listOf(
                Color(0xFF39FF14),
                Color(0xFFFF3F34),
                Color(0xFFFFFF00)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF39FF14),
                Color(0xFFFF3F34),
                Color(0xFFFFFF00),
                Color(0xFFFF00FF)
            ),
            count = 70..120,
            type = ParticleType.CHAOTIC
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.8f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.GUMMY
    )

    val PEACH_RINGS = CandyTheme(
        id = "peach_rings",
        name = "Peach Rings",
        description = "Peachy orange-pink gummy rings",
        baseColors = BaseColors(
            primary = Color(0xFFFFCBA4),      // Peach
            secondary = Color(0xFFFF9999),    // Light red
            tertiary = Color(0xFFFF6B6B),     // Coral
            accent = Color(0xFFFF8C69),       // Salmon
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFFFE4E1),
            background = Color(0xFFFFF0F5),
            onPrimary = Color(0xFF1A1A1A),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.2f, 0.1f, "Peachiness"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFF0F5),
                Color(0xFFFFCBA4).copy(alpha = 0.4f),
                Color(0xFFFF9999).copy(alpha = 0.3f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFFCBA4),
                Color(0xFFFF9999),
                Color(0xFFFF6B6B)
            ),
            count = 40..60,
            type = ParticleType.SWIRLING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.1f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.GUMMY
    )

    // ═════════════════════════════════════════════════════════════════
    // LOLLIPOP COLLECTION (3 themes)
    // ═════════════════════════════════════════════════════════════════

    val LOLLIPOP_SWIRL = CandyTheme(
        id = "lollipop_swirl",
        name = "Lollipop Swirl",
        description = "Spiral rainbow lollipop",
        baseColors = BaseColors(
            primary = Color(0xFFFF1493),      // Deep pink
            secondary = Color(0xFF00BFFF),    // Deep sky blue
            tertiary = Color(0xFF32CD32),     // Lime green
            accent = Color(0xFFFFD700),       // Gold
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFF0F8FF),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.4f, 0.1f, "Swirl"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFF1493).copy(alpha = 0.4f),
                Color(0xFF00BFFF).copy(alpha = 0.4f),
                Color(0xFF32CD32).copy(alpha = 0.4f),
                Color(0xFFFFD700).copy(alpha = 0.3f)
            ),
            animationSpeed = 0.9f,
            complexity = 7
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF1493),
                Color(0xFF00BFFF),
                Color(0xFF32CD32),
                Color(0xFFFFD700)
            ),
            count = 50..80,
            type = ParticleType.SWIRLING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.3f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.LOLLIPOP
    )

    val CHUPA_CHUPS = CandyTheme(
        id = "chupa_chups",
        name = "Chupa Chups",
        description = "Classic red-white striped lollipop",
        baseColors = BaseColors(
            primary = Color(0xFFFF0000),      // Red
            secondary = Color(0xFFFFFFFF),    // White
            tertiary = Color(0xFFFF6B6B),     // Light red
            accent = Color(0xFFFFD700),       // Gold (wrapper)
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFF0F0F0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1f, 0.1f, "Classic"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFFF0000).copy(alpha = 0.3f),
                Color(0xFFFF6B6B).copy(alpha = 0.2f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF0000),
                Color(0xFFFFFFFF),
                Color(0xFFFFD700)
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
        category = ThemeCategory.LOLLIPOP
    )

    val RAINBOW_POP = CandyTheme(
        id = "rainbow_pop",
        name = "Rainbow Pop",
        description = "Bright rainbow popsicle",
        baseColors = BaseColors(
            primary = Color(0xFFFF0000),      // Red
            secondary = Color(0xFFFFA500),    // Orange
            tertiary = Color(0xFFFFFF00),     // Yellow
            accent = Color(0xFF00FF00),       // Green
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFF0FFF0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.7f, 0.1f, "Pop"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFF8F0),
                Color(0xFFFF0000).copy(alpha = 0.25f),
                Color(0xFFFFA500).copy(alpha = 0.25f),
                Color(0xFF00FF00).copy(alpha = 0.25f),
                Color(0xFF00BFFF).copy(alpha = 0.25f)
            ),
            intenseColors = listOf(
                Color(0xFFFF0000),
                Color(0xFFFFA500),
                Color(0xFFFFFF00),
                Color(0xFF00FF00),
                Color(0xFF00BFFF)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF0000),
                Color(0xFFFFA500),
                Color(0xFFFFFF00),
                Color(0xFF00FF00),
                Color(0xFF00BFFF),
                Color(0xFF8B00FF)
            ),
            count = 80..120,
            type = ParticleType.RISING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.5f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.LOLLIPOP
    )

    // ═════════════════════════════════════════════════════════════════
    // SUGAR RUSH EXTREME COLLECTION (6 themes)
    // ═════════════════════════════════════════════════════════════════

    val NUCLEAR_SUGAR = CandyTheme(
        id = "nuclear_sugar",
        name = "Nuclear Sugar",
        description = "Neon green-yellow radioactive overload",
        baseColors = BaseColors(
            primary = Color(0xFF39FF14),      // Neon green
            secondary = Color(0xFFFFFF00),    // Yellow
            tertiary = Color(0xFF00FF00),     // Lime
            accent = Color(0xFF7FFF00),       // Chartreuse
            surface = Color(0xFFF0FFF0),
            surfaceVariant = Color(0xFFE0FFE0),
            background = Color(0xFFFAFAD2),
            onPrimary = Color(0xFF1A1A1A),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 2f, 0.1f, "Radiation"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFF39FF14).copy(alpha = 0.6f),
                Color(0xFFFFFF00).copy(alpha = 0.5f),
                Color(0xFF00FF00).copy(alpha = 0.5f)
            ),
            animationSpeed = 1.5f,
            complexity = 8
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF39FF14),
                Color(0xFFFFFF00),
                Color(0xFF00FF00),
                Color(0xFF7FFF00)
            ),
            count = 100..150,
            type = ParticleType.CHAOTIC
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.EXTREME
    )

    val CANDY_OVERLOAD = CandyTheme(
        id = "candy_overload",
        name = "Candy Overload",
        description = "Maximum saturation candy explosion",
        baseColors = BaseColors(
            primary = Color(0xFFFF1493),      // Deep pink
            secondary = Color(0xFF00FFFF),    // Cyan
            tertiary = Color(0xFFFF00FF),     // Magenta
            accent = Color(0xFFFFFF00),       // Yellow
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFF0F8FF),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 2f, 0.1f, "Overload"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFF1493),
                Color(0xFF00FFFF),
                Color(0xFFFF00FF),
                Color(0xFFFFFF00)
            ),
            intenseColors = listOf(
                Color(0xFFFF1493),
                Color(0xFF00FFFF),
                Color(0xFFFF00FF),
                Color(0xFFFFFF00),
                Color(0xFF00FF00)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF1493),
                Color(0xFF00FFFF),
                Color(0xFFFF00FF),
                Color(0xFFFFFF00),
                Color(0xFF00FF00)
            ),
            count = 120..180,
            type = ParticleType.EXPLODING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.EXTREME
    )

    val COTTON_CANDY_CLOUD = CandyTheme(
        id = "cotton_candy_cloud",
        name = "Cotton Candy Cloud",
        description = "Ultra fluffy pink-blue cloud",
        baseColors = BaseColors(
            primary = Color(0xFFFF9ECD),      // Pink
            secondary = Color(0xFFB5DEFF),    // Blue
            tertiary = Color(0xFFE8C5FF),     // Purple
            accent = Color(0xFFFFB6C1),       // Light pink
            surface = Color(0xFFFFF0F7),
            surfaceVariant = Color(0xFFE8F4FF),
            background = Color(0xFFFAF0FF),
            onPrimary = Color(0xFF2D1B2E),
            onSurface = Color(0xFF2D1B2E),
            onBackground = Color(0xFF2D1B2E)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.8f, 0.1f, "Fluffiness"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFF9ECD).copy(alpha = 0.5f),
                Color(0xFFB5DEFF).copy(alpha = 0.5f),
                Color(0xFFE8C5FF).copy(alpha = 0.4f)
            ),
            animationSpeed = 0.3f,
            complexity = 6
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF9ECD).copy(alpha = 0.7f),
                Color(0xFFB5DEFF).copy(alpha = 0.7f),
                Color(0xFFE8C5FF).copy(alpha = 0.6f)
            ),
            count = 80..140,
            type = ParticleType.FLOATING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.6f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.EXTREME
    )

    val SOUR_PATCH = CandyTheme(
        id = "sour_patch",
        name = "Sour Patch",
        description = "Electric sour green candy",
        baseColors = BaseColors(
            primary = Color(0xFF00FF00),      // Green
            secondary = Color(0xFF39FF14),    // Neon green
            tertiary = Color(0xFFFFFF00),     // Yellow
            accent = Color(0xFFADFF2F),       // Green yellow
            surface = Color(0xFFF0FFF0),
            surfaceVariant = Color(0xFFE0FFE0),
            background = Color(0xFFFAFAD2),
            onPrimary = Color(0xFF1A1A1A),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 1.9f, 0.1f, "Sour"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFAFAD2),
                Color(0xFF00FF00).copy(alpha = 0.5f),
                Color(0xFF39FF14).copy(alpha = 0.4f)
            ),
            intenseColors = listOf(
                Color(0xFF39FF14),
                Color(0xFF00FF00),
                Color(0xFFFFFF00)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF00FF00),
                Color(0xFF39FF14),
                Color(0xFFFFFF00)
            ),
            count = 90..140,
            type = ParticleType.CHAOTIC
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.9f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.EXTREME
    )

    val SKITTLES_STORM = CandyTheme(
        id = "skittles_storm",
        name = "Skittles Storm",
        description = "Rainbow candy storm explosion",
        baseColors = BaseColors(
            primary = Color(0xFFFF0000),      // Red
            secondary = Color(0xFFFFA500),    // Orange
            tertiary = Color(0xFFFFFF00),     // Yellow
            accent = Color(0xFF00FF00),       // Green
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFF0FFF0),
            background = Color(0xFFFFFBF7),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 2f, 0.1f, "Storm"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFFF0000).copy(alpha = 0.5f),
                Color(0xFFFFA500).copy(alpha = 0.5f),
                Color(0xFFFFFF00).copy(alpha = 0.5f),
                Color(0xFF00FF00).copy(alpha = 0.5f),
                Color(0xFF00BFFF).copy(alpha = 0.5f),
                Color(0xFF8B00FF).copy(alpha = 0.5f)
            ),
            animationSpeed = 1.2f,
            complexity = 9
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF0000),
                Color(0xFFFFA500),
                Color(0xFFFFFF00),
                Color(0xFF00FF00),
                Color(0xFF00BFFF),
                Color(0xFF8B00FF)
            ),
            count = 150..200,
            type = ParticleType.RAINING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.EXTREME
    )

    val STARBURST_BURST = CandyTheme(
        id = "starburst_burst",
        name = "Starburst Burst",
        description = "Orange-red-yellow fruit candy burst",
        baseColors = BaseColors(
            primary = Color(0xFFFF4500),      // Orange red
            secondary = Color(0xFFFF6347),    // Tomato
            tertiary = Color(0xFFFFD700),     // Gold
            accent = Color(0xFFFF8C00),       // Dark orange
            surface = Color(0xFFFFF8F0),
            surfaceVariant = Color(0xFFFFE4C4),
            background = Color(0xFFFFF0E0),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1A),
            onBackground = Color(0xFF1A1A1A)
        ),
        intensityConfig = IntensityConfig(0f, 2f, 2f, 0.1f, "Burst"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFF0E0),
                Color(0xFFFF4500).copy(alpha = 0.6f),
                Color(0xFFFF6347).copy(alpha = 0.5f),
                Color(0xFFFFD700).copy(alpha = 0.4f)
            ),
            intenseColors = listOf(
                Color(0xFFFF4500),
                Color(0xFFFF6347),
                Color(0xFFFFD700),
                Color(0xFFFF8C00)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF4500),
                Color(0xFFFF6347),
                Color(0xFFFFD700),
                Color(0xFFFF8C00)
            ),
            count = 100..160,
            type = ParticleType.EXPLODING
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.EXTREME
    )

    /**
     * Get all Sugar Extreme themes
     */
    fun getAllThemes(): List<CandyTheme> = listOf(
        // Chocolate Collection
        DARK_CHOCOLATE,
        MILK_CHOCOLATE,
        WHITE_CHOCOLATE,
        RUBY_CHOCOLATE,
        // Caramel Collection
        SALTED_CARAMEL,
        CARAMEL_DELIGHT,
        BUTTERSCOTCH,
        // Gummy Collection
        GUMMY_BEAR,
        GUMMY_WORM,
        SOUR_GUMMY,
        PEACH_RINGS,
        // Lollipop Collection
        LOLLIPOP_SWIRL,
        CHUPA_CHUPS,
        RAINBOW_POP,
        // Extreme Collection
        NUCLEAR_SUGAR,
        CANDY_OVERLOAD,
        COTTON_CANDY_CLOUD,
        SOUR_PATCH,
        SKITTLES_STORM,
        STARBURST_BURST
    )
}
