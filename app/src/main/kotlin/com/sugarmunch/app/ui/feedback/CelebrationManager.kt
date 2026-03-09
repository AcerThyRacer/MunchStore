package com.sugarmunch.app.ui.feedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * EXTREME Celebration Manager for SugarMunch
 * Confetti, fireworks, rainbow bursts, and achievement effects
 */

/**
 * Celebration type
 */
enum class CelebrationType {
    CONFETTI_CANNON,
    FIREWORKS,
    RAINBOW_BURST,
    ACHIEVEMENT_BADGE,
    SPARKLE_SHOWER,
    BALLOON_RELEASE,
    STAR_EXPLOSION,
    SUGAR_RUSH_CELEBRATION
}

/**
 * Celebration configuration
 */
data class CelebrationConfig(
    val type: CelebrationType = CelebrationType.CONFETTI_CANNON,
    val intensity: Float = 1.0f,
    val duration: Int = 3000,
    val particleCount: Int = 100,
    val colors: List<Color> = listOf(
        SugarDimens.Brand.hotPink,
        SugarDimens.Brand.mint,
        SugarDimens.Brand.yellow,
        SugarDimens.Brand.candyOrange,
        SugarDimens.Brand.bubblegumBlue
    )
)

/**
 * Particle state for celebrations
 */
data class CelebrationParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val alpha: Float = 1f,
    val rotation: Float = 0f,
    val rotationSpeed: Float = 0f,
    val shape: ParticleShape = ParticleShape.CIRCLE
)

enum class ParticleShape {
    CIRCLE,
    SQUARE,
    TRIANGLE,
    STAR
}

/**
 * Main celebration manager composable
 */
@Composable
fun CelebrationManager(
    triggered: Boolean,
    config: CelebrationConfig = CelebrationConfig(),
    onFinished: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val isCelebrating = remember { mutableStateOf(false) }
    val particles = remember { mutableStateListOf<CelebrationParticle>() }
    val celebrationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(triggered) {
        if (triggered && !isCelebrating.value) {
            isCelebrating.value = true
            celebrationProgress.snapTo(0f)
            
            // Generate particles based on type
            particles.clear()
            when (config.type) {
                CelebrationType.CONFETTI_CANNON -> generateConfetti(particles, config)
                CelebrationType.FIREWORKS -> generateFireworks(particles, config)
                CelebrationType.RAINBOW_BURST -> generateRainbowBurst(particles, config)
                CelebrationType.SPARKLE_SHOWER -> generateSparkleShower(particles, config)
                CelebrationType.STAR_EXPLOSION -> generateStarExplosion(particles, config)
                CelebrationType.SUGAR_RUSH_CELEBRATION -> generateSugarRush(particles, config)
                else -> {}
            }
            
            // Animate celebration
            celebrationProgress.animateTo(
                1f,
                animationSpec = tween(config.duration)
            )
            
            isCelebrating.value = false
            onFinished()
        }
    }
    
    if (isCelebrating.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { particle ->
                    drawCelebrationParticle(particle)
                }
            }
        }
    }
}

/**
 * Generate confetti particles
 */
private fun generateConfetti(
    particles: MutableList<CelebrationParticle>,
    config: CelebrationConfig
) {
    val centerX = 1000f // Will be scaled to actual size
    val centerY = 0f
    
    repeat(config.particleCount) {
        val angle = (Random.nextFloat() * 180 - 90) * Math.PI / 180
        val speed = Random.nextFloat() * 10 * config.intensity + 5f
        
        particles.add(
            CelebrationParticle(
                x = centerX,
                y = centerY,
                vx = cos(angle).toFloat() * speed,
                vy = sin(angle).toFloat() * speed + 5f, // Gravity
                size = Random.nextFloat() * 8 + 4,
                color = config.colors.random(),
                rotation = Random.nextFloat() * 360,
                rotationSpeed = Random.nextFloat() * 10 - 5,
                shape = ParticleShape.entries.random()
            )
        )
    }
}

/**
 * Generate fireworks particles
 */
private fun generateFireworks(
    particles: MutableList<CelebrationParticle>,
    config: CelebrationConfig
) {
    val fireworkCount = (5 * config.intensity).toInt()
    
    repeat(fireworkCount) { i ->
        delay(i * 300L)
        
        val centerX = Random.nextFloat() * 800 + 100
        val centerY = Random.nextFloat() * 400 + 100
        val fireworkColor = config.colors.random()
        
        repeat(config.particleCount / fireworkCount) {
            val angle = (it * 360f / (config.particleCount / fireworkCount)) * Math.PI / 180
            val speed = Random.nextFloat() * 8 * config.intensity + 3f
            
            particles.add(
                CelebrationParticle(
                    x = centerX,
                    y = centerY,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    size = Random.nextFloat() * 6 + 3,
                    color = fireworkColor,
                    alpha = 1f
                )
            )
        }
    }
}

/**
 * Generate rainbow burst
 */
