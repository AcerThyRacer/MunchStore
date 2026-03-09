package com.sugarmunch.app.physics.quantum

import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Spring System - Advanced spring physics for animations
 * 
 * Features:
 * - Configurable mass, tension, damping
 * - Multiple spring types
 * - Spring chains and networks
 * - Haptic feedback integration
 */
class SpringSystem {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Active springs
    private val _springs = MutableStateFlow<List<Spring>>(emptyList())
    val springs: StateFlow<List<Spring>> = _springs.asStateFlow()

    // Spring state for animations
    private val _springAnimations = MutableStateFlow<Map<String, SpringAnimationState>>(emptyMap())
    val springAnimations: StateFlow<Map<String, SpringAnimationState>> = _springAnimations.asStateFlow()

    // Global spring configuration
    var defaultConfig = SpringConfig(
        mass = 1f,
        tension = 100f,
        damping = 10f,
        restPosition = 0f
    )

    /**
     * Create and add a spring
     */
    fun createSpring(
        id: String,
        config: SpringConfig = defaultConfig,
        type: SpringType = SpringType.LINEAR
    ): Spring {
        val spring = Spring(
            id = id,
            config = config,
            type = type,
            position = config.restPosition,
            velocity = 0f
        )

        _springs.value = _springs.value + spring
        startSpringSimulation(spring)

        return spring
    }

    /**
     * Start spring simulation
     */
    private fun startSpringSimulation(spring: Spring) {
        scope.launch {
            var lastTime = System.currentTimeMillis()

            while (_springs.value.any { it.id == spring.id }) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastTime) / 1000f
                lastTime = currentTime

                updateSpring(spring, deltaTime)

                delay(16) // ~60 FPS
            }
        }
    }

    /**
     * Update spring state
     */
    private fun updateSpring(spring: Spring, deltaTime: Float) {
        val springs = _springs.value.toMutableList()
        val index = springs.indexOfFirst { it.id == spring.id }
        if (index == -1) return

        val currentSpring = springs[index]

        // Calculate acceleration using Hooke's Law: F = -kx
        val displacement = currentSpring.position - currentSpring.config.restPosition
        val springForce = -currentSpring.config.tension * displacement

        // Damping force: F = -bv
        val dampingForce = -currentSpring.config.damping * currentSpring.velocity

        // Total force
        val totalForce = springForce + dampingForce

        // Acceleration: F = ma
        val acceleration = totalForce / currentSpring.config.mass

        // Update velocity and position
        val newVelocity = currentSpring.velocity + acceleration * deltaTime
        val newPosition = currentSpring.position + newVelocity * deltaTime

        // Check if spring is at rest
        val isAtRest = abs(newVelocity) < 0.01f && abs(newPosition - currentSpring.config.restPosition) < 0.01f

        val updatedSpring = currentSpring.copy(
            position = newPosition,
            velocity = newVelocity,
            isAtRest = isAtRest
        )

        springs[index] = updatedSpring
        _springs.value = springs

        // Update animation state
        updateAnimationState(updatedSpring)
    }

    /**
     * Update animation state for Compose
     */
    private fun updateAnimationState(spring: Spring) {
        val animations = _springAnimations.value.toMutableMap()
        animations[spring.id] = SpringAnimationState(
            value = spring.position,
            velocity = spring.velocity,
            isAtRest = spring.isAtRest
        )
        _springAnimations.value = animations
    }

    /**
     * Apply impulse to spring
     */
    fun applyImpulse(springId: String, impulse: Float) {
        val springs = _springs.value.toMutableList()
        val index = springs.indexOfFirst { it.id == springId }
        if (index == -1) return

        springs[index] = springs[index].copy(
            velocity = springs[index].velocity + impulse / springs[index].config.mass
        )
        _springs.value = springs
    }

    /**
     * Set spring rest position
     */
    fun setRestPosition(springId: String, position: Float) {
        val springs = _springs.value.toMutableList()
        val index = springs.indexOfFirst { it.id == springId }
        if (index == -1) return

        springs[index] = springs[index].copy(
            config = springs[index].config.copy(restPosition = position)
        )
        _springs.value = springs
    }

    /**
     * Update spring config
     */
    fun updateConfig(springId: String, config: SpringConfig) {
        val springs = _springs.value.toMutableList()
        val index = springs.indexOfFirst { it.id == springId }
        if (index == -1) return

        springs[index] = springs[index].copy(config = config)
        _springs.value = springs
    }

    /**
     * Remove spring
     */
    fun removeSpring(springId: String) {
        _springs.value = _springs.value.filter { it.id != springId }
        val animations = _springAnimations.value.toMutableMap()
        animations.remove(springId)
        _springAnimations.value = animations
    }

    /**
     * Create spring animation spec for Compose
     */
    fun createAnimationSpec(
        mass: Float = defaultConfig.mass,
        tension: Float = defaultConfig.tension,
        damping: Float = defaultConfig.damping
    ): SpringSpec<Float> {
        return spring(
            stiffness = tension,
            dampingRatio = dampingRatioFromConfig(damping, tension, mass)
        )
    }

    private fun dampingRatioFromConfig(damping: Float, tension: Float, mass: Float): Float {
        val criticalDamping = 2 * sqrt(tension * mass)
        return damping / criticalDamping
    }

    /**
     * Create spring chain (multiple connected springs)
     */
    fun createSpringChain(
        count: Int,
        baseConfig: SpringConfig,
        spacing: Float
    ): List<Spring> {
        val springs = mutableListOf<Spring>()

        for (i in 0 until count) {
            val spring = createSpring(
                id = "chain_${i}_${System.currentTimeMillis()}",
                config = baseConfig.copy(
                    restPosition = i * spacing,
                    tension = baseConfig.tension * (1 - i.toFloat() / count)
                )
            )
            springs.add(spring)
        }

        return springs
    }

    /**
     * Create spring network (2D grid of springs)
     */
    fun createSpringNetwork(
        rows: Int,
        cols: Int,
        baseConfig: SpringConfig,
        spacing: Float
    ): List<Spring> {
        val springs = mutableListOf<Spring>()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val spring = createSpring(
                    id = "network_${row}_${col}_${System.currentTimeMillis()}",
                    config = baseConfig.copy(
                        restPosition = row * spacing
                    )
                )
                springs.add(spring)
            }
        }

        return springs
    }

    companion object {
        @Volatile
        private var instance: SpringSystem? = null

        fun getInstance(): SpringSystem {
            return instance ?: synchronized(this) {
                instance ?: SpringSystem().also { instance = it }
            }
        }
    }
}

