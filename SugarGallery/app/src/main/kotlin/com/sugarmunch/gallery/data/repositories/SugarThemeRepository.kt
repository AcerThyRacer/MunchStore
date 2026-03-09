package com.sugarmunch.gallery.data.repositories

import android.content.Context
import com.sugarmunch.gallery.SugarTheme
import com.sugarmunch.gallery.ThemeCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sugar Theme Repository
 * Manages theme selection, custom themes, and theme persistence
 */
class SugarThemeRepository(private val context: Context) {

    private val _selectedTheme = MutableStateFlow<SugarTheme?>(null)
    val selectedTheme: Flow<SugarTheme?> = _selectedTheme.asStateFlow()

    private val _favoriteThemes = MutableStateFlow<Set<String>>(emptySet())
    val favoriteThemes: Flow<Set<String>> = _favoriteThemes.asStateFlow()

    private val _customThemes = MutableStateFlow<List<SugarTheme>>(emptyList())
    val customThemes: Flow<List<SugarTheme>> = _customThemes.asStateFlow()

    init {
        loadSelectedTheme()
        loadFavoriteThemes()
        loadCustomThemes()
    }

    /**
     * Get all available themes including custom themes
     */
    fun getAllThemes(): List<SugarTheme> {
        return SugarTheme.ALL_THEMES + _customThemes.value
    }

    /**
     * Get themes by category
     */
    fun getThemesByCategory(category: ThemeCategory): List<SugarTheme> {
        return getAllThemes().filter { it.category == category }
    }

    /**
     * Get theme by ID
     */
    fun getThemeById(id: String): SugarTheme? {
        return getAllThemes().find { it.id == id }
    }

    /**
     * Set the currently selected theme
     */
    fun setSelectedTheme(themeId: String) {
        val theme = getThemeById(themeId)
        _selectedTheme.value = theme
        saveSelectedTheme(themeId)
    }

    /**
     * Toggle favorite status for a theme
     */
    fun toggleFavoriteTheme(themeId: String) {
        val current = _favoriteThemes.value.toMutableSet()
        if (themeId in current) {
            current.remove(themeId)
        } else {
            current.add(themeId)
        }
        _favoriteThemes.value = current
        saveFavoriteThemes(current)
    }

    /**
     * Check if a theme is favorited
     */
    fun isThemeFavorited(themeId: String): Boolean {
        return themeId in _favoriteThemes.value
    }

    /**
     * Add a custom theme
     */
    fun addCustomTheme(theme: SugarTheme) {
        val current = _customThemes.value.toMutableList()
        current.add(theme)
        _customThemes.value = current
        saveCustomThemes(current)
    }

    /**
     * Remove a custom theme
     */
    fun removeCustomTheme(themeId: String) {
        val current = _customThemes.value.filter { it.id != themeId }
        _customThemes.value = current
        saveCustomThemes(current)
    }

    /**
     * Update a custom theme
     */
    fun updateCustomTheme(theme: SugarTheme) {
        val current = _customThemes.value.toMutableList()
        val index = current.indexOfFirst { it.id == theme.id }
        if (index >= 0) {
            current[index] = theme
            _customThemes.value = current
            saveCustomThemes(current)
        }
    }

    /**
     * Get popular themes (most used)
     */
    fun getPopularThemes(limit: Int = 5): List<SugarTheme> {
        // In a real implementation, this would track usage statistics
        return SugarTheme.ALL_THEMES.take(limit)
    }

    /**
     * Get recently used themes
     */
    fun getRecentlyUsedThemes(limit: Int = 5): List<SugarTheme> {
        // In a real implementation, this would track recent selections
        return SugarTheme.ALL_THEMES.take(limit)
    }

    /**
     * Search themes by name or description
     */
    fun searchThemes(query: String): List<SugarTheme> {
        if (query.isBlank()) return getAllThemes()
        
        val lowerQuery = query.lowercase()
        return getAllThemes().filter { theme ->
            theme.name.lowercase().contains(lowerQuery) ||
            theme.description.lowercase().contains(lowerQuery) ||
            theme.category.name.lowercase().contains(lowerQuery)
        }
    }

    /**
     * Get random theme suggestion
     */
    fun getRandomThemeSuggestion(): SugarTheme {
        return getAllThemes().random()
    }

    // ═════════════════════════════════════════════════════════════════
    // PERSISTENCE METHODS
    // ═════════════════════════════════════════════════════════════════

    private fun loadSelectedTheme() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeId = prefs.getString(KEY_SELECTED_THEME, "cotton_candy") ?: "cotton_candy"
        _selectedTheme.value = getThemeById(themeId)
    }

    private fun saveSelectedTheme(themeId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_THEME, themeId).apply()
    }

    private fun loadFavoriteThemes() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet(KEY_FAVORITE_THEMES, emptySet()) ?: emptySet()
        _favoriteThemes.value = favorites
    }

    private fun saveFavoriteThemes(favorites: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_FAVORITE_THEMES, favorites).apply()
    }

    private fun loadCustomThemes() {
        // In a real implementation, this would load from database or file
        _customThemes.value = emptyList()
    }

    private fun saveCustomThemes(themes: List<SugarTheme>) {
        // In a real implementation, this would save to database or file
    }

    companion object {
        private const val PREFS_NAME = "sugargallery_themes"
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_FAVORITE_THEMES = "favorite_themes"
    }
}
