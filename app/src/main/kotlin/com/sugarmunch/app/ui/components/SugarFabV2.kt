package com.sugarmunch.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

/**
 * Sugar FAB V2 - Candy Dispenser Floating Action Button
 * Interactive gumball machine-style FAB with candy particles and trail effects
 */

// ═════════════════════════════════════════════════════════════════
// MODELS
// ═════════════════════════════════════════════════════════════════

enum class CandyDispenserStyle {
    GUMBALL_MACHINE,    // Classic round glass globe
    JAWBREAKER,         // Large spherical candy
    LOLLIPOP_DISPENSER  // Lollipop-shaped
}

enum class CandyType {
    GUMBALL,            // Round colorful gumballs
    JELLY_BEAN,         // Small bean-shaped candies
    LOLLIPOP,           // Mini lollipops
    CANDY_CANE,         // Mini candy canes
    SOUR_BALL           // Sour spherical candies
}

enum class TrailEffectType {
    SPARKLE,            // Sparkling trail
    CANDY_DUST,         // Colored dust particles
    RAINBOW,            // Rainbow trail
    HEARTS,             // Heart particles
    STARS               // Star particles
}

data class SugarFabConfig(
    val style: CandyDispenserStyle = CandyDispenserStyle.GUMBALL_MACHINE,
    val candyType: CandyType = CandyType.GUMBALL,
    val trailEffect: TrailEffectType = TrailEffectType.SPARKLE,
    val size: Float = 1f,
    val opacity: Float = 1f,
    val shortcutEffects: List<String> = emptyList()
)

data class CandyParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val life: Float
)

data class TrailParticle(
    val x: Float,
    val y: Float,
    val life: Float,
    val color: Color,
    val size: Float
)

// ═════════════════════════════════════════════════════════════════
// MAIN FAB COMPOSABLE
// ═════════════════════════════════════════════════════════════════

@Composable
fun SugarFabV2(
    modifier: Modifier = Modifier,
    config: SugarFabConfig = SugarFabConfig(),
    onEffectSelected: (String) -> Unit = {},
    onFabClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var isExpanded by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var showCandyBurst by remember { mutableStateOf(false) }
    var candyParticles by remember { mutableStateOf(emptyList<CandyParticle>()) }
    var trailParticles by remember { mutableStateOf(emptyList<TrailParticle>()) }
    
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    var offsetX by remember { mutableStateOf((screenWidth - 72.dp).value) }
    var offsetY by remember { mutableStateOf((screenHeight - 200.dp).value) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "fab_rotation")
    val candyRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "candy_rotation"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val fabSize = 56.dp * config.size
    val scale = if (isPressed) 0.9f else 1f
    
    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            delay(300)
                            trailParticles = emptyList()
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        
                        // Add trail particles
                        scope.launch {
                            trailParticles = trailParticles + TrailParticle(
                                x = offsetX + fabSize.toPx() / 2,
                                y = offsetY + fabSize.toPx() / 2,
                                life = 1f,
                                color = getTrailColor(config.trailEffect),
                                size = 4f * config.size
                            )
                            
                            // Fade out trail
                            delay(50)
                            trailParticles = trailParticles.map { it.copy(life = it.life - 0.1f) }
                                .filter { it.life > 0f }
                        }
                    }
                )
            }
            .scale(scale)
            .size(fabSize)
            .clip(CircleShape)
            .shadow(
                elevation = 8.dp * config.size,
                shape = CircleShape,
                clip = false
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF69B4),
                        Color(0xFFFF1493),
                        Color(0xFFC71585)
                    )
                ),
                shape = CircleShape
            )
            .clickable {
                isPressed = true
                scope.launch {
                    delay(100)
                    isPressed = false
                    
                    if (isExpanded) {
                        isExpanded = false
                    } else {
                        showCandyBurst = true
                        generateCandyBurst(
                            centerX = offsetX + fabSize.toPx() / 2,
                            centerY = offsetY + fabSize.toPx() / 2,
                            candyType = config.candyType
                        ) { particles ->
                            candyParticles = particles
                            scope.launch {
                                animateCandyParticles(particles) { updatedParticles ->
                                    candyParticles = updatedParticles
                                }
                            }
                        }
                        onFabClick()
                    }
                }
            }
    ) {
        // Candy dispenser globe
        when (config.style) {
            CandyDispenserStyle.GUMBALL_MACHINE -> {
                GumballMachineGlobe(
                    rotation = candyRotation,
                    glowAlpha = glowAlpha,
                    candyType = config.candyType
                )
            }
            CandyDispenserStyle.JAWBREAKER -> {
                JawbreakerGlobe(
                    rotation = candyRotation,
                    glowAlpha = glowAlpha
                )
            }
            CandyDispenserStyle.LOLLIPOP_DISPENSER -> {
                LollipopDispenserGlobe(
                    rotation = candyRotation,
                    glowAlpha = glowAlpha
                )
            }
        }
        
        // Trail effect overlay
        Canvas(modifier = Modifier.matchParentSize()) {
            trailParticles.forEach { particle ->
                drawCircle(
                    color = particle.color.copy(alpha = particle.life * 0.5f * config.opacity),
                    radius = particle.size * particle.life,
                    center = Offset(particle.x - offsetX, particle.y - offsetY)
                )
            }
        }
    }
    
    // Candy burst overlay
    if (showCandyBurst) {
        CandyBurstOverlay(
            particles = candyParticles,
            onAnimationComplete = {
                showCandyBurst = false
                candyParticles = emptyList()
            }
        )
    }
    
    // Expanded menu
    if (isExpanded) {
        SugarFabMenu(
            offsetX = offsetX,
            offsetY = offsetY,
            fabSize = fabSize,
            effects = config.shortcutEffects,
            onEffectSelected = { effect ->
                isExpanded = false
                onEffectSelected(effect)
            },
            onClose = { isExpanded = false }
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// GLOBE STYLES
// ═════════════════════════════════════════════════════════════════

@Composable
private fun GumballMachineGlobe(
    rotation: Float,
    glowAlpha: Float,
    candyType: CandyType
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.85f
        
        // Outer glow
        drawCircle(
            color = Color(0xFFFF69B4).copy(alpha = glowAlpha * 0.3f),
            radius = radius * 1.2f
        )
        
        // Glass globe
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE0F7FA).copy(alpha = 0.4f),
                    Color(0xFFB2EBF2).copy(alpha = 0.2f),
                    Color(0xFF80DEEA).copy(alpha = 0.1f)
                )
            ),
            radius = radius,
            center = center
        )
        
        // Glass highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = radius * 0.9f,
            center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f)
        )
        
        // Rotating candies inside
        rotate(rotation) {
            repeat(8) { i ->
                val angle = (i * 45f).toRadians()
                val candyX = center.x + cos(angle) * radius * 0.6f
                val candyY = center.y + sin(angle) * radius * 0.6f
                
                drawCandy(
                    center = Offset(candyX, candyY),
                    size = radius * 0.15f,
                    color = getCandyColor(candyType, i),
                    type = candyType
                )
            }
        }
        
        // Base
        drawRect(
            color = Color(0xFF424242),
            topLeft = Offset(size.width * 0.3f, size.height * 0.85f),
            size = Size(size.width * 0.4f, size.height * 0.1f)
        )
    }
}

