package com.sugarmunch.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sugarmunch.app.plugin.model.PluginCategory
import com.sugarmunch.app.plugin.store.InstallStatus
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.ui.viewmodels.PluginStoreViewModel
import com.sugarmunch.app.ui.viewmodels.SortBy
import kotlinx.coroutines.launch

/**
 * Plugin Store Screen with Folder Integration
 * 
 * Features:
 * - Browse available plugins with real data from PluginStore
 * - Search and filter by category
 * - Install, update, uninstall plugins
 * - Rate and review plugins
 * - Add plugins to folders
 * - Show installed plugins with folder badges
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginStoreScreen(
    viewModel: PluginStoreViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    folderRepository: com.sugarmunch.app.data.repository.FolderRepository,
    onNavigateBack: () -> Unit,
    onAddToFolder: (String, String) -> Unit // pluginId, folderId
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Theme integration
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    // Collect state from ViewModel
    val filteredPlugins by viewModel.filteredPlugins.collectAsState()
    val featuredPlugins by viewModel.featuredPlugins.collectAsState()
    val topRatedPlugins by viewModel.topRatedPlugins.collectAsState()
    val newPlugins by viewModel.newPlugins.collectAsState()
    val installedPlugins by viewModel.installedPlugins.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val installStatus by viewModel.installStatus.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedPlugin by viewModel.selectedPlugin.collectAsState()

    // Collect folders
    val folders by folderRepository.getAllFolders().collectAsState(initial = emptyList())

    // Search text field state
    var searchTextFieldValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Plugin Store",
                        color = colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                actions = {
                    // Updates badge
                    if (uiState.availableUpdates.isNotEmpty()) {
                        IconButton(onClick = { /* Show updates */ }) {
                            BadgeBox(count = uiState.availableUpdates.size) {
                                Icon(
                                    Icons.Outlined.Update,
                                    contentDescription = "Updates available",
                                    tint = colors.onSurface
                                )
                            }
                        }
                    }
                    // Refresh button
                    IconButton(onClick = { viewModel.refreshPlugins() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Dynamic theme background
            AnimatedThemeBackground()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.85f)
                            )
                        )
                    ),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                item {
                    SearchBar(
                        query = searchTextFieldValue,
                        onQueryChange = {
                            searchTextFieldValue = it
                            viewModel.searchPlugins(it)
                        },
                        colors = colors
                    )
                }

                // Category Filter Chips
                item {
                    CategoryFilterRow(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.filterByCategory(it) },
                        colors = colors
                    )
                }

                // Sort Options
                item {
                    SortOptionsRow(
                        sortBy = sortBy,
                        onSortSelected = { viewModel.sortBy(it) },
                        colors = colors
                    )
                }

                // Featured Plugins (Horizontal Scroll)
                if (featuredPlugins.isNotEmpty() && searchQuery.isBlank() && selectedCategory == null) {
                    item {
                        PluginSection(
                            title = "Featured",
                            icon = Icons.Default.Star,
                            colors = colors
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(featuredPlugins) { plugin ->
                                    FeaturedPluginCard(
                                        plugin = plugin,
                                        isInstalled = installedPlugins.contains(plugin.id),
                                        installStatus = installStatus[plugin.id] ?: InstallStatus.NOT_INSTALLED,
                                        downloadProgress = downloadProgress[plugin.id] ?: 0f,
                                        onInstallClick = { viewModel.installPlugin(plugin.id) },
                                        onUninstallClick = { viewModel.uninstallPlugin(plugin.id) },
                                        onUpdateClick = { viewModel.updatePlugin(plugin.id) },
                                        onClick = { viewModel.selectPlugin(plugin) },
                                        onAddToFolder = { folderId ->
                                            viewModel.addToFolder(plugin.id, folderId)
                                            onAddToFolder(plugin.id, folderId)
                                        },
                                        folders = folders,
                                        pluginFolderIds = viewModel.getFoldersForPlugin(plugin.id),
                                        colors = colors
                                    )
                                }
                            }
                        }
                    }
                }

                // Top Rated Plugins (Horizontal Scroll)
                if (topRatedPlugins.isNotEmpty() && searchQuery.isBlank() && selectedCategory == null) {
                    item {
                        PluginSection(
                            title = "Top Rated",
                            icon = Icons.Outlined.Star,
                            colors = colors
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(topRatedPlugins) { plugin ->
                                    FeaturedPluginCard(
                                        plugin = plugin,
                                        isInstalled = installedPlugins.contains(plugin.id),
                                        installStatus = installStatus[plugin.id] ?: InstallStatus.NOT_INSTALLED,
                                        downloadProgress = downloadProgress[plugin.id] ?: 0f,
                                        onInstallClick = { viewModel.installPlugin(plugin.id) },
                                        onUninstallClick = { viewModel.uninstallPlugin(plugin.id) },
                                        onUpdateClick = { viewModel.updatePlugin(plugin.id) },
                                        onClick = { viewModel.selectPlugin(plugin) },
                                        onAddToFolder = { folderId ->
                                            viewModel.addToFolder(plugin.id, folderId)
                                            onAddToFolder(plugin.id, folderId)
                                        },
                                        folders = folders,
                                        pluginFolderIds = viewModel.getFoldersForPlugin(plugin.id),
                                        colors = colors
                                    )
                                }
                            }
                        }
                    }
                }

                // New Releases (Horizontal Scroll)
                if (newPlugins.isNotEmpty() && searchQuery.isBlank() && selectedCategory == null) {
                    item {
                        PluginSection(
                            title = "New Releases",
                            icon = Icons.Default.NewReleases,
                            colors = colors
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(newPlugins) { plugin ->
                                    FeaturedPluginCard(
                                        plugin = plugin,
                                        isInstalled = installedPlugins.contains(plugin.id),
                                        installStatus = installStatus[plugin.id] ?: InstallStatus.NOT_INSTALLED,
                                        downloadProgress = downloadProgress[plugin.id] ?: 0f,
                                        onInstallClick = { viewModel.installPlugin(plugin.id) },
                                        onUninstallClick = { viewModel.uninstallPlugin(plugin.id) },
                                        onUpdateClick = { viewModel.updatePlugin(plugin.id) },
                                        onClick = { viewModel.selectPlugin(plugin) },
                                        onAddToFolder = { folderId ->
                                            viewModel.addToFolder(plugin.id, folderId)
                                            onAddToFolder(plugin.id, folderId)
                                        },
                                        folders = folders,
                                        pluginFolderIds = viewModel.getFoldersForPlugin(plugin.id),
                                        colors = colors
                                    )
                                }
                            }
                        }
                    }
                }

                // All Plugins Grid
                item {
                    PluginSection(
                        title = if (selectedCategory != null) "${selectedCategory?.name} Plugins" else "All Plugins",
                        icon = Icons.Default.Apps,
                        colors = colors
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Plugin Grid
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredPlugins) { plugin ->
                            PluginCard(
                                plugin = plugin,
                                isInstalled = installedPlugins.contains(plugin.id),
                                installStatus = installStatus[plugin.id] ?: InstallStatus.NOT_INSTALLED,
                                downloadProgress = downloadProgress[plugin.id] ?: 0f,
                                onInstallClick = { viewModel.installPlugin(plugin.id) },
                                onUninstallClick = { viewModel.uninstallPlugin(plugin.id) },
                                onUpdateClick = { viewModel.updatePlugin(plugin.id) },
                                onClick = { viewModel.selectPlugin(plugin) },
                                onAddToFolder = { folderId ->
                                    viewModel.addToFolder(plugin.id, folderId)
                                    onAddToFolder(plugin.id, folderId)
                                },
                                folders = folders,
                                pluginFolderIds = viewModel.getFoldersForPlugin(plugin.id),
                                userRating = viewModel.getUserRating(plugin.id),
                                colors = colors
                            )
                        }
                    }
                }

                // Empty state
                if (filteredPlugins.isEmpty()) {
                    item {
                        EmptyState(
                            message = if (searchQuery.isNotEmpty()) {
                                "No plugins found for \"$searchQuery\""
                            } else {
                                "No plugins available"
                            },
                            colors = colors
                        )
                    }
                }
            }

            // Plugin Details Dialog
            if (uiState.showPluginDetails && selectedPlugin != null) {
                PluginDetailsDialog(
                    plugin = selectedPlugin!!,
                    isInstalled = installedPlugins.contains(selectedPlugin!!.id),
                    installStatus = installStatus[selectedPlugin!!.id] ?: InstallStatus.NOT_INSTALLED,
                    downloadProgress = downloadProgress[selectedPlugin!!.id] ?: 0f,
                    userRating = viewModel.getUserRating(selectedPlugin!!.id),
                    folders = folders,
                    pluginFolderIds = viewModel.getFoldersForPlugin(selectedPlugin!!.id),
                    onDismiss = { viewModel.dismissPluginDetails() },
                    onInstallClick = { viewModel.installPlugin(selectedPlugin!!.id) },
                    onUninstallClick = { viewModel.uninstallPlugin(selectedPlugin!!.id) },
                    onUpdateClick = { viewModel.updatePlugin(selectedPlugin!!.id) },
                    onRatePlugin = { rating -> viewModel.ratePlugin(selectedPlugin!!.id, rating) },
                    onAddToFolder = { folderId ->
                        viewModel.addToFolder(selectedPlugin!!.id, folderId)
                        onAddToFolder(selectedPlugin!!.id, folderId)
                    },
                    colors = colors
                )
            }
        }
    }
}