private fun generateRainbowBurst(
    particles: MutableList<CelebrationParticle>,
    config: CelebrationConfig
) {
    val centerX = 500f
    val centerY = 500f
    
    repeat(config.particleCount * 2) { i ->
        val angle = (i * 360f / (config.particleCount * 2)) * Math.PI / 180
        val distance = i.toFloat() / config.particleCount * 300 * config.intensity
        val colorIndex = (i * 7) % config.colors.size
        
        particles.add(
            CelebrationParticle(
                x = centerX + cos(angle).toFloat() * distance,
                y = centerY + sin(angle).toFloat() * distance,
                vx = cos(angle).toFloat() * 2f,
                vy = sin(angle).toFloat() * 2f,
                size = 10f,
                color = config.colors[colorIndex],
                alpha = 1f - (distance / 300f)
            )
        )
    }
}

/**
 * Generate sparkle shower
 */
private fun generateSparkleShower(
    particles: MutableList<CelebrationParticle>,
    config: CelebrationConfig
) {
    repeat(config.particleCount) {
        particles.add(
            CelebrationParticle(
                x = Random.nextFloat() * 1000,
                y = Random.nextFloat() * -500,
                vx = Random.nextFloat() * 2 - 1,
                vy = Random.nextFloat() * 5 + 3,
                size = Random.nextFloat() * 4 + 2,
                color = Color.White,
                alpha = Random.nextFloat() * 0.5f + 0.5f,
                shape = ParticleShape.STAR
            )
        )
    }
}

/**
 * Generate star explosion
 */
private fun generateStarExplosion(
    particles: MutableList<CelebrationParticle>,
    config: CelebrationConfig
) {
    val centerX = 500f
    val centerY = 500f
    
    repeat(config.particleCount) {
        val angle = Random.nextFloat() * 360 * Math.PI / 180
        val speed = Random.nextFloat() * 15 * config.intensity + 5f
        
        particles.add(
            CelebrationParticle(
                x = centerX,
                y = centerY,
                vx = cos(angle).toFloat() * speed,
                vy = sin(angle).toFloat() * speed,
                size = Random.nextFloat() * 12 + 6,
                color = config.colors.random(),
                shape = ParticleShape.STAR
            )
        )
    }
}

/**
 * Generate Sugar Rush celebration
 */
private fun generateSugarRush(
    particles: MutableList<CelebrationParticle>,
    config: CelebrationConfig
) {
    // Combine multiple effects for maximum impact
    generateConfetti(particles, config.copy(particleCount = config.particleCount / 2))
    generateSparkleShower(particles, config.copy(particleCount = config.particleCount / 2))
}

/**
 * Draw a celebration particle
 */
private fun DrawScope.drawCelebrationParticle(particle: CelebrationParticle) {
    when (particle.shape) {
        ParticleShape.CIRCLE -> {
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }
        ParticleShape.SQUARE -> {
            drawRect(
                color = particle.color.copy(alpha = particle.alpha),
                size = androidx.compose.ui.geometry.Size(particle.size * 2, particle.size * 2),
                topLeft = Offset(particle.x - particle.size, particle.y - particle.size)
            )
        }
        ParticleShape.TRIANGLE -> {
            // Draw triangle
        }
        ParticleShape.STAR -> {
            // Draw star shape
            drawStar(
                color = particle.color.copy(alpha = particle.alpha),
                center = Offset(particle.x, particle.y),
                outerRadius = particle.size,
                innerRadius = particle.size / 2,
                rotation = particle.rotation
            )
        }
    }
}

/**
 * Draw star shape
 */
private fun DrawScope.drawStar(
    color: Color,
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    rotation: Float,
    points: Int = 5
) {
    val path = androidx.compose.ui.graphics.Path()
    val angleStep = 180f / points
    
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (i * angleStep + rotation) * Math.PI / 180
        val x = center.x + cos(angle).toFloat() * radius
        val y = center.y + sin(angle).toFloat() * radius
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    path.close()
    drawPath(path, color)
}

/**
 * Quick celebration triggers
 */
@Composable
fun QuickConfetti(triggered: Boolean) {
    CelebrationManager(
        triggered = triggered,
        config = CelebrationConfig(
            type = CelebrationType.CONFETTI_CANNON,
            intensity = 1.0f,
            particleCount = 100
        )
    )
}

@Composable
fun QuickFireworks(triggered: Boolean) {
    CelebrationManager(
        triggered = triggered,
        config = CelebrationConfig(
            type = CelebrationType.FIREWORKS,
            intensity = 1.5f,
            particleCount = 200,
            duration = 4000
        )
    )
}

@Composable
fun QuickSugarRush(triggered: Boolean) {
    CelebrationManager(
        triggered = triggered,
        config = CelebrationConfig(
            type = CelebrationType.SUGAR_RUSH_CELEBRATION,
            intensity = 2.0f,
            particleCount = 300,
            duration = 5000
        )
    )
}
