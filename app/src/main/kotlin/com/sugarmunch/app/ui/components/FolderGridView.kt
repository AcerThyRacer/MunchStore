package com.sugarmunch.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sugarmunch.app.data.local.FolderEntity
import com.sugarmunch.app.data.local.FolderStyle
import com.sugarmunch.app.hub.UnifiedAppInfo
import com.sugarmunch.app.ui.viewmodels.FolderSortOption
import com.sugarmunch.app.ui.viewmodels.FolderSuggestion
import kotlinx.coroutines.launch

/**
 * Folder Grid View with Drag-and-Drop Support
 * 
 * Features:
 * - Drag apps onto folders to add them
 * - Drag apps onto apps to create new folders
 * - Drag folders onto folders to nest them
 * - Visual feedback during drag operations
 * - Haptic feedback on successful drops
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FolderGridView(
    apps: List<UnifiedAppInfo>,
    folders: List<FolderEntity>,
    onAppDroppedOnFolder: (String, String) -> Unit,
    onAppDroppedOnApp: (String, String) -> Unit,
    onFolderDroppedOnFolder: (String, String) -> Unit,
    onCreateFolder: (String, List<String>) -> Unit,
    onFolderClick: (FolderEntity) -> Unit,
    onFolderLongClick: (FolderEntity) -> Unit,
    onAppClick: (UnifiedAppInfo) -> Unit,
    onAppLongClick: (UnifiedAppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var draggedAppId by remember { mutableStateOf<String?>(null) }
    var draggedFolderId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var pendingAppsForFolder by remember { mutableStateOf<List<String>>(emptyList()) }

    // Animation for drag feedback
    val dragScale by animateFloatAsState(
        targetValue = if (draggedAppId != null || draggedFolderId != null) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        // Folders
        items(folders, key = { it.id }) { folder ->
            val isBeingDraggedOver = draggedAppId != null || draggedFolderId != null

            FolderCard(
                folder = folder,
                appCount = folder.appIds.size,
                subFolderCount = folder.subFolderIds.size,
                modifier = Modifier
                    .animateItemPlacement()
                    .scale(if (draggedFolderId == folder.id) 0.9f else 1f)
                    .pointerInput(folder.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedFolderId = folder.id
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount
                            },
                            onDragEnd = {
                                draggedFolderId = null
                                dragOffset = Offset.Zero
                            },
                            onDragCancel = {
                                draggedFolderId = null
                                dragOffset = Offset.Zero
                            }
                        )
                    }
                    .pointerInput(folder.id) {
                        detectTapGestures(
                            onTap = { onFolderClick(folder) },
                            onLongPress = { onFolderLongClick(folder) }
                        )
                    }
                    .combinedClickable(
                        onClick = { onFolderClick(folder) },
                        onLongClick = { onFolderLongClick(folder) }
                    ),
                isDropTarget = draggedAppId != null,
                onDrop = { appId ->
                    if (appId != null) {
                        onAppDroppedOnFolder(appId, folder.id)
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    }
                }
            )
        }

        // Apps (not in any folder)
        val appsNotInFolders = apps.filter { app ->
            app.folderIds.isEmpty() && !app.isPlugin
        }

        items(appsNotInFolders, key = { it.id }) { app ->
            AppGridCard(
                app = app,
                modifier = Modifier
                    .animateItemPlacement()
                    .scale(if (draggedAppId == app.id) dragScale else 1f)
                    .pointerInput(app.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedAppId = app.id
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount
                            },
                            onDragEnd = {
                                val targetAppId = appsNotInFolders
                                    .find { it.id != app.id }
                                    ?.id
                                
                                if (targetAppId != null) {
                                    onAppDroppedOnApp(app.id, targetAppId)
                                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                }
                                
                                draggedAppId = null
                                dragOffset = Offset.Zero
                            },
                            onDragCancel = {
                                draggedAppId = null
                                dragOffset = Offset.Zero
                            }
                        )
                    }
                    .combinedClickable(
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    ),
                isDropTarget = draggedAppId != null && draggedAppId != app.id,
                onDrop = { sourceAppId ->
                    if (sourceAppId != null && sourceAppId != app.id) {
                        onAppDroppedOnApp(sourceAppId, app.id)
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    }
                }
            )
        }
    }

    // Drag feedback overlay
    if (draggedAppId != null || draggedFolderId != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            draggedAppId = null
                            draggedFolderId = null
                            dragOffset = Offset.Zero
                        }
                    )
                }
        ) {
            // Visual hint for drop targets
            if (draggedAppId != null) {
                Text(
                    text = "Drag to a folder or another app",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            apps = pendingAppsForFolder.mapNotNull { id -> apps.find { it.id == id } },
            onCreate = { name, appIds ->
                onCreateFolder(name, appIds)
                showCreateFolderDialog = false
                pendingAppsForFolder = emptyList()
            },
            onDismiss = {
                showCreateFolderDialog = false
                pendingAppsForFolder = emptyList()
            }
        )
    }
}

/**
 * Folder Card with Quick Actions Menu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderCard(
    folder: FolderEntity,
    appCount: Int,
    subFolderCount: Int,
    modifier: Modifier = Modifier,
    isDropTarget: Boolean = false,
    onDrop: () -> Unit = {}
) {
    val context = LocalContext.current
    var showQuickActions by remember { mutableStateOf(false) }

    // Get folder style colors
    val folderStyle = FolderStyle.valueOfOrNull(folder.folderStyle) ?: FolderStyle.DEFAULT
    val backgroundColor = folder.backgroundColor?.let { Color(it) } ?: folderStyle.defaultBackground
    val iconColor = folder.iconColor?.let { Color(it) } ?: folderStyle.defaultIcon

    Card(
        modifier = modifier
            .size(110.dp)
            .then(
                if (isDropTarget) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Folder Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                iconColor.copy(alpha = 0.8f),
                                iconColor
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )

                // Subfolder indicator
                if (subFolderCount > 0) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(subFolderCount.toString())
                    }
                }
            }

            // Folder Name
            Text(
                text = folder.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // App Count
            Text(
                text = "$appCount items",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }

    // Quick Actions Menu
    if (showQuickActions) {
        FolderQuickActionsMenu(
            folder = folder,
            onDismiss = { showQuickActions = false },
            onRename = { },
            onChangeStyle = { },
            onChangeColor = { },
            onAutoSort = { },
            onSetFavorite = { },
            onDelete = { }
        )
    }
}

/**
 * App Grid Card
 */
