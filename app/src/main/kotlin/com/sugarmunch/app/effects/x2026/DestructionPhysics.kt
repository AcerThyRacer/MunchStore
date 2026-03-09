package com.sugarmunch.app.effects.x2026

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*

/**
 * Destruction physics simulation for shattering glass, candy, ice
 * Uses Voronoi fracture patterns and rigid body dynamics
 */
class DestructionPhysics(
    val gravity: Float = 9.8f,
    val friction: Float = 0.8f,
    val restitution: Float = 0.6f
) {
    // Shards (debris pieces)
    private val shards = mutableListOf<Shard>()
    
    // Particle debris
    private val debris = mutableListOf<Debris>()
    
    // Physics settings
    var airResistance = 0.02f
    var angularDamping = 0.95f
    var maxShards = 100
    var maxDebris = 200
    
    /**
     * Shatter an object at impact point
     */
    fun shatter(
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        impactX: Float,
        impactY: Float,
        force: Float,
        materialType: MaterialType = MaterialType.GLASS
    ) {
        // Generate Voronoi fracture pattern
        val fragments = generateVoronoiFracture(
            centerX, centerY, width, height,
            impactX, impactY, force, materialType
        )
        
        // Create shards from fragments
        fragments.forEach { fragment ->
            if (shards.size < maxShards) {
                val velocityX = (fragment.centerX - impactX) * force * 0.1f
                val velocityY = (fragment.centerY - impactY) * force * 0.1f
                val angularVelocity = (Random.nextFloat() - 0.5f) * force * 0.05f
                
                shards.add(Shard(
                    vertices = fragment.vertices,
                    x = fragment.centerX,
                    y = fragment.centerY,
                    vx = velocityX,
                    vy = velocityY,
                    rotation = Random.nextFloat() * 360f,
                    angularVelocity = angularVelocity,
                    color = getMaterialColor(materialType),
                    materialType = materialType,
                    size = fragment.size,
                    alive = true
                ))
            }
        }
        
        // Generate debris particles
        val debrisCount = (force * 5).toInt().coerceIn(10, 50)
        repeat(debrisCount) {
            if (debris.size < maxDebris) {
                val angle = Random.nextFloat() * 2f * PI.toFloat()
                val speed = Random.nextFloat() * force * 2f
                
                debris.add(Debris(
                    x = impactX + (Random.nextFloat() - 0.5f) * width * 0.5f,
                    y = impactY + (Random.nextFloat() - 0.5f) * height * 0.5f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    size = Random.nextFloat() * 3f + 1f,
                    color = getMaterialColor(materialType),
                    alive = true
                ))
            }
        }
    }
    
    /**
     * Generate Voronoi fracture pattern
     */
    private fun generateVoronoiFracture(
        centerX: Float, centerY: Float,
        width: Float, height: Float,
        impactX: Float, impactY: Float,
        force: Float,
        materialType: MaterialType
    ): List<Fragment> {
        val fragments = mutableListOf<Fragment>()
        
        // Number of fracture points based on force
        val numPoints = (force * 2).toInt().coerceIn(5, 20)
        
        // Generate random seed points
        val points = mutableListOf<Offset>()
        points.add(Offset(impactX, impactY)) // Impact point is always a seed
        
        repeat(numPoints - 1) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val distance = Random.nextFloat() * max(width, height) * 0.5f
            val x = centerX + cos(angle) * distance
            val y = centerY + sin(angle) * distance
            
            // Keep within bounds
            if (x >= centerX - width / 2 && x <= centerX + width / 2 &&
                y >= centerY - height / 2 && y <= centerY + height / 2) {
                points.add(Offset(x, y))
            }
        }
        
        // Generate polygons around each point (simplified Voronoi)
        points.forEach { point ->
            val vertices = mutableListOf<Offset>()
            val numSides = (Random.nextFloat() * 4 + 3).toInt() // 3-6 sides
            
            for (i in 0 until numSides) {
                val angle = (i.toFloat() / numSides) * 2f * PI.toFloat()
                val radius = Random.nextFloat() * 20f + 10f
                val vertexX = point.x + cos(angle) * radius
                val vertexY = point.y + sin(angle) * radius
                vertices.add(Offset(vertexX, vertexY))
            }
            
            fragments.add(Fragment(
                vertices = vertices,
                centerX = point.x,
                centerY = point.y,
                size = calculateFragmentSize(vertices)
            ))
        }
        
        return fragments
    }
    
    /**
     * Calculate fragment size from vertices
     */
    private fun calculateFragmentSize(vertices: List<Offset>): Float {
        if (vertices.isEmpty()) return 10f
        
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        
        vertices.forEach { vertex ->
            minX = minOf(minX, vertex.x)
            maxX = maxOf(maxX, vertex.x)
            minY = minOf(minY, vertex.y)
            maxY = maxOf(maxY, vertex.y)
        }
        
        return maxOf(maxX - minX, maxY - minY)
    }
    
    /**
     * Get material color
     */
    private fun getMaterialColor(materialType: MaterialType): Color {
        return when (materialType) {
            MaterialType.GLASS -> Color(0xFFE0F7FA).copy(alpha = 0.8f)
            MaterialType.CANDY -> Color(0xFFFF69B4)
            MaterialType.ICE -> Color(0xFFB3E5FC)
            MaterialType.CERAMIC -> Color(0xFFF5F5F5)
            MaterialType.WOOD -> Color(0xFF8D6E63)
        }
    }
    
    /**
     * Step simulation forward
     */
    fun step(dt: Float = 0.016f) {
        // Update shards
        shards.forEach { shard ->
            if (!shard.alive) return@forEach
            
            // Apply gravity
            shard.vy += gravity * 50f * dt
            
            // Apply air resistance
            shard.vx *= (1f - airResistance)
            shard.vy *= (1f - airResistance)
            
            // Update position
            shard.x += shard.vx * dt
            shard.y += shard.vy * dt
            
            // Update rotation
            shard.rotation += shard.angularVelocity
            shard.angularVelocity *= angularDamping
            
            // Boundary collisions
            if (shard.y > 1000f) { // Ground level
                shard.y = 1000f
                shard.vy *= -restitution
                shard.vx *= friction
                shard.angularVelocity *= 0.5f
                
                // Fade out slowly
                shard.alpha -= 0.02f
                if (shard.alpha <= 0f) {
                    shard.alive = false
                }
            }
        }
        
        // Update debris
        debris.forEach { d ->
            if (!d.alive) return@forEach
            
            // Apply gravity
            d.vy += gravity * 30f * dt
            
            // Apply air resistance
            d.vx *= (1f - airResistance * 2f)
            d.vy *= (1f - airResistance * 2f)
            
            // Update position
            d.x += d.vx * dt
            d.y += d.vy * dt
            
            // Fade out
            d.alpha -= 0.01f
            if (d.alpha <= 0f || d.y > 1000f) {
                d.alive = false
            }
        }
        
        // Remove dead objects
        shards.removeAll { !it.alive }
        debris.removeAll { !it.alive }
    }
    
    /**
     * Get all shards
     */
    fun getShards(): List<Shard> = shards.filter { it.alive }
    
    /**
     * Get all debris
     */
    fun getDebris(): List<Debris> = debris.filter { it.alive }
    
    /**
     * Clear all objects
     */
    fun clear() {
        shards.clear()
        debris.clear()
    }
}

/**
 * Shard (large fragment)
 */
data class Shard(
    val vertices: List<Offset>,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var angularVelocity: Float,
    val color: Color,
    val materialType: MaterialType,
    val size: Float,
    var alpha: Float = 1f,
    var alive: Boolean
)

/**
 * Debris particle
 */
data class Debris(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color,
    var alpha: Float = 1f,
    var alive: Boolean
)

/**
 * Fragment data for Voronoi generation
 */
data class Fragment(
    val vertices: List<Offset>,
    val centerX: Float,
    val centerY: Float,
    val size: Float
)

/**
 * Material types with different properties
 */
enum class MaterialType {
    GLASS,      // Transparent, shatters into many pieces
    CANDY,      // Colorful, sticky pieces
    ICE,        // White/blue, cracks
    CERAMIC,    // White, sharp shards
    WOOD        // Brown, splinters
}

/**
 * Random number generator
 */
private object Random {
    private var seed = System.nanoTime()
    
    fun nextFloat(): Float {
        seed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        return (seed shr 12).toFloat() / 0x100000000L.toFloat()
    }
}