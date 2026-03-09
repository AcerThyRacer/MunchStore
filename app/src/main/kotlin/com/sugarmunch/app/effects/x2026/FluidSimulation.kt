package com.sugarmunch.app.effects.x2026

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*

/**
 * Grid-based Navier-Stokes fluid simulation for SugarMunch
 * Simulates liquid candy, honey, slime with viscosity control
 */
class FluidSimulation(
    val gridSize: Int = 64,
    var viscosity: Float = 0.0001f,
    var diffusion: Float = 0.00001f,
    var dt: Float = 0.1f
) {
    // Grid arrays
    private val size = (gridSize + 2) * (gridSize + 2)
    
    // Velocity fields
    private val u = FloatArray(size)
    private val v = FloatArray(size)
    private val uPrev = FloatArray(size)
    private val vPrev = FloatArray(size)
    
    // Density field (for rendering)
    private val density = FloatArray(size)
    private val densityPrev = FloatArray(size)
    
    // Pressure field for projection step
    private val pressure = FloatArray(size)
    private val divergence = FloatArray(size)
    
    // Fluid properties
    var surfaceTension = 0.5f
    var gravity = 9.8f
    var fluidColor = Color(0xFFFF69B4) // Hot pink default
    var secondaryColor = Color(0xFF00FFA3) // Mint
    
    // Interaction points
    private val touchPoints = mutableListOf<FluidTouchPoint>()
    
    /**
     * Add density and velocity at a point
     */
    fun addDensity(x: Int, y: Int, amount: Float) {
        val index = IX(x, y)
        densityPrev[index] += amount
    }
    
    fun addVelocity(x: Int, y: Int, amountX: Float, amountY: Float) {
        val index = IX(x, y)
        uPrev[index] += amountX
        vPrev[index] += amountY
    }
    
    /**
     * Add touch interaction point
     */
    fun addTouchPoint(x: Float, y: Float, force: Float = 10f) {
        touchPoints.add(FluidTouchPoint(x, y, force))
    }
    
    /**
     * Step simulation forward
     */
    fun step() {
        // Process touch points
        processTouchPoints()
        touchPoints.clear()
        
        // Velocity step
        diffuse(u, uPrev, viscosity, dt)
        diffuse(v, vPrev, viscosity, dt)
        
        project(u, v, uPrev, vPrev)
        
        advect(u, uPrev, u, v, dt)
        advect(v, vPrev, u, v, dt)
        
        project(u, v, uPrev, vPrev)
        
        // Density step
        diffuse(density, densityPrev, diffusion, dt)
        advect(density, densityPrev, u, v, dt)
        
        // Apply gravity
        applyGravity()
        
        // Apply surface tension
        applySurfaceTension()
    }
    
    /**
     * Process touch interaction points
     */
    private fun processTouchPoints() {
        touchPoints.forEach { point ->
            val gx = (point.x * gridSize).toInt() + 1
            val gy = (point.y * gridSize).toInt() + 1
            
            // Add density
            for (i in -1..1) {
                for (j in -1..1) {
                    val nx = (gx + i).coerceIn(1, gridSize)
                    val ny = (gy + j).coerceIn(1, gridSize)
                    addDensity(nx, ny, point.force * 10f)
                    
                    // Radial velocity
                    val dx = i.toFloat()
                    val dy = j.toFloat()
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist > 0) {
                        addVelocity(nx, ny, dx / dist * point.force, dy / dist * point.force)
                    }
                }
            }
        }
    }
    
    /**
     * Apply gravity to fluid
     */
    private fun applyGravity() {
        for (i in 1..gridSize) {
            for (j in 1..gridSize) {
                val index = IX(i, j)
                v[index] += gravity * dt * 0.01f
            }
        }
    }
    
    /**
     * Apply surface tension effects
     */
    private fun applySurfaceTension() {
        for (i in 1..gridSize) {
            for (j in 1..gridSize) {
                val index = IX(i, j)
                val d = density[index]
                
                if (d > 0.1f) {
                    // Calculate curvature
                    val left = if (i > 1) density[IX(i - 1, j)] else 0f
                    val right = if (i < gridSize) density[IX(i + 1, j)] else 0f
                    val top = if (j > 1) density[IX(i, j - 1)] else 0f
                    val bottom = if (j < gridSize) density[IX(i, j + 1)] else 0f
                    
                    val curvature = (left + right + top + bottom) / 4f - d
                    
                    // Apply surface tension force
                    density[index] += curvature * surfaceTension * 0.1f
                }
            }
        }
    }
    
    /**
     * Diffuse quantity through grid
     */
    private fun diffuse(x: FloatArray, x0: FloatArray, diff: Float, dt: Float) {
        val a = dt * diff * gridSize * gridSize
        lin_solve(x, x0, a, 1f + 4f * a)
    }
    
    /**
     * Linear solver for diffusion and projection
     */
    private fun lin_solve(x: FloatArray, x0: FloatArray, a: Float, c: Float) {
        val cRecip = 1f / c
        for (k in 0 until 10) {
            for (i in 1..gridSize) {
                for (j in 1..gridSize) {
                    val index = IX(i, j)
                    x[index] = (x0[index] + a * (
                        x[IX(i + 1, j)] +
                        x[IX(i - 1, j)] +
                        x[IX(i, j + 1)] +
                        x[IX(i, j - 1)]
                    )) * cRecip
                }
            }
            set_bnd(x)
        }
    }
    
    /**
     * Project step to maintain mass conservation
     */
    private fun project(u: FloatArray, v: FloatArray, p: FloatArray, div: FloatArray) {
        for (i in 1..gridSize) {
            for (j in 1..gridSize) {
                val index = IX(i, j)
                div[index] = -0.5f * (
                    u[IX(i + 1, j)] - u[IX(i - 1, j)] +
                    v[IX(i, j + 1)] - v[IX(i, j - 1)]
                ) / gridSize
                p[index] = 0f
            }
        }
        
        set_bnd(div)
        set_bnd(p)
        
        lin_solve(p, div, 1f, 4f)
        
        for (i in 1..gridSize) {
            for (j in 1..gridSize) {
                val index = IX(i, j)
                u[index] -= 0.5f * gridSize * (p[IX(i + 1, j)] - p[IX(i - 1, j)])
                v[index] -= 0.5f * gridSize * (p[IX(i, j + 1)] - p[IX(i, j - 1)])
            }
        }
    }
    
    /**
     * Advect quantity through velocity field
     */
    private fun advect(d: FloatArray, d0: FloatArray, u: FloatArray, v: FloatArray, dt: Float) {
        var i0: Int
        var j0: Int
        var i1: Int
        var j1: Int
        var x: Float
        var y: Float
        var s0: Float
        var t0: Float
        var s1: Float
        var t1: Float
        var dt0: Float
        
        dt0 = dt * gridSize
        
        for (i in 1..gridSize) {
            for (j in 1..gridSize) {
                val index = IX(i, j)
                
                x = i - dt0 * u[index]
                y = j - dt0 * v[index]
                
                x = x.coerceIn(0.5f, gridSize + 0.5f)
                y = y.coerceIn(0.5f, gridSize + 0.5f)
                
                i0 = x.toInt()
                i1 = i0 + 1
                j0 = y.toInt()
                j1 = j0 + 1
                
                s1 = x - i0
                s0 = 1f - s1
                t1 = y - j0
                t0 = 1f - t1
                
                d[index] = (
                    s0 * (t0 * d0[IX(i0, j0)] + t1 * d0[IX(i0, j1)]) +
                    s1 * (t0 * d0[IX(i1, j0)] + t1 * d0[IX(i1, j1)])
                )
            }
        }
        
        set_bnd(d)
    }
    
    /**
     * Set boundary conditions
     */
    private fun set_bnd(x: FloatArray) {
        for (i in 1..gridSize) {
            x[IX(i, 0)] = x[IX(i, 1)]
            x[IX(i, gridSize + 1)] = x[IX(i, gridSize)]
        }
        
        for (j in 1..gridSize) {
            x[IX(0, j)] = x[IX(1, j)]
            x[IX(gridSize + 1, j)] = x[IX(gridSize, j)]
        }
        
        x[IX(0, 0)] = 0.5f * (x[IX(1, 0)] + x[IX(0, 1)])
        x[IX(0, gridSize + 1)] = 0.5f * (x[IX(1, gridSize + 1)] + x[IX(0, gridSize)])
        x[IX(gridSize + 1, 0)] = 0.5f * (x[IX(gridSize, 0)] + x[IX(gridSize + 1, 1)])
        x[IX(gridSize + 1, gridSize + 1)] = 0.5f * (x[IX(gridSize, gridSize + 1)] + x[IX(gridSize + 1, gridSize)])
    }
    
    /**
     * Index mapping function
     */
    private fun IX(x: Int, y: Int): Int {
        return (x + y * (gridSize + 2)).coerceIn(0, size - 1)
    }
    
    /**
     * Get density at grid position
     */
    fun getDensity(x: Int, y: Int): Float {
        return density[IX(x, y)].coerceIn(0f, 1f)
    }
    
    /**
     * Get velocity at grid position
     */
    fun getVelocity(x: Int, y: Int): Offset {
        val index = IX(x, y)
        return Offset(u[index], v[index])
    }
    
    /**
     * Get fluid color with gradient based on density and velocity
     */
    fun getFluidColor(x: Int, y: Int): Color {
        val d = getDensity(x, y).coerceIn(0f, 1f)
        val vel = getVelocity(x, y)
        val speed = sqrt(vel.x * vel.x + vel.y * vel.y) * 0.1f
        
        // Mix colors based on density and speed
        val r = (fluidColor.red * d + secondaryColor.red * speed).coerceIn(0f, 1f)
        val g = (fluidColor.green * d + secondaryColor.green * speed).coerceIn(0f, 1f)
        val b = (fluidColor.blue * d + secondaryColor.blue * speed).coerceIn(0f, 1f)
        val a = d * 0.8f + speed * 0.2f
        
        return Color(r, g, b, a)
    }
    
    /**
     * Reset simulation
     */
    fun reset() {
        u.fill(0f)
        v.fill(0f)
        uPrev.fill(0f)
        vPrev.fill(0f)
        density.fill(0f)
        densityPrev.fill(0f)
        pressure.fill(0f)
        divergence.fill(0f)
    }
}

