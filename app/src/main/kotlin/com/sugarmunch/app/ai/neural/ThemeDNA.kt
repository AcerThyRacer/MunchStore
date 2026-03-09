package com.sugarmunch.app.ai.neural

import android.graphics.Color
import android.util.Base64
import androidx.core.graphics.ColorUtils
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.model.ParticleConfig
import com.sugarmunch.app.theme.model.ThemeColors
import com.sugarmunch.app.theme.model.GradientSpec
import com.sugarmunch.app.theme.model.GradientDirection
import com.sugarmunch.app.theme.model.FloatRange
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Theme DNA - Shareable and mixable theme genetic code
 * 
 * Encodes all theme properties into a shareable format
 * that can be mixed, mutated, and evolved
 */
@Serializable
data class ThemeDNA(
    val id: String,
    val colorGenes: List<ColorGene>,
    val styleGenes: List<StyleGene>,
    val particleGenes: List<ParticleGene>,
    val gradientGenes: List<GradientGene>,
    val metadata: ThemeDNAMetadata
) {
    /**
     * Convert theme to DNA
     */
    constructor(theme: CandyTheme, colorPalette: WallpaperColorPalette) : this(
        id = "dna_${UUID.randomUUID()}",
        colorGenes = ColorGene.fromColors(colorPalette.dominantColors),
        styleGenes = StyleGene.fromTheme(theme),
        particleGenes = ParticleGene.fromParticleConfig(theme.particleConfig),
        gradientGenes = GradientGene.fromGradient(theme.themeGradient),
        metadata = ThemeDNAMetadata(
            name = theme.name,
            description = theme.description,
            createdAt = System.currentTimeMillis(),
            category = theme.category.name,
            isDark = theme.isDark
        )
    )

    /**
     * Convert DNA back to CandyTheme
     */
    fun toTheme(): CandyTheme {
        val colors = colorGenes.toThemeColors()
        val gradient = gradientGenes.firstOrNull()?.toGradientSpec() ?: GradientSpec(
            colors = listOf("#000000", "#333333"),
            startOffset = listOf(0f, 1f),
            direction = GradientDirection.VERTICAL
        )

        val particleConfig = particleGenes.firstOrNull()?.toParticleConfig() ?: ParticleConfig(
            count = 20..50,
            speed = FloatRange(0.5f, 2f),
            colors = colorGenes.map { it.toHexColor() }
        )

        return CandyTheme(
            id = id,
            name = metadata.name,
            description = metadata.description ?: "AI-generated theme",
            category = com.sugarmunch.app.theme.model.ThemeCategory.valueOf(metadata.category),
            isDark = metadata.isDark,
            colors = colors,
            themeGradient = gradient,
            particleConfig = particleConfig
        )
    }

    /**
     * Export to Base64 string for sharing
     */
    fun toBase64(): String {
        val json = Json.encodeToString(this)
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Import from Base64 string
     */
    companion object {
        fun fromBase64(base64: String): ThemeDNA {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val json = String(bytes, StandardCharsets.UTF_8)
            return Json.decodeFromString(json)
        }

        fun fromTheme(theme: CandyTheme, colorPalette: WallpaperColorPalette): ThemeDNA {
            return ThemeDNA(theme, colorPalette)
        }
    }
}

/**
 * Color gene - Encodes a single color's properties
 */
@Serializable
data class ColorGene(
    val hue: Float,
    val saturation: Float,
    val lightness: Float,
    val alpha: Float = 1f,
    val weight: Float = 1f,
    val temperature: Float = 0f, // -1 (cool) to 1 (warm)
    val vibrancy: Float = 0.5f
) {
    fun toColor(): Int {
        val hsv = floatArrayOf(hue, saturation, lightness)
        return Color.HSVToColor((alpha * 255).toInt(), hsv)
    }

    fun toHexColor(): String {
        return ColorUtils.colorToHex(toColor())
    }

    companion object {
        fun fromColor(color: Int): ColorGene {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)

            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(color, hsl)

            return ColorGene(
                hue = hsv[0],
                saturation = hsv[1],
                lightness = hsl[2],
                alpha = Color.alpha(color) / 255f,
                temperature = calculateTemperature(hsv[0]),
                vibrancy = hsv[1]
            )
        }

        fun fromColors(colors: List<Int>): List<ColorGene> {
            return colors.map { fromColor(it) }
        }

        private fun calculateTemperature(hue: Float): Float {
            return when {
                hue < 60 -> 0.8f // Warm (red-yellow)
                hue < 180 -> -0.5f // Cool (green-cyan)
                hue < 270 -> -0.8f // Cool (blue)
                else -> 0.5f // Warm (purple-red)
            }
        }
    }
}

