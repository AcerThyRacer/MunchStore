package com.sugarmunch.app.theme.components

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Modular Theme Component System
 * 
 * Each component is a self-contained theme element that can be:
 * - Mixed and matched with other components
 * - Independently configured
 * - Enabled/disabled
 * - Shared and traded
 * 
 * This enables a "Photoshop for themes" approach where users
 * build custom themes from modular components.
 */

/**
 * Base interface for all theme components
 */
interface ThemeComponent {
    val id: String
    val name: String
    val description: String
    val version: String
    val author: String
    val isPremium: Boolean
    val category: ComponentCategory
}

/**
 * Component categories for organization
 */
enum class ComponentCategory(val displayName: String, val icon: String) {
    GRADIENT("Gradients", "🌈"),
    PARTICLES("Particles", "✨"),
    EFFECTS("Effects", "💫"),
    ANIMATIONS("Animations", "🎬"),
    COLORS("Colors", "🎨"),
    TEXTURES("Textures", "📄"),
    LIGHTING("Lighting", "💡"),
    UI("UI Elements", "🖼️")
}

// ═════════════════════════════════════════════════════════════
// GRADIENT COMPONENTS
// ═════════════════════════════════════════════════════════════

/**
 * Mesh Gradient Component
 * Creates complex multi-point gradient meshes
 */
@Serializable
data class MeshGradientComponent(
    override val id: String = "mesh_gradient_${System.currentTimeMillis()}",
    override val name: String = "Mesh Gradient",
    override val description: String = "Complex multi-point gradient mesh",
    override val version: String = "1.0",
    override val author: String = "SugarMunch",
    override val isPremium: Boolean = false,
    override val category: ComponentCategory = ComponentCategory.GRADIENT,
    
    val controlPoints: List<GradientControlPoint>,
    val meshResolution: Int = 10,
    val animationType: MeshAnimationType = MeshAnimationType.FLOAT,
    val animationSpeed: Float = 1f,
    val smoothness: Float = 0.5f
) : ThemeComponent

@Serializable
data class GradientControlPoint(
    val x: Float, // 0-1
    val y: Float, // 0-1
    val color: Color,
    val radius: Float = 0.3f,
    val intensity: Float = 1f
)

@Serializable
enum class MeshAnimationType(val displayName: String) {
    FLOAT("Float"),
    PULSE("Pulse"),
    ROTATE("Rotate"),
    WAVE("Wave"),
    RANDOM("Random")
}

/**
 * Vortex Gradient Component
 * Creates swirling vortex gradient patterns
 */
@Serializable
data class VortexGradientComponent(
    override val id: String = "vortex_gradient_${System.currentTimeMillis()}",
    override val name: String = "Vortex Gradient",
    override val description: String = "Swirling vortex gradient pattern",
    override val version: String = "1.0",
    override val author: String = "SugarMunch",
    override val isPremium: Boolean = false,
    override val category: ComponentCategory = ComponentCategory.GRADIENT,
    
    val centerX: Float = 0.5f,
    val centerY: Float = 0.5f,
    val colors: List<Color>,
    val rotationSpeed: Float = 1f,
    val spiralTightness: Float = 1f,
    val armCount: Int = 3
) : ThemeComponent

// ═════════════════════════════════════════════════════════════
// PARTICLE COMPONENTS
// ═════════════════════════════════════════════════════════════

/**
 * Vortex Particle Component
 * Creates particles that swirl in vortex patterns
 */
@Serializable
data class VortexParticleComponent(
    override val id: String = "vortex_particles_${System.currentTimeMillis()}",
    override val name: String = "Vortex Particles",
    override val description: String = "Particles swirling in vortex patterns",
    override val version: String = "1.0",
    override val author: String = "SugarMunch",
    override val isPremium: Boolean = false,
    override val category: ComponentCategory = ComponentCategory.PARTICLES,
    
    val particleCount: IntRange = 50..100,
    val particleType: ParticleShape = ParticleShape.CIRCLE,
    val colors: List<Color>,
    val vortexCenterX: Float = 0.5f,
    val vortexCenterY: Float = 0.5f,
    val rotationSpeed: Float = 1f,
    val inwardPull: Float = 0.5f,
    val sizeVariation: Float = 0.5f
) : ThemeComponent

