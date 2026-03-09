package com.sugarmunch.app.theme.layers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import kotlinx.serialization.Serializable

/**
 * SugarRush Theme Layers System
 * Photoshop-like layers for infinite theme combinations
 */

/**
 * Represents a single theme layer with its own configuration
 */
@Serializable
data class ThemeLayer(
    val id: String,
    val name: String,
    val layerType: LayerType,
    val isEnabled: Boolean = true,
    val opacity: Float = 1f,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val config: LayerConfig,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Get preview color for UI
     */
    fun getPreviewColor(): Color {
        return when (layerType) {
            is LayerType.BACKGROUND_GRADIENT -> config.gradientColors.firstOrNull() ?: Color.Gray
            is LayerType.MESH_GRADIENT -> config.meshColors.firstOrNull() ?: Color.Gray
            is LayerType.PARTICLE_SYSTEM -> config.particleColors.firstOrNull() ?: Color.White
            is LayerType.COLOR_OVERLAY -> config.overlayColor
            is LayerType.TEXTURE -> config.textureColor
            is LayerType.LIGHT_EFFECTS -> config.glowColor
            is LayerType.UI_ELEMENTS -> config.accentColor
        }
    }
    
    companion object {
        fun createDefault(type: LayerType): ThemeLayer {
            return ThemeLayer(
                id = java.util.UUID.randomUUID().toString(),
                name = type.displayName,
                layerType = type,
                isEnabled = true,
                opacity = 1f,
                blendMode = BlendMode.NORMAL,
                config = type.defaultConfig()
            )
        }
    }
}

/**
 * Layer types available for theme composition
 */
@Serializable
sealed class LayerType(val displayName: String, val icon: String) {
    abstract fun defaultConfig(): LayerConfig
    
    @Serializable
    data object BACKGROUND_GRADIENT : LayerType("Background Gradient", "🌈") {
        override fun defaultConfig() = LayerConfig(
            gradientColors = listOf(Color(0xFFFF69B4), Color(0xFF9370DB)),
            gradientAngle = 45f,
            gradientSpread = 1f
        )
    }
    
    @Serializable
    data object MESH_GRADIENT : LayerType("Mesh Gradient", "🕸️") {
        override fun defaultConfig() = LayerConfig(
            meshColors = listOf(Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1)),
            meshComplexity = 3,
            animationSpeed = 1f
        )
    }
    
    @Serializable
    data object PARTICLE_SYSTEM : LayerType("Particle System", "✨") {
        override fun defaultConfig() = LayerConfig(
            particleColors = listOf(Color(0xFFFFD700), Color(0xFFFF69B4)),
            particleCount = 50,
            particleSize = 1f,
            particleSpeed = 1f,
            particleType = "floating"
        )
    }
    
    @Serializable
    data object COLOR_OVERLAY : LayerType("Color Overlay", "🎨") {
        override fun defaultConfig() = LayerConfig(
            overlayColor = Color(0xFFFF69B4).copy(alpha = 0.3f),
            overlayBlendMode = BlendMode.OVERLAY
        )
    }
    
    @Serializable
    data object TEXTURE : LayerType("Texture", "📷") {
        override fun defaultConfig() = LayerConfig(
            textureColor = Color.White,
            textureOpacity = 0.5f,
            textureScale = 1f
        )
    }
    
    @Serializable
    data object LIGHT_EFFECTS : LayerType("Light Effects", "💡") {
        override fun defaultConfig() = LayerConfig(
            glowColor = Color(0xFFFFD700),
            glowIntensity = 1f,
            glowRadius = 50f
        )
    }
    
    @Serializable
    data object UI_ELEMENTS : LayerType("UI Elements", "🔲") {
        override fun defaultConfig() = LayerConfig(
            accentColor = Color(0xFFFF69B4),
            cornerRadius = 16f,
            elevation = 4f
        )
    }
}

/**
 * Configuration for a layer
 */
