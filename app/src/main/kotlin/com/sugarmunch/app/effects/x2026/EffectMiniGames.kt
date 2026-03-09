package com.sugarmunch.app.effects.x2026

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

/**
 * 🎮 EFFECT MINI-GAMES
 * Play interactive games using your unlocked effects!
 */

// ═════════════════════════════════════════════════════════════════
// GAME DATA CLASSES
// ═════════════════════════════════════════════════════════════════

data class MiniGame(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val requiredEffect: String,
    val xpReward: Int,
    val difficulty: GameDifficulty
) {
    enum class GameDifficulty { EASY, MEDIUM, HARD, EXTREME }
}

object MiniGamesCatalog {
    val ALL_GAMES = listOf(
        MiniGame(
            id = "particle_catcher",
            name = "Particle Catcher",
            description = "Catch falling quantum particles before they decay!",
            emoji = "⚛️",
            requiredEffect = "quantum_flux",
            xpReward = 50,
            difficulty = MiniGame.GameDifficulty.EASY
        ),
        MiniGame(
            id = "black_hole_dodger",
            name = "Black Hole Dodger",
            description = "Navigate through gravitational fields without getting sucked in!",
            emoji = "⚫",
            requiredEffect = "black_hole",
            xpReward = 100,
            difficulty = MiniGame.GameDifficulty.HARD
        ),
        MiniGame(
            id = "ecosystem_manager",
            name = "Ecosystem Manager",
            description = "Maintain balance in your candy ecosystem for as long as possible!",
            emoji = "🌍",
            requiredEffect = "candy_ecosystem",
            xpReward = 75,
            difficulty = MiniGame.GameDifficulty.MEDIUM
        ),
        MiniGame(
            id = "time_reflex",
            name = "Time Reflex",
            description = "Tap when time slows down perfectly!",
            emoji = "⏰",
            requiredEffect = "time_warp",
            xpReward = 60,
            difficulty = MiniGame.GameDifficulty.MEDIUM
        ),
        MiniGame(
            id = "fractal_zoomer",
            name = "Fractal Zoomer",
            description = "Navigate deep into the Mandelbrot set!",
            emoji = "🔍",
            requiredEffect = "fractal_zoom",
            xpReward = 80,
            difficulty = MiniGame.GameDifficulty.HARD
        ),
        MiniGame(
            id = "dragon_trainer",
            name = "Dragon Trainer",
            description = "Control the dragon's breath to hit targets!",
            emoji = "🐉",
            requiredEffect = "dragon_breath",
            xpReward = 90,
            difficulty = MiniGame.GameDifficulty.MEDIUM
        ),
        MiniGame(
            id = "matrix_runner",
            name = "Matrix Runner",
            description = "Dodge the digital rain in cyberspace!",
            emoji = "🌧️",
            requiredEffect = "matrix_rain",
            xpReward = 70,
            difficulty = MiniGame.GameDifficulty.EASY
        ),
        MiniGame(
            id = "chaos_control",
            name = "Chaos Control",
            description = "Predict and control chaotic particle movements!",
            emoji = "🦋",
            requiredEffect = "chaos_theory",
            xpReward = 150,
            difficulty = MiniGame.GameDifficulty.EXTREME
        )
    )
    
    fun getAvailableGames(unlockedEffects: List<String>): List<MiniGame> {
        return ALL_GAMES.filter { it.requiredEffect in unlockedEffects }
    }
}

// ═════════════════════════════════════════════════════════════════
// PARTICLE CATCHER GAME
// ═════════════════════════════════════════════════════════════════

@Composable
fun ParticleCatcherGame(
    onGameOver: (Int) -> Unit,
    onScoreChange: (Int) -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var gameActive by remember { mutableStateOf(true) }
    var particles by remember { mutableStateListOf<CatchableParticle>() }
    var basketPosition by remember { mutableFloatStateOf(0.5f) }
    
    data class CatchableParticle(
        var x: Float,
        var y: Float,
        var speed: Float,
        var color: Color,
        var isQuantum: Boolean = false,
        var id: Int = Random.nextInt()
    )
    
    LaunchedEffect(gameActive) {
        while (gameActive) {
            delay(1000)
            if (Random.nextFloat() < 0.7f) {
                particles.add(CatchableParticle(
                    x = Random.nextFloat(),
                    y = 0f,
                    speed = 0.01f + Random.nextFloat() * 0.02f,
                    color = if (Random.nextFloat() < 0.2f) Color.Magenta else Color.Cyan,
                    isQuantum = Random.nextFloat() < 0.2f
                ))
            }
        }
    }
    
    LaunchedEffect(gameActive) {
        while (gameActive) {
            delay(16)
            
            particles.forEach { particle ->
                particle.y += particle.speed
                
                // Quantum fluctuation
                if (particle.isQuantum) {
                    particle.x += (Random.nextFloat() - 0.5f) * 0.02f
                    particle.x = particle.x.coerceIn(0f, 1f)
                }
            }
            
            // Check catches
            val caught = particles.filter { particle ->
                particle.y > 0.85f && 
                particle.y < 0.95f &&
                kotlin.math.abs(particle.x - basketPosition) < 0.1f
            }
            
            caught.forEach { 
                score += if (it.isQuantum) 50 else 10
                onScoreChange(score)
            }
            particles.removeAll(caught)
            
            // Remove missed particles
            val missed = particles.filter { it.y > 1f }
            particles.removeAll(missed)
            
            if (missed.any { it.isQuantum }) {
                gameActive = false
                onGameOver(score)
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    basketPosition = (offset.x / size.width).coerceIn(0.1f, 0.9f)
                }
            }
    ) {
        // Background grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..10) {
                drawLine(
                    color = Color.Cyan.copy(alpha = 0.1f),
                    start = Offset(i * size.width / 10, 0f),
                    end = Offset(i * size.width / 10, size.height)
                )
            }
        }
        
        // Particles
        particles.forEach { particle ->
            val isSuperposed = particle.isQuantum && (System.currentTimeMillis() / 200) % 2 == 0L
            
            Box(
                modifier = Modifier
                    .offset(
                        (particle.x * 100).dp,
                        (particle.y * 100).dp
                    )
                    .size(if (particle.isQuantum) 24.dp else 16.dp)
                    .background(
                        color = if (isSuperposed) Color.Transparent else particle.color,
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = particle.color,
                        shape = CircleShape
                    )
                    .alpha(if (particle.isQuantum) 0.8f else 1f)
            )
            
            if (particle.isQuantum) {
                // Ghost positions
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .offset(
                                ((particle.x + (i - 1) * 0.05f) * 100).dp,
                                (particle.y * 100).dp
                            )
                            .size(16.dp)
                            .background(
                                color = Color.Magenta.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
        
        // Catcher basket
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset(x = ((basketPosition - 0.5f) * 300).dp)
                    .width(80.dp)
                    .height(20.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Cyan, Color.Magenta)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
            )
        }
        
        // Score
        Text(
            text = "Score: $score",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// BLACK HOLE DODGER GAME
// ═════════════════════════════════════════════════════════════════

@Composable
fun BlackHoleDodgerGame(
    onGameOver: (Int) -> Unit,
    onScoreChange: (Int) -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var gameActive by remember { mutableStateOf(true) }
    var playerPos by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var playerVelocity by remember { mutableStateOf(Offset.Zero) }
    var blackHoles by remember { mutableStateListOf<BlackHole>() }
    var time by remember { mutableFloatStateOf(0f) }
    
    data class BlackHole(
        var position: Offset,
        var mass: Float,
        var accretionRotation: Float = 0f
    )
    
    LaunchedEffect(gameActive) {
        // Spawn black holes
        while (gameActive) {
            delay(3000)
            if (blackHoles.size < 3) {
                blackHoles.add(BlackHole(
                    position = Offset(
                        Random.nextFloat() * 0.8f + 0.1f,
                        Random.nextFloat() * 0.8f + 0.1f
                    ),
                    mass = 1000f + Random.nextFloat() * 2000f
                ))
            }
        }
    }
    
    LaunchedEffect(gameActive) {
        while (gameActive) {
            delay(16)
            time += 0.016f
            score++
            onScoreChange(score)
            
            // Calculate gravitational forces
            var totalForce = Offset.Zero
            blackHoles.forEach { hole ->
                val delta = hole.position - playerPos
                val distance = sqrt(delta.x.pow(2) + delta.y.pow(2))
                
                if (distance > 0.02f) {
                    val forceMagnitude = hole.mass / (distance.pow(2) * 100000f)
                    totalForce += (delta / distance) * forceMagnitude
                } else {
                    // Too close - game over!
                    gameActive = false
                    onGameOver(score)
                }
                
                // Rotate accretion disk
                hole.accretionRotation += 0.05f
            }
            
            playerVelocity += totalForce * 0.016f
            playerVelocity *= 0.98f // Damping
            playerPos += playerVelocity * 0.016f
            
            // Keep in bounds
            playerPos = Offset(
                playerPos.x.coerceIn(0.05f, 0.95f),
                playerPos.y.coerceIn(0.05f, 0.95f)
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val sensitivity = 0.001f
                    playerVelocity += Offset(
                        dragAmount.x * sensitivity,
                        dragAmount.y * sensitivity
                    )
                }
            }
    ) {
        // Warped space grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 20
            for (i in 0..gridSize) {
                for (j in 0..gridSize) {
                    val x = i * size.width / gridSize
                    val y = j * size.height / gridSize
                    
                    // Calculate space warping
                    var warpX = 0f
                    var warpY = 0f
                    
                    blackHoles.forEach { hole ->
                        val holeX = hole.position.x * size.width
                        val holeY = hole.position.y * size.height
                        val dist = sqrt((x - holeX).pow(2) + (y - holeY).pow(2))
                        val warp = (hole.mass / 10000f) / (dist + 1f)
                        val angle = atan2(y - holeY, x - holeX)
                        
                        warpX += cos(angle) * warp
                        warpY += sin(angle) * warp
                    }
                    
                    drawCircle(
                        color = Color.Purple.copy(alpha = 0.2f),
                        radius = 2f,
                        center = Offset(x + warpX, y + warpY)
                    )
                }
            }
            
            // Draw black holes
            blackHoles.forEach { hole ->
                val center = Offset(
                    hole.position.x * size.width,
                    hole.position.y * size.height
                )
                
                // Event horizon
                drawCircle(
                    color = Color.Black,
                    radius = 30f,
                    center = center
                )
                
                // Photon ring
                drawCircle(
                    color = Color(0xFFFFA500).copy(alpha = 0.8f),
                    radius = 35f,
                    center = center,
                    style = Stroke(width = 3f)
                )
                
                // Accretion disk
                for (ring in 1..3) {
                    drawArc(
                        color = Color(0xFFFF4500).copy(alpha = 0.6f / ring),
                        startAngle = hole.accretionRotation,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = center - Offset(40f + ring * 10f, 40f + ring * 10f),
                        size = androidx.compose.ui.geometry.Size(
                            (80f + ring * 20f),
                            (80f + ring * 20f)
                        ),
                        style = Stroke(width = 4f)
                    )
                }
            }
            
            // Draw player
            drawCircle(
                color = Color.Cyan,
                radius = 10f,
                center = Offset(
                    playerPos.x * size.width,
                    playerPos.y * size.height
                )
            )
            
            // Player trail
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.3f),
                radius = 15f,
                center = Offset(
                    playerPos.x * size.width - playerVelocity.x * 50,
                    playerPos.y * size.height - playerVelocity.y * 50
                )
            )
        }
        
        // UI
        Text(
            text = "SURVIVAL TIME: ${time.toInt()}s",
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
        
        Text(
            text = "Drag to escape gravity!",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// TIME REFLEX GAME
// ═════════════════════════════════════════════════════════════════

@Composable
fun TimeReflexGame(
    onGameOver: (Int) -> Unit,
    onScoreChange: (Int) -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var gameActive by remember { mutableStateOf(true) }
    var timeScale by remember { mutableFloatStateOf(1f) }
    var targetScale by remember { mutableFloatStateOf(1f) }
    var pulsePhase by remember { mutableFloatStateOf(0f) }
    var canTap by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(gameActive) {
        while (gameActive) {
            // Random target phase
            delay(1000 + Random.nextLong(2000))
            targetScale = 0.2f + Random.nextFloat() * 0.3f
            canTap = true
            
            delay(2000)
            if (canTap) {
                // Missed!
                feedback = "TOO SLOW!"
                delay(500)
                gameActive = false
                onGameOver(score)
            }
        }
    }
    
    LaunchedEffect(gameActive) {
        while (gameActive) {
            delay(16)
            pulsePhase += 0.05f * timeScale
            
            // Oscillate time scale
            timeScale = 1f + sin(pulsePhase) * 0.5f
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    if (canTap) {
                        val accuracy = 1f - abs(timeScale - targetScale)
                        val points = (accuracy * 100).toInt()
                        
                        score += points
                        onScoreChange(score)
                        
                        feedback = when {
                            accuracy > 0.9f -> "PERFECT! +$points"
                            accuracy > 0.7f -> "GREAT! +$points"
                            accuracy > 0.5f -> "GOOD! +$points"
                            else -> "OKAY +$points"
                        }
                        
                        canTap = false
                        targetScale = 1f
                        
                        scope.launch {
                            delay(500)
                            feedback = null
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Time distortion rings
        repeat(5) { i ->
            val scale = 1f + i * 0.3f * timeScale
            val alpha = 0.5f - i * 0.1f
            
            Box(
                modifier = Modifier
                    .size((200 * scale).dp)
                    .border(
                        width = 3.dp,
                        color = Color(0xFFFFD700).copy(alpha = alpha.coerceIn(0f, 1f)),
                        shape = CircleShape
                    )
                    .rotate(pulsePhase * 10f * (i + 1))
            )
        }
        
        // Center clock
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFF8C00)
                        )
                    ),
                    shape = CircleShape
                )
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "⏰",
                    fontSize = 40.sp
                )
                Text(
                    text = "%.1fx".format(timeScale),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        
        // Target indicator
        if (canTap) {
            Text(
                text = "TARGET: %.1fx".format(targetScale),
                fontSize = 20.sp,
                color = Color.Cyan,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
            )
        }
        
        // Score
        Text(
            text = "Score: $score",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
        
        // Feedback
        AnimatedVisibility(
            visible = feedback != null,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Text(
                text = feedback ?: "",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF00),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// MINI GAME CARD
// ═════════════════════════════════════════════════════════════════

@Composable
fun MiniGameCard(
    game: MiniGame,
    isAvailable: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    val difficultyColor = when (game.difficulty) {
        MiniGame.GameDifficulty.EASY -> Color(0xFF4CAF50)
        MiniGame.GameDifficulty.MEDIUM -> Color(0xFFFFC107)
        MiniGame.GameDifficulty.HARD -> Color(0xFFFF5722)
        MiniGame.GameDifficulty.EXTREME -> Color(0xFF9C27B0)
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable)
                colors.surface.copy(alpha = 0.95f)
            else
                colors.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isAvailable) {
            BorderStroke(2.dp, difficultyColor.copy(alpha = 0.5f))
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        difficultyColor.copy(alpha = 0.2f),
                        CircleShape
                    )
                    .border(3.dp, difficultyColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(game.emoji, fontSize = 36.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    game.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAvailable) colors.onSurface else colors.onSurface.copy(alpha = 0.5f)
                )
                
                Text(
                    game.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = if (isAvailable) 0.7f else 0.3f)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Difficulty badge
                    Box(
                        modifier = Modifier
                            .background(
                                difficultyColor.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            game.difficulty.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = difficultyColor
                        )
                    }
                    
                    // XP reward
                    Text(
                        "+${game.xpReward} XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary
                    )
                }
            }
            
            if (!isAvailable) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = colors.onSurface.copy(alpha = 0.3f)
                )
            } else {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = colors.primary
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// GAME SELECTION DIALOG
// ═════════════════════════════════════════════════════════════════

@Composable
fun MiniGamesSelectionDialog(
    unlockedEffects: List<String>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onGameSelected: (MiniGame) -> Unit,
    onDismiss: () -> Unit
) {
    val availableGames = remember(unlockedEffects) {
        MiniGamesCatalog.getAvailableGames(unlockedEffects)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎮 ")
                Text("Effect Mini-Games")
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availableGames.size) { index ->
                    MiniGameCard(
                        game = availableGames[index],
                        isAvailable = true,
                        colors = colors,
                        onClick = { onGameSelected(availableGames[index]) }
                    )
                }
                
                // Locked games
                val lockedGames = MiniGamesCatalog.ALL_GAMES.filter { 
                    it.requiredEffect !in unlockedEffects 
                }
                
                if (lockedGames.isNotEmpty()) {
                    item {
                        Text(
                            "Locked Games",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    items(lockedGames.take(3)) { game ->
                        MiniGameCard(
                            game = game,
                            isAvailable = false,
                            colors = colors,
                            onClick = {}
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
