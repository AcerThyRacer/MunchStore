package com.sugarmunch.app.theme.dsl

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import com.sugarmunch.app.theme.components.*
import com.sugarmunch.app.theme.model.*
import com.sugarmunch.app.theme.macros.*
import kotlinx.serialization.Serializable

/**
 * SugarMunch Theme DSL - Kotlin-based scripting API for advanced theme creation
 *
 * Features:
 * - Type-safe theme building
 * - Programmatic component creation
 * - Custom animations and transitions
 * - Conditional theme logic
 * - Reusable theme templates
 *
 * Example:
 * ```kotlin
 * val myTheme = sugarTheme {
 *     name = "Cyberpunk Night"
 *     description = "Neon-soaked cyberpunk aesthetic"
 *
 *     layers {
 *         background {
 *             gradient(
 *                 colors = listOf(Color(0xFF0D0D2B), Color(0xFF1A0B2E)),
 *                 angle = 135f,
 *                 animated = true,
 *                 speed = 0.5f
 *             )
 *         }
 *
 *         particles {
 *             vortex(
 *                 count = 150,
 *                 colors = listOf(Color(0xFFFF00FF), Color(0xFF00FFFF)),
 *                 rotationSpeed = 2f
 *             )
 *         }
 *
 *         effects {
 *             bloom(intensity = 1.5f, color = Color(0xFFFF00FF))
 *             chromaticAberration(intensity = 0.03f)
 *         }
 *     }
 *
 *     macros {
 *         onTime(hour = 22, minute = 0) {
 *             switchTo("nightMode")
 *         }
 *
 *         onAppLaunch("com.game.example") {
 *             animateIntensity(
 *                 particleIntensity = 1.5f,
 *                 duration = 2000
 *             )
 *         }
 *     }
 * }
 * ```
 */

// ═════════════════════════════════════════════════════════════
// THEME BUILDER DSL
// ═════════════════════════════════════════════════════════════

/**
 * Main DSL entry point for creating themes
 */
fun sugarTheme(block: ThemeBuilder.() -> Unit): SugarThemeDefinition {
    val builder = ThemeBuilder()
    builder.block()
    return builder.build()
}

/**
 * Theme builder context
 */
@SugarThemeDslMarker
class ThemeBuilder {
    var name: String = "Custom Theme"
    var description: String = ""
    var author: String = "User"
    var version: String = "1.0"
    var isPremium: Boolean = false

    private val layers = mutableListOf<LayerDefinition>()
    private val macros = mutableListOf<MacroDefinition>()
    private val granularConfig = GranularThemeConfig.DEFAULT

    /**
     * Define theme layers
     */
    fun layers(block: LayerBuilder.() -> Unit) {
        val layerBuilder = LayerBuilder()
        layerBuilder.block()
        layers.addAll(layerBuilder.build())
    }

    /**
     * Define theme macros
     */
    fun macros(block: MacroBuilder.() -> Unit) {
        val macroBuilder = MacroBuilder()
        macroBuilder.block()
        macros.addAll(macroBuilder.build())
    }

    /**
     * Configure granular settings
     */
    fun granular(block: GranularBuilder.() -> Unit) {
        val granularBuilder = GranularBuilder()
        granularBuilder.block()
        // Apply granular config
    }

    internal fun build(): SugarThemeDefinition {
        return SugarThemeDefinition(
            name = name,
            description = description,
            author = author,
            version = version,
            isPremium = isPremium,
            layers = layers,
            macros = macros,
            granularConfig = granularConfig
        )
    }
}

@DslMarker
annotation class SugarThemeDslMarker

// ═════════════════════════════════════════════════════════════
// LAYER DSL
// ═════════════════════════════════════════════════════════════

@SugarThemeDslMarker
class LayerBuilder {
    private val layers = mutableListOf<LayerDefinition>()

    /**
     * Background layer
     */
    fun background(block: BackgroundLayerBuilder.() -> Unit) {
        val builder = BackgroundLayerBuilder()
        builder.block()
        layers.add(builder.build())
    }

    /**
     * Particle layer
     */
    fun particles(block: ParticleLayerBuilder.() -> Unit) {
        val builder = ParticleLayerBuilder()
        builder.block()
        layers.add(builder.build())
    }

