package com.sugarmunch.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.data.local.FolderEntity
import com.sugarmunch.app.data.local.FolderStyle
import com.sugarmunch.app.data.repository.FolderRepository
import com.sugarmunch.app.hub.AllAppsManager
import com.sugarmunch.app.hub.UnifiedAppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Folder Management
 * Handles folder CRUD operations, drag-and-drop, and smart suggestions
 */
@HiltViewModel
class FolderViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val allAppsManager: AllAppsManager
) : ViewModel() {

    // ========== STATE FLOWS ==========

    /** All folders */
    val allFolders: StateFlow<List<FolderEntity>> = folderRepository.getAllFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Root folders (no parent) */
    val rootFolders: StateFlow<List<FolderEntity>> = folderRepository.getRootFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Custom (user-created) folders */
    val customFolders: StateFlow<List<FolderEntity>> = folderRepository.getCustomFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** All apps for drag-and-drop */
    val allApps: StateFlow<List<UnifiedAppInfo>> = allAppsManager.allApps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ========== UI STATE ==========

    private val _uiState = MutableStateFlow(FolderUiState())
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    private val _selectedFolder = MutableStateFlow<FolderEntity?>(null)
    val selectedFolder: StateFlow<FolderEntity?> = _selectedFolder.asStateFlow()

    private val _draggedAppId = MutableStateFlow<String?>(null)
    val draggedAppId: StateFlow<String?> = _draggedAppId.asStateFlow()

    private val _draggedFolderId = MutableStateFlow<String?>(null)
    val draggedFolderId: StateFlow<String?> = _draggedFolderId.asStateFlow()

    private val _folderSuggestions = MutableStateFlow<List<FolderSuggestion>>(emptyList())
    val folderSuggestions: StateFlow<List<FolderSuggestion>> = _folderSuggestions.asStateFlow()

    // ========== FOLDER CRUD ==========

    /**
     * Create a new folder
     */
    fun createFolder(
        name: String,
        parentId: String? = null,
        style: FolderStyle = FolderStyle.DEFAULT,
        iconColor: String? = null,
        backgroundColor: String? = null
    ) {
        viewModelScope.launch {
            folderRepository.createFolder(
                name = name,
                parentId = parentId,
                style = style,
                iconColor = iconColor,
                backgroundColor = backgroundColor,
                isSystemFolder = false
            )
        }
    }

    /**
     * Update an existing folder
     */
    fun updateFolder(folder: FolderEntity) {
        viewModelScope.launch {
            folderRepository.updateFolder(folder)
        }
    }

    /**
     * Delete a folder
     */
    fun deleteFolder(folder: FolderEntity) {
        viewModelScope.launch {
            folderRepository.deleteFolder(folder)
            if (_selectedFolder.value?.id == folder.id) {
                _selectedFolder.value = null
            }
        }
    }

    /**
     * Add an app to a folder
     */
    fun addAppToFolder(folderId: String, appId: String) {
        viewModelScope.launch {
            folderRepository.addAppToFolder(folderId, appId)
        }
    }

    /**
     * Remove an app from a folder
     */
    fun removeAppFromFolder(folderId: String, appId: String) {
        viewModelScope.launch {
            folderRepository.removeAppFromFolder(folderId, appId)
        }
    }

    /**
     * Add a subfolder to a parent folder
     */
    fun addSubFolder(parentId: String, subFolderId: String) {
        viewModelScope.launch {
            folderRepository.addSubFolder(parentId, subFolderId)
        }
    }

    /**
     * Remove a subfolder from a parent folder
     */
    fun removeSubFolder(parentId: String, subFolderId: String) {
        viewModelScope.launch {
            folderRepository.removeSubFolder(parentId, subFolderId)
        }
    }

    // ========== DRAG AND DROP ==========

    /**
     * Start dragging an app
     */
    fun startDragApp(appId: String) {
        _draggedAppId.value = appId
        _uiState.value = _uiState.value.copy(isDraggingApp = true)
    }

    /**
     * Stop dragging an app
     */
    fun stopDragApp() {
        _draggedAppId.value = null
        _uiState.value = _uiState.value.copy(isDraggingApp = false)
    }

    /**
     * Start dragging a folder
     */
    fun startDragFolder(folderId: String) {
        _draggedFolderId.value = folderId
        _uiState.value = _uiState.value.copy(isDraggingFolder = true)
    }

    /**
     * Stop dragging a folder
     */
    fun stopDragFolder() {
        _draggedFolderId.value = null
        _uiState.value = _uiState.value.copy(isDraggingFolder = false)
    }

    /**
     * Drop app on folder
     */
    fun dropAppOnFolder(appId: String, folderId: String) {
        viewModelScope.launch {
            // Check if app is already in folder
            val folder = folderRepository.getFolderById(folderId)
            if (folder != null && appId !in folder.appIds) {
                folderRepository.addAppToFolder(folderId, appId)
                _uiState.value = _uiState.value.copy(
                    lastAction = "Added app to ${folder.name}",
                    showSnackbar = true
                )
            }
            stopDragApp()
        }
    }

    /**
     * Drop app on app (create folder)
     */
    fun dropAppOnApp(sourceAppId: String, targetAppId: String) {
        viewModelScope.launch {
            val sourceApp = allApps.value.find { it.id == sourceAppId }
            val targetApp = allApps.value.find { it.id == targetAppId }

            if (sourceApp != null && targetApp != null) {
                // Create new folder with both apps
                val folderName = suggestFolderName(listOf(sourceApp, targetApp))
                val folderId = createFolderAndGetId(folderName)

                // Add both apps to the new folder
                folderRepository.addAppToFolder(folderId, sourceAppId)
                folderRepository.addAppToFolder(folderId, targetAppId)

                _uiState.value = _uiState.value.copy(
                    lastAction = "Created folder '$folderName'",
                    showSnackbar = true
                )
            }
            stopDragApp()
        }
    }

    /**
     * Drop folder on folder (move subfolder)
     */
    fun dropFolderOnFolder(sourceFolderId: String, targetFolderId: String) {
        viewModelScope.launch {
            val sourceFolder = folderRepository.getFolderById(sourceFolderId)
            val targetFolder = folderRepository.getFolderById(targetFolderId)

            if (sourceFolder != null && targetFolder != null && sourceFolderId != targetFolderId) {
                // Remove from old parent if exists
                sourceFolder.parentId?.let { oldParentId ->
                    folderRepository.removeSubFolder(oldParentId, sourceFolderId)
                }

                // Add to new parent
                folderRepository.addSubFolder(targetFolderId, sourceFolderId)

                // Update folder's parent ID
                val updatedFolder = sourceFolder.copy(parentId = targetFolderId)
                folderRepository.updateFolder(updatedFolder)

                _uiState.value = _uiState.value.copy(
                    lastAction = "Moved ${sourceFolder.name} into ${targetFolder.name}",
                    showSnackbar = true
                )
            }
            stopDragFolder()
        }
    }

    // ========== SMART SUGGESTIONS ==========

    /**
     * Get folder suggestions for an app
     */
    fun suggestFoldersForApp(app: UnifiedAppInfo) {
        viewModelScope.launch {
            val suggestions = mutableListOf<FolderSuggestion>()
            val folders = allFolders.value

            // Suggest based on category
            app.category?.let { category ->
                val matchingFolder = folders.find { it.name.equals(category, ignoreCase = true) }
                if (matchingFolder != null && app.id !in matchingFolder.appIds) {
                    suggestions.add(
                        FolderSuggestion(
                            folder = matchingFolder,
                            reason = "Matches category: $category",
                            shouldCreate = false
                        )
                    )
                }
            }

            // Suggest based on usage patterns
            if (app.launchCount > 10) {
                val frequentFolder = folders.find { it.name.equals("Frequent", ignoreCase = true) }
                if (frequentFolder != null && app.id !in frequentFolder.appIds) {
                    suggestions.add(
                        FolderSuggestion(
                            folder = frequentFolder,
                            reason = "Frequently used app",
                            shouldCreate = false
                        )
                    )
                }
            }

            // Suggest based on existing folder memberships
            if (app.folderIds.isNotEmpty()) {
                app.folderIds.forEach { folderId ->
                    folders.find { it.id == folderId }?.let { folder ->
                        suggestions.add(
                            FolderSuggestion(
                                folder = folder,
                                reason = "Related to existing folders",
                                shouldCreate = false
                            )
                        )
                    }
                }
            }

            // Suggest creating new folder if no match
            if (suggestions.isEmpty()) {
                suggestions.add(
                    FolderSuggestion(
                        folder = null,
                        reason = "Create new folder",
                        shouldCreate = true,
                        suggestedName = suggestFolderName(listOf(app))
                    )
                )
            }

            _folderSuggestions.value = suggestions
        }
    }

    /**
     * Clear folder suggestions
     */
    fun clearSuggestions() {
        _folderSuggestions.value = emptyList()
    }

    // ========== QUICK ACTIONS ==========

    /**
     * Rename folder
     */
    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            val folder = folderRepository.getFolderById(folderId)
            if (folder != null) {
                folderRepository.updateFolder(folder.copy(name = newName))
                _uiState.value = _uiState.value.copy(
                    lastAction = "Renamed to '$newName'",
                    showSnackbar = true
                )
            }
        }
    }

    /**
     * Change folder style
     */
    fun changeFolderStyle(folderId: String, style: FolderStyle) {
        viewModelScope.launch {
            val folder = folderRepository.getFolderById(folderId)
            if (folder != null) {
                folderRepository.updateFolder(folder.copy(folderStyle = style.name))
            }
        }
    }

    /**
     * Change folder color
     */
    fun changeFolderColor(folderId: String, iconColor: String, backgroundColor: String) {
        viewModelScope.launch {
            val folder = folderRepository.getFolderById(folderId)
            if (folder != null) {
                folderRepository.updateFolder(
                    folder.copy(
                        iconColor = iconColor,
                        backgroundColor = backgroundColor
                    )
                )
            }
        }
    }

    /**
     * Auto-sort apps in folder
     */
    fun autoSortFolder(folderId: String, sortBy: FolderSortOption) {
        viewModelScope.launch {
            val folder = folderRepository.getFolderById(folderId)
            if (folder != null) {
                val appsInFolder = folder.appIds.mapNotNull { appId ->
                    allApps.value.find { it.id == appId }
                }

                val sortedAppIds = when (sortBy) {
                    FolderSortOption.NAME -> appsInFolder.sortedBy { it.name }.map { it.id }
                    FolderSortOption.USAGE -> appsInFolder.sortedByDescending { it.launchCount }.map { it.id }
                    FolderSortOption.CATEGORY -> appsInFolder.sortedBy { it.category }.map { it.id }
                    FolderSortOption.INSTALL_DATE -> appsInFolder.sortedByDescending { it.installTime }.map { it.id }
                }

                // Update folder with sorted app IDs (would need to add sortOrder to FolderEntity)
                // For now, just update the app IDs in sorted order
                folderRepository.updateFolder(folder.copy(appIds = sortedAppIds))
            }
        }
    }

    /**
     * Set folder as favorite
     */
    fun setFolderAsFavorite(folderId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val folder = folderRepository.getFolderById(folderId)
            if (folder != null) {
                // Would need to add isFavorite to FolderEntity
                // For now, track in UI state
                val currentFavorites = _uiState.value.favoriteFolderIds.toMutableSet()
                if (isFavorite) {
                    currentFavorites.add(folderId)
                } else {
                    currentFavorites.remove(folderId)
                }
                _uiState.value = _uiState.value.copy(favoriteFolderIds = currentFavorites)
            }
        }
    }

    // ========== SELECTION ==========

    /**
     * Select a folder
     */
    fun selectFolder(folder: FolderEntity?) {
        _selectedFolder.value = folder
        _uiState.value = _uiState.value.copy(showFolderDetails = folder != null)
    }

    /**
     * Deselect folder
     */
    fun deselectFolder() {
        _selectedFolder.value = null
        _uiState.value = _uiState.value.copy(showFolderDetails = false)
    }

    /**
     * Toggle folder details visibility
     */
    fun toggleFolderDetails() {
        _uiState.value = _uiState.value.copy(
            showFolderDetails = !_uiState.value.showFolderDetails
        )
    }

    /**
     * Dismiss snackbar
     */
    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(showSnackbar = false)
    }

    // ========== UTILITY ==========

    /**
     * Create folder and return its ID
     */
    private suspend fun createFolderAndGetId(name: String): String {
        val id = "folder_${System.currentTimeMillis()}_${(0..10000).random()}"
        val folder = FolderEntity(
            id = id,
            name = name,
            parentId = null,
            folderStyle = FolderStyle.DEFAULT.name,
            isSystemFolder = false,
            appIds = emptyList(),
            subFolderIds = emptyList()
        )
        folderRepository.createFolder(
            name = name,
            style = FolderStyle.DEFAULT
        )
        return id
    }

    /**
     * Suggest folder name based on apps
     */
    private fun suggestFolderName(apps: List<UnifiedAppInfo>): String {
        if (apps.isEmpty()) return "New Folder"
        if (apps.size == 1) {
            // Use category or first letter
            return apps.first().category ?: "Apps"
        }

        // Check if all apps share a category
        val categories = apps.mapNotNull { it.category }.distinct()
        if (categories.size == 1) {
            return categories.first()
        }

        // Default name
        return "Folder"
    }

    /**
     * Get apps in a folder
     */
    suspend fun getAppsInFolder(folderId: String): List<UnifiedAppInfo> {
        return allAppsManager.getAppsInFolder(folderId)
    }
}

/**
 * Folder sort options
 */
enum class FolderSortOption {
    NAME,
    USAGE,
    CATEGORY,
    INSTALL_DATE
}

/**
 * Folder suggestion data class
 */
data class FolderSuggestion(
    val folder: FolderEntity?,
    val reason: String,
    val shouldCreate: Boolean,
    val suggestedName: String? = null
)

/**
 * UI state for Folder Management
 */
data class FolderUiState(
    val isDraggingApp: Boolean = false,
    val isDraggingFolder: Boolean = false,
    val showFolderDetails: Boolean = false,
    val showSnackbar: Boolean = false,
    val lastAction: String? = null,
    val favoriteFolderIds: Set<String> = emptySet(),
    val showCreateFolderDialog: Boolean = false,
    val showStyleSelector: Boolean = false,
    val showColorPicker: Boolean = false,
    val showSortOptions: Boolean = false
)
