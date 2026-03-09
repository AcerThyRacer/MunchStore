package com.sugarmunch.app.theme.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * SugarRush Hyper-Granular Theme Configuration
 * 20+ independent controls for maximum customization
 */
@Serializable
data class GranularThemeConfig(
    // ═════════════════════════════════════════════════════════════
    // COLOR CUSTOMIZATION (8 sliders)
    // ═════════════════════════════════════════════════════════════
    
    /** Primary color saturation (0-200%, default 100%) */
    val primarySaturation: Float = 1f,
    /** Primary color brightness (0-200%, default 100%) */
    val primaryBrightness: Float = 1f,
    /** Secondary color saturation (0-200%, default 100%) */
    val secondarySaturation: Float = 1f,
    /** Secondary color brightness (0-200%, default 100%) */
    val secondaryBrightness: Float = 1f,
    /** Accent color saturation (0-200%, default 100%) */
    val accentSaturation: Float = 1f,
    /** Accent color brightness (0-200%, default 100%) */
    val accentBrightness: Float = 1f,
    /** Surface color warmth (-100% cool to +100% warm, default 0%) */
    val surfaceWarmth: Float = 0f,
    /** Background contrast (0-200%, default 100%) */
    val backgroundContrast: Float = 1f,
    
    // ═════════════════════════════════════════════════════════════
    // GRADIENT CUSTOMIZATION (6 sliders)
    // ═════════════════════════════════════════════════════════════
    
    /** Gradient angle in degrees (0-360°, default 90°) */
    val gradientAngle: Float = 90f,
    /** Gradient spread (0-200%, default 100%) */
    val gradientSpread: Float = 1f,
    /** Gradient smoothness (0-200%, default 100%) */
    val gradientSmoothness: Float = 1f,
    /** Gradient horizontal offset (-100% to +100%, default 0%) */
    val gradientOffsetX: Float = 0f,
    /** Gradient vertical offset (-100% to +100%, default 0%) */
    val gradientOffsetY: Float = 0f,
    /** Gradient animation speed (0-300%, default 100%) */
    val gradientAnimationSpeed: Float = 1f,
    
    // ═════════════════════════════════════════════════════════════
    // PARTICLE CUSTOMIZATION (4 sliders)
    // ═════════════════════════════════════════════════════════════
    
    /** Particle size (0-300%, default 100%) */
    val particleSize: Float = 1f,
    /** Particle density (0-500%, default 100%) */
    val particleDensity: Float = 1f,
    /** Particle opacity (0-100%, default 100%) */
    val particleOpacity: Float = 1f,
    /** Particle physics intensity (0-200%, default 100%) */
    val particlePhysics: Float = 1f,
    
    // ═════════════════════════════════════════════════════════════
    // ANIMATION CUSTOMIZATION (4 sliders)
    // ═════════════════════════════════════════════════════════════
    
    /** Overall animation speed (0-300%, default 100%) */
    val animationSpeed: Float = 1f,
    /** Animation smoothness/interpolation (0-200%, default 100%) */
    val animationSmoothness: Float = 1f,
    /** Transition duration (0-500%, default 100%) */
    val transitionDuration: Float = 1f,
    /** Stagger delay between elements (0-500%, default 100%) */
    val staggerDelay: Float = 1f,
    
    // ═════════════════════════════════════════════════════════════
    // ADVANCED CUSTOMIZATION (4 sliders)
    // ═════════════════════════════════════════════════════════════
    
    /** Bloom/glow intensity (0-200%, default 100%) */
    val bloomIntensity: Float = 1f,
    /** Color vibrance (0-200%, default 100%) */
    val colorVibrance: Float = 1f,
    /** Shadow depth (0-200%, default 100%) */
    val shadowDepth: Float = 1f,
    /** Highlight intensity (0-200%, default 100%) */
    val highlightIntensity: Float = 1f
) {
    companion object {
        val DEFAULT = GranularThemeConfig()
        
        /** Preset configurations */
        val MINIMAL = GranularThemeConfig(
            primarySaturation = 0.5f,
            primaryBrightness = 0.7f,
            secondarySaturation = 0.5f,
            particleDensity = 0.2f,
            particleOpacity = 0.3f,
            animationSpeed = 0.5f,
            bloomIntensity = 0.3f
        )
        
        val BALANCED = GranularThemeConfig(
            primarySaturation = 1f,
            primaryBrightness = 1f,
            gradientSmoothness = 1f,
            particleDensity = 1f,
            animationSpeed = 1f
        )
        
        val INTENSE = GranularThemeConfig(
            primarySaturation = 1.5f,
            primaryBrightness = 1.3f,
            secondarySaturation = 1.5f,
            accentSaturation = 1.5f,
            gradientSpread = 1.3f,
            particleDensity = 1.5f,
            particleSize = 1.3f,
            animationSpeed = 1.5f,
            bloomIntensity = 1.5f,
            colorVibrance = 1.5f
        )
        
        val MAXIMUM = GranularThemeConfig(
            primarySaturation = 2f,
            primaryBrightness = 1.5f,
            secondarySaturation = 2f,
            secondaryBrightness = 1.5f,
            accentSaturation = 2f,
            accentBrightness = 1.5f,
            gradientSpread = 1.5f,
            gradientAnimationSpeed = 2f,
            particleDensity = 3f,
            particleSize = 2f,
            particleOpacity = 1f,
            particlePhysics = 1.5f,
            animationSpeed = 2f,
            animationSmoothness = 1.5f,
            bloomIntensity = 2f,
            colorVibrance = 2f,
            highlightIntensity = 1.8f
        )
    }
    
    /**
     * Apply color adjustments to a base color
     */
    fun applyColorAdjustments(
        color: Color,
        saturationMult: Float = 1f,
        brightnessMult: Float = 1f,
        warmth: Float = 0f
    ): Color {
        val hsl = color.toHsl()
        var h = hsl[0]
        var s = hsl[1]
        var l = hsl[2]
        
        // Apply saturation
        s = (s * saturationMult).coerceIn(0f, 1f)
        
        // Apply brightness
        l = (l * brightnessMult).coerceIn(0f, 1f)
        
        // Apply warmth (shift hue toward orange/red)
        if (warmth > 0) {
            h = (h + warmth * 30f) % 360f
        } else if (warmth < 0) {
            h = (h + warmth * 30f + 360f) % 360f
        }
        
        return Color.hsl(h, s, l, color.alpha)
    }
    
    /**
     * Get adjusted gradient colors based on configuration
     */
    fun getAdjustedGradientColors(baseColors: List<Color>): List<Color> {
        return baseColors.map { color ->
            applyColorAdjustments(
                color,
                saturationMult = colorVibrance,
                brightnessMult = highlightIntensity
            )
        }
    }
    
    /**
     * Get adjusted particle config based on granular settings
     */
    fun getAdjustedParticleConfig(baseConfig: ParticleConfig): ParticleConfig {
        return baseConfig.copy(
            count = (baseConfig.count.first * particleDensity).toInt()..
                    (baseConfig.count.last * particleDensity).toInt(),
            speed = FloatRange(
                baseConfig.speed.min * animationSpeed,
                baseConfig.speed.max * animationSpeed
            ),
            size = FloatRange(
                baseConfig.size.min * particleSize,
                baseConfig.size.max * particleSize
            )
        )
    }
    
    /**
     * Get adjusted animation config based on granular settings
     */
    fun getAdjustedAnimationConfig(baseConfig: AnimationConfig): AnimationConfig {
        return baseConfig.copy(
            cardPulseSpeed = baseConfig.cardPulseSpeed * animationSpeed,
            transitionDuration = (baseConfig.transitionDuration * transitionDuration).toInt(),
            staggerDelay = (baseConfig.staggerDelay * staggerDelay).toInt()
        )
    }
    
    /**
     * Merge with another config (override specific values)
     */
    fun merge(other: GranularThemeConfig): GranularThemeConfig {
        return GranularThemeConfig(
            primarySaturation = other.primarySaturation.takeIf { it != 1f } ?: primarySaturation,
            primaryBrightness = other.primaryBrightness.takeIf { it != 1f } ?: primaryBrightness,
            secondarySaturation = other.secondarySaturation.takeIf { it != 1f } ?: secondarySaturation,
            secondaryBrightness = other.secondaryBrightness.takeIf { it != 1f } ?: secondaryBrightness,
            accentSaturation = other.accentSaturation.takeIf { it != 1f } ?: accentSaturation,
            accentBrightness = other.accentBrightness.takeIf { it != 1f } ?: accentBrightness,
            surfaceWarmth = other.surfaceWarmth.takeIf { it != 0f } ?: surfaceWarmth,
            backgroundContrast = other.backgroundContrast.takeIf { it != 1f } ?: backgroundContrast,
            gradientAngle = other.gradientAngle.takeIf { it != 90f } ?: gradientAngle,
            gradientSpread = other.gradientSpread.takeIf { it != 1f } ?: gradientSpread,
            gradientSmoothness = other.gradientSmoothness.takeIf { it != 1f } ?: gradientSmoothness,
            gradientOffsetX = other.gradientOffsetX.takeIf { it != 0f } ?: gradientOffsetX,
            gradientOffsetY = other.gradientOffsetY.takeIf { it != 0f } ?: gradientOffsetY,
            gradientAnimationSpeed = other.gradientAnimationSpeed.takeIf { it != 1f } ?: gradientAnimationSpeed,
            particleSize = other.particleSize.takeIf { it != 1f } ?: particleSize,
            particleDensity = other.particleDensity.takeIf { it != 1f } ?: particleDensity,
            particleOpacity = other.particleOpacity.takeIf { it != 1f } ?: particleOpacity,
            particlePhysics = other.particlePhysics.takeIf { it != 1f } ?: particlePhysics,
            animationSpeed = other.animationSpeed.takeIf { it != 1f } ?: animationSpeed,
            animationSmoothness = other.animationSmoothness.takeIf { it != 1f } ?: animationSmoothness,
            transitionDuration = other.transitionDuration.takeIf { it != 1f } ?: transitionDuration,
            staggerDelay = other.staggerDelay.takeIf { it != 1f } ?: staggerDelay,
            bloomIntensity = other.bloomIntensity.takeIf { it != 1f } ?: bloomIntensity,
            colorVibrance = other.colorVibrance.takeIf { it != 1f } ?: colorVibrance,
            shadowDepth = other.shadowDepth.takeIf { it != 1f } ?: shadowDepth,
            highlightIntensity = other.highlightIntensity.takeIf { it != 1f } ?: highlightIntensity
        )
    }
    
    /**
     * Reset specific category to defaults
     */
    fun resetCategory(category: GranularCategory): GranularThemeConfig {
        return when (category) {
            GranularCategory.COLORS -> copy(
                primarySaturation = 1f, primaryBrightness = 1f,
                secondarySaturation = 1f, secondaryBrightness = 1f,
                accentSaturation = 1f, accentBrightness = 1f,
                surfaceWarmth = 0f, backgroundContrast = 1f
            )
            GranularCategory.GRADIENT -> copy(
                gradientAngle = 90f, gradientSpread = 1f, gradientSmoothness = 1f,
                gradientOffsetX = 0f, gradientOffsetY = 0f, gradientAnimationSpeed = 1f
            )
            GranularCategory.PARTICLES -> copy(
                particleSize = 1f, particleDensity = 1f,
                particleOpacity = 1f, particlePhysics = 1f
            )
            GranularCategory.ANIMATIONS -> copy(
                animationSpeed = 1f, animationSmoothness = 1f,
                transitionDuration = 1f, staggerDelay = 1f
            )
            GranularCategory.ADVANCED -> copy(
                bloomIntensity = 1f, colorVibrance = 1f,
                shadowDepth = 1f, highlightIntensity = 1f
            )
        }
    }
    
    /**
     * Get all values as a map for easy iteration
     */
    fun toMap(): Map<String, Float> = mapOf(
        "primarySaturation" to primarySaturation,
        "primaryBrightness" to primaryBrightness,
        "secondarySaturation" to secondarySaturation,
        "secondaryBrightness" to secondaryBrightness,
        "accentSaturation" to accentSaturation,
        "accentBrightness" to accentBrightness,
        "surfaceWarmth" to surfaceWarmth,
        "backgroundContrast" to backgroundContrast,
        "gradientAngle" to gradientAngle,
        "gradientSpread" to gradientSpread,
        "gradientSmoothness" to gradientSmoothness,
        "gradientOffsetX" to gradientOffsetX,
        "gradientOffsetY" to gradientOffsetY,
        "gradientAnimationSpeed" to gradientAnimationSpeed,
        "particleSize" to particleSize,
        "particleDensity" to particleDensity,
        "particleOpacity" to particleOpacity,
        "particlePhysics" to particlePhysics,
        "animationSpeed" to animationSpeed,
        "animationSmoothness" to animationSmoothness,
        "transitionDuration" to transitionDuration,
        "staggerDelay" to staggerDelay,
        "bloomIntensity" to bloomIntensity,
        "colorVibrance" to colorVibrance,
        "shadowDepth" to shadowDepth,
        "highlightIntensity" to highlightIntensity
    )
    
    /**
     * Create config from map (for deserialization)
     */
    companion object {
        fun fromMap(map: Map<String, Float>): GranularThemeConfig {
            return GranularThemeConfig(
                primarySaturation = map["primarySaturation"] ?: 1f,
                primaryBrightness = map["primaryBrightness"] ?: 1f,
                secondarySaturation = map["secondarySaturation"] ?: 1f,
                secondaryBrightness = map["secondaryBrightness"] ?: 1f,
                accentSaturation = map["accentSaturation"] ?: 1f,
                accentBrightness = map["accentBrightness"] ?: 1f,
                surfaceWarmth = map["surfaceWarmth"] ?: 0f,
                backgroundContrast = map["backgroundContrast"] ?: 1f,
                gradientAngle = map["gradientAngle"] ?: 90f,
                gradientSpread = map["gradientSpread"] ?: 1f,
                gradientSmoothness = map["gradientSmoothness"] ?: 1f,
                gradientOffsetX = map["gradientOffsetX"] ?: 0f,
                gradientOffsetY = map["gradientOffsetY"] ?: 0f,
                gradientAnimationSpeed = map["gradientAnimationSpeed"] ?: 1f,
                particleSize = map["particleSize"] ?: 1f,
                particleDensity = map["particleDensity"] ?: 1f,
                particleOpacity = map["particleOpacity"] ?: 1f,
                particlePhysics = map["particlePhysics"] ?: 1f,
                animationSpeed = map["animationSpeed"] ?: 1f,
                animationSmoothness = map["animationSmoothness"] ?: 1f,
                transitionDuration = map["transitionDuration"] ?: 1f,
                staggerDelay = map["staggerDelay"] ?: 1f,
                bloomIntensity = map["bloomIntensity"] ?: 1f,
                colorVibrance = map["colorVibrance"] ?: 1f,
                shadowDepth = map["shadowDepth"] ?: 1f,
                highlightIntensity = map["highlightIntensity"] ?: 1f
            )
        }
    }
}

