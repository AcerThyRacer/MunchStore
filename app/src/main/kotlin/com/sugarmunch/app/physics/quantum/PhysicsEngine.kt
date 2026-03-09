package com.sugarmunch.app.physics.quantum

import android.graphics.RectF
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Quantum Physics Engine - Advanced physics simulation for UI animations
 * 
 * Features:
 * - Spring physics with adjustable parameters
 * - Fluid dynamics simulation
 * - Gravity and collision detection
 * - Elastic boundaries
 * - Particle systems
 * - Motion trails
 */
class PhysicsEngine {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Physics state
    private val _physicsObjects = MutableStateFlow<List<PhysicsObject>>(emptyList())
    val physicsObjects: StateFlow<List<PhysicsObject>> = _physicsObjects.asStateFlow()

    private val _activeForces = MutableStateFlow<List<Force>>(emptyList())
    val activeForces: StateFlow<List<Force>> = _activeForces.asStateFlow()

    // Physics configuration
    var physicsConfig = PhysicsConfig(
        gravity = 9.8f,
        friction = 0.98f,
        elasticity = 0.7f,
        timeScale = 1f,
        maxVelocity = 5000f,
        subSteps = 8
    )

    // Simulation state
    private var isRunning = false
    private var lastFrameTime = 0L

    /**
     * Start physics simulation
     */
    fun startSimulation() {
        if (isRunning) return
        isRunning = true
        lastFrameTime = System.currentTimeMillis()
        startPhysicsLoop()
    }

    /**
     * Stop physics simulation
     */
    fun stopSimulation() {
        isRunning = false
    }

