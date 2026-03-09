package com.sugarmunch.app.social.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * User Profile Screen - View and edit user profiles
 */
@Composable
fun UserProfileScreen(
    userId: String,
    onBackClick: () -> Unit,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Mock user data
    val user = remember { getMockUserProfile(userId) }
    val userThemes = remember { getMockUserThemes(userId) }
    val userAchievements = remember { getMockUserAchievements(userId) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (user.isCurrentUser) {
                        IconButton(onClick = onEditProfile) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    } else {
                        IconButton(onClick = { /* Follow */ }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Follow")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile header
            item {
                ProfileHeader(user = user)
            }
            
            // Stats row
            item {
                StatsRow(stats = user.stats)
            }
            
            // Bio section
            if (user.bio.isNotBlank()) {
                item {
                    BioSection(bio = user.bio)
                }
            }
            
            // Achievements
            item {
                SectionHeader(
                    title = "Achievements",
                    count = userAchievements.size
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(userAchievements) { achievement ->
                        AchievementBadge(achievement = achievement)
                    }
                }
            }
            
            // Created themes
            item {
                SectionHeader(
                    title = "Created Themes",
                    count = userThemes.size
                )
            }
            
            items(userThemes) { theme ->
                ThemeListItem(theme = theme)
            }
            
            // Activity graph
            item {
                ActivitySection(userId = userId)
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF69B4), Color(0xFF9370DB))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display name
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            // Username
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Level badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Level ${user.stats.level}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
            // Member since
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Member since ${user.memberSince}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StatsRow(stats: UserStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatColumn(
            value = stats.themesCreated.toString(),
            label = "Themes"
        )
        StatColumn(
            value = formatNumber(stats.totalDownloads),
            label = "Downloads"
        )
        StatColumn(
            value = stats.followers.toString(),
            label = "Followers"
        )
        StatColumn(
            value = stats.following.toString(),
            label = "Following"
        )
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BioSection(bio: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AchievementBadge(achievement: UserAchievement) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = when (achievement.rarity) {
                            Rarity.COMMON -> listOf(Color(0xFFB0B0B0), Color(0xFF808080))
                            Rarity.UNCOMMON -> listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                            Rarity.RARE -> listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
                            Rarity.EPIC -> listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                            Rarity.LEGENDARY -> listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = achievement.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = achievement.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ThemeListItem(theme: UserTheme) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = theme.previewColors.map { Color(it) }
                        )
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatNumber(theme.downloads),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatNumber(theme.likes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitySection(userId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Activity",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mock activity items
            repeat(5) { index ->
                ActivityItem(
                    text = "Downloaded a theme",
                    time = "${index + 1}h ago"
                )
                if (index < 4) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(text: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═════════════════════════════════════════════════════════════
// DATA MODELS
// ═════════════════════════════════════════════════════════════

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarUrl: String?,
    val isCurrentUser: Boolean,
    val memberSince: String,
    val stats: UserStats
)

data class UserStats(
    val level: Int,
    val xp: Int,
    val themesCreated: Int,
    val totalDownloads: Int,
    val followers: Int,
    val following: Int,
    val sugarPoints: Int
)

data class UserAchievement(
    val id: String,
    val name: String,
    val rarity: Rarity,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

enum class Rarity {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
}

data class UserTheme(
    val id: String,
    val name: String,
    val previewColors: List<Long>,
    val downloads: Int,
    val likes: Int
)

private fun formatNumber(num: Int): String {
    return when {
        num >= 1000000 -> "%.1fM".format(num / 1000000f)
        num >= 1000 -> "%.1fK".format(num / 1000f)
        else -> num.toString()
    }
}

private fun getMockUserProfile(userId: String): UserProfile {
    return UserProfile(
        id = userId,
        username = "thememaster",
        displayName = "Theme Master",
        bio = "Creating beautiful themes for SugarMunch! 🎨 Follow for daily uploads!",
        avatarUrl = null,
        isCurrentUser = userId == "current_user",
        memberSince = "January 2025",
        stats = UserStats(
            level = 42,
            xp = 8500,
            themesCreated = 45,
            totalDownloads = 125000,
            followers = 3240,
            following = 156,
            sugarPoints = 28500
        )
    )
}

private fun getMockUserThemes(userId: String): List<UserTheme> {
    return listOf(
        UserTheme(
            id = "theme_1",
            name = "Sunset Paradise",
            previewColors = listOf(0xFFFF6B6B, 0xFFFF8E53, 0xFFFFC107),
            downloads = 15420,
            likes = 3240
        ),
        UserTheme(
            id = "theme_2",
            name = "Ocean Deep",
            previewColors = listOf(0xFF0077B6, 0xFF00B4D8, 0xFF90E0EF),
            downloads = 12350,
            likes = 2890
        ),
        UserTheme(
            id = "theme_3",
            name = "Neon Nights",
            previewColors = listOf(0xFFFF00FF, 0xFF00FFFF, 0xFF7B2CBF),
            downloads = 10200,
            likes = 2150
        )
    )
}

private fun getMockUserAchievements(userId: String): List<UserAchievement> {
    return listOf(
        UserAchievement(
            id = "ach_1",
            name = "Creator",
            rarity = Rarity.RARE,
            icon = Icons.Default.Palette
        ),
        UserAchievement(
            id = "ach_2",
            name = "Popular",
            rarity = Rarity.EPIC,
            icon = Icons.Default.Star
        ),
        UserAchievement(
            id = "ach_3",
            name = "Legend",
            rarity = Rarity.LEGENDARY,
            icon = Icons.Default.EmojiEvents
        )
    )
}
