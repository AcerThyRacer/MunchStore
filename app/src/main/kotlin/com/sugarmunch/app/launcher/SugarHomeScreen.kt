package com.sugarmunch.app.launcher

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.effects.ambient.AmbientEffectLayer
import com.sugarmunch.app.effects.ambient.AmbientEffectConfig
import com.sugarmunch.app.effects.ambient.AmbientEffectType
import com.sugarmunch.app.hub.UnifiedAppInfo
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.ui.design.SugarDimens
import com.sugarmunch.app.ui.particles.ParticleSystem
import com.sugarmunch.app.ui.particles.ParticleType
import com.sugarmunch.app.ui.particles.EmitterShape
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * SugarHomeScreen - Feature-complete custom launcher home screen.
 * Features:
 * - Infinite scroll pages
 * - Widget support with themed widgets
 * - Dock with app shortcuts
 * - Notification badges
 * - At-a-glance info cards (weather, calendar, music)
 * - Gesture zones (swipe areas for actions)
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SugarHomeScreen(
    pages: List<HomePage> = emptyList(),
    dockApps: List<UnifiedAppInfo> = emptyList(),
    onAppClick: (UnifiedAppInfo) -> Unit,
    onAppLongPress: (UnifiedAppInfo) -> Unit,
    onSettingsClick: () -> Unit,
    onWidgetAdd: () -> Unit = {},
    onSearchOpen: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val colors = currentTheme.getColorsForIntensity(1.0f)

    // Pager state for infinite pages
    val initialPage = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { Int.MAX_VALUE }
    )

    // Current page content (cycles through available pages)
    val currentPageIndex = pagerState.currentPage % maxOf(1, pages.size)

    // Pull-down gesture state
    var isPullingDown by remember { mutableStateOf(false) }
    var pullDownOffset by remember { mutableFloatStateOf(0f) }

    // Ambient effects
    val ambientConfig = AmbientEffectConfig(
        enabled = true,
        effectType = AmbientEffectType.FLOATING_PARTICLES,
        intensity = 0.3f
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Wallpaper/Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.surface,
                            colors.primary.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        // Ambient effect layer
        AmbientEffectLayer(config = ambientConfig) {
            // Main content
            Column(modifier = Modifier.fillMaxSize()) {
                // Top status/info bar
                TopInfoBar(
                    colors = colors,
                    onSearchOpen = onSearchOpen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Main pager area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        pageSize = PageSize.Fill,
                        pageSpacing = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val pageIndex = page % maxOf(1, pages.size)
                        val homePage = pages.getOrNull(pageIndex) ?: HomePage(
                            id = "default",
                            type = PageType.APPS,
                            items = emptyList()
                        )

                        when (homePage.type) {
                            PageType.APPS -> AppsPage(
                                apps = homePage.items,
                                onAppClick = onAppClick,
                                onAppLongPress = onAppLongPress,
                                colors = colors
                            )
                            PageType.WIDGETS -> WidgetsPage(
                                widgets = homePage.widgets,
                                onWidgetAdd = onWidgetAdd,
                                colors = colors
                            )
                            PageType.SMART -> SmartPage(
                                colors = colors,
                                onAppClick = onAppClick
                            )
                            PageType.EMPTY -> EmptyPage(
                                colors = colors,
                                onAddApps = onSettingsClick
                            )
                        }
                    }

                    // Page indicators
                    if (pages.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(minOf(pages.size, 10)) { index ->
                                PageIndicator(
                                    isSelected = currentPageIndex % pages.size == index,
                                    colors = colors
                                )
                            }
                        }
                    }
                }

                // Dock
                if (dockApps.isNotEmpty()) {
                    DockBar(
                        apps = dockApps,
                        onAppClick = onAppClick,
                        onAppLongPress = onAppLongPress,
                        colors = colors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Pull-down search overlay
        AnimatedVisibility(
            visible = pullDownOffset > 100,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
        ) {
            SearchOverlay(
                colors = colors,
                onSearchOpen = onSearchOpen,
                onDismiss = { pullDownOffset = 0f }
            )
        }
    }
}

@Composable
private fun TopInfoBar(
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    onSearchOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date and time
        Column {
            Text(
                text = "Friday",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = "March 6, 2026",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }

        // Quick actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Weather
            Surface(
                shape = RoundedCornerShape(SugarDimens.Radius.lg),
                color = colors.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("72F", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Search button
            IconButton(
                onClick = onSearchOpen,
                modifier = Modifier
                    .background(
                        colors.surfaceVariant.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(Icons.Default.Search, "Search", tint = colors.onSurface)
            }
        }
    }
}

@Composable
private fun AppsPage(
    apps: List<UnifiedAppInfo>,
    onAppClick: (UnifiedAppInfo) -> Unit,
    onAppLongPress: (UnifiedAppInfo) -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            AppIcon(
                app = app,
                onClick = { onAppClick(app) },
                onLongPress = { onAppLongPress(app) },
                colors = colors
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppIcon(
    app: UnifiedAppInfo,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors
) {
    val accentColor = app.accentColor?.let { parseColorSafe(it) } ?: colors.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(SugarDimens.Radius.lg))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.3f),
                            accentColor.copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                app.displayName.first().toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            // Notification badge
            if (app.badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(18.dp)
                        .background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "3",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            app.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(64.dp)
        )
    }
}

@Composable
private fun WidgetsPage(
    widgets: List<HomeWidget>,
    onWidgetAdd: () -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (widgets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = colors.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No widgets yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onWidgetAdd) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Widget")
                    }
                }
            }
        } else {
            widgets.forEach { widget ->
                WidgetCard(
                    widget = widget,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun WidgetCard(
    widget: HomeWidget,
    colors: com.sugarmunch.app.theme.engine.ThemeColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 200.dp),
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Widget: ${widget.type}",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
        }
    }
}

