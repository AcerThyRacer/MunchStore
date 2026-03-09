package com.sugarmunch.app.theme.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * SugarRush Theme System - Maximum intensity candy themes
 */
data class CandyTheme(
    val id: String,
    val name: String,
    val description: String,
    val baseColors: BaseColors,
    val intensityConfig: IntensityConfig,
    val backgroundStyle: BackgroundStyle,
    val particleConfig: ParticleConfig,
    val animationConfig: AnimationConfig,
    val isDark: Boolean = false,
    val category: ThemeCategory = ThemeCategory.CLASSIC
) {
    /**
     * Get colors adjusted for current intensity (0.0 - 2.0)
     */
    fun getColorsForIntensity(intensity: Float): AdjustedColors {
        val normalizedIntensity = intensity.coerceIn(0.0f, 2.0f)
        val saturationBoost = 1f + (normalizedIntensity * 0.5f) // Saturation up to 2x
        val brightnessBoost = 1f + (normalizedIntensity * 0.3f) // Brightness up to 1.3x
        
        return AdjustedColors(
            primary = boostColor(baseColors.primary, saturationBoost, brightnessBoost),
            secondary = boostColor(baseColors.secondary, saturationBoost, brightnessBoost),
            tertiary = boostColor(baseColors.tertiary, saturationBoost, brightnessBoost),
            accent = boostColor(baseColors.accent, saturationBoost, brightnessBoost),
            surface = baseColors.surface,
            surfaceVariant = baseColors.surfaceVariant,
            background = baseColors.background,
            onPrimary = baseColors.onPrimary,
            onSurface = baseColors.onSurface,
            onBackground = baseColors.onBackground,
            error = baseColors.error,
            success = baseColors.success
        )
    }
    
    /**
     * Get background gradient based on intensity
     */
    fun getBackgroundGradient(intensity: Float): Brush {
        return when (backgroundStyle) {
            is BackgroundStyle.Gradient -> {
                val colors = if (intensity >= 1.5f && backgroundStyle.intenseColors != null) {
                    backgroundStyle.intenseColors
                } else {
                    backgroundStyle.colors
                }
                val adjustedColors = colors.map { color ->
                    boostColor(color, 1f + intensity * 0.3f, 1f)
                }
                Brush.verticalGradient(adjustedColors)
            }
            is BackgroundStyle.AnimatedMesh -> {
                // Returns base gradient, mesh handled by composable
                Brush.verticalGradient(backgroundStyle.baseColors)
            }
            is BackgroundStyle.Solid -> {
                Brush.linearGradient(listOf(backgroundStyle.color, backgroundStyle.color))
            }
        }
    }
    
    private fun boostColor(color: Color, saturationMult: Float, brightnessMult: Float): Color {
        val hsl = colorToHsl(color)
        return hslToColor(
            h = hsl[0],
            s = (hsl[1] * saturationMult).coerceIn(0f, 1f),
            l = (hsl[2] * brightnessMult).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }
    
    private fun colorToHsl(color: Color): FloatArray {
        val r = color.red
        val g = color.green
        val b = color.blue
        
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val l = (max + min) / 2f
        
        if (max == min) {
            return floatArrayOf(0f, 0f, l)
        }
        
        val d = max - min
        val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
        
        val h = when (max) {
            r -> (g - b) / d + (if (g < b) 6 else 0)
            g -> (b - r) / d + 2
            b -> (r - g) / d + 4
            else -> 0f
        } / 6f
        
        return floatArrayOf(h * 360f, s, l)
    }
    
    private fun hslToColor(h: Float, s: Float, l: Float, alpha: Float): Color {
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f
        
        val (r, g, b) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        
        return Color(
            red = (r + m).coerceIn(0f, 1f),
            green = (g + m).coerceIn(0f, 1f),
            blue = (b + m).coerceIn(0f, 1f),
            alpha = alpha
        )
    }
}

data class BaseColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val accent: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val background: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val onBackground: Color,
    val error: Color = Color(0xFFFF6B6B),
    val success: Color = Color(0xFF51CF66)
)

data class AdjustedColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val accent: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val background: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val onBackground: Color,
    val error: Color,
    val success: Color
)

data class IntensityConfig(
    val minValue: Float = 0f,
    val maxValue: Float = 2f,
    val defaultValue: Float = 1f,
    val stepSize: Float = 0.1f,
    val label: String = "Intensity"
)

sealed class BackgroundStyle {
    data class Gradient(
        val colors: List<Color>,
        val intenseColors: List<Color>? = null,
        /** Gradient angle in degrees (0 = left-to-right, 90 = top-to-bottom). */
        val angleDegrees: Float = 90f
    ) : BackgroundStyle()
    
    data class AnimatedMesh(
        val baseColors: List<Color>,
        val animationSpeed: Float = 1f,
        val complexity: Int = 3
    ) : BackgroundStyle()
    
    data class Solid(
        val color: Color
    ) : BackgroundStyle()
}

data class ParticleConfig(
    val enabled: Boolean = true,
    val colors: List<Color>,
    val count: IntRange = 20..60,
    val speed: FloatRange = FloatRange(0.5f, 2f),
    val size: FloatRange = FloatRange(2f, 8f),
    val type: ParticleType = ParticleType.FLOATING,
    val intensityMultiplier: Float = 1f
)

data class AnimationConfig(
    val cardPulseEnabled: Boolean = true,
    val cardPulseSpeed: Float = 1f,
    val backgroundAnimationEnabled: Boolean = true,
    val transitionDuration: Int = 300,
    val staggerDelay: Int = 50
)

data class FloatRange(val min: Float, val max: Float)

enum class ParticleType {
    FLOATING,     // Gentle floating particles
    RAINING,      // Falling from top
    RISING,       // Rising from bottom
    EXPLODING,    // Burst from center
    SWIRLING,     // Circular motion
    CHAOTIC,      // Random crazy movement
    BUBBLES,      // Large, slow, high alpha
    SPARKLE,      // Small, fast twinkle
    // NEW: Candy-specific particle types
    CANDY_CANE,   // Rotating striped candy canes
    GUMMY_BEAR,   // Bouncing gummy bear shapes
    SPRINKLES,    // Tiny colored confetti rods
    LOLLIPOP_SPIN,// Rotating circular lollipops
    SUGAR_CRYSTAL // Sparkling diamond crystals
}

enum class ThemeCategory {
    CLASSIC,      // Original candy themes
    SUGARRUSH,    // High intensity themes
    TRIPPY,       // Psychedelic themes
    CHILL,        // Calm, muted themes
    DARK,         // Dark mode optimized
    SEASONAL,     // Holiday themes
    CUSTOM,       // User created
    // NEW: Sugar Extreme Collections
    CHOCOLATE,    // Chocolate-themed presets
    CARAMEL,      // Caramel-themed presets
    GUMMY,        // Gummy candy-themed presets
    LOLLIPOP,     // Lollipop-themed presets
    EXTREME       // Maximum intensity extreme themes
}

/**
 * Theme intensity preset levels
 */
object IntensityLevels {
    const val CHILL = 0.3f      // Muted, calm
    const val NORMAL = 0.7f     // Standard candy
    const val SWEET = 1.0f      // Full candy experience
    const val SUGARRUSH = 1.5f  // High intensity
    const val MAXIMUM = 2.0f    // Overdrive
}