    /**
     * Color overlay layer
     */
    fun overlay(block: ColorOverlayBuilder.() -> Unit) {
        val builder = ColorOverlayBuilder()
        builder.block()
        layers.add(builder.build())
    }

    /**
     * Effects layer
     */
    fun effects(block: EffectsLayerBuilder.() -> Unit) {
        val builder = EffectsLayerBuilder()
        builder.block()
        layers.add(builder.build())
    }

    /**
     * Light effects layer
     */
    fun lighting(block: LightingLayerBuilder.() -> Unit) {
        val builder = LightingLayerBuilder()
        builder.block()
        layers.add(builder.build())
    }

    internal fun build(): List<LayerDefinition> = layers
}

// ═════════════════════════════════════════════════════════════
// BACKGROUND LAYER
// ═════════════════════════════════════════════════════════════

@SugarThemeDslMarker
class BackgroundLayerBuilder {
    var enabled: Boolean = true
    var opacity: Float = 1f
    var blendMode: BlendMode = BlendMode.Normal

    private var gradientType: GradientType = GradientType.LINEAR
    private var gradientColors: List<Color> = emptyList()
    private var gradientAngle: Float = 90f
    private var gradientSpread: Float = 1f
    private var animated: Boolean = false
    private var animationSpeed: Float = 1f
    private var solidColor: Color? = null

    fun gradient(
        colors: List<Color>,
        type: GradientType = GradientType.LINEAR,
        angle: Float = 90f,
        spread: Float = 1f,
        animated: Boolean = false,
        speed: Float = 1f
    ) {
        gradientColors = colors
        gradientType = type
        gradientAngle = angle
        gradientSpread = spread
        this.animated = animated
        animationSpeed = speed
    }

    fun solid(color: Color) {
        solidColor = color
    }

    internal fun build(): LayerDefinition {
        return LayerDefinition.Background(
            enabled = enabled,
            opacity = opacity,
            blendMode = blendMode,
            gradient = GradientConfig(
                colors = gradientColors.map { it.toLong() },
                type = gradientType,
                angle = gradientAngle,
                spread = gradientSpread
            ),
            solidColor = solidColor,
            animated = animated,
            animationSpeed = animationSpeed
        )
    }
}

// ═════════════════════════════════════════════════════════════
// PARTICLE LAYER
// ═════════════════════════════════════════════════════════════

@SugarThemeDslMarker
class ParticleLayerBuilder {
    var enabled: Boolean = true
    var opacity: Float = 1f
    var blendMode: BlendMode = BlendMode.Screen

    private var particleType: ParticleType = ParticleType.CIRCLES
    private var count: IntRange = 50..100
    private var colors: List<Color> = emptyList()
    private var speed: FloatRange = 1f..3f
    private var size: FloatRange = 4f..12f
    private var physicsEnabled: Boolean = true

    /**
     * Vortex particle pattern
     */
    fun vortex(
        count: Int = 100,
        colors: List<Color>,
        rotationSpeed: Float = 1f,
        inwardPull: Float = 0.5f
    ) {
        particleType = ParticleType.CIRCLES
        this.count = count..count
        this.colors = colors
        speed = rotationSpeed..(rotationSpeed * 2)
    }

    /**
     * Rain particle pattern
     */
    fun rain(
        count: Int = 150,
        color: Color = Color.White,
        fallSpeed: Float = 3f,
        wind: Float = 0f
    ) {
        particleType = ParticleType.CIRCLES
        this.count = count..count
        colors = listOf(color)
        speed = fallSpeed..(fallSpeed * 1.5f)
    }

    /**
     * Custom particle configuration
     */
    fun custom(
        type: ParticleType,
        count: IntRange,
        colors: List<Color>,
        speed: FloatRange,
        size: FloatRange
    ) {
        particleType = type
        this.count = count
        this.colors = colors
        this.speed = speed
        this.size = size
    }

    internal fun build(): LayerDefinition {
        return LayerDefinition.Particles(
            enabled = enabled,
            opacity = opacity,
            blendMode = blendMode,
            particleType = particleType,
            density = count,
            colors = colors.map { it.toLong() },
            speed = speed,
            size = size,
            physicsEnabled = physicsEnabled
        )
    }
}

