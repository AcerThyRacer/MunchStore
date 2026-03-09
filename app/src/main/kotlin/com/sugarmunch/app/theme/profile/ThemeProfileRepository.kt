package com.sugarmunch.app.theme.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sugarmunch.app.theme.presets.ThemePresets
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeProfilesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sugarmunch_theme_profiles"
)

@Singleton
class ThemeProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val currentProfileIdKey = stringPreferencesKey("current_theme_profile_id")
    private val customProfilesKey = stringPreferencesKey("custom_theme_profiles_json")
    private val importedFontsKey = stringPreferencesKey("imported_theme_fonts_json")

    private val profileListType = object : TypeToken<List<ThemeProfile>>() {}.type
    private val fontListType = object : TypeToken<List<ImportedFontAsset>>() {}.type

    private val builtInProfiles: List<ThemeProfile> by lazy {
        ThemePresets.ALL_THEMES_2026.map { it.toThemeProfile(builtIn = true) }
    }

    val customProfiles: Flow<List<ThemeProfile>> = context.themeProfilesDataStore.data.map { prefs ->
        prefs[customProfilesKey]?.let(::decodeProfiles).orEmpty()
    }

    val importedFonts: Flow<List<ImportedFontAsset>> = context.themeProfilesDataStore.data.map { prefs ->
        prefs[importedFontsKey]?.let(::decodeFonts).orEmpty()
    }

    val allProfiles: Flow<List<ThemeProfile>> = customProfiles.map { custom ->
        (builtInProfiles + custom).distinctBy { it.id }
    }

    val currentProfileId: Flow<String> = context.themeProfilesDataStore.data.map { prefs ->
        prefs[currentProfileIdKey] ?: ThemePresets.getDefault().id
    }

    val currentProfile: Flow<ThemeProfile> = combine(allProfiles, currentProfileId) { profiles, currentId ->
        profiles.firstOrNull { it.id == currentId } ?: builtInProfiles.first()
    }

    suspend fun getProfile(profileId: String): ThemeProfile? {
        return allProfiles.first().firstOrNull { it.id == profileId }
    }

    suspend fun setCurrentProfileId(profileId: String) {
        context.themeProfilesDataStore.edit { prefs ->
            prefs[currentProfileIdKey] = profileId
        }
    }

    suspend fun upsertProfile(profile: ThemeProfile): ThemeProfile {
        val existing = customProfiles.first().toMutableList()
        val normalized = profile.withUpdatedTimestamp()
        val index = existing.indexOfFirst { it.id == normalized.id }
        if (index >= 0) {
            existing[index] = normalized
        } else {
            existing += normalized
        }
        persistProfiles(existing)
        return normalized
    }

    suspend fun duplicateProfile(profileId: String): ThemeProfile? {
        val original = getProfile(profileId) ?: return null
        val duplicate = original.copy(
            id = "${original.id}-${System.currentTimeMillis()}",
            name = "${original.name} Copy",
            category = com.sugarmunch.app.theme.model.ThemeCategory.CUSTOM,
            metadata = original.metadata.copy(
                builtIn = false,
                sourceProfileId = original.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        return upsertProfile(duplicate)
    }

    suspend fun deleteProfile(profileId: String) {
        val updated = customProfiles.first().filterNot { it.id == profileId }
        persistProfiles(updated)
        if (currentProfileId.first() == profileId) {
            setCurrentProfileId(ThemePresets.getDefault().id)
        }
    }

    suspend fun saveImportedFont(font: ImportedFontAsset) {
        val updated = importedFonts.first()
            .filterNot { it.id == font.id }
            .plus(font)
            .sortedByDescending { it.importedAt }
        context.themeProfilesDataStore.edit { prefs ->
            prefs[importedFontsKey] = gson.toJson(updated, fontListType)
        }
    }

    suspend fun removeImportedFont(fontId: String) {
        val updated = importedFonts.first().filterNot { it.id == fontId }
        context.themeProfilesDataStore.edit { prefs ->
            prefs[importedFontsKey] = gson.toJson(updated, fontListType)
        }
    }

    private suspend fun persistProfiles(profiles: List<ThemeProfile>) {
        context.themeProfilesDataStore.edit { prefs ->
            prefs[customProfilesKey] = gson.toJson(profiles, profileListType)
        }
    }

    private fun decodeProfiles(json: String): List<ThemeProfile> {
        return runCatching { gson.fromJson<List<ThemeProfile>>(json, profileListType) }
            .getOrElse { emptyList() }
    }

    private fun decodeFonts(json: String): List<ImportedFontAsset> {
        return runCatching { gson.fromJson<List<ImportedFontAsset>>(json, fontListType) }
            .getOrElse { emptyList() }
    }
}
