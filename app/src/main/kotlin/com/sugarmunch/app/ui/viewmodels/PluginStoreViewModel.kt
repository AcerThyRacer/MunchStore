package com.sugarmunch.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.hub.AllAppsManager
import com.sugarmunch.app.plugin.model.PluginCategory
import com.sugarmunch.app.plugin.model.StorePlugin
import com.sugarmunch.app.plugin.store.InstallStatus
import com.sugarmunch.app.plugin.store.PluginStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for the Plugin Store screen.
 * Manages plugin browsing, searching, installation, and folder integration.
 */
@HiltViewModel
class PluginStoreViewModel @Inject constructor(
    private val pluginStore: PluginStore,
    private val allAppsManager: AllAppsManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ========== STATE FLOWS ==========

    /** Available plugins from the store */
    val availablePlugins: StateFlow<List<StorePlugin>> = pluginStore.availablePlugins
        .map { it.values.toList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Featured plugins */
    val featuredPlugins: StateFlow<List<StorePlugin>> = pluginStore.featuredPlugins
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Top rated plugins */
    val topRatedPlugins: StateFlow<List<StorePlugin>> = pluginStore.topRatedPlugins
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** New plugins */
    val newPlugins: StateFlow<List<StorePlugin>> = pluginStore.newPlugins
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Installed plugins (package names) */
    val installedPlugins: StateFlow<List<String>> = pluginStore.installedPlugins
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Download progress map (pluginId -> progress 0.0-1.0) */
    val downloadProgress: StateFlow<Map<String, Float>> = pluginStore.downloadProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    /** Install status map (pluginId -> status) */
    val installStatus: StateFlow<Map<String, InstallStatus>> = pluginStore.installStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    /** Plugin categories */
    val categories: StateFlow<List<PluginCategory>> = pluginStore.categories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PluginCategory.values().toList()
        )

    // ========== SEARCH & FILTER STATE ==========

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<PluginCategory?>(null)
    val selectedCategory: StateFlow<PluginCategory?> = _selectedCategory.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.POPULAR)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    // ========== SEARCH RESULTS ==========

    /** Filtered and sorted plugins based on search query and category */
    val filteredPlugins: StateFlow<List<StorePlugin>> = combine(
        availablePlugins,
        searchQuery,
        selectedCategory,
        sortBy
    ) { plugins, query, category, sort ->
        plugins.filter { plugin ->
            // Search query filter
            val matchesQuery = query.isBlank() ||
                plugin.name.contains(query, ignoreCase = true) ||
                plugin.description.contains(query, ignoreCase = true) ||
                plugin.author.contains(query, ignoreCase = true) ||
                plugin.tags.any { it.contains(query, ignoreCase = true) }

            // Category filter
            val matchesCategory = category == null || plugin.category == category

            matchesQuery && matchesCategory
        }.let { filtered ->
            // Sort
            when (sort) {
                SortBy.POPULAR -> filtered.sortedByDescending { it.downloadCount }
                SortBy.RATING -> filtered.sortedByDescending { it.rating }
                SortBy.NEWEST -> filtered.sortedByDescending { it.publishDate }
                SortBy.NAME -> filtered.sortedBy { it.name.lowercase() }
                SortBy.SIZE -> filtered.sortedBy { it.size }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ========== UI STATE ==========

    private val _uiState = MutableStateFlow(PluginStoreUiState())
    val uiState: StateFlow<PluginStoreUiState> = _uiState.asStateFlow()

    private val _selectedPlugin = MutableStateFlow<StorePlugin?>(null)
    val selectedPlugin: StateFlow<StorePlugin?> = _selectedPlugin.asStateFlow()

    private val _userRatings = MutableStateFlow<Map<String, Int>>(emptyMap())
    val userRatings: StateFlow<Map<String, Int>> = _userRatings.asStateFlow()

    // ========== ACTIONS ==========

    /**
     * Search plugins by query
     */
    fun searchPlugins(query: String) {
        _searchQuery.value = query
    }

    /**
     * Filter by category
     */
    fun filterByCategory(category: PluginCategory?) {
        _selectedCategory.value = category
    }

    /**
     * Sort plugins
     */
    fun sortBy(sortBy: SortBy) {
        _sortBy.value = sortBy
    }

    /**
     * Install a plugin
     */
    fun installPlugin(pluginId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isInstalling = true)
            val result = pluginStore.installPlugin(pluginId)
            _uiState.value = _uiState.value.copy(
                isInstalling = false,
                installResult = result
            )
            // Refresh app list after installation
            allAppsManager.refresh()
        }
    }

    /**
     * Uninstall a plugin
     */
    fun uninstallPlugin(pluginId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUninstalling = true)
            val result = pluginStore.uninstallPlugin(pluginId)
            _uiState.value = _uiState.value.copy(
                isUninstalling = false,
                uninstallResult = result
            )
            // Refresh app list after uninstallation
            allAppsManager.refresh()
        }
    }

    /**
     * Update a plugin
     */
    fun updatePlugin(pluginId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            val result = pluginStore.updatePlugin(pluginId)
            _uiState.value = _uiState.value.copy(
                isUpdating = false,
                updateResult = result
            )
            // Refresh app list after update
            allAppsManager.refresh()
        }
    }

    /**
     * Rate a plugin
     */
    fun ratePlugin(pluginId: String, rating: Int) {
        viewModelScope.launch {
            val result = pluginStore.ratePlugin(pluginId, rating)
            if (result.isSuccess) {
                _userRatings.value = _userRatings.value + (pluginId to rating)
            }
        }
    }

    /**
     * Get user's rating for a plugin
     */
    fun getUserRating(pluginId: String): Int? {
        return _userRatings.value[pluginId] ?: pluginStore.getUserRating(pluginId)
    }

    /**
     * Select a plugin for viewing details
     */
    fun selectPlugin(plugin: StorePlugin?) {
        _selectedPlugin.value = plugin
        _uiState.value = _uiState.value.copy(
            showPluginDetails = plugin != null
        )
    }

    /**
     * Dismiss plugin details
     */
    fun dismissPluginDetails() {
        _selectedPlugin.value = null
        _uiState.value = _uiState.value.copy(showPluginDetails = false)
    }

    /**
     * Clear install result
     */
    fun clearInstallResult() {
        _uiState.value = _uiState.value.copy(installResult = null)
    }

    /**
     * Clear uninstall result
     */
    fun clearUninstallResult() {
        _uiState.value = _uiState.value.copy(uninstallResult = null)
    }

    /**
     * Clear update result
     */
    fun clearUpdateResult() {
        _uiState.value = _uiState.value.copy(updateResult = null)
    }

    /**
     * Refresh plugin list
     */
    fun refreshPlugins() {
        pluginStore.refreshPlugins()
    }

    /**
     * Check for plugin updates
     */
    fun checkForUpdates() {
        viewModelScope.launch {
            val updates = pluginStore.checkForUpdates()
            _uiState.value = _uiState.value.copy(
                availableUpdates = updates.values.toList()
            )
        }
    }

    /**
     * Get plugin details by ID
     */
    fun getPluginDetails(pluginId: String): StorePlugin? {
        return pluginStore.getPluginDetails(pluginId)
    }

    /**
     * Get related plugins
     */
    fun getRelatedPlugins(pluginId: String, limit: Int = 5): List<StorePlugin> {
        return pluginStore.getRelatedPlugins(pluginId, limit)
    }

    /**
     * Get plugins by developer
     */
    fun getPluginsByDeveloper(developerId: String): List<StorePlugin> {
        return pluginStore.getPluginsByDeveloper(developerId)
    }

    /**
     * Check if a plugin is installed
     */
    fun isPluginInstalled(pluginId: String): Boolean {
        return pluginStore.isPluginInstalled(pluginId)
    }

    /**
     * Get install status for a plugin
     */
    fun getInstallStatus(pluginId: String): InstallStatus {
        return pluginStore.getInstallStatus(pluginId)
    }

    /**
     * Get download progress for a plugin
     */
    fun getDownloadProgress(pluginId: String): Float {
        return pluginStore.getDownloadProgress(pluginId)
    }

    /**
     * Add plugin to folder (delegated to AllAppsManager/FolderRepository)
     */
    fun addToFolder(pluginId: String, folderId: String) {
        viewModelScope.launch {
            // This would integrate with FolderRepository
            // For now, we track it in the UI state
            val currentMappings = _uiState.value.folderMappings.toMutableMap()
            val currentPluginFolders = currentMappings[pluginId]?.toMutableList() ?: mutableListOf()
            if (folderId !in currentPluginFolders) {
                currentPluginFolders.add(folderId)
                currentMappings[pluginId] = currentPluginFolders
                _uiState.value = _uiState.value.copy(folderMappings = currentMappings)
            }
        }
    }

    /**
     * Remove plugin from folder
     */
    fun removeFromFolder(pluginId: String, folderId: String) {
        viewModelScope.launch {
            val currentMappings = _uiState.value.folderMappings.toMutableMap()
            val currentPluginFolders = currentMappings[pluginId]?.toMutableList() ?: mutableListOf()
            currentPluginFolders.remove(folderId)
            if (currentPluginFolders.isEmpty()) {
                currentMappings.remove(pluginId)
            } else {
                currentMappings[pluginId] = currentPluginFolders
            }
            _uiState.value = _uiState.value.copy(folderMappings = currentMappings)
        }
    }

    /**
     * Get folders for a plugin
     */
    fun getFoldersForPlugin(pluginId: String): List<String> {
        return _uiState.value.folderMappings[pluginId] ?: emptyList()
    }
}

/**
 * Sort options for plugins
 */
enum class SortBy {
    POPULAR,
    RATING,
    NEWEST,
    NAME,
    SIZE
}

/**
 * UI state for the Plugin Store screen
 */
data class PluginStoreUiState(
    val isInstalling: Boolean = false,
    val isUninstalling: Boolean = false,
    val isUpdating: Boolean = false,
    val isLoading: Boolean = false,
    val showPluginDetails: Boolean = false,
    val installResult: Result<File>? = null,
    val uninstallResult: Result<Unit>? = null,
    val updateResult: Result<File>? = null,
    val errorMessage: String? = null,
    val availableUpdates: List<StorePlugin> = emptyList(),
    val folderMappings: Map<String, List<String>> = emptyMap() // pluginId -> folderIds
)
