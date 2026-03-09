package com.sugarmunch.app.theme.model

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Layered Theme System - Photoshop-like layer composition for themes
 * 
 * Each theme is composed of multiple independent layers that can be:
 * - Enabled/disabled individually
 * - Adjusted opacity
 * - Blended using different blend modes
 * - Animated independently
 * 
 * This allows users to mix and match elements from different themes.
 */
@Serializable
data class LayeredThemeConfig(
    val layers: List<ThemeLayer>,
    val globalOpacity: Float = 1f,
    val globalBlendMode: BlendMode = BlendMode.Normal
) {
    /**
     * Get enabled layers in render order (bottom to top)
     */
    fun getEnabledLayers(): List<ThemeLayer> = layers.filter { it.isEnabled }

    /**
     * Add a new layer
     */
    fun addLayer(layer: ThemeLayer): LayeredThemeConfig {
        return copy(layers = layers + layer)
    }

    /**
     * Remove a layer by ID
     */
    fun removeLayer(layerId: String): LayeredThemeConfig {
        return copy(layers = layers.filter { it.id != layerId })
    }

    /**
     * Update a layer's properties
     */
    fun updateLayer(layerId: String, update: ThemeLayer.() -> ThemeLayer): LayeredThemeConfig {
        return copy(
            layers = layers.map { layer ->
                if (layer.id == layerId) layer.update() else layer
            }
        )
    }

    /**
     * Reorder layers (change render order)
     */
    fun reorderLayer(layerId: String, newIndex: Int): LayeredThemeConfig {
        val layer = layers.find { it.id == layerId } ?: return this
        val remaining = layers.filter { it.id != layerId }
        return copy(
            layers = remaining.toMutableList().apply {
                add(newIndex.coerceIn(0, remaining.size), layer)
            }
        )
    }

    /**
     * Duplicate a layer
     */
    fun duplicateLayer(layerId: String): LayeredThemeConfig {
        val layer = layers.find { it.id == layerId } ?: return this
        val index = layers.indexOfFirst { it.id == layerId }
        val duplicate = layer.copy(id = "${layer.id}_copy_${System.currentTimeMillis()}")
        return copy(
            layers = layers.toMutableList().apply {
                add(index + 1, duplicate)
            }
        )
    }

    companion object {
        /**
         * Create a default layered theme from a CandyTheme
         */
        fun fromCandyTheme(theme: CandyTheme): LayeredThemeConfig {
            return LayeredThemeConfig(
                layers = listOf(
                    ThemeLayer.BackgroundLayer(
                        id = "background",
                        isEnabled = true,
                        opacity = 1f,
                        blendMode = BlendMode.Normal,
                        gradient = theme.backgroundGradient,
                        animationSpeed = 1f
                    ),
                    ThemeLayer.ParticleLayer(
                        id = "particles",
                        isEnabled = theme.enableParticles,
                        opacity = 1f,
                        blendMode = BlendMode.Screen,
                        particleType = theme.particleConfig.type,
                        density = theme.particleConfig.count,
                        speed = theme.particleConfig.speed,
                        size = theme.particleConfig.size
                    ),
                    ThemeLayer.ColorOverlay(
                        id = "color_overlay",
                        isEnabled = false,
                        opacity = 0.2f,
                        blendMode = BlendMode.Overlay,
                        color = theme.primaryColor,
                        animationSpeed = 0.5f
                    ),
                    ThemeLayer.LightEffects(
                        id = "light_effects",
                        isEnabled = theme.enableBloom,
                        opacity = theme.bloomIntensity,
                        blendMode = BlendMode.Screen,
                        bloomStrength = theme.bloomIntensity,
                        glowColor = theme.primaryColor
                    )
                )
            )
        }
    }
}

/**
 * A single theme layer with rendering properties
 */
@Serializable
sealed class ThemeLayer {
    abstract val id: String
    abstract val isEnabled: Boolean
    abstract val opacity: Float
    abstract val blendMode: BlendMode
    abstract val type: LayerType

    /**
     * Toggle layer visibility
     */
    fun toggle(): ThemeLayer = when (this) {
        is BackgroundLayer -> copy(isEnabled = !isEnabled)
        is ParticleLayer -> copy(isEnabled = !isEnabled)
        is ColorOverlay -> copy(isEnabled = !isEnabled)
        is TextureLayer -> copy(isEnabled = !isEnabled)
        is LightEffects -> copy(isEnabled = !isEnabled)
    }

    /**
     * Set opacity (0-1)
     */
    fun withOpacity(newOpacity: Float): ThemeLayer = when (this) {
        is BackgroundLayer -> copy(opacity = newOpacity.coerceIn(0f, 1f))
        is ParticleLayer -> copy(opacity = newOpacity.coerceIn(0f, 1f))
        is ColorOverlay -> copy(opacity = newOpacity.coerceIn(0f, 1f))
        is TextureLayer -> copy(opacity = newOpacity.coerceIn(0f, 1f))
        is LightEffects -> copy(opacity = newOpacity.coerceIn(0f, 1f))
    }