@Serializable
enum class ParticleShape(val displayName: String) {
    CIRCLE("Circle"),
    SQUARE("Square"),
    TRIANGLE("Triangle"),
    STAR("Star"),
    HEART("Heart"),
    DIAMOND("Diamond"),
    CUSTOM("Custom")
}

/**
 * Rain Particle Component
 * Creates falling rain-like particles
 */
@Serializable
data class RainParticleComponent(
    override val id: String = "rain_particles_${System.currentTimeMillis()}",
    override val name: String = "Rain Particles",
    override val description: String = "Falling rain-like particles",
    override val version: String = "1.0",
    override val author: String = "SugarMunch",
    override val isPremium: Boolean = false,
    override val category: ComponentCategory = ComponentCategory.PARTICLES,
    
    val particleCount: IntRange = 100..200,
    val fallSpeed: FloatRange = 2f..5f,
    val wind: Float = 0f,
    val length: FloatRange = 10f..30f,
    val colors: List<Color> = listOf(Color.White),
    val opacity: Float = 0.5f
) : ThemeComponent

// ═════════════════════════════════════════════════════════════
// EFFECT COMPONENTS
// ═════════════════════════════════════════════════════════════

/**
 * Bloom Effect Component
 * Creates glow/bloom effects
 */
@Serializable
data class BloomEffectComponent(
    override val id: String = "bloom_effect_${System.currentTimeMillis()}",
    override val name: String = "Bloom Effect",
    override val description: String = "Glow/bloom lighting effect",
    override val version: String = "1.0",
    override val author: String = "SugarMunch",
    override val isPremium: Boolean = false,
    override val category: ComponentCategory = ComponentCategory.EFFECTS,
    
    val intensity: Float = 1f,
    val threshold: Float = 0.8f,
    val radius: Float = 50f,
    val color: Color? = null,
    val animationType: BloomAnimationType = BloomAnimationType.PULSE,
    val animationSpeed: Float = 1f
) : ThemeComponent

@Serializable
enum class BloomAnimationType(val displayName: String) {
    NONE("None"),
    PULSE("Pulse"),
    WAVE("Wave"),
    RANDOM("Random")
}

/**
 * Chromatic Aberration Component
 * Creates RGB split effect
 */
@Serializable
data class ChromaticAberrationComponent(
    override val id: String = "chromatic_aberration_${System.currentTimeMillis()}",
    override val name: String = "Chromatic Aberration",
    override val description: String = "RGB split chromatic aberration effect",
    override val version: String = "1.0",
    override val author: String = "SugarMunch",
    override val isPremium: Boolean = true,
    override val category: ComponentCategory = ComponentCategory.EFFECTS,
    
    val intensity: Float = 0.02f,
    val direction: Float = 0f,
    val animationType: ChromaticAnimationType = ChromaticAnimationType.NONE,
    val animationSpeed: Float = 1f
) : ThemeComponent

@Serializable
enum class ChromaticAnimationType(val displayName: String) {
    NONE("None"),
    OSCILLATE("Oscillate"),
    ROTATE("Rotate"),
    RANDOM("Random")
}

// ═════════════════════════════════════════════════════════════
// ANIMATION COMPONENTS
// ═════════════════════════════════════════════════════════════

/**
 * Wave Animation Component
 * Creates wave-like motion animations
 */
