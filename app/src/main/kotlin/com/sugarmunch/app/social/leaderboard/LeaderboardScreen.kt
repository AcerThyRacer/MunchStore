package com.sugarmunch.app.social.leaderboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Global Leaderboards - Rank users by various metrics
 */
@Composable
fun LeaderboardScreen(
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(LeaderboardCategory.ALL_TIME) }
    val leaders = remember(selectedCategory) { getMockLeaders(selectedCategory) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboards") },
                actions = {
                    IconButton(onClick = { /* Refresh */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(LeaderboardCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.displayName) }
                    )
                }
            }
            
            // Leaders list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leaders) { leader ->
                    LeaderRow(
                        leader = leader,
                        rank = leaders.indexOf(leader) + 1,
                        onClick = { onUserClick(leader.userId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderRow(
    leader: LeaderboardEntry,
    rank: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when (rank) {
                1 -> MaterialTheme.colorScheme.primaryContainer
                2 -> MaterialTheme.colorScheme.secondaryContainer
                3 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (rank) {
                                1 -> Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500)))
                                2 -> Brush.linearGradient(listOf(Color(0xFFC0C0C0), Color(0xFF808080)))
                                3 -> Brush.linearGradient(listOf(Color(0xFFCD7F32), Color(0xFF8B4513)))
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (rank <= 3) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    } else {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Avatar & name
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFF69B4), Color(0xFF9370DB))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = leader.username.take(2).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = leader.username,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Level ${leader.level}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Score
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatScore(leader.score),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = leader.category.unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatScore(score: Int): String {
    return when {
        score >= 1000000 -> "%.1fM".format(score / 1000000f)
        score >= 1000 -> "%.1fK".format(score / 1000f)
        else -> score.toString()
    }
}

enum class LeaderboardCategory(val displayName: String, val unit: String) {
    ALL_TIME("All Time", "points"),
    WEEKLY("This Week", "points"),
    DOWNLOADS("Downloads", "downloads"),
    THEMES("Themes Created", "themes"),
    STREAK("Longest Streak", "days")
}

data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val level: Int,
    val score: Int,
    val category: LeaderboardCategory
)

private fun getMockLeaders(category: LeaderboardCategory): List<LeaderboardEntry> {
    return listOf(
        LeaderboardEntry("user_1", "ThemeMaster", 42, 125000, category),
        LeaderboardEntry("user_2", "OceanLover", 38, 98000, category),
        LeaderboardEntry("user_3", "NeonDreamer", 35, 87500, category),
        LeaderboardEntry("user_4", "CandyKing", 31, 76000, category),
        LeaderboardEntry("user_5", "SugarQueen", 29, 65000, category)
    )
}