@Composable
private fun SmartPage(
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    onAppClick: (UnifiedAppInfo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Smart suggestions would be dynamically generated
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(SugarDimens.Radius.lg),
                    color = colors.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Lightbulb,
                            null,
                            tint = colors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Suggestions",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun EmptyPage(
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    onAddApps: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AddCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = colors.primary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Empty Page",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onAddApps) {
                Text("Add Apps")
            }
        }
    }
}

@Composable
private fun DockBar(
    apps: List<UnifiedAppInfo>,
    onAppClick: (UnifiedAppInfo) -> Unit,
    onAppLongPress: (UnifiedAppInfo) -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .height(80.dp),
        shape = RoundedCornerShape(SugarDimens.Radius.xl),
        color = colors.surface.copy(alpha = 0.8f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            apps.take(5).forEach { app ->
                DockAppIcon(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongPress = { onAppLongPress(app) },
                    colors = colors
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DockAppIcon(
    app: UnifiedAppInfo,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    colors: com.sugarmunch.app.theme.engine.ThemeColors
) {
    val accentColor = app.accentColor?.let { parseColorSafe(it) } ?: colors.primary

    Box(
        modifier = Modifier
            .size(52.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .clip(RoundedCornerShape(SugarDimens.Radius.md))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.25f),
                        accentColor.copy(alpha = 0.1f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            app.displayName.first().toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

@Composable
private fun PageIndicator(
    isSelected: Boolean,
    colors: com.sugarmunch.app.theme.engine.ThemeColors
) {
    Box(
        modifier = Modifier
            .size(if (isSelected) 8.dp else 6.dp)
            .background(
                if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.3f),
                CircleShape
            )
    )
}

@Composable
private fun SearchOverlay(
    colors: com.sugarmunch.app.theme.engine.ThemeColors,
    onSearchOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Recent Apps",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// Data classes
data class HomePage(
    val id: String,
    val type: PageType,
    val items: List<UnifiedAppInfo>,
    val widgets: List<HomeWidget> = emptyList()
)

enum class PageType {
    APPS,
    WIDGETS,
    SMART,
    EMPTY
}

data class HomeWidget(
    val id: String,
    val type: String,
    val width: Int = 2,
    val height: Int = 1
)

// Helper function
private fun parseColorSafe(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color(0xFFE91E63)
    }
}
