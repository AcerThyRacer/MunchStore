package com.sugarmunch.app.physics.quantum

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Fluid Simulation - Liquid-like motion effects
 * 
 * Features:
 * - Navier-Stokes based fluid dynamics
 * - Particle-based fluid (SPH)
 * - Viscosity control
 * - Surface tension
 */
class FluidSimulation {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Fluid particles
    private val _particles = MutableStateFlow<List<FluidParticle>>(emptyList())
    val particles: StateFlow<List<FluidParticle>> = _particles.asStateFlow()

    // Fluid grid for pressure calculation
    private var gridSize = 100
    private val pressureGrid = Array(gridSize) { FloatArray(gridSize) }
    private val velocityGrid = Array(gridSize) { Array(gridSize) { Offset.Zero } }

    // Fluid configuration
    var fluidConfig = FluidConfig(
        particleCount = 500,
        viscosity = 0.01f,
        surfaceTension = 0.5f,
        gravity = Offset(0f, 500f),
        particleRadius = 5f,
        restDensity = 1000f,
        gasConstant = 2000f,
        viscosityMultiplier = 0.001f
    )

    // Simulation bounds
    var bounds = FluidBounds(0f, 0f, 1080f, 2400f)

    private var isRunning = false

    /**
     * Start fluid simulation
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        initializeParticles()
        startSimulationLoop()
    }

    /**
     * Stop fluid simulation
     */
    fun stop() {
        isRunning = false
    }

    /**
     * Initialize fluid particles
     */
    private fun initializeParticles() {
        val particles = mutableListOf<FluidParticle>()

        for (i in 0 until fluidConfig.particleCount) {
            val x = bounds.left + (bounds.width * (i % 20) / 20f)
            val y = bounds.top + (bounds.height * (i / 20) / 25f)

            particles.add(
                FluidParticle(
                    id = i,
                    position = Offset(x, y),
                    velocity = Offset.Zero,
                    acceleration = Offset.Zero,
                    density = fluidConfig.restDensity,
                    pressure = 0f,
                    color = Color.Blue.copy(alpha = 0.6f)
                )
            )
        }

        _particles.value = particles
    }

    /**
     * Main simulation loop
     */
    private fun startSimulationLoop() {
        scope.launch {
            val timeStep = 1f / 60f

            while (isRunning) {
                updateFluid(timeStep)
                delay(16)
            }
        }
    }

    /**
     * Update fluid simulation
     */
    private fun updateFluid(deltaTime: Float) {
        val particles = _particles.value.toMutableList()

        // Step 1: Calculate density and pressure
        calculateDensityAndPressure(particles)

        // Step 2: Calculate forces
        calculateForces(particles)

        // Step 3: Integrate motion
        integrateMotion(particles, deltaTime)

        // Step 4: Apply constraints
        applyConstraints(particles)

        _particles.value = particles
    }

    /**
     * Calculate density and pressure for each particle
     */
    private fun calculateDensityAndPressure(particles: MutableList<FluidParticle>) {
        for (i in particles.indices) {
            var density = 0f

            for (j in particles.indices) {
                if (i == j) continue

                val particleI = particles[i]
                val particleJ = particles[j]

                val distance = (particleI.position - particleJ.position).getDistance()

                if (distance < fluidConfig.particleRadius * 2) {
                    density += calculateKernel(distance, fluidConfig.particleRadius * 2)
                }
            }

            particles[i] = particles[i].copy(
                density = density * fluidConfig.restDensity,
                pressure = fluidConfig.gasConstant * (density - fluidConfig.restDensity)
            )
        }
    }