/**
 * Spring configuration
 */
data class SpringConfig(
    val mass: Float = 1f,
    val tension: Float = 100f,
    val damping: Float = 10f,
    val restPosition: Float = 0f
)

/**
 * Spring types
 */
enum class SpringType {
    LINEAR,      // Standard linear spring
    ANGULAR,     // Rotational spring
    TORSIONAL,   // Twisting spring
    VOLUMETRIC   // Volume-based spring
}

/**
 * Spring in simulation
 */
data class Spring(
    val id: String,
    val config: SpringConfig,
    val type: SpringType,
    val position: Float,
    val velocity: Float,
    val isAtRest: Boolean = false
)

/**
 * Spring animation state for Compose
 */
data class SpringAnimationState(
    val value: Float,
    val velocity: Float,
    val isAtRest: Boolean
)

/**
 * Preset spring configurations for common animations
 */
object SpringPresets {

    val BOUNCY = SpringConfig(
        mass = 1f,
        tension = 200f,
        damping = 5f
    )

    val GENTLE = SpringConfig(
        mass = 1f,
        tension = 50f,
        damping = 15f
    )

    val SNAPPY = SpringConfig(
        mass = 0.5f,
        tension = 300f,
        damping = 20f
    )

    val SLOW = SpringConfig(
        mass = 2f,
        tension = 30f,
        damping = 25f
    )

    val WOBBLY = SpringConfig(
        mass = 1.5f,
        tension = 80f,
        damping = 3f
    )

    val STIFF = SpringConfig(
        mass = 0.3f,
        tension = 500f,
        damping = 30f
    )

    val JELLY = SpringConfig(
        mass = 2f,
        tension = 40f,
        damping = 2f
    )

    val INSTANT = SpringConfig(
        mass = 0.1f,
        tension = 1000f,
        damping = 100f
    )
}

/**
 * Spring-based animation builder
 */
class SpringAnimationBuilder {

    private var mass = 1f
    private var tension = 100f
    private var damping = 10f
    private var restPosition = 0f

    fun mass(mass: Float) = apply { this.mass = mass }
    fun tension(tension: Float) = apply { this.tension = tension }
    fun damping(damping: Float) = apply { this.damping = damping }
    fun restPosition(restPosition: Float) = apply { this.restPosition = restPosition }

    fun build(): SpringConfig {
        return SpringConfig(mass, tension, damping, restPosition)
    }

    fun buildAndCreate(springSystem: SpringSystem, id: String): Spring {
        return springSystem.createSpring(id, build())
    }
}

/**
 * Create spring animation builder
 */
fun springAnimation(block: SpringAnimationBuilder.() -> Unit): SpringConfig {
    return SpringAnimationBuilder().apply(block).build()
}
