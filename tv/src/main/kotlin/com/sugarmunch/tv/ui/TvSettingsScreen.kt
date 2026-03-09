package com.sugarmunch.tv.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import com.sugarmunch.tv.presentation.TvMainViewModel

/**
 * TV Settings Screen
 * Settings interface optimized for TV with:
 * - Leanback Preference-style layout
 * - D-pad navigation
 * - Categories: General, Downloads, Effects, About
 * - Toggle switches and navigation items
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvSettingsScreen(
    viewModel: TvMainViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Customize your SugarMunch TV experience",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // General Settings
        item {
            SettingsCategory(
                title = "General",
                icon = Icons.Default.Settings
            ) {
                SettingToggleItem(
                    title = "Auto-update Apps",
                    description = "Automatically check for app updates",
                    icon = Icons.Default.Update,
                    defaultValue = true
                )

                SettingToggleItem(
                    title = "Notifications",
                    description = "Show notifications for new apps",
                    icon = Icons.Default.Notifications,
                    defaultValue = true
                )

                SettingToggleItem(
                    title = "Analytics",
                    description = "Help improve SugarMunch with usage data",
                    icon = Icons.Default.Analytics,
                    defaultValue = false
                )
            }
        }

        // Downloads Settings
        item {
            SettingsCategory(
                title = "Downloads",
                icon = Icons.Default.CloudDownload
            ) {
                SettingNavigationItem(
                    title = "Download Location",
                    description = "/storage/emulated/0/Download/SugarMunch",
                    icon = Icons.Default.Storage
                )

                SettingToggleItem(
                    title = "Wi-Fi Only",
                    description = "Only download apps on Wi-Fi",
                    icon = Icons.Default.Wifi,
                    defaultValue = true
                )

                SettingToggleItem(
                    title = "Auto-install",
                    description = "Install apps automatically after download",
                    icon = Icons.Default.VerifiedUser,
                    defaultValue = false
                )

                SettingActionItem(
                    title = "Clear Cache",
                    description = "Free up storage space",
                    icon = Icons.Default.Delete,
                    onClick = { /* Clear cache */ }
                )
            }
        }

        // Effects Settings
        item {
            SettingsCategory(
                title = "Effects & Appearance",
                icon = Icons.AutoMirrored.Filled.Label
            ) {
                SettingToggleItem(
                    title = "TV Animations",
                    description = "Enable smooth transitions and animations",
                    icon = Icons.Default.Refresh,
                    defaultValue = true
                )

                SettingToggleItem(
                    title = "Sound Effects",
                    description = "Play sounds on navigation",
                    icon = Icons.Default.Build,
                    defaultValue = true
                )

                SettingNavigationItem(
                    title = "Theme Settings",
                    description = "Change colors and appearance",
                    icon = Icons.Default.Settings
                )
            }
        }

        // Security Settings
        item {
            SettingsCategory(
                title = "Security",
                icon = Icons.Default.Security
            ) {
                SettingToggleItem(
                    title = "Verify APK Signatures",
                    description = "Check app authenticity before install",
                    icon = Icons.Default.VerifiedUser,
                    defaultValue = true
                )

                SettingToggleItem(
                    title = "Scan Downloads",
                    description = "Scan apps for malware",
                    icon = Icons.Default.Security,
                    defaultValue = true
                )
            }
        }

        // About Section
        item {
            SettingsCategory(
                title = "About",
                icon = Icons.Default.Info
            ) {
                SettingInfoItem(
                    title = "Version",
                    value = "1.0.0 (TV Edition)",
                    icon = Icons.Default.Build
                )

                SettingNavigationItem(
                    title = "Privacy Policy",
                    description = "Read our privacy policy",
                    icon = Icons.Default.Security
                )

                SettingNavigationItem(
                    title = "Terms of Service",
                    description = "Read the terms of service",
                    icon = Icons.AutoMirrored.Filled.Label
                )

                SettingNavigationItem(
                    title = "Help & Support",
                    description = "Get help with SugarMunch TV",
                    icon = Icons.AutoMirrored.Filled.Help
                )

                SettingActionItem(
                    title = "Check for Updates",
                    description = "Look for new versions",
                    icon = Icons.Default.Update,
                    onClick = { /* Check updates */ }
                )

                SettingActionItem(
                    title = "Visit Website",
                    description = "Open sugarmunch.com",
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    onClick = { /* Open website */ }
                )
            }
        }

        // Footer
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SugarMunch TV Edition",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Made with 💜 for Android TV",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsCategory(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    defaultValue: Boolean
) {
    var checked by remember { mutableStateOf(defaultValue) }
    var isFocused by remember { mutableStateOf(false) }

    ListItem(
        selected = false,
        onClick = { checked = !checked },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isFocused) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = { checked = it }
            )
        }
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingNavigationItem(
    title: String,
    description: String,
    icon: ImageVector
) {
    var isFocused by remember { mutableStateOf(false) }

    ListItem(
        selected = false,
        onClick = { /* Navigate */ },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isFocused) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingInfoItem(
    title: String,
    value: String,
    icon: ImageVector
) {
    ListItem(
        selected = false,
        onClick = { },
        modifier = Modifier.fillMaxWidth(),
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingActionItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    ListItem(
        selected = false,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isFocused) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
