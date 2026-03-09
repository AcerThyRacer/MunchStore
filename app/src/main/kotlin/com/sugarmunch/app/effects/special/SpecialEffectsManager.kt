package com.sugarmunch.app.effects.special

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.effectsDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_effects")

/**
 * 🎆 SPECIAL EFFECTS MANAGER
 * Controls all special visual effects with persistence and reactive state
 */
class SpecialEffectsManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    
    // DataStore keys
    private val ACTIVE_EFFECT_IDS_KEY = stringSetPreferencesKey("active_effect_ids")
    private val EFFECT_CONFIGS_KEY = stringPreferencesKey("effect_configs")
    private val FAVORITE_EFFECTS_KEY = stringSetPreferencesKey("favorite_effects")
    private val QUICK_TOGGLE_ENABLED_KEY = booleanPreferencesKey("quick_toggle_enabled")
    
    // ═════════════════════════════════════════════════════════════════
    // STATE FLOWS
    // ═════════════════════════════════════════════════════════════════
    
    // Currently active/enabled effects
    private val _activeEffectIds = MutableStateFlow<Set<String>>(emptySet())
    val activeEffectIds: StateFlow<Set<String>> = _activeEffectIds.asStateFlow()
    
    // Effect configurations
    private val _effectConfigs = MutableStateFlow<Map<String, SpecialEffectConfig>>(emptyMap())
    val effectConfigs: StateFlow<Map<String, SpecialEffectConfig>> = _effectConfigs.asStateFlow()
    
    // Favorite effects
    private val _favoriteEffects = MutableStateFlow<Set<String>>(emptySet())
    val favoriteEffects: StateFlow<Set<String>> = _favoriteEffects.asStateFlow()
    
    // Quick toggle enabled (for FAB)
    private val _quickToggleEnabled = MutableStateFlow(true)
    val quickToggleEnabled: StateFlow<Boolean> = _quickToggleEnabled.asStateFlow()
    
    // Currently running temporary effects
    private val _runningEffects = MutableStateFlow<List<ActiveEffectState>>(emptyList())
    val runningEffects: StateFlow<List<ActiveEffectState>> = _runningEffects.asStateFlow()
    
    // ═════════════════════════════════════════════════════════════════
    // DERIVED STATES
    // ═════════════════════════════════════════════════════════════════
    
    val activeEffects: StateFlow<List<SpecialEffect>> = combine(
        activeEffectIds,
        effectConfigs
    ) { ids, configs ->
        ids.mapNotNull { id ->
            SpecialEffectsCatalog.getById(id)
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    
    val effectsByCategory = combine(
        activeEffectIds,
        effectConfigs
    ) { ids, configs ->
        ids.mapNotNull { id ->
            SpecialEffectsCatalog.getById(id)?.let { effect ->
                effect to (configs[id] ?: SpecialEffectsCatalog.getDefaultConfig(id))
            }
        }.groupBy { it.first.category }
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())
    
    val hasActiveEffects: StateFlow<Boolean> = activeEffectIds
        .map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, false)
    
    val activeEffectCount: StateFlow<Int> = activeEffectIds
        .map { it.size }
        .stateIn(scope, SharingStarted.Eagerly, 0)
    
    init {
        loadSettings()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // SETTINGS OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    private fun loadSettings() {
        scope.launch {
            context.effectsDataStore.data.collect { prefs ->
                // Load active effects
                prefs[ACTIVE_EFFECT_IDS_KEY]?.let {
                    _activeEffectIds.value = it
                }
                
                // Load effect configs
                prefs[EFFECT_CONFIGS_KEY]?.let { configJson ->
                    try {
                        val configs = json.decodeFromString<Map<String, SpecialEffectConfig>>(configJson)
                        _effectConfigs.value = configs
                    } catch (e: Exception) {
                        // Use defaults
                    }
                }
                
                // Load favorites
                prefs[FAVORITE_EFFECTS_KEY]?.let {
                    _favoriteEffects.value = it
                }
                
                // Load quick toggle
                prefs[QUICK_TOGGLE_ENABLED_KEY]?.let {
                    _quickToggleEnabled.value = it
                }
            }
        }
    }
    
    private fun saveActiveEffects() {
        scope.launch {
            context.effectsDataStore.edit { prefs ->
                prefs[ACTIVE_EFFECT_IDS_KEY] = _activeEffectIds.value
            }
        }
    }
    
    private fun saveEffectConfigs() {
        scope.launch {
            context.effectsDataStore.edit { prefs ->
                prefs[EFFECT_CONFIGS_KEY] = json.encodeToString(_effectConfigs.value)
            }
        }
    }
    
    private fun saveFavorites() {
        scope.launch {
            context.effectsDataStore.edit { prefs ->
                prefs[FAVORITE_EFFECTS_KEY] = _favoriteEffects.value
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // EFFECT MANAGEMENT
    // ═════════════════════════════════════════════════════════════════
    
    fun enableEffect(effectId: String) {
        val current = _activeEffectIds.value.toMutableSet()
        current.add(effectId)
        _activeEffectIds.value = current
        
        // Initialize config if not exists
        if (!_effectConfigs.value.containsKey(effectId)) {
            val config = SpecialEffectsCatalog.getDefaultConfig(effectId)
            updateEffectConfig(config)
        }
        
        saveActiveEffects()
    }
    
    fun disableEffect(effectId: String) {
        val current = _activeEffectIds.value.toMutableSet()
        current.remove(effectId)
        _activeEffectIds.value = current
        saveActiveEffects()
        
        // Also stop if running
        stopEffect(effectId)
    }
    
    fun toggleEffect(effectId: String): Boolean {
        return if (_activeEffectIds.value.contains(effectId)) {
            disableEffect(effectId)
            false
        } else {
            enableEffect(effectId)
            true
        }
    }
    
    fun isEffectEnabled(effectId: String): Boolean {
        return _activeEffectIds.value.contains(effectId)
    }
    
    fun updateEffectConfig(config: SpecialEffectConfig) {
        val current = _effectConfigs.value.toMutableMap()
        current[config.effectId] = config
        _effectConfigs.value = current
        saveEffectConfigs()
    }
    
    fun getEffectConfig(effectId: String): SpecialEffectConfig {
        return _effectConfigs.value[effectId] 
            ?: SpecialEffectsCatalog.getDefaultConfig(effectId)
    }
    
    // ═════════════════════════════════════════════════════════════════
    // TEMPORARY EFFECT RUNNING
    // ═════════════════════════════════════════════════════════════════
    
    fun triggerEffect(effectId: String, durationMs: Long = 5000) {
        val effect = SpecialEffectsCatalog.getById(effectId) ?: return
        
        val state = ActiveEffectState(
            effectId = effectId,
            durationMs = durationMs,
            startTime = System.currentTimeMillis()
        )
        
        val current = _runningEffects.value.toMutableList()
        current.add(state)
        _runningEffects.value = current
        
        // Auto-remove after duration
        scope.launch {
            delay(durationMs)
            stopEffect(state)
        }
    }
    
    fun stopEffect(effectId: String) {
        _runningEffects.value = _runningEffects.value.filter { it.effectId != effectId }
    }
    
    private fun stopEffect(state: ActiveEffectState) {
        _runningEffects.value = _runningEffects.value.filter { it != state }
    }
    
    fun stopAllEffects() {
        _runningEffects.value = emptyList()
    }
    
    fun isEffectRunning(effectId: String): Boolean {
        return _runningEffects.value.any { it.effectId == effectId }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // FAVORITES
    // ═════════════════════════════════════════════════════════════════
    
    fun toggleFavorite(effectId: String): Boolean {
        val current = _favoriteEffects.value.toMutableSet()
        val isNowFavorite = if (current.contains(effectId)) {
            current.remove(effectId)
            false
        } else {
            current.add(effectId)
            true
        }
        _favoriteEffects.value = current
        saveFavorites()
        return isNowFavorite
    }
    
    fun isFavorite(effectId: String): Boolean {
        return _favoriteEffects.value.contains(effectId)
    }
    
    // ═════════════════════════════════════════════════════════════════
    // QUICK TOGGLE (FAB)
    // ═════════════════════════════════════════════════════════════════
    
    fun setQuickToggleEnabled(enabled: Boolean) {
        _quickToggleEnabled.value = enabled
        scope.launch {
            context.effectsDataStore.edit { prefs ->
                prefs[QUICK_TOGGLE_ENABLED_KEY] = enabled
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // BATCH OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    fun enableAllFreeEffects() {
        val freeIds = SpecialEffectsCatalog.FREE_EFFECTS.map { it.id }.toSet()
        _activeEffectIds.value = freeIds
        
        // Initialize configs
        val configs = _effectConfigs.value.toMutableMap()
        freeIds.forEach { id ->
            if (!configs.containsKey(id)) {
                configs[id] = SpecialEffectsCatalog.getDefaultConfig(id)
            }
        }
        _effectConfigs.value = configs
        
        saveActiveEffects()
        saveEffectConfigs()
    }
    
    fun disableAllEffects() {
        _activeEffectIds.value = emptySet()
        stopAllEffects()
        saveActiveEffects()
    }
    
    fun resetToDefaults() {
        _activeEffectIds.value = emptySet()
        _effectConfigs.value = emptyMap()
        _runningEffects.value = emptyList()
        saveActiveEffects()
        saveEffectConfigs()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // UTILITY
    // ═════════════════════════════════════════════════════════════════
    
    fun getEffectIntensity(effectId: String): Float {
        return getEffectConfig(effectId).intensity
    }
    
    fun getEffectSpeed(effectId: String): Float {
        return getEffectConfig(effectId).speed
    }
    
    fun getCustomSetting(effectId: String, settingKey: String): Float? {
        return getEffectConfig(effectId).customSettings[settingKey]
    }
    
    // ═════════════════════════════════════════════════════════════════
    // THEME-AWARE COLORS
    // ═════════════════════════════════════════════════════════════════
    
    fun getEffectColors(effectId: String): List<androidx.compose.ui.graphics.Color> {
        val effect = SpecialEffectsCatalog.getById(effectId) ?: return emptyList()
        val themeManager = ThemeManager.getInstance(context)
        val currentTheme = themeManager.currentTheme.value
        
        // If effect supports theme colors, blend with theme
        return if (effect.supportsColors) {
            effect.defaultColors.map { defaultColor ->
                // Blend with theme primary color for cohesion
                blendWithTheme(defaultColor, currentTheme.baseColors.primary)
            }
        } else {
            effect.defaultColors
        }
    }
    
    private fun blendWithTheme(
        effectColor: androidx.compose.ui.graphics.Color,
        themeColor: androidx.compose.ui.graphics.Color
    ): androidx.compose.ui.graphics.Color {
        val blendRatio = 0.2f // 20% theme influence
        return androidx.compose.ui.graphics.Color(
            red = effectColor.red * (1 - blendRatio) + themeColor.red * blendRatio,
            green = effectColor.green * (1 - blendRatio) + themeColor.green * blendRatio,
            blue = effectColor.blue * (1 - blendRatio) + themeColor.blue * blendRatio,
            alpha = effectColor.alpha
        )
    }
    
    // ═════════════════════════════════════════════════════════════════
    // COMPANION
    // ═════════════════════════════════════════════════════════════════
    
    companion object {
        @Volatile
        private var instance: SpecialEffectsManager? = null
        
        fun getInstance(context: Context): SpecialEffectsManager {
            return instance ?: synchronized(this) {
                instance ?: SpecialEffectsManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
        
        fun destroy() {
            instance = null
        }
    }
}

/**
 * Extension to check if effect is currently visible/active
 */
fun SpecialEffectsManager.isEffectActive(effectId: String): Boolean {
    return isEffectEnabled(effectId) || isEffectRunning(effectId)
}
