package com.sugarmunch.app.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import kotlin.math.*

/**
 * Phase 2: Volumetric Lighting System
 * 
 * Real-time god rays, light shafts, and atmospheric lighting effects.
 * Creates dramatic lighting that appears to shine through UI elements.
 */

// ─────────────────────────────────────────────────────────────────────────────────
// LIGHT SOURCE TYPES
// ─────────────────────────────────────────────────────────────────────────────────

enum class LightSourceType {
    POINT,          // Single point light
    DIRECTIONAL,    // Directional light (sun-like)
    SPOT,           // Cone-shaped spotlight
    AREA,           // Rectangular area light
    VOLUMETRIC      // Volumetric fog light
}

enum class LightQuality {
    SOFT,           // Soft, diffused light
    HARD,           // Hard, sharp shadows
    DRAMATIC,       // High contrast dramatic lighting
    ETHEREAL        // Dreamy, ethereal glow
}

// ─────────────────────────────────────────────────────────────────────────────────
// LIGHT CONFIGURATION
// ─────────────────────────────────────────────────────────────────────────────────

data class LightSource(
    val id: String,
    val type: LightSourceType,
    val position: Offset = Offset.Zero,
    val direction: Float = 0f,          // For directional/spot lights (degrees)
    val color: Color = Color.White,
    val intensity: Float = 1f,
    val radius: Float = 100f,
    val angle: Float = 45f,             // For spotlights (cone angle)
    val quality: LightQuality = LightQuality.SOFT,
    val animated: Boolean = true,
    val flickerIntensity: Float = 0f,   // 0-1, for fire/candle effects
    val pulseSpeed: Float = 1f
)

data class VolumetricConfig(
    val lightSources: List<LightSource> = emptyList(),
    val globalIntensity: Float = 1f,
    val rayCount: Int = 100,
    val rayLength: Float = 0.8f,        // Relative to screen size
    val rayWidth: Float = 0.02f,        // Relative to screen size
    val scattering: Float = 0.5f,       // Light scattering amount
    val dustParticles: Boolean = true,
    val dustCount: Int = 50,
    val atmosphericFog: Float = 0.2f,
    val colorBleed: Float = 0.3f,       // Color spreading into surroundings
    val godRayDecay: Float = 0.95f,     // How quickly rays fade
    val obstructionSoftness: Float = 0.5f
)

// ─────────────────────────────────────────────────────────────────────────────────
// DUST PARTICLE
// ─────────────────────────────────────────────────────────────────────────────────

private data class DustParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val brightness: Float,
    val drift: Float
)

// ─────────────────────────────────────────────────────────────────────────────────
// VOLUMETRIC LIGHTING RENDERER
// ─────────────────────────────────────────────────────────────────────────────────

