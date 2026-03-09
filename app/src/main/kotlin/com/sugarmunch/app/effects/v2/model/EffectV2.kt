package com.sugarmunch.app.effects.v2.model

import android.content.Context
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * SugarRush Effect System V2 - Maximum Intensity
 */
interface EffectV2 {
    val id: String
    val name: String
    val description: String
    val category: EffectCategory
    val type: EffectType
    val icon: String // Emoji icon
    
    // Intensity support
    val minIntensity: Float
    val maxIntensity: Float
    val defaultIntensity: Float
    
    // Effect capabilities
    val hasVisual: Boolean
    val hasHaptic: Boolean
    val hasAudio: Boolean
    
    // Settings
    val settings: List<EffectSetting>
    
    /**
     * Enable effect with specific intensity (0.0 - 2.0)
     */
    fun enable(context: Context, windowManager: WindowManager, intensity: Float = 1f)
    
    /**
     * Disable effect
     */
    fun disable()
    
    /**
     * Check if effect is currently active
     */
    fun isActive(): Boolean
    
    /**
     * Set intensity while active
     */
    fun setIntensity(intensity: Float)
    
    /**
     * Get current intensity
     */
    fun getCurrentIntensity(): Float
    
    /**
     * Preview composable for UI
     */
    @Composable
    fun Preview(intensity: Float)
}

enum class EffectCategory {
    VISUAL_OVERLAY,    // Screen overlays (tint, dim, etc)
    PARTICLES,         // Particle effects
    ANIMATIONS,        // Motion/animation effects
    HAPTIC,           // Vibration patterns
    AUDIO,            // Sound effects
    SYSTEM            // System-level effects
}

enum class EffectType {
    OVERLAY,          // Window overlay effect
    BACKGROUND,       // Background-running effect
    INTERACTIVE,      // User-interactive effect
    TRIGGERED         // Event-triggered effect
}

sealed class EffectSetting {
    abstract val key: String
    abstract val label: String
    
    data class Slider(
        override val key: String,
        override val label: String,
        val min: Float,
        val max: Float,
        val default: Float,
        val step: Float = 0.1f
    ) : EffectSetting()
    
    data class Toggle(
        override val key: String,
        override val label: String,
        val default: Boolean
    ) : EffectSetting()
    
    data class ColorPicker(
        override val key: String,
        override val label: String,
        val defaultColor: Color
    ) : EffectSetting()
    
    data class Selection(
        override val key: String,
        override val label: String,
        val options: List<String>,
        val defaultIndex: Int
    ) : EffectSetting()
}

/**
 * Effect preset - combination of effects with settings
 */
data class EffectPreset(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val effects: List<EffectConfig>,
    val category: PresetCategory = PresetCategory.CUSTOM
) {
    val totalIntensity: Float
        get() = effects.map { it.intensity }.average().toFloat()
}

data class EffectConfig(
    val effectId: String,
    val intensity: Float = 1f,
    val settings: Map<String, Any> = emptyMap()
)

enum class PresetCategory {
    CHILL,          // Relaxed presets
    FOCUS,          // Productivity presets
    PARTY,          // Celebration presets
    TRIPPY,         // Psychedelic presets
    GAMING,         // Gaming-focused presets
    CUSTOM          // User-created
}

/**
 * Effect intensity levels
 */
object EffectIntensityLevels {
    const val MINIMUM = 0.2f
    const val SUBTLE = 0.5f
    const val NORMAL = 1.0f
    const val ENHANCED = 1.5f
    const val MAXIMUM = 2.0f
}

/**
 * Effect state for UI
 */
data class EffectState(
    val effect: EffectV2,
    val isActive: Boolean,
    val currentIntensity: Float,
    val settings: Map<String, Any>
)

/**
 * Effect combination/chain
 */
data class EffectChain(
    val id: String,
    val name: String,
    val effects: List<EffectConfig>,
    val trigger: ChainTrigger,
    val duration: Long? = null // null = indefinite
)

sealed class ChainTrigger {
    data object Manual : ChainTrigger()
    data class Timer(val intervalMs: Long) : ChainTrigger()
    data class Event(val eventType: String) : ChainTrigger()
    data class Shake(val threshold: Float) : ChainTrigger()
}