@Composable
fun AppGridCard(
    app: UnifiedAppInfo,
    modifier: Modifier = Modifier,
    isDropTarget: Boolean = false,
    onDrop: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .size(90.dp)
            .then(
                if (isDropTarget) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (app.icon != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .drawable(app.icon)
                            .build(),
                        contentDescription = app.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (app.iconUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(app.iconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = app.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback icon
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(10.dp),
                        color = app.accentColor?.let { Color(it) } ?: Color.Gray
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = app.name.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            // App Name
            Text(
                text = app.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Folder Quick Actions Menu
 */
@Composable
private fun FolderQuickActionsMenu(
    folder: FolderEntity,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onChangeStyle: () -> Unit,
    onChangeColor: () -> Unit,
    onAutoSort: () -> Unit,
    onSetFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showStyleSelector by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Folder Options") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionRow(
                    icon = Icons.Default.Edit,
                    label = "Rename",
                    onClick = {
                        onDismiss()
                        showRenameDialog = true
                    }
                )

                QuickActionRow(
                    icon = Icons.Default.Palette,
                    label = "Change Style",
                    onClick = {
                        onDismiss()
                        showStyleSelector = true
                    }
                )

                QuickActionRow(
                    icon = Icons.Default.Colorize,
                    label = "Change Color",
                    onClick = {
                        onDismiss()
                        showColorPicker = true
                    }
                )

                QuickActionRow(
                    icon = Icons.Default.Sort,
                    label = "Auto-Sort Apps",
                    onClick = {
                        onDismiss()
                        showSortOptions = true
                    }
                )

                QuickActionRow(
                    icon = Icons.Default.Star,
                    label = "Set as Favorite",
                    onClick = {
                        onDismiss()
                        onSetFavorite()
                    }
                )

                Divider()

                QuickActionRow(
                    icon = Icons.Default.Delete,
                    label = "Delete Folder",
                    isDestructive = true,
                    onClick = {
                        onDismiss()
                        showDeleteConfirm = true
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    // Sub-dialogs would be shown here
    if (showRenameDialog) {
        RenameFolderDialog(
            currentName = folder.name,
            onRename = { newName ->
                onRename()
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Folder?") },
            text = { Text("This will remove the folder but keep the apps.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuickActionRow(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) Color.Red else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                color = if (isDestructive) Color.Red else MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

/**
 * Create Folder Dialog
 */
@Composable
private fun CreateFolderDialog(
    apps: List<UnifiedAppInfo>,
    onCreate: (String, List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("New Folder") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Folder") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (apps.isNotEmpty()) {
                    Text(
                        text = "Apps in this folder:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        apps.forEach { app ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (app.icon != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .drawable(app.icon)
                                                .build(),
                                            contentDescription = app.name,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = app.displayName,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val appIds = apps.map { it.id }
                    onCreate(folderName, appIds)
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Rename Folder Dialog
 */
@Composable
private fun RenameFolderDialog(
    currentName: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Folder") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onRename(newName) }
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Folder Suggestions Card
 */
@Composable
fun FolderSuggestionsCard(
    suggestions: List<FolderSuggestion>,
    onSuggestionSelected: (FolderSuggestion) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suggested Folders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss")
                }
            }

            suggestions.forEach { suggestion ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(onClick = { onSuggestionSelected(suggestion) }),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = suggestion.folder?.name ?: suggestion.suggestedName ?: "New Folder",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = suggestion.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            imageVector = if (suggestion.shouldCreate) Icons.Default.Add else Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// Helper extension for FolderStyle
private fun FolderStyle.Companion.valueOfOrNull(name: String): FolderStyle? {
    return try {
        FolderStyle.valueOf(name)
    } catch (e: IllegalArgumentException) {
        null
    }
}