@Composable
fun VolumetricLightingOverlay(
    config: VolumetricConfig,
    time: Float,
    modifier: Modifier = Modifier
) {
    // Pre-generate dust particles
    val dustParticles = remember(config.dustCount) {
        List(config.dustCount) { index ->
            val random = kotlin.random.Random(index)
            DustParticle(
                x = random.nextFloat(),
                y = random.nextFloat(),
                size = 1f + random.nextFloat() * 3f,
                speed = 0.1f + random.nextFloat() * 0.3f,
                brightness = 0.3f + random.nextFloat() * 0.7f,
                drift = random.nextFloat() * 2f - 1f
            )
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw atmospheric fog first
        if (config.atmosphericFog > 0) {
            drawAtmosphericFog(config, time)
        }
        
        // Draw each light source
        for (light in config.lightSources) {
            drawLightSource(light, config, time, size)
        }
        
        // Draw dust particles
        if (config.dustParticles) {
            drawDustParticles(dustParticles, config, time)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// LIGHT SOURCE RENDERING
// ─────────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawLightSource(
    light: LightSource,
    config: VolumetricConfig,
    time: Float,
    canvasSize: Size
) {
    val intensity = light.intensity * config.globalIntensity
    
    // Apply animation
    val animatedIntensity = if (light.animated) {
        val pulse = 1f + sin(time * light.pulseSpeed * 2f * PI.toFloat()) * 0.1f
        val flicker = if (light.flickerIntensity > 0) {
            1f - (kotlin.random.Random(time.toInt() * 100 + light.id.hashCode()).nextFloat() * light.flickerIntensity)
        } else 1f
        intensity * pulse * flicker
    } else intensity
    
    when (light.type) {
        LightSourceType.POINT -> drawPointLight(light, config, time, canvasSize, animatedIntensity)
        LightSourceType.DIRECTIONAL -> drawDirectionalLight(light, config, time, canvasSize, animatedIntensity)
        LightSourceType.SPOT -> drawSpotlight(light, config, time, canvasSize, animatedIntensity)
        LightSourceType.AREA -> drawAreaLight(light, config, time, canvasSize, animatedIntensity)
        LightSourceType.VOLUMETRIC -> drawVolumetricLight(light, config, time, canvasSize, animatedIntensity)
    }
}

/**
 * Point light - radiates in all directions
 */
private fun DrawScope.drawPointLight(
    light: LightSource,
    config: VolumetricConfig,
    time: Float,
    canvasSize: Size,
    intensity: Float
) {
    val centerX = light.position.x.takeIf { it != 0f } ?: canvasSize.width / 2f
    val centerY = light.position.y.takeIf { it != 0f } ?: canvasSize.height / 2f
    val maxRadius = light.radius * config.rayLength * max(canvasSize.width, canvasSize.height)
    
    // Draw god rays
    val rayCount = config.rayCount
    for (i in 0 until rayCount) {
        val angle = (i.toFloat() / rayCount) * 2f * PI.toFloat()
        val rayLength = maxRadius * (0.5f + 0.5f * sin(angle * 3f + time * light.pulseSpeed))
        
        val endX = centerX + cos(angle) * rayLength
        val endY = centerY + sin(angle) * rayLength
        
        val rayAlpha = (1f - (i % (rayCount / 4)) / (rayCount / 4f)) * 
                       0.1f * intensity * config.godRayDecay
        
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    light.color.copy(alpha = rayAlpha),
                    light.color.copy(alpha = rayAlpha * 0.5f),
                    Color.Transparent
                ),
                start = Offset(centerX, centerY),
                end = Offset(endX, endY)
            ),
            start = Offset(centerX, centerY),
            end = Offset(endX, endY),
            strokeWidth = canvasSize.width * config.rayWidth
        )
    }
    
    // Draw central glow
    val glowRadius = light.radius * intensity
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                light.color.copy(alpha = 0.4f * intensity),
                light.color.copy(alpha = 0.2f * intensity),
                light.color.copy(alpha = 0.05f * intensity),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = glowRadius
        ),
        radius = glowRadius,
        center = Offset(centerX, centerY),
        blendMode = BlendMode.Screen
    )
    
    // Light core
    drawCircle(
        color = Color.White.copy(alpha = 0.8f * intensity),
        radius = light.radius * 0.1f,
        center = Offset(centerX, centerY),
        blendMode = BlendMode.Screen
    )
}

/**
 * Directional light - parallel rays like sunlight
 */