    /**
     * Main physics loop
     */
    private fun startPhysicsLoop() {
        scope.launch {
            while (isRunning) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastFrameTime) / 1000f * physicsConfig.timeScale
                lastFrameTime = currentTime

                updatePhysics(deltaTime)

                // Cap at 120 FPS
                delay(8)
            }
        }
    }

    /**
     * Update physics simulation
     */
    private fun updatePhysics(deltaTime: Float) {
        val subStepDelta = deltaTime / physicsConfig.subSteps

        repeat(physicsConfig.subSteps) {
            // Apply forces
            applyForces(subStepDelta)

            // Update positions
            updatePositions(subStepDelta)

            // Check collisions
            checkCollisions()

            // Apply constraints
            applyConstraints()
        }

        // Emit updated state
        _physicsObjects.value = _physicsObjects.value.map { it.copy() }
    }

    /**
     * Apply all active forces to objects
     */
    private fun applyForces(deltaTime: Float) {
        val objects = _physicsObjects.value.toMutableList()

        objects.forEach { obj ->
            // Apply gravity
            if (obj.affectsByGravity) {
                obj.velocity = Offset(
                    obj.velocity.x,
                    obj.velocity.y + physicsConfig.gravity * obj.mass * deltaTime
                )
            }

            // Apply active forces
            _activeForces.value.forEach { force ->
                if (force.affectsObject(obj)) {
                    val forceAcceleration = Offset(
                        force.direction.x * force.magnitude / obj.mass,
                        force.direction.y * force.magnitude / obj.mass
                    )
                    obj.velocity = Offset(
                        obj.velocity.x + forceAcceleration.x * deltaTime,
                        obj.velocity.y + forceAcceleration.y * deltaTime
                    )
                }
            }

            // Apply friction
            obj.velocity = Offset(
                obj.velocity.x * physicsConfig.friction,
                obj.velocity.y * physicsConfig.friction
            )

            // Clamp velocity
            val velocityMagnitude = sqrt(obj.velocity.x.pow(2) + obj.velocity.y.pow(2))
            if (velocityMagnitude > physicsConfig.maxVelocity) {
                val scale = physicsConfig.maxVelocity / velocityMagnitude
                obj.velocity = Offset(obj.velocity.x * scale, obj.velocity.y * scale)
            }
        }

        _physicsObjects.value = objects
    }

    /**
     * Update object positions
     */
    private fun updatePositions(deltaTime: Float) {
        val objects = _physicsObjects.value.toMutableList()

        objects.forEach { obj ->
            obj.position = Offset(
                obj.position.x + obj.velocity.x * deltaTime,
                obj.position.y + obj.velocity.y * deltaTime
            )

            // Update rotation based on angular velocity
            obj.rotation += obj.angularVelocity * deltaTime
        }

        _physicsObjects.value = objects
    }

    /**
     * Check and resolve collisions
     */
    private fun checkCollisions() {
        val objects = _physicsObjects.value.toMutableList()

        for (i in objects.indices) {
            for (j in (i + 1) until objects.size) {
                val obj1 = objects[i]
                val obj2 = objects[j]

                if (checkCollision(obj1, obj2)) {
                    resolveCollision(obj1, obj2)
                }
            }
        }

        _physicsObjects.value = objects
    }

    /**
     * Check if two objects collide
     */
    private fun checkCollision(obj1: PhysicsObject, obj2: PhysicsObject): Boolean {
        return when {
            obj1.shape is Shape.Circle && obj2.shape is Shape.Circle -> {
                val dx = obj2.position.x - obj1.position.x
                val dy = obj2.position.y - obj1.position.y
                val distance = sqrt(dx * dx + dy * dy)
                val radius1 = (obj1.shape as Shape.Circle).radius
                val radius2 = (obj2.shape as Shape.Circle).radius
                distance < (radius1 + radius2)
            }
            obj1.shape is Shape.Rectangle && obj2.shape is Shape.Rectangle -> {
                val rect1 = obj1.bounds
                val rect2 = obj2.bounds
                RectF.intersects(rect1, rect2)
            }
            else -> false
        }
    }

    /**
     * Resolve collision between two objects
     */
    private fun resolveCollision(obj1: PhysicsObject, obj2: PhysicsObject) {
        val elasticity = physicsConfig.elasticity

        // Calculate collision normal
        val normal = Offset(
            obj2.position.x - obj1.position.x,
            obj2.position.y - obj1.position.y
        ).let {
            val mag = sqrt(it.x * it.x + it.y * it.y)
            if (mag > 0) Offset(it.x / mag, it.y / mag) else Offset(0f, 0f)
        }

        // Relative velocity
        val relativeVelocity = Offset(
            obj2.velocity.x - obj1.velocity.x,
            obj2.velocity.y - obj1.velocity.y
        )

        // Relative velocity along normal
        val velocityAlongNormal = relativeVelocity.x * normal.x + relativeVelocity.y * normal.y

        // Do not resolve if velocities are separating
        if (velocityAlongNormal > 0) return

        // Calculate impulse scalar
        val restitution = elasticity
        val impulseScalar = -(1 + restitution) * velocityAlongNormal / (1 / obj1.mass + 1 / obj2.mass)

        // Apply impulse
        val impulse = Offset(normal.x * impulseScalar, normal.y * impulseScalar)

        obj1.velocity = Offset(
            obj1.velocity.x - impulse.x / obj1.mass,
            obj1.velocity.y - impulse.y / obj1.mass
        )
        obj2.velocity = Offset(
            obj2.velocity.x + impulse.x / obj2.mass,
            obj2.velocity.y + impulse.y / obj2.mass
        )
    }

    /**
     * Apply boundary constraints
     */
    private fun applyConstraints() {
        val objects = _physicsObjects.value.toMutableList()

        objects.forEach { obj ->
            obj.constraints.forEach { constraint ->
                constraint.apply(obj)
            }
        }

        _physicsObjects.value = objects
    }

    // ========== OBJECT MANAGEMENT ==========

    /**
     * Add physics object
     */
    fun addObject(obj: PhysicsObject) {
        _physicsObjects.value = _physicsObjects.value + obj
    }

    /**
     * Remove physics object
     */
    fun removeObject(id: String) {
        _physicsObjects.value = _physicsObjects.value.filter { it.id != id }
    }

    /**
     * Clear all objects
     */
    fun clearObjects() {
        _physicsObjects.value = emptyList()
    }

    /**
     * Add force
     */
    fun addForce(force: Force) {
        _activeForces.value = _activeForces.value + force

        // Auto-remove temporary forces
        if (force.duration > 0) {
            scope.launch {
                delay(force.duration.toLong())
                removeForce(force.id)
            }
        }
    }

    /**
     * Remove force
     */
    fun removeForce(id: String) {
        _activeForces.value = _activeForces.value.filter { it.id != id }
    }

    /**
     * Clear all forces
     */
    fun clearForces() {
        _activeForces.value = emptyList()
    }

    // ========== PRESET FORCES ==========

    /**
     * Apply explosion force from point
     */
    fun applyExplosion(position: Offset, magnitude: Float, radius: Float) {
        val force = Force(
            id = "explosion_${System.currentTimeMillis()}",
            type = ForceType.EXPLOSION,
            position = position,
            direction = Offset.Zero,
            magnitude = magnitude,
            radius = radius,
            duration = 100
        )
        addForce(force)
    }

    /**
     * Apply wind force
     */
    fun applyWind(direction: Offset, magnitude: Float, duration: Float = 0f) {
        val force = Force(
            id = "wind_${System.currentTimeMillis()}",
            type = ForceType.WIND,
            position = Offset.Zero,
            direction = direction,
            magnitude = magnitude,
            radius = Float.MAX_VALUE,
            duration = duration
        )
        addForce(force)
    }

    /**
     * Apply attraction force to point
     */
    fun applyAttraction(position: Offset, magnitude: Float) {
        val force = Force(
            id = "attraction_${System.currentTimeMillis()}",
            type = ForceType.ATTRACTION,
            position = position,
            direction = Offset.Zero,
            magnitude = magnitude,
            radius = Float.MAX_VALUE,
            duration = 0f
        )
        addForce(force)
    }

    /**
     * Apply repulsion force from point
     */
    fun applyRepulsion(position: Offset, magnitude: Float) {
        val force = Force(
            id = "repulsion_${System.currentTimeMillis()}",
            type = ForceType.REPULSION,
            position = position,
            direction = Offset.Zero,
            magnitude = magnitude,
            radius = Float.MAX_VALUE,
            duration = 0f
        )
        addForce(force)
    }

    /**
     * Apply vortex force
     */
    fun applyVortex(position: Offset, magnitude: Float, radius: Float) {
        val force = Force(
            id = "vortex_${System.currentTimeMillis()}",
            type = ForceType.VORTEX,
            position = position,
            direction = Offset.Zero,
            magnitude = magnitude,
            radius = radius,
            duration = 0f
        )
        addForce(force)
    }

    companion object {
        @Volatile
        private var instance: PhysicsEngine? = null

        fun getInstance(): PhysicsEngine {
            return instance ?: synchronized(this) {
                instance ?: PhysicsEngine().also { instance = it }
            }
        }
    }
}

