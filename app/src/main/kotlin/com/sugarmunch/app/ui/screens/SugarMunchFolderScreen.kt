package com.sugarmunch.app.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.data.local.FolderEntity
import com.sugarmunch.app.data.local.FolderStyle
import com.sugarmunch.app.data.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SugarMunch Folder Screen - Main launcher home with folder organization
 */
@Composable
fun SugarMunchFolderScreen(
    viewModel: SugarMunchFolderViewModel = hiltViewModel(),
    onFolderClick: (String) -> Unit,
    onAppClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            SugarMunchHeader(
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Access Bar
            QuickAccessBar(
                folders = uiState.rootFolders.take(5),
                onFolderClick = onFolderClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Folder Grid
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.rootFolders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No folders yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.rootFolders, key = { it.id }) { folder ->
                            FolderCard(
                                folder = folder,
                                appCount = uiState.appCounts[folder.id] ?: 0,
                                onClick = { onFolderClick(folder.id) },
                                onLongClick = {
                                    // Show folder options menu
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button for creating new folder
        FloatingActionButton(
            onClick = { viewModel.showCreateFolderDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Folder")
        }
    }

    // Create Folder Dialog
    if (uiState.showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { viewModel.dismissCreateFolderDialog() },
            onCreate = { name, style ->
                viewModel.createFolder(name, style)
            }
        )
    }
}

@Composable
private fun SugarMunchHeader(
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SugarMunch",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF69B4), // Hot pink
                            Color(0xFF00FFA3), // Mint
                            Color(0xFFFFD700)  // Yellow
                        )
                    )
                )
            )
            Text(
                text = "Your apps, organized beautifully",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}

@Composable
private fun QuickAccessBar(
    folders: List<FolderEntity>,
    onFolderClick: (String) -> Unit
) {
    if (folders.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            folders.forEach { folder ->
                QuickAccessItem(
                    folder = folder,
                    onClick = { onFolderClick(folder.id) }
                )
            }
        }
    }
}

@Composable
private fun QuickAccessItem(
    folder: FolderEntity,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(getFolderBackgroundBrush(folder.folderStyle))
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = folder.name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = folder.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderCard(
    folder: FolderEntity,
    appCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .aspectRatio(0.8f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onLongPress = { onLongClick() },
                    onTap = { onClick() }
                )
            }
            .graphicsLayer {
                scaleX = if (isPressed) 0.95f else 1f
                scaleY = if (isPressed) 0.95f else 1f
                shadowElevation = if (isPressed) 8.dp.toPx() else 16.dp.toPx()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getFolderBackgroundBrush(folder.folderStyle))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Folder Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = folder.name,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Folder Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$appCount apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Glassmorphic overlay effect
            if (folder.folderStyle == FolderStyle.GLASSMORPHIC.name) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .blur(8.dp)
                )
            }
        }
    }
}

@Composable
private fun getFolderBackgroundBrush(style: String): Brush {
    return when (FolderStyle.valueOf(style)) {
        FolderStyle.DEFAULT -> Brush.linearGradient(
            colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
        )
        FolderStyle.GLASSMORPHIC -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF667eea).copy(alpha = 0.7f),
                Color(0xFF764ba2).copy(alpha = 0.7f)
            )
        )
        FolderStyle.NEON -> Brush.linearGradient(
            colors = listOf(Color(0xFFFF00FF), Color(0xFF00FFFF))
        )
        FolderStyle.HOLOGRAPHIC -> Brush.sweepGradient(
            colors = listOf(
                Color(0xFFFF0080),
                Color(0xFF00FFFF),
                Color(0xFF80FF00),
                Color(0xFFFF0080)
            )
        )
        FolderStyle.LIQUID -> Brush.radialGradient(
            colors = listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))
        )
        FolderStyle.CRYSTAL -> Brush.linearGradient(
            colors = listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
        )
    }
}

@Composable
private fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String, FolderStyle) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(FolderStyle.DEFAULT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Folder") },
        text = {
            Column {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Style",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FolderStyle.values()) { style ->
                        StyleOption(
                            style = style,
                            isSelected = selectedStyle == style,
                            onClick = { selectedStyle = style }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(folderName, selectedStyle) },
                enabled = folderName.isNotBlank()
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

@Composable
private fun StyleOption(
    style: FolderStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getFolderBackgroundBrush(style.name))
        ) {
            Text(
                text = style.name,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * ViewModel for SugarMunch Folder Screen
 */
@HiltViewModel
class SugarMunchFolderViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SugarMunchFolderUiState())
    val uiState: StateFlow<SugarMunchFolderUiState> = _uiState.asStateFlow()

    init {
        loadFolders()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            folderRepository.getRootFolders().collect { folders ->
                val appCounts = mutableMapOf<String, Int>()
                folders.forEach { folder ->
                    appCounts[folder.id] = folder.appIds.size
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    rootFolders = folders,
                    appCounts = appCounts
                )
            }
        }
    }

    fun createFolder(name: String, style: FolderStyle) {
        viewModelScope.launch {
            folderRepository.createFolder(name, style = style)
            dismissCreateFolderDialog()
        }
    }

    fun showCreateFolderDialog() {
        _uiState.value = _uiState.value.copy(showCreateFolderDialog = true)
    }

    fun dismissCreateFolderDialog() {
        _uiState.value = _uiState.value.copy(showCreateFolderDialog = false)
    }
}

data class SugarMunchFolderUiState(
    val isLoading: Boolean = false,
    val rootFolders: List<FolderEntity> = emptyList(),
    val appCounts: Map<String, Int> = emptyMap(),
    val showCreateFolderDialog: Boolean = false
)
