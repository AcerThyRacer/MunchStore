package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.features.CandyAchievements
import com.sugarmunch.app.features.model.Achievement
import com.sugarmunch.app.features.model.AchievementCategory
import com.sugarmunch.app.features.model.AchievementRarity

/**
 * Achievements Screen
 * View and track all 80+ achievements
 */

@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit
) {
    // Mock user progress - in real app, this would come from AchievementManager
    val unlockedAchievements = remember { emptySet<String>() }
    val allAchievements by remember { mutableStateOf(CandyAchievements.getOrderedAchievements()) }
    
    val completionPercentage = remember(unlockedAchievements) {
        CandyAchievements.calculateCompletion(unlockedAchievements)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Progress header
            item {
                AchievementHeader(
                    totalAchievements = allAchievements.size,
                    unlockedCount = unlockedAchievements.size,
                    completionPercentage = completionPercentage
                )
            }
            
            // Categories
            items(AchievementCategory.values().toList()) { category ->
                AchievementCategorySection(
                    category = category,
                    achievements = CandyAchievements.getByCategory(category),
                    unlockedIds = unlockedAchievements
                )
            }
        }
    }
}

@Composable
private fun AchievementHeader(
    totalAchievements: Int,
    unlockedCount: Int,
    completionPercentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Trophy icon
            Text(
                text = "🏆",
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Completion percentage
            Text(
                text = "${(completionPercentage * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Completion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = completionPercentage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = unlockedCount.toString(),
                    label = "Unlocked"
                )
                
                StatItem(
                    value = (totalAchievements - unlockedCount).toString(),
                    label = "Locked"
                )
                
                StatItem(
                    value = totalAchievements.toString(),
                    label = "Total"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AchievementCategorySection(
    category: AchievementCategory,
    achievements: List<Achievement>,
    unlockedIds: Set<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Category header
        Text(
            text = getCategoryName(category),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Achievements grid
        achievements.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        isUnlocked = unlockedIds.contains(achievement.id),
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if odd number
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (!isUnlocked) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon with lock overlay
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = achievement.icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔒",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Name
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                color = if (isUnlocked) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Description
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = if (isUnlocked) {
                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Rarity badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getRarityEmoji(achievement.rarity),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = achievement.rarity.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = getRarityColor(achievement.rarity)
                )
            }
        }
    }
}

private fun getCategoryName(category: AchievementCategory): String {
    return when (category) {
        AchievementCategory.INSTALLER -> "📥 Installer"
        AchievementCategory.EXPLORER -> "🔍 Explorer"
        AchievementCategory.CUSTOMIZER -> "🎨 Customizer"
        AchievementCategory.SOCIAL -> "👥 Social"
        AchievementCategory.COLLECTOR -> "💎 Collector"
        AchievementCategory.MASTER -> "🏆 Master"
    }
}

private fun getRarityEmoji(rarity: AchievementRarity): String {
    return when (rarity) {
        AchievementRarity.COMMON -> "🥉"
        AchievementRarity.UNCOMMON -> "🥈"
        AchievementRarity.RARE -> "🥇"
        AchievementRarity.EPIC -> "💎"
        AchievementRarity.LEGENDARY -> "🌟"
    }
}

private fun getRarityColor(rarity: AchievementRarity): Color {
    return when (rarity) {
        AchievementRarity.COMMON -> Color(0xFFCD7F32)
        AchievementRarity.UNCOMMON -> Color(0xFFC0C0C0)
        AchievementRarity.RARE -> Color(0xFFFFD700)
        AchievementRarity.EPIC -> Color(0xFF9370DB)
        AchievementRarity.LEGENDARY -> Color(0xFFFF1493)
    }
}
