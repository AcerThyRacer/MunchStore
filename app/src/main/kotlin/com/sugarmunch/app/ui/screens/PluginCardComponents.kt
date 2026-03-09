package com.sugarmunch.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sugarmunch.app.plugin.model.PluginCategory
import com.sugarmunch.app.plugin.model.StorePlugin
import com.sugarmunch.app.plugin.store.InstallStatus
import com.sugarmunch.app.data.local.FolderEntity

/**
 * Featured Plugin Card (Horizontal scroll layout)
 */
@Composable
fun FeaturedPluginCard(
    plugin: StorePlugin,
    isInstalled: Boolean,
    installStatus: InstallStatus,
    downloadProgress: Float,
    onInstallClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onClick: () -> Unit,
    onAddToFolder: (String) -> Unit,
    folders: List<FolderEntity>,
    pluginFolderIds: List<String>,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    var showFolderMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(220.dp)
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Plugin Icon
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(plugin.iconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = plugin.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Plugin Name
            Text(
                text = plugin.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Author
            Text(
                text = "by ${plugin.author}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", plugin.rating),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurface
                )
                Text(
                    text = " (${plugin.ratingCount})",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Downloads
            Text(
                text = "${plugin.formattedDownloads} downloads",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category Badge
            AssistChip(
                onClick = { },
                label = { Text(plugin.category.name, style = MaterialTheme.typography.labelSmall) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = colors.primary.copy(alpha = 0.2f)
                ),
                border = AssistChipDefaults.assistChipBorder(
                    borderColor = colors.primary.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Action Button / Progress
            when (installStatus) {
                InstallStatus.DOWNLOADING -> {
                    LinearProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                InstallStatus.INSTALLING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.primary
                    )
                    Text(
                        text = "Installing...",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                InstallStatus.INSTALLED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { showFolderMenu = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.surfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Button(
                            onClick = onUpdateClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Update")
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onInstallClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Install")
                    }
                }
            }
        }
    }

    // Folder selection menu
    if (showFolderMenu) {
        FolderSelectionDropdown(
            folders = folders,
            selectedFolderIds = pluginFolderIds,
            onFolderSelected = onAddToFolder,
            onDismiss = { showFolderMenu = false }
        )
    }
}

/**
 * Standard Plugin Card (Grid layout)
 */
@Composable
fun PluginCard(
    plugin: StorePlugin,
    isInstalled: Boolean,
    installStatus: InstallStatus,
    downloadProgress: Float,
    onInstallClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onClick: () -> Unit,
    onAddToFolder: (String) -> Unit,
    folders: List<FolderEntity>,
    pluginFolderIds: List<String>,
    userRating: Int?,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    var showFolderMenu by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }

    Card(
        onClick = { showDetails = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plugin Icon
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(plugin.iconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = plugin.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Plugin Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name and Featured Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (plugin.isFeatured) {
                        Badge(
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text("Featured")
                        }
                    }
                }

                // Author
                Text(
                    text = "by ${plugin.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )

                // Rating and Downloads Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", plugin.rating),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurface
                        )
                    }
                    Text(
                        text = "${plugin.formattedDownloads} downloads",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.5f)
                    )
                }

                // User's rating indicator
                if (userRating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Your rating: $userRating/5",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (installStatus) {
                    InstallStatus.DOWNLOADING -> {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = downloadProgress,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = colors.primary
                            )
                            Text(
                                text = "${(downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface
                            )
                        }
                    }
                    InstallStatus.INSTALLING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = colors.primary
                        )
                    }
                    InstallStatus.INSTALLED -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { showFolderMenu = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Folder,
                                    contentDescription = "Add to folder",
                                    tint = colors.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onUpdateClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Update,
                                    contentDescription = "Update",
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    else -> {
                        FilledTonalIconButton(
                            onClick = onInstallClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Install",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Folder badges
                if (pluginFolderIds.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        pluginFolderIds.take(3).forEach { folderId ->
                            val folder = folders.find { it.id == folderId }
                            if (folder != null) {
                                Surface(
                                    modifier = Modifier.size(16.dp),
                                    shape = CircleShape,
                                    color = Color(folder.iconColor ?: "#888888")
                                ) {}
                            }
                        }
                        if (pluginFolderIds.size > 3) {
                            Text(
                                text = "+${pluginFolderIds.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Folder selection menu
    if (showFolderMenu) {
        FolderSelectionDropdown(
            folders = folders,
            selectedFolderIds = pluginFolderIds,
            onFolderSelected = onAddToFolder,
            onDismiss = { showFolderMenu = false }
        )
    }

    // Details dialog
    if (showDetails) {
        PluginDetailsDialog(
            plugin = plugin,
            isInstalled = isInstalled,
            installStatus = installStatus,
            downloadProgress = downloadProgress,
            userRating = userRating,
            folders = folders,
            pluginFolderIds = pluginFolderIds,
            onDismiss = { showDetails = false },
            onInstallClick = onInstallClick,
            onUninstallClick = onUninstallClick,
            onUpdateClick = onUpdateClick,
            onRatePlugin = { },
            onAddToFolder = onAddToFolder,
            colors = colors
        )
    }
}

/**
 * Folder Selection Dropdown Menu
 */
@Composable
private fun FolderSelectionDropdown(
    folders: List<FolderEntity>,
    selectedFolderIds: List<String>,
    onFolderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    if (expanded) {
        AlertDialog(
            onDismissRequest = {
                expanded = false
                onDismiss()
            },
            title = { Text("Add to Folder") },
            text = {
                LazyColumn {
                    items(folders) { folder ->
                        val isSelected = selectedFolderIds.contains(folder.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onFolderSelected(folder.id)
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(folder.iconColor ?: "#888888"))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(folder.name)
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    expanded = false
                    onDismiss()
                }) {
                    Text("Done")
                }
            }
        )
    }
}

/**
 * Plugin Details Dialog
 */
@Composable
fun PluginDetailsDialog(
    plugin: StorePlugin,
    isInstalled: Boolean,
    installStatus: InstallStatus,
    downloadProgress: Float,
    userRating: Int?,
    folders: List<FolderEntity>,
    pluginFolderIds: List<String>,
    onDismiss: () -> Unit,
    onInstallClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onRatePlugin: (Int) -> Unit,
    onAddToFolder: (String) -> Unit,
    colors: com.sugarmunch.app.theme.model.AdjustedColors
) {
    var showFolderMenu by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableStateOf(userRating ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(plugin.iconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = plugin.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "by ${plugin.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Description
                item {
                    Text(
                        text = plugin.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface
                    )
                }

                // Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Rating
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", plugin.rating),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                            }
                            Text(
                                text = "${plugin.ratingCount} reviews",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        // Downloads
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = plugin.formattedDownloads,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "downloads",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        // Size
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = plugin.formattedSize,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "size",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // User Rating
                if (isInstalled) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = colors.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Your Rating",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.onSurface
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    (1..5).forEach { star ->
                                        IconButton(
                                            onClick = {
                                                selectedRating = star
                                                onRatePlugin(star)
                                            }
                                        ) {
                                            Icon(
                                                if (star <= selectedRating) Icons.Default.Star else Icons.Outlined.Star,
                                                contentDescription = "$star stars",
                                                tint = if (star <= selectedRating) Color(0xFFFFC107) else colors.onSurface.copy(alpha = 0.3f),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Folder Membership
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "In Folders",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onSurface
                            )
                            if (pluginFolderIds.isEmpty()) {
                                Text(
                                    text = "Not in any folder",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurface.copy(alpha = 0.5f)
                                )
                            } else {
                                pluginFolderIds.forEach { folderId ->
                                    val folder = folders.find { it.id == folderId }
                                    if (folder != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(folder.iconColor ?: "#888888"))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = folder.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                            TextButton(
                                onClick = { showFolderMenu = true }
                            ) {
                                Text("Manage Folders")
                            }
                        }
                    }
                }

                // Category and Tags
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text(plugin.category.name) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colors.primary.copy(alpha = 0.2f)
                            )
                        )
                        plugin.tags.take(3).forEach { tag ->
                            AssistChip(
                                onClick = { },
                                label = { Text("#$tag") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = colors.surfaceVariant
                                )
                            )
                        }
                    }
                }

                // Version Info
                item {
                    Text(
                        text = "Version ${plugin.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        },
        confirmButton = {
            when (installStatus) {
                InstallStatus.DOWNLOADING -> {
                    Text(
                        text = "${(downloadProgress * 100).toInt()}%",
                        color = colors.onSurface
                    )
                }
                InstallStatus.INSTALLING -> {
                    Text(
                        text = "Installing...",
                        color = colors.onSurface
                    )
                }
                InstallStatus.INSTALLED -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onUninstallClick) {
                            Text("Uninstall")
                        }
                        Button(onClick = onUpdateClick) {
                            Text("Update")
                        }
                    }
                }
                else -> {
                    Button(onClick = onInstallClick) {
                        Text("Install")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    // Folder selection menu
    if (showFolderMenu) {
        FolderSelectionDropdown(
            folders = folders,
            selectedFolderIds = pluginFolderIds,
            onFolderSelected = onAddToFolder,
            onDismiss = { showFolderMenu = false }
        )
    }
}

// Required for AsyncImage
@Composable
private fun LocalContext(): android.content.Context {
    return androidx.compose.ui.platform.LocalContext.current
}
