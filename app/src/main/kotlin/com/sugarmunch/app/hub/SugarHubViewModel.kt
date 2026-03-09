package com.sugarmunch.app.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.local.FolderEntity
import com.sugarmunch.app.data.repository.FolderRepository
import com.sugarmunch.app.data.ManifestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SugarHubUiState(
    val isLoading: Boolean = true,
    val allApps: List<AppEntry> = emptyList(),
    val searchResults: List<AppEntry> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SugarHubViewModel @Inject constructor(
    private val manifestRepository: ManifestRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SugarHubUiState())
    val uiState: StateFlow<SugarHubUiState> = _uiState.asStateFlow()

    private val _folders = MutableStateFlow<List<FolderEntity>>(emptyList())
    val folders: StateFlow<List<FolderEntity>> = _folders.asStateFlow()

    private val _recentApps = MutableStateFlow<List<AppEntry>>(emptyList())
    val recentApps: StateFlow<List<AppEntry>> = _recentApps.asStateFlow()

    private val _favoriteApps = MutableStateFlow<List<AppEntry>>(emptyList())
    val favoriteApps: StateFlow<List<AppEntry>> = _favoriteApps.asStateFlow()

    private val _suggestedApps = MutableStateFlow<List<AppEntry>>(emptyList())
    val suggestedApps: StateFlow<List<AppEntry>> = _suggestedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchExpanded = MutableStateFlow(false)
    val isSearchExpanded: StateFlow<Boolean> = _isSearchExpanded.asStateFlow()

    init {
        loadHubData()
    }

    fun loadHubData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load apps from manifest
            manifestRepository.apps
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { apps ->
                    _uiState.update {
                        it.copy(
                            allApps = apps,
                            isLoading = false
                        )
                    }

                    // Generate smart suggestions
                    generateSuggestions(apps)

                    // Set recent and favorites (would be from usage tracking in real app)
                    _recentApps.value = apps.sortedByDescending { it.name }.take(10)
                    _favoriteApps.value = apps.filter { it.featured == true }.take(10)
                }

            // Load folders
            folderRepository.getAllFolders()
                .catch { e ->
                    // Handle error silently for folders
                }
                .collect { folderList ->
                    _folders.value = folderList
                }
        }
    }

    private fun generateSuggestions(apps: List<AppEntry>) {
        // ML-based suggestions would go here
        // For now, suggest featured apps and random selection
        val suggested = apps
            .filter { it.featured == true }
            .shuffled()
            .take(5)
        _suggestedApps.value = suggested
    }

    fun search(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        val results = _uiState.value.allApps.filter { app ->
            app.name.contains(query, ignoreCase = true) ||
            app.description.contains(query, ignoreCase = true) ||
            app.packageName.contains(query, ignoreCase = true) ||
            app.category?.contains(query, ignoreCase = true) == true
        }

        _uiState.update { it.copy(searchResults = results) }
    }

    fun toggleSearch() {
        _isSearchExpanded.value = !_isSearchExpanded.value
        if (!_isSearchExpanded.value) {
            _searchQuery.value = ""
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun createFolder(name: String, appIds: List<String> = emptyList()) {
        viewModelScope.launch {
            val folder = FolderEntity(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                appIds = appIds,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            folderRepository.insertFolder(folder)
        }
    }

    fun addAppToFolder(folderId: String, appId: String) {
        viewModelScope.launch {
            val folder = _folders.value.find { it.id == folderId } ?: return@launch
            val updatedAppIds = (folder.appIds + appId).distinct()
            folderRepository.updateFolder(folder.copy(
                appIds = updatedAppIds,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    fun removeAppFromFolder(folderId: String, appId: String) {
        viewModelScope.launch {
            val folder = _folders.value.find { it.id == folderId } ?: return@launch
            val updatedAppIds = folder.appIds.filter { it != appId }
            folderRepository.updateFolder(folder.copy(
                appIds = updatedAppIds,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            folderRepository.deleteFolder(folderId)
        }
    }

    fun autoCategorizeApps() {
        viewModelScope.launch {
            val apps = _uiState.value.allApps
            val categories = apps.groupBy { it.category ?: "Other" }

            categories.forEach { (category, categoryApps) ->
                val existingFolder = _folders.value.find { it.name == category }
                if (existingFolder == null && categoryApps.isNotEmpty()) {
                    createFolder(
                        name = category,
                        appIds = categoryApps.map { it.id }
                    )
                }
            }
        }
    }
}
