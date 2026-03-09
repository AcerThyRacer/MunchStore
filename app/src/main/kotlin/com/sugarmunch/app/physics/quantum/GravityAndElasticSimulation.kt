package com.sugarmunch.app.physics.quantum

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Gravity Simulation - Gravity-based object movements
 */
class GravitySimulation {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _objects = MutableStateFlow<List<GravityObject>>(emptyList())
    val objects: StateFlow<List<GravityObject>> = _objects.asStateFlow()

    var gravityConfig = GravityConfig(
        gravityStrength = 9.8f,
        minDistance = 1f,
        softening = 10f
    )

    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        startSimulationLoop()
    }

    fun stop() {
        isRunning = false
    }

    private fun startSimulationLoop() {
        scope.launch {
            while (isRunning) {
                updateGravity(1f / 60f)
                delay(16)
            }
        }
    }

    private fun updateGravity(deltaTime: Float) {
        val objects = _objects.value.toMutableList()

        for (i in objects.indices) {
            var totalForce = Offset.Zero

            for (j in objects.indices) {
                if (i == j) continue

                val objI = objects[i]
                val objJ = objects[j]

                val direction = objJ.position - objI.position
                val distance = direction.getDistance().coerceAtLeast(gravityConfig.minDistance)

                // Newton's law of gravitation: F = G * m1 * m2 / r^2
                val forceMagnitude = gravityConfig.gravityStrength * objI.mass * objJ.mass /
                    (distance * distance + gravityConfig.softening)

                val force = direction / distance * forceMagnitude
                totalForce += force
            }

            val acceleration = totalForce / objI.mass
            objects[i] = objI.copy(
                velocity = objI.velocity + acceleration * deltaTime,
                position = objI.position + objI.velocity * deltaTime
            )
        }

        _objects.value = objects
    }

    fun addObject(obj: GravityObject) {
        _objects.value = _objects.value + obj
    }

    fun removeObject(id: String) {
        _objects.value = _objects.value.filter { it.id != id }
    }

    fun clear() {
        _objects.value = emptyList()
    }
}

data class GravityConfig(
    val gravityStrength: Float = 9.8f,
    val minDistance: Float = 1f,
    val softening: Float = 10f
)

data class GravityObject(
    val id: String,
    val position: Offset,
    val velocity: Offset,
    val mass: Float,
    val radius: Float,
    val color: androidx.compose.ui.graphics.Color
)

/**
 * Elastic Boundaries - Bouncy edge effects
 */
class ElasticBoundaries {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _boundaryObjects = MutableStateFlow<List<ElasticObject>>(emptyList())
    val boundaryObjects: StateFlow<List<ElasticObject>> = _boundaryObjects.asStateFlow()

    var elasticConfig = ElasticConfig(
        stiffness = 100f,
        damping = 5f,
        boundaryRect = Rect(0f, 0f, 1080f, 2400f),
        bounceFactor = 0.7f
    )

    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        startSimulationLoop()
    }

    fun stop() {
        isRunning = false
    }

    private fun startSimulationLoop() {
        scope.launch {
            while (isRunning) {
                updateElastic(1f / 60f)
                delay(16)
            }
        }
    }

    private fun updateElastic(deltaTime: Float) {
        val objects = _boundaryObjects.value.toMutableList()
        val bounds = elasticConfig.boundaryRect

        for (i in objects.indices) {
            var obj = objects[i]
            var newPosition = obj.position
            var newVelocity = obj.velocity

            // Left boundary
            if (obj.position.x - obj.radius < bounds.left) {
                newPosition = Offset(bounds.left + obj.radius, obj.position.y)
                newVelocity = Offset(-obj.velocity.x * elasticConfig.bounceFactor, obj.velocity.y)
            }

            // Right boundary
            if (obj.position.x + obj.radius > bounds.right) {
                newPosition = Offset(bounds.right - obj.radius, obj.position.y)
                newVelocity = Offset(-obj.velocity.x * elasticConfig.bounceFactor, obj.velocity.y)
            }

            // Top boundary
            if (obj.position.y - obj.radius < bounds.top) {
                newPosition = Offset(obj.position.x, bounds.top + obj.radius)
                newVelocity = Offset(obj.velocity.x, -obj.velocity.y * elasticConfig.bounceFactor)
            }

            // Bottom boundary
            if (obj.position.y + obj.radius > bounds.bottom) {
                newPosition = Offset(obj.position.x, bounds.bottom - obj.radius)
                newVelocity = Offset(obj.velocity.x, -obj.velocity.y * elasticConfig.bounceFactor)
            }

            // Apply spring force towards center
            val center = Offset(bounds.width / 2, bounds.height / 2)
            val displacement = obj.position - center
            val springForce = -displacement * elasticConfig.stiffness / obj.mass
            newVelocity += springForce * deltaTime

            // Apply damping
            newVelocity *= (1 - elasticConfig.damping * deltaTime)

            objects[i] = obj.copy(
                position = newPosition + newVelocity * deltaTime,
                velocity = newVelocity
            )
        }

        _boundaryObjects.value = objects
    }

    fun addObject(obj: ElasticObject) {
        _boundaryObjects.value = _boundaryObjects.value + obj
    }

    fun applyImpulse(position: Offset, force: Offset, radius: Float = 100f) {
        val objects = _boundaryObjects.value.toMutableList()

        for (i in objects.indices) {
            val obj = objects[i]
            val distance = (obj.position - position).getDistance()

            if (distance < radius) {
                val influence = 1f - (distance / radius)
                objects[i] = obj.copy(
                    velocity = obj.velocity + force * influence
                )
            }
        }

        _boundaryObjects.value = objects
    }

    fun clear() {
        _boundaryObjects.value = emptyList()
    }
}

data class ElasticConfig(
    val stiffness: Float = 100f,
    val damping: Float = 5f,
    val boundaryRect: Rect = Rect(0f, 0f, 1080f, 2400f),
    val bounceFactor: Float = 0.7f
)

data class ElasticObject(
    val id: String,
    val position: Offset,
    val velocity: Offset,
    val radius: Float,
    val mass: Float,
    val color: androidx.compose.ui.graphics.Color
)