@Serializable
data class LayerConfig(
    // Gradient settings
    val gradientColors: List<Color> = emptyList(),
    val gradientAngle: Float = 90f,
    val gradientSpread: Float = 1f,
    val gradientSmoothness: Float = 1f,
    val gradientOffsetX: Float = 0f,
    val gradientOffsetY: Float = 0f,
    
    // Mesh gradient settings
    val meshColors: List<Color> = emptyList(),
    val meshComplexity: Int = 3,
    val animationSpeed: Float = 1f,
    val meshSeed: Long = System.currentTimeMillis(),
    
    // Particle settings
    val particleColors: List<Color> = emptyList(),
    val particleCount: Int = 50,
    val particleSize: Float = 1f,
    val particleSpeed: Float = 1f,
    val particleType: String = "floating",
    val particlePhysics: Float = 1f,
    
    // Overlay settings
    val overlayColor: Color = Color.Transparent,
    val overlayBlendMode: BlendMode = BlendMode.NORMAL,
    
    // Texture settings
    val textureColor: Color = Color.White,
    val textureOpacity: Float = 0.5f,
    val textureScale: Float = 1f,
    val textureRotation: Float = 0f,
    
    // Light effects settings
    val glowColor: Color = Color.White,
    val glowIntensity: Float = 1f,
    val glowRadius: Float = 50f,
    val bloomEnabled: Boolean = true,
    
    // UI element settings
    val accentColor: Color = Color.Unspecified,
    val cornerRadius: Float = 16f,
    val elevation: Float = 4f,
    val borderWidth: Float = 0f,
    val borderColor: Color = Color.Transparent
) {
    companion object {
        val EMPTY = LayerConfig()
    }
}

/**
 * Blend modes for layer composition
 */
@Serializable
enum class BlendMode(val displayName: String, val description: String) {
    NORMAL("Normal", "Default blend mode"),
    MULTIPLY("Multiply", "Darkens by multiplying colors"),
    SCREEN("Screen", "Lightens by inverting and multiplying"),
    OVERLAY("Overlay", "Combines multiply and screen"),
    SOFT_LIGHT("Soft Light", "Gentle overlay effect"),
    HARD_LIGHT("Hard Light", "Strong overlay effect"),
    COLOR_DODGE("Color Dodge", "Brightens by decreasing contrast"),
    COLOR_BURN("Color Burn", "Darkens by increasing contrast"),
    DIFFERENCE("Difference", "Subtracts colors from each other"),
    EXCLUSION("Exclusion", "Similar to difference but softer"),
    HUE("Hue", "Uses hue of top layer"),
    SATURATION("Saturation", "Uses saturation of top layer"),
    COLOR("Color", "Uses hue and saturation of top layer"),
    LUMINOSITY("Luminosity", "Uses brightness of top layer"),
    ADD("Add", "Adds color values"),
    SUBTRACT("Subtract", "Subtracts color values"),
    DIVIDE("Divide", "Divides color values"),
    AVERAGE("Average", "Averages color values"),
    NEGATION("Negation", "Inverts and averages"),
    REFLECT("Reflect", "Creates reflection effect"),
    GLOW("Glow", "Creates glowing effect"),
    PHOENIX("Phoenix", "High contrast blend")
}

/**
 * Complete layered theme composition
 */
