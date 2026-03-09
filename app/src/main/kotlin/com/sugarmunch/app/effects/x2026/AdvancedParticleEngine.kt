package com.sugarmunch.app.effects.x2026

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

/**
 * ⚛️ ADVANCED PARTICLE PHYSICS ENGINE 2026
 * Realistic physics simulations with forces, collisions, and emergent behavior
 */

// ═════════════════════════════════════════════════════════════════
// PHYSICS DATA CLASSES
// ═════════════════════════════════════════════════════════════════

data class Particle(
    var position: Offset,
    var velocity: Offset,
    var mass: Float = 1f,
    var radius: Float = 5f,
    var color: Color,
    var lifetime: Float = 1f,
    var maxLifetime: Float = 1f,
    var type: ParticleType = ParticleType.NORMAL
) {
    val isAlive: Boolean get() = lifetime > 0
    val age: Float get() = 1f - (lifetime / maxLifetime)
    
    fun update(deltaTime: Float) {
        position += velocity * deltaTime
        lifetime -= deltaTime
    }
    
    fun applyForce(force: Offset) {
        velocity += force / mass
    }
}

enum class ParticleType {
    NORMAL, SPARK, TRAIL, EXPLOSION, GRAVITY_WELL, QUANTUM, PLASMA
}

data class PhysicsBody(
    var position: Offset,
    var velocity: Offset,
    var mass: Float,
    var radius: Float,
    val isStatic: Boolean = false
) {
    fun applyForce(force: Offset) {
        if (!isStatic) {
            velocity += force / mass
        }
    }
}

data class ForceField(
    val position: Offset,
    val strength: Float,
    val radius: Float,
    val type: ForceType
) {
    enum class ForceType { ATTRACT, REPEL, VORTEX, OSCILLATE }
}

// ═════════════════════════════════════════════════════════════════
// QUANTUM PARTICLE SYSTEM
// ═════════════════════════════════════════════════════════════════

class QuantumParticleSystem {
    val particles = mutableStateListOf<QuantumParticle>()
    var superpositionEnabled = true
    var entanglementPairs = mutableMapOf<Int, Int>()
    
    data class QuantumParticle(
        val id: Int,
        var position: Offset,
        var velocity: Offset,
        val possibleStates: List<Offset>,
        var currentStateIndex: Int = 0,
        var isEntangled: Boolean = false,
        var entangledPartner: Int? = null,
        var probabilityAmplitude: Float = 1f
    ) {
        val observedPosition: Offset
            get() = if (superpositionEnabled) possibleStates[currentStateIndex] else position
    }
    
    fun addParticle(position: Offset, stateCount: Int = 5): QuantumParticle {
        val id = particles.size
        val states = List(stateCount) {
            position + Offset(
                Random.nextFloat() * 100 - 50,
                Random.nextFloat() * 100 - 50
            )
        }
        val particle = QuantumParticle(id, position, Offset.Zero, states)
        particles.add(particle)
        return particle
    }
    
    fun entangle(particle1Id: Int, particle2Id: Int) {
        val p1 = particles.find { it.id == particle1Id }
        val p2 = particles.find { it.id == particle2Id }
        if (p1 != null && p2 != null) {
            p1.isEntangled = true
            p1.entangledPartner = particle2Id
            p2.isEntangled = true
            p2.entangledPartner = particle1Id
            entanglementPairs[particle1Id] = particle2Id
        }
    }
    
    fun observe(particleId: Int): Offset {
        val particle = particles.find { it.id == particleId } ?: return Offset.Zero
        
        // Collapse wave function
        particle.currentStateIndex = Random.nextInt(particle.possibleStates.size)
        particle.probabilityAmplitude = 0f
        
        // Instantaneous state change for entangled particle (spooky action!)
        particle.entangledPartner?.let { partnerId ->
            particles.find { it.id == partnerId }?.let { partner ->
                partner.currentStateIndex = (particle.currentStateIndex + 2) % partner.possibleStates.size
            }
        }
        
        return particle.observedPosition
    }
    
