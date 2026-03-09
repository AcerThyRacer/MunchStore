package com.sugarmunch.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEffectsClick: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onShopClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onQuestsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onAutomationClick: () -> Unit = {},
    onAnimationClick: () -> Unit = {},
    onExtremeCustomizationClick: () -> Unit = {},
    onSpecialEffectsClick: () -> Unit = {},
    onFabConfigClick: () -> Unit = {},
    onEffects2026Click: () -> Unit = {},
    onClanClick: () -> Unit = {},
    onPruningClick: () -> Unit = {},
    onP2PClick: () -> Unit = {},
    onDeveloperClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onAccessibilityClick: () -> Unit = {},
    onHelpClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    // Theme integration
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val bgIntensity by themeManager.backgroundIntensity.collectAsState()
    val intensityLabel by themeManager.intensityLabel.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val overlayEnabled by viewModel.overlayEnabled.collectAsState(initial = false)
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsState(initial = false)
    val reduceMotion by viewModel.reduceMotion.collectAsState(initial = false)
    val catalogGridColumns by viewModel.catalogGridColumns.collectAsState(initial = 2)
    val catalogDefaultView by viewModel.catalogDefaultView.collectAsState(initial = "list")
    val catalogCardStyle by viewModel.catalogCardStyle.collectAsState(initial = "default")
    
    // Special Effects
    val fabEffectCount by viewModel.fabEffectCount.collectAsState()
    val activeEffectCount by viewModel.activeEffectCount.collectAsState()

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
                    IconButton(onClick = onBack) {
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
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background.copy(alpha = 0.95f),
                                colors.background.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Theme Card
                ThemeSettingsCard(
                    themeName = currentTheme.name,
                    intensityLabel = intensityLabel,
                    colors = colors,
                    onClick = onThemeClick
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Animation Studio Card
                AnimationSettingsCard(
                    colors = colors,
                    onClick = onAnimationClick
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // EXTREME Customization Card (NEW!)
                ExtremeCustomizationSettingsCard(
                    colors = colors,
                    onClick = onExtremeCustomizationClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Special Effects Card (New!)
                SpecialEffectsSettingsCard(
                    fabEffectCount = fabEffectCount,
                    activeEffectCount = activeEffectCount,
                    colors = colors,
                    onSpecialEffectsClick = onSpecialEffectsClick,
                    onFabConfigClick = onFabConfigClick
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 2026 Effects Card (NEW!)
                Effects2026SettingsCard(
                    colors = colors,
                    onClick = onEffects2026Click
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Effects Card
                EffectsSettingsCard(
                    colors = colors,
                    onClick = onEffectsClick
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sugar Shop Card
                ShopSettingsCard(
                    colors = colors,
                    onClick = onShopClick
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Daily Rewards Card
                RewardsSettingsCard(
                    colors = colors,
                    onClick = onRewardsClick
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                AchievementsSettingsCard(
                    colors = colors,
                    onClick = onAchievementsClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                QuestsSettingsCard(
                    colors = colors,
                    onClick = onQuestsClick
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                // Clans Card
                ClanSettingsCard(colors = colors, onClick = onClanClick)
                Spacer(modifier = Modifier.height(16.dp))
                // P2P / Candy Jar Card
                P2PSettingsCard(colors = colors, onClick = onP2PClick)
                Spacer(modifier = Modifier.height(16.dp))
                // Developer Card
                DeveloperSettingsCard(colors = colors, onClick = onDeveloperClick)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Storage / Pruning Card
                PruningSettingsCard(colors = colors, onClick = onPruningClick)
                Spacer(modifier = Modifier.height(16.dp))
                // Analytics Card
                AnalyticsSettingsCard(colors = colors, onClick = onAnalyticsClick)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Automation Card
                AutomationSettingsCard(
                    colors = colors,
                    onClick = onAutomationClick
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Reduce motion (optional)
                ReduceMotionSettingsCard(
                    enabled = reduceMotion,
                    colors = colors,
                    onToggle = { viewModel.setReduceMotion(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Catalog options
                CatalogOptionsCard(
                    gridColumns = catalogGridColumns,
                    defaultView = catalogDefaultView,
                    cardStyle = catalogCardStyle,
                    colors = colors,
                    onGridColumnsChange = { viewModel.setCatalogGridColumns(it) },
                    onDefaultViewChange = { viewModel.setCatalogDefaultView(it) },
                    onCardStyleChange = { viewModel.setCatalogCardStyle(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Overlay FAB Card
                OverlaySettingsCard(
                    enabled = overlayEnabled,
                    hasPermission = hasOverlayPermission,
                    colors = colors,
                    onToggle = { enabled ->
                        if (enabled && !hasOverlayPermission) {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            )
                        }
                        viewModel.setOverlayEnabled(enabled)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // About Card
                AboutCard(colors = colors)
                
                Spacer(modifier = Modifier.height(16.dp))

                // Account Settings Card (NEW!)
                AccountSettingsCard(
                    colors = colors,
                    onClick = onAccountClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notification Settings Card
                NotificationSettingsCard(
                    colors = colors,
                    onClick = onNotificationClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Accessibility Settings Card
                AccessibilitySettingsCard(
                    colors = colors,
                    onClick = onAccessibilityClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Help & Support Card
                HelpSettingsCard(
                    colors = colors,
                    onClick = onHelpClick
                )
            }
        }
    }
}

@Composable
private fun ThemeSettingsCard(
    themeName: String,
    intensityLabel: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme icon with gradient
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
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Theme Studio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "$themeName • $intensityLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.primary
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

@Composable
private fun AnimationSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(Color(0xFFFF4081), Color(0xFFE040FB))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Animation,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Animation Studio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "Customize transitions and motion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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

@Composable
private fun AutomationSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(Color(0xFF9C27B0), Color(0xFF673AB7))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoMode,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Automation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "Create custom automated workflows",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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

@Composable
private fun EffectsSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(colors.tertiary, colors.accent)
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colors.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Effects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "Manage visual effects and overlays",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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

@Composable
private fun CatalogOptionsCard(
    gridColumns: Int,
    defaultView: String,
    cardStyle: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onGridColumnsChange: (Int) -> Unit,
    onDefaultViewChange: (String) -> Unit,
    onCardStyleChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Catalog options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = "Grid columns, default view, card style",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2, 3, 4).forEach { n ->
                    FilterChip(
                        selected = gridColumns == n,
                        onClick = { onGridColumnsChange(n) },
                        label = { Text("$n cols") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = defaultView == "list",
                    onClick = { onDefaultViewChange("list") },
                    label = { Text("List") }
                )
                FilterChip(
                    selected = defaultView == "grid",
                    onClick = { onDefaultViewChange("grid") },
                    label = { Text("Grid") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "default" to "Default",
                    "compact" to "Compact",
                    "accent" to "Accent",
                    "glass" to "Glass",
                    "max" to "MAX"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = cardStyle == value,
                        onClick = { onCardStyleChange(value) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReduceMotionSettingsCard(
    enabled: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(colors.tertiary, colors.accent)
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = colors.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reduce motion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "Reduce animations and motion effects (particles, pulses, transitions)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.primary,
                    checkedTrackColor = colors.primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun OverlaySettingsCard(
    enabled: Boolean,
    hasPermission: Boolean,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = colors.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Overlay FAB",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = if (hasPermission) "Show candy FAB on top of other apps" 
                          else "Permission required for overlay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasPermission) colors.onSurface.copy(alpha = 0.7f) else colors.error
                )
            }
            
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.primary,
                    checkedTrackColor = colors.primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun AboutCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            AboutItem(
                icon = Icons.Default.Info,
                title = "Version",
                subtitle = "1.0.0 - SugarRush Edition",
                colors = colors
            )
            
            AboutItem(
                icon = Icons.Default.Favorite,
                title = "Made with love",
                subtitle = "Live, Life, Love \u2764",
                colors = colors
            )
            
            AboutItem(
                icon = Icons.Default.Code,
                title = "Open Source",
                subtitle = "GPL v3 License",
                colors = colors
            )
        }
    }
}

@Composable
private fun AboutItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}


@Composable
private fun ShopSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(Color(0xFFFF69B4), Color(0xFFFF1493))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sugar Shop",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "Spend Sugar Points on exclusive items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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

@Composable
private fun DeveloperSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) { Text("\uD83C\uDF6C", style = MaterialTheme.typography.titleLarge) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Developer studio", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text("QA, reports, asset gen", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.7f))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun P2PSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(listOf(colors.secondary, colors.tertiary)),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) { Text("\uD83C\uDFFA", style = MaterialTheme.typography.titleLarge) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Candy Jars & beam", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text("P2P sharing, host catalog", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.7f))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun PruningSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(listOf(colors.primary.copy(alpha = 0.6f), colors.secondary.copy(alpha = 0.6f))),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) { Text("\uD83D\uDDC2", style = MaterialTheme.typography.titleLarge) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Storage & pruning", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text("Free space, backup to P2P", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.7f))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun ClanSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(colors.primary, colors.secondary)
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83C\uDF6C", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Clans",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                Text(
                    "Guilds, wars, co-op discovery",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.7f)
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

@Composable
private fun RewardsSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Rewards",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "Claim daily bonuses and build your streak",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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

@Composable
private fun AchievementsSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.primary.copy(alpha = 0.15f)
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = colors.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track unlocked badges, rarity, and overall completion progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.primary)
        }
    }
}

@Composable
private fun QuestsSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.15f)
            ) {
                Icon(
                    Icons.Default.Checklist,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = Color(0xFFB8860B)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Quests",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See daily and seasonal challenge progress with claimable rewards.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.primary)
        }
    }
}

@Composable
private fun AnalyticsSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(Color(0xFF00CED1), Color(0xFF008B8B))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "View your app usage and achievements",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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


@Composable
private fun SpecialEffectsSettingsCard(
    fabEffectCount: Int,
    activeEffectCount: Int,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onSpecialEffectsClick: () -> Unit,
    onFabConfigClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFFF1493), // Deep Pink
                                    Color(0xFFFF69B4), // Hot Pink
                                    Color(0xFFFFB6C1)  // Light Pink
                                )
                            ),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✨",
                        fontSize = 28.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Special Effects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "🍭 $fabEffectCount in FAB • 🎆 $activeEffectCount active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Choose Effects Button
            Button(
                onClick = onSpecialEffectsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Effects 🎨")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // FAB Configuration Button
            OutlinedButton(
                onClick = onFabConfigClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.secondary
                ),
                border = BorderStroke(1.dp, colors.secondary.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configure FAB 🎮")
                }
            }
            
            // Quick info chips
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf(
                    "🎊 Confetti" to Color(0xFFFF6B6B),
                    "🌈 Rainbow" to Color(0xFF4ECDC4),
                    "💕 Hearts" to Color(0xFFFF69B4),
                    "✨ Sparkle" to Color(0xFFFFD700)
                )
                
                chips.forEach { (label, chipColor) ->
                    AssistChip(
                        onClick = {},
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            leadingIconContentColor = chipColor
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = chipColor.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}


@Composable
private fun Effects2026SettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF00FFFF), // Cyan
                                Color(0xFFFF00FF), // Magenta
                                Color(0xFFFFFF00)  // Yellow
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🚀",
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "2026 Effects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "50+ next-gen effects with XP, levels & mini-games!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.primary
                )
                
                // Feature tags
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FeatureTag("⚛️ Quantum", colors)
                    FeatureTag("🧬 Biology", colors)
                    FeatureTag("⚫ Black Holes", colors)
                    FeatureTag("🎮 Games", colors)
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun FeatureTag(
    text: String,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    Box(
        modifier = Modifier
            .background(
                colors.primary.copy(alpha = 0.15f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = colors.primary
        )
    }
}

@Composable
private fun AccountSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(colors.primary, colors.secondary)
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "Manage your account, email, password, and data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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

@Composable
private fun NotificationSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "Manage notification preferences and quiet hours",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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

@Composable
private fun AccessibilitySettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(Color(0xFF9C27B0), Color(0xFF673AB7))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Accessibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Accessibility",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "Vision, hearing, motor, and cognitive accessibility",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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

@Composable
private fun HelpSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(Color(0xFFFF9800), Color(0xFFFFC107))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Help,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Help & Support",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "FAQs, guides, tutorials, and contact support",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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

@Composable
private fun ExtremeCustomizationSettingsCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            listOf(SugarDimens.Brand.hotPink, SugarDimens.Brand.bubblegumBlue)
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EXTREME Customization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "200+ granular controls for every aspect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
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