@Serializable
data class LayeredTheme(
    val id: String,
    val name: String,
    val description: String = "",
    val layers: List<ThemeLayer>,
    val layerOrder: List<String>,  // Z-index order (first = bottom)
    val globalSettings: GlobalThemeSettings = GlobalThemeSettings(),
    val isDark: Boolean = false,
    val category: String = "CUSTOM",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Get layers in render order
     */
    fun getLayersInOrder(): List<ThemeLayer> {
        return layerOrder.mapNotNull { id -> layers.find { it.id == id } }
    }
    
    /**
     * Get enabled layers only
     */
    fun getEnabledLayersInOrder(): List<ThemeLayer> {
        return getLayersInOrder().filter { it.isEnabled }
    }
    
    /**
     * Add a new layer
     */
    fun addLayer(layer: ThemeLayer, atIndex: Int = layerOrder.size): LayeredTheme {
        return copy(
            layers = layers + layer,
            layerOrder = layerOrder.toMutableList().apply {
                add(atIndex.coerceIn(0, this.size), layer.id)
            },
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Remove a layer
     */
    fun removeLayer(layerId: String): LayeredTheme {
        return copy(
            layers = layers.filter { it.id != layerId },
            layerOrder = layerOrder.filter { it != layerId },
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Reorder layers
     */
    fun reorderLayers(oldIndex: Int, newIndex: Int): LayeredTheme {
        val newOrder = layerOrder.toMutableList()
        val layerId = newOrder.removeAt(oldIndex)
        newOrder.add(newIndex, layerId)
        return copy(
            layerOrder = newOrder,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Update layer opacity
     */
    fun updateLayerOpacity(layerId: String, opacity: Float): LayeredTheme {
        return copy(
            layers = layers.map {
                if (it.id == layerId) it.copy(opacity = opacity, updatedAt = System.currentTimeMillis())
                else it
            },
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Update layer blend mode
     */
    fun updateLayerBlendMode(layerId: String, blendMode: BlendMode): LayeredTheme {
        return copy(
            layers = layers.map {
                if (it.id == layerId) it.copy(blendMode = blendMode, updatedAt = System.currentTimeMillis())
                else it
            },
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Toggle layer enabled state
     */
    fun toggleLayer(layerId: String): LayeredTheme {
        return copy(
            layers = layers.map {
                if (it.id == layerId) it.copy(isEnabled = !it.isEnabled, updatedAt = System.currentTimeMillis())
                else it
            },
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Update layer config
     */
    fun updateLayerConfig(layerId: String, config: LayerConfig): LayeredTheme {
        return copy(
            layers = layers.map {
                if (it.id == layerId) it.copy(config = config, updatedAt = System.currentTimeMillis())
                else it
            },
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Get combined background brush from gradient layers
     */
    fun getBackgroundBrush(): Brush {
        val gradientLayer = layers.find { 
            it.isEnabled && it.layerType is LayerType.BACKGROUND_GRADIENT 
        }
        
        if (gradientLayer != null) {
            val config = gradientLayer.config
            val angleRad = Math.toRadians(config.gradientAngle.toDouble())
            // Simplified - actual implementation would use angle for gradient direction
            return Brush.linearGradient(
                colors = config.gradientColors,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(
                    kotlin.math.cos(angleRad).toFloat(),
                    kotlin.math.sin(angleRad).toFloat()
                )
            )
        }
        
        // Default fallback
        return Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
    }
    
    companion object {
        fun createEmpty(name: String = "Custom Layered Theme"): LayeredTheme {
            val baseLayer = ThemeLayer.createDefault(LayerType.BACKGROUND_GRADIENT)
            return LayeredTheme(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                layers = listOf(baseLayer),
                layerOrder = listOf(baseLayer.id)
            )
        }
        
        /**
         * Create from existing CandyTheme
         */
        fun fromCandyTheme(theme: com.sugarmunch.app.theme.model.CandyTheme): LayeredTheme {
            val backgroundLayer = when (theme.backgroundStyle) {
                is com.sugarmunch.app.theme.model.BackgroundStyle.Gradient -> {
                    ThemeLayer(
                        id = java.util.UUID.randomUUID().toString(),
                        name = "Background Gradient",
                        layerType = LayerType.BACKGROUND_GRADIENT,
                        config = LayerConfig(
                            gradientColors = theme.backgroundStyle.colors
                        )
                    )
                }
                is com.sugarmunch.app.theme.model.BackgroundStyle.AnimatedMesh -> {
                    ThemeLayer(
                        id = java.util.UUID.randomUUID().toString(),
                        name = "Mesh Gradient",
                        layerType = LayerType.MESH_GRADIENT,
                        config = LayerConfig(
                            meshColors = theme.backgroundStyle.baseColors,
                            animationSpeed = theme.backgroundStyle.animationSpeed
                        )
                    )
                }
                is com.sugarmunch.app.theme.model.BackgroundStyle.Solid -> {
                    ThemeLayer(
                        id = java.util.UUID.randomUUID().toString(),
                        name = "Solid Background",
                        layerType = LayerType.COLOR_OVERLAY,
                        config = LayerConfig(
                            overlayColor = theme.backgroundStyle.color
                        )
                    )
                }
            }
            
            val particleLayer = if (theme.particleConfig.enabled) {
                ThemeLayer(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Particles",
                    layerType = LayerType.PARTICLE_SYSTEM,
                    config = LayerConfig(
                        particleColors = theme.particleConfig.colors,
                        particleCount = theme.particleConfig.count.average().toInt(),
                        particleSpeed = theme.particleConfig.speed.max,
                        particleType = theme.particleConfig.type.name.lowercase()
                    )
                )
            } else null
            
            val layers = listOfNotNull(backgroundLayer, particleLayer)
            
            return LayeredTheme(
                id = theme.id,
                name = theme.name,
                description = theme.description,
                layers = layers,
                layerOrder = layers.map { it.id },
                isDark = theme.isDark,
                category = theme.category.name
            )
        }
    }
}

/**
 * Global settings for layered theme
 */
@Serializable
data class GlobalThemeSettings(
    val masterOpacity: Float = 1f,
    val colorAdjustments: ColorAdjustments = ColorAdjustments(),
    val animationSettings: AnimationSettings = AnimationSettings(),
    val performanceSettings: PerformanceSettings = PerformanceSettings()
)

/**
 * Global color adjustments applied to all layers
 */
@Serializable
data class ColorAdjustments(
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val hue: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val vibrance: Float = 1f,
    val colorBalance: ColorBalance = ColorBalance()
)

/**
 * Color balance for shadows, midtones, highlights
 */
@Serializable
data class ColorBalance(
    val shadowRed: Float = 0f,
    val shadowGreen: Float = 0f,
    val shadowBlue: Float = 0f,
    val midtoneRed: Float = 0f,
    val midtoneGreen: Float = 0f,
    val midtoneBlue: Float = 0f,
    val highlightRed: Float = 0f,
    val highlightGreen: Float = 0f,
    val highlightBlue: Float = 0f
)

/**
 * Global animation settings
 */
@Serializable
data class AnimationSettings(
    val globalSpeed: Float = 1f,
    val globalSmoothness: Float = 1f,
    val transitionDuration: Int = 300,
    val staggerDelay: Int = 50,
    val enableAnimations: Boolean = true,
    val reduceMotion: Boolean = false
)

/**
 * Performance settings for layered themes
 */
@Serializable
data class PerformanceSettings(
    val maxParticleCount: Int = 200,
    val enableBlur: Boolean = true,
    val blurRadius: Int = 10,
    val renderQuality: RenderQuality = RenderQuality.HIGH,
    val enableOptimizations: Boolean = true
)

enum class RenderQuality {
    LOW, MEDIUM, HIGH, ULTRA
}

/**
 * Layer preset for quick application
 */
@Serializable
data class LayerPreset(
    val id: String,
    val name: String,
    val description: String,
    val layerType: LayerType,
    val config: LayerConfig,
    val thumbnail: String? = null,
    val isPremium: Boolean = false,
    val downloadCount: Int = 0,
    val rating: Float = 0f
)

/**
 * Collection of layer presets
 */
object LayerPresets {
    // Background Gradient Presets
    val SUNSET_GRADIENT = LayerPreset(
        id = "preset_sunset_gradient",
        name = "Sunset Gradient",
        description = "Warm sunset colors",
        layerType = LayerType.BACKGROUND_GRADIENT,
        config = LayerConfig(
            gradientColors = listOf(
                Color(0xFFFF6B6B),
                Color(0xFFFF8E53),
                Color(0xFFFFC371)
            ),
            gradientAngle = 45f
        )
    )
    
    val OCEAN_GRADIENT = LayerPreset(
        id = "preset_ocean_gradient",
        name = "Ocean Gradient",
        description = "Cool ocean blues",
        layerType = LayerType.BACKGROUND_GRADIENT,
        config = LayerConfig(
            gradientColors = listOf(
                Color(0xFF4B6CB7),
                Color(0xFF182848)
            ),
            gradientAngle = 90f
        )
    )
    
    val FOREST_GRADIENT = LayerPreset(
        id = "preset_forest_gradient",
        name = "Forest Gradient",
        description = "Natural green tones",
        layerType = LayerType.BACKGROUND_GRADIENT,
        config = LayerConfig(
            gradientColors = listOf(
                Color(0xFF134E5E),
                Color(0xFF71B280)
            ),
            gradientAngle = 135f
        )
    )
    
    val NEON_GRADIENT = LayerPreset(
        id = "preset_neon_gradient",
        name = "Neon Gradient",
        description = "Vibrant neon colors",
        layerType = LayerType.BACKGROUND_GRADIENT,
        config = LayerConfig(
            gradientColors = listOf(
                Color(0xFFFF00FF),
                Color(0xFF00FFFF),
                Color(0xFFFF0080)
            ),
            gradientAngle = 45f
        )
    )
    
    // Mesh Gradient Presets
    val AURORA_MESH = LayerPreset(
        id = "preset_aurora_mesh",
        name = "Aurora Mesh",
        description = "Northern lights effect",
        layerType = LayerType.MESH_GRADIENT,
        config = LayerConfig(
            meshColors = listOf(
                Color(0xFF00F5FF),
                Color(0xFF00D4FF),
                Color(0xFF0099FF),
                Color(0xFF7B2FF7)
            ),
            meshComplexity = 4,
            animationSpeed = 0.8f
        )
    )
    
    val CANDY_MESH = LayerPreset(
        id = "preset_candy_mesh",
        name = "Candy Mesh",
        description = "Sweet candy colors",
        layerType = LayerType.MESH_GRADIENT,
        config = LayerConfig(
            meshColors = listOf(
                Color(0xFFFF69B4),
                Color(0xFFFFB6C1),
                Color(0xFFDDA0DD),
                Color(0xFF87CEEB)
            ),
            meshComplexity = 3,
            animationSpeed = 1.2f
        )
    )
    
    // Particle Presets
    val GOLDEN_SPARKLES = LayerPreset(
        id = "preset_golden_sparkles",
        name = "Golden Sparkles",
        description = "Shimmering gold particles",
        layerType = LayerType.PARTICLE_SYSTEM,
        config = LayerConfig(
            particleColors = listOf(Color(0xFFFFD700), Color(0xFFFFE55C)),
            particleCount = 80,
            particleSize = 1.5f,
            particleSpeed = 0.8f,
            particleType = "floating"
        )
    )
    
    val RAINING_HEARTS = LayerPreset(
        id = "preset_raining_hearts",
        name = "Raining Hearts",
        description = "Falling heart particles",
        layerType = LayerType.PARTICLE_SYSTEM,
        config = LayerConfig(
            particleColors = listOf(Color(0xFFFF69B4), Color(0xFFFF1493)),
            particleCount = 50,
            particleSize = 2f,
            particleSpeed = 1.5f,
            particleType = "raining"
        )
    )
    
    val BUBBLES = LayerPreset(
        id = "preset_bubbles",
        name = "Bubbles",
        description = "Floating bubble particles",
        layerType = LayerType.PARTICLE_SYSTEM,
        config = LayerConfig(
            particleColors = listOf(Color(0xFF87CEEB), Color(0xFFE0FFFF)),
            particleCount = 40,
            particleSize = 3f,
            particleSpeed = 0.5f,
            particleType = "rising"
        )
    )
    
    // Light Effect Presets
    val WARM_GLOW = LayerPreset(
        id = "preset_warm_glow",
        name = "Warm Glow",
        description = "Cozy warm lighting",
        layerType = LayerType.LIGHT_EFFECTS,
        config = LayerConfig(
            glowColor = Color(0xFFFFD700),
            glowIntensity = 0.8f,
            glowRadius = 100f,
            bloomEnabled = true
        )
    )
    
    val COOL_BLOOM = LayerPreset(
        id = "preset_cool_bloom",
        name = "Cool Bloom",
        description = "Cool bloom effect",
        layerType = LayerType.LIGHT_EFFECTS,
        config = LayerConfig(
            glowColor = Color(0xFF00FFFF),
            glowIntensity = 1f,
            glowRadius = 80f,
            bloomEnabled = true
        )
    )
    
    val ALL_PRESETS = listOf(
        SUNSET_GRADIENT, OCEAN_GRADIENT, FOREST_GRADIENT, NEON_GRADIENT,
        AURORA_MESH, CANDY_MESH,
        GOLDEN_SPARKLES, RAINING_HEARTS, BUBBLES,
        WARM_GLOW, COOL_BLOOM
    )
    
    fun getPresetsByType(type: LayerType): List<LayerPreset> {
        return ALL_PRESETS.filter { it.layerType == type }
    }
}
