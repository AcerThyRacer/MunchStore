package com.sugarmunch.app.plugin

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.model.*
import kotlin.math.*

/**
 * SugarMunch Theme Plugin - Base class for custom theme plugins
 * 
 * Plugin developers extend this class to create custom themes with
 * dynamic colors, animations, and particle configurations.
 * 
 * Example:
 * ```kotlin
 * class CyberpunkTheme : ThemePlugin(
 *     id = "cyberpunk_neon",
 *     name = "Cyberpunk Neon",
 *     description = "High-tech neon theme with cyber aesthetics"
 * ) {
 *     override fun generateColors(intensity: Float): BaseColors {
 *         return BaseColors(
 *             primary = Color(0xFF00FFFF),
 *             // ... other colors
 *         )
 *     }
 * }
 * ```
 */
abstract class ThemePlugin(
    val id: String,
    val name: String,
    val description: String,
    val category: ThemeCategory = ThemeCategory.CUSTOM,
    val isDark: Boolean = false,
    val minIntensity: Float = 0f,
    val maxIntensity: Float = 2f,
    val defaultIntensity: Float = 1f
) {
    /**
     * Generate base colors for this theme at a given intensity
     * @param intensity The intensity level (0.0 - 2.0)
     * @return BaseColors for the theme
     */
    abstract fun generateColors(intensity: Float): BaseColors
    
    /**
     * Generate background style for this theme
     * @param intensity The intensity level (0.0 - 2.0)
     * @return BackgroundStyle configuration
     */
    abstract fun generateBackground(intensity: Float): BackgroundStyle
    
    /**
     * Generate particle configuration for this theme
     * @param intensity The intensity level (0.0 - 2.0)
     * @return ParticleConfig configuration
     */
    abstract fun generateParticles(intensity: Float): ParticleConfig
    
    /**
     * Generate animation configuration for this theme
     * @param intensity The intensity level (0.0 - 2.0)
     * @return AnimationConfig configuration
     */
    abstract fun generateAnimations(intensity: Float): AnimationConfig
    
    /**
     * Get adjusted colors for a specific intensity
     * Override for custom color adjustment logic
     */
    open fun getColorsForIntensity(intensity: Float): AdjustedColors {
        val baseColors = generateColors(intensity)
        val normalizedIntensity = intensity.coerceIn(0f, 2f)
        val saturationBoost = 1f + (normalizedIntensity * 0.5f)
        val brightnessBoost = 1f + (normalizedIntensity * 0.3f)
        
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
     * Get background gradient for a specific intensity
     */
    open fun getBackgroundGradient(intensity: Float): Brush {
        return when (val style = generateBackground(intensity)) {
            is BackgroundStyle.Gradient -> {
                val colors = if (intensity >= 1.5f && style.intenseColors != null) {
                    style.intenseColors
                } else {
                    style.colors
                }
                val adjustedColors = colors.map { 
                    boostColor(it, 1f + intensity * 0.3f, 1f) 
                }
                Brush.verticalGradient(adjustedColors)
            }
            is BackgroundStyle.AnimatedMesh -> {
                Brush.verticalGradient(style.baseColors)
            }
            is BackgroundStyle.Solid -> {
                Brush.linearGradient(listOf(style.color, style.color))
            }
        }
    }
    
    /**
     * Custom background composable
     * Override to provide a fully custom animated background
     */
    @Composable
    open fun AnimatedBackground(modifier: Modifier = Modifier, intensity: Float) {
        DefaultAnimatedBackground(modifier, intensity)
    }
    
    /**
     * Theme preview composable
     * Override to provide a custom preview
     */
    @Composable
    open fun Preview(modifier: Modifier = Modifier) {
        DefaultPreview(modifier)
    }
    
    /**
     * Convert to CandyTheme for use with ThemeManager
     */
    fun toCandyTheme(): CandyTheme {
        return CandyTheme(
            id = id,
            name = name,
            description = description,
            baseColors = generateColors(defaultIntensity),
            intensityConfig = IntensityConfig(
                minValue = minIntensity,
                maxValue = maxIntensity,
                defaultValue = defaultIntensity
            ),
            backgroundStyle = generateBackground(defaultIntensity),
            particleConfig = generateParticles(defaultIntensity),
            animationConfig = generateAnimations(defaultIntensity),
            isDark = isDark,
            category = category
        )
    }
    
    // Helper methods
    
    protected fun boostColor(color: Color, saturationMult: Float, brightnessMult: Float): Color {
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
    
    // Default implementations
    
    @Composable
    private fun DefaultAnimatedBackground(modifier: Modifier = Modifier, intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "bg")
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing)
            ),
            label = "offset"
        )
        
        Box(
            modifier = modifier.background(
                Brush.radialGradient(
                    colors = listOf(
                        generateColors(intensity).primary.copy(alpha = 0.3f),
                        generateColors(intensity).background
                    ),
                    center = Offset(
                        0.5f + 0.3f * sin(offset * 2 * PI.toFloat()),
                        0.5f + 0.3f * cos(offset * 2 * PI.toFloat())
                    ),
                    radius = 1000f
                )
            )
        )
    }
    
    @Composable
    private fun DefaultPreview(modifier: Modifier = Modifier) {
        val colors = remember { generateColors(1f) }
        
        Column(modifier = modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = colors.background
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.primary)
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.secondary)
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.tertiary)
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.accent)
                    )
                }
            }
        }
    }
}

