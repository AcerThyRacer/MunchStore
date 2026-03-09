package com.sugarmunch.app.effects.fab

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.fabConfigDataStore: DataStore<Preferences> by preferencesDataStore(name = "fab_configuration")

/**
 * 🍭 FAB Configuration Manager
 * Manages persistence and state for FAB special effects configuration
 */
class FabConfigurationManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    
    // DataStore keys
    private val FAB_CONFIG_KEY = stringPreferencesKey("fab_configuration")
    private val SELECTED_EFFECTS_KEY = stringSetPreferencesKey("selected_effect_ids")
    private val PRIMARY_EFFECT_KEY = stringPreferencesKey("primary_effect_id")
    private val LAYOUT_MODE_KEY = stringPreferencesKey("layout_mode")
    private val ANIMATION_STYLE_KEY = stringPreferencesKey("animation_style")
    private val SHOW_LABELS_KEY = booleanPreferencesKey("show_labels")
    private val CELEBRATE_INSTALL_KEY = booleanPreferencesKey("celebrate_on_install")
    private val AUTO_ROTATE_KEY = booleanPreferencesKey("auto_rotate_effects")
    
    // ═════════════════════════════════════════════════════════════════
    // STATE FLOWS
    // ═════════════════════════════════════════════════════════════════
    
    private val _configuration = MutableStateFlow(FabConfiguration())
    val configuration: StateFlow<FabConfiguration> = _configuration.asStateFlow()
    
    private val _selectedEffectIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedEffectIds: StateFlow<Set<String>> = _selectedEffectIds.asStateFlow()
    
    private val _primaryEffectId = MutableStateFlow<String?>(null)
    val primaryEffectId: StateFlow<String?> = _primaryEffectId.asStateFlow()
    
    private val _layoutMode = MutableStateFlow(FabLayoutMode.GRID)
    val layoutMode: StateFlow<FabLayoutMode> = _layoutMode.asStateFlow()
    
    private val _animationStyle = MutableStateFlow(FabAnimationStyle.BOUNCE)
    val animationStyle: StateFlow<FabAnimationStyle> = _animationStyle.asStateFlow()
    
    // Derived state
    val selectedEffects: StateFlow<List<FabEffectDisplayInfo>> = selectedEffectIds
        .map { ids ->
            ids.mapNotNull { FabEffectDisplayCatalog.getById(it) }
                .sortedBy { ids.indexOf(it.effectId) }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    
    val hasSelection: StateFlow<Boolean> = selectedEffectIds
        .map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, false)
    
    val selectionCount: StateFlow<Int> = selectedEffectIds
        .map { it.size }
        .stateIn(scope, SharingStarted.Eagerly, 0)
    
    init {
        loadConfiguration()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // SETTINGS OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    private fun loadConfiguration() {
        scope.launch {
            context.fabConfigDataStore.data.collect { prefs ->
                // Load full config if available
                prefs[FAB_CONFIG_KEY]?.let { configJson ->
                    try {
                        val config = json.decodeFromString<FabConfiguration>(configJson)
                        _configuration.value = config
                        _selectedEffectIds.value = config.selectedEffectIds.toSet()
                        _primaryEffectId.value = config.primaryEffectId
                        _layoutMode.value = config.layoutMode
                        _animationStyle.value = config.animationStyle
                    } catch (e: Exception) {
                        // Fall back to individual prefs
                        loadIndividualPrefs(prefs)
                    }
                } ?: run {
                    loadIndividualPrefs(prefs)
                }
            }
        }
    }
    
    private fun loadIndividualPrefs(prefs: Preferences) {
        prefs[SELECTED_EFFECTS_KEY]?.let { 
            _selectedEffectIds.value = it 
        }
        prefs[PRIMARY_EFFECT_KEY]?.let { 
            _primaryEffectId.value = it 
        }
        prefs[LAYOUT_MODE_KEY]?.let { 
            _layoutMode.value = FabLayoutMode.valueOf(it) 
        }
        prefs[ANIMATION_STYLE_KEY]?.let { 
            _animationStyle.value = FabAnimationStyle.valueOf(it) 
        }
        prefs[SHOW_LABELS_KEY]?.let { show ->
            _configuration.value = _configuration.value.copy(showLabels = show)
        }
        prefs[CELEBRATE_INSTALL_KEY]?.let { celebrate ->
            _configuration.value = _configuration.value.copy(celebrateOnInstall = celebrate)
        }
        prefs[AUTO_ROTATE_KEY]?.let { rotate ->
            _configuration.value = _configuration.value.copy(autoRotateEffects = rotate)
        }
    }
    
    private fun saveConfiguration() {
        scope.launch {
            val config = FabConfiguration(
                selectedEffectIds = _selectedEffectIds.value.toList(),
                primaryEffectId = _primaryEffectId.value,
                layoutMode = _layoutMode.value,
                animationStyle = _animationStyle.value,
                showLabels = _configuration.value.showLabels,
                celebrateOnInstall = _configuration.value.celebrateOnInstall,
                autoRotateEffects = _configuration.value.autoRotateEffects,
                rotationIntervalSeconds = _configuration.value.rotationIntervalSeconds,
                customColorScheme = _configuration.value.customColorScheme
            )
            _configuration.value = config
            
            context.fabConfigDataStore.edit { prefs ->
                prefs[FAB_CONFIG_KEY] = json.encodeToString(config)
                prefs[SELECTED_EFFECTS_KEY] = _selectedEffectIds.value
                prefs[PRIMARY_EFFECT_KEY] = _primaryEffectId.value ?: ""
                prefs[LAYOUT_MODE_KEY] = _layoutMode.value.name
                prefs[ANIMATION_STYLE_KEY] = _animationStyle.value.name
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // EFFECT SELECTION MANAGEMENT
    // ═════════════════════════════════════════════════════════════════
    
    fun selectEffect(effectId: String) {
        val current = _selectedEffectIds.value.toMutableSet()
        current.add(effectId)
        _selectedEffectIds.value = current
        
        // Set as primary if first selection
        if (_primaryEffectId.value == null) {
            _primaryEffectId.value = effectId
        }
        
        saveConfiguration()
    }
    
    fun deselectEffect(effectId: String) {
        val current = _selectedEffectIds.value.toMutableSet()
        current.remove(effectId)
        _selectedEffectIds.value = current
        
        // Clear primary if it was this effect
        if (_primaryEffectId.value == effectId) {
            _primaryEffectId.value = current.firstOrNull()
        }
        
        saveConfiguration()
    }
    
    fun toggleEffectSelection(effectId: String): Boolean {
        return if (_selectedEffectIds.value.contains(effectId)) {
            deselectEffect(effectId)
            false
        } else {
            selectEffect(effectId)
            true
        }
    }
    
    fun isEffectSelected(effectId: String): Boolean {
        return _selectedEffectIds.value.contains(effectId)
    }
    
    fun setPrimaryEffect(effectId: String) {
        if (_selectedEffectIds.value.contains(effectId)) {
            _primaryEffectId.value = effectId
            saveConfiguration()
        }
    }
    
    fun reorderEffects(orderedIds: List<String>) {
        val currentSet = _selectedEffectIds.value
        val validIds = orderedIds.filter { it in currentSet }
        _selectedEffectIds.value = validIds.toSet()
        saveConfiguration()
    }
    
    fun clearAllSelections() {
        _selectedEffectIds.value = emptySet()
        _primaryEffectId.value = null
        saveConfiguration()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // CONFIGURATION SETTINGS
    // ═════════════════════════════════════════════════════════════════
    
    fun setLayoutMode(mode: FabLayoutMode) {
        _layoutMode.value = mode
        saveConfiguration()
    }
    
    fun setAnimationStyle(style: FabAnimationStyle) {
        _animationStyle.value = style
        saveConfiguration()
    }
    
    fun setShowLabels(show: Boolean) {
        _configuration.value = _configuration.value.copy(showLabels = show)
        saveConfiguration()
    }
    
    fun setCelebrateOnInstall(celebrate: Boolean) {
        _configuration.value = _configuration.value.copy(celebrateOnInstall = celebrate)
        saveConfiguration()
    }
    
    fun setAutoRotate(autoRotate: Boolean) {
        _configuration.value = _configuration.value.copy(autoRotateEffects = autoRotate)
        saveConfiguration()
    }
    
    fun setRotationInterval(seconds: Int) {
        _configuration.value = _configuration.value.copy(rotationIntervalSeconds = seconds)
        saveConfiguration()
    }
    
    fun setColorScheme(scheme: FabColorScheme) {
        _configuration.value = _configuration.value.copy(customColorScheme = scheme)
        saveConfiguration()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // PRESET APPLICATION
    // ═════════════════════════════════════════════════════════════════
    
    fun applyPreset(preset: FabConfiguration) {
        _selectedEffectIds.value = preset.selectedEffectIds.toSet()
        _primaryEffectId.value = preset.primaryEffectId
        _layoutMode.value = preset.layoutMode
        _animationStyle.value = preset.animationStyle
        _configuration.value = preset
        saveConfiguration()
    }
    
    fun applyPresetById(presetId: String): Boolean {
        val preset = FabConfigurationPresets.ALL_PRESETS.find { it.first == presetId }?.second
        return if (preset != null) {
            applyPreset(preset)
            true
        } else {
            false
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // UTILITY
    // ═════════════════════════════════════════════════════════════════
    
    fun getSelectedEffectsInOrder(): List<FabEffectDisplayInfo> {
        return _selectedEffectIds.value.mapNotNull { 
            FabEffectDisplayCatalog.getById(it) 
        }
    }
    
    fun getPrimaryEffect(): FabEffectDisplayInfo? {
        return _primaryEffectId.value?.let { 
            FabEffectDisplayCatalog.getById(it) 
        }
    }
    
    fun getRandomSelectedEffect(): FabEffectDisplayInfo? {
        val effects = getSelectedEffectsInOrder()
        return if (effects.isNotEmpty()) effects.random() else null
    }
    
    // ═════════════════════════════════════════════════════════════════
    // COMPANION
    // ═════════════════════════════════════════════════════════════════
    
    companion object {
        @Volatile
        private var instance: FabConfigurationManager? = null
        
        fun getInstance(context: Context): FabConfigurationManager {
            return instance ?: synchronized(this) {
                instance ?: FabConfigurationManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
        
        fun destroy() {
            instance = null
        }
    }
}