// ═════════════════════════════════════════════════════════════
// COLOR OVERLAY LAYER
// ═════════════════════════════════════════════════════════════

@SugarThemeDslMarker
class ColorOverlayBuilder {
    var enabled: Boolean = false
    var opacity: Float = 0.2f
    var blendMode: BlendMode = BlendMode.Overlay

    private var color: Color = Color.White
    private var gradient: List<Color>? = null
    private var animationSpeed: Float = 0f

    fun solid(
        color: Color,
        opacity: Float = 0.2f,
        blendMode: BlendMode = BlendMode.Overlay
    ) {
        this.color = color
        this.opacity = opacity
        this.blendMode = blendMode
    }

    fun gradient(
        colors: List<Color>,
        opacity: Float = 0.2f,
        animated: Boolean = false,
        speed: Float = 0.5f
    ) {
        gradient = colors
        this.opacity = opacity
        animationSpeed = if (animated) speed else 0f
    }

    internal fun build(): LayerDefinition {
        return LayerDefinition.ColorOverlay(
            enabled = enabled,
            opacity = opacity,
            blendMode = blendMode,
            color = color,
            gradient = gradient?.map { it.toLong() },
            animationSpeed = animationSpeed
        )
    }
}

// ═════════════════════════════════════════════════════════════
// EFFECTS LAYER
// ═════════════════════════════════════════════════════════════

@SugarThemeDslMarker
class EffectsLayerBuilder {
    var enabled: Boolean = true
    var opacity: Float = 1f

    private val effects = mutableListOf<EffectDefinition>()

    /**
     * Bloom/glow effect
     */
    fun bloom(
        intensity: Float = 1f,
        threshold: Float = 0.8f,
        radius: Float = 50f,
        color: Color? = null,
        animated: Boolean = false,
        speed: Float = 1f
    ) {
        effects.add(
            EffectDefinition.Bloom(
                enabled = enabled,
                opacity = opacity,
                intensity = intensity,
                threshold = threshold,
                radius = radius,
                color = color,
                animated = animated,
                animationSpeed = speed
            )
        )
    }

    /**
     * Chromatic aberration effect
     */
    fun chromaticAberration(
        intensity: Float = 0.02f,
        direction: Float = 0f,
        animated: Boolean = false,
        speed: Float = 1f
    ) {
        effects.add(
            EffectDefinition.ChromaticAberration(
                enabled = enabled,
                opacity = opacity,
                intensity = intensity,
                direction = direction,
                animated = animated,
                animationSpeed = speed
            )
        )
    }

    internal fun build(): LayerDefinition {
        return LayerDefinition.Effects(
            enabled = enabled,
            opacity = opacity,
            effects = effects
        )
    }
}

// ═════════════════════════════════════════════════════════════
// LIGHTING LAYER
// ═════════════════════════════════════════════════════════════

@SugarThemeDslMarker
class LightingLayerBuilder {
    var enabled: Boolean = true
    var opacity: Float = 1f

    private var bloomStrength: Float = 1f
    private var glowColor: Color = Color.White
    private var glowRadius: Float = 50f
    private var highlightIntensity: Float = 1f

    fun bloom(
        strength: Float = 1f,
        color: Color = Color.White,
        radius: Float = 50f
    ) {
        bloomStrength = strength
        glowColor = color
        glowRadius = radius
    }

    fun highlights(
        intensity: Float = 1f
    ) {
        highlightIntensity = intensity
    }

    internal fun build(): LayerDefinition {
        return LayerDefinition.Lighting(
            enabled = enabled,
            opacity = opacity,
            bloomStrength = bloomStrength,
            glowColor = glowColor,
            glowRadius = glowRadius,
            highlightIntensity = highlightIntensity
        )
    }
}

// ═════════════════════════════════════════════════════════════
// MACRO DSL
// ═════════════════════════════════════════════════════════════

@SugarThemeDslMarker
class MacroBuilder {
    private val macros = mutableListOf<MacroDefinition>()

