package com.sugarmunch.app.features.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.features.AchievementManager
import com.sugarmunch.app.features.AchievementSystem
import com.sugarmunch.app.features.model.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val achievementManager = remember { AchievementManager.getInstance(context) }
    
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val userAchievements by achievementManager.userAchievements.collectAsState()
    val totalProgress by achievementManager.totalProgress.collectAsState()
    val sugarPoints by achievementManager.sugarPoints.collectAsState()
    val unlockedAchievements by achievementManager.unlockedAchievements.collectAsState()
    
    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }
    var showHidden by remember { mutableStateOf(false) }
    
    val achievements = if (selectedCategory != null) {
        AchievementSystem.getByCategory(selectedCategory!!)
    } else {
        AchievementSystem.ALL_ACHIEVEMENTS
    }.filter { !it.hidden || showHidden || userAchievements[it.id]?.isUnlocked == true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Achievements",
                        color = colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Stats Header
                AchievementsStatsHeader(
                    unlockedCount = unlockedAchievements.size,
                    totalCount = AchievementSystem.ALL_ACHIEVEMENTS.size,
                    progress = totalProgress,
                    sugarPoints = sugarPoints,
                    colors = colors
                )
                
                // Category Filter
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    colors = colors
                )
                
                // Show Hidden Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show Hidden Achievements",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = showHidden,
                        onCheckedChange = { showHidden = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.primary,
                            checkedTrackColor = colors.primary.copy(alpha = 0.5f)
                        )
                    )
                }
                
                // Achievements Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(achievements.sortedBy { it.order }) { achievement ->
                        val userAchievement = userAchievements[achievement.id]
                        AchievementCard(
                            achievement = achievement,
                            userAchievement = userAchievement,
                            colors = colors
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsStatsHeader(
    unlockedCount: Int,
    totalCount: Int,
    progress: Float,
    sugarPoints: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "$unlockedCount/$totalCount",
                    label = "Unlocked",
                    icon = "🏆",
                    colors = colors
                )
                StatItem(
                    value = "${(progress * 100).toInt()}%",
                    label = "Complete",
                    icon = "📊",
                    colors = colors
                )
                StatItem(
                    value = "$sugarPoints",
                    label = "Sugar Points",
                    icon = "🍬",
                    colors = colors
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    progress >= 0.9f -> Color(0xFFE91E63) // Legendary
                    progress >= 0.7f -> Color(0xFF9C27B0) // Epic
                    progress >= 0.5f -> Color(0xFFFF9800) // Rare
                    progress >= 0.3f -> Color(0xFFCDDC39) // Uncommon
                    else -> colors.primary
                },
                trackColor = colors.surfaceVariant
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: AchievementCategory?,
    onCategorySelected: (AchievementCategory?) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("All") },
            leadingIcon = if (selectedCategory == null) {
                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
            } else null
        )
        
        AchievementCategory.values().forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) },
                leadingIcon = if (selectedCategory == category) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    userAchievement: UserAchievement?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val isUnlocked = userAchievement?.isUnlocked ?: false
    val progress = if (isUnlocked) 1f else (userAchievement?.progressPercent ?: 0f)
    
    val rarityColor = when (achievement.rarity) {
        AchievementRarity.COMMON -> Color(0xFFCD7F32)    // Bronze
        AchievementRarity.UNCOMMON -> Color(0xFFC0C0C0)  // Silver
        AchievementRarity.RARE -> Color(0xFFFFD700)      // Gold
        AchievementRarity.EPIC -> Color(0xFF9C27B0)      // Purple
        AchievementRarity.LEGENDARY -> Color(0xFFE91E63) // Pink
    }
    
    val cardBackground = if (isUnlocked) {
        colors.surface
    } else {
        colors.surfaceVariant.copy(alpha = 0.5f)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        border = if (isUnlocked) {
            androidx.compose.foundation.BorderStroke(2.dp, rarityColor)
        } else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked) rarityColor.copy(alpha = 0.2f)
                            else colors.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = achievement.icon,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.alpha(if (isUnlocked) 1f else 0.5f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) colors.onSurface else colors.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (achievement.hidden && !isUnlocked) "???" else achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = if (isUnlocked) 0.7f else 0.4f),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                
                if (!isUnlocked && progress > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = rarityColor,
                        trackColor = colors.surfaceVariant
                    )
                }
            }
            
            // Rarity badge
            if (isUnlocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(rarityColor)
                )
            }
        }
    }
}
