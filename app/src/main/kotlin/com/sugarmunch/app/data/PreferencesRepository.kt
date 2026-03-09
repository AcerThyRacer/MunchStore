package com.sugarmunch.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sugarmunch.app.phaseone.UtilityCustomization
import com.sugarmunch.app.theme.profile.AppThemeOverride
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
private val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")

// Catalog layout
private val CATALOG_GRID_COLUMNS = intPreferencesKey("catalog_grid_columns")
private val CATALOG_DEFAULT_VIEW = stringPreferencesKey("catalog_default_view")
private val CATALOG_CARD_STYLE = stringPreferencesKey("catalog_card_style")

// Catalog sort & filter
private val CATALOG_SORT = stringPreferencesKey("catalog_sort")
private val CATALOG_FILTER_CATEGORY = stringPreferencesKey("catalog_filter_category")
private val CATALOG_FILTER_FEATURED = booleanPreferencesKey("catalog_filter_featured")
private val APP_ACCENT_OVERRIDES = stringPreferencesKey("app_accent_overrides")
private val UTILITY_CUSTOMIZATIONS = stringPreferencesKey("utility_customizations")
private val APP_THEME_OVERRIDES = stringPreferencesKey("app_theme_overrides")

/**
 * Repository for app preferences using DataStore.
 * Injected via Hilt - single instance across the app.
 */
@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, String>>() {}.type
    private val utilityMapType = object : TypeToken<MutableMap<String, UtilityCustomization>>() {}.type
    private val appThemeMapType = object : TypeToken<MutableMap<String, AppThemeOverride>>() {}.type

    val overlayEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[OVERLAY_ENABLED] ?: false
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        dataStore.edit { it[OVERLAY_ENABLED] = enabled }
    }

    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    /** When true, animations (particles, gradient shift, card pulse, etc.) are reduced or disabled. Default false. */
    val reduceMotion: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[REDUCE_MOTION] ?: false
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        dataStore.edit { it[REDUCE_MOTION] = enabled }
    }

    // Catalog layout: grid columns 2..4, default view "list" | "grid"
    val catalogGridColumns: Flow<Int> = dataStore.data.map {
        (it[CATALOG_GRID_COLUMNS] ?: 2).coerceIn(2, 4)
    }
    suspend fun setCatalogGridColumns(columns: Int) {
        dataStore.edit { it[CATALOG_GRID_COLUMNS] = columns.coerceIn(2, 4) }
    }
    val catalogDefaultView: Flow<String> = dataStore.data.map {
        it[CATALOG_DEFAULT_VIEW] ?: "list"
    }
    suspend fun setCatalogDefaultView(view: String) {
        dataStore.edit { it[CATALOG_DEFAULT_VIEW] = view }
    }

    // Catalog card style: "compact" | "default" | "accent" | "glass" | "max"
    val catalogCardStyle: Flow<String> = dataStore.data.map {
        it[CATALOG_CARD_STYLE] ?: "default"
    }
    suspend fun setCatalogCardStyle(style: String) {
        dataStore.edit { it[CATALOG_CARD_STYLE] = style }
    }

    // Catalog sort: "default" | "name_asc" | "name_desc" | "category"
    val catalogSort: Flow<String> = dataStore.data.map {
        it[CATALOG_SORT] ?: "default"
    }
    suspend fun setCatalogSort(sort: String) {
        dataStore.edit { it[CATALOG_SORT] = sort }
    }
    val catalogFilterCategory: Flow<String?> = dataStore.data.map {
        it[CATALOG_FILTER_CATEGORY]
    }
    suspend fun setCatalogFilterCategory(category: String?) {
        dataStore.edit {
            if (category != null) it[CATALOG_FILTER_CATEGORY] = category
            else it.remove(CATALOG_FILTER_CATEGORY)
        }
    }
    val catalogFilterFeatured: Flow<Boolean> = dataStore.data.map {
        it[CATALOG_FILTER_FEATURED] ?: false
    }
    suspend fun setCatalogFilterFeatured(featuredOnly: Boolean) {
        dataStore.edit { it[CATALOG_FILTER_FEATURED] = featuredOnly }
    }

    // Per-app accent override (appId -> hex color)
    fun getAppAccentOverrides(): Flow<Map<String, String>> = dataStore.data.map { prefs ->
        val json = prefs[APP_ACCENT_OVERRIDES] ?: return@map emptyMap()
        runCatching { gson.fromJson<Map<String, String>>(json, mapType) }.getOrElse { emptyMap() }
    }
    fun getAppAccent(appId: String): Flow<String?> = getAppAccentOverrides().map { it[appId] }
    suspend fun setAppAccent(appId: String, hexColor: String?) {
        dataStore.edit {
            val current = it[APP_ACCENT_OVERRIDES]?.let { json ->
                runCatching { gson.fromJson<MutableMap<String, String>>(json, mapType) }.getOrNull()
            } ?: mutableMapOf()
            if (hexColor != null) current[appId] = hexColor
            else current.remove(appId)
            it[APP_ACCENT_OVERRIDES] = gson.toJson(current)
        }
    }

    fun getAppThemeOverrides(): Flow<Map<String, AppThemeOverride>> = dataStore.data.map { prefs ->
        val json = prefs[APP_THEME_OVERRIDES] ?: return@map emptyMap()
        runCatching { gson.fromJson<Map<String, AppThemeOverride>>(json, appThemeMapType) }
            .getOrElse { emptyMap() }
    }

    fun getAppThemeOverride(appId: String): Flow<AppThemeOverride?> =
        getAppThemeOverrides().map { it[appId] }

    suspend fun setAppThemeOverride(appId: String, override: AppThemeOverride?) {
        dataStore.edit {
            val current = it[APP_THEME_OVERRIDES]?.let { json ->
                runCatching { gson.fromJson<MutableMap<String, AppThemeOverride>>(json, appThemeMapType) }
                    .getOrNull()
            } ?: mutableMapOf()
            if (override != null) current[appId] = override else current.remove(appId)
            it[APP_THEME_OVERRIDES] = gson.toJson(current)
        }
    }

    suspend fun clearAppThemeOverride(appId: String) {
        setAppThemeOverride(appId, null)
    }

    fun getUtilityCustomization(
        appId: String,
        defaultValue: UtilityCustomization
    ): Flow<UtilityCustomization> = dataStore.data.map { prefs ->
        val current = prefs[UTILITY_CUSTOMIZATIONS]?.let { json ->
            runCatching { gson.fromJson<MutableMap<String, UtilityCustomization>>(json, utilityMapType) }
                .getOrNull()
        } ?: mutableMapOf()
        current[appId] ?: defaultValue
    }

    suspend fun setUtilityCustomization(appId: String, customization: UtilityCustomization) {
        dataStore.edit {
            val current = it[UTILITY_CUSTOMIZATIONS]?.let { json ->
                runCatching { gson.fromJson<MutableMap<String, UtilityCustomization>>(json, utilityMapType) }
                    .getOrNull()
            } ?: mutableMapOf()
            current[appId] = customization
            it[UTILITY_CUSTOMIZATIONS] = gson.toJson(current)
        }
    }
}