private fun DrawScope.drawDirectionalLight(
    light: LightSource,
    config: VolumetricConfig,
    time: Float,
    canvasSize: Size,
    intensity: Float
) {
    val angleRad = Math.toRadians(light.direction.toDouble()).toFloat()
    val rayLength = max(canvasSize.width, canvasSize.height) * config.rayLength
    
    // Calculate ray start and end points
    val rayDirX = cos(angleRad)
    val rayDirY = sin(angleRad)
    val perpX = -rayDirY
    val perpY = rayDirX
    
    val rayCount = config.rayCount
    val spacing = max(canvasSize.width, canvasSize.height) / rayCount
    
    for (i in 0 until rayCount) {
        val offset = (i - rayCount / 2f) * spacing
        val startX = canvasSize.width / 2f + perpX * offset - rayDirX * rayLength * 0.3f
        val startY = canvasSize.height / 2f + perpY * offset - rayDirY * rayLength * 0.3f
        val endX = startX + rayDirX * rayLength
        val endY = startY + rayDirY * rayLength
        
        // Animated intensity variation
        val rayIntensity = intensity * (0.7f + 0.3f * sin(i * 0.1f + time * light.pulseSpeed))
        val rayAlpha = 0.08f * rayIntensity * config.godRayDecay
        
        // Width variation for natural look
        val width = canvasSize.width * config.rayWidth * (0.8f + 0.4f * sin(i * 0.2f + time))
        
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    light.color.copy(alpha = rayAlpha),
                    light.color.copy(alpha = rayAlpha * 0.7f),
                    Color.Transparent
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = width
        )
    }
    
    // Light source indicator (sun)
    val sunX = canvasSize.width / 2f - rayDirX * rayLength * 0.4f
    val sunY = canvasSize.height / 2f - rayDirY * rayLength * 0.4f
    val sunRadius = 50f * intensity
    
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.9f * intensity),
                light.color.copy(alpha = 0.6f * intensity),
                light.color.copy(alpha = 0.2f * intensity),
                Color.Transparent
            ),
            center = Offset(sunX, sunY),
            radius = sunRadius * 2
        ),
        radius = sunRadius * 2,
        center = Offset(sunX, sunY),
        blendMode = BlendMode.Screen
    )
}

/**
 * Spotlight - cone-shaped light
 */
private fun DrawScope.drawSpotlight(
    light: LightSource,
    config: VolumetricConfig,
    time: Float,
    canvasSize: Size,
    intensity: Float
) {
    val centerX = light.position.x.takeIf { it != 0f } ?: canvasSize.width / 2f
    val centerY = light.position.y.takeIf { it != 0f } ?: canvasSize.height * 0.2f
    
    val angleRad = Math.toRadians(light.direction.toDouble()).toFloat()
    val coneAngle = Math.toRadians(light.angle.toDouble()).toFloat()
    val coneLength = light.radius * config.rayLength * max(canvasSize.width, canvasSize.height)
    
    // Draw cone shape with god rays
    val rayCount = config.rayCount / 2
    for (i in 0 until rayCount) {
        val t = i.toFloat() / rayCount
        val rayAngle = angleRad - coneAngle / 2 + t * coneAngle
        
        val endX = centerX + cos(rayAngle).toFloat() * coneLength
        val endY = centerY + sin(rayAngle).toFloat() * coneLength
        
        // Intensity falls off towards edges
        val edgeFade = 1f - abs(t - 0.5f) * 2f
        val rayAlpha = 0.1f * intensity * edgeFade * config.godRayDecay
        
        // Animated shimmer
        val shimmer = 0.8f + 0.2f * sin(t * 10f + time * light.pulseSpeed * 3f)
        
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    light.color.copy(alpha = rayAlpha * shimmer),
                    light.color.copy(alpha = rayAlpha * 0.5f * shimmer),
                    Color.Transparent
                ),
                start = Offset(centerX, centerY),
                end = Offset(endX, endY)
            ),
            start = Offset(centerX, centerY),
            end = Offset(endX, endY),
            strokeWidth = canvasSize.width * config.rayWidth * 2
        )
    }
    
    // Cone edge glow
    val path = Path().apply {
        moveTo(centerX, centerY)
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                centerX - coneLength,
                centerY - coneLength,
                centerX + coneLength,
                centerY + coneLength
            ),
            startAngleDegrees = light.direction - light.angle / 2,
            sweepAngleDegrees = light.angle,
            forceMoveTo = false
        )
        close()
    }
    
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                light.color.copy(alpha = 0.15f * intensity),
                light.color.copy(alpha = 0.05f * intensity),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = coneLength
        ),
        blendMode = BlendMode.Screen
    )
    
    // Spotlight source
    drawCircle(
        color = light.color.copy(alpha = 0.8f * intensity),
        radius = 15f,
        center = Offset(centerX, centerY),
        blendMode = BlendMode.Screen
    )
}