// ========== EXAMPLE THEME IMPLEMENTATIONS ==========

/**
 * Cyberpunk Neon Theme - High-tech neon aesthetic
 */
class CyberpunkTheme : ThemePlugin(
    id = "cyberpunk_neon",
    name = "Cyberpunk Neon",
    description = "Neon-lit cyberpunk theme with electric blues and hot pinks",
    category = ThemeCategory.SUGARRUSH,
    isDark = true,
    defaultIntensity = 1.2f
) {
    override fun generateColors(intensity: Float): BaseColors {
        val boost = 1f + (intensity - 1f) * 0.3f
        return BaseColors(
            primary = Color(0xFF00FFFF).copy(red = (0f * boost).coerceIn(0f, 1f)),
            secondary = Color(0xFFFF00FF),
            tertiary = Color(0xFF7700FF),
            accent = Color(0xFFFF3366),
            surface = Color(0xFF0A0A1A),
            surfaceVariant = Color(0xFF151525),
            background = Color(0xFF050510),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFFE0E0FF),
            onBackground = Color(0xFFB0B0D0),
            error = Color(0xFFFF0055),
            success = Color(0xFF00FF88)
        )
    }
    
    override fun generateBackground(intensity: Float): BackgroundStyle {
        return BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFF050510),
                Color(0xFF0A0A20),
                Color(0xFF151530)
            ),
            intenseColors = listOf(
                Color(0xFF0A0020),
                Color(0xFF200040),
                Color(0xFF400060)
            )
        )
    }
    
    override fun generateParticles(intensity: Float): ParticleConfig {
        return ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF00FFFF),
                Color(0xFFFF00FF),
                Color(0xFF7700FF)
            ),
            count = (20 * intensity).toInt()..(50 * intensity).toInt(),
            speed = FloatRange(0.5f, 2f * intensity),
            size = FloatRange(1f, 4f),
            type = ParticleType.CHAOTIC,
            intensityMultiplier = intensity
        )
    }
    
    override fun generateAnimations(intensity: Float): AnimationConfig {
        return AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 1.5f * intensity,
            backgroundAnimationEnabled = true,
            transitionDuration = (300 / intensity).toInt().coerceAtLeast(100),
            staggerDelay = (50 / intensity).toInt().coerceAtLeast(20)
        )
    }
    
    @Composable
    override fun AnimatedBackground(modifier: Modifier, intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "cyberpunk")
        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = LinearEasing)
            ),
            label = "phase"
        )
        
        val gridOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing)
            ),
            label = "grid"
        )
        
        androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
            // Draw neon grid
            val gridColor = Color(0xFF00FFFF).copy(alpha = 0.1f * intensity)
            val gridSpacing = 50f
            
            for (i in 0..(size.width / gridSpacing).toInt() + 1) {
                val x = i * gridSpacing + gridOffset % gridSpacing
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }
            
            for (i in 0..(size.height / gridSpacing).toInt() + 1) {
                val y = i * gridSpacing
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
            
            // Draw pulsing orbs
            repeat(3) { i ->
                val x = size.width * (0.2f + i * 0.3f)
                val y = size.height * (0.3f + 0.1f * sin(phase + i))
                val radius = 30f + 10f * sin(phase * 2 + i)
                
                drawCircle(
                    color = when (i) {
                        0 -> Color(0xFF00FFFF).copy(alpha = 0.3f * intensity)
                        1 -> Color(0xFFFF00FF).copy(alpha = 0.3f * intensity)
                        else -> Color(0xFF7700FF).copy(alpha = 0.3f * intensity)
                    },
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

/**
 * Nature Theme - Organic greens and earth tones
 */
class NatureTheme : ThemePlugin(
    id = "nature_organic",
    name = "Nature",
    description = "Calming organic theme with forest greens and earth tones",
    category = ThemeCategory.CHILL,
    isDark = false,
    defaultIntensity = 0.8f
) {
    override fun generateColors(intensity: Float): BaseColors {
        return BaseColors(
            primary = Color(0xFF4CAF50),
            secondary = Color(0xFF8BC34A),
            tertiary = Color(0xFF795548),
            accent = Color(0xFFFF9800),
            surface = Color(0xFFF5F5DC),
            surfaceVariant = Color(0xFFE8E8D0),
            background = Color(0xFFFAFAF0),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF3E2723),
            onBackground = Color(0xFF4E342E),
            error = Color(0xFFE53935),
            success = Color(0xFF43A047)
        )
    }
    
    override fun generateBackground(intensity: Float): BackgroundStyle {
        return BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFAFAF0),
                Color(0xFFF0F8E8),
                Color(0xFFE8F5E0)
            ),
            intenseColors = listOf(
                Color(0xFFF5F5DC),
                Color(0xFFE8F5D0),
                Color(0xFFD0E8C0)
            )
        )
    }
    
    override fun generateParticles(intensity: Float): ParticleConfig {
        return ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFF4CAF50).copy(alpha = 0.5f),
                Color(0xFF8BC34A).copy(alpha = 0.5f),
                Color(0xFFCDDC39).copy(alpha = 0.5f)
            ),
            count = (15 * intensity).toInt()..(40 * intensity).toInt(),
            speed = FloatRange(0.2f, 0.8f * intensity),
            size = FloatRange(3f, 8f),
            type = ParticleType.FLOATING,
            intensityMultiplier = intensity
        )
    }
    
    override fun generateAnimations(intensity: Float): AnimationConfig {
        return AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.5f,
            backgroundAnimationEnabled = true,
            transitionDuration = (500 / intensity.coerceAtLeast(0.5f)).toInt(),
            staggerDelay = (80 / intensity.coerceAtLeast(0.5f)).toInt()
        )
    }
    
    @Composable
    override fun AnimatedBackground(modifier: Modifier, intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "nature")
        val sway by infiniteTransition.animateFloat(
            initialValue = -0.1f,
            targetValue = 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sway"
        )
        
        androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
            // Draw leaves swaying
            repeat(8) { i ->
                val baseX = size.width * (0.1f + i * 0.11f)
                val baseY = size.height * (0.1f + (i % 3) * 0.3f)
                val leafSway = sway * (1f + i * 0.2f)
                
                drawCircle(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f * intensity),
                    radius = 20f + i * 5f,
                    center = Offset(baseX + leafSway * 50, baseY)
                )
            }
        }
    }
}

