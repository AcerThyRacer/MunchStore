package com.sugarmunch.app.theme.builder

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.util.CoroutineUtils.safeCollect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "custom_themes")

/**
 * Theme Repository - Manages custom themes
 */
@Singleton
class ThemeRepository @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.themeDataStore
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        val CUSTOM_THEMES_KEY = stringPreferencesKey("custom_themes")
        val SHARED_THEMES_KEY = stringPreferencesKey("shared_themes")
    }

    /**
     * Get all custom themes
     */
    fun getCustomThemes(): Flow<List<CustomTheme>> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[CUSTOM_THEMES_KEY] ?: "[]"
            try {
                json.decodeFromString<List<CustomTheme>>(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        }.safeCollect()
    }

    /**
     * Get shared themes from community
     */
    fun getSharedThemes(): Flow<List<CustomTheme>> {
        return dataStore.data.map { prefs ->
            val jsonStr = prefs[SHARED_THEMES_KEY] ?: "[]"
            try {
                json.decodeFromString<List<CustomTheme>>(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        }.safeCollect()
    }

    /**
     * Save a custom theme
     */
    suspend fun saveCustomTheme(theme: CustomTheme) {
        val themes = getCustomThemes().first().toMutableList()
        val existingIndex = themes.indexOfFirst { it.id == theme.id }
        if (existingIndex >= 0) {
            themes[existingIndex] = theme
        } else {
            themes.add(theme)
        }
        dataStore.edit { prefs ->
            prefs[CUSTOM_THEMES_KEY] = json.encodeToString(themes)
        }
    }

    /**
     * Delete a custom theme
     */
    suspend fun deleteCustomTheme(themeId: String) {
        val themes = getCustomThemes().first().filter { it.id != themeId }
        dataStore.edit { prefs ->
            prefs[CUSTOM_THEMES_KEY] = json.encodeToString(themes)
        }
    }

    /**
     * Import theme from share code
     */
    suspend fun importTheme(shareCode: String, theme: CustomTheme): Result<Unit> {
        return try {
            val themes = getSharedThemes().first().toMutableList()
            themes.add(theme.copy(shareCode = shareCode, isShared = true))
            dataStore.edit { prefs ->
                prefs[SHARED_THEMES_KEY] = json.encodeToString(themes)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export theme to share code
     */
    suspend fun exportTheme(themeId: String): String? {
        val themes = getCustomThemes().first()
        val theme = themes.find { it.id == themeId } ?: return null
        return theme.shareCode ?: "SM-${System.currentTimeMillis().toString(36).uppercase()}"
    }

    /**
     * Get theme by ID
     */
    suspend fun getThemeById(themeId: String): CustomTheme? {
        val customThemes = getCustomThemes().first()
        val sharedThemes = getSharedThemes().first()
        return (customThemes + sharedThemes).find { it.id == themeId }
    }

    /**
     * Import theme from JSON string
     */
    suspend fun importFromJson(jsonString: String): Result<CustomTheme> {
        return try {
            val theme = json.decodeFromString<CustomTheme>(jsonString)
            saveCustomTheme(theme)
            Result.success(theme)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export theme to JSON string
     */
    suspend fun exportToJson(themeId: String): String? {
        val theme = getThemeById(themeId) ?: return null
        return json.encodeToString(theme)
    }

    /**
     * Get theme count
     */
    suspend fun getCustomThemeCount(): Int {
        return getCustomThemes().first().size
    }
}