/**
 * Categories for granular controls
 */
enum class GranularCategory(val displayName: String, val icon: String) {
    COLORS("Colors", "🎨"),
    GRADIENT("Gradient", "🌈"),
    PARTICLES("Particles", "✨"),
    ANIMATIONS("Animations", "🎬"),
    ADVANCED("Advanced", "⚡")
}

/**
 * Slider configuration for UI
 */
data class GranularSliderConfig(
    val key: String,
    val label: String,
    val icon: String,
    val category: GranularCategory,
    val min: Float,
    val max: Float,
    val step: Float = 0.1f,
    val defaultValue: Float,
    val unit: String = "",
    val description: String
)

/**
 * All available granular sliders with metadata
 */
object GranularSliderConfigs {
    val ALL_SLIDERS = listOf(
        // Colors
        GranularSliderConfig(
            key = "primarySaturation",
            label = "Primary Saturation",
            icon = "🎨",
            category = GranularCategory.COLORS,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Intensity of primary color"
        ),
        GranularSliderConfig(
            key = "primaryBrightness",
            label = "Primary Brightness",
            icon = "💡",
            category = GranularCategory.COLORS,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Brightness of primary color"
        ),
        GranularSliderConfig(
            key = "secondarySaturation",
            label = "Secondary Saturation",
            icon = "🎨",
            category = GranularCategory.COLORS,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Intensity of secondary color"
        ),
        GranularSliderConfig(
            key = "secondaryBrightness",
            label = "Secondary Brightness",
            icon = "💡",
            category = GranularCategory.COLORS,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Brightness of secondary color"
        ),
        GranularSliderConfig(
            key = "accentSaturation",
            label = "Accent Saturation",
            icon = "🎨",
            category = GranularCategory.COLORS,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Intensity of accent color"
        ),
        GranularSliderConfig(
            key = "accentBrightness",
            label = "Accent Brightness",
            icon = "💡",
            category = GranularCategory.COLORS,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Brightness of accent color"
        ),
        GranularSliderConfig(
            key = "surfaceWarmth",
            label = "Surface Warmth",
            icon = "🌡️",
            category = GranularCategory.COLORS,
            min = -1f, max = 1f, defaultValue = 0f,
            unit = "",
            description = "Shift surface colors warm or cool"
        ),
        GranularSliderConfig(
            key = "backgroundContrast",
            label = "Background Contrast",
            icon = "◐",
            category = GranularCategory.COLORS,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Contrast of background elements"
        ),
        
        // Gradient
        GranularSliderConfig(
            key = "gradientAngle",
            label = "Gradient Angle",
            icon = "📐",
            category = GranularCategory.GRADIENT,
            min = 0f, max = 360f, defaultValue = 90f,
            step = 1f,
            unit = "°",
            description = "Direction of gradient"
        ),
        GranularSliderConfig(
            key = "gradientSpread",
            label = "Gradient Spread",
            icon = "↔️",
            category = GranularCategory.GRADIENT,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Spread of gradient colors"
        ),
        GranularSliderConfig(
            key = "gradientSmoothness",
            label = "Gradient Smoothness",
            icon = "〰️",
            category = GranularCategory.GRADIENT,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Smoothness of color transitions"
        ),
        GranularSliderConfig(
            key = "gradientOffsetX",
            label = "Gradient X Offset",
            icon = "↔️",
            category = GranularCategory.GRADIENT,
            min = -1f, max = 1f, defaultValue = 0f,
            unit = "%",
            description = "Horizontal position of gradient"
        ),
        GranularSliderConfig(
            key = "gradientOffsetY",
            label = "Gradient Y Offset",
            icon = "↕️",
            category = GranularCategory.GRADIENT,
            min = -1f, max = 1f, defaultValue = 0f,
            unit = "%",
            description = "Vertical position of gradient"
        ),
        GranularSliderConfig(
            key = "gradientAnimationSpeed",
            label = "Gradient Animation",
            icon = "🎬",
            category = GranularCategory.GRADIENT,
            min = 0f, max = 3f, defaultValue = 1f,
            unit = "%",
            description = "Speed of animated gradients"
        ),
        
        // Particles
        GranularSliderConfig(
            key = "particleSize",
            label = "Particle Size",
            icon = "🔵",
            category = GranularCategory.PARTICLES,
            min = 0f, max = 3f, defaultValue = 1f,
            unit = "%",
            description = "Size of particles"
        ),
        GranularSliderConfig(
            key = "particleDensity",
            label = "Particle Density",
            icon = "⬤⬤⬤",
            category = GranularCategory.PARTICLES,
            min = 0f, max = 5f, defaultValue = 1f,
            unit = "%",
            description = "Number of particles"
        ),
        GranularSliderConfig(
            key = "particleOpacity",
            label = "Particle Opacity",
            icon = "👻",
            category = GranularCategory.PARTICLES,
            min = 0f, max = 1f, defaultValue = 1f,
            unit = "%",
            description = "Transparency of particles"
        ),
        GranularSliderConfig(
            key = "particlePhysics",
            label = "Particle Physics",
            icon = "🎯",
            category = GranularCategory.PARTICLES,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Physics simulation intensity"
        ),
        
        // Animations
        GranularSliderConfig(
            key = "animationSpeed",
            label = "Animation Speed",
            icon = "⚡",
            category = GranularCategory.ANIMATIONS,
            min = 0f, max = 3f, defaultValue = 1f,
            unit = "%",
            description = "Overall animation speed"
        ),
        GranularSliderConfig(
            key = "animationSmoothness",
            label = "Animation Smoothness",
            icon = "🌊",
            category = GranularCategory.ANIMATIONS,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Smoothness of animations"
        ),
        GranularSliderConfig(
            key = "transitionDuration",
            label = "Transition Duration",
            icon = "⏱️",
            category = GranularCategory.ANIMATIONS,
            min = 0f, max = 5f, defaultValue = 1f,
            unit = "%",
            description = "Duration of transitions"
        ),
        GranularSliderConfig(
            key = "staggerDelay",
            label = "Stagger Delay",
            icon = "📊",
            category = GranularCategory.ANIMATIONS,
            min = 0f, max = 5f, defaultValue = 1f,
            unit = "%",
            description = "Delay between staggered animations"
        ),
        
        // Advanced
        GranularSliderConfig(
            key = "bloomIntensity",
            label = "Bloom Intensity",
            icon = "✨",
            category = GranularCategory.ADVANCED,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Glow/bloom effect strength"
        ),
        GranularSliderConfig(
            key = "colorVibrance",
            label = "Color Vibrance",
            icon = "🌈",
            category = GranularCategory.ADVANCED,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Overall color vibrancy"
        ),
        GranularSliderConfig(
            key = "shadowDepth",
            label = "Shadow Depth",
            icon = "🌑",
            category = GranularCategory.ADVANCED,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Depth of shadows"
        ),
        GranularSliderConfig(
            key = "highlightIntensity",
            label = "Highlight Intensity",
            icon = "🌟",
            category = GranularCategory.ADVANCED,
            min = 0f, max = 2f, defaultValue = 1f,
            unit = "%",
            description = "Intensity of highlights"
        )
    )
    
    fun getSlidersByCategory(category: GranularCategory): List<GranularSliderConfig> {
        return ALL_SLIDERS.filter { it.category == category }
    }
}

/**
 * Extension function for Color to HSL conversion
 */
fun Color.toHsl(): FloatArray {
    val r = this.red
    val g = this.green
    val b = this.blue
    
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    var h = 0f
    var s = 0f
    val l = (max + min) / 2f
    
    if (max != min) {
        val d = max - min
        s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
        h = when (max) {
            r -> (g - b) / d + (if (g < b) 6f else 0f)
            g -> (b - r) / d + 2f
            b -> (r - g) / d + 4f
            else -> 0f
        }
        h /= 6f
    }
    
    return floatArrayOf(h * 360f, s, l)
}
