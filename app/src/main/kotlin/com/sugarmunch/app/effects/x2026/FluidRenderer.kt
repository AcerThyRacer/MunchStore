package com.sugarmunch.app.effects.x2026

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Composable for rendering fluid simulation
 */
@Composable
fun FluidSimulationRenderer(
    modifier: Modifier = Modifier,
    simulation: FluidSimulation,
    renderMode: FluidRenderMode = FluidRenderMode.SURFACE,
    onTouch: (Offset) -> Unit = {}
) {
    val alpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            alpha.animateTo(1f, animationSpec = tween(1000))
        }
    }
    
    // Auto-step simulation
    LaunchedEffect(simulation) {
        while (true) {
            simulation.step()
            kotlinx.coroutines.delay(16) // ~60fps
        }
    }
    
    androidx.compose.ui.graphics.Canvas(
        modifier = modifier
    ) {
        drawFluid(simulation, renderMode, alpha.value, onTouch)
    }
}

/**
 * Draw fluid simulation
 */
private fun DrawScope.drawFluid(
    simulation: FluidSimulation,
    renderMode: FluidRenderMode,
    alpha: Float,
    onTouch: (Offset) -> Unit
) {
    val gridSize = simulation.gridSize
    val cellWidth = size.width / gridSize
    val cellHeight = size.height / gridSize
    
    when (renderMode) {
        FluidRenderMode.SURFACE -> drawSurfaceFluid(simulation, gridSize, cellWidth, cellHeight, alpha)
        FluidRenderMode.PARTICLES -> drawParticleFluid(simulation, gridSize, cellWidth, cellHeight, alpha)
        FluidRenderMode.WIREFRAME -> drawWireframeFluid(simulation, gridSize, cellWidth, cellHeight, alpha)
        FluidRenderMode.HEATMAP -> drawHeatmapFluid(simulation, gridSize, cellWidth, cellHeight, alpha)
    }
    
    // Touch interaction visualization
    drawTouchRipples(simulation, onTouch)
}

/**
 * Surface rendering with smooth gradients
 */