    /**
     * Calculate forces on each particle
     */
    private fun calculateForces(particles: MutableList<FluidParticle>) {
        for (i in particles.indices) {
            var pressureForce = Offset.Zero
            var viscosityForce = Offset.Zero
            var gravityForce = fluidConfig.gravity

            for (j in particles.indices) {
                if (i == j) continue

                val particleI = particles[i]
                val particleJ = particles[j]

                val distance = (particleI.position - particleJ.position).getDistance()
                val direction = (particleJ.position - particleI.position).let {
                    if (it.getDistance() > 0) it / it.getDistance() else Offset.Zero
                }

                if (distance < fluidConfig.particleRadius * 2) {
                    // Pressure force
                    val pressureGradient = calculatePressureGradient(
                        particleI.pressure,
                        particleJ.pressure,
                        distance,
                        fluidConfig.particleRadius * 2
                    )
                    pressureForce += direction * pressureGradient

                    // Viscosity force
                    val viscosityLaplacian = calculateViscosityLaplacian(
                        particleI.velocity,
                        particleJ.velocity,
                        distance,
                        fluidConfig.particleRadius * 2
                    )
                    viscosityForce += viscosityLaplacian * fluidConfig.viscosityMultiplier
                }
            }

            val totalForce = pressureForce + viscosityForce + gravityForce

            particles[i] = particles[i].copy(
                acceleration = totalForce / particles[i].density
            )
        }
    }

    /**
     * Integrate particle motion
     */
    private fun integrateMotion(particles: MutableList<FluidParticle>, deltaTime: Float) {
        for (i in particles.indices) {
            val particle = particles[i]

            val newVelocity = particle.velocity + particle.acceleration * deltaTime
            val newPosition = particle.position + newVelocity * deltaTime

            // Apply surface tension
            val surfaceTensionForce = calculateSurfaceTension(particle, particles)
            val tensionAdjustedVelocity = newVelocity + surfaceTensionForce * deltaTime

            particles[i] = particle.copy(
                velocity = tensionAdjustedVelocity,
                position = newPosition
            )
        }
    }

    /**
     * Apply boundary constraints
     */
    private fun applyConstraints(particles: MutableList<FluidParticle>) {
        val damping = 0.5f

        for (i in particles.indices) {
            val particle = particles[i]
            var newPosition = particle.position
            var newVelocity = particle.velocity

            // Left boundary
            if (newPosition.x < bounds.left + fluidConfig.particleRadius) {
                newPosition = Offset(bounds.left + fluidConfig.particleRadius, newPosition.y)
                newVelocity = Offset(-newVelocity.x * damping, newVelocity.y)
            }

            // Right boundary
            if (newPosition.x > bounds.right - fluidConfig.particleRadius) {
                newPosition = Offset(bounds.right - fluidConfig.particleRadius, newPosition.y)
                newVelocity = Offset(-newVelocity.x * damping, newVelocity.y)
            }

            // Top boundary
            if (newPosition.y < bounds.top + fluidConfig.particleRadius) {
                newPosition = Offset(newPosition.x, bounds.top + fluidConfig.particleRadius)
                newVelocity = Offset(newVelocity.x, -newVelocity.y * damping)
            }

            // Bottom boundary
            if (newPosition.y > bounds.bottom - fluidConfig.particleRadius) {
                newPosition = Offset(newPosition.x, bounds.bottom - fluidConfig.particleRadius)
                newVelocity = Offset(newVelocity.x, -newVelocity.y * damping)
            }

            particles[i] = particle.copy(
                position = newPosition,
                velocity = newVelocity
            )
        }
    }

    // ========== KERNEL FUNCTIONS ==========

    private fun calculateKernel(distance: Float, radius: Float): Float {
        val h = radius
        val r = distance

        return when {
            r < 0 -> 0f
            r < h -> (315f / (64f * PI.toFloat() * pow(h, 9))) * pow(h * h - r * r, 3)
            else -> 0f
        }
    }

    private fun calculatePressureGradient(pressureI: Float, pressureJ: Float, distance: Float, radius: Float): Float {
        val h = radius
        val r = distance

        return when {
            r < 0 -> 0f
            r < h -> -(45f / (PI.toFloat() * pow(h, 6))) * pow(h - r, 2) * (pressureI + pressureJ) / 2
            else -> 0f
        }
    }