/**
 * Style gene - Encodes visual style properties
 */
@Serializable
data class StyleGene(
    val type: String, // MINIMAL, GLASSMORPHIC, NEUMORPHIC, etc.
    val intensity: Float,
    val variation: Float,
    val borderRadius: Float,
    val shadowIntensity: Float,
    val glowIntensity: Float,
    val textureType: String = "NONE"
) {
    companion object {
        fun fromTheme(theme: CandyTheme): List<StyleGene> {
            return listOf(
                StyleGene(
                    type = inferStyleType(theme),
                    intensity = 0.7f,
                    variation = 0.3f,
                    borderRadius = 0.5f,
                    shadowIntensity = 0.4f,
                    glowIntensity = 0.2f
                )
            )
        }

        private fun inferStyleType(theme: CandyTheme): String {
            return when {
                theme.isDark -> "GLASSMORPHIC"
                theme.colors.primary.let { ColorUtils.calculateLuminance(it) > 0.7 } -> "NEUMORPHIC"
                else -> "MINIMAL"
            }
        }
    }
}

/**
 * Particle gene - Encodes particle system properties
 */
@Serializable
data class ParticleGene(
    val shape: String, // CIRCLE, SQUARE, TRIANGLE, STAR
    val size: Float,
    val speed: Float,
    val density: Float,
    val opacity: Float,
    val behavior: String, // FLOAT, BOUNCE, ORBIT, CHASE
    val colorVariation: Float
) {
    fun toParticleConfig(): ParticleConfig {
        val countRange = when (density) {
            in 0f..0.3f -> 10..30
            in 0.3f..0.6f -> 30..60
            else -> 60..100
        }

        val speedRange = FloatRange(
            min = speed * 0.5f,
            max = speed * 1.5f
        )

        return ParticleConfig(
            count = countRange,
            speed = speedRange,
            colors = listOf("#FFFFFF", "#CCCCCC", "#888888")
        )
    }

    companion object {
        fun fromParticleConfig(config: ParticleConfig): List<ParticleGene> {
            val avgCount = (config.count.first + config.count.last) / 2f
            val avgSpeed = (config.speed.min + config.speed.max) / 2f

            return listOf(
                ParticleGene(
                    shape = "CIRCLE",
                    size = 0.5f,
                    speed = avgSpeed,
                    density = avgCount / 100f,
                    opacity = 0.7f,
                    behavior = "FLOAT",
                    colorVariation = 0.3f
                )
            )
        }
    }
}

/**
 * Gradient gene - Encodes gradient properties
 */
@Serializable
data class GradientGene(
    val angle: Float,
    val colorStops: List<String>,
    val animationSpeed: Float,
    val animationType: String, // STATIC, PULSE, FLOW, SPARKLE
    val blendMode: String // NORMAL, MULTIPLY, SCREEN, OVERLAY
) {
    fun toGradientSpec(): GradientSpec {
        return GradientSpec(
            colors = colorStops,
            startOffset = colorStops.indices.map { it.toFloat() / (colorStops.size - 1) },
            direction = angleToDirection(angle)
        )
    }

    private fun angleToDirection(angle: Float): GradientDirection {
        return when {
            angle < 45 -> GradientDirection.HORIZONTAL
            angle < 135 -> GradientDirection.VERTICAL
            angle < 225 -> GradientDirection.DIAGONAL_TOP_LEFT
            else -> GradientDirection.DIAGONAL_TOP_RIGHT
        }
    }

    companion object {
        fun fromGradient(gradient: GradientSpec?): List<GradientGene> {
            if (gradient == null) {
                return listOf(
                    GradientGene(
                        angle = 90f,
                        colorStops = listOf("#000000", "#333333"),
                        animationSpeed = 0f,
                        animationType = "STATIC",
                        blendMode = "NORMAL"
                    )
                )
            }

            val angle = when (gradient.direction) {
                GradientDirection.HORIZONTAL -> 0f
                GradientDirection.VERTICAL -> 90f
                GradientDirection.DIAGONAL_TOP_LEFT -> 135f
                GradientDirection.DIAGONAL_TOP_RIGHT -> 45f
                GradientDirection.RADIAL -> 0f
            }

            return listOf(
                GradientGene(
                    angle = angle,
                    colorStops = gradient.colors,
                    animationSpeed = 0f,
                    animationType = "STATIC",
                    blendMode = "NORMAL"
                )
            )
        }
    }
}

