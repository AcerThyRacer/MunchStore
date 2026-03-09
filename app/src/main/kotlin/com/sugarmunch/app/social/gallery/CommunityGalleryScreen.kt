package com.sugarmunch.app.social.gallery

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
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
import kotlinx.coroutines.launch

/**
 * Community Gallery - Browse and download community themes & effects
 */
@Composable
fun CommunityGalleryScreen(
    onThemeDownload: (CommunityTheme) -> Unit,
    onUserProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showUploadDialog by remember { mutableStateOf(false) }
    
    // Mock data - would come from server
    val trendingThemes = remember { getMockTrendingThemes() }
    val recentThemes = remember { getMockRecentThemes() }
    val topCreators = remember { getMockTopCreators() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Gallery") },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showUploadDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Upload")
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
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Trending") },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Recent") },
                    icon = { Icon(Icons.Default.AccessTime, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Top Creators") },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) }
                )
            }
            
            // Content based on tab
            when (selectedTab) {
                0 -> TrendingContent(
                    themes = trendingThemes,
                    onThemeDownload = onThemeDownload,
                    onUserProfileClick = onUserProfileClick
                )
                1 -> RecentContent(
                    themes = recentThemes,
                    onThemeDownload = onThemeDownload,
                    onUserProfileClick = onUserProfileClick
                )
                2 -> TopCreatorsContent(
                    creators = topCreators,
                    onUserProfileClick = onUserProfileClick
                )
            }
        }
    }
    
    if (showUploadDialog) {
        UploadThemeDialog(
            onDismiss = { showUploadDialog = false },
            onUpload = { /* Upload theme */ }
        )
    }
}

@Composable
private fun TrendingContent(
    themes: List<CommunityTheme>,
    onThemeDownload: (CommunityTheme) -> Unit,
    onUserProfileClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Featured theme
        if (themes.isNotEmpty()) {
            item {
                FeaturedThemeCard(
                    theme = themes.first(),
                    onDownload = { onThemeDownload(themes.first()) },
                    onCreatorClick = { onUserProfileClick(themes.first().creatorId) }
                )
            }
        }
        
        // Trending grid
        item {
            Text(
                text = "Trending This Week",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(themes.drop(1)) { theme ->
            ThemeCard(
                theme = theme,
                onDownload = { onThemeDownload(theme) },
                onCreatorClick = { onUserProfileClick(theme.creatorId) }
            )
        }
    }
}

@Composable
private fun RecentContent(
    themes: List<CommunityTheme>,
    onThemeDownload: (CommunityTheme) -> Unit,
    onUserProfileClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(themes) { theme ->
            CompactThemeCard(
                theme = theme,
                onDownload = { onThemeDownload(theme) }
            )
        }
    }
}

