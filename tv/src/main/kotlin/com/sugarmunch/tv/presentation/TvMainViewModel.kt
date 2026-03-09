package com.sugarmunch.tv.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.effects.v2.model.EffectV2
import com.sugarmunch.app.effects.v2.presets.EffectRegistry
import com.sugarmunch.app.repository.SmartManifestRepository
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.tv.data.TvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main ViewModel for TV interface
 * Manages UI state and coordinates between TV UI and shared repositories
 */
@HiltViewModel
class TvMainViewModel @Inject constructor(
    application: Application,
    private val repository: SmartManifestRepository
) : AndroidViewModel(application) {

    private val tvRepository = TvRepository(application, repository)
    private val themeManager = ThemeManager.getInstance(application)

    // UI State
    private val _uiState = MutableStateFlow(TvUiState())
    val uiState: StateFlow<TvUiState> = _uiState.asStateFlow()

    // Effects
    private val _effectsState = MutableStateFlow(EffectsUiState())
    val effectsState: StateFlow<EffectsUiState> = _effectsState.asStateFlow()

    // Themes
    private val _themesState = MutableStateFlow(ThemesUiState())
    val themesState: StateFlow<ThemesUiState> = _themesState.asStateFlow()

    // Search
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    init {
        loadInitialData()
        loadEffects()
        loadThemes()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load featured apps
                val featured = tvRepository.getFeaturedAppsForTv()
                _uiState.update { 
                    it.copy(featuredApps = featured, isLoading = false) 
                }

                // Load apps by category
                val categories = tvRepository.getAppsByCategoryForTv()
                _uiState.update { 
                    it.copy(categoryApps = categories) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message, isLoading = false) 
                }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                tvRepository.refreshForTv()
                loadInitialData()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message, isLoading = false) 
                }
            }
        }
    }

    private fun loadEffects() {
        viewModelScope.launch {
            val effects = EffectRegistry.getAllEffects()
            _effectsState.update { 
                it.copy(
                    availableEffects = effects,
                    activeEffects = EffectRegistry.getActiveEffects()
                ) 
            }
        }
    }

    fun toggleEffect(effectId: String) {
        viewModelScope.launch {
            EffectRegistry.toggleEffect(effectId)
            _effectsState.update {
                it.copy(activeEffects = EffectRegistry.getActiveEffects())
            }
        }
    }

    fun applyEffectPreset(presetName: String) {
        viewModelScope.launch {
            when (presetName) {
                "none" -> EffectRegistry.clearAllEffects()
                "focus" -> {
                    EffectRegistry.clearAllEffects()
                    EffectRegistry.toggleEffect("minimal_glow")
                }
                "celebration" -> {
                    EffectRegistry.clearAllEffects()
                    EffectRegistry.toggleEffect("confetti_burst")
                    EffectRegistry.toggleEffect("sparkle_trail")
                }
                "chill" -> {
                    EffectRegistry.clearAllEffects()
                    EffectRegistry.toggleEffect("gentle_pulse")
                    EffectRegistry.toggleEffect("soft_glow")
                }
                "maximum" -> {
                    EffectRegistry.getAllEffects().forEach { effect ->
                        if (!EffectRegistry.isEffectActive(effect.id)) {
                            EffectRegistry.toggleEffect(effect.id)
                        }
                    }
                }
            }
            _effectsState.update {
                it.copy(activeEffects = EffectRegistry.getActiveEffects())
            }
        }
    }

    private fun loadThemes() {
        viewModelScope.launch {
            val currentTheme = themeManager.currentTheme.value
            val allThemes = themeManager.getAvailableThemes()
            _themesState.update {
                it.copy(
                    currentTheme = currentTheme,
                    availableThemes = allThemes,
                    categories = allThemes.map { theme -> theme.category }.distinct()
                )
            }
        }
    }

    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            themeManager.setThemeById(themeId)
            _themesState.update {
                it.copy(currentTheme = themeManager.currentTheme.value)
            }
        }
    }

    fun setThemeCategoryFilter(category: com.sugarmunch.app.theme.model.ThemeCategory?) {
        _themesState.update { it.copy(selectedCategory = category) }
    }

    fun search(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchState.update {
                    it.copy(query = query, results = emptyList(), suggestions = emptyList())
                }
                return@launch
            }

            _searchState.update { it.copy(query = query, isSearching = true) }

            try {
                val results = tvRepository.searchAppsForTv(query)
                val suggestions = generateSearchSuggestions(query)
                
                _searchState.update {
                    it.copy(
                        query = query,
                        results = results,
                        suggestions = suggestions,
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                _searchState.update {
                    it.copy(
                        query = query,
                        isSearching = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun generateSearchSuggestions(query: String): List<String> {
        // Generate search suggestions based on popular searches and app names
        val commonSuggestions = listOf(
            "games", "tools", "media", "productivity",
            "browser", "player", "launcher", "file manager"
        )
        return commonSuggestions.filter { it.contains(query, ignoreCase = true) }
    }

    fun clearSearch() {
        _searchState.update {
            it.copy(query = "", results = emptyList(), suggestions = emptyList())
        }
    }

    fun getAppById(appId: String): AppEntry? {
        return (_uiState.value.featuredApps + _uiState.value.categoryApps.values.flatten())
            .find { it.id == appId }
    }

    fun getRelatedApps(appId: String, category: String?): List<AppEntry> {
        val allApps = _uiState.value.categoryApps.values.flatten()
        return allApps
            .filter { it.id != appId && it.category == category }
            .take(6)
    }
}

/**
 * UI State for main catalog screen
 */
data class TvUiState(
    val isLoading: Boolean = false,
    val featuredApps: List<AppEntry> = emptyList(),
    val categoryApps: Map<String, List<AppEntry>> = emptyMap(),
    val error: String? = null
)

/**
 * UI State for effects screen
 */
data class EffectsUiState(
    val availableEffects: List<EffectV2> = emptyList(),
    val activeEffects: List<String> = emptyList(),
    val previewIntensity: Float = 1.0f
)

/**
 * UI State for themes screen
 */
data class ThemesUiState(
    val currentTheme: CandyTheme? = null,
    val availableThemes: List<CandyTheme> = emptyList(),
    val categories: List<com.sugarmunch.app.theme.model.ThemeCategory> = emptyList(),
    val selectedCategory: com.sugarmunch.app.theme.model.ThemeCategory? = null
)

/**
 * UI State for search screen
 */
data class SearchUiState(
    val query: String = "",
    val results: List<AppEntry> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)