/**
 * Area light - rectangular light source
 */
private fun DrawScope.drawAreaLight(
    light: LightSource,
    config: VolumetricConfig,
    time: Float,
    canvasSize: Size,
    intensity: Float
) {
    val centerX = light.position.x.takeIf { it != 0f } ?: canvasSize.width / 2f
    val centerY = light.position.y.takeIf { it != 0f } ?: canvasSize.height / 2f
    val width = light.radius * 2
    val height = light.radius * 0.5f
    
    // Draw light panel
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                light.color.copy(alpha = 0.6f * intensity),
                light.color.copy(alpha = 0.4f * intensity),
                light.color.copy(alpha = 0.6f * intensity)
            ),
            start = Offset(centerX - width / 2, centerY - height / 2),
            end = Offset(centerX - width / 2, centerY + height / 2)
        ),
        topLeft = Offset(centerX - width / 2, centerY - height / 2),
        size = Size(width, height),
        blendMode = BlendMode.Screen
    )
    
    // Draw light rays from panel edges
    val rayCount = config.rayCount / 2
    for (i in 0 until rayCount) {
        val t = i.toFloat() / rayCount
        val startX = centerX - width / 2 + t * width
        val rayLength = light.radius * config.rayLength * 
                       (1f + 0.3f * sin(t * PI.toFloat()))
        
        // Rays spread downward
        val endX = startX + (startX - centerX) * 0.3f
        val endY = centerY + height / 2 + rayLength
        
        val rayAlpha = 0.05f * intensity * config.godRayDecay
        
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    light.color.copy(alpha = rayAlpha),
                    Color.Transparent
                ),
                start = Offset(startX, centerY + height / 2),
                end = Offset(endX, endY)
            ),
            start = Offset(startX, centerY + height / 2),
            end = Offset(endX, endY),
            strokeWidth = width / rayCount
        )
    }
}

/**
 * Volumetric light - fog-like atmospheric light
 */
private fun DrawScope.drawVolumetricLight(
    light: LightSource,
    config: VolumetricConfig,
    time: Float,
    canvasSize: Size,
    intensity: Float
) {
    val centerX = light.position.x.takeIf { it != 0f } ?: canvasSize.width / 2f
    val centerY = light.position.y.takeIf { it != 0f } ?: canvasSize.height / 2f
    
    // Multiple layers of volumetric fog
    for (layer in 0 until 5) {
        val layerRadius = light.radius * (1f + layer * 0.5f) * 
                         (1f + 0.1f * sin(time * light.pulseSpeed + layer))
        val layerAlpha = (0.15f - layer * 0.02f) * intensity
        
        // Irregular fog shape using multiple overlapping circles
        for (blob in 0 until 8) {
            val blobAngle = blob * PI.toFloat() / 4 + time * 0.2f + layer * 0.5f
            val blobDist = layerRadius * 0.3f * (1f + 0.5f * sin(blobAngle * 2))
            val blobX = centerX + cos(blobAngle) * blobDist
            val blobY = centerY + sin(blobAngle) * blobDist
            val blobRadius = layerRadius * (0.4f + 0.3f * sin(blob + time))
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        light.color.copy(alpha = layerAlpha),
                        light.color.copy(alpha = layerAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(blobX, blobY),
                    radius = blobRadius
                ),
                radius = blobRadius,
                center = Offset(blobX, blobY),
                blendMode = BlendMode.Screen
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// ATMOSPHERIC EFFECTS
// ─────────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawAtmosphericFog(
    config: VolumetricConfig,
    time: Float
) {
    // Base fog layer
    val fogAlpha = config.atmosphericFog * config.globalIntensity
    
    // Gradient fog (denser at bottom)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = fogAlpha * 0.3f),
                Color.White.copy(alpha = fogAlpha * 0.5f),
                Color.White.copy(alpha = fogAlpha)
            ),
            startY = 0f,
            endY = size.height
        ),
        size = size,
        blendMode = BlendMode.Softlight
    )
    
    // Animated fog wisps
    for (wisp in 0 until 5) {
        val wispY = size.height * (0.5f + wisp * 0.1f)
        val wispOffset = sin(time * 0.5f + wisp) * 50f
        
        val path = Path().apply {
            moveTo(0f, wispY)
            for (x in 0..size.width.toInt() step 50) {
                val y = wispY + sin((x * 0.01f + time + wisp).toDouble()).toFloat() * 30f
                lineTo(x.toFloat(), y)
            }
            lineTo(size.width, wispY + 50)
            lineTo(0f, wispY + 50)
            close()
        }
        
        drawPath(
            path = path,
            color = Color.White.copy(alpha = fogAlpha * 0.1f),
            blendMode = BlendMode.Softlight
        )
    }
}