/**
 * Touch interaction point for fluid
 */
data class FluidTouchPoint(
    val x: Float,
    val y: Float,
    val force: Float
)

/**
 * Fluid simulation presets
 */
object FluidPresets {
    fun waterConfig(): FluidSimulationConfig {
        return FluidSimulationConfig(
            viscosity = 0.0001f,
            diffusion = 0.00001f,
            surfaceTension = 0.3f,
            gravity = 9.8f,
            fluidColor = Color(0xFF00BFFF),
            secondaryColor = Color(0xFF87CEEB),
            name = "Water"
        )
    }
    
    fun honeyConfig(): FluidSimulationConfig {
        return FluidSimulationConfig(
            viscosity = 0.5f,
            diffusion = 0.00001f,
            surfaceTension = 0.7f,
            gravity = 9.8f,
            fluidColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFFFFA500),
            name = "Honey"
        )
    }
    
    fun slimeConfig(): FluidSimulationConfig {
        return FluidSimulationConfig(
            viscosity = 2.0f,
            diffusion = 0.0001f,
            surfaceTension = 0.9f,
            gravity = 4.9f,
            fluidColor = Color(0xFF00FF00),
            secondaryColor = Color(0xFF008000),
            name = "Slime"
        )
    }
    
    fun lavaConfig(): FluidSimulationConfig {
        return FluidSimulationConfig(
            viscosity = 0.1f,
            diffusion = 0.001f,
            surfaceTension = 0.5f,
            gravity = 9.8f,
            fluidColor = Color(0xFFFF4500),
            secondaryColor = Color(0xFFFFD700),
            name = "Lava"
        )
    }
    
    fun candySyrupConfig(): FluidSimulationConfig {
        return FluidSimulationConfig(
            viscosity = 0.3f,
            diffusion = 0.0001f,
            surfaceTension = 0.6f,
            gravity = 9.8f,
            fluidColor = Color(0xFFFF69B4),
            secondaryColor = Color(0xFF00FFA3),
            name = "Candy Syrup"
        )
    }
}

data class FluidSimulationConfig(
    val viscosity: Float,
    val diffusion: Float,
    val surfaceTension: Float,
    val gravity: Float,
    val fluidColor: Color,
    val secondaryColor: Color,
    val name: String
)
