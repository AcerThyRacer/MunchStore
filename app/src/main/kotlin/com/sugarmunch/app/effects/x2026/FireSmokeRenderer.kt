package com.sugarmunch.app.effects.x2026

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Composable for rendering fire and smoke simulation
 */
@Composable
fun FireSmokeRenderer(
    modifier: Modifier = Modifier,
    simulation: FireSmokeSimulation,
    renderMode: FireRenderMode = FireRenderMode.VOLUMETRIC,
    onTouch: (Offset) -> Unit = {}
) {
    val alpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            alpha.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(1000))
        }
    }
    
    // Auto-step simulation
    LaunchedEffect(simulation) {
        while (true) {
            simulation.step(0.016f)
            kotlinx.coroutines.delay(16) // ~60fps
        }
    }
    
    androidx.compose.ui.graphics.Canvas(
        modifier = modifier
    ) {
        drawFireSmoke(simulation, renderMode, alpha.value, onTouch)
    }
}

/**
 * Draw fire and smoke
 */
private fun DrawScope.drawFireSmoke(
    simulation: FireSmokeSimulation,
    renderMode: FireRenderMode,
    alpha: Float,
    onTouch: (Offset) -> Unit
) {
    val particles = simulation.getParticles()
    
    when (renderMode) {
        FireRenderMode.VOLUMETRIC -> drawVolumetricFire(particles, alpha)
        FireRenderMode.PARTICLE -> drawParticleFire(particles, alpha)
        FireRenderMode.STYLIZED -> drawStylizedFire(particles, alpha)
        FireRenderMode.REALISTIC -> drawRealisticFire(particles, alpha)
    }
}

/**
 * Volumetric fire rendering with soft gradients
 */
private fun DrawScope.drawVolumetricFire(
    particles: List<FireParticle>,
    alpha: Float
) {
    // Sort by y position (back to front)
    val sortedParticles = particles.sortedBy { it.y }
    
    sortedParticles.forEach { particle ->
        // Multiple layers for volumetric effect
        repeat(3) { layer ->
            val layerAlpha = alpha * particle.color.alpha / 255f * (0.3f - layer * 0.1f)
            val layerSize = particle.size * (1f + layer * 0.5f)
            
            drawCircle(
                color = particle.color.copy(alpha = layerAlpha),
                radius = layerSize.dp.toPx(),
                center = Offset(particle.x, particle.y),
                blendMode = BlendMode.Screen
            )
        }
    }
    
    // Add glow effect
    if (particles.isNotEmpty()) {
        val avgX = particles.map { it.x }.average().toFloat()
        val avgY = particles.map { it.y }.average().toFloat()
        
        drawCircle(
            color = Color(0xFFFFA500).copy(alpha = alpha * 0.2f),
            radius = 200.dp.toPx(),
            center = Offset(avgX, avgY),
            blendMode = BlendMode.Screen
        )
    }
}

/**
 * Particle-based fire rendering
 */
private fun DrawScope.drawParticleFire(
    particles: List<FireParticle>,
    alpha: Float
) {
    particles.forEach { particle ->
        val particleAlpha = alpha * particle.color.alpha / 255f * particle.fuel
        
        when (particle.particleType) {
            ParticleType.FIRE -> {
                // Draw fire particle as teardrop shape
                val path = Path().apply {
                    moveTo(particle.x, particle.y - particle.size)
                    quadraticBezierTo(
                        particle.x + particle.size * 0.5f,
                        particle.y,
                        particle.x,
                        particle.y + particle.size
                    )
                    quadraticBezierTo(
                        particle.x - particle.size * 0.5f,
                        particle.y,
                        particle.x,
                        particle.y - particle.size
                    )
                    close()
                }
                
                drawPath(
                    path = path,
                    color = particle.color.copy(alpha = particleAlpha),
                    blendMode = BlendMode.Screen
                )
            }
            ParticleType.SMOKE -> {
                // Draw smoke as soft circle
                drawCircle(
                    color = particle.color.copy(alpha = particleAlpha * 0.5f),
                    radius = particle.size * 1.5f,
                    center = Offset(particle.x, particle.y),
                    blendMode = BlendMode.SrcOver
                )
            }
            ParticleType.EMBER -> {
                // Draw ember as small bright circle
                drawCircle(
                    color = particle.color.copy(alpha = particleAlpha),
                    radius = particle.size * 0.3f,
                    center = Offset(particle.x, particle.y),
                    blendMode = BlendMode.Screen
                )
            }
        }
    }
}

/**
 * Stylized fire rendering
 */