@Composable
private fun TopCreatorsContent(
    creators: List<CommunityCreator>,
    onUserProfileClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(creators) { creator ->
            CreatorCard(
                creator = creator,
                onClick = { onUserProfileClick(creator.id) }
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════
// THEME CARDS
// ═════════════════════════════════════════════════════════════

@Composable
private fun FeaturedThemeCard(
    theme: CommunityTheme,
    onDownload: () -> Unit,
    onCreatorClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Theme preview gradient
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = theme.previewColors.map { Color(it) }
                    )
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Creator info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onCreatorClick)
                    ) {
                        Avatar(
                            url = theme.creatorAvatar,
                            name = theme.creatorName,
                            size = 32.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = theme.creatorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                    
                    // Stats
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatChip(
                            icon = Icons.Default.Download,
                            value = formatNumber(theme.downloadCount)
                        )
                        StatChip(
                            icon = Icons.Default.Favorite,
                            value = formatNumber(theme.likeCount)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Theme info
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = theme.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Download button
                Button(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download")
                }
            }
            
            // Featured badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "#1 Trending",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: CommunityTheme,
    onDownload: () -> Unit,
    onCreatorClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
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
                    .size(80.dp)
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
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onCreatorClick)
                    ) {
                        Avatar(
                            url = theme.creatorAvatar,
                            name = theme.creatorName,
                            size = 20.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = theme.creatorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatNumber(theme.downloadCount),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Download button
            IconButton(onClick = onDownload) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Download",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CompactThemeCard(
    theme: CommunityTheme,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Preview gradient
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = theme.previewColors.map { Color(it) }
                    )
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatNumber(theme.downloadCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                    
                    FilledTonalIconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// CREATOR CARDS
// ═════════════════════════════════════════════════════════════

@Composable
private fun CreatorCard(
    creator: CommunityCreator,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(onClick = onClick),
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
                            when (creator.rank) {
                                1 -> Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500)))
                                2 -> Brush.linearGradient(listOf(Color(0xFFC0C0C0), Color(0xFF808080)))
                                3 -> Brush.linearGradient(listOf(Color(0xFFCD7F32), Color(0xFF8B4513)))
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${creator.rank}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (creator.rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Avatar & name
                Avatar(
                    url = creator.avatarUrl,
                    name = creator.name,
                    size = 48.dp
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = creator.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${creator.totalDownloads} downloads",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Follow button
            OutlinedButton(onClick = { /* Follow */ }) {
                Text("Follow")
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// UPLOAD DIALOG
// ═════════════════════════════════════════════════════════════

@Composable
private fun UploadThemeDialog(
    onDismiss: () -> Unit,
    onUpload: (CommunityTheme) -> Unit
) {
    var themeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColors by remember { mutableStateOf(listOf<Long>(0xFFFF69B4, 0xFF9370DB)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Theme to Community") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    label = { Text("Theme Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Text(
                    text = "Preview Colors",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedColors.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val theme = CommunityTheme(
                        id = "theme_${System.currentTimeMillis()}",
                        name = themeName,
                        description = description,
                        previewColors = selectedColors,
                        creatorId = "current_user",
                        creatorName = "Current User",
                        creatorAvatar = null,
                        downloadCount = 0,
                        likeCount = 0,
                        createdAt = System.currentTimeMillis()
                    )
                    onUpload(theme)
                    onDismiss()
                },
                enabled = themeName.isNotBlank()
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ═════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ═════════════════════════════════════════════════════════════

@Composable
private fun Avatar(
    url: String?,
    name: String,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFF69B4), Color(0xFF9370DB))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(2).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

@Composable
private fun formatNumber(num: Int): String {
    return when {
        num >= 1000000 -> "%.1fM".format(num / 1000000f)
        num >= 1000 -> "%.1fK".format(num / 1000f)
        else -> num.toString()
    }
}

// ═════════════════════════════════════════════════════════════
// MOCK DATA
// ═════════════════════════════════════════════════════════════

data class CommunityTheme(
    val id: String,
    val name: String,
    val description: String,
    val previewColors: List<Long>,
    val creatorId: String,
    val creatorName: String,
    val creatorAvatar: String?,
    val downloadCount: Int,
    val likeCount: Int,
    val createdAt: Long
)

data class CommunityCreator(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val rank: Int,
    val totalDownloads: Int,
    val totalThemes: Int
)

private fun getMockTrendingThemes(): List<CommunityTheme> {
    return listOf(
        CommunityTheme(
            id = "theme_1",
            name = "Sunset Paradise",
            description = "Beautiful sunset gradient with warm colors",
            previewColors = listOf(0xFFFF6B6B, 0xFFFF8E53, 0xFFFFC107),
            creatorId = "user_1",
            creatorName = "ThemeMaster",
            creatorAvatar = null,
            downloadCount = 15420,
            likeCount = 3240,
            createdAt = System.currentTimeMillis() - 86400000 * 3
        ),
        CommunityTheme(
            id = "theme_2",
            name = "Ocean Deep",
            description = "Deep ocean blues with mysterious vibes",
            previewColors = listOf(0xFF0077B6, 0xFF00B4D8, 0xFF90E0EF),
            creatorId = "user_2",
            creatorName = "OceanLover",
            creatorAvatar = null,
            downloadCount = 12350,
            likeCount = 2890,
            createdAt = System.currentTimeMillis() - 86400000 * 5
        ),
        CommunityTheme(
            id = "theme_3",
            name = "Neon Nights",
            description = "Cyberpunk neon aesthetic",
            previewColors = listOf(0xFFFF00FF, 0xFF00FFFF, 0xFF7B2CBF),
            creatorId = "user_3",
            creatorName = "NeonDreamer",
            creatorAvatar = null,
            downloadCount = 10200,
            likeCount = 2150,
            createdAt = System.currentTimeMillis() - 86400000 * 2
        )
    )
}

private fun getMockRecentThemes(): List<CommunityTheme> {
    return listOf(
        CommunityTheme(
            id = "theme_r1",
            name = "Forest Mist",
            description = "Calm forest greens",
            previewColors = listOf(0xFF2D6A4F, 0xFF40916C, 0xFF74C69D),
            creatorId = "user_4",
            creatorName = "NatureFan",
            creatorAvatar = null,
            downloadCount = 520,
            likeCount = 89,
            createdAt = System.currentTimeMillis() - 3600000
        ),
        CommunityTheme(
            id = "theme_r2",
            name = "Berry Smoothie",
            description = "Sweet berry colors",
            previewColors = listOf(0xFFD63384, 0xFFE83E8C, 0xFFF783AC),
            creatorId = "user_5",
            creatorName = "BerrySweet",
            creatorAvatar = null,
            downloadCount = 340,
            likeCount = 67,
            createdAt = System.currentTimeMillis() - 7200000
        )
    )
}

private fun getMockTopCreators(): List<CommunityCreator> {
    return listOf(
        CommunityCreator(
            id = "user_1",
            name = "ThemeMaster",
            avatarUrl = null,
            rank = 1,
            totalDownloads = 125000,
            totalThemes = 45
        ),
        CommunityCreator(
            id = "user_2",
            name = "OceanLover",
            avatarUrl = null,
            rank = 2,
            totalDownloads = 98000,
            totalThemes = 32
        ),
        CommunityCreator(
            id = "user_3",
            name = "NeonDreamer",
            avatarUrl = null,
            rank = 3,
            totalDownloads = 87500,
            totalThemes = 28
        )
    )
}
