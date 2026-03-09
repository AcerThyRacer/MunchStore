package com.sugarmunch.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.CandyTrailEntry
import com.sugarmunch.app.data.ManifestRepository
import com.sugarmunch.app.data.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CatalogUiState {
    data object Loading : CatalogUiState()
    data class Success(
        val apps: List<AppEntry>,
        val filteredApps: List<AppEntry>,
        val featuredApps: List<AppEntry> = emptyList()
    ) : CatalogUiState()
    data class Error(val message: String) : CatalogUiState()
}

/** Sort mode for catalog. */
enum class CatalogSortMode(val key: String) {
    DEFAULT("default"),
    NAME_ASC("name_asc"),
    NAME_DESC("name_desc"),
    CATEGORY("category");

    companion object {
        fun fromKey(key: String?) = entries.find { it.key == key } ?: DEFAULT
    }
}

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repository: ManifestRepository,
    private val prefs: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(CatalogSortMode.DEFAULT)
    val sortMode: StateFlow<CatalogSortMode> = _sortMode.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()

    private val _filterFeaturedOnly = MutableStateFlow(false)
    val filterFeaturedOnly: StateFlow<Boolean> = _filterFeaturedOnly.asStateFlow()

    private val _trails = MutableStateFlow<List<CandyTrailEntry>>(emptyList())
    val trails: StateFlow<List<CandyTrailEntry>> = _trails.asStateFlow()

    private var allApps: List<AppEntry> = emptyList()

    init {
        repository.trails.onEach { _trails.value = it }.launchIn(viewModelScope)
        prefs.catalogSort.onEach {
            _sortMode.value = CatalogSortMode.fromKey(it)
            if (_uiState.value is CatalogUiState.Success) applySortAndFilter()
        }.launchIn(viewModelScope)
        prefs.catalogFilterCategory.onEach {
            _filterCategory.value = it
            if (_uiState.value is CatalogUiState.Success) applySortAndFilter()
        }.launchIn(viewModelScope)
        prefs.catalogFilterFeatured.onEach {
            _filterFeaturedOnly.value = it
            if (_uiState.value is CatalogUiState.Success) applySortAndFilter()
        }.launchIn(viewModelScope)
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            repository.fetchApps()
                .onSuccess { apps ->
                    allApps = apps
                    val featured = apps.filter { it.featured == true }
                    applySortAndFilter()
                }
                .onFailure { e -> _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load") }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        applySortAndFilter()
    }

    fun setSortMode(mode: CatalogSortMode) {
        _sortMode.value = mode
        viewModelScope.launch { prefs.setCatalogSort(mode.key) }
        applySortAndFilter()
    }

    fun setFilterCategory(category: String?) {
        _filterCategory.value = category
        viewModelScope.launch { prefs.setCatalogFilterCategory(category) }
        applySortAndFilter()
    }

    fun setFilterFeaturedOnly(featuredOnly: Boolean) {
        _filterFeaturedOnly.value = featuredOnly
        viewModelScope.launch { prefs.setCatalogFilterFeatured(featuredOnly) }
        applySortAndFilter()
    }

    private fun applySortAndFilter() {
        val currentState = _uiState.value
        if (currentState !is CatalogUiState.Success && currentState !is CatalogUiState.Loading) return
        val query = _searchQuery.value.lowercase().trim()
        var list = allApps
        if (query.isNotEmpty()) {
            list = list.filter { app ->
                app.name.lowercase().contains(query) ||
                app.description.lowercase().contains(query) ||
                app.id.lowercase().contains(query)
            }
        }
        if (_filterFeaturedOnly.value) {
            list = list.filter { it.featured == true }
        }
        _filterCategory.value?.let { cat ->
            list = list.filter { it.category == cat }
        }
        list = when (_sortMode.value) {
            CatalogSortMode.DEFAULT -> list.sortedWith(
                compareBy<AppEntry> { it.category ?: "" }.thenBy { it.sortOrder ?: Int.MAX_VALUE }.thenBy { it.name }
            )
            CatalogSortMode.NAME_ASC -> list.sortedBy { it.name }
            CatalogSortMode.NAME_DESC -> list.sortedByDescending { it.name }
            CatalogSortMode.CATEGORY -> list.sortedWith(
                compareBy<AppEntry> { it.category ?: "" }.thenBy { it.name }
            )
        }
        val featured = allApps.filter { it.featured == true }
        _uiState.value = CatalogUiState.Success(allApps, list, featured)
    }

    /** Apps that belong to a trail (by appIds). */
    fun getAppsForTrail(appIds: List<String>): List<AppEntry> =
        allApps.filter { it.id in appIds }
}