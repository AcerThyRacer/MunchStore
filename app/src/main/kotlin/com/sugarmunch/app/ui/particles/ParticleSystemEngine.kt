package com.sugarmunch.app.ui.particles

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.*
import kotlin.random.Random

enum class EmitterShape {
    POINT, LINE, CIRCLE, RECTANGLE, SCREEN_TOP, SCREEN_BOTTOM, SCREEN_EDGES
}

enum class ParticleType {
    CIRCLE, STAR, HEART, CANDY, SNOWFLAKE, BUBBLE, SPARKLE, LIGHTNING,
    CONFETTI_RECT, CONFETTI_CIRCLE, GUMMY_BEAR, LOLLIPOP
}

data class ParticleSystemConfig(
    val emitterShape: EmitterShape = EmitterShape.SCREEN_TOP,
    val emitterPosition: Offset = Offset.Zero,
    val emitterSize: Size = Size.Zero,
    val particleTypes: Set<ParticleType> = setOf(ParticleType.CIRCLE),
    val colors: List<Color> = listOf(Color(0xFFFFB6C1), Color(0xFF98FF98), Color(0xFFB5DEFF)),
    val spawnRate: Float = 10f,
    val maxParticles: Int = 200,
    val lifetime: ClosedFloatingPointRange<Float> = 2f..4f,
    val initialSpeed: ClosedFloatingPointRange<Float> = 50f..150f,
    val initialAngle: ClosedFloatingPointRange<Float> = 0f..360f,
    val size: ClosedFloatingPointRange<Float> = 4f..12f,
    val gravity: Float = 98f,
    val wind: Float = 0f,
    val turbulence: Float = 0f,
    val fadeIn: Float = 0.1f,
    val fadeOut: Float = 0.3f,
    val sizeOverLife: List<Float> = listOf(1f, 1f),
    val rotationSpeed: ClosedFloatingPointRange<Float> = -180f..180f,
    val collision: Boolean = false,
    val bounciness: Float = 0.6f
)

private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var alpha: Float,
    var color: Color,
    var type: ParticleType,
    var age: Float,
    var lifetime: Float,
    var alive: Boolean = true
)

class ParticleEngine(private var config: ParticleSystemConfig) {

    private val particles = mutableListOf<Particle>()
    private var spawnAccumulator = 0f
    private val random = Random(System.nanoTime())

    fun update(deltaTime: Float, canvasWidth: Float, canvasHeight: Float) {
        val dt = deltaTime.coerceIn(0f, 0.05f)

        // Spawn new particles
        spawnAccumulator += config.spawnRate * dt
        while (spawnAccumulator >= 1f && particles.size < config.maxParticles) {
            particles.add(spawnParticle(canvasWidth, canvasHeight))
            spawnAccumulator -= 1f
        }
        if (particles.size >= config.maxParticles) {
            spawnAccumulator = 0f
        }

        // Update existing particles
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            updateParticle(p, dt, canvasWidth, canvasHeight)
            if (!p.alive) iterator.remove()
        }

