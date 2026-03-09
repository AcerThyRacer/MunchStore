package com.sugarmunch.app.effects.x2026

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*

/**
 * Particle-based fire and smoke simulation
 * Simulates combustion, temperature, buoyancy, and volumetric smoke
 */
class FireSmokeSimulation(
    val maxParticles: Int = 500,
    val emissionRate: Int = 20,
    gravity: Float = -9.8f
) {
    // Particle arrays
    private val particles = mutableListOf<FireParticle>()
    
    // Physics parameters
    var gravity = gravity
    var buoyancy = 15f
    var turbulence = 0.5f
    var wind = Offset.Zero
    var ambientTemperature = 20f // Celsius
    
    // Fire properties
    var fireTemperature = 1500f // Celsius
    var fuelConsumption = 0.1f
    var smokeProduction = 0.3f
    
    // Emitter
    private var emitterPosition = Offset(0.5f, 0.9f) // Normalized coordinates
    private var emitterSize = 50f
    private var isEmitting = true
    
    /**
     * Step simulation forward
     */
    fun step(dt: Float = 0.016f) {
        // Emit new particles
        if (isEmitting) {
            emitParticles(emissionRate)
        }
        
        // Update particles
        updateParticles(dt)
        
        // Remove dead particles
        particles.removeAll { !it.alive }
        
        // Limit particle count
        while (particles.size > maxParticles) {
            particles.removeAt(0)
        }
    }
    
    /**
     * Emit new fire/smoke particles
     */
    private fun emitParticles(count: Int) {
        repeat(count) {
            val baseX = emitterPosition.x * 1000f // Assuming 1000px width
            val baseY = emitterPosition.y * 1000f
            
            val offsetX = (Random.nextFloat() - 0.5f) * emitterSize
            val offsetY = (Random.nextFloat() - 0.5f) * emitterSize * 0.3f
            
            val particle = FireParticle(
                x = baseX + offsetX,
                y = baseY + offsetY,
                vx = (Random.nextFloat() - 0.5f) * 20f,
                vy = -Random.nextFloat() * 50f - 20f,
                temperature = fireTemperature + Random.nextFloat() * 500f,
                fuel = 1f,
                size = Random.nextFloat() * 20f + 10f,
                particleType = if (Random.nextFloat() < smokeProduction) ParticleType.SMOKE else ParticleType.FIRE
            )
            
            particles.add(particle)
        }
    }
    
    /**
     * Update all particles
     */
    private fun updateParticles(dt: Float) {
        particles.forEach { particle ->
            if (!particle.alive) return@forEach
            
            // Temperature decreases over time
            particle.temperature -= (particle.temperature - ambientTemperature) * 0.1f * dt
            
            // Fuel consumption
            particle.fuel -= fuelConsumption * dt
            
            // Check if particle is dead
            if (particle.fuel <= 0f || particle.temperature < 100f) {
                particle.alive = false
                return@forEach
            }
            
            // Buoyancy (hot air rises)
            val buoyancyForce = buoyancy * (particle.temperature / fireTemperature)
            
            // Apply forces
            particle.vy += (gravity + buoyancyForce) * dt
            
            // Wind
            particle.vx += wind.x * dt
            particle.vy += wind.y * dt
            
            // Turbulence
            particle.vx += (Random.nextFloat() - 0.5f) * turbulence * 10f * dt
            particle.vy += (Random.nextFloat() - 0.5f) * turbulence * 10f * dt
            
            // Update position
            particle.x += particle.vx * dt
            particle.y += particle.vy * dt
            
            // Size changes with temperature
            particle.size = particle.size * 0.99f + 5f * (particle.temperature / fireTemperature)
            
            // Color transition based on temperature and fuel
            updateParticleColor(particle)
        }
    }
    
    /**
     * Update particle color based on state
     */
    private fun updateParticleColor(particle: FireParticle) {
        when (particle.particleType) {
            ParticleType.FIRE -> {
                // Fire color gradient: white -> yellow -> orange -> red
                particle.color = when {
                    particle.temperature > 1200f -> Color(0xFFFFFFFF) // White hot
                    particle.temperature > 900f -> Color(0xFFFFD700)  // Yellow
                    particle.temperature > 600f -> Color(0xFFFFA500)  // Orange
                    else -> Color(0xFFFF4500)                         // Red
                }
            }
            ParticleType.SMOKE -> {
                // Smoke: dark gray -> light gray -> transparent
                val alpha = (particle.fuel * 0.5f).coerceIn(0f, 1f)
                val gray = (200f * particle.fuel).coerceIn(50f, 200f)
                particle.color = Color(gray.toInt(), gray.toInt(), gray.toInt(), (alpha * 255).toInt())
            }
            ParticleType.EMBER -> {
                // Ember: bright orange -> dim red
                val brightness = particle.fuel
                particle.color = Color(
                    red = 1f,
                    green = brightness * 0.5f,
                    blue = 0f,
                    alpha = (brightness * 255).toInt()
                )
            }
        }
    }
    
    /**
     * Set emitter position
     */
    fun setEmitterPosition(x: Float, y: Float) {
        emitterPosition = Offset(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
    }
    
    /**
     * Set emitter size
     */
    fun setEmitterSize(size: Float) {
        emitterSize = size.coerceIn(10f, 200f)
    }
    
    /**
     * Toggle emission
     */
    fun setEmitting(emitting: Boolean) {
        isEmitting = emitting
    }
    
    /**
     * Get all active particles
     */
    fun getParticles(): List<FireParticle> {
        return particles.filter { it.alive }
    }
    
    /**
     * Clear all particles
     */
    fun clear() {
        particles.clear()
    }
    
    /**
     * Set fire preset
     */
    fun setPreset(preset: FireSmokePreset) {
        fireTemperature = preset.temperature
        smokeProduction = preset.smokeAmount
        fuelConsumption = preset.consumptionRate
        buoyancy = preset.buoyancy
        emissionRate = preset.emissionRate
    }
}

/**
 * Fire/Smoke particle
 */
data class FireParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var temperature: Float,
    var fuel: Float,
    var size: Float,
    var particleType: ParticleType = ParticleType.FIRE,
    var color: Color = Color(0xFFFFA500),
    var alive: Boolean = true
)