/**
 * Sunset Theme - Warm oranges, pinks, and purples
 */
class SunsetTheme : ThemePlugin(
    id = "sunset_gradient",
    name = "Sunset",
    description = "Warm sunset gradients with golden hour vibes",
    category = ThemeCategory.CLASSIC,
    isDark = false,
    defaultIntensity = 1f
) {
    override fun generateColors(intensity: Float): BaseColors {
        return BaseColors(
            primary = Color(0xFFFF7043),
            secondary = Color(0xFFFFAB91),
            tertiary = Color(0xFFFFCC80),
            accent = Color(0xFFBA68C8),
            surface = Color(0xFFFFF3E0),
            surfaceVariant = Color(0xFFFFE0B2),
            background = Color(0xFFFFF8E7),
            onPrimary = Color(0xFF000000),
            onSurface = Color(0xFF3E2723),
            onBackground = Color(0xFF4E342E),
            error = Color(0xFFE53935),
            success = Color(0xFF66BB6A)
        )
    }
    
    override fun generateBackground(intensity: Float): BackgroundStyle {
        return BackgroundStyle.Gradient(
            colors = listOf(
                Color(0xFFFFF8E7),
                Color(0xFFFFE0B2),
                Color(0xFFFFCC80)
            ),
            intenseColors = listOf(
                Color(0xFFFFE0B2),
                Color(0xFFFFAB91),
                Color(0xFFFF8A65)
            )
        )
    }
    
    override fun generateParticles(intensity: Float): ParticleConfig {
        return ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF7043).copy(alpha = 0.4f),
                Color(0xFFFFAB91).copy(alpha = 0.4f),
                Color(0xFFFFCC80).copy(alpha = 0.4f)
            ),
            count = (20 * intensity).toInt()..(50 * intensity).toInt(),
            speed = FloatRange(0.3f, 1f * intensity),
            size = FloatRange(2f, 6f),
            type = ParticleType.FLOATING,
            intensityMultiplier = intensity
        )
    }
    
    override fun generateAnimations(intensity: Float): AnimationConfig {
        return AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.8f * intensity,
            backgroundAnimationEnabled = true,
            transitionDuration = 400,
            staggerDelay = 60
        )
    }
    
    @Composable
    override fun AnimatedBackground(modifier: Modifier, intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "sunset")
        val sunPosition by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing)
            ),
            label = "sun"
        )
        
        val colorShift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing)
            ),
            label = "color"
        )
        
        Box(
            modifier = modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color.hsv(
                            (20f + colorShift * 0.1f) % 360f, 0.8f, 1f, 0.3f * intensity
                        ),
                        Color(0xFFFFF8E7).copy(alpha = 0.9f)
                    )
                )
            )
        )
    }
}

