package com.sugarmunch.app.effects.x2026

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*

/**
 * Cloth simulation using Verlet integration
 * Simulates fabric physics with constraints, wind, and collisions
 */
class ClothSimulation(
    val width: Int = 40,
    val height: Int = 30,
    val spacing: Float = 8f,
    gravity: Float = 9.8f,
    stiffness: Float = 0.9f
) {
    // Point masses
    private val points = mutableListOf<PointMass>()
    
    // Structural constraints (keep points connected)
    private val constraints = mutableListOf<Constraint>()
    
    // Physics parameters
    var gravity = gravity
    var stiffness = stiffness
    var damping = 0.98f
    var windStrength = 0.5f
    var windTurbulence = 0.3f
    
    // Cloth dimensions
    private val clothWidth: Float
        get() = (width - 1) * spacing
    private val clothHeight: Float
        get() = (height - 1) * spacing
    
    // Interaction
    private var touchPoint: Offset? = null
    private var touchRadius = 50f
    private var touchForce = 20f
    
    init {
        initializeCloth()
    }
    
    /**
     * Initialize cloth grid
     */
    private fun initializeCloth() {
        points.clear()
        constraints.clear()
        
        // Create point masses
        for (y in 0 until height) {
            for (x in 0 until width) {
                val posX = x * spacing
                val posY = y * spacing
                
                // Pin top row
                val pinned = y == 0
                
                val point = PointMass(
                    x = posX,
                    y = posY,
                    pinned = pinned
                )
                points.add(point)
            }
        }
        
        // Create constraints
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = getIndex(x, y)
                
                // Horizontal constraint
                if (x < width - 1) {
                    val rightIndex = getIndex(x + 1, y)
                    constraints.add(Constraint(index, rightIndex, spacing))
                }
                
                // Vertical constraint
                if (y < height - 1) {
                    val bottomIndex = getIndex(x, y + 1)
                    constraints.add(Constraint(index, bottomIndex, spacing))
                }
                
                // Shear constraints (diagonal)
                if (x < width - 1 && y < height - 1) {
                    val diagIndex = getIndex(x + 1, y + 1)
                    constraints.add(Constraint(index, diagIndex, spacing * sqrt(2f)))
                }
                
                if (x > 0 && y < height - 1) {
                    val diagIndex = getIndex(x - 1, y + 1)
                    constraints.add(Constraint(index, diagIndex, spacing * sqrt(2f)))
                }
            }
        }
    }
    
    /**
     * Get index from x,y coordinates
     */
    private fun getIndex(x: Int, y: Int): Int {
        return (y * width + x).coerceIn(0, points.size - 1)
    }
    
    /**
     * Step simulation forward
     */
    fun step(dt: Float = 0.016f) {
        // Apply forces
        applyForces(dt)
        
        // Update points
        updatePoints(dt)
        
        // Satisfy constraints
        for (i in 0 until 5) { // Multiple iterations for stability
            satisfyConstraints()
        }
        
        // Handle collisions
        handleCollisions()
        
        // Clear touch point
        touchPoint = null
    }
    
    /**
     * Apply forces to all points
     */
    private fun applyForces(dt: Float) {
        val time = System.currentTimeMillis() / 1000f
        
        points.forEach { point ->
            if (!point.pinned) {
                // Gravity
                point.forceY += gravity * 100
                
                // Wind
                val windX = sin(time * 2f + point.y * 0.1f) * windStrength * 50
                val windY = cos(time * 1.5f + point.x * 0.1f) * windStrength * 30
                val turbulence = (Random.nextFloat() - 0.5f) * windTurbulence * 50
                
                point.forceX += windX + turbulence
                point.forceY += windY
            }
        }
    }
    
    /**
     * Update point positions using Verlet integration
     */
    private fun updatePoints(dt: Float) {
        points.forEach { point ->
            if (!point.pinned) {
                val vx = (point.x - point.prevX) * damping
                val vy = (point.y - point.prevY) * damping
                
                point.prevX = point.x
                point.prevY = point.y
                
                point.x += vx + point.forceX * dt
                point.y += vy + point.forceY * dt
                
                // Touch interaction
                touchPoint?.let { touch ->
                    val dx = point.x - touch.x
                    val dy = point.y - touch.y
                    val dist = sqrt(dx * dx + dy * dy)
                    
                    if (dist < touchRadius) {
                        val force = (1f - dist / touchRadius) * touchForce
                        point.x += (dx / dist) * force
                        point.y += (dy / dist) * force
                    }
                }
            }
            
            // Reset forces
            point.forceX = 0f
            point.forceY = 0f
        }
    }
    
    /**
     * Satisfy all constraints
     */
    private fun satisfyConstraints() {
        constraints.forEach { constraint ->
            val point1 = points[constraint.index1]
            val point2 = points[constraint.index2]
            
            val dx = point2.x - point1.x
            val dy = point2.y - point1.y
            val dist = sqrt(dx * dx + dy * dy)
            
            if (dist > 0) {
                val diff = (dist - constraint.restLength) / dist
                val correction = diff * 0.5f * stiffness
                
                val offsetX = dx * correction
                val offsetY = dy * correction
                
                if (!point1.pinned) {
                    point1.x += offsetX
                    point1.y += offsetY
                }
                
                if (!point2.pinned) {
                    point2.x -= offsetX
                    point2.y -= offsetY
                }
            }
        }
    }
    
    /**
     * Handle boundary collisions
     */
    private fun handleCollisions() {
        points.forEach { point ->
            if (!point.pinned) {
                // Bottom boundary
                if (point.y > clothHeight + 100) {
                    point.y = clothHeight + 100
                    point.prevY = point.y + (point.prevY - point.y) * 0.5f
                }
                
                // Left boundary
                if (point.x < 0) {
                    point.x = 0f
                    point.prevX = point.x + (point.prevX - point.x) * 0.5f
                }
                
                // Right boundary
                if (point.x > clothWidth) {
                    point.x = clothWidth
                    point.prevX = point.x + (point.prevX - point.x) * 0.5f
                }
            }
        }
    }
    
    /**
     * Set touch interaction point
     */
    fun setTouchPoint(x: Float, y: Float) {
        touchPoint = Offset(x, y)
    }
    
    /**
     * Get point at index
     */
    fun getPoint(index: Int): PointMass {
        return points[index]
    }
    
    /**
     * Get all points
     */
    fun getAllPoints(): List<PointMass> {
        return points
    }
    
    /**
     * Get constraint between two points
     */
    fun getConstraints(): List<Constraint> {
        return constraints
    }
    
    /**
     * Reset cloth
     */
    fun reset() {
        initializeCloth()
    }
    
    /**
     * Change fabric type
     */
    fun setFabricType(type: FabricType) {
        when (type) {
            FabricType.SILK -> {
                gravity = 9.8f
                stiffness = 0.95f
                damping = 0.99f
                windStrength = 0.8f
            }
            FabricType.COTTON -> {
                gravity = 9.8f
                stiffness = 0.85f
                damping = 0.97f
                windStrength = 0.5f
            }
            FabricType.VELVET -> {
                gravity = 9.8f
                stiffness = 0.75f
                damping = 0.95f
                windStrength = 0.3f
            }
            FabricType.METALLIC -> {
                gravity = 9.8f
                stiffness = 0.98f
                damping = 0.995f
                windStrength = 0.2f
            }
        }
    }
}

/**
 * Point mass in the cloth simulation
 */
data class PointMass(
    var x: Float,
    var y: Float,
    val pinned: Boolean
) {
    var prevX = x
    var prevY = y
    var forceX = 0f
    var forceY = 0f
}

/**
 * Constraint between two points
 */
data class Constraint(
    val index1: Int,
    val index2: Int,
    val restLength: Float
)

/**
 * Fabric types with different physical properties
 */
enum class FabricType {
    SILK,       // Light, flowing
    COTTON,     // Medium weight
    VELVET,     // Heavy, drapey
    METALLIC    // Stiff, reflective
}

/**
 * Simple random number generator for wind turbulence
 */
private object Random {
    private var seed = System.nanoTime()
    
    fun nextFloat(): Float {
        seed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        return (seed shr 12).toFloat() / 0x100000000L.toFloat()
    }
}