private fun DrawScope.drawSurfaceFluid(
    simulation: FluidSimulation,
    gridSize: Int,
    cellWidth: Float,
    cellHeight: Float,
    alpha: Float
) {
    val path = Path()
    
    // Draw fluid surface
    for (i in 1..gridSize) {
        for (j in 1..gridSize) {
            val density = simulation.getDensity(i, j)
            
            if (density > 0.05f) {
                val x = (i - 1) * cellWidth
                val y = (j - 1) * cellHeight
                val color = simulation.getFluidColor(i, j).copy(alpha = alpha * density)
                
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(cellWidth + 1, cellHeight + 1)
                )
            }
        }
    }
    
    // Draw surface highlights
    for (i in 1..gridSize) {
        var surfaceY = -1
        for (j in gridSize downTo 1) {
            if (simulation.getDensity(i, j) > 0.1f && surfaceY == -1) {
                surfaceY = j
            }
        }
        
        if (surfaceY > 0) {
            val x = (i - 1) * cellWidth + cellWidth / 2
            val y = (surfaceY - 1) * cellHeight
            
            // Surface highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.3f * alpha),
                radius = cellWidth * 0.8f,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Particle-based fluid rendering
 */
private fun DrawScope.drawParticleFluid(
    simulation: FluidSimulation,
    gridSize: Int,
    cellWidth: Float,
    cellHeight: Float,
    alpha: Float
) {
    for (i in 1..gridSize) {
        for (j in 1..gridSize) {
            val density = simulation.getDensity(i, j)
            
            if (density > 0.1f) {
                val x = (i - 1) * cellWidth + cellWidth / 2
                val y = (j - 1) * cellHeight + cellHeight / 2
                val velocity = simulation.getVelocity(i, j)
                val speed = sqrt(velocity.x * velocity.x + velocity.y * velocity.y)
                
                val particleSize = (cellWidth * 0.3f * density * (1f + speed * 0.5f)).dp.toPx()
                val color = simulation.getFluidColor(i, j).copy(alpha = alpha)
                
                drawCircle(
                    color = color,
                    radius = particleSize,
                    center = Offset(x, y)
                )
            }
        }
    }
}

/**
 * Wireframe visualization
 */
private fun DrawScope.drawWireframeFluid(
    simulation: FluidSimulation,
    gridSize: Int,
    cellWidth: Float,
    cellHeight: Float,
    alpha: Float
) {
    for (i in 1..gridSize) {
        for (j in 1..gridSize) {
            val density = simulation.getDensity(i, j)
            val velocity = simulation.getVelocity(i, j)
            
            val x = (i - 1) * cellWidth + cellWidth / 2
            val y = (j - 1) * cellHeight + cellHeight / 2
            
            // Draw grid cell
            drawRect(
                color = Color.White.copy(alpha = 0.1f * alpha),
                topLeft = Offset((i - 1) * cellWidth, (j - 1) * cellHeight),
                size = androidx.compose.ui.geometry.Size(cellWidth, cellHeight),
                style = Stroke(width = 1.dp.toPx())
            )
            
            // Draw velocity vector
            if (density > 0.1f) {
                val vectorLength = 20f * density
                drawLine(
                    color = simulation.fluidColor.copy(alpha = alpha * density),
                    start = Offset(x, y),
                    end = Offset(x + velocity.x * vectorLength, y + velocity.y * vectorLength),
                    strokeWidth = 2.dp.toPx() * density
                )
            }
        }
    }
}

/**
 * Heatmap visualization
 */
private fun DrawScope.drawHeatmapFluid(
    simulation: FluidSimulation,
    gridSize: Int,
    cellWidth: Float,
    cellHeight: Float,
    alpha: Float
) {
    for (i in 1..gridSize) {
        for (j in 1..gridSize) {
            val density = simulation.getDensity(i, j)
            
            if (density > 0.01f) {
                val x = (i - 1) * cellWidth
                val y = (j - 1) * cellHeight
                
                // Heatmap color: blue -> green -> yellow -> red
                val heatmapColor = when {
                    density < 0.25f -> Color.Blue.copy(alpha = alpha * density * 4)
                    density < 0.5f -> Color.Green.copy(alpha = alpha * (density - 0.25f) * 4)
                    density < 0.75f -> Color.Yellow.copy(alpha = alpha * (density - 0.5f) * 4)
                    else -> Color.Red.copy(alpha = alpha * (density - 0.75f) * 4)
                }
                
                drawRect(
                    color = heatmapColor,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(cellWidth + 1, cellHeight + 1)
                )
            }
        }
    }
}

/**
 * Draw touch ripples
 */
private fun DrawScope.drawTouchRipples(
    simulation: FluidSimulation,
    onTouch: (Offset) -> Unit
) {
    // This would visualize touch interactions
    // Actual touch handling is done through pointerInput in the composable
}

/**
 * Fluid rendering modes
 */
enum class FluidRenderMode {
    SURFACE,      // Smooth surface rendering
    PARTICLES,    // Particle-based rendering
    WIREFRAME,    // Grid and velocity visualization
    HEATMAP       // Density heatmap
}

/**
 * Get composable fluid effect overlay
 */
@Composable
fun FluidEffectOverlay(
    modifier: Modifier = Modifier,
    config: FluidSimulationConfig,
    renderMode: FluidRenderMode = FluidRenderMode.SURFACE,
    intensity: Float = 1.0f
) {
    val simulation = remember {
        FluidSimulation(
            gridSize = 64,
            viscosity = config.viscosity,
            diffusion = config.diffusion,
            dt = 0.1f * intensity
        ).apply {
            surfaceTension = config.surfaceTension
            gravity = config.gravity
            fluidColor = config.fluidColor
            secondaryColor = config.secondaryColor
        }
    }
    
    FluidSimulationRenderer(
        modifier = modifier,
        simulation = simulation,
        renderMode = renderMode
    ) { offset ->
        // Add touch interaction
        simulation.addTouchPoint(
            offset.x / 64f,
            offset.y / 64f,
            10f * intensity
        )
    }
}