private fun DrawScope.drawDustParticles(
    particles: List<DustParticle>,
    config: VolumetricConfig,
    time: Float
) {
    val intensity = config.globalIntensity
    
    for (particle in particles) {
        // Animate particle position
        val x = ((particle.x + time * particle.speed * 0.01f) % 1f) * size.width
        val y = ((particle.y + time * particle.speed * 0.005f + sin(time + particle.drift) * 0.001f) % 1f) * size.height
        
        // Brightness varies with position and time
        val brightness = particle.brightness * (0.5f + 0.5f * sin(time * 2f + particle.drift * 10f))
        val alpha = brightness * intensity * 0.3f
        
        // Draw particle with glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha),
                    Color.White.copy(alpha = alpha * 0.3f),
                    Color.Transparent
                ),
                center = Offset(x, y),
                radius = particle.size * 3
            ),
            radius = particle.size * 3,
            center = Offset(x, y),
            blendMode = BlendMode.Screen
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────
// PRESET LIGHTING CONFIGURATIONS
// ─────────────────────────────────────────────────────────────────────────────────

object VolumetricLightingPresets {
    
    fun goldenHour(): VolumetricConfig {
        return VolumetricConfig(
            lightSources = listOf(
                LightSource(
                    id = "sun",
                    type = LightSourceType.DIRECTIONAL,
                    direction = 30f,
                    color = Color(0xFFFFAA33),
                    intensity = 1.2f,
                    quality = LightQuality.SOFT,
                    pulseSpeed = 0.3f
                )
            ),
            globalIntensity = 0.8f,
            rayCount = 80,
            rayLength = 0.7f,
            scattering = 0.6f,
            dustParticles = true,
            dustCount = 30,
            atmosphericFog = 0.15f,
            colorBleed = 0.4f
        )
    }
    
    fun dramaticSpotlight(): VolumetricConfig {
        return VolumetricConfig(
            lightSources = listOf(
                LightSource(
                    id = "spot",
                    type = LightSourceType.SPOT,
                    position = Offset(0.5f, 0.1f), // Will be scaled
                    direction = 90f,
                    color = Color.White,
                    intensity = 1.5f,
                    angle = 30f,
                    quality = LightQuality.DRAMATIC,
                    pulseSpeed = 0.5f
                )
            ),
            globalIntensity = 1f,
            rayCount = 60,
            rayLength = 0.9f,
            scattering = 0.3f,
            dustParticles = true,
            dustCount = 80,
            atmosphericFog = 0.1f
        )
    }
    