private fun DrawScope.drawStylizedFire(
    particles: List<FireParticle>,
    alpha: Float
) {
    // Group particles by temperature bands
    val hotParticles = particles.filter { it.temperature > 1000f }
    val mediumParticles = particles.filter { it.temperature in 600f..1000f }
    val coolParticles = particles.filter { it.temperature < 600f }
    
    // Draw each band with different style
    hotParticles.forEach { particle ->
        drawCircle(
            color = Color(0xFFFFFFFF).copy(alpha = alpha * 0.8f),
            radius = particle.size * 0.5f,
            center = Offset(particle.x, particle.y)
        )
    }
    
    mediumParticles.forEach { particle ->
        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = alpha * 0.6f),
            radius = particle.size,
            center = Offset(particle.x, particle.y)
        )
    }
    
    coolParticles.forEach { particle ->
        drawCircle(
            color = Color(0xFFFF4500).copy(alpha = alpha * 0.4f),
            radius = particle.size * 1.2f,
            center = Offset(particle.x, particle.y)
        )
    }
}

/**
 * Realistic fire rendering with turbulence
 */
private fun DrawScope.drawRealisticFire(
    particles: List<FireParticle>,
    alpha: Float
) {
    // Sort by y for proper blending
    val sortedParticles = particles.sortedByDescending { it.y }
    
    sortedParticles.forEach { particle ->
        val turbulence = (System.currentTimeMillis() / 100f).toFloat()
        val offsetX = kotlin.math.sin(turbulence + particle.x * 0.01f) * 5f
        
        // Draw flame shape with turbulence
        val path = Path().apply {
            moveTo(particle.x + offsetX, particle.y - particle.size * 2f)
            
            // Left side of flame
            quadraticBezierTo(
                particle.x + offsetX - particle.size,
                particle.y - particle.size * 0.5f,
                particle.x + offsetX - particle.size * 0.8f,
                particle.y + particle.size * 0.5f
            )
            
            // Bottom of flame
            quadraticBezierTo(
                particle.x + offsetX,
                particle.y + particle.size * 0.8f,
                particle.x + offsetX + particle.size * 0.8f,
                particle.y + particle.size * 0.5f
            )
            
            // Right side of flame
            quadraticBezierTo(
                particle.x + offsetX + particle.size,
                particle.y - particle.size * 0.5f,
                particle.x + offsetX,
                particle.y - particle.size * 2f
            )
            
            close()
        }
        
        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(
                    particle.color.copy(alpha = alpha * particle.fuel),
                    particle.color.copy(alpha = alpha * particle.fuel * 0.5f),
                    Color.Transparent
                ),
                center = Offset(particle.x + offsetX, particle.y),
                radius = particle.size * 2f
            ),
            blendMode = BlendMode.Screen
        )
    }
}

/**
 * Fire rendering modes
 */
enum class FireRenderMode {
    VOLUMETRIC,  // Soft volumetric glow
    PARTICLE,    // Individual particle rendering
    STYLIZED,    // Artistic temperature bands
    REALISTIC    // Realistic flame shapes
}

/**
 * Get composable fire effect overlay
 */
@Composable
fun FireEffectOverlay(
    modifier: Modifier = Modifier,
    preset: FireSmokePreset = FireSmokePresets.campfirePreset(),
    renderMode: FireRenderMode = FireRenderMode.VOLUMETRIC,
    intensity: Float = 1.0f
) {
    val simulation = remember {
        FireSmokeSimulation(
            maxParticles = (500 * intensity).toInt(),
            emissionRate = (preset.emissionRate * intensity).toInt()
        ).apply {
            setPreset(preset)
        }
    }
    
    FireSmokeRenderer(
        modifier = modifier,
        simulation = simulation,
        renderMode = renderMode
    ) { offset ->
        simulation.setEmitterPosition(
            offset.x / 1000f,
            offset.y / 1000f
        )
    }
}

/**
 * Get composable smoke effect overlay
 */
@Composable
fun SmokeEffectOverlay(
    modifier: Modifier = Modifier,
    smokeAmount: Float = 0.8f,
    intensity: Float = 1.0f
) {
    val simulation = remember {
        FireSmokeSimulation(
            maxParticles = (300 * intensity).toInt(),
            emissionRate = (15 * intensity).toInt()
        ).apply {
            fireTemperature = 600f
            smokeProduction = smokeAmount
            fuelConsumption = 0.02f
            buoyancy = 8f
        }
    }
    
    FireSmokeRenderer(
        modifier = modifier,
        simulation = simulation,
        renderMode = FireRenderMode.VOLUMETRIC
    ) { offset ->
        simulation.setEmitterPosition(
            offset.x / 1000f,
            offset.y / 1000f
        )
    }
}
