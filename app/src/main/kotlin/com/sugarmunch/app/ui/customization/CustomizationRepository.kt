package com.sugarmunch.app.ui.customization

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.data.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central repository for all EXTREME customization settings
 * Manages persistence and retrieval of all customization profiles
 */

private val Context.customizationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sugarmunch_extreme_customization"
)

@Singleton
class CustomizationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // DataStore keys
    private val Keys = object {
        val BACKGROUND_CONFIG = stringPreferencesKey("background_config")
        val COLOR_PROFILE = stringPreferencesKey("color_profile")
        val ANIMATION_PROFILE = stringPreferencesKey("animation_profile")
        val GESTURE_MAPPING = stringPreferencesKey("gesture_mapping")
        val HAPTIC_PATTERN = stringPreferencesKey("haptic_pattern")
        val LAYOUT_CONFIG = stringPreferencesKey("layout_config")
        val NAVIGATION_CONFIG = stringPreferencesKey("navigation_config")
        val CARD_STYLE_CONFIG = stringPreferencesKey("card_style_config")
        val TYPOGRAPHY_CONFIG = stringPreferencesKey("typography_config")
        val EFFECT_CONFIGS = stringPreferencesKey("effect_configs")
        val PARTICLE_CONFIG = stringPreferencesKey("particle_config")
        val USER_PROFILES = stringPreferencesKey("user_profiles")
        val PRESET_CONFIGS = stringPreferencesKey("preset_configs")
        val EXPERIMENTAL_FLAGS = stringPreferencesKey("experimental_flags")
        val ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
    }
    
    // Background Configuration
    val backgroundConfigFlow: Flow<BackgroundConfig> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.BACKGROUND_CONFIG]?.let { 
                json.decodeFromString<BackgroundConfig>(it) 
            } ?: BackgroundConfig()
        }
    
    suspend fun saveBackgroundConfig(config: BackgroundConfig) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.BACKGROUND_CONFIG] = json.encodeToString(config)
        }
    }
    
    // Color Profile
    val colorProfileFlow: Flow<ColorProfile> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.COLOR_PROFILE]?.let { 
                json.decodeFromString<ColorProfile>(it) 
            } ?: ColorProfile()
        }
    
    suspend fun saveColorProfile(profile: ColorProfile) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.COLOR_PROFILE] = json.encodeToString(profile)
        }
    }
    
    // Animation Profile
    val animationProfileFlow: Flow<AnimationProfile> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.ANIMATION_PROFILE]?.let { 
                json.decodeFromString<AnimationProfile>(it) 
            } ?: AnimationProfile()
        }
    
    suspend fun saveAnimationProfile(profile: AnimationProfile) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.ANIMATION_PROFILE] = json.encodeToString(profile)
        }
    }
    
    // Gesture Mapping
    val gestureMappingFlow: Flow<GestureMapping> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.GESTURE_MAPPING]?.let { 
                json.decodeFromString<GestureMapping>(it) 
            } ?: GestureMapping()
        }
    
    suspend fun saveGestureMapping(mapping: GestureMapping) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.GESTURE_MAPPING] = json.encodeToString(mapping)
        }
    }
    
    // Haptic Pattern
    val hapticPatternFlow: Flow<HapticPattern> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.HAPTIC_PATTERN]?.let { 
                json.decodeFromString<HapticPattern>(it) 
            } ?: HapticPattern()
        }
    
    suspend fun saveHapticPattern(pattern: HapticPattern) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.HAPTIC_PATTERN] = json.encodeToString(pattern)
        }
    }
    
    // Layout Configuration
    val layoutConfigFlow: Flow<LayoutConfig> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.LAYOUT_CONFIG]?.let { 
                json.decodeFromString<LayoutConfig>(it) 
            } ?: LayoutConfig()
        }
    
    suspend fun saveLayoutConfig(config: LayoutConfig) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.LAYOUT_CONFIG] = json.encodeToString(config)
        }
    }
    
    // Navigation Configuration
    val navigationConfigFlow: Flow<NavigationConfig> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.NAVIGATION_CONFIG]?.let { 
                json.decodeFromString<NavigationConfig>(it) 
            } ?: NavigationConfig()
        }
    
    suspend fun saveNavigationConfig(config: NavigationConfig) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.NAVIGATION_CONFIG] = json.encodeToString(config)
        }
    }
    
    // Card Style Configuration
    val cardStyleConfigFlow: Flow<CardStyleConfig> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.CARD_STYLE_CONFIG]?.let { 
                json.decodeFromString<CardStyleConfig>(it) 
            } ?: CardStyleConfig()
        }
    
    suspend fun saveCardStyleConfig(config: CardStyleConfig) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.CARD_STYLE_CONFIG] = json.encodeToString(config)
        }
    }
    
    // Typography Configuration
    val typographyConfigFlow: Flow<TypographyConfig> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.TYPOGRAPHY_CONFIG]?.let { 
                json.decodeFromString<TypographyConfig>(it) 
            } ?: TypographyConfig()
        }
    
    suspend fun saveTypographyConfig(config: TypographyConfig) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.TYPOGRAPHY_CONFIG] = json.encodeToString(config)
        }
    }
    
    // Effect Configurations (Map of effect ID to config)
    val effectConfigsFlow: Flow<Map<String, EffectConfig>> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[EFFECT_CONFIGS]?.let { 
                json.decodeFromString<Map<String, EffectConfig>>(it) 
            } ?: emptyMap()
        }
    
    suspend fun saveEffectConfigs(configs: Map<String, EffectConfig>) {
        context.customizationDataStore.edit { preferences ->
            preferences[EFFECT_CONFIGS] = json.encodeToString(configs)
        }
    }
    
    // Particle Configuration
    val particleConfigFlow: Flow<ParticleConfig> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.PARTICLE_CONFIG]?.let { 
                json.decodeFromString<ParticleConfig>(it) 
            } ?: ParticleConfig()
        }
    
    suspend fun saveParticleConfig(config: ParticleConfig) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.PARTICLE_CONFIG] = json.encodeToString(config)
        }
    }
    
    // User Profiles
    val userProfilesFlow: Flow<List<UserProfile>> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.USER_PROFILES]?.let { 
                json.decodeFromString<List<UserProfile>>(it) 
            } ?: emptyList()
        }
    
    suspend fun saveUserProfiles(profiles: List<UserProfile>) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.USER_PROFILES] = json.encodeToString(profiles)
        }
    }
    
    suspend fun addUserProfile(profile: UserProfile) {
        val profiles = userProfilesFlow.value.toMutableList()
        profiles.add(profile)
        saveUserProfiles(profiles)
    }
    
    suspend fun updateUserProfile(profile: UserProfile) {
        val profiles = userProfilesFlow.value.toMutableList()
        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            profiles[index] = profile
            saveUserProfiles(profiles)
        }
    }
    
    suspend fun deleteUserProfile(profileId: String) {
        val profiles = userProfilesFlow.value.filter { it.id != profileId }
        saveUserProfiles(profiles)
    }
    
    // Preset Configurations
    val presetConfigsFlow: Flow<List<PresetConfig>> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.PRESET_CONFIGS]?.let { 
                json.decodeFromString<List<PresetConfig>>(it) 
            } ?: emptyList()
        }
    
    suspend fun savePresetConfigs(configs: List<PresetConfig>) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.PRESET_CONFIGS] = json.encodeToString(configs)
        }
    }
    
    // Experimental Flags
    val experimentalFlagsFlow: Flow<ExperimentalFlags> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.EXPERIMENTAL_FLAGS]?.let { 
                json.decodeFromString<ExperimentalFlags>(it) 
            } ?: ExperimentalFlags()
        }
    
    suspend fun saveExperimentalFlags(flags: ExperimentalFlags) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.EXPERIMENTAL_FLAGS] = json.encodeToString(flags)
        }
    }
    
    // Active Profile
    val activeProfileIdFlow: Flow<String?> = 
        context.customizationDataStore.data.map { preferences ->
            preferences[Keys.ACTIVE_PROFILE_ID]
        }
    
    suspend fun setActiveProfileId(profileId: String?) {
        context.customizationDataStore.edit { preferences ->
            preferences[Keys.ACTIVE_PROFILE_ID] = profileId ?: ""
        }
    }
    
    // Backup & Restore
    suspend fun createBackup(backupType: BackupType, categories: List<BackupCategory>): BackupData {
        val backupData = BackupData(
            backupType = backupType,
            includedCategories = categories,
            backgroundConfig = if (BackupCategory.BACKGROUNDS in categories) backgroundConfigFlow.value else null,
            colorProfile = if (BackupCategory.COLORS in categories) colorProfileFlow.value else null,
            animationProfile = if (BackupCategory.ANIMATIONS in categories) animationProfileFlow.value else null,
            gestureMapping = if (BackupCategory.GESTURES in categories) gestureMappingFlow.value else null,
            hapticPattern = if (BackupCategory.HAPTICS in categories) hapticPatternFlow.value else null,
            layoutConfig = if (BackupCategory.LAYOUTS in categories) layoutConfigFlow.value else null,
            navigationConfig = if (BackupCategory.NAVIGATION in categories) navigationConfigFlow.value else null,
            cardStyleConfig = if (BackupCategory.CARDS in categories) cardStyleConfigFlow.value else null,
            typographyConfig = if (BackupCategory.TYPOGRAPHY in categories) typographyConfigFlow.value else null,
            effectConfigs = if (BackupCategory.EFFECTS in categories) effectConfigsFlow.value else null,
            particleConfig = if (BackupCategory.PARTICLES in categories) particleConfigFlow.value else null,
            profiles = userProfilesFlow.value,
            presets = presetConfigsFlow.value
        )
        
        // Generate checksum
        val checksum = json.encodeToString(backupData).hashCode().toString()
        return backupData.copy(checksum = checksum)
    }
    
    suspend fun restoreBackup(backupData: BackupData, merge: Boolean) {
        // Verify checksum
        val checksum = json.encodeToString(backupData.copy(checksum = "")).hashCode().toString()
        if (checksum != backupData.checksum) {
            throw IllegalStateException("Backup checksum mismatch")
        }
        
        if (merge) {
            // Merge with existing settings
            backupData.backgroundConfig?.let { saveBackgroundConfig(it) }
            backupData.colorProfile?.let { saveColorProfile(it) }
            backupData.animationProfile?.let { saveAnimationProfile(it) }
            backupData.gestureMapping?.let { saveGestureMapping(it) }
            backupData.hapticPattern?.let { saveHapticPattern(it) }
            backupData.layoutConfig?.let { saveLayoutConfig(it) }
            backupData.navigationConfig?.let { saveNavigationConfig(it) }
            backupData.cardStyleConfig?.let { saveCardStyleConfig(it) }
            backupData.typographyConfig?.let { saveTypographyConfig(it) }
            backupData.effectConfigs?.let { saveEffectConfigs(it) }
            backupData.particleConfig?.let { saveParticleConfig(it) }
            
            // Merge profiles and presets
            val existingProfiles = userProfilesFlow.value.toMutableList()
            existingProfiles.addAll(backupData.profiles)
            saveUserProfiles(existingProfiles.distinctBy { it.id })
            
            val existingPresets = presetConfigsFlow.value.toMutableList()
            existingPresets.addAll(backupData.presets)
            savePresetConfigs(existingPresets.distinctBy { it.id })
        } else {
            // Full replace
            backupData.backgroundConfig?.let { saveBackgroundConfig(it) }
            backupData.colorProfile?.let { saveColorProfile(it) }
            backupData.animationProfile?.let { saveAnimationProfile(it) }
            backupData.gestureMapping?.let { saveGestureMapping(it) }
            backupData.hapticPattern?.let { saveHapticPattern(it) }
            backupData.layoutConfig?.let { saveLayoutConfig(it) }
            backupData.navigationConfig?.let { saveNavigationConfig(it) }
            backupData.cardStyleConfig?.let { saveCardStyleConfig(it) }
            backupData.typographyConfig?.let { saveTypographyConfig(it) }
            backupData.effectConfigs?.let { saveEffectConfigs(it) }
            backupData.particleConfig?.let { saveParticleConfig(it) }
            saveUserProfiles(backupData.profiles)
            savePresetConfigs(backupData.presets)
        }
    }
    
    // Export/Import as JSON string
    suspend fun exportSettings(categories: List<BackupCategory>): String {
        val backupData = createBackup(BackupType.PARTIAL, categories)
        return json.encodeToString(backupData)
    }
    
    suspend fun importSettings(jsonString: String, merge: Boolean) {
        val backupData = json.decodeFromString<BackupData>(jsonString)
        restoreBackup(backupData, merge)
    }
}