    fun neonCity(): VolumetricConfig {
        return VolumetricConfig(
            lightSources = listOf(
                LightSource(
                    id = "neon1",
                    type = LightSourceType.POINT,
                    color = Color(0xFFFF00FF),
                    intensity = 0.8f,
                    radius = 150f,
                    pulseSpeed = 2f
                ),
                LightSource(
                    id = "neon2",
                    type = LightSourceType.POINT,
                    color = Color(0xFF00FFFF),
                    intensity = 0.8f,
                    radius = 150f,
                    pulseSpeed = 2.5f
                ),
                LightSource(
                    id = "neon3",
                    type = LightSourceType.AREA,
                    color = Color(0xFFFF0088),
                    intensity = 0.6f,
                    radius = 200f,
                    pulseSpeed = 1.5f
                )
            ),
            globalIntensity = 0.9f,
            rayCount = 50,
            rayLength = 0.5f,
            scattering = 0.7f,
            dustParticles = true,
            dustCount = 100,
            atmosphericFog = 0.25f
        )
    }
    
    fun candlelight(): VolumetricConfig {
        return VolumetricConfig(
            lightSources = listOf(
                LightSource(
                    id = "candle",
                    type = LightSourceType.POINT,
                    color = Color(0xFFFFAA44),
                    intensity = 0.7f,
                    radius = 100f,
                    quality = LightQuality.SOFT,
                    flickerIntensity = 0.3f,
                    pulseSpeed = 4f
                )
            ),
            globalIntensity = 0.6f,
            rayCount = 40,
            rayLength = 0.4f,
            scattering = 0.8f,
            dustParticles = true,
            dustCount = 60,
            atmosphericFog = 0.3f,
            colorBleed = 0.5f
        )
    }
    
    fun etherealGlow(): VolumetricConfig {
        return VolumetricConfig(
            lightSources = listOf(
                LightSource(
                    id = "ethereal",
                    type = LightSourceType.VOLUMETRIC,
                    color = Color(0xFF88AAFF),
                    intensity = 0.5f,
                    radius = 200f,
                    quality = LightQuality.ETHEREAL,
                    pulseSpeed = 0.5f
                )
            ),
            globalIntensity = 0.7f,
            rayCount = 100,
            rayLength = 0.6f,
            scattering = 0.9f,
            dustParticles = true,
            dustCount = 150,
            atmosphericFog = 0.4f,
            colorBleed = 0.6f
        )
    }
    
    fun concertLights(): VolumetricConfig {
        return VolumetricConfig(
            lightSources = listOf(
                LightSource(
                    id = "spot1",
                    type = LightSourceType.SPOT,
                    direction = 60f,
                    color = Color.Red,
                    intensity = 1f,
                    angle = 20f,
                    pulseSpeed = 3f
                ),
                LightSource(
                    id = "spot2",
                    type = LightSourceType.SPOT,
                    direction = 120f,
                    color = Color.Blue,
                    intensity = 1f,
                    angle = 20f,
                    pulseSpeed = 3.5f
                ),
                LightSource(
                    id = "spot3",
                    type = LightSourceType.SPOT,
                    direction = 180f,
                    color = Color.Green,
                    intensity = 1f,
                    angle = 20f,
                    pulseSpeed = 2.8f
                )
            ),
            globalIntensity = 1.2f,
            rayCount = 80,
            rayLength = 0.8f,
            scattering = 0.5f,
            dustParticles = true,
            dustCount = 200,
            atmosphericFog = 0.35f
        )
    }
    
    fun getAllPresets(): List<Pair<String, VolumetricConfig>> {
        return listOf(
            "Golden Hour" to goldenHour(),
            "Dramatic Spotlight" to dramaticSpotlight(),
            "Neon City" to neonCity(),
            "Candlelight" to candlelight(),
            "Ethereal Glow" to etherealGlow(),
            "Concert Lights" to concertLights()
        )
    }
}