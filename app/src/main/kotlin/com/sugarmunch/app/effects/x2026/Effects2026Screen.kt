package com.sugarmunch.app.effects.x2026

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Effects2026Screen(
    onBack: () -> Unit,
    onEffectDetail: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val levelManager = remember { EffectsLevelManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    
    val progress by levelManager.progress.collectAsState()
    val availableUnlocks by levelManager.availableUnlocks.collectAsState()
    val newAchievements by levelManager.newAchievements.collectAsState()
    val currentChallenge by levelManager.currentChallenge.collectAsState()
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    var selectedCategory by remember { mutableStateOf<Effect2026Category?>(null) }
    var showAchievements by remember { mutableStateOf(false) }
    var showCombos by remember { mutableStateOf(false) }
    
    // Level up animation trigger
    var showLevelUp by remember { mutableStateOf(false) }
    var lastLevel by remember { mutableStateOf(progress.currentLevel) }
    
    LaunchedEffect(progress.currentLevel) {
        if (progress.currentLevel > lastLevel) {
            showLevelUp = true
            delay(3000)
            showLevelUp = false
        }
        lastLevel = progress.currentLevel
    }
    
    // Check for new achievements
    LaunchedEffect(newAchievements) {
        if (newAchievements.isNotEmpty()) {
            delay(500)
            showAchievements = true
        }
    }
    
    Scaffold(
        topBar = {
            Effects2026TopBar(
                progress = progress,
                colors = colors,
                onBack = onBack,
                onAchievementsClick = { showAchievements = true },
                onCombosClick = { showCombos = true }
            )
        },
        floatingActionButton = {
            if (availableUnlocks.isNotEmpty()) {
                UnlockFab(
                    count = availableUnlocks.size,
                    colors = colors,
                    onClick = { selectedCategory = null }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            
            // Dynamic background effects based on level
            LevelBackgroundEffect(level = progress.currentLevel)
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player Stats Card
                item {
                    PlayerStatsCard(
                        progress = progress,
                        colors = colors,
                        xpProgress = levelManager.getXpProgress()
                    )
                }
                
                // Daily Challenge
                currentChallenge?.let { challenge ->
                    item {
                        DailyChallengeCard(
                            challenge = challenge,
                            colors = colors,
                            onComplete = { levelManager.completeDailyChallenge() }
                        )
                    }
                }
                
                // Available Unlocks
                if (availableUnlocks.isNotEmpty()) {
                    item {
                        AvailableUnlocksCarousel(
                            effects = availableUnlocks,
                            colors = colors,
                            onUnlock = { levelManager.unlockEffect(it) }
                        )
                    }
                }
                
                // Category Filter
                item {
                    CategoryFilterRow(
                        selectedCategory = selectedCategory,
                        colors = colors,
                        onCategorySelect = { selectedCategory = it }
                    )
                }
                
                // Effects Grid
                val filteredEffects = selectedCategory?.let { cat ->
                    Effects2026Catalog.getByCategory(cat)
                } ?: Effects2026Catalog.ALL_EFFECTS_2026
                
                items(filteredEffects.chunked(2)) { rowEffects ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowEffects.forEach { effect ->
                            val isUnlocked = progress.effectsUnlocked.contains(effect.id)
                            val canUnlock = levelManager.canUnlockEffect(effect)
                            
                            Effect2026Card(
                                effect = effect,
                                isUnlocked = isUnlocked,
                                canUnlock = canUnlock,
                                playerLevel = progress.currentLevel,
                                colors = colors,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (isUnlocked) {
                                        levelManager.recordEffectUse(effect.id)
                                        onEffectDetail(effect.id)
                                    } else if (canUnlock) {
                                        levelManager.unlockEffect(effect.id)
                                    }
                                }
                            )
                        }
                        if (rowEffects.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            
            // Level Up Animation
            AnimatedVisibility(
                visible = showLevelUp,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                LevelUpOverlay(
                    level = progress.currentLevel,
                    colors = colors
                )
            }
            
            // Achievements Dialog
            if (showAchievements && newAchievements.isNotEmpty()) {
                AchievementsDialog(
                    achievements = newAchievements,
                    colors = colors,
                    onDismiss = {
                        levelManager.clearNewAchievements()
                        showAchievements = false
                    }
                )
            }
            
            // Combos Dialog
            if (showCombos) {
                CombosDialog(
                    unlockedEffects = progress.effectsUnlocked,
                    level = progress.currentLevel,
                    colors = colors,
                    onDismiss = { showCombos = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Effects2026TopBar(
    progress: PlayerProgress,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onBack: () -> Unit,
    onAchievementsClick: () -> Unit,
    onCombosClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "🚀 2026 Effects",
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "Level ${progress.currentLevel} • ${progress.effectsUnlocked.size}/${Effects2026Catalog.TOTAL_EFFECTS} Unlocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
            }
        },
        actions = {
            // Achievements
            BadgedBox(
                badge = {
                    if (progress.achievementsUnlocked.isNotEmpty()) {
                        Badge {
                            Text(progress.achievementsUnlocked.size.toString())
                        }
                    }
                }
            ) {
                IconButton(onClick = onAchievementsClick) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Achievements", tint = colors.primary)
                }
            }
            
            // Combos
            IconButton(onClick = onCombosClick) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Combos", tint = colors.secondary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun PlayerStatsCard(
    progress: PlayerProgress,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    xpProgress: Pair<Int, Int>
) {
    val (current, needed) = xpProgress
    val progressPercent = if (needed > 0) current.toFloat() / needed else 1f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(colors.primary, colors.secondary)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${progress.currentLevel}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimary
                        )
                        Text(
                            "LEVEL",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "XP Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = colors.primary,
                        trackColor = colors.surfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        "$current / $needed XP to Level ${progress.currentLevel + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("🔥", "${progress.dailyStreak}", "Day Streak", colors)
                StatItem("⚡", "${progress.totalXpEarned}", "Total XP", colors)
                StatItem("🏆", "${progress.achievementsUnlocked.size}", "Achievements", colors)
                StatItem("⚔️", "${progress.combosPerformed}", "Combos", colors)
            }
        }
    }
}

@Composable
private fun StatItem(
    emoji: String,
    value: String,
    label: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DailyChallengeCard(
    challenge: DailyChallenge,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFD700).copy(alpha = 0.15f)
        ),
        border = BorderStroke(2.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Color(0xFFFFD700).copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(challenge.emoji, fontSize = 28.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Daily Challenge",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFD700)
                )
                Text(
                    challenge.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface
                )
                Text(
                    "+${challenge.xpReward} XP reward",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary
                )
            }
        }
    }
}

@Composable
private fun AvailableUnlocksCarousel(
    effects: List<Effect2026>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onUnlock: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.primary.copy(alpha = 0.1f)
        ),
        border = BorderStroke(2.dp, colors.primary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = colors.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Available to Unlock!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Badge {
                    Text(effects.size.toString())
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(effects.take(5)) { effect ->
                    UnlockableEffectChip(
                        effect = effect,
                        colors = colors,
                        onUnlock = { onUnlock(effect.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UnlockableEffectChip(
    effect: Effect2026,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onUnlock: () -> Unit
) {
    val gradientColors = effect.gradientColors.map { Color(it) }
    
    Card(
        onClick = onUnlock,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(gradientColors),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(effect.emoji, fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                effect.name,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface
            )
            
            Text(
                "${effect.xpValue} XP",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: Effect2026Category?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onCategorySelect: (Effect2026Category?) -> Unit
) {
    val categories = Effect2026Category.values().toList()
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text("🌟 All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary,
                    selectedLabelColor = colors.onPrimary
                )
            )
        }
        
        items(categories) { category ->
            val emoji = when (category) {
                Effect2026Category.QUANTUM -> "⚛️"
                Effect2026Category.HOLOGRAPHIC -> "🔮"
                Effect2026Category.TEMPORAL -> "⏰"
                Effect2026Category.BIOLOGICAL -> "🦠"
                Effect2026Category.NEBULA -> "🌌"
                Effect2026Category.CYBERNETIC -> "🧠"
                Effect2026Category.ELEMENTAL -> "🌪️"
                Effect2026Category.MYTHICAL -> "🐉"
                Effect2026Category.SYNTHWAVE -> "🕹️"
                Effect2026Category.CHAOS -> "🦋"
            }
            
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelect(category) },
                label = { Text("$emoji ${category.name}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary,
                    selectedLabelColor = colors.onPrimary
                )
            )
        }
    }
}

@Composable
private fun Effect2026Card(
    effect: Effect2026,
    isUnlocked: Boolean,
    canUnlock: Boolean,
    playerLevel: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradientColors = effect.gradientColors.map { Color(it) }
    val rarityColor = Color(effect.rarity.color())
    
    val alpha = when {
        isUnlocked -> 1f
        canUnlock -> 0.7f
        else -> 0.4f
    }
    
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.8f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                colors.surface.copy(alpha = 0.95f)
            else
                colors.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isUnlocked) {
            BorderStroke(2.dp, Brush.linearGradient(gradientColors))
        } else if (canUnlock) {
            BorderStroke(2.dp, rarityColor.copy(alpha = 0.5f))
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                gradientColors[0].copy(alpha = if (isUnlocked) 0.3f else 0.1f),
                                gradientColors.getOrElse(1) { gradientColors[0] }
                                    .copy(alpha = if (isUnlocked) 0.1f else 0.05f)
                            )
                        )
                    )
            )
            
            // Locked overlay
            if (!isUnlocked && !canUnlock) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Rarity indicator
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = if (isUnlocked) gradientColors else gradientColors.map { it.copy(alpha = 0.5f) }
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            color = if (isUnlocked) rarityColor else rarityColor.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = effect.emoji,
                        fontSize = 30.sp,
                        modifier = Modifier.alpha(alpha)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = effect.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) colors.onSurface else colors.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = effect.category.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = if (isUnlocked) 0.7f else 0.3f)
                )
                
                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (canUnlock) {
                        Text(
                            "🔓 TAP TO UNLOCK",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            "🔒 LVL ${effect.levelRequired}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.error.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Tags
                if (isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (effect.hasPhysics) {
                            TagChip("⚛️", colors)
                        }
                        if (effect.hasAI) {
                            TagChip("🧠", colors)
                        }
                        if (effect.isInteractive) {
                            TagChip("👆", colors)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagChip(emoji: String, colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Box(
        modifier = Modifier
            .background(
                colors.primary.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(emoji, fontSize = 12.sp)
    }
}

@Composable
private fun UnlockFab(
    count: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = colors.primary,
        contentColor = colors.onPrimary,
        shape = CircleShape
    ) {
        BadgedBox(
            badge = {
                Badge {
                    Text(count.toString())
                }
            }
        ) {
            Icon(Icons.Default.LockOpen, contentDescription = "Unlock Effects")
        }
    }
}

@Composable
private fun LevelUpOverlay(
    level: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "celebration")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎉", fontSize = 80.sp, modifier = Modifier.scale(scale))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "LEVEL UP!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
            
            Text(
                "You reached Level $level",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "New effects unlocked!",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.primary
            )
        }
    }
}

@Composable
private fun LevelBackgroundEffect(level: Int) {
    // Add subtle particle effects based on level
    when {
        level >= 100 -> TranscendenceBackground()
        level >= 50 -> CosmicBackground()
        level >= 25 -> TechBackground()
        level >= 10 -> SparkleBackground()
    }
}

@Composable
private fun SparkleBackground() {
    // Subtle sparkle particles
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(10) { index ->
            FloatingSparkle(index = index)
        }
    }
}