/**
 * Particle types
 */
enum class ParticleType {
    FIRE,
    SMOKE,
    EMBER
}

/**
 * Fire and smoke presets
 */
object FireSmokePresets {
    
    fun candlePreset(): FireSmokePreset {
        return FireSmokePreset(
            name = "Candle",
            temperature = 1000f,
            smokeAmount = 0.1f,
            consumptionRate = 0.05f,
            buoyancy = 10f,
            emissionRate = 5,
            color = Color(0xFFFFA500)
        )
    }
    
    fun campfirePreset(): FireSmokePreset {
        return FireSmokePreset(
            name = "Campfire",
            temperature = 1200f,
            smokeAmount = 0.3f,
            consumptionRate = 0.1f,
            buoyancy = 15f,
            emissionRate = 30,
            color = Color(0xFFFF4500)
        )
    }
    
    fun bonfirePreset(): FireSmokePreset {
        return FireSmokePreset(
            name = "Bonfire",
            temperature = 1400f,
            smokeAmount = 0.4f,
            consumptionRate = 0.15f,
            buoyancy = 20f,
            emissionRate = 50,
            color = Color(0xFFFF6347)
        )
    }
    
    fun neonFirePreset(): FireSmokePreset {
        return FireSmokePreset(
            name = "Neon Fire",
            temperature = 2000f,
            smokeAmount = 0.0f,
            consumptionRate = 0.08f,
            buoyancy = 25f,
            emissionRate = 40,
            color = Color(0xFF00FFFF)
        )
    }
    
    fun rainbowSmokePreset(): FireSmokePreset {
        return FireSmokePreset(
            name = "Rainbow Smoke",
            temperature = 800f,
            smokeAmount = 0.8f,
            consumptionRate = 0.03f,
            buoyancy = 12f,
            emissionRate = 20,
            color = Color(0xFFFF00FF)
        )
    }
    
    fun dragonFirePreset(): FireSmokePreset {
        return FireSmokePreset(
            name = "Dragon Fire",
            temperature = 2500f,
            smokeAmount = 0.2f,
            consumptionRate = 0.2f,
            buoyancy = 30f,
            emissionRate = 60,
            color = Color(0xFF00FF00)
        )
    }
}

data class FireSmokePreset(
    val name: String,
    val temperature: Float,
    val smokeAmount: Float,
    val consumptionRate: Float,
    val buoyancy: Float,
    val emissionRate: Int,
    val color: Color
)

/**
 * Simple random number generator
 */
private object Random {
    private var seed = System.nanoTime()
    
    fun nextFloat(): Float {
        seed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        return (seed shr 12).toFloat() / 0x100000000L.toFloat()
    }
}
