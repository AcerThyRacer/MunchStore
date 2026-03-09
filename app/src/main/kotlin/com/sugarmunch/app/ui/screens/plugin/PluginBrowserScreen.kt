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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.plugin.model.*
import com.sugarmunch.app.plugin.store.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.launch

/**
 * SugarMunch Plugin Browser Screen
 * 
 * Browse, search, and install plugins from the store.
 * Features category filters, search, featured plugins, and more.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginBrowserScreen(
    onBack: () -> Unit,
    onPluginClick: (String) -> Unit
) {
    val context = LocalContext.current
    val pluginStore = remember { PluginStore.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    // Theme
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    
    // Category filter
    var selectedCategory by remember { mutableStateOf<PluginCategory?>(null) }
    
    // Plugins
    val featuredPlugins by pluginStore.featuredPlugins.collectAsState()
    val topRatedPlugins by pluginStore.topRatedPlugins.collectAsState()
    val newPlugins by pluginStore.newPlugins.collectAsState()
    
    // Search results
    val searchResults by remember(searchQuery, selectedCategory) {
        pluginStore.searchPlugins(searchQuery, selectedCategory)
    }.collectAsState(initial = emptyList())
    
    val displayedPlugins = if (searchQuery.isNotBlank() || selectedCategory != null) {
        searchResults
    } else null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        SearchTextField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = { isSearching = false },
                            colors = colors
                        )
                    } else {
                        Column {
                            Text("Plugin Store", color = colors.onSurface)
                            Text(
                                "Discover amazing effects & themes",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.primary
                            )
                        }
                    }
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
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Close Search" else "Search",
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
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category filters
                item {
                    CategoryFilterRow(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        colors = colors
                    )
                }
                
                // Show search results if searching
                if (displayedPlugins != null) {
                    item {
                        Text(
                            "${displayedPlugins.size} results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(displayedPlugins.chunked(2)) { rowPlugins ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowPlugins.forEach { plugin ->
                                PluginCard(
                                    plugin = plugin,
                                    colors = colors,
                                    installStatus = pluginStore.getInstallStatus(plugin.id),
                                    onClick = { onPluginClick(plugin.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowPlugins.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    // Featured Plugins Section
                    if (featuredPlugins.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "✨ Featured",
                                colors = colors,
                                onSeeAllClick = { /* Navigate to featured list */ }
                            )
                        }
                        
                        item {
                            FeaturedPluginsCarousel(
                                plugins = featuredPlugins,
                                colors = colors,
                                onPluginClick = onPluginClick
                            )
                        }
                    }
                    
                    // Top Rated Section
                    if (topRatedPlugins.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "⭐ Top Rated",
                                colors = colors,
                                onSeeAllClick = { /* Navigate to top rated list */ }
                            )
                        }
                        
                        items(topRatedPlugins.take(4).chunked(2)) { rowPlugins ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowPlugins.forEach { plugin ->
                                    PluginCard(
                                        plugin = plugin,
                                        colors = colors,
                                        installStatus = pluginStore.getInstallStatus(plugin.id),
                                        onClick = { onPluginClick(plugin.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowPlugins.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    
                    // New Plugins Section
                    if (newPlugins.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "🆕 New & Hot",
                                colors = colors,
                                onSeeAllClick = { /* Navigate to new plugins list */ }
                            )
                        }
                        
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(newPlugins) { plugin ->
                                    NewPluginCard(
                                        plugin = plugin,
                                        colors = colors,
                                        onClick = { onPluginClick(plugin.id) }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Categories Section
                    item {
                        SectionHeader(
                            title = "📂 Browse by Category",
                            colors = colors,
                            showSeeAll = false
                        )
                    }
                    
                    item {
                        CategoriesGrid(
                            onCategoryClick = { selectedCategory = it },
                            colors = colors
                        )
                    }
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
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colors.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (query.isEmpty()) {
                    Text(
                        "Search plugins...",
                        color = colors.onSurface.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: PluginCategory?,
    onCategorySelected: (PluginCategory?) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val categories = listOf(null) + PluginCategory.values().toList()
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { 
                    Text(
                        category?.name?.replace("_", " ") ?: "All",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = if (selectedCategory == category) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary,
                    selectedLabelColor = colors.onPrimary
                )
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    showSeeAll: Boolean = true,
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        
        if (showSeeAll) {
            TextButton(onClick = onSeeAllClick) {
                Text("See All", color = colors.primary)
            }
        }
    }
}

@Composable
private fun FeaturedPluginsCarousel(
    plugins: List<StorePlugin>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onPluginClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        plugins.take(5).forEach { plugin ->
            FeaturedPluginCard(
                plugin = plugin,
                colors = colors,
                onClick = { onPluginClick(plugin.id) }
            )
        }
    }
}

@Composable
private fun FeaturedPluginCard(
    plugin: StorePlugin,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    val gradientColors = listOf(
        colors.primary.copy(alpha = 0.8f),
        colors.secondary.copy(alpha = 0.6f)
    )
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(160.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
        ) {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Category badge
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        plugin.category.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Info
                Column {
                    Text(
                        plugin.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        plugin.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Rating badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                color = Color(0xFFFFB800),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "${plugin.rating}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PluginCard(
    plugin: StorePlugin,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    installStatus: InstallStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Icon and rating row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Plugin icon placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(colors.primary, colors.secondary)
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Extension,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "${plugin.rating}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Name
            Text(
                plugin.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Author
            Text(
                plugin.author,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bottom row with downloads and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = colors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        plugin.formattedDownloads,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Install status indicator
                when (installStatus) {
                    InstallStatus.INSTALLED -> {
                        Surface(
                            color = colors.success.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "INSTALLED",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.success,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    InstallStatus.UPDATING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colors.primary,
                            strokeWidth = 2.dp
                        )
                    }
                    else -> {
                        Text(
                            plugin.formattedSize,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewPluginCard(
    plugin: StorePlugin,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(colors.tertiary, colors.accent)
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NewReleases,
                    contentDescription = null,
                    tint = colors.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                plugin.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                "v${plugin.version}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary
            )
        }
    }
}

@Composable
private fun CategoriesGrid(
    onCategoryClick: (PluginCategory) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    val categories = PluginCategory.values()
    val categoryColors = listOf(
        Color(0xFFFF6B6B),  // EFFECTS - Red
        Color(0xFF4ECDC4),  // THEMES - Teal
        Color(0xFFFFE66D),  // TOOLS - Yellow
        Color(0xFF95E1D3),  // WIDGETS - Mint
        Color(0xFFA8E6CF),  // OVERLAYS - Light Green
        Color(0xFFFFD93D),  // INTEGRATIONS - Gold
        Color(0xFF6C5CE7)   // MISC - Purple
    )
    
    val categoryIcons = listOf(
        Icons.Default.AutoAwesome,      // EFFECTS
        Icons.Default.Palette,          // THEMES
        Icons.Default.Build,            // TOOLS
        Icons.Default.Widgets,          // WIDGETS
        Icons.Default.Layers,           // OVERLAYS
        Icons.Default.Extension,        // INTEGRATIONS
        Icons.Default.MoreHoriz         // MISC
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.chunked(2).forEachIndexed { rowIndex, rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowCategories.forEachIndexed { index, category ->
                    val colorIndex = rowIndex * 2 + index
                    CategoryCard(
                        category = category,
                        color = categoryColors[colorIndex % categoryColors.size],
                        icon = categoryIcons[colorIndex % categoryIcons.size],
                        colors = colors,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: PluginCategory,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                category.name.replace("_", " "),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }
    }
}
