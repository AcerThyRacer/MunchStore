package com.sugarmunch.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager

/**
 * Notification settings data class
 */
data class NotificationSettings(
    val dailyRewardReminders: Boolean = true,
    val clanWarNotifications: Boolean = true,
    val tradeAlerts: Boolean = true,
    val achievementUnlocks: Boolean = true,
    val themeRecommendations: Boolean = false,
    val effectRecommendations: Boolean = false,
    val seasonalEventReminders: Boolean = true,
    val batteryOptimizationWarnings: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val notificationSound: String = "default",
    val vibrationEnabled: Boolean = true,
    val vibrationPattern: VibrationPattern = VibrationPattern.SHORT,
    val priority: NotificationPriority = NotificationPriority.NORMAL
)

enum class VibrationPattern {
    SHORT,
    MEDIUM,
    LONG,
    CUSTOM
}

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    settings: NotificationSettings = NotificationSettings(),
    onSettingsChange: (NotificationSettings) -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    var notificationSettings by remember { mutableStateOf(settings) }
    var showQuietHoursDialog by remember { mutableStateOf(false) }

    // Check notification permission
    val hasNotificationPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
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
                // Permission Status Card
                if (!hasNotificationPermission) {
                    PermissionWarningCard(
                        colors = colors,
                        onRequestPermission = {
                            // Request notification permission
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Rewards & Events Card
                NotificationCategoryCard(
                    title = "Rewards & Events",
                    icon = Icons.Default.Celebration,
                    colors = colors
                ) {
                    NotificationToggleRow(
                        icon = Icons.Default.CardGiftcard,
                        label = "Daily Reward Reminders",
                        description = "Get reminded to claim your daily rewards",
                        checked = notificationSettings.dailyRewardReminders,
                        onCheckedChange = {
                            notificationSettings = notificationSettings.copy(
                                dailyRewardReminders = it
                            )
                            onSettingsChange(notificationSettings)
                        }
                    )

                    NotificationToggleRow(
                        icon = Icons.Default.Events,
                        label = "Seasonal Event Reminders",
                        description = "Notifications about special events and seasons",
                        checked = notificationSettings.seasonalEventReminders,
                        onCheckedChange = {
                            notificationSettings = notificationSettings.copy(
                                seasonalEventReminders = it
                            )
                            onSettingsChange(notificationSettings)
                        }
                    )

                    NotificationToggleRow(
                        icon = Icons.Default.Star,
                        label = "Achievement Unlocks",
                        description = "Celebrate when you unlock achievements",
                        checked = notificationSettings.achievementUnlocks,
                        onCheckedChange = {
                            notificationSettings = notificationSettings.copy(
                                achievementUnlocks = it
                            )
                            onSettingsChange(notificationSettings)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Social & Trading Card
                NotificationCategoryCard(
                    title = "Social & Trading",
                    icon = Icons.Default.People,
                    colors = colors
                ) {
                    NotificationToggleRow(
                        icon = Icons.Default.Shield,
                        label = "Clan War Notifications",
                        description = "Updates about clan wars and battles",
                        checked = notificationSettings.clanWarNotifications,
                        onCheckedChange = {
                            notificationSettings = notificationSettings.copy(
                                clanWarNotifications = it
                            )
                            onSettingsChange(notificationSettings)
                        }
                    )

                    NotificationToggleRow(
                        icon = Icons.Default.SwapHoriz,
                        label = "Trade Alerts",
                        description = "Notifications about your trades and offers",
                        checked = notificationSettings.tradeAlerts,
                        onCheckedChange = {
                            notificationSettings = notificationSettings.copy(
                                tradeAlerts = it
                            )
                            onSettingsChange(notificationSettings)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recommendations Card
                NotificationCategoryCard(
                    title = "Recommendations",
                    icon = Icons.Default.Lightbulb,
                    colors = colors
                ) {
                    NotificationToggleRow(
                        icon = Icons.Default.Palette,
                        label = "Theme Recommendations",
                        description = "Discover new themes based on your preferences",
                        checked = notificationSettings.themeRecommendations,
                        onCheckedChange = {
                            notificationSettings = notificationSettings.copy(
                                themeRecommendations = it
                            )
                            onSettingsChange(notificationSettings)
                        }
                    )

                    NotificationToggleRow(
                        icon = Icons.Default.AutoAwesome,
                        label = "Effect Recommendations",
                        description = "Discover new effects to try",
                        checked = notificationSettings.effectRecommendations,
                        onCheckedChange = {
                            notificationSettings = notificationSettings.copy(
                                effectRecommendations = it
                            )
                            onSettingsChange(notificationSettings)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // System Card
                NotificationCategoryCard(
                    title = "System",
                    icon = Icons.Default.Settings,
                    colors = colors
                ) {
                    NotificationToggleRow(
                        icon = Icons.Default.BatteryAlert,
                        label = "Battery Optimization Warnings",
                        description = "Alerts about battery usage and optimization",
                        checked = notificationSettings.batteryOptimizationWarnings,
                        onCheckedChange = {
                            notificationSettings = notificationSettings.copy(
                                batteryOptimizationWarnings = it
                            )
                            onSettingsChange(notificationSettings)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quiet Hours Card
                QuietHoursCard(
                    settings = notificationSettings,
                    colors = colors,
                    onToggleQuietHours = {
                        notificationSettings = notificationSettings.copy(
                            quietHoursEnabled = it
                        )
                        onSettingsChange(notificationSettings)
                    },
                    onEditQuietHours = { showQuietHoursDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notification Behavior Card
                NotificationBehaviorCard(
                    settings = notificationSettings,
                    colors = colors,
                    onSoundChange = { /* TODO: Implement sound picker */ },
                    onVibrationChange = {
                        notificationSettings = notificationSettings.copy(
                            vibrationEnabled = it
                        )
                        onSettingsChange(notificationSettings)
                    },
                    onPatternChange = { pattern ->
                        notificationSettings = notificationSettings.copy(
                            vibrationPattern = pattern
                        )
                        onSettingsChange(notificationSettings)
                    },
                    onPriorityChange = { priority ->
                        notificationSettings = notificationSettings.copy(
                            priority = priority
                        )
                        onSettingsChange(notificationSettings)
                    }
                )
            }
        }
    }
}

@Composable
private fun PermissionWarningCard(
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.warning.copy(alpha = 0.2f)
        ),
        border = CardDefaults.outlinedCardBorder.copy(
            brush = Brush.linearGradient(listOf(colors.warning, colors.warning))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.NotificationImportant,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notification Permission Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "Enable notifications to receive rewards and updates",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            }

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.warning
                )
            ) {
                Text("Enable")
            }
        }
    }
}

@Composable
private fun NotificationCategoryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    content: @Composable ColumnScope.() -> Unit
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
            Row(
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun NotificationToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun QuietHoursCard(
    settings: NotificationSettings,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onToggleQuietHours: (Boolean) -> Unit,
    onEditQuietHours: () -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Nightlight,
                    contentDescription = null,
                    tint = colors.secondary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Quiet Hours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Schedule quiet hours",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface
                    )
                    if (settings.quietHoursEnabled) {
                        Text(
                            text = "${settings.quietHoursStart} - ${settings.quietHoursEnd}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Switch(
                    checked = settings.quietHoursEnabled,
                    onCheckedChange = onToggleQuietHours
                )
            }

            if (settings.quietHoursEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onEditQuietHours,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.secondary
                    )
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Schedule")
                }
            }
        }
    }
}

@Composable
private fun NotificationBehaviorCard(
    settings: NotificationSettings,
    colors: com.sugarmunch.app.theme.model.AdjustedColors,
    onSoundChange: () -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onPatternChange: (VibrationPattern) -> Unit,
    onPriorityChange: (NotificationPriority) -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    tint = colors.tertiary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Notification Behavior",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sound
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notification Sound",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = settings.notificationSound.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onSoundChange,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.primary
                    )
                ) {
                    Text("Change")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vibration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Vibration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vibration",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (settings.vibrationEnabled) {
                        Text(
                            text = settings.vibrationPattern.name.replaceFirstChar { it.lowercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = settings.vibrationEnabled,
                    onCheckedChange = onVibrationChange
                )
            }

            if (settings.vibrationEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VibrationPattern.entries.forEach { pattern ->
                        FilterChip(
                            selected = settings.vibrationPattern == pattern,
                            onClick = { onPatternChange(pattern) },
                            label = { Text(pattern.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PriorityHigh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notification Priority",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = settings.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NotificationPriority.entries.forEach { priority ->
                        FilterChip(
                            selected = settings.priority == priority,
                            onClick = { onPriorityChange(priority) },
                            label = { Text(priority.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary
                            )
                        )
                    }
                }
            }
        }
    }
}
