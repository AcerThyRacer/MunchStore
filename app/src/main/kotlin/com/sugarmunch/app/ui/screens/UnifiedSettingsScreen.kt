package com.sugarmunch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager

/**
 * Unified Settings Screen - Master Settings Hub
 * 
 * Organizes all settings into logical categories:
 * - Appearance (Themes, Card Styles, Effects)
 * - Touch & Haptics (Haptic Feedback, Touch Sounds, Gestures, Per-App Settings)
 * - App Store (Plugin Store, Installed Plugins, Auto-Updates)
 * - Organization (Folders, Smart Sorting, Favorites)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedSettingsScreen(
    onNavigateBack: () -> Unit,
    onThemeClick: () -> Unit = {},
    onCardStyleClick: () -> Unit = {},
    onEffectsClick: () -> Unit = {},
    onHapticFeedbackClick: () -> Unit = {},
    onTouchSoundsClick: () -> Unit = {},
    onGesturesClick: () -> Unit = {},
    onPerAppSettingsClick: () -> Unit = {},
    onPluginStoreClick: () -> Unit = {},
    onInstalledPluginsClick: () -> Unit = {},
    onAutoUpdatesClick: () -> Unit = {},
    onFoldersClick: () -> Unit = {},
    onSmartSortingClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
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
                // Appearance Category
                category("Appearance") {
                    SettingsRow(
                        title = "Themes",
                        subtitle = "Customize colors and styles",
                        icon = Icons.Default.Palette,
                        colors = colors,
                        onClick = onThemeClick
                    )
                    SettingsRow(
                        title = "Card Styles",
                        subtitle = "Choose card appearance",
                        icon = Icons.Default.GridOn,
                        colors = colors,
                        onClick = onCardStyleClick
                    )
                    SettingsRow(
                        title = "Effects",
                        subtitle = "Visual effects and animations",
                        icon = Icons.Default.AutoAwesome,
                        colors = colors,
                        onClick = onEffectsClick
                    )
                }

                // Touch & Haptics Category
                category("Touch & Haptics") {
                    SettingsRow(
                        title = "Haptic Feedback",
                        subtitle = "Vibration patterns and intensity",
                        icon = Icons.Default.Vibration,
                        colors = colors,
                        onClick = onHapticFeedbackClick
                    )
                    SettingsRow(
                        title = "Touch Sounds",
                        subtitle = "Audio feedback for touches",
                        icon = Icons.Default.VolumeUp,
                        colors = colors,
                        onClick = onTouchSoundsClick
                    )
                    SettingsRow(
                        title = "Gestures",
                        subtitle = "Custom gesture mappings",
                        icon = Icons.Default.TouchApp,
                        colors = colors,
                        onClick = onGesturesClick
                    )
                    SettingsRow(
                        title = "Per-App Settings",
                        subtitle = "App-specific customization",
                        icon = Icons.Default.Apps,
                        colors = colors,
                        onClick = onPerAppSettingsClick
                    )
                }

                // App Store Category
                category("App Store") {
                    SettingsRow(
                        title = "Plugin Store",
                        subtitle = "Browse and install plugins",
                        icon = Icons.Default.ShoppingCart,
                        colors = colors,
                        onClick = onPluginStoreClick
                    )
                    SettingsRow(
                        title = "Installed Plugins",
                        subtitle = "Manage installed plugins",
                        icon = Icons.Default.Folder,
                        colors = colors,
                        onClick = onInstalledPluginsClick
                    )
                    SettingsRow(
                        title = "Auto-Updates",
                        subtitle = "Automatic plugin updates",
                        icon = Icons.Default.Update,
                        colors = colors,
                        onClick = onAutoUpdatesClick
                    )
                }

                // Organization Category
                category("Organization") {
                    SettingsRow(
                        title = "Folders",
                        subtitle = "Manage app folders",
                        icon = Icons.Default.Folder,
                        colors = colors,
                        onClick = onFoldersClick
                    )
                    SettingsRow(
                        title = "Smart Sorting",
                        subtitle = "Automatic app organization",
                        icon = Icons.Default.Sort,
                        colors = colors,
                        onClick = onSmartSortingClick
                    )
                    SettingsRow(
                        title = "Favorites",
                        subtitle = "Favorite apps and folders",
                        icon = Icons.Default.Star,
                        colors = colors,
                        onClick = onFavoritesClick
                    )
                }

                // About Category
                category("About") {
                    SettingsRow(
                        title = "About SugarMunch",
                        subtitle = "Version, credits, licenses",
                        icon = Icons.Default.Info,
                        colors = colors,
                        onClick = onAboutClick
                    )
                }
            }
        }
    }
}

/**
 * Settings category header
 */
@Composable
private fun category(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        content()
    }
}

/**
 * Settings row item
 */
@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Settings row with toggle switch
 */
@Composable
private fun SettingsRowWithSwitch(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }

            // Switch
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