        // Handle collisions
        if (config.collision) {
            resolveCollisions()
        }
    }

    fun draw(drawScope: DrawScope) {
        for (p in particles) {
            if (p.alive && p.alpha > 0f) {
                drawParticle(drawScope, p)
            }
        }
    }

    fun reset() {
        particles.clear()
        spawnAccumulator = 0f
    }

    fun updateConfig(newConfig: ParticleSystemConfig) {
        config = newConfig
    }

    // --- Spawning ---

    private fun spawnParticle(canvasWidth: Float, canvasHeight: Float): Particle {
        val pos = computeSpawnPosition(canvasWidth, canvasHeight)
        val angleDeg = randomInRange(config.initialAngle)
        val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
        val speed = randomInRange(config.initialSpeed)

        return Particle(
            x = pos.x,
            y = pos.y,
            vx = cos(angleRad) * speed,
            vy = sin(angleRad) * speed,
            size = randomInRange(config.size),
            rotation = random.nextFloat() * 360f,
            rotationSpeed = randomInRange(config.rotationSpeed),
            alpha = 0f,
            color = config.colors[random.nextInt(config.colors.size)],
            type = config.particleTypes.random(random),
            age = 0f,
            lifetime = randomInRange(config.lifetime)
        )
    }

    private fun computeSpawnPosition(w: Float, h: Float): Offset {
        val pos = config.emitterPosition
        val sz = config.emitterSize
        return when (config.emitterShape) {
            EmitterShape.POINT -> pos
            EmitterShape.LINE -> Offset(
                pos.x + random.nextFloat() * sz.width,
                pos.y
            )
            EmitterShape.CIRCLE -> {
                val angle = random.nextFloat() * TWO_PI
                val radius = sqrt(random.nextFloat()) * (sz.width / 2f)
                Offset(pos.x + cos(angle) * radius, pos.y + sin(angle) * radius)
            }
            EmitterShape.RECTANGLE -> Offset(
                pos.x + random.nextFloat() * sz.width,
                pos.y + random.nextFloat() * sz.height
            )
            EmitterShape.SCREEN_TOP -> Offset(
                random.nextFloat() * w,
                -20f
            )
            EmitterShape.SCREEN_BOTTOM -> Offset(
                random.nextFloat() * w,
                h + 20f
            )
            EmitterShape.SCREEN_EDGES -> {
                when (random.nextInt(4)) {
                    0 -> Offset(random.nextFloat() * w, -20f)
                    1 -> Offset(random.nextFloat() * w, h + 20f)
                    2 -> Offset(-20f, random.nextFloat() * h)
                    else -> Offset(w + 20f, random.nextFloat() * h)
                }
            }
        }
    }

    // --- Updating ---

    private fun updateParticle(p: Particle, dt: Float, w: Float, h: Float) {
        p.age += dt
        if (p.age >= p.lifetime) {
            p.alive = false
            return
        }

        val lifeFraction = p.age / p.lifetime

        // Alpha: fade in / fade out
        p.alpha = when {
            lifeFraction < config.fadeIn -> (lifeFraction / config.fadeIn).coerceIn(0f, 1f)
            lifeFraction > (1f - config.fadeOut) -> ((1f - lifeFraction) / config.fadeOut).coerceIn(0f, 1f)
            else -> 1f
        }

        // Size over life interpolation
        val sizeMultiplier = interpolateCurve(config.sizeOverLife, lifeFraction)
        // We store original size in the particle; apply multiplier during draw

        // Physics
        p.vy += config.gravity * dt
        p.vx += config.wind * dt

        if (config.turbulence > 0f) {
            p.vx += (random.nextFloat() - 0.5f) * 2f * config.turbulence * dt
            p.vy += (random.nextFloat() - 0.5f) * 2f * config.turbulence * dt
        }

        p.x += p.vx * dt
        p.y += p.vy * dt

        p.rotation += p.rotationSpeed * dt

        // Floor/wall collision
        if (config.collision) {
            val effectiveSize = p.size * sizeMultiplier
            if (p.y + effectiveSize > h) {
                p.y = h - effectiveSize
                p.vy = -abs(p.vy) * config.bounciness
            }
            if (p.y - effectiveSize < 0f) {
                p.y = effectiveSize
                p.vy = abs(p.vy) * config.bounciness
            }
            if (p.x + effectiveSize > w) {
                p.x = w - effectiveSize
                p.vx = -abs(p.vx) * config.bounciness
            }
            if (p.x - effectiveSize < 0f) {
                p.x = effectiveSize
                p.vx = abs(p.vx) * config.bounciness
            }
        }

        // Kill particles far off screen
        val margin = 100f
        if (p.x < -margin || p.x > w + margin || p.y < -margin || p.y > h + margin) {
            if (!config.collision) {
                p.alive = false
            }
        }
    }

    private fun resolveCollisions() {
        for (i in particles.indices) {
            val a = particles[i]
            if (!a.alive) continue
            for (j in i + 1 until particles.size) {
                val b = particles[j]
                if (!b.alive) continue
                val dx = b.x - a.x
                val dy = b.y - a.y
                val distSq = dx * dx + dy * dy
                val minDist = a.size + b.size
                if (distSq < minDist * minDist && distSq > 0.01f) {
                    val dist = sqrt(distSq)
                    val nx = dx / dist
                    val ny = dy / dist
                    val overlap = minDist - dist
                    a.x -= nx * overlap * 0.5f
                    a.y -= ny * overlap * 0.5f
                    b.x += nx * overlap * 0.5f
                    b.y += ny * overlap * 0.5f

                    val relVx = b.vx - a.vx
                    val relVy = b.vy - a.vy
                    val relDot = relVx * nx + relVy * ny
                    if (relDot < 0) {
                        val impulse = relDot * config.bounciness
                        a.vx += impulse * nx
                        a.vy += impulse * ny
                        b.vx -= impulse * nx
                        b.vy -= impulse * ny
                    }
                }
            }
        }
    }

    // --- Drawing ---

    private fun drawParticle(drawScope: DrawScope, p: Particle) {
        val lifeFraction = p.age / p.lifetime
        val sizeMultiplier = interpolateCurve(config.sizeOverLife, lifeFraction)
        val effectiveSize = p.size * sizeMultiplier
        val drawColor = p.color.copy(alpha = p.alpha)

        drawScope.translate(left = p.x, top = p.y) {
            rotate(degrees = p.rotation) {
                when (p.type) {
                    ParticleType.CIRCLE -> drawCircleParticle(this, effectiveSize, drawColor)
                    ParticleType.STAR -> drawStarParticle(this, effectiveSize, drawColor)
                    ParticleType.HEART -> drawHeartParticle(this, effectiveSize, drawColor)
                    ParticleType.CANDY -> drawCandyParticle(this, effectiveSize, drawColor)
                    ParticleType.SNOWFLAKE -> drawSnowflakeParticle(this, effectiveSize, drawColor)
                    ParticleType.BUBBLE -> drawBubbleParticle(this, effectiveSize, drawColor)
                    ParticleType.SPARKLE -> drawSparkleParticle(this, effectiveSize, drawColor)
                    ParticleType.LIGHTNING -> drawLightningParticle(this, effectiveSize, drawColor)
                    ParticleType.CONFETTI_RECT -> drawConfettiRectParticle(this, effectiveSize, drawColor)
                    ParticleType.CONFETTI_CIRCLE -> drawConfettiCircleParticle(this, effectiveSize, drawColor)
                    ParticleType.GUMMY_BEAR -> drawGummyBearParticle(this, effectiveSize, drawColor)
                    ParticleType.LOLLIPOP -> drawLollipopParticle(this, effectiveSize, drawColor)
                }
            }
        }
    }

    private fun drawCircleParticle(scope: DrawScope, size: Float, color: Color) {
        scope.drawCircle(color = color, radius = size, center = Offset.Zero)
    }

    private fun drawStarParticle(scope: DrawScope, size: Float, color: Color) {
        val path = Path()
        val outerRadius = size
        val innerRadius = size * 0.4f
        val points = 5
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = (PI / 2.0) + (i * PI / points)
            val px = (cos(angle) * radius).toFloat()
            val py = (-sin(angle) * radius).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        scope.drawPath(path = path, color = color, style = Fill)
    }

    private fun drawHeartParticle(scope: DrawScope, size: Float, color: Color) {
        val path = Path().apply {
            val s = size
            moveTo(0f, s * 0.3f)
            cubicTo(-s, -s * 0.5f, -s * 0.3f, -s, 0f, -s * 0.4f)
            cubicTo(s * 0.3f, -s, s, -s * 0.5f, 0f, s * 0.3f)
        }
        scope.drawPath(path = path, color = color, style = Fill)
    }

    private fun drawCandyParticle(scope: DrawScope, size: Float, color: Color) {
        // Main candy body
        scope.drawCircle(color = color, radius = size * 0.6f, center = Offset.Zero)
        // Left wrapper
        val wrapperColor = color.copy(alpha = color.alpha * 0.7f)
        scope.drawRect(
            color = wrapperColor,
            topLeft = Offset(-size * 1.1f, -size * 0.15f),
            size = Size(size * 0.5f, size * 0.3f)
        )
        // Right wrapper
        scope.drawRect(
            color = wrapperColor,
            topLeft = Offset(size * 0.6f, -size * 0.15f),
            size = Size(size * 0.5f, size * 0.3f)
        )
    }

    private fun drawSnowflakeParticle(scope: DrawScope, size: Float, color: Color) {
        val stroke = Stroke(width = size * 0.12f, cap = StrokeCap.Round)
        for (i in 0 until 6) {
            val angle = (i * 60.0).toFloat()
            val rad = Math.toRadians(angle.toDouble())
            val endX = (cos(rad) * size).toFloat()
            val endY = (sin(rad) * size).toFloat()
            // Main arm
            scope.drawLine(color = color, start = Offset.Zero, end = Offset(endX, endY), strokeWidth = stroke.width)
            // Small branches at 2/3 of the arm
            val branchStart = Offset(endX * 0.65f, endY * 0.65f)
            val branchLen = size * 0.3f
            val branchAngle1 = Math.toRadians((angle + 45.0).toDouble())
            val branchAngle2 = Math.toRadians((angle - 45.0).toDouble())
            scope.drawLine(
                color = color, start = branchStart,
                end = Offset(branchStart.x + (cos(branchAngle1) * branchLen).toFloat(), branchStart.y + (sin(branchAngle1) * branchLen).toFloat()),
                strokeWidth = stroke.width * 0.7f
            )
            scope.drawLine(
                color = color, start = branchStart,
                end = Offset(branchStart.x + (cos(branchAngle2) * branchLen).toFloat(), branchStart.y + (sin(branchAngle2) * branchLen).toFloat()),
                strokeWidth = stroke.width * 0.7f
            )
        }
    }

    private fun drawBubbleParticle(scope: DrawScope, size: Float, color: Color) {
        // Outer bubble
        scope.drawCircle(
            color = color.copy(alpha = color.alpha * 0.3f),
            radius = size,
            center = Offset.Zero
        )
        // Rim
        scope.drawCircle(
            color = color.copy(alpha = color.alpha * 0.6f),
            radius = size,
            center = Offset.Zero,
            style = Stroke(width = size * 0.08f)
        )
        // Highlight
        scope.drawCircle(
            color = Color.White.copy(alpha = color.alpha * 0.5f),
            radius = size * 0.25f,
            center = Offset(-size * 0.3f, -size * 0.3f)
        )
    }

    private fun drawSparkleParticle(scope: DrawScope, size: Float, color: Color) {
        val path = Path()
        val outerRadius = size
        val innerRadius = size * 0.2f
        val points = 4
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = (PI / 2.0) + (i * PI / points)
            val px = (cos(angle) * radius).toFloat()
            val py = (-sin(angle) * radius).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        scope.drawPath(path = path, color = color, style = Fill)
    }

    private fun drawLightningParticle(scope: DrawScope, size: Float, color: Color) {
        val path = Path().apply {
            moveTo(0f, -size)
            lineTo(-size * 0.3f, -size * 0.2f)
            lineTo(size * 0.15f, -size * 0.15f)
            lineTo(-size * 0.15f, size * 0.3f)
            lineTo(size * 0.3f, size * 0.15f)
            lineTo(-size * 0.1f, size)
        }
        scope.drawPath(
            path = path,
            color = color,
            style = Stroke(width = size * 0.15f, cap = StrokeCap.Round)
        )
    }

    private fun drawConfettiRectParticle(scope: DrawScope, size: Float, color: Color) {
        scope.drawRect(
            color = color,
            topLeft = Offset(-size * 0.5f, -size * 0.25f),
            size = Size(size, size * 0.5f)
        )
    }

    private fun drawConfettiCircleParticle(scope: DrawScope, size: Float, color: Color) {
        scope.drawCircle(color = color, radius = size * 0.5f, center = Offset.Zero)
    }

    private fun drawGummyBearParticle(scope: DrawScope, size: Float, color: Color) {
        val headRadius = size * 0.35f
        val bodyWidth = size * 0.5f
        val bodyHeight = size * 0.6f
        val earRadius = size * 0.15f

        // Body oval
        scope.drawOval(
            color = color,
            topLeft = Offset(-bodyWidth / 2f, -bodyHeight * 0.15f),
            size = Size(bodyWidth, bodyHeight)
        )
        // Head
        scope.drawCircle(color = color, radius = headRadius, center = Offset(0f, -headRadius * 0.7f))
        // Left ear
        scope.drawCircle(color = color, radius = earRadius, center = Offset(-headRadius * 0.7f, -headRadius * 1.3f))
        // Right ear
        scope.drawCircle(color = color, radius = earRadius, center = Offset(headRadius * 0.7f, -headRadius * 1.3f))
        // Eyes
        val eyeColor = Color.White.copy(alpha = color.alpha * 0.9f)
        scope.drawCircle(color = eyeColor, radius = size * 0.06f, center = Offset(-headRadius * 0.3f, -headRadius * 0.8f))
        scope.drawCircle(color = eyeColor, radius = size * 0.06f, center = Offset(headRadius * 0.3f, -headRadius * 0.8f))
    }

    private fun drawLollipopParticle(scope: DrawScope, size: Float, color: Color) {
        val candyRadius = size * 0.45f
        val stickWidth = size * 0.1f
        val stickHeight = size * 0.8f

        // Stick
        scope.drawRect(
            color = Color(0xFFD2B48C).copy(alpha = color.alpha),
            topLeft = Offset(-stickWidth / 2f, candyRadius * 0.3f),
            size = Size(stickWidth, stickHeight)
        )
        // Candy circle
        scope.drawCircle(color = color, radius = candyRadius, center = Offset(0f, -candyRadius * 0.2f))
        // Swirl highlight
        scope.drawCircle(
            color = Color.White.copy(alpha = color.alpha * 0.35f),
            radius = candyRadius * 0.55f,
            center = Offset(-candyRadius * 0.15f, -candyRadius * 0.35f)
        )
    }

    // --- Utilities ---

    private fun randomInRange(range: ClosedFloatingPointRange<Float>): Float {
        return range.start + random.nextFloat() * (range.endInclusive - range.start)
    }

    private fun interpolateCurve(curve: List<Float>, t: Float): Float {
        if (curve.isEmpty()) return 1f
        if (curve.size == 1) return curve[0]
        val clampedT = t.coerceIn(0f, 1f)
        val scaledIndex = clampedT * (curve.size - 1)
        val lowerIndex = scaledIndex.toInt().coerceIn(0, curve.size - 2)
        val upperIndex = (lowerIndex + 1).coerceIn(0, curve.size - 1)
        val fraction = scaledIndex - lowerIndex
        return curve[lowerIndex] + (curve[upperIndex] - curve[lowerIndex]) * fraction
    }

    companion object {
        private const val TWO_PI = (2.0 * PI).toFloat()
    }
}

/**
 * Composable that renders and animates a particle system on a Canvas.
 * Uses [withFrameNanos] for smooth 60fps updates.
 */
@Composable
fun ParticleSystem(
    config: ParticleSystemConfig,
    modifier: Modifier = Modifier
) {
    val engine = remember { ParticleEngine(config) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(config) {
        engine.updateConfig(config)
    }

    LaunchedEffect(Unit) {
        var lastFrameNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                val dt = if (lastFrameNanos == 0L) {
                    0.016f
                } else {
                    ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                }
                lastFrameNanos = frameNanos
                engine.update(dt, canvasSize.width, canvasSize.height)
            }
        }
    }

    Canvas(modifier = modifier) {
        canvasSize = size
        engine.draw(this)
    }
}