    private fun calculateViscosityLaplacian(velocityI: Offset, velocityJ: Offset, distance: Float, radius: Float): Float {
        val h = radius
        val r = distance

        return when {
            r < 0 -> 0f
            r < h -> (45f / (PI.toFloat() * pow(h, 6))) * (h - r)
            else -> 0f
        }
    }

    private fun calculateSurfaceTension(particle: FluidParticle, allParticles: List<FluidParticle>): Offset {
        // Simplified surface tension based on particle density
        val normal = calculateNormal(particle, allParticles)
        val curvature = calculateCurvature(particle, allParticles)

        return normal * fluidConfig.surfaceTension * curvature
    }

    private fun calculateNormal(particle: FluidParticle, allParticles: List<FluidParticle>): Offset {
        var normal = Offset.Zero

        for (other in allParticles) {
            if (other.id == particle.id) continue

            val direction = particle.position - other.position
            val distance = direction.getDistance()

            if (distance < fluidConfig.particleRadius * 2) {
                normal += direction * calculateKernel(distance, fluidConfig.particleRadius * 2)
            }
        }

        return if (normal.getDistance() > 0) normal / normal.getDistance() else Offset.Zero
    }

    private fun calculateCurvature(particle: FluidParticle, allParticles: List<FluidParticle>): Float {
        return 1f - (particle.density / fluidConfig.restDensity)
    }

    // ========== INTERACTION ==========

    /**
     * Add force at position
     */
    fun addForce(position: Offset, force: Offset, radius: Float = 50f) {
        val particles = _particles.value.toMutableList()

        for (i in particles.indices) {
            val particle = particles[i]
            val distance = (particle.position - position).getDistance()

            if (distance < radius) {
                val influence = 1f - (distance / radius)
                particles[i] = particle.copy(
                    velocity = particle.velocity + force * influence
                )
            }
        }

        _particles.value = particles
    }

    /**
     * Add particles at position
     */
    fun addParticles(position: Offset, count: Int = 10) {
        val particles = _particles.value.toMutableList()
        val startId = particles.maxOfOrNull { it.id } ?: 0

        for (i in 0 until count) {
            particles.add(
                FluidParticle(
                    id = startId + i + 1,
                    position = position + Offset((Math.random().toFloat() - 0.5f) * 20, (Math.random().toFloat() - 0.5f) * 20),
                    velocity = Offset.Zero,
                    acceleration = Offset.Zero,
                    density = fluidConfig.restDensity,
                    pressure = 0f,
                    color = Color.Blue.copy(alpha = 0.6f)
                )
            )
        }

        _particles.value = particles
    }

    /**
     * Clear all particles
     */
    fun clear() {
        _particles.value = emptyList()
    }

    /**
     * Reset simulation
     */
    fun reset() {
        stop()
        _particles.value = emptyList()
        start()
    }
}

/**
 * Fluid configuration
 */
data class FluidConfig(
    val particleCount: Int = 500,
    val viscosity: Float = 0.01f,
    val surfaceTension: Float = 0.5f,
    val gravity: Offset = Offset(0f, 500f),
    val particleRadius: Float = 5f,
    val restDensity: Float = 1000f,
    val gasConstant: Float = 2000f,
    val viscosityMultiplier: Float = 0.001f
)

/**
 * Fluid bounds
 */
data class FluidBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

/**
 * Fluid particle
 */
data class FluidParticle(
    val id: Int,
    val position: Offset,
    val velocity: Offset,
    val acceleration: Offset,
    val density: Float,
    val pressure: Float,
    val color: Color
)

/**
 * Fluid interaction types
 */
enum class FluidInteractionType {
    POKE,          // Single point force
    STIR,          // Rotational force
    ATTRACT,       // Attraction force
    REPEL,         // Repulsion force
    WAVE           // Wave pattern
}