@Serializable
data class WaveAnimationComponent(
    override val id: String = "wave_animation_${System.currentTimeMillis()}",
    override val name: String = "Wave Animation",
    override val description: String = "Wave-like motion animation",
    override val version: String = "1.0",
    override val author: String = "SugarMunch",
    override val isPremium: Boolean = false,
    override val category: ComponentCategory = ComponentCategory.ANIMATIONS,
    
    val wavelength: Float = 100f,
    val amplitude: Float = 20f,
    val speed: Float = 1f,
    val direction: Float = 0f,
    val phase: Float = 0f
) : ThemeComponent

/**
 * Pulse Animation Component
 * Creates rhythmic pulsing animations
 */
@Serializable
data class PulseAnimationComponent(
    override val id: String = "pulse_animation_${System.currentTimeMillis()}",
    override val name: String = "Pulse Animation",
    override val description: String = "Rhythmic pulsing animation",
    override val version: String = "1.0",
    override val author: String = "SugarMunch",
    override val isPremium: Boolean = false,
    override val category: ComponentCategory = ComponentCategory.ANIMATIONS,
    
    val frequency: Float = 1f, // pulses per second
    val amplitude: Float = 0.2f, // 0-1 scale variation
    val phase: Float = 0f,
    val easing: EasingType = EasingType.EASE_IN_OUT
) : ThemeComponent

@Serializable
enum class EasingType(val displayName: String) {
    LINEAR("Linear"),
    EASE_IN("Ease In"),
    EASE_OUT("Ease Out"),
    EASE_IN_OUT("Ease In Out"),
    BOUNCE("Bounce"),
    ELASTIC("Elastic")
}

// ═════════════════════════════════════════════════════════════
// COMPONENT LIBRARY
// ═════════════════════════════════════════════════════════════

/**
 * Registry of available theme components
 * In production, this would load from disk/network
 */
object ComponentLibrary {
    
    private val components = mutableMapOf<String, ThemeComponent>()
    
    /**
     * Register a component
     */
    fun register(component: ThemeComponent) {
        components[component.id] = component
    }
    
    /**
     * Get a component by ID
     */
    fun getComponent(id: String): ThemeComponent? = components[id]
    
    /**
     * Get all components in a category
     */
    fun getByCategory(category: ComponentCategory): List<ThemeComponent> {
        return components.values.filter { it.category == category }
    }
    
    /**
     * Get premium components
     */
    fun getPremiumComponents(): List<ThemeComponent> {
        return components.values.filter { it.isPremium }
    }
    
    /**
     * Get free components
     */
    fun getFreeComponents(): List<ThemeComponent> {
        return components.values.filter { !it.isPremium }
    }
    
    /**
     * Search components
     */
    fun search(query: String): List<ThemeComponent> {
        val lowerQuery = query.lowercase()
        return components.values.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery)
        }
    }
    
    // Register default components
    init {
        register(MeshGradientComponent())
        register(VortexGradientComponent(colors = listOf(Color(0xFFFF69B4), Color(0xFF9370DB))))
        register(VortexParticleComponent(colors = listOf(Color(0xFFFF69B4), Color(0xFF00BFFF))))
        register(RainParticleComponent())
        register(BloomEffectComponent())
        register(ChromaticAberrationComponent())
        register(WaveAnimationComponent())
        register(PulseAnimationComponent())
    }
}

/**
 * A complete theme built from components
 */
@Serializable
data class ComponentTheme(
    val name: String,
    val description: String,
    val components: List<ThemeComponent>,
    val componentConfigs: Map<String, ComponentConfig> = emptyMap()
) {
    /**
     * Get config for a specific component
     */
    fun getConfig(componentId: String): ComponentConfig? = componentConfigs[componentId]
    
    /**
     * Update config for a component
     */
    fun updateConfig(componentId: String, config: ComponentConfig): ComponentTheme {
        return copy(
            componentConfigs = componentConfigs + (componentId to config)
        )
    }
}

/**
 * Configuration for a component instance
 */
@Serializable
data class ComponentConfig(
    val enabled: Boolean = true,
    val opacity: Float = 1f,
    val customProperties: Map<String, Float> = emptyMap()
)