    fun update(deltaTime: Float) {
        particles.forEach { particle ->
            // Quantum fluctuation
            if (superpositionEnabled) {
                particle.currentStateIndex = (particle.currentStateIndex + 1) % particle.possibleStates.size
            }
            
            // Update actual position
            particle.position += particle.velocity * deltaTime
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// GRAVITY & SPACETIME SIMULATION
// ═════════════════════════════════════════════════════════════════

class SpacetimeSimulation {
    val masses = mutableListOf<GravityWell>()
    var spacetimeGrid = mutableListOf<GridPoint>()
    
    data class GravityWell(
        val position: Offset,
        val mass: Float,
        val radius: Float,
        val isBlackHole: Boolean = false
    )
    
    data class GridPoint(
        val x: Float,
        val y: Float,
        var displacement: Float = 0f,
        var originalPosition: Offset = Offset(x, y)
    )
    
    fun addMass(position: Offset, mass: Float, radius: Float, isBlackHole: Boolean = false) {
        masses.add(GravityWell(position, mass, radius, isBlackHole))
    }
    
    fun calculateSpacetimeDeformation(width: Float, height: Float, gridResolution: Int = 20) {
        spacetimeGrid.clear()
        
        for (x in 0..gridResolution) {
            for (y in 0..gridResolution) {
                val px = x * width / gridResolution
                val py = y * height / gridResolution
                val point = GridPoint(px, py)
                
                // Calculate deformation from all masses
                masses.forEach { mass ->
                    val distance = sqrt((px - mass.position.x).pow(2) + (py - mass.position.y).pow(2))
                    if (distance > 0) {
                        val deformation = mass.mass / (distance * 0.1f + 1)
                        point.displacement += deformation
                    }
                }
                
                spacetimeGrid.add(point)
            }
        }
    }
    
    fun getGravitationalForce(position: Offset): Offset {
        var totalForce = Offset.Zero
        
        masses.forEach { mass ->
            val delta = mass.position - position
            val distance = sqrt(delta.x.pow(2) + delta.y.pow(2))
            
            if (distance > 0 && distance > mass.radius) {
                val forceMagnitude = mass.mass / (distance.pow(2) * 0.01f + 1)
                val direction = delta / distance
                totalForce += direction * forceMagnitude
                
                // Black hole event horizon
                if (mass.isBlackHole && distance < mass.radius * 1.5f) {
                    totalForce *= 10f // Stronger pull near black hole
                }
            }
        }
        
        return totalForce
    }
}

// ═════════════════════════════════════════════════════════════════
// BIOLOGICAL ECOSYSTEM SIMULATION
// ═════════════════════════════════════════════════════════════════

class EcosystemSimulation {
    val organisms = mutableStateListOf<Organism>()
    val food = mutableStateListOf<Food>()
    
    data class Organism(
        var position: Offset,
        var velocity: Offset,
        var energy: Float,
        val dna: DNA,
        var age: Float = 0f,
        var state: State = State.WANDERING
    ) {
        enum class State { WANDERING, HUNTING, FLEEING, REPRODUCING, DEAD }
        
        val speed: Float get() = dna.speedGene * (energy / 100f)
        val size: Float get() = dna.sizeGene * (1f + age / 100f)
        val canReproduce: Boolean get() = energy > 80 && age > 20
        val isDead: Boolean get() = energy <= 0 || age > dna.lifespanGene
        
        data class DNA(
            val speedGene: Float,
            val sizeGene: Float,
            val senseRange: Float,
            val lifespanGene: Float,
            val colorGene: Color
        )
        
        fun mutate(): DNA {
            return DNA(
                speedGene = (speedGene + Random.nextFloat() * 0.2f - 0.1f).coerceIn(0.5f, 3f),
                sizeGene = (sizeGene + Random.nextFloat() * 0.2f - 0.1f).coerceIn(0.3f, 2f),
                senseRange = (senseRange + Random.nextFloat() * 10f - 5f).coerceIn(20f, 200f),
                lifespanGene = (lifespanGene + Random.nextFloat() * 10f - 5f).coerceIn(30f, 150f),
                colorGene = colorGene.copy(
                    red = (colorGene.red + Random.nextFloat() * 0.1f - 0.05f).coerceIn(0f, 1f),
                    green = (colorGene.green + Random.nextFloat() * 0.1f - 0.05f).coerceIn(0f, 1f),
                    blue = (colorGene.blue + Random.nextFloat() * 0.1f - 0.05f).coerceIn(0f, 1f)
                )
            )
        }
    }
    
    data class Food(
        var position: Offset,
        var energy: Float,
        var isBeingEaten: Boolean = false
    )
    
    fun spawnOrganism(position: Offset) {
        val dna = Organism.DNA(
            speedGene = Random.nextFloat() * 2f + 0.5f,
            sizeGene = Random.nextFloat() * 1f + 0.5f,
            senseRange = Random.nextFloat() * 100f + 50f,
            lifespanGene = Random.nextFloat() * 80f + 50f,
            colorGene = Color(
                Random.nextFloat(),
                Random.nextFloat(),
                Random.nextFloat()
            )
        )
        organisms.add(Organism(position, Offset.Zero, 50f, dna))
    }
    
    fun spawnFood(position: Offset, energy: Float = 20f) {
        food.add(Food(position, energy))
    }
    
    fun update(deltaTime: Float, bounds: Offset) {
        // Update organisms
        organisms.forEach { organism ->
            if (organism.isDead) {
                organism.state = Organism.State.DEAD
                return@forEach
            }
            
            organism.age += deltaTime
            organism.energy -= deltaTime * 0.1f // Metabolic cost
            
            // AI Behavior
            when (organism.state) {
                Organism.State.WANDERING -> {
                    // Random walk
                    organism.velocity += Offset(
                        Random.nextFloat() * 20 - 10,
                        Random.nextFloat() * 20 - 10
                    ) * deltaTime
                    
                    // Look for food
                    val nearestFood = food.minByOrNull {
                        val dist = it.position - organism.position
                        sqrt(dist.x.pow(2) + dist.y.pow(2))
                    }
                    
                    nearestFood?.let { target ->
                        val dist = target.position - organism.position
                        val distance = sqrt(dist.x.pow(2) + dist.y.pow(2))
                        if (distance < organism.dna.senseRange) {
                            organism.state = Organism.State.HUNTING
                        }
                    }
                }
                
                Organism.State.HUNTING -> {
                    val nearestFood = food.minByOrNull {
                        val dist = it.position - organism.position
                        sqrt(dist.x.pow(2) + dist.y.pow(2))
                    }
                    
                    nearestFood?.let { target ->
                        val direction = (target.position - organism.position).normalize()
                        organism.velocity = direction * organism.speed * 2f
                        
                        // Eat food
                        val dist = target.position - organism.position
                        if (sqrt(dist.x.pow(2) + dist.y.pow(2)) < organism.size + 5f) {
                            organism.energy += target.energy
                            food.remove(target)
                            organism.state = Organism.State.WANDERING
                        }
                    }
                }
                
                Organism.State.REPRODUCING -> {
                    // Reproduction handled separately
                }
                
                else -> {}
            }
            
            // Update position
            organism.position += organism.velocity * deltaTime
            organism.velocity *= 0.95f // Friction
            
            // Boundary wrap
            organism.position = Offset(
                organism.position.x.mod(bounds.x),
                organism.position.y.mod(bounds.y)
            )
        }
        
        // Remove dead organisms
        organisms.removeAll { it.isDead }
        
        // Reproduction
        organisms.filter { it.canReproduce }.forEach { parent ->
            if (Random.nextFloat() < 0.01f) { // 1% chance per frame
                val childDna = parent.dna.mutate()
                organisms.add(Organism(
                    position = parent.position + Offset(Random.nextFloat() * 20 - 10, Random.nextFloat() * 20 - 10),
                    velocity = Offset.Zero,
                    energy = 50f,
                    dna = childDna
                ))
                parent.energy -= 40f // Reproduction cost
                parent.state = Organism.State.WANDERING
            }
        }
    }
    
    private fun Offset.normalize(): Offset {
        val length = sqrt(x.pow(2) + y.pow(2))
        return if (length > 0) this / length else Offset.Zero
    }
    
    private fun Float.mod(other: Float): Float {
        val result = this % other
        return if (result < 0) result + other else result
    }
}

// ═════════════════════════════════════════════════════════════════
// COMPOSABLE PARTICLE RENDERERS
// ═════════════════════════════════════════════════════════════════

@Composable
fun QuantumParticleRenderer(
    system: QuantumParticleSystem,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.pointerInput(Unit) {
        detectTapGestures { tapOffset ->
            // Observe nearest particle on tap
            system.particles.minByOrNull { particle ->
                val dist = particle.position - tapOffset
                dist.x.pow(2) + dist.y.pow(2)
            }?.let { nearest ->
                system.observe(nearest.id)
            }
        }
    }) {
        system.particles.forEach { particle ->
            // Draw superposition cloud
            if (system.superpositionEnabled) {
                particle.possibleStates.forEachIndexed { index, state ->
                    val alpha = 0.3f / (index + 1)
                    drawCircle(
                        color = Color.Cyan.copy(alpha = alpha),
                        radius = 8f,
                        center = state
                    )
                }
            }
            
            // Draw actual particle (when observed)
            val color = if (particle.isEntangled) Color.Magenta else Color.Cyan
            drawCircle(
                color = color,
                radius = 10f,
                center = particle.position
            )
            
            // Draw entanglement line
            particle.entangledPartner?.let { partnerId ->
                system.particles.find { it.id == partnerId }?.let { partner ->
                    drawLine(
                        color = Color.Magenta.copy(alpha = 0.5f),
                        start = particle.position,
                        end = partner.position,
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }
        }
    }
}

@Composable
fun GravityWellRenderer(
    simulation: SpacetimeSimulation,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Draw spacetime grid
        simulation.spacetimeGrid.forEach { point ->
            val deformation = point.displacement.coerceIn(0f, 50f)
            val color = Color.Blue.copy(alpha = deformation / 100f)
            
            drawCircle(
                color = color,
                radius = 2f,
                center = Offset(point.x, point.y + deformation)
            )
        }
        
        // Draw masses
        simulation.masses.forEach { mass ->
            val color = if (mass.isBlackHole) Color.Black else Color(0xFF8B4513)
            
            // Event horizon glow for black holes
            if (mass.isBlackHole) {
                drawCircle(
                    color = Color.Purple.copy(alpha = 0.3f),
                    radius = mass.radius * 2f,
                    center = mass.position
                )
                
                // Accretion disk
                drawCircle(
                    color = Color(0xFFFF4500).copy(alpha = 0.5f),
                    radius = mass.radius * 1.5f,
                    center = mass.position,
                    style = Stroke(width = 8f)
                )
            }
            
            drawCircle(
                color = color,
                radius = mass.radius,
                center = mass.position
            )
        }
    }
}

@Composable
fun EcosystemRenderer(
    ecosystem: EcosystemSimulation,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.pointerInput(Unit) {
        detectTapGestures { tapOffset ->
            ecosystem.spawnFood(tapOffset, 30f)
        }
        detectDragGestures { change, _ ->
            if (Random.nextFloat() < 0.3f) {
                ecosystem.spawnFood(change.position, 20f)
            }
        }
    }) {
        // Draw food
        ecosystem.food.forEach { food ->
            drawCircle(
                color = Color.Green,
                radius = 4f,
                center = food.position
            )
        }
        
        // Draw organisms
        ecosystem.organisms.forEach { organism ->
            drawCircle(
                color = organism.dna.colorGene,
                radius = organism.size * 5f,
                center = organism.position
            )
            
            // Draw sense range (subtle)
            drawCircle(
                color = organism.dna.colorGene.copy(alpha = 0.1f),
                radius = organism.dna.senseRange,
                center = organism.position,
                style = Stroke(width = 1f)
            )
            
            // Draw energy indicator
            val energyPercent = organism.energy / 100f
            drawArc(
                color = if (energyPercent > 0.5f) Color.Green else Color.Red,
                startAngle = -90f,
                sweepAngle = 360f * energyPercent,
                useCenter = false,
                topLeft = organism.position - Offset(organism.size * 6f, organism.size * 6f),
                size = androidx.compose.ui.geometry.Size(
                    organism.size * 12f,
                    organism.size * 12f
                ),
                style = Stroke(width = 2f)
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// UTILITY FUNCTIONS
// ═════════════════════════════════════════════════════════════════

fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction.coerceIn(0f, 1f)
}

fun Color.lerp(other: Color, fraction: Float): Color {
    return Color(
        red = lerp(red, other.red, fraction),
        green = lerp(green, other.green, fraction),
        blue = lerp(blue, other.blue, fraction),
        alpha = lerp(alpha, other.alpha, fraction)
    )
}

// Fractal generation
fun generateMandelbrotPoint(cx: Float, cy: Float, maxIterations: Int = 100): Pair<Int, Float> {
    var x = 0f
    var y = 0f
    var iteration = 0
    
    while (x * x + y * y <= 4 && iteration < maxIterations) {
        val xTemp = x * x - y * y + cx
        y = 2 * x * y + cy
        x = xTemp
        iteration++
    }
    
    // Smooth coloring
    val smoothIteration = if (iteration < maxIterations) {
        iteration + 1 - ln(ln(sqrt(x * x + y * y))) / ln(2f)
    } else {
        iteration.toFloat()
    }
    
    return Pair(iteration, smoothIteration / maxIterations)
}

// Noise function for organic movement
fun simplexNoise(x: Float, y: Float): Float {
    // Simplified noise function
    return (sin(x * 0.1f) * cos(y * 0.1f) + 1f) / 2f
}