/**
 * Theme DNA metadata
 */
@Serializable
data class ThemeDNAMetadata(
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val parentDNA: List<String> = emptyList(),
    val generation: Int = 1,
    val category: String = "CUSTOM",
    val isDark: Boolean = false,
    val tags: List<String> = emptyList(),
    val author: String? = null,
    val version: Int = 1
)

/**
 * Extension to convert ColorGenes to ThemeColors
 */
fun List<ColorGene>.toThemeColors(): ThemeColors {
    if (isEmpty()) {
        return ThemeColors.DEFAULT
    }

    val primary = firstOrNull()?.toColor() ?: Color.BLACK
    val secondary = getOrNull(1)?.toColor() ?: primary
    val tertiary = getOrNull(2)?.toColor() ?: secondary

    return ThemeColors(
        primary = primary,
        onPrimary = getContrastingColor(primary),
        secondary = secondary,
        onSecondary = getContrastingColor(secondary),
        tertiary = tertiary,
        onTertiary = getContrastingColor(tertiary),
        background = primary,
        onBackground = getContrastingColor(primary),
        surface = ColorUtils.setAlphaComponent(secondary, 128),
        onSurface = getContrastingColor(secondary),
        error = Color.RED,
        onError = Color.WHITE
    )
}

private fun getContrastingColor(color: Int): Int {
    return if (ColorUtils.calculateLuminance(color) > 0.5) Color.BLACK else Color.WHITE
}

/**
 * DNA mutation for evolution
 */
fun ThemeDNA.mutate(strength: Float = 0.1f): ThemeDNA {
    val mutatedColorGenes = colorGenes.map { gene ->
        gene.copy(
            hue = (gene.hue + (Math.random().toFloat() - 0.5f) * 360f * strength) % 360f,
            saturation = (gene.saturation + (Math.random().toFloat() - 0.5f) * strength).coerceIn(0f, 1f),
            lightness = (gene.lightness + (Math.random().toFloat() - 0.5f) * strength).coerceIn(0f, 1f)
        )
    }

    return copy(
        colorGenes = mutatedColorGenes,
        metadata = metadata.copy(
            name = "${metadata.name} (Mutated)",
            version = version + 1
        )
    )
}

/**
 * DNA crossover for breeding two themes
 */
fun ThemeDNA.crossoverWith(other: ThemeDNA, crossoverPoint: Float = 0.5f): ThemeDNA {
    val splitIndex = (colorGenes.size * crossoverPoint).toInt()

    val newColorGenes = colorGenes.take(splitIndex) + other.colorGenes.drop(splitIndex)
    val newStyleGenes = if (Math.random() > 0.5) styleGenes else other.styleGenes
    val newParticleGenes = if (Math.random() > 0.5) particleGenes else other.particleGenes
    val newGradientGenes = if (Math.random() > 0.5) gradientGenes else other.gradientGenes

    return copy(
        colorGenes = newColorGenes,
        styleGenes = newStyleGenes,
        particleGenes = newParticleGenes,
        gradientGenes = newGradientGenes,
        metadata = metadata.copy(
            name = "${metadata.name} × ${other.metadata.name}",
            parentDNA = listOf(id, other.id),
            generation = maxOf(metadata.generation, other.metadata.generation) + 1,
            version = 1
        )
    )
}
