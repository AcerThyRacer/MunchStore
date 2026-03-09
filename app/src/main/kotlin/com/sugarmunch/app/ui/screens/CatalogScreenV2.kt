package com.sugarmunch.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.CandyTrailEntry
import com.sugarmunch.app.data.LocalPreferencesRepository
import com.sugarmunch.app.phaseone.PhaseOneUtilities
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.components.rememberReduceMotion
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.trails.TrailProgressManager
import com.sugarmunch.app.clan.ClanGoalsManager
import com.sugarmunch.app.ui.components.*

enum class CatalogViewMode {
    LIST, GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreenV2(
    onAppClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onThemeClick: () -> Unit,
    onEffectsClick: () -> Unit,
    onShopClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onTrailClick: (CandyTrailEntry) -> Unit = {},
    onClanClick: () -> Unit = {},
    onConfectionerClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = LocalPreferencesRepository.current
    val viewModel: CatalogViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val filterFeaturedOnly by viewModel.filterFeaturedOnly.collectAsState()
    val gridColumns by prefs.catalogGridColumns.collectAsState(initial = 2)
    val defaultView by prefs.catalogDefaultView.collectAsState(initial = "list")
    val cardStyle by prefs.catalogCardStyle.collectAsState(initial = "default")
    val appAccentOverrides by prefs.getAppAccentOverrides().collectAsState(initial = emptyMap())
    val appThemeOverrides by prefs.getAppThemeOverrides().collectAsState(initial = emptyMap())
    val trails by viewModel.trails.collectAsState()
    val trailProgressManager = remember { TrailProgressManager.getInstance(context) }
    val completedTrailIds by trailProgressManager.completedTrails.collectAsState(initial = emptySet())
    val clanGoalsManager = remember { ClanGoalsManager.getInstance(context) }
    val featuredClan by clanGoalsManager.featuredWinningClan.collectAsState(initial = null)
    var isSearchExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var viewMode by remember(defaultView) { mutableStateOf(if (defaultView == "grid") CatalogViewMode.GRID else CatalogViewMode.LIST) }
    
    // Theme integration
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val bgIntensity by themeManager.backgroundIntensity.collectAsState()
    val animIntensity by themeManager.animationIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = !isSearchExpanded,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Column {
                            Text(
                                "SugarMunch", 
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.onSurface
                            )
                            Text(
                                "Live, Life, Love \u2764",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.primary
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = isSearchExpanded,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.search(it) },
                            placeholder = { Text("Search candy...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = colors.primary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { }),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.search("") }) {
                                        Icon(Icons.Default.Close, "Clear search")
                                    }
                                }
                            }
                        )
                    }
                },
                actions = {
                    // View toggle
                    ViewToggleButton(
                        isGridView = viewMode == CatalogViewMode.GRID,
                        onToggle = {
                            viewMode = if (viewMode == CatalogViewMode.LIST)
                                CatalogViewMode.GRID
                            else
                                CatalogViewMode.LIST
                            scope.launch {
                                prefs.setCatalogDefaultView(if (viewMode == CatalogViewMode.GRID) "grid" else "list")
                            }
                        }
                    )
                    
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.onSurface
                        )
                    }
                    IconButton(onClick = onShopClick) {
                        Icon(
                            Icons.Default.ShoppingCart, 
                            contentDescription = "Shop",
                            tint = colors.primary
                        )
                    }
                    IconButton(onClick = onRewardsClick) {
                        Icon(
                            Icons.Default.CardGiftcard, 
                            contentDescription = "Daily Rewards",
                            tint = Color(0xFFFFD700)
                        )
                    }
                    IconButton(onClick = onThemeClick) {
                        Icon(
                            Icons.Default.Palette, 
                            contentDescription = "Theme",
                            tint = colors.onSurface
                        )
                    }
                    IconButton(onClick = onAnalyticsClick) {
                        Icon(
                            Icons.Default.BarChart, 
                            contentDescription = "Analytics",
                            tint = colors.onSurface
                        )
                    }
                    IconButton(onClick = onConfectionerClick) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Confectioner",
                            tint = colors.primary
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
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
        },
        bottomBar = {
            SugarMunchBottomNav(
                currentRoute = "catalog",
                onNavigate = { route ->
                    when (route) {
                        "effects" -> onEffectsClick()
                        "theme" -> onThemeClick()
                        "settings" -> onSettingsClick()
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
            // Dynamic theme background
            AnimatedThemeBackground()
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.8f),
                                colors.background.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            when (val state = uiState) {
                is CatalogUiState.Loading -> {
                    CatalogLoadingState(colors = colors)
                }
                is CatalogUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                    }
                }
                is CatalogUiState.Success -> {
                    if (state.filteredApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No candy found \uD83C\uDF6C",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurface
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            SortFilterChips(
                                sortMode = sortMode,
                                filterFeaturedOnly = filterFeaturedOnly,
                                onSortClick = { viewModel.setSortMode(it) },
                                onFilterFeaturedClick = { viewModel.setFilterFeaturedOnly(it) },
                                colors = colors
                            )
                            if (viewMode == CatalogViewMode.GRID) {
                                CatalogGridView(
                                        apps = state.filteredApps,
                                        onAppClick = onAppClick,
                                        gridColumns = gridColumns,
                                        cardStyle = cardStyle,
                                        accentOverrides = appAccentOverrides,
                                        modifier = Modifier.fillMaxSize()
                                    )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (featuredClan != null) {
                                        item(key = "featured_clan_banner") {
                                            StaggeredEntrance(0) {
                                                Card(
                                                    onClick = onClanClick,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = colors.primary.copy(alpha = 0.2f)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            featuredClan!!.emblem,
                                                            style = MaterialTheme.typography.headlineSmall
                                                        )
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                "Featured clan",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = colors.onSurface.copy(alpha = 0.8f)
                                                            )
                                                            Text(
                                                                featuredClan!!.name,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                color = colors.onSurface
                                                            )
                                                        }
                                                        Icon(
                                                            Icons.Default.ChevronRight,
                                                            contentDescription = null,
                                                            tint = colors.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (trails.isNotEmpty()) {
                                        item(key = "trails_header") {
                                            StaggeredEntrance(0) {
                                                Text(
                                                    "Candy Trails",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = colors.primary,
                                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                                )
                                            }
                                        }
                                        item(key = "trails_row") {
                                            StaggeredEntrance(1) {
                                                LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    items(trails, key = { it.id }) { trail ->
                                                        CandyTrailCard(
                                                            trail = trail,
                                                            colors = colors,
                                                            completed = trail.id in completedTrailIds,
                                                            onClick = { onTrailClick(trail) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (state.featuredApps.isNotEmpty()) {
                                        item(key = "featured_header") {
                                            StaggeredEntrance(0) {
                                                Text(
                                                "Featured",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = colors.primary,
                                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                            )
                                            }
                                        }
                                        item(key = "featured_row") {
                                            StaggeredEntrance(1) {
                                                LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    items(state.featuredApps, key = { it.id }) { app ->
                                                        CandyAppCardV2(
                                                            app = app,
                                                            colors = colors,
                                                            cardStyle = cardStyle,
                                                            accentOverride = appAccentOverrides[app.id],
                                                            hasThemeOverride = appThemeOverrides[app.id]?.enabled == true,
                                                            intensity = animIntensity,
                                                            onClick = { onAppClick(app.id) },
                                                            compact = true
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    val grouped = state.filteredApps.groupBy { it.category?.takeIf { c -> c.isNotBlank() } ?: "Other" }
                                    val pinnedCategories = listOf("Utilities", "Video & Music", "Stores", "Other")
                                    val categoryOrder = pinnedCategories + grouped.keys.filter { it !in pinnedCategories }.sorted()
                                    categoryOrder.forEach { cat ->
                                        val catApps = grouped[cat] ?: emptyList()
                                        if (catApps.isEmpty()) return@forEach
                                        item(key = "cat-$cat") {
                                            StaggeredEntrance(0) {
                                                Text(
                                                    text = cat,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = colors.primary,
                                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                                )
                                            }
                                        }
                                        itemsIndexed(catApps, key = { _, app -> app.id }) { index, app ->
                                            StaggeredEntrance(index) {
                                                CandyAppCardV2(
                                                    app = app,
                                                    colors = colors,
                                                    cardStyle = cardStyle,
                                                    accentOverride = appAccentOverrides[app.id],
                                                    hasThemeOverride = appThemeOverrides[app.id]?.enabled == true,
                                                    intensity = animIntensity,
                                                    onClick = { onAppClick(app.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogLoadingState(colors: com.sugarmunch.app.theme.model.AdjustedColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(4) { index ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surface.copy(alpha = 0.88f - (index * 0.08f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(colors.primary.copy(alpha = 0.28f), colors.secondary.copy(alpha = 0.18f))
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(50))
                                .background(colors.surfaceVariant)
                        )
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(50))
                                .background(colors.surfaceVariant.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaggeredEntrance(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(index) {
        kotlinx.coroutines.delay((index * 35).toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 4 }
    ) {
        content()
    }
}

@Composable
private fun CandyTrailCard(
    trail: CandyTrailEntry,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    completed: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(colors.primary, colors.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(trail.icon, style = MaterialTheme.typography.headlineMedium)
            }
            if (completed) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colors.success.copy(alpha = 0.3f)
                ) {
                    Text(
                        "Completed",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = colors.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = trail.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = trail.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SortFilterChips(
    sortMode: CatalogSortMode,
    filterFeaturedOnly: Boolean,
    onSortClick: (CatalogSortMode) -> Unit,
    onFilterFeaturedClick: (Boolean) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box {
            FilterChip(
                selected = sortMode != CatalogSortMode.DEFAULT,
                onClick = { sortMenuExpanded = true },
                label = { Text("Sort: ${sortMode.key.replace("_", " ")}") }
            )
            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false }
            ) {
                CatalogSortMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode.key.replace("_", " ")) },
                        onClick = {
                            onSortClick(mode)
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }
        FilterChip(
            selected = filterFeaturedOnly,
            onClick = { onFilterFeaturedClick(!filterFeaturedOnly) },
            label = { Text("Featured only") }
        )
    }
}

@Composable
internal fun CandyAppCardV2(
    app: AppEntry,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    cardStyle: String,
    accentOverride: String? = null,
    hasThemeOverride: Boolean = false,
    intensity: Float,
    onClick: () -> Unit,
    compact: Boolean = false
) {
    val reduceMotion = rememberReduceMotion()
    val utilitySpec = remember(app.id) { PhaseOneUtilities.specFor(app.id) }
    val infiniteTransition = rememberInfiniteTransition(label = "card")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (reduceMotion) 1f else 1f + (0.02f * intensity),
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) Int.MAX_VALUE else (1500 / maxOf(intensity, 0.5f)).toInt()),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val accentColor = (accentOverride ?: app.accentColor)?.let { hex ->
        try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) { null }
    } ?: colors.primary
    val showAccentBorder = cardStyle == "accent" || cardStyle == "max"
    val isCompact = cardStyle == "compact" || compact
    val isMax = cardStyle == "max"
    val surfaceAlpha = when (cardStyle) {
        "glass" -> 0.6f
        "max" -> 1f
        else -> if (utilitySpec != null) 0.98f else 0.95f
    }
    val heroColors = utilitySpec?.heroColors ?: listOf(accentColor, colors.secondary, colors.tertiary)

    Card(
        onClick = onClick,
        modifier = Modifier
            .then(if (compact) Modifier.width(160.dp) else Modifier.fillMaxWidth())
            .scale(scale)
            .then(
                if (showAccentBorder) Modifier.border(2.dp, accentColor, RoundedCornerShape(20.dp))
                else Modifier
            )
            .springPressable(enabled = true),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = surfaceAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = (4f * intensity).dp)
    ) {
        Row(
            modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 40.dp else 48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = heroColors
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(utilitySpec?.emojiIcon ?: "\uD83C\uDF6C", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.name,
                        style = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleLarge,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    app.badge?.let { badge ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accentColor.copy(alpha = 0.3f)
                        ) {
                            Text(
                                badge,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = colors.onSurface
                            )
                        }
                    }
                    if (hasThemeOverride) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accentColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "Theme",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = accentColor
                            )
                        }
                    }
                }
                utilitySpec?.let { spec ->
                    Text(
                        text = spec.shortTagline,
                        style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.72f),
                        maxLines = if (isCompact) 1 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!isCompact) {
                    Text(
                        text = app.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (utilitySpec != null && !isCompact) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        app.components.orEmpty().take(2).forEach { component ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = heroColors.first().copy(alpha = if (isMax) 0.22f else 0.14f)
                            ) {
                                Text(
                                    text = component.replace('-', ' '),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "v${app.version}",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }
        }
    }
}
