package com.sugarmunch.app.ui.lighting

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.*

/**
 * Dynamic lighting system with multiple light types
 * Supports point lights, directional lights, spot lights, area lights, and ambient
 */
class DynamicLightingSystem {
    // Light sources
    val pointLights = mutableListOf<PointLight>()
    val directionalLights = mutableListOf<DirectionalLight>()
    val spotLights = mutableListOf<SpotLight>()
    val areaLights = mutableListOf<AreaLight>()
    
    // Global settings
    var ambientLight = AmbientLight(Color(0xFF404040), 0.3f)
    var globalIntensity = 1.0f
    var shadowEnabled = true
    
    /**
     * Add a point light
     */
    fun addPointLight(
        position: Offset,
        color: Color,
        intensity: Float = 1f,
        radius: Float = 200f
    ) {
        pointLights.add(PointLight(position, color, intensity, radius))
    }
    
    /**
     * Add a directional light (sun/moon)
     */
    fun addDirectionalLight(
        direction: Offset,
        color: Color,
        intensity: Float = 1f
    ) {
        directionalLights.add(DirectionalLight(direction, color, intensity))
    }
    
    /**
     * Add a spot light
     */
    fun addSpotLight(
        position: Offset,
        direction: Offset,
        color: Color,
        intensity: Float = 1f,
        coneAngle: Float = 45f,
        radius: Float = 300f
    ) {
        spotLights.add(SpotLight(position, direction, color, intensity, coneAngle, radius))
    }
    
    /**
     * Add an area light
     */
    fun addAreaLight(
        topLeft: Offset,
        size: androidx.compose.ui.geometry.Size,
        color: Color,
        intensity: Float = 1f
    ) {
        areaLights.add(AreaLight(topLeft, size, color, intensity))
    }
    
    /**
     * Clear all lights
     */
    fun clear() {
        pointLights.clear()
        directionalLights.clear()
        spotLights.clear()
        areaLights.clear()
    }
    
    /**
     * Apply preset lighting
     */
    fun applyPreset(preset: LightingPreset) {
        clear()
        
        when (preset) {
            is LightingPreset.GoldenHour -> {
                addDirectionalLight(Offset(1f, 0.5f), Color(0xFFFFD700), 1.2f)
                ambientLight = AmbientLight(Color(0xFFFF8C00), 0.4f)
            }
            is LightingPreset.NeonCity -> {
                addPointLight(Offset(200f, 200f), Color(0xFFFF00FF), 1.5f, 300f)
                addPointLight(Offset(600f, 300f), Color(0xFF00FFFF), 1.5f, 300f)
                addPointLight(Offset(400f, 500f), Color(0xFF00FF00), 1.5f, 250f)
                ambientLight = AmbientLight(Color(0xFF202040), 0.3f)
            }
            is LightingPreset.Candlelight -> {
                addPointLight(preset.position, Color(0xFFFFA500), 0.8f, 150f)
                ambientLight = AmbientLight(Color(0xFF302010), 0.2f)
            }
            is LightingPreset.StudioLighting -> {
                addDirectionalLight(Offset(0.5f, 1f), Color(0xFFFFFFFF), 1.0f) // Key light
                addDirectionalLight(Offset(-0.3f, 0.5f), Color(0xFFCCCCCC), 0.5f) // Fill light
                addDirectionalLight(Offset(0f, -0.5f), Color(0xFFFFA500), 0.3f) // Back light
                ambientLight = AmbientLight(Color(0xFF606060), 0.3f)
            }
            is LightingPreset.Moonlight -> {
                addDirectionalLight(Offset(-0.5f, 0.3f), Color(0xFFB0C4DE), 0.6f)
                ambientLight = AmbientLight(Color(0xFF102030), 0.3f)
            }
        }
    }
    