    /**
     * Time-based trigger
     */
    fun onTime(
        hour: Int,
        minute: Int,
        days: List<Int> = emptyList(),
        action: MacroActionBuilder.() -> Unit
    ) {
        macros.add(
            MacroDefinition(
                name = "Time Trigger $hour:$minute",
                trigger = MacroTrigger.TimeTrigger(hour, minute, days.toSet()),
                action = buildAction(action)
            )
        )
    }

    /**
     * App launch trigger
     */
    fun onAppLaunch(
        vararg packageNames: String,
        action: MacroActionBuilder.() -> Unit
    ) {
        macros.add(
            MacroDefinition(
                name = "App Launch: ${packageNames.first()}",
                trigger = MacroTrigger.AppTrigger(packageNames.toSet()),
                action = buildAction(action)
            )
        )
    }

    /**
     * Battery trigger
     */
    fun onBatteryLow(
        threshold: Int = 20,
        action: MacroActionBuilder.() -> Unit
    ) {
        macros.add(
            MacroDefinition(
                name = "Battery Low",
                trigger = MacroTrigger.BatteryTrigger(threshold, MacroTrigger.BatteryCondition.BELOW),
                action = buildAction(action)
            )
        )
    }

    /**
     * Music playback trigger
     */
    fun onMusicPlay(action: MacroActionBuilder.() -> Unit) {
        macros.add(
            MacroDefinition(
                name = "Music Playing",
                trigger = MacroTrigger.MusicTrigger(),
                action = buildAction(action)
            )
        )
    }

    /**
     * Weather trigger
     */
    fun onWeather(
        vararg conditions: WeatherCondition,
        action: MacroActionBuilder.() -> Unit
    ) {
        macros.add(
            MacroDefinition(
                name = "Weather: ${conditions.joinToString()}",
                trigger = MacroTrigger.WeatherTrigger(conditions.toSet()),
                action = buildAction(action)
            )
        )
    }

    private fun buildAction(block: MacroActionBuilder.() -> Unit): MacroAction {
        val builder = MacroActionBuilder()
        builder.block()
        return builder.build()
    }

    internal fun build(): List<MacroDefinition> = macros
}

@SugarThemeDslMarker
class MacroActionBuilder {
    private var themeId: String? = null
    private var targetThemeIntensity: Float? = null
    private var targetParticleIntensity: Float? = null
    private var targetAnimationIntensity: Float? = null
    private var durationMs: Long = 1000
    private var granularConfig: GranularThemeConfig? = null

    fun switchTo(themeId: String) {
        this.themeId = themeId
    }

    fun animateIntensity(
        theme: Float? = null,
        particles: Float? = null,
        animations: Float? = null,
        duration: Long = 1000
    ) {
        targetThemeIntensity = theme
        targetParticleIntensity = particles
        targetAnimationIntensity = animations
        durationMs = duration
    }

    fun applyConfig(config: GranularThemeConfig) {
        granularConfig = config
    }

    internal fun build(): MacroAction {
        return when {
            themeId != null -> MacroAction.SwitchTheme(themeId!!, durationMs.toInt())
            targetThemeIntensity != null -> MacroAction.AnimateIntensity(
                targetThemeIntensity = targetThemeIntensity,
                targetParticleIntensity = targetParticleIntensity,
                targetAnimationIntensity = targetAnimationIntensity,
                durationMs = durationMs
            )
            granularConfig != null -> MacroAction.ApplyGranularConfig(granularConfig!!)
            else -> MacroAction.SwitchTheme("default")
        }
    }
}

// ═════════════════════════════════════════════════════════════
// GRANULAR CONFIG DSL
// ═════════════════════════════════════════════════════════════

@SugarThemeDslMarker
class GranularBuilder {
    // Colors
    var primarySaturation: Float = 1f
    var primaryBrightness: Float = 1f
    var secondarySaturation: Float = 1f
    var secondaryBrightness: Float = 1f
    var accentSaturation: Float = 1f
    var accentBrightness: Float = 1f
    var surfaceWarmth: Float = 0f
    var backgroundContrast: Float = 1f

    // Gradient
    var gradientAngle: Float = 90f
    var gradientSpread: Float = 1f
    var gradientSmoothness: Float = 1f
    var gradientOffsetX: Float = 0f
    var gradientOffsetY: Float = 0f
    var gradientAnimationSpeed: Float = 1f

