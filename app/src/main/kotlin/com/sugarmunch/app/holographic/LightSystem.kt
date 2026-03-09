package com.sugarmunch.app.holographic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Light System - Dynamic light sources and volumetric lighting
 * 
 * Features:
 * - Multiple dynamic light sources
 * - Real-time shadow casting
 * - Volumetric light effects
 * - Ambient occlusion
 * - Light color temperature
 */
class LightSystem {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Active light sources
    private val _lightSources = MutableStateFlow<List<LightSource>>(emptyList())
    val lightSources: StateFlow<List<LightSource>> = _lightSources.asStateFlow()

    // Global lighting state
    private val _globalLighting = MutableStateFlow(GlobalLightingState())
    val globalLighting: StateFlow<GlobalLightingState> = _globalLighting.asStateFlow()

    // Lighting configuration
    var lightingConfig = LightingConfig(
        ambientLightIntensity = 0.3f,
        ambientLightColor = Color.White,
        shadowQuality = ShadowQuality.HIGH,
        volumetricEnabled = true,
        volumetricDensity = 0.5f,
        lightFalloff = 0.1f
    )

    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        startLightingLoop()
    }

    fun stop() {
        isRunning = false
    }

    private fun startLightingLoop() {
        scope.launch {
            var time = 0f

            while (isRunning) {
                time += 0.016f

                // Update animated lights
                _lightSources.value = _lightSources.value.map { light ->
                    if (light.isAnimated) {
                        light.copy(
                            position = animateLightPosition(light, time),
                            intensity = animateLightIntensity(light, time),
                            color = animateLightColor(light, time)
                        )
                    } else {
                        light
                    }
                }

                // Update global lighting state
                _globalLighting.value = GlobalLightingState(
                    time = time,
                    totalLightIntensity = calculateTotalLightIntensity(),
                    dominantLightDirection = calculateDominantLightDirection(),
                    averageColorTemperature = calculateAverageColorTemperature()
                )

                delay(16)
            }
        }
    }

    private fun animateLightPosition(light: LightSource, time: Float): Offset {
        return when (light.animationType) {
            LightAnimationType.ORBIT -> {
                val orbitRadius = light.orbitRadius ?: 100f
                Offset(
                    light.basePosition.x + cos(time * light.animationSpeed) * orbitRadius,
                    light.basePosition.y + sin(time * light.animationSpeed) * orbitRadius
                )
            }
            LightAnimationType.PULSE -> {
                val pulseAmount = sin(time * light.animationSpeed) * 20
                Offset(
                    light.basePosition.x,
                    light.basePosition.y + pulseAmount
                )
            }
            LightAnimationType.FIGURE8 -> {
                val scale = 50f
                Offset(
                    light.basePosition.x + sin(time * light.animationSpeed) * scale,
                    light.basePosition.y + sin(time * light.animationSpeed * 2) * scale
                )
            }
            LightAnimationType.NONE -> light.position
        }
    }

    private fun animateLightIntensity(light: LightSource, time: Float): Float {
        return when (light.animationType) {
            LightAnimationType.FLICKER -> {
                light.baseIntensity * (0.8f + Math.random().toFloat() * 0.4f)
            }
            LightAnimationType.PULSE -> {
                light.baseIntensity * (0.7f + 0.3f * sin(time * light.animationSpeed))
            }
            else -> light.baseIntensity
        }
    }

    private fun animateLightColor(light: LightSource, time: Float): Color {
        return when (light.animationType) {
            LightAnimationType.COLOR_CYCLE -> {
                val hue = (time * light.animationSpeed * 50) % 360
                Color.hsl(hue, light.colorSaturation, light.colorLightness)
            }
            else -> light.color
        }
    }

    private fun calculateTotalLightIntensity(): Float {
        return _lightSources.value.sumOf { it.intensity } + lightingConfig.ambientLightIntensity
    }

    private fun calculateDominantLightDirection(): Offset {
        if (_lightSources.value.isEmpty()) return Offset.Zero

        val brightestLight = _lightSources.value.maxByOrNull { it.intensity } ?: return Offset.Zero
        return (brightestLight.position - Offset(500f, 1000f)).let {
            if (it.getDistance() > 0) it / it.getDistance() else Offset.Zero
        }
    }

    private fun calculateAverageColorTemperature(): Float {
        if (_lightSources.value.isEmpty()) return 6500f

        return _lightSources.value.map { it.colorTemperature }.average().toFloat()
    }

    // ========== LIGHT SOURCE MANAGEMENT ==========

    fun addLightSource(light: LightSource) {
        _lightSources.value = _lightSources.value + light
    }

    fun removeLightSource(id: String) {
        _lightSources.value = _lightSources.value.filter { it.id != id }
    }

    fun updateLightSource(id: String, update: LightSource.() -> LightSource) {
        _lightSources.value = _lightSources.value.map { light ->
            if (light.id == id) light.update() else light
        }
    }

    fun clearLightSources() {
        _lightSources.value = emptyList()
    }

    // ========== PRESET LIGHT CONFIGURATIONS ==========

    fun createSunLight(): LightSource {
        return LightSource(
            id = "sun_${System.currentTimeMillis()}",
            type = LightType.DIRECTIONAL,
            position = Offset(800f, 200f),
            direction = Offset(-1f, 1f),
            intensity = 1.0f,
            color = Color(0xFFFFF5E6),
            colorTemperature = 5500f,
            castShadows = true,
            shadowSoftness = 0.3f
        )
    }

    fun createMoonLight(): LightSource {
        return LightSource(
            id = "moon_${System.currentTimeMillis()}",
            type = LightType.DIRECTIONAL,
            position = Offset(200f, 300f),
            direction = Offset(1f, 1f),
            intensity = 0.3f,
            color = Color(0xFFE6F3FF),
            colorTemperature = 8000f,
            castShadows = true,
            shadowSoftness = 0.5f
        )
    }

    fun createPointLight(position: Offset, color: Color = Color.White): LightSource {
        return LightSource(
            id = "point_${System.currentTimeMillis()}",
            type = LightType.POINT,
            position = position,
            intensity = 0.8f,
            color = color,
            colorTemperature = 4000f,
            castShadows = true,
            shadowSoftness = 0.2f,
            radius = 100f
        )
    }

    fun createSpotLight(
        position: Offset,
        direction: Offset,
        color: Color = Color.White
    ): LightSource {
        return LightSource(
            id = "spot_${System.currentTimeMillis()}",
            type = LightType.SPOT,
            position = position,
            direction = direction,
            intensity = 1.0f,
            color = color,
            colorTemperature = 3200f,
            castShadows = true,
            shadowSoftness = 0.1f,
            spotAngle = 30f
        )
    }

    fun createAnimatedLight(
        position: Offset,
        animationType: LightAnimationType,
        color: Color = Color.White
    ): LightSource {
        return LightSource(
            id = "animated_${System.currentTimeMillis()}",
            type = LightType.POINT,
            position = position,
            basePosition = position,
            intensity = 0.7f,
            baseIntensity = 0.7f,
            color = color,
            colorTemperature = 5000f,
            castShadows = true,
            isAnimated = true,
            animationType = animationType,
            animationSpeed = 1.0f
        )
    }

    // ========== SHADOW CALCULATIONS ==========

    fun calculateShadow(
        objectPosition: Offset,
        objectSize: androidx.compose.ui.geometry.Size,
        lightPosition: Offset
    ): Shadow {
        val lightDir = (objectPosition - lightPosition).let {
            if (it.getDistance() > 0) it / it.getDistance() else Offset.Zero
        }

        val shadowOffset = Offset(
            lightDir.x * 20 * lightingConfig.lightFalloff,
            lightDir.y * 20 * lightingConfig.lightFalloff
        )

        val shadowLight = _lightSources.value.firstOrNull { it.castShadows }
        val blurRadius = shadowLight?.shadowSoftness?.let { it * 30 } ?: 10f

        return Shadow(
            color = Color.Black.copy(alpha = 0.3f),
            blurRadius = blurRadius,
            offset = shadowOffset
        )
    }

    // ========== VOLUMETRIC LIGHTING ==========

    fun calculateVolumetricLight(
        viewPosition: Offset,
        lightPosition: Offset
    ): Float {
        if (!lightingConfig.volumetricEnabled) return 0f

        val distance = (viewPosition - lightPosition).getDistance()
        val baseIntensity = 1f / (1f + distance * lightingConfig.lightFalloff)

        return baseIntensity * lightingConfig.volumetricDensity
    }

    // ========== AMBIENT OCCLUSION ==========

    fun calculateAmbientOcclusion(
        position: Offset,
        nearbyObjects: List<Offset>
    ): Float {
        if (!lightingConfig.ambientOcclusion) return 1f

        var occlusion = 0f

        nearbyObjects.forEach { objPos ->
            val distance = (position - objPos).getDistance()
            if (distance < 100f) {
                occlusion += (100f - distance) / 100f
            }
        }

        return (1f - occlusion.coerceIn(0f, 1f) * 0.5f)
    }
}