/**
 * Monochrome Theme - Black and white with grayscale
 */
class MonochromeTheme : ThemePlugin(
    id = "monochrome_minimal",
    name = "Monochrome",
    description = "Clean black and white minimal theme",
    category = ThemeCategory.CHILL,
    isDark = false,
    defaultIntensity = 0.7f
) {
    override fun generateColors(intensity: Float): BaseColors {
        val grayValue = (0.3f + intensity * 0.2f).coerceIn(0f, 1f)
        return BaseColors(
            primary = Color(0xFF212121),
            secondary = Color(0xFF616161),
            tertiary = Color(0xFF9E9E9E),
            accent = Color(0xFFE0E0E0),
            surface = Color(0xFFFAFAFA),
            surfaceVariant = Color(0xFFF5F5F5),
            background = Color(0xFFFFFFFF),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF212121),
            onBackground = Color(0xFF424242),
            error = Color(0xFF424242),
            success = Color(0xFF616161)
        )
    }
    
    override fun generateBackground(intensity: Float): BackgroundStyle {
        return BackgroundStyle.Solid(Color.White)
    }
    
    override fun generateParticles(intensity: Float): ParticleConfig {
        return ParticleConfig(
            enabled = false,
            colors = emptyList(),
            count = 0..0,
            speed = FloatRange(0f, 0f),
            size = FloatRange(0f, 0f),
            type = ParticleType.FLOATING,
            intensityMultiplier = 0f
        )
    }
    
    override fun generateAnimations(intensity: Float): AnimationConfig {
        return AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0f,
            backgroundAnimationEnabled = false,
            transitionDuration = 200,
            staggerDelay = 30
        )
    }
}
