package com.sugarmunch.app.ui.screens.plugin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.plugin.model.*
import com.sugarmunch.app.plugin.store.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

/**
 * SugarMunch Plugin Detail Screen
 * 
 * Shows detailed information about a plugin including:
 * - Screenshots and preview
 * - Description and features
 * - Reviews and ratings
 * - Changelog
 * - Permissions
 * - Developer info
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginDetailScreen(
    pluginId: String,
    onBack: () -> Unit,
    onDeveloperClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val pluginStore = remember { PluginStore.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    // Theme
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    // Plugin data
    val plugin = pluginStore.getPluginDetails(pluginId)
    
    // Install status
    val installStatus by pluginStore.installStatus.collectAsState()
    val currentStatus = installStatus[pluginId] ?: InstallStatus.NOT_INSTALLED
    
    // Download progress
    val downloadProgress by pluginStore.downloadProgress.collectAsState()
    val progress = downloadProgress[pluginId] ?: 0f
    
    // User rating
    val userRating = plugin?.let { pluginStore.getUserRating(it.id) }
    
    // Reviews
    var reviews by remember { mutableStateOf<List<PluginReview>>(emptyList()) }
    
    LaunchedEffect(pluginId) {
        plugin?.let {
            pluginStore.getPluginReviews(it.id).onSuccess {
                reviews = it
            }
        }
    }
    
    if (plugin == null) {
        // Plugin not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Plugin not found",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.error
                )
            }
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plugin Details", color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share plugin */ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = colors.onSurface
                        )
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            BottomActionBar(
                plugin = plugin,
                installStatus = currentStatus,
                downloadProgress = progress,
                colors = colors,
                onInstall = {
                    scope.launch {
                        pluginStore.installPlugin(plugin.id)
                    }
                },
                onUninstall = {
                    scope.launch {
                        pluginStore.uninstallPlugin(plugin.id)
                    }
                },
                onUpdate = {
                    scope.launch {
                        pluginStore.updatePlugin(plugin.id)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header section with icon and basic info
                item {
                    PluginHeader(
                        plugin = plugin,
                        userRating = userRating,
                        colors = colors,
                        onRate = { rating ->
                            scope.launch {
                                pluginStore.ratePlugin(plugin.id, rating)
                            }
                        }
                    )
                }
                
                // Screenshots
                if (plugin.screenshots.isNotEmpty()) {
                    item {
                        ScreenshotsSection(
                            screenshots = plugin.screenshots,
                            colors = colors
                        )
                    }
                }
                
                // Description
                item {
                    DescriptionSection(
                        description = plugin.description,
                        colors = colors
                    )
                }
                
                // Tags
                item {
                    TagsSection(
                        tags = plugin.tags,
                        colors = colors
                    )
                }
                
                // Stats row
                item {
                    StatsSection(
                        plugin = plugin,
                        colors = colors
                    )
                }
                
                // Divider
                item {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = colors.surfaceVariant
                    )
                }
                
                // Changelog
                item {
                    ChangelogSection(
                        changelog = plugin.changelog,
                        colors = colors
                    )
                }
                
                // Divider
                item {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = colors.surfaceVariant
                    )
                }
                
                // Reviews
                item {
                    ReviewsSection(
                        reviews = reviews,
                        rating = plugin.rating,
                        ratingCount = plugin.ratingCount,
                        colors = colors
                    )
                }
                
                // Developer info
                item {
                    DeveloperSection(
                        plugin = plugin,
                        colors = colors,
                        onDeveloperClick = { onDeveloperClick(plugin.authorId) }
                    )
                }
                
                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun PluginHeader(
    plugin: StorePlugin,
    userRating: Int?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onRate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plugin icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(colors.primary, colors.secondary)
                        ),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Extension,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Plugin info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plugin.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                
                Text(
                    "v${plugin.version} • ${plugin.category.name.replace("_", " ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rating stars
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRating(
                        rating = plugin.rating,
                        starColor = Color(0xFFFFB800),
                        emptyStarColor = colors.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${plugin.rating} (${plugin.formattedDownloads})",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // User rating
        if (userRating != null) {
            Surface(
                color = colors.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.success,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "You rated this ${userRating} stars",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface
                    )
                }
            }
        } else {
            // Rate prompt
            Surface(
                color = colors.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        "Rate this plugin",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InteractiveStarRating(
                        onRate = onRate,
                        starColor = Color(0xFFFFB800),
                        emptyStarColor = colors.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StarRating(
    rating: Float,
    starColor: Color,
    emptyStarColor: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            val starValue = index + 1
            val icon = when {
                rating >= starValue -> Icons.Default.Star
                rating >= starValue - 0.5f -> Icons.Default.StarHalf
                else -> Icons.Outlined.StarOutline
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (rating >= starValue - 0.5f) starColor else emptyStarColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun InteractiveStarRating(
    onRate: (Int) -> Unit,
    starColor: Color,
    emptyStarColor: Color
) {
    var hoverRating by remember { mutableStateOf(0) }
    
    Row {
        repeat(5) { index ->
            val starValue = index + 1
            IconButton(
                onClick = { onRate(starValue) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (starValue <= hoverRating) Icons.Default.Star else Icons.Outlined.StarOutline,
                    contentDescription = "$starValue stars",
                    tint = if (starValue <= hoverRating) starColor else emptyStarColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ScreenshotsSection(
    screenshots: List<String>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            "Screenshots",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(screenshots) { screenshot ->
                ScreenshotCard(screenshotUrl = screenshot, colors = colors)
            }
        }
    }
}

@Composable
private fun ScreenshotCard(
    screenshotUrl: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(360.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder - would load actual image
            Icon(
                Icons.Default.Image,
                contentDescription = "Screenshot",
                tint = colors.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun DescriptionSection(
    description: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface.copy(alpha = 0.8f),
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = if (expanded) androidx.compose.ui.text.style.TextOverflow.Visible else androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        
        if (description.length > 100) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (expanded) "Show Less" else "Read More")
            }
        }
    }
}

@Composable
private fun TagsSection(
    tags: List<String>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                color = colors.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "#$tag",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun StatsSection(
    plugin: StorePlugin,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            icon = Icons.Default.Download,
            value = plugin.formattedDownloads,
            label = "Downloads",
            colors = colors
        )
        StatItem(
            icon = Icons.Default.Star,
            value = "${plugin.rating}",
            label = "Rating",
            colors = colors
        )
        StatItem(
            icon = Icons.Default.Storage,
            value = plugin.formattedSize,
            label = "Size",
            colors = colors
        )
        StatItem(
            icon = Icons.Default.Update,
            value = formatDate(plugin.publishDate),
            label = "Updated",
            colors = colors
        )
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ChangelogSection(
    changelog: List<ChangelogEntry>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    var expanded by remember { mutableStateOf(false) }
    val displayEntries = if (expanded) changelog else changelog.take(2)
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            "What's New",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        displayEntries.forEach { entry ->
            ChangelogItem(entry = entry, colors = colors)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (changelog.size > 2) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (expanded) "Show Less" else "View Full Changelog")
            }
        }
    }
}

@Composable
private fun ChangelogItem(
    entry: ChangelogEntry,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(colors.primary, CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                "Version ${entry.version}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = colors.primary
            )
            Text(
                entry.changes,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.8f)
            )
            Text(
                formatDate(entry.date),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ReviewsSection(
    reviews: List<PluginReview>,
    rating: Float,
    ratingCount: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Reviews",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Text(
                "$ratingCount reviews",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (reviews.isEmpty()) {
            Text(
                "No reviews yet. Be the first to review!",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            reviews.take(3).forEach { review ->
                ReviewItem(review = review, colors = colors)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (reviews.size > 3) {
                OutlinedButton(
                    onClick = { /* View all reviews */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View All Reviews")
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(
    review: PluginReview,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(colors.primary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            review.userName.first().toString(),
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        review.userName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface
                    )
                }
                
                StarRating(
                    rating = review.rating.toFloat(),
                    starColor = Color(0xFFFFB800),
                    emptyStarColor = colors.surfaceVariant
                )
            }
            
            if (review.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    review.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                formatDate(review.date),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun DeveloperSection(
    plugin: StorePlugin,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onDeveloperClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            "Developer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            onClick = onDeveloperClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(colors.secondary, colors.tertiary)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        plugin.author.first().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        plugin.author,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface
                    )
                    Text(
                        "View all plugins by this developer",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    plugin: StorePlugin,
    installStatus: InstallStatus,
    downloadProgress: Float,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
    onUpdate: () -> Unit
) {
    Surface(
        color = colors.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (installStatus) {
                InstallStatus.NOT_INSTALLED -> {
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INSTALL (${plugin.formattedSize})")
                    }
                }
                
                InstallStatus.DOWNLOADING -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = downloadProgress,
                            modifier = Modifier.fillMaxWidth(),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Downloading... ${(downloadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                InstallStatus.INSTALLING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Installing...")
                    }
                }
                
                InstallStatus.INSTALLED -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onUninstall,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("UNINSTALL")
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Button(
                            onClick = { /* Open plugin settings */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.success
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OPEN")
                        }
                    }
                }
                
                InstallStatus.UPDATING -> {
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UPDATING...")
                    }
                }
                
                InstallStatus.ERROR -> {
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RETRY INSTALL")
                    }
                }
            }
        }
    }
}

// Helper functions
private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 24 * 60 * 60 * 1000 -> "Today"
        diff < 48 * 60 * 60 * 1000 -> "Yesterday"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        diff < 30L * 24 * 60 * 60 * 1000 -> "${diff / (7 * 24 * 60 * 60 * 1000)} weeks ago"
        diff < 365L * 24 * 60 * 60 * 1000 -> "${diff / (30L * 24 * 60 * 60 * 1000)} months ago"
        else -> "${diff / (365L * 24 * 60 * 60 * 1000)} years ago"
    }
}

/**
 * A simple FlowRow implementation for tags
 */
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hGapPx = 0
        val vGapPx = 0
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val itemWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)

            if (currentRow.isNotEmpty() && currentRowWidth + hGapPx + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                itemWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }

            currentRow.add(placeable)
            currentRowWidth += if (currentRow.size == 1) placeable.width else hGapPx + placeable.width
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            itemWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        val width = itemWidths.maxOrNull()?.coerceIn(constraints.minWidth, constraints.maxWidth)
            ?: constraints.minWidth
        val height = rowHeights.sumOf { it } + (rowHeights.size - 1).coerceAtLeast(0) * vGapPx

        layout(width, height) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = when (horizontalArrangement) {
                    Arrangement.End -> width - itemWidths[rowIndex]
                    Arrangement.Center -> (width - itemWidths[rowIndex]) / 2
                    else -> 0
                }

                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + hGapPx
                }

                y += rowHeights[rowIndex] + vGapPx
            }
        }
    }
}