/**
 * Search Bar composable
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search plugins...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.onSurface.copy(alpha = 0.3f)
        ),
        singleLine = true
    )
}

/**
 * Category filter chips row
 */
@Composable
private fun CategoryFilterRow(
    categories: List<PluginCategory>,
    selectedCategory: PluginCategory?,
    onCategorySelected: (PluginCategory?) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary,
                    selectedLabelColor = colors.onPrimary
                )
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary,
                    selectedLabelColor = colors.onPrimary
                )
            )
        }
    }
}

/**
 * Sort options row
 */
@Composable
private fun SortOptionsRow(
    sortBy: SortBy,
    onSortSelected: (SortBy) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Sort by:",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurface.copy(alpha = 0.7f)
        )
        SortBy.entries.forEach { sort ->
            AssistChip(
                onClick = { onSortSelected(sort) },
                label = { Text(sort.name) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (sortBy == sort) colors.primary else colors.surface,
                    labelColor = if (sortBy == sort) colors.onPrimary else colors.onSurface
                )
            )
        }
    }
}

/**
 * Plugin section header
 */
@Composable
private fun PluginSection(
    title: String,
    icon: ImageVector,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

/**
 * Badge box for showing counts on icons
 */
@Composable
private fun BadgeBox(count: Int, content: @Composable () -> Unit) {
    Box {
        content()
        if (count > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
            ) {
                Text(count.toString())
            }
        }
    }
}

/**
 * Empty state composable
 */
@Composable
private fun EmptyState(
    message: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = colors.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// Plugin card components will be in a separate file
// These are forward declarations for the components defined below