@Composable
private fun JawbreakerGlobe(
    rotation: Float,
    glowAlpha: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.9f
        
        // Outer glow
        drawCircle(
            color = Color(0xFFFF69B4).copy(alpha = glowAlpha * 0.3f),
            radius = radius * 1.1f
        )
        
        // Jawbreaker layers
        repeat(5) { i ->
            drawCircle(
                color = Color(
                    red = 0.5f + 0.5f * sin(rotation.toRadians() + i * 0.5f),
                    green = 0.5f + 0.5f * cos(rotation.toRadians() + i * 0.3f),
                    blue = 0.5f + 0.5f * sin(rotation.toRadians() + i * 0.7f),
                    alpha = 0.3f
                ),
                radius = radius * (1f - i * 0.15f),
                center = center
            )
        }
        
        // Swirl pattern
        rotate(rotation) {
            repeat(6) { i ->
                drawArc(
                    color = Color.White.copy(alpha = 0.4f),
                    startAngle = i * 60f,
                    sweepAngle = 30f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.8f, center.y - radius * 0.8f),
                    size = Size(radius * 1.6f, radius * 1.6f),
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

@Composable
private fun LollipopDispenserGlobe(
    rotation: Float,
    glowAlpha: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.7f
        
        // Outer glow
        drawCircle(
            color = Color(0xFFFF69B4).copy(alpha = glowAlpha * 0.3f),
            radius = radius * 1.2f
        )
        
        // Lollipop spiral
        rotate(rotation) {
            repeat(8) { i ->
                drawArc(
                    color = RainbowColors[i % RainbowColors.size].copy(alpha = 0.5f),
                    startAngle = i * 45f,
                    sweepAngle = 40f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
        }
        
        // Center circle
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = radius * 0.3f,
            center = center
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// CANDY BURST EFFECT
// ═════════════════════════════════════════════════════════════════

private fun generateCandyBurst(
    centerX: Float,
    centerY: Float,
    candyType: CandyType,
    onComplete: (List<CandyParticle>) -> Unit
) {
    val particles = List(20) { i ->
        val angle = (i * 18f).toRadians()
        val speed = 5f + Random.nextFloat() * 10f
        CandyParticle(
            x = centerX,
            y = centerY,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            size = 8f + Random.nextFloat() * 8f,
            color = getCandyColor(candyType, i),
            rotation = Random.nextFloat() * 360f,
            life = 1f
        )
    }
    onComplete(particles)
}

@Composable
private fun CandyBurstOverlay(
    particles: List<CandyParticle>,
    onAnimationComplete: () -> Unit
) {
    LaunchedEffect(particles) {
        delay(2000L)
        onAnimationComplete()
    }
    
    Popup {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                particles.forEach { particle ->
                    drawCandy(
                        center = Offset(particle.x, particle.y),
                        size = particle.size,
                        color = particle.color,
                        rotation = particle.rotation
                    )
                }
            }
        }
    }
}

private suspend fun animateCandyParticles(
    particles: List<CandyParticle>,
    onUpdate: (List<CandyParticle>) -> Unit
) {
    var updatedParticles = particles
    repeat(40) {
        delay(50)
        updatedParticles = updatedParticles.map { p ->
            p.copy(
                x = p.x + p.vx,
                y = p.y + p.vy + 0.5f, // Gravity
                vy = p.vy + 0.3f,      // Gravity acceleration
                rotation = p.rotation + 5f,
                life = p.life - 0.025f
            )
        }.filter { it.life > 0f && it.y < 2000f }
        onUpdate(updatedParticles)
        if (updatedParticles.isEmpty()) break
    }
}

private fun drawCandy(
    DrawScope: DrawScope,
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float,
    type: CandyType = CandyType.GUMBALL
) {
    with(DrawScope) {
        rotate(rotation) {
            when (type) {
                CandyType.GUMBALL -> {
                    drawCircle(color = color, radius = size, center = center)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = size * 0.3f,
                        center = Offset(center.x - size * 0.3f, center.y - size * 0.3f)
                    )
                }
                CandyType.JELLY_BEAN -> {
                    drawOval(
                        color = color,
                        topLeft = Offset(center.x - size, center.y - size * 0.7f),
                        size = Size(size * 2, size * 1.4f)
                    )
                }
                CandyType.LOLLIPOP -> {
                    drawCircle(color = color, radius = size, center = center)
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x, center.y + size),
                        end = Offset(center.x, center.y + size * 2.5f),
                        strokeWidth = 3f
                    )
                }
                CandyType.CANDY_CANE -> {
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - size, center.y - size),
                        size = Size(size * 2, size * 2),
                        style = Stroke(width = 4f)
                    )
                }
                CandyType.SOUR_BALL -> {
                    drawCircle(color = color, radius = size, center = center)
                    // Sour sparkle
                    repeat(4) { i ->
                        val angle = (i * 90f).toRadians()
                        val sparkleX = center.x + cos(angle) * size * 0.7f
                        val sparkleY = center.y + sin(angle) * size * 0.7f
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f),
                            radius = 2f,
                            center = Offset(sparkleX, sparkleY)
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// EXPANDED MENU
// ═════════════════════════════════════════════════════════════════

@Composable
private fun SugarFabMenu(
    offsetX: Int,
    offsetY: Int,
    fabSize: androidx.compose.ui.unit.Dp,
    effects: List<String>,
    onEffectSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    Popup(
        offset = IntOffset(offsetX - 100, offsetY.toInt() - 200)
    ) {
        Card(
            modifier = Modifier
                .width(200.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Quick Effects",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                effects.forEach { effect ->
                    FilterChip(
                        selected = false,
                        onClick = { onEffectSelected(effect) },
                        label = { Text(effect) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                if (effects.isEmpty()) {
                    Text(
                        text = "No shortcuts configured",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ═════════════════════════════════════════════════════════════════

private fun getCandyColor(type: CandyType, index: Int): Color {
    return when (type) {
        CandyType.GUMBALL -> listOf(
            Color(0xFFFF69B4), Color(0xFF00FFA3), Color(0xFFFFD700),
            Color(0xFF00BFFF), Color(0xFFFF6347), Color(0xFF9370DB)
        )[index % 6]
        CandyType.JELLY_BEAN -> listOf(
            Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77),
            Color(0xFF4D96FF), Color(0xFF9D4EDD)
        )[index % 5]
        CandyType.LOLLIPOP -> RainbowColors[index % RainbowColors.size]
        CandyType.CANDY_CANE -> if (index % 2 == 0) Color(0xFFFF0000) else Color.White
        CandyType.SOUR_BALL -> listOf(
            Color(0xFF39FF14), Color(0xFFFF3F34), Color(0xFFFFFF00)
        )[index % 3]
    }
}

private fun getTrailColor(type: TrailEffectType): Color {
    return when (type) {
        TrailEffectType.SPARKLE -> Color(0xFFFFD700)
        TrailEffectType.CANDY_DUST -> Color(0xFFFF69B4)
        TrailEffectType.RAINBOW -> RainbowColors.random()
        TrailEffectType.HEARTS -> Color(0xFFFF1493)
        TrailEffectType.STARS -> Color(0xFFFFD700)
    }
}

private val RainbowColors = listOf(
    Color(0xFFFF0000), Color(0xFFFFA500), Color(0xFFFFFF00),
    Color(0xFF00FF00), Color(0xFF00BFFF), Color(0xFF8B00FF)
)

private fun Float.toRadians(): Float = this * Math.PI.toFloat() / 180f
