package com.sugarmunch.app.theme.presets

import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.theme.model.AnimationConfig
import com.sugarmunch.app.theme.model.BackgroundStyle
import com.sugarmunch.app.theme.model.BaseColors
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.model.FloatRange
import com.sugarmunch.app.theme.model.IntensityConfig
import com.sugarmunch.app.theme.model.ParticleConfig
import com.sugarmunch.app.theme.model.ParticleType
import com.sugarmunch.app.theme.model.ThemeCategory

object PhaseOneUtilityThemes {
    val SUGAR_FILES_GLOW = CandyTheme(
        id = "sugar_files_glow",
        name = "Sugar Files Glow",
        description = "Electric vault blues with cool candy chrome.",
        baseColors = BaseColors(
            primary = Color(0xFF6C63FF),
            secondary = Color(0xFF3B82F6),
            tertiary = Color(0xFF7DD3FC),
            accent = Color(0xFF9F7AEA),
            surface = Color(0xFFF8FAFF),
            surfaceVariant = Color(0xFFE6ECFF),
            background = Color(0xFFF8FBFF),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF151B34),
            onBackground = Color(0xFF151B34)
        ),
        intensityConfig = IntensityConfig(0.3f, 2f, 1f, 0.1f, "Vault glow"),
        backgroundStyle = BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                Color(0xFFEEF2FF),
                Color(0xFFDCEBFF),
                Color(0xFFEDE9FE)
            ),
            animationSpeed = 0.55f,
            complexity = 4
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF6C63FF),
                Color(0xFF3B82F6),
                Color(0xFF7DD3FC)
            ),
            count = 25..55,
            speed = FloatRange(0.6f, 1.8f),
            type = ParticleType.SPARKLE
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.9f,
            backgroundAnimationEnabled = true
        ),
        isDark = false,
        category = ThemeCategory.CHILL
    )

    val TAFFY_TRANSFER_RUSH = CandyTheme(
        id = "taffy_transfer_rush",
        name = "Taffy Transfer Rush",
        description = "Bright ribbons, warm bursts, and sugary nearby scans.",
        baseColors = BaseColors(
            primary = Color(0xFFFF8A65),
            secondary = Color(0xFFFF4D8D),
            tertiary = Color(0xFFFFC857),
            accent = Color(0xFFFF6B6B),
            surface = Color(0xFFFFF7F3),
            surfaceVariant = Color(0xFFFFE8E0),
            background = Color(0xFFFFFBF8),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF2B1411),
            onBackground = Color(0xFF2B1411)
        ),
        intensityConfig = IntensityConfig(0.4f, 2f, 1.1f, 0.1f, "Transfer rush"),
        backgroundStyle = BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFFBF8),
                Color(0xFFFFE0D4).copy(alpha = 0.45f),
                Color(0xFFFFE7F2).copy(alpha = 0.35f)
            ),
            intenseColors = listOf(
                Color(0xFFFF8A65).copy(alpha = 0.35f),
                Color(0xFFFF4D8D).copy(alpha = 0.3f),
                Color(0xFFFFC857).copy(alpha = 0.25f)
            )
        ),
        particleConfig = ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF8A65),
                Color(0xFFFF4D8D),
                Color(0xFFFFC857)
            ),
            count = 40..85,
            speed = FloatRange(1.2f, 3.2f),
            type = ParticleType.BUBBLES
        ),
        animationConfig = AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.6f,
            backgroundAnimationEnabled = true,
            transitionDuration = 220
        ),
        isDark = false,
        category = ThemeCategory.SUGARRUSH
    )

    val ALL = listOf(SUGAR_FILES_GLOW, TAFFY_TRANSFER_RUSH)
}