/**
 * Physics object configuration
 */
data class PhysicsConfig(
    val gravity: Float = 9.8f,
    val friction: Float = 0.98f,
    val elasticity: Float = 0.7f,
    val timeScale: Float = 1f,
    val maxVelocity: Float = 5000f,
    val subSteps: Int = 8
)

/**
 * Physics object in simulation
 */
data class PhysicsObject(
    val id: String,
    var position: Offset,
    var velocity: Offset,
    var acceleration: Offset = Offset.Zero,
    val mass: Float = 1f,
    val shape: Shape,
    val color: Color = Color.White,
    var rotation: Float = 0f,
    var angularVelocity: Float = 0f,
    val affectsByGravity: Boolean = true,
    val isStatic: Boolean = false,
    val constraints: List<Constraint> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
) {
    val bounds: RectF
        get() = when (shape) {
            is Shape.Circle -> {
                val r = shape.radius
                RectF(
                    position.x - r,
                    position.y - r,
                    position.x + r,
                    position.y + r
                )
            }
            is Shape.Rectangle -> {
                RectF(
                    position.x - shape.width / 2,
                    position.y - shape.height / 2,
                    position.x + shape.width / 2,
                    position.y + shape.height / 2
                )
            }
        }
}

/**
 * Shape types
 */
sealed class Shape {
    data class Circle(val radius: Float) : Shape()
    data class Rectangle(val width: Float, val height: Float) : Shape()
    data class Polygon(val points: List<Offset>) : Shape()
}

/**
 * Force applied to objects
 */
data class Force(
    val id: String,
    val type: ForceType,
    val position: Offset,
    val direction: Offset,
    val magnitude: Float,
    val radius: Float,
    val duration: Float = 0f // 0 = permanent
) {
    fun affectsObject(obj: PhysicsObject): Boolean {
        if (radius == Float.MAX_VALUE) return true

        val dx = obj.position.x - position.x
        val dy = obj.position.y - position.y
        val distance = sqrt(dx * dx + dy * dy)

        return distance <= radius
    }
}

/**
 * Force types
 */
enum class ForceType {
    EXPLOSION,
    WIND,
    ATTRACTION,
    REPULSION,
    VORTEX,
    GRAVITY_WELL,
    CUSTOM
}

/**
 * Constraint for physics objects
 */
interface Constraint {
    fun apply(obj: PhysicsObject)
}

/**
 * Boundary constraint
 */
data class BoundaryConstraint(
    val bounds: RectF,
    val bounceFactor: Float = 0.7f
) : Constraint {
    override fun apply(obj: PhysicsObject) {
        val boundingRect = obj.bounds

        // Left boundary
        if (boundingRect.left < bounds.left) {
            obj.position = Offset(
                bounds.left + (boundingRect.width / 2),
                obj.position.y
            )
            obj.velocity = Offset(-obj.velocity.x * bounceFactor, obj.velocity.y)
        }

        // Right boundary
        if (boundingRect.right > bounds.right) {
            obj.position = Offset(
                bounds.right - (boundingRect.width / 2),
                obj.position.y
            )
            obj.velocity = Offset(-obj.velocity.x * bounceFactor, obj.velocity.y)
        }

        // Top boundary
        if (boundingRect.top < bounds.top) {
            obj.position = Offset(
                obj.position.x,
                bounds.top + (boundingRect.height / 2)
            )
            obj.velocity = Offset(obj.velocity.x, -obj.velocity.y * bounceFactor)
        }

        // Bottom boundary
        if (boundingRect.bottom > bounds.bottom) {
            obj.position = Offset(
                obj.position.x,
                bounds.bottom - (boundingRect.height / 2)
            )
            obj.velocity = Offset(obj.velocity.x, -obj.velocity.y * bounceFactor)
        }
    }
}

/**
 * Spring constraint
 */
data class SpringConstraint(
    val anchorPoint: Offset,
    val restLength: Float,
    val stiffness: Float,
    val damping: Float
) : Constraint {
    override fun apply(obj: PhysicsObject) {
        val dx = obj.position.x - anchorPoint.x
        val dy = obj.position.y - anchorPoint.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > 0) {
            val displacement = distance - restLength
            val springForce = displacement * stiffness

            val forceX = (dx / distance) * springForce
            val forceY = (dy / distance) * springForce

            obj.velocity = Offset(
                obj.velocity.x - forceX - obj.velocity.x * damping,
                obj.velocity.y - forceY - obj.velocity.y * damping
            )
        }
    }
}