@Composable
private fun FloatingSparkle(index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle$index")
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000 + index * 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000 + index * 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Text(
        "✨",
        fontSize = 20.sp,
        modifier = Modifier
            .offset((index * 30 + offsetX).dp, (index * 50 + offsetY).dp)
            .alpha(alpha)
    )
}

@Composable
private fun TechBackground() {
    // Digital rain effect
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(5) { index ->
            DigitalRainColumn(index = index)
        }
    }
}

@Composable
private fun DigitalRainColumn(index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain$index")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000 + index * 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain"
    )
    
    Text(
        "01",
        fontSize = 14.sp,
        color = Color(0xFF00FF00).copy(alpha = 0.2f),
        modifier = Modifier.offset((index * 80).dp, offsetY.dp)
    )
}

@Composable
private fun CosmicBackground() {
    // Stars and nebula
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(20) { index ->
            FloatingStar(index = index)
        }
    }
}

@Composable
private fun FloatingStar(index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "star$index")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000 + index * 100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Text(
        "⭐",
        fontSize = (10 + index).sp,
        modifier = Modifier
            .offset((index * 50).dp, (index * 80).dp)
            .scale(scale)
            .alpha(0.3f)
    )
}

@Composable
private fun TranscendenceBackground() {
    // Ultimate rainbow effect
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF0000).copy(alpha = 0.1f),
                        Color(0xFFFF7F00).copy(alpha = 0.1f),
                        Color(0xFFFFFF00).copy(alpha = 0.1f),
                        Color(0xFF00FF00).copy(alpha = 0.1f),
                        Color(0xFF0000FF).copy(alpha = 0.1f),
                        Color(0xFF9400D3).copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun AchievementsDialog(
    achievements: List<Achievement>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏆 ")
                Text("Achievement Unlocked!")
            }
        },
        text = {
            LazyColumn {
                items(achievements) { achievement ->
                    AchievementCard(achievement = achievement, colors = colors)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Awesome!")
            }
        }
    )
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFD700).copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(achievement.emoji, fontSize = 32.sp)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Text(
                "+${achievement.xpReward} XP",
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CombosDialog(
    unlockedEffects: List<String>,
    level: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onDismiss: () -> Unit
) {
    val availableCombos = remember(unlockedEffects, level) {
        EffectCombosCatalog.getAvailableCombos(unlockedEffects, level)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚡ Effect Combos") },
        text = {
            if (availableCombos.isEmpty()) {
                Text(
                    "Unlock more effects to discover combos!\n\nCombos multiply your XP when you use specific effect combinations together.",
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            } else {
                LazyColumn {
                    items(availableCombos) { combo ->
                        ComboCard(combo = combo, colors = colors)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it!")
            }
        }
    )
}

@Composable
private fun ComboCard(
    combo: EffectCombo,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(combo.emoji, fontSize = 32.sp)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    combo.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    combo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    "${combo.xpMultiplier}x XP Bonus",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary
                )
            }
        }
    }
}