    /**
     * Set blend mode
     */
    fun withBlendMode(mode: BlendMode): ThemeLayer = when (this) {
        is BackgroundLayer -> copy(blendMode = mode)
        is ParticleLayer -> copy(blendMode = mode)
        is ColorOverlay -> copy(blendMode = mode)
        is TextureLayer -> copy(blendMode = mode)
        is LightEffects -> copy(blendMode = mode)
    }

    @Serializable
    enum class LayerType(val displayName: String, val icon: String) {
        BACKGROUND("Background", "🖼️"),
        PARTICLES("Particles", "✨"),
        COLOR_OVERLAY("Color Overlay", "🎨"),
        TEXTURE("Texture", "📄"),
        LIGHT_EFFECTS("Light Effects", "💡")
    }

    /**
     * Background layer - gradient or solid color
     */
    @Serializable
    data class BackgroundLayer(
        override val id: String,
        override val isEnabled: Boolean,
        override val opacity: Float,
        override val blendMode: BlendMode,
        val gradient: GradientConfig,
        val animationSpeed: Float = 1f,
        val solidColor: Color? = null
    ) : ThemeLayer() {
        override val type = LayerType.BACKGROUND
    }

    /**
     * Particle layer - animated particles
     */
    @Serializable
    data class ParticleLayer(
        override val id: String,
        override val isEnabled: Boolean,
        override val opacity: Float,
        override val blendMode: BlendMode,
        val particleType: ParticleType,
        val density: IntRange,
        val speed: FloatRange,
        val size: FloatRange,
        val color: Color? = null,
        val physicsEnabled: Boolean = true
    ) : ThemeLayer() {
        override val type = LayerType.PARTICLES
    }

    /**
     * Color overlay layer - tints the entire theme
     */
    @Serializable
    data class ColorOverlay(
        override val id: String,
        override val isEnabled: Boolean,
        override val opacity: Float,
        override val blendMode: BlendMode,
        val color: Color,
        val animationSpeed: Float = 0f,
        val gradient: GradientConfig? = null
    ) : ThemeLayer() {
        override val type = LayerType.COLOR_OVERLAY
    }

    /**
     * Texture layer - image or pattern overlay
     */
    @Serializable
    data class TextureLayer(
        override val id: String,
        override val isEnabled: Boolean,
        override val opacity: Float,
        override val blendMode: BlendMode,
        val textureType: TextureType,
        val scale: Float = 1f,
        val rotation: Float = 0f,
        val animationSpeed: Float = 0f
    ) : ThemeLayer() {
        override val type = LayerType.TEXTURE
    }

    /**
     * Light effects layer - bloom, glow, highlights
     */
    @Serializable
    data class LightEffects(
        override val id: String,
        override val isEnabled: Boolean,
        override val opacity: Float,
        override val blendMode: BlendMode,
        val bloomStrength: Float,
        val glowColor: Color,
        val glowRadius: Float = 50f,
        val highlightIntensity: Float = 1f
    ) : ThemeLayer() {
        override val type = LayerType.LIGHT_EFFECTS
    }
}

/**
 * Texture types for texture layers
 */
@Serializable
enum class TextureType(val displayName: String) {
    NOISE("Noise"),
    GRID("Grid"),
    DOTS("Dots"),
    LINES("Lines"),
    GRADIENT("Gradient"),
    CUSTOM("Custom")
}

/**
 * Blend mode presets for easy selection
 */
object BlendModePresets {
    val NORMAL = BlendMode.Normal
    val MULTIPLY = BlendMode.Multiply
    val SCREEN = BlendMode.Screen
    val OVERLAY = BlendMode.Overlay
    val DARKEN = BlendMode.Darken
    val LIGHTEN = BlendMode.Lighten
    val COLOR_DODGE = BlendMode.ColorDodge
    val COLOR_BURN = BlendMode.ColorBurn
    val HARD_LIGHT = BlendMode.HardLight
    val SOFT_LIGHT = BlendMode.SoftLight
    val DIFFERENCE = BlendMode.Difference
    val EXCLUSION = BlendMode.Exclusion

    /**
     * Get blend modes suitable for particle layers
     */
    val PARTICLE_BLEND_MODES = listOf(
        NORMAL, SCREEN, OVERLAY, LIGHTEN, COLOR_DODGE, ADD
    )

    /**
     * Get blend modes suitable for color overlays
     */
    val OVERLAY_BLEND_MODES = listOf(
        NORMAL, MULTIPLY, SCREEN, OVERLAY, SOFT_LIGHT, HARD_LIGHT
    )
}