    // Particles
    var particleSize: Float = 1f
    var particleDensity: Float = 1f
    var particleOpacity: Float = 1f
    var particlePhysics: Float = 1f

    // Animations
    var animationSpeed: Float = 1f
    var animationSmoothness: Float = 1f
    var transitionDuration: Float = 1f
    var staggerDelay: Float = 1f

    // Advanced
    var bloomIntensity: Float = 1f
    var colorVibrance: Float = 1f
    var shadowDepth: Float = 1f
    var highlightIntensity: Float = 1f

    internal fun build(): GranularThemeConfig {
        return GranularThemeConfig(
            primarySaturation = primarySaturation,
            primaryBrightness = primaryBrightness,
            secondarySaturation = secondarySaturation,
            secondaryBrightness = secondaryBrightness,
            accentSaturation = accentSaturation,
            accentBrightness = accentBrightness,
            surfaceWarmth = surfaceWarmth,
            backgroundContrast = backgroundContrast,
            gradientAngle = gradientAngle,
            gradientSpread = gradientSpread,
            gradientSmoothness = gradientSmoothness,
            gradientOffsetX = gradientOffsetX,
            gradientOffsetY = gradientOffsetY,
            gradientAnimationSpeed = gradientAnimationSpeed,
            particleSize = particleSize,
            particleDensity = particleDensity,
            particleOpacity = particleOpacity,
            particlePhysics = particlePhysics,
            animationSpeed = animationSpeed,
            animationSmoothness = animationSmoothness,
            transitionDuration = transitionDuration,
            staggerDelay = staggerDelay,
            bloomIntensity = bloomIntensity,
            colorVibrance = colorVibrance,
            shadowDepth = shadowDepth,
            highlightIntensity = highlightIntensity
        )
    }
}

// ═════════════════════════════════════════════════════════════
// THEME DEFINITION
// ═════════════════════════════════════════════════════════════

/**
 * Complete theme definition from DSL
 */
@Serializable
data class SugarThemeDefinition(
    val name: String,
    val description: String,
    val author: String,
    val version: String,
    val isPremium: Boolean,
    val layers: List<LayerDefinition>,
    val macros: List<MacroDefinition>,
    val granularConfig: GranularThemeConfig
)

/**
 * Layer definition sealed class
 */
@Serializable
sealed class LayerDefinition {
    abstract val enabled: Boolean
    abstract val opacity: Float
    abstract val blendMode: BlendMode

    @Serializable
    data class Background(
        override val enabled: Boolean,
        override val opacity: Float,
        override val blendMode: BlendMode,
        val gradient: GradientConfig,
        val solidColor: Color?,
        val animated: Boolean,
        val animationSpeed: Float
    ) : LayerDefinition()

    @Serializable
    data class Particles(
        override val enabled: Boolean,
        override val opacity: Float,
        override val blendMode: BlendMode,
        val particleType: ParticleType,
        val density: IntRange,
        val colors: List<Long>,
        val speed: FloatRange,
        val size: FloatRange,
        val physicsEnabled: Boolean
    ) : LayerDefinition()

    @Serializable
    data class ColorOverlay(
        override val enabled: Boolean,
        override val opacity: Float,
        override val blendMode: BlendMode,
        val color: Color,
        val gradient: List<Long>?,
        val animationSpeed: Float
    ) : LayerDefinition()

    @Serializable
    data class Effects(
        override val enabled: Boolean,
        override val opacity: Float,
        val effects: List<EffectDefinition>
    ) : LayerDefinition()

    @Serializable
    data class Lighting(
        override val enabled: Boolean,
        override val opacity: Float,
        val bloomStrength: Float,
        val glowColor: Color,
        val glowRadius: Float,
        val highlightIntensity: Float
    ) : LayerDefinition()
}

/**
 * Effect definition sealed class
 */
@Serializable
sealed class EffectDefinition {
    abstract val enabled: Boolean
    abstract val opacity: Float

    @Serializable
    data class Bloom(
        override val enabled: Boolean,
        override val opacity: Float,
        val intensity: Float,
        val threshold: Float,
        val radius: Float,
        val color: Color?,
        val animated: Boolean,
        val animationSpeed: Float
    ) : EffectDefinition()

