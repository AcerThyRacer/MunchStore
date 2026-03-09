package com.sugarmunch.app.hub

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sugarmunch.app.data.local.FolderEntity
import com.sugarmunch.app.theme.engine.ThemeColors
import com.sugarmunch.app.ui.design.SugarDimens
import com.sugarmunch.app.ui.particles.ParticleSystem
import com.sugarmunch.app.ui.particles.ParticleType
import com.sugarmunch.app.ui.particles.EmitterShape
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Drawer edge position
 */
enum class DrawerEdge {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM
}

/**
 * Drawer content configuration
 */
data class QuickAccessConfig(
    val edge: DrawerEdge = DrawerEdge.RIGHT,
    val showFavorites: Boolean = true,
    val showRecent: Boolean = true,
    val showSuggested: Boolean = true,
    val showFolders: Boolean = true,
    val showSearch: Boolean = true,
    val handleSize: Float = 24f,
    val drawerWidth: Float = 280f,
    val drawerHeight: Float = 0.7f, // Percentage of screen height
    val enableGestures: Boolean = true,
    val enableHaptics: Boolean = true,
    val blurBackground: Boolean = true,
    val showParticleEffects: Boolean = true
)

/**
 * QuickAccessDrawer - A sliding drawer that can appear from any screen edge.
 * Provides quick access to:
 * - Favorite apps
 * - Recent apps
 * - ML-suggested apps
 * - Folder shortcuts
 * - Quick search
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAccessDrawer(
    modifier: Modifier = Modifier,
    config: QuickAccessConfig = QuickAccessConfig(),
    favoriteApps: List<UnifiedAppInfo> = emptyList(),
    recentApps: List<UnifiedAppInfo> = emptyList(),
    suggestedApps: List<UnifiedAppInfo> = emptyList(),
    folders: List<FolderEntity> = emptyList(),
    onAppClick: (UnifiedAppInfo) -> Unit,
    onFolderClick: (FolderEntity) -> Unit,
    onSearchClick: () -> Unit = {},
    colors: ThemeColors,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Drawer state
    var isOpen by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // Animation spec
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffnessness = Spring.StiffnessLow
    )

    // Calculate dimensions based on edge
    val drawerSize = when (config.edge) {
        DrawerEdge.LEFT, DrawerEdge.RIGHT -> config.drawerWidth.dp
        DrawerEdge.TOP, DrawerEdge.BOTTOM -> (config.drawerHeight * 100).dp
    }

    // Animated offset
    val animatedOffset by animateFloatAsState(
        targetValue = if (isOpen) 0f else getClosedOffset(config.edge, drawerSize, density),
        animationSpec = animationSpec,
        label = "drawer_offset"
    )

    // Background alpha for overlay effect
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.5f else 0f,
        animationSpec = tween(300),
        label = "bg_alpha"
    )

    // Particle effect state
    var showParticles by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Main content
        content()

        // Dark overlay when drawer is open
        if (isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backgroundAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isOpen = false }
                    .zIndex(1f)
            )
        }

        // Drawer handle
        DrawerHandle(
            edge = config.edge,
            isOpen = isOpen,
            onClick = { isOpen = !isOpen },
            colors = colors,
            modifier = Modifier.zIndex(3f)
        )

        // Drawer content
        Box(
            modifier = Modifier
                .zIndex(2f)
                .offset { getOffsetForEdge(config.edge, animatedOffset + dragOffset, drawerSize, density) }
                .graphicsLayer {
                    // Apply blur effect if enabled
                    if (config.blurBackground && isOpen) {
                        shadowElevation = 16f
                    }
                }
        ) {
            DrawerContent(
                config = config,
                favoriteApps = favoriteApps,
                recentApps = recentApps,
                suggestedApps = suggestedApps,
                folders = folders,
                onAppClick = { app ->
                    onAppClick(app)
                    if (config.showParticleEffects) {
                        showParticles = true
                    }
                    isOpen = false
                },
                onFolderClick = { folder ->
                    onFolderClick(folder)
                    isOpen = false
                },
                onSearchClick = {
                    onSearchClick()
                    isOpen = false
                },
                onCloseClick = { isOpen = false },
                colors = colors,
                modifier = Modifier.size(
                    width = if (config.edge in listOf(DrawerEdge.LEFT, DrawerEdge.RIGHT)) drawerSize else 400.dp,
                    height = if (config.edge in listOf(DrawerEdge.LEFT, DrawerEdge.RIGHT)) 600.dp else drawerSize
                )
            )
        }

        // Particle effect when app launched
        if (showParticles) {
            ParticleSystem(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f),
                particleType = ParticleType.SPARKLE,
                emitterShape = EmitterShape.SCREEN_CENTER,
                intensity = 0.6f
            )
            LaunchedEffect(showParticles) {
                kotlinx.coroutines.delay(1000)
                showParticles = false
            }
        }
    }
}

@Composable
private fun DrawerHandle(
    edge: DrawerEdge,
    isOpen: Boolean,
    onClick: () -> Unit,
    colors: ThemeColors,
    modifier: Modifier = Modifier
) {
    val handleColor = colors.primary.copy(alpha = 0.8f)
    val handleSize = 24.dp

    val handleModifier = modifier
        .clip(RoundedCornerShape(
            when (edge) {
                DrawerEdge.LEFT -> RoundedCornerShape(topEnd = SugarDimens.Radius.lg, bottomEnd = SugarDimens.Radius.lg)
                DrawerEdge.RIGHT -> RoundedCornerShape(topStart = SugarDimens.Radius.lg, bottomStart = SugarDimens.Radius.lg)
                DrawerEdge.TOP -> RoundedCornerShape(bottomStart = SugarDimens.Radius.lg, bottomEnd = SugarDimens.Radius.lg)
                DrawerEdge.BOTTOM -> RoundedCornerShape(topStart = SugarDimens.Radius.lg, topEnd = SugarDimens.Radius.lg)
            }
        ))
        .background(handleColor)
        .clickable(onClick = onClick)
        .padding(4.dp)

    Box(
        modifier = when (edge) {
            DrawerEdge.LEFT -> handleModifier.align(Alignment.CenterStart).width(8.dp).height(48.dp)
            DrawerEdge.RIGHT -> handleModifier.align(Alignment.CenterEnd).width(8.dp).height(48.dp)
            DrawerEdge.TOP -> handleModifier.align(Alignment.TopCenter).height(8.dp).width(48.dp)
            DrawerEdge.BOTTOM -> handleModifier.align(Alignment.BottomCenter).height(8.dp).width(48.dp)
        },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isOpen) Icons.Default.Close else when (edge) {
                DrawerEdge.LEFT -> Icons.Default.KeyboardArrowRight
                DrawerEdge.RIGHT -> Icons.Default.KeyboardArrowLeft
                DrawerEdge.TOP -> Icons.Default.KeyboardArrowDown
                DrawerEdge.BOTTOM -> Icons.Default.KeyboardArrowUp
            },
            contentDescription = if (isOpen) "Close drawer" else "Open drawer",
            tint = colors.onPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun DrawerContent(
    config: QuickAccessConfig,
    favoriteApps: List<UnifiedAppInfo>,
    recentApps: List<UnifiedAppInfo>,
    suggestedApps: List<UnifiedAppInfo>,
    folders: List<FolderEntity>,
    onAppClick: (UnifiedAppInfo) -> Unit,
    onFolderClick: (FolderEntity) -> Unit,
    onSearchClick: () -> Unit,
    onCloseClick: () -> Unit,
    colors: ThemeColors,
    modifier: Modifier = Modifier
) {
    val backgroundColor = colors.surface.copy(alpha = 0.95f)

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(
                when (config.edge) {
                    DrawerEdge.LEFT -> RoundedCornerShape(topEnd = SugarDimens.Radius.xl, bottomEnd = SugarDimens.Radius.xl)
                    DrawerEdge.RIGHT -> RoundedCornerShape(topStart = SugarDimens.Radius.xl, bottomStart = SugarDimens.Radius.xl)
                    DrawerEdge.TOP -> RoundedCornerShape(bottomStart = SugarDimens.Radius.xl, bottomEnd = SugarDimens.Radius.xl)
                    DrawerEdge.BOTTOM -> RoundedCornerShape(topStart = SugarDimens.Radius.xl, topEnd = SugarDimens.Radius.xl)
                }
            ))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        colors.primary.copy(alpha = 0.05f),
                        backgroundColor
                    )
                )
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SugarDimens.Spacing.md)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Quick Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }

                IconButton(onClick = onCloseClick) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

            // Search bar
            if (config.showSearch) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SugarDimens.Radius.lg),
                    color = colors.surfaceVariant.copy(alpha = 0.5f),
                    onClick = onSearchClick
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Search apps...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))
            }

            // Scrollable content
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.lg)
            ) {
                // Favorites section
                if (config.showFavorites && favoriteApps.isNotEmpty()) {
                    item {
                        DrawerSection(
                            title = "Favorites",
                            icon = Icons.Default.Star,
                            colors = colors
                        )
                    }
                    item {
                        AppRow(
                            apps = favoriteApps.take(6),
                            onAppClick = onAppClick,
                            colors = colors
                        )
                    }
                }

                // Recent section
                if (config.showRecent && recentApps.isNotEmpty()) {
                    item {
                        DrawerSection(
                            title = "Recent",
                            icon = Icons.Default.History,
                            colors = colors
                        )
                    }
                    item {
                        AppRow(
                            apps = recentApps.take(6),
                            onAppClick = onAppClick,
                            colors = colors
                        )
                    }
                }

                // Suggested section
                if (config.showSuggested && suggestedApps.isNotEmpty()) {
                    item {
                        DrawerSection(
                            title = "Suggested",
                            icon = Icons.Default.Lightbulb,
                            colors = colors
                        )
                    }
                    item {
                        AppRow(
                            apps = suggestedApps.take(6),
                            onAppClick = onAppClick,
                            colors = colors
                        )
                    }
                }

                // Folders section
                if (config.showFolders && folders.isNotEmpty()) {
                    item {
                        DrawerSection(
                            title = "Folders",
                            icon = Icons.Outlined.Folder,
                            colors = colors
                        )
                    }
                    items(folders.take(8)) { folder ->
                        FolderListItem(
                            folder = folder,
                            onClick = { onFolderClick(folder) },
                            colors = colors
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerSection(
    title: String,
    icon: ImageVector,
    colors: ThemeColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun AppRow(
    apps: List<UnifiedAppInfo>,
    onAppClick: (UnifiedAppInfo) -> Unit,
    colors: ThemeColors
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps) { app ->
            DrawerAppItem(
                app = app,
                onClick = { onAppClick(app) },
                colors = colors
            )
        }
    }
}

@Composable
private fun DrawerAppItem(
    app: UnifiedAppInfo,
    onClick: () -> Unit,
    colors: ThemeColors
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffnessness = Spring.StiffnessMedium),
        label = "scale"
    )

    val accentColor = app.accentColor?.let { parseColorSafe(it) } ?: colors.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
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
                app.displayName.first().toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            app.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(56.dp)
        )
    }
}

@Composable
private fun FolderListItem(
    folder: FolderEntity,
    onClick: () -> Unit,
    colors: ThemeColors
) {
    val accentColor = folder.iconColor?.let { parseColorSafe(it) } ?: colors.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.md),
        color = colors.surfaceVariant.copy(alpha = 0.3f),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(SugarDimens.Radius.sm))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    folder.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface
                )
                Text(
                    "${folder.appIds.size} apps",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

// Helper functions
private fun getClosedOffset(edge: DrawerEdge, size: androidx.compose.ui.unit.Dp, density: androidx.compose.ui.unit.Density): Float {
    return with(density) {
        when (edge) {
            DrawerEdge.LEFT -> -size.toPx()
            DrawerEdge.RIGHT -> size.toPx()
            DrawerEdge.TOP -> -size.toPx()
            DrawerEdge.BOTTOM -> size.toPx()
        }
    }
}

private fun getOffsetForEdge(edge: DrawerEdge, offset: Float, size: androidx.compose.ui.unit.Dp, density: androidx.compose.ui.unit.Density): IntOffset {
    return with(density) {
        when (edge) {
            DrawerEdge.LEFT -> IntOffset(offset.roundToInt(), 0)
            DrawerEdge.RIGHT -> IntOffset((size.toPx() + offset).roundToInt(), 0)
            DrawerEdge.TOP -> IntOffset(0, offset.roundToInt())
            DrawerEdge.BOTTOM -> IntOffset(0, (size.toPx() + offset).roundToInt())
        }
    }
}

private fun parseColorSafe(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color(0xFFE91E63)
    }
}
