package com.sugarmunch.app.ui.customization.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sugarmunch.app.ui.customization.*
import com.sugarmunch.app.ui.customization.manager.BackupManager
import com.sugarmunch.app.ui.customization.manager.PresetEngine
import com.sugarmunch.app.ui.customization.manager.ProfileManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * EXTREME Customization ViewModel for SugarMunch
 * Central ViewModel for all customization screens and features
 */
class CustomizationViewModel(
    application: Application,
    private val customizationRepository: CustomizationRepository
) : AndroidViewModel(application) {

    // ═══════════════════════════════════════════════════════════════
    // MANAGERS
    // ═══════════════════════════════════════════════════════════════

    private val profileManager: ProfileManager = ProfileManager(customizationRepository)
    private val presetEngine: PresetEngine = PresetEngine(customizationRepository)
    private val backupManager: BackupManager = BackupManager(application, customizationRepository)

    // ═══════════════════════════════════════════════════════════════
    // STATE FLOWS
    // ═══════════════════════════════════════════════════════════════

    // Background Configuration
    private val _backgroundConfig = MutableStateFlow(BackgroundConfig())
    val backgroundConfig: StateFlow<BackgroundConfig> = _backgroundConfig.asStateFlow()

    // Color Profile
    private val _colorProfile = MutableStateFlow(ColorProfile())
    val colorProfile: StateFlow<ColorProfile> = _colorProfile.asStateFlow()

    // Animation Profile
    private val _animationProfile = MutableStateFlow(AnimationProfile())
    val animationProfile: StateFlow<AnimationProfile> = _animationProfile.asStateFlow()

    // Gesture Mapping
    private val _gestureMapping = MutableStateFlow(GestureMapping())
    val gestureMapping: StateFlow<GestureMapping> = _gestureMapping.asStateFlow()

    // Haptic Pattern
    private val _hapticPattern = MutableStateFlow(HapticPattern())
    val hapticPattern: StateFlow<HapticPattern> = _hapticPattern.asStateFlow()

    // Layout Configuration
    private val _layoutConfig = MutableStateFlow(LayoutConfig())
    val layoutConfig: StateFlow<LayoutConfig> = _layoutConfig.asStateFlow()

    // Navigation Configuration
    private val _navigationConfig = MutableStateFlow(NavigationConfig())
    val navigationConfig: StateFlow<NavigationConfig> = _navigationConfig.asStateFlow()

    // Card Style Configuration
    private val _cardStyleConfig = MutableStateFlow(CardStyleConfig())
    val cardStyleConfig: StateFlow<CardStyleConfig> = _cardStyleConfig.asStateFlow()

    // Typography Configuration
    private val _typographyConfig = MutableStateFlow(TypographyConfig())
    val typographyConfig: StateFlow<TypographyConfig> = _typographyConfig.asStateFlow()

    // Effect Configurations
    private val _effectConfigs = MutableStateFlow<Map<String, EffectConfig>>(emptyMap())
    val effectConfigs: StateFlow<Map<String, EffectConfig>> = _effectConfigs.asStateFlow()

    // Particle Configuration
    private val _particleConfig = MutableStateFlow(ParticleConfig())
    val particleConfig: StateFlow<ParticleConfig> = _particleConfig.asStateFlow()

    // Experimental Flags
    private val _experimentalFlags = MutableStateFlow(ExperimentalFlags())
    val experimentalFlags: StateFlow<ExperimentalFlags> = _experimentalFlags.asStateFlow()

    // User Profiles
    val profiles: StateFlow<List<UserProfile>> = profileManager.profiles
    val activeProfileId: StateFlow<String?> = profileManager.activeProfileId

    // Presets
    val presets: StateFlow<List<PresetConfig>> = presetEngine.presets
    val activePresetId: StateFlow<String?> = presetEngine.activePresetId

    // Backup History
    val backupHistory: StateFlow<List<BackupManager.BackupInfo>> = backupManager.backupHistory
    val isBackingUp: StateFlow<Boolean> = backupManager.isBackingUp
    val isRestoring: StateFlow<Boolean> = backupManager.isRestoring

    // UI State
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastSaveTime = MutableStateFlow<Long?>(null)
    val lastSaveTime: StateFlow<Long?> = _lastSaveTime.asStateFlow()

    // ═══════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════

    init {
        viewModelScope.launch {
            loadAllSettings()
        }
    }

    /**
     * Load all settings from repository
     */
    private suspend fun loadAllSettings() {
        _isLoading.value = true

        try {
            // Initialize managers
            profileManager.initialize()
            presetEngine.initialize()
            backupManager.initialize()

            // Collect all flows
            launch {
                customizationRepository.backgroundConfigFlow.collect { _backgroundConfig.value = it }
            }
            launch {
                customizationRepository.colorProfileFlow.collect { _colorProfile.value = it }
            }
            launch {
                customizationRepository.animationProfileFlow.collect { _animationProfile.value = it }
            }
            launch {
                customizationRepository.gestureMappingFlow.collect { _gestureMapping.value = it }
            }
            launch {
                customizationRepository.hapticPatternFlow.collect { _hapticPattern.value = it }
            }
            launch {
                customizationRepository.layoutConfigFlow.collect { _layoutConfig.value = it }
            }
            launch {
                customizationRepository.navigationConfigFlow.collect { _navigationConfig.value = it }
            }
            launch {
                customizationRepository.cardStyleConfigFlow.collect { _cardStyleConfig.value = it }
            }
            launch {
                customizationRepository.typographyConfigFlow.collect { _typographyConfig.value = it }
            }
            launch {
                customizationRepository.effectConfigsFlow.collect { _effectConfigs.value = it }
            }
            launch {
                customizationRepository.particleConfigFlow.collect { _particleConfig.value = it }
            }
            launch {
                customizationRepository.experimentalFlagsFlow.collect { _experimentalFlags.value = it }
            }
        } finally {
            _isLoading.value = false
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SAVE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    fun saveBackgroundConfig(config: BackgroundConfig) {
        viewModelScope.launch {
            customizationRepository.saveBackgroundConfig(config)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveColorProfile(profile: ColorProfile) {
        viewModelScope.launch {
            customizationRepository.saveColorProfile(profile)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveAnimationProfile(profile: AnimationProfile) {
        viewModelScope.launch {
            customizationRepository.saveAnimationProfile(profile)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveGestureMapping(mapping: GestureMapping) {
        viewModelScope.launch {
            customizationRepository.saveGestureMapping(mapping)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveHapticPattern(pattern: HapticPattern) {
        viewModelScope.launch {
            customizationRepository.saveHapticPattern(pattern)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveLayoutConfig(config: LayoutConfig) {
        viewModelScope.launch {
            customizationRepository.saveLayoutConfig(config)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveNavigationConfig(config: NavigationConfig) {
        viewModelScope.launch {
            customizationRepository.saveNavigationConfig(config)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveCardStyleConfig(config: CardStyleConfig) {
        viewModelScope.launch {
            customizationRepository.saveCardStyleConfig(config)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveTypographyConfig(config: TypographyConfig) {
        viewModelScope.launch {
            customizationRepository.saveTypographyConfig(config)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveEffectConfigs(configs: Map<String, EffectConfig>) {
        viewModelScope.launch {
            customizationRepository.saveEffectConfigs(configs)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveParticleConfig(config: ParticleConfig) {
        viewModelScope.launch {
            customizationRepository.saveParticleConfig(config)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    fun saveExperimentalFlags(flags: ExperimentalFlags) {
        viewModelScope.launch {
            customizationRepository.saveExperimentalFlags(flags)
            _lastSaveTime.value = System.currentTimeMillis()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PROFILE OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    fun createProfile(
        name: String,
        category: ProfileCategory = ProfileCategory.CUSTOM,
        description: String = "",
        iconId: String = "default",
        iconColor: String = "#FFFF69B4"
    ) {
        viewModelScope.launch {
            profileManager.createProfile(name, category, description, iconId, iconColor)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            profileManager.updateProfile(profile)
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            profileManager.deleteProfile(profileId)
        }
    }

    fun activateProfile(profileId: String) {
        viewModelScope.launch {
            profileManager.activateProfile(profileId)
        }
    }

    fun deactivateProfile() {
        viewModelScope.launch {
            profileManager.deactivateProfile()
        }
    }

    fun duplicateProfile(sourceProfileId: String, newName: String) {
        viewModelScope.launch {
            profileManager.duplicateProfile(sourceProfileId, newName)
        }
    }

    fun setProfileFavorite(profileId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            profileManager.setFavorite(profileId, isFavorite)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PRESET OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    fun createPreset(
        name: String,
        description: String = "",
        category: PresetCategory = PresetCategory.CUSTOM,
        backgroundConfig: BackgroundConfig? = null,
        colorProfile: ColorProfile? = null,
        animationProfile: AnimationProfile? = null
    ) {
        viewModelScope.launch {
            presetEngine.createPreset(
                name = name,
                description = description,
                category = category,
                backgroundConfig = backgroundConfig,
                colorProfile = colorProfile,
                animationProfile = animationProfile
            )
        }
    }

    fun updatePreset(preset: PresetConfig) {
        viewModelScope.launch {
            presetEngine.updatePreset(preset)
        }
    }

    fun deletePreset(presetId: String) {
        viewModelScope.launch {
            presetEngine.deletePreset(presetId)
        }
    }

    fun activatePreset(presetId: String) {
        viewModelScope.launch {
            presetEngine.activatePreset(presetId)
        }
    }

    fun ratePreset(presetId: String, rating: Float) {
        viewModelScope.launch {
            presetEngine.ratePreset(presetId, rating)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // BACKUP OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    fun createBackup(
        type: BackupType = BackupType.FULL,
        categories: List<BackupCategory> = BackupCategory.entries
    ) {
        viewModelScope.launch {
            backupManager.createBackup(type, categories)
        }
    }

    fun restoreBackup(backupId: String, merge: Boolean = false) {
        viewModelScope.launch {
            backupManager.restoreBackup(backupId, merge)
        }
    }

    fun deleteBackup(backupId: String) {
        viewModelScope.launch {
            backupManager.deleteBackup(backupId)
        }
    }

    fun exportSettings(
        categories: List<BackupCategory> = BackupCategory.entries,
        onExported: (String) -> Unit
    ) {
        viewModelScope.launch {
            val json = backupManager.exportSettings(categories)
            onExported(json)
        }
    }

    fun importSettings(jsonString: String, merge: Boolean = false, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.importSettings(jsonString, merge)
            onResult(result is BackupManager.BackupResult.Success)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CONTEXTUAL OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    fun checkContextualTriggers(
        batteryLevel: Int? = null,
        isCharging: Boolean? = null,
        hourOfDay: Int? = null,
        currentApp: String? = null
    ) {
        viewModelScope.launch {
            presetEngine.checkContextualTriggers(
                batteryLevel = batteryLevel,
                isCharging = isCharging,
                hourOfDay = hourOfDay,
                currentApp = currentApp
            )
        }
    }

    fun autoSwitchProfile(
        batteryLevel: Int? = null,
        isCharging: Boolean? = null,
        hourOfDay: Int? = null
    ) {
        viewModelScope.launch {
            profileManager.autoSwitchProfile(
                batteryLevel = batteryLevel,
                isCharging = isCharging,
                hourOfDay = hourOfDay
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SEARCH & FILTER
    // ═══════════════════════════════════════════════════════════════

    fun searchProfiles(query: String): List<UserProfile> {
        return profileManager.searchProfiles(query)
    }

    fun searchPresets(query: String): List<PresetConfig> {
        return presetEngine.searchPresets(query)
    }

    fun getProfilesByCategory(category: ProfileCategory): List<UserProfile> {
        return profileManager.getProfilesByCategory(category)
    }

    fun getPresetsByCategory(category: PresetCategory): List<PresetConfig> {
        return presetEngine.getPresetsByCategory(category)
    }

    // ═══════════════════════════════════════════════════════════════
    // RESET OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    fun resetToDefaults() {
        viewModelScope.launch {
            saveBackgroundConfig(BackgroundConfig())
            saveColorProfile(ColorProfile())
            saveAnimationProfile(AnimationProfile())
            saveGestureMapping(GestureMapping())
            saveHapticPattern(HapticPattern())
            saveLayoutConfig(LayoutConfig())
            saveNavigationConfig(NavigationConfig())
            saveCardStyleConfig(CardStyleConfig())
            saveTypographyConfig(TypographyConfig())
            saveEffectConfigs(emptyMap())
            saveParticleConfig(ParticleConfig())
            saveExperimentalFlags(ExperimentalFlags())
        }
    }

    fun resetCategory(category: ResetCategory) {
        viewModelScope.launch {
            when (category) {
                ResetCategory.BACKGROUNDS -> saveBackgroundConfig(BackgroundConfig())
                ResetCategory.COLORS -> saveColorProfile(ColorProfile())
                ResetCategory.ANIMATIONS -> saveAnimationProfile(AnimationProfile())
                ResetCategory.GESTURES -> saveGestureMapping(GestureMapping())
                ResetCategory.HAPTICS -> saveHapticPattern(HapticPattern())
                ResetCategory.LAYOUTS -> saveLayoutConfig(LayoutConfig())
                ResetCategory.NAVIGATION -> saveNavigationConfig(NavigationConfig())
                ResetCategory.CARDS -> saveCardStyleConfig(CardStyleConfig())
                ResetCategory.TYPOGRAPHY -> saveTypographyConfig(TypographyConfig())
                ResetCategory.EFFECTS -> saveEffectConfigs(emptyMap())
                ResetCategory.PARTICLES -> saveParticleConfig(ParticleConfig())
                ResetCategory.EXPERIMENTAL -> saveExperimentalFlags(ExperimentalFlags())
            }
        }
    }
}

enum class ResetCategory {
    BACKGROUNDS,
    COLORS,
    ANIMATIONS,
    GESTURES,
    HAPTICS,
    LAYOUTS,
    NAVIGATION,
    CARDS,
    TYPOGRAPHY,
    EFFECTS,
    PARTICLES,
    EXPERIMENTAL
}