    @Serializable
    data class ChromaticAberration(
        override val enabled: Boolean,
        override val opacity: Float,
        val intensity: Float,
        val direction: Float,
        val animated: Boolean,
        val animationSpeed: Float
    ) : EffectDefinition()
}

/**
 * Macro definition data class
 */
@Serializable
data class MacroDefinition(
    val name: String,
    val trigger: MacroTrigger,
    val action: MacroAction,
    val conditions: List<MacroCondition> = emptyList()
)

// ═════════════════════════════════════════════════════════════
// UTILITY FUNCTIONS
// ═════════════════════════════════════════════════════════════

/**
 * Convert Color to Long for serialization
 */
fun Color.toLong(): Long {
    return (alpha.value.toLong() shl 24) or
            (red.value.toLong() shl 16) or
            (green.value.toLong() shl 8) or
            blue.value.toLong()
}

/**
 * Create a Color from Long
 */
fun fromLong(value: Long): Color {
    return Color(
        alpha = ((value shr 24) and 0xFF).toFloat() / 255f,
        red = ((value shr 16) and 0xFF).toFloat() / 255f,
        green = ((value shr 8) and 0xFF).toFloat() / 255f,
        blue = (value and 0xFF).toFloat() / 255f
    )
}

/**
 * DSL for creating color palettes
 */
fun colorPalette(block: ColorPaletteBuilder.() -> Unit): List<Color> {
    val builder = ColorPaletteBuilder()
    builder.block()
    return builder.build()
}

@SugarThemeDslMarker
class ColorPaletteBuilder {
    private val colors = mutableListOf<Color>()

    fun color(hex: String) {
        colors.add(Color(androidx.compose.ui.graphics.Color.parseColor(hex).value))
    }

    fun color(r: Int, g: Int, b: Int) {
        colors.add(Color(r, g, b))
    }

    internal fun build(): List<Color> = colors
}

/**
 * Example theme using the DSL
 */
val cyberpunkTheme = sugarTheme {
    name = "Cyberpunk Night"
    description = "Neon-soaked cyberpunk aesthetic with vibrant colors"
    author = "SugarMunch"
    isPremium = false

    layers {
        background {
            gradient(
                colors = listOf(Color(0xFF0D0D2B), Color(0xFF1A0B2E), Color(0xFF2D1B4E)),
                type = GradientType.LINEAR,
                angle = 135f,
                animated = true,
                speed = 0.5f
            )
        }

        particles {
            vortex(
                count = 150,
                colors = listOf(Color(0xFFFF00FF), Color(0xFF00FFFF), Color(0xFFFFEB3B)),
                rotationSpeed = 2f,
                inwardPull = 0.3f
            )
        }

        overlay {
            gradient(
                colors = listOf(Color(0xFFFF00FF).copy(alpha = 0.15f), Color.Transparent),
                opacity = 0.2f,
                animated = true,
                speed = 0.3f
            )
        }

        effects {
            bloom(
                intensity = 1.5f,
                threshold = 0.7f,
                radius = 60f,
                color = Color(0xFFFF00FF),
                animated = true,
                speed = 0.8f
            )
            chromaticAberration(
                intensity = 0.02f,
                animated = false
            )
        }

        lighting {
            bloom(
                strength = 1.3f,
                color = Color(0xFF00FFFF),
                radius = 80f
            )
            highlights(intensity = 1.2f)
        }
    }

    macros {
        onTime(hour = 22, minute = 0) {
            switchTo("nightMode")
        }

        onAppLaunch("com.epicgames.fortnite") {
            animateIntensity(
                theme = 1.5f,
                particles = 1.5f,
                animations = 1.3f,
                duration = 2000
            )
        }

        onBatteryLow(threshold = 20) {
            animateIntensity(
                theme = 0.5f,
                particles = 0.3f,
                animations = 0.5f,
                duration = 1000
            )
        }
    }

    granular {
        primarySaturation = 1.5f
        primaryBrightness = 1.3f
        colorVibrance = 1.4f
        bloomIntensity = 1.5f
        animationSpeed = 1.2f
        particleDensity = 1.3f
    }
}