/**
 * Light source configuration
 */
data class LightSource(
    val id: String,
    val type: LightType,
    val position: Offset,
    val basePosition: Offset = position,
    val direction: Offset = Offset.Zero,
    val intensity: Float,
    val baseIntensity: Float = intensity,
    val color: Color,
    val colorTemperature: Float = 6500f,
    val colorSaturation: Float = 0.5f,
    val colorLightness: Float = 0.5f,
    val castShadows: Boolean = true,
    val shadowSoftness: Float = 0.2f,
    val radius: Float = 50f,
    val spotAngle: Float = 45f,
    val isAnimated: Boolean = false,
    val animationType: LightAnimationType = LightAnimationType.NONE,
    val animationSpeed: Float = 1.0f,
    val orbitRadius: Float? = null
)

/**
 * Light types
 */
enum class LightType {
    DIRECTIONAL,  // Sun/moon-like infinite distance light
    POINT,        // Omnidirectional point light
    SPOT,         // Directional cone of light
    AREA          // Rectangular area light
}

/**
 * Light animation types
 */
enum class LightAnimationType {
    NONE,
    ORBIT,
    PULSE,
    FLICKER,
    FIGURE8,
    COLOR_CYCLE
}

/**
 * Global lighting state
 */
data class GlobalLightingState(
    val time: Float = 0f,
    val totalLightIntensity: Float = 0f,
    val dominantLightDirection: Offset = Offset.Zero,
    val averageColorTemperature: Float = 6500f,
    val isDaytime: Boolean = true
)