    /**
     * Calculate lighting at a point
     */
    fun calculateLightingAt(
        position: Offset,
        normal: Offset,
        viewDirection: Offset
    ): LightingResult {
        var ambient = ambientLight.color
        var diffuse = Color.Black
        var specular = Color.Black
        
        // Directional lights
        directionalLights.forEach { light ->
            val lightDir = normalize(light.direction)
            val diff = maxOf(0f, dot(normal, lightDir))
            
            diffuse += light.color * diff * light.intensity * globalIntensity
            
            // Specular (Blinn-Phong)
            val halfDir = normalize(lightDir + viewDirection)
            val spec = pow(maxOf(0f, dot(normal, halfDir)), 32f)
            specular += light.color * spec * light.intensity * globalIntensity
        }
        
        // Point lights
        pointLights.forEach { light ->
            val lightDir = normalize(light.position - position)
            val distance = distance(light.position, position)
            val attenuation = 1f / (1f + distance * 0.01f)
            val diff = maxOf(0f, dot(normal, lightDir))
            
            diffuse += light.color * diff * light.intensity * attenuation * globalIntensity
            
            // Specular
            val halfDir = normalize(lightDir + viewDirection)
            val spec = pow(maxOf(0f, dot(normal, halfDir)), 32f)
            specular += light.color * spec * light.intensity * attenuation * globalIntensity
        }
        
        // Spot lights
        spotLights.forEach { light ->
            val lightDir = normalize(light.position - position)
            val distance = distance(light.position, position)
            
            // Check if in cone
            val angle = acos(dot(lightDir, normalize(light.direction)))
            val coneAngleRad = Math.toRadians(light.coneAngle.toDouble()).toFloat()
            
            if (angle < coneAngleRad) {
                val attenuation = 1f / (1f + distance * 0.01f)
                val spotlightEffect = (cos(angle) / cos(coneAngleRad)).pow(16f)
                val diff = maxOf(0f, dot(normal, lightDir))
                
                diffuse += light.color * diff * light.intensity * attenuation * spotlightEffect * globalIntensity
            }
        }
        
        return LightingResult(ambient, diffuse, specular)
    }
}

/**
 * Point light source
 */
data class PointLight(
    val position: Offset,
    val color: Color,
    val intensity: Float,
    val radius: Float
)

/**
 * Directional light (sun/moon)
 */
data class DirectionalLight(
    val direction: Offset,
    val color: Color,
    val intensity: Float
)

/**
 * Spot light with cone angle
 */
data class SpotLight(
    val position: Offset,
    val direction: Offset,
    val color: Color,
    val intensity: Float,
    val coneAngle: Float,
    val radius: Float
)

/**
 * Area light (rectangle)
 */
data class AreaLight(
    val topLeft: Offset,
    val size: androidx.compose.ui.geometry.Size,
    val color: Color,
    val intensity: Float
)

/**
 * Ambient light
 */
data class AmbientLight(
    val color: Color,
    val intensity: Float
)

/**
 * Lighting calculation result
 */
data class LightingResult(
    val ambient: Color,
    val diffuse: Color,
    val specular: Color
) {
    operator fun plus(other: LightingResult): LightingResult {
        return LightingResult(
            ambient + other.ambient,
            diffuse + other.diffuse,
            specular + other.specular
        )
    }
}

/**
 * Lighting presets
 */
sealed class LightingPreset {
    data object GoldenHour : LightingPreset()
    data object NeonCity : LightingPreset()
    data class Candlelight(val position: Offset = Offset(400f, 300f)) : LightingPreset()
    data object StudioLighting : LightingPreset()
    data object Moonlight : LightingPreset()
}

// Vector math helpers
private fun normalize(v: Offset): Offset {
    val len = sqrt(v.x * v.x + v.y * v.y)
    return if (len > 0) Offset(v.x / len, v.y / len) else v
}

private fun dot(a: Offset, b: Offset): Float = a.x * b.x + a.y * b.y

private fun distance(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}

private operator fun Color.plus(other: Color): Color {
    return Color(
        (red + other.red).coerceIn(0f, 1f),
        (green + other.green).coerceIn(0f, 1f),
        (blue + other.blue).coerceIn(0f, 1f),
        (alpha + other.alpha).coerceIn(0f, 1f)
    )
}

private operator fun Color.times(scalar: Float): Color {
    return Color(
        red * scalar,
        green * scalar,
        blue * scalar,
        alpha
    )
}

private fun pow(base: Float, exponent: Float): Float = base.pow(exponent)
