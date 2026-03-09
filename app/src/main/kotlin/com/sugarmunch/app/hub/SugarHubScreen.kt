package com.sugarmunch.app.hub

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.local.FolderEntity
import com.sugarmunch.app.data.local.FolderStyle
import com.sugarmunch.app.hub.components.*
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.ui.design.CardStyle
import com.sugarmunch.app.ui.design.SugarCard
import com.sugarmunch.app.ui.design.SugarDimens
import com.sugarmunch.app.ui.particles.ParticleSystem
import com.sugarmunch.app.ui.particles.ParticleType
import com.sugarmunch.app.ui.particles.EmitterShape
import kotlinx.coroutines.launch

/**
 * SugarHubScreen - Central dashboard for ALL apps organized by smart categories.
 * Features:
 * - Smart folder organization with auto-categorization
 * - Quick access to favorites, recent, and suggested apps
 * - Visual folder previews with animations
 * - Unified search across all apps
 * - Drag-drop folder assignment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SugarHubScreen(
    onAppClick: (String) -> Unit,
    onFolderClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: SugarHubViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State
    val uiState by viewModel.uiState.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val recentApps by viewModel.recentApps.collectAsState()
    val favoriteApps by viewModel.favoriteApps.collectAsState()
    val suggestedApps by viewModel.suggestedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsState()

    // Theme
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    // Animation states
    var isFolderExpanded by remember { mutableStateOf<String?>(null) }
    val reduceMotion = false

    // Particle effect state
    var showParticles by remember { mutableStateOf(false) }
    var particleOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        viewModel.loadHubData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated background gradient
        val infiniteTransition = rememberInfiniteTransition(label = "bg")
        val gradientOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "gradient"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            colors.surface,
                            colors.primary.copy(alpha = 0.1f),
                            colors.surface
                        ),
                        start = Offset(0f, gradientOffset * 1000),
                        end = Offset(1000f, (1 - gradientOffset) * 1000)
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                "SugarHub",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }
                    },
                    actions = {
                        // Search button
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colors.onSurface
                            )
                        }
                        // Quick settings
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Settings",
                                tint = colors.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = colors.onSurface
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.lg)
            ) {
                // Expanded search bar
                if (isSearchExpanded) {
                    item {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.search(it) },
                            results = uiState.searchResults,
                            onAppClick = onAppClick,
                            colors = colors,
                            modifier = Modifier.padding(horizontal = SugarDimens.Spacing.md)
                        )
                    }
                }

                // Quick Access Section
                if (!isSearchExpanded) {
                    item {
                        QuickAccessSection(
                            recentApps = recentApps.take(5),
                            favoriteApps = favoriteApps.take(5),
                            suggestedApps = suggestedApps.take(5),
                            onAppClick = onAppClick,
                            colors = colors,
                            reduceMotion = reduceMotion
                        )
                    }

                    // Smart Categories
                    item {
                        Text(
                            "Smart Folders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            modifier = Modifier.padding(
                                horizontal = SugarDimens.Spacing.md,
                                vertical = SugarDimens.Spacing.sm
                            )
                        )
                    }

                    // Folder Grid
                    item {
                        FolderGrid(
                            folders = folders,
                            onFolderClick = { folder ->
                                isFolderExpanded = if (isFolderExpanded == folder.id) null else folder.id
                            },
                            onAppClick = onAppClick,
                            expandedFolderId = isFolderExpanded,
                            colors = colors,
                            reduceMotion = reduceMotion
                        )
                    }

                    // All Apps Section
                    item {
                        AllAppsSection(
                            apps = uiState.allApps,
                            onAppClick = onAppClick,
                            colors = colors,
                            reduceMotion = reduceMotion
                        )
                    }
                }
            }
        }

        // Particle overlay effect
        if (showParticles) {
            ParticleSystem(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f),
                particleType = ParticleType.CANDY,
                emitterShape = EmitterShape.POINT,
                emitterPosition = particleOffset,
                intensity = 0.8f
            )
        }
    }
}

@Composable
private fun QuickAccessSection(
    recentApps: List<AppEntry>,
    favoriteApps: List<AppEntry>,
    suggestedApps: List<AppEntry>,
    onAppClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    reduceMotion: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
    ) {
        // Quick Access Row
        if (recentApps.isNotEmpty()) {
            QuickAccessRow(
                title = "Recent",
                icon = Icons.Default.History,
                apps = recentApps,
                onAppClick = onAppClick,
                colors = colors,
                reduceMotion = reduceMotion
            )
        }

        if (favoriteApps.isNotEmpty()) {
            QuickAccessRow(
                title = "Favorites",
                icon = Icons.Default.Star,
                apps = favoriteApps,
                onAppClick = onAppClick,
                colors = colors,
                reduceMotion = reduceMotion
            )
        }

        if (suggestedApps.isNotEmpty()) {
            QuickAccessRow(
                title = "Suggested",
                icon = Icons.Default.Lightbulb,
                apps = suggestedApps,
                onAppClick = onAppClick,
                colors = colors,
                reduceMotion = reduceMotion
            )
        }
    }
}

@Composable
private fun QuickAccessRow(
    title: String,
    icon: ImageVector,
    apps: List<AppEntry>,
    onAppClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    reduceMotion: Boolean
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = SugarDimens.Spacing.md)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
            contentPadding = PaddingValues(horizontal = SugarDimens.Spacing.md)
        ) {
            items(apps) { app ->
                QuickAccessAppCard(
                    app = app,
                    onClick = { onAppClick(app.id) },
                    colors = colors,
                    reduceMotion = reduceMotion
                )
            }
        }
    }
}

@Composable
private fun QuickAccessAppCard(
    app: AppEntry,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    reduceMotion: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    val accentColor = app.accentColor?.let { parseColorSafe(it) } ?: colors.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(if (reduceMotion) 1f else scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(SugarDimens.Spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(SugarDimens.Radius.md))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.2f),
                            accentColor.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(SugarDimens.Radius.md)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.name.first().toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.name,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dp)
        )
    }
}

@Composable
private fun FolderGrid(
    folders: List<FolderEntity>,
    onFolderClick: (FolderEntity) -> Unit,
    onAppClick: (String) -> Unit,
    expandedFolderId: String?,
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    reduceMotion: Boolean
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp)
            .padding(horizontal = SugarDimens.Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
    ) {
        items(folders) { folder ->
            FolderCard(
                folder = folder,
                isExpanded = expandedFolderId == folder.id,
                onClick = { onFolderClick(folder) },
                onAppClick = onAppClick,
                colors = colors,
                reduceMotion = reduceMotion
            )
        }
    }
}

@Composable
private fun FolderCard(
    folder: FolderEntity,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onAppClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    reduceMotion: Boolean
) {
    val folderStyle = try {
        FolderStyle.valueOf(folder.folderStyle)
    } catch (e: IllegalArgumentException) {
        FolderStyle.DEFAULT
    }

    val accentColor = folder.iconColor?.let { parseColorSafe(it) } ?: colors.primary

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    val cardModifier = Modifier
        .fillMaxWidth()
        .scale(if (reduceMotion) 1f else scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )

    val cardStyle = when (folderStyle) {
        FolderStyle.GLASSMORPHIC -> CardStyle.GLASSMORPHIC
        FolderStyle.NEON -> CardStyle.NEON
        FolderStyle.HOLOGRAPHIC -> CardStyle.HOLOGRAPHIC
        FolderStyle.LIQUID -> CardStyle.LIQUID
        FolderStyle.CRYSTAL -> CardStyle.CRYSTAL
        else -> CardStyle.ELEVATED
    }

    SugarCard(
        modifier = cardModifier,
        style = cardStyle,
        onClick = null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Folder icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${folder.appIds.size} apps",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }

            // Badge for notification count or new apps
            if (folder.appIds.isNotEmpty()) {
                Badge(
                    containerColor = accentColor,
                    modifier = Modifier.size(20.dp)
                ) {
                    Text(
                        folder.appIds.size.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Expanded folder preview
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                // Show mini app icons preview
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(minOf(4, folder.appIds.size)) { index ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(SugarDimens.Radius.xs))
                                .background(colors.surfaceVariant.copy(alpha = 0.5f))
                        )
                    }
                    if (folder.appIds.size > 4) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(SugarDimens.Radius.xs))
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "+${folder.appIds.size - 4}",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllAppsSection(
    apps: List<AppEntry>,
    onAppClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    reduceMotion: Boolean
) {
    Column {
        Text(
            "All Apps",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(
                horizontal = SugarDimens.Spacing.md,
                vertical = SugarDimens.Spacing.sm
            )
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
            contentPadding = PaddingValues(horizontal = SugarDimens.Spacing.md)
        ) {
            items(apps.chunked(10)) { appChunk ->
                // App chunk card
                SugarCard(
                    modifier = Modifier.width(160.dp),
                    style = CardStyle.ELEVATED
                ) {
                    appChunk.forEach { app ->
                        AppListItem(
                            app = app,
                            onClick = { onAppClick(app.id) },
                            colors = colors
                        )
                    }
                    if (apps.size > 10) {
                        TextButton(
                            onClick = { /* Show all apps */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "View All (${apps.size})",
                                color = colors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppListItem(
    app: AppEntry,
    onClick: () -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors
) {
    val accentColor = app.accentColor?.let { parseColorSafe(it) } ?: colors.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                app.name.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            app.name,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<AppEntry>,
    onAppClick: (String) -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search all apps...") },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = colors.primary)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, "Clear", tint = colors.onSurface)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                cursorColor = colors.primary
            ),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        )

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(results) { app ->
                    AppListItem(
                        app = app,
                        onClick = { onAppClick(app.id) },
                        colors = colors
                    )
                }
            }
        }
    }
}

// Helper function to safely parse color strings
private fun parseColorSafe(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color(0xFFE91E63) // Default pink
    }
}