/**
 * Lighting configuration
 */
data class LightingConfig(
    val ambientLightIntensity: Float = 0.3f,
    val ambientLightColor: Color = Color.White,
    val shadowQuality: ShadowQuality = ShadowQuality.HIGH,
    val volumetricEnabled: Boolean = true,
    val volumetricDensity: Float = 0.5f,
    val lightFalloff: Float = 0.1f,
    val ambientOcclusion: Boolean = true,
    val maxLights: Int = 8
)

/**
 * Lighting presets
 */
object LightingPresets {

    val DAYLIGHT = LightingConfig(
        ambientLightIntensity = 0.8f,
        ambientLightColor = Color(0xFFFFF5E6),
        shadowQuality = ShadowQuality.HIGH,
        volumetricEnabled = true,
        volumetricDensity = 0.3f,
        lightFalloff = 0.05f
    )

    val SUNSET = LightingConfig(
        ambientLightIntensity = 0.5f,
        ambientLightColor = Color(0xFFFFD7A0),
        shadowQuality = ShadowQuality.MEDIUM,
        volumetricEnabled = true,
        volumetricDensity = 0.7f,
        lightFalloff = 0.08f
    )

    val NIGHT = LightingConfig(
        ambientLightIntensity = 0.1f,
        ambientLightColor = Color(0xFF1A237E),
        shadowQuality = ShadowQuality.LOW,
        volumetricEnabled = true,
        volumetricDensity = 0.4f,
        lightFalloff = 0.15f
    )

    val STUDIO = LightingConfig(
        ambientLightIntensity = 0.6f,
        ambientLightColor = Color.White,
        shadowQuality = ShadowQuality.ULTRA,
        volumetricEnabled = true,
        volumetricDensity = 0.6f,
        lightFalloff = 0.03f
    )

    val DRAMATIC = LightingConfig(
        ambientLightIntensity = 0.2f,
        ambientLightColor = Color(0xFF1A1A2E),
        shadowQuality = ShadowQuality.HIGH,
        volumetricEnabled = true,
        volumetricDensity = 0.8f,
        lightFalloff = 0.2f
    )
}
