package com.sugarmunch.app.effects.v2.engine

import android.content.Context
import android.view.WindowManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sugarmunch.app.progression.ProgressionTracker
import com.sugarmunch.app.effects.v2.model.*
import com.sugarmunch.app.effects.v2.presets.EffectPresets
import com.sugarmunch.app.effects.v2.presets.EffectRegistry
import com.sugarmunch.app.util.SecureLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private val Context.effectDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_effects_v2")

/**
 * SugarRush Effect Engine V2 - Maximum intensity control
 *
 * Manages all visual and haptic effects with:
 * - Per-effect settings persistence
 * - Master intensity control
 * - Preset support
 * - Effect chains
 * - Boost mode
 */
class EffectEngineV2 private constructor(
    private val context: Context,
    private val gson: Gson = Gson()
) {

    private val logger = SecureLogger.create("EffectEngineV2")
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val progressionTracker = ProgressionTracker.getInstance(context)
    private val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    // DataStore keys
    private fun activeKey(effectId: String) = booleanPreferencesKey("effect_active_$effectId")
    private fun intensityKey(effectId: String) = floatPreferencesKey("effect_intensity_$effectId")
    private fun settingsKey(effectId: String) = stringPreferencesKey("effect_settings_$effectId")

    // Type token for settings map deserialization
    private val settingsMapType = object : TypeToken<Map<String, Any>>() {}.type

    // All available effects
    private val _allEffects = MutableStateFlow<List<EffectV2>>(emptyList())
    val allEffects: StateFlow<List<EffectV2>> = _allEffects.asStateFlow()

    // Effects by category
    val effectsByCategory = _allEffects.map { effects ->
        effects.groupBy { it.category }
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())

    // Active effects state
    private val _activeEffects = MutableStateFlow<Map<String, EffectState>>(emptyMap())
    val activeEffects: StateFlow<Map<String, EffectState>> = _activeEffects.asStateFlow()

    // Active effect IDs only
    val activeEffectIds = _activeEffects.map { it.keys }.stateIn(
        scope, SharingStarted.Eagerly, emptySet()
    )

    // Master intensity (affects all effects)
    private val _masterIntensity = MutableStateFlow(1f)
    val masterIntensity: StateFlow<Float> = _masterIntensity.asStateFlow()

    // Current preset
    private val _currentPreset = MutableStateFlow<EffectPreset?>(null)
    val currentPreset: StateFlow<EffectPreset?> = _currentPreset.asStateFlow()

    // Intensity multiplier (for boost mode)
    private val _intensityMultiplier = MutableStateFlow(1f)

    // Effect chains
    private val _activeChains = MutableStateFlow<List<EffectChain>>(emptyList())

    // Global haptic enabled
    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    // Global audio enabled
    private val _audioEnabled = MutableStateFlow(false) // Off by default
    val audioEnabled: StateFlow<Boolean> = _audioEnabled.asStateFlow()

    // Settings cache for quick access
    private val settingsCache = mutableMapOf<String, Map<String, Any>>()

    init {
        logger.d("Initializing EffectEngineV2")
        loadEffects()
        loadSavedStates()
    }

    private fun loadEffects() {
        _allEffects.value = EffectRegistry.ALL_EFFECTS
        logger.d("Loaded ${_allEffects.value.size} effects")
    }

    /**
     * Load saved effect states and settings from DataStore.
     * This includes:
     * - Active/inactive state
     * - Intensity levels
     * - Per-effect settings (JSON serialized)
     */
    private fun loadSavedStates() {
        scope.launch {
            context.effectDataStore.data.collect { prefs ->
                logger.d("Loading saved effect states")
                val states = mutableMapOf<String, EffectState>()

                _allEffects.value.forEach { effect ->
                    val isActive = prefs[activeKey(effect.id)] ?: false
                    val intensity = prefs[intensityKey(effect.id)] ?: effect.defaultIntensity
                    
                    // Load effect-specific settings
                    val settingsJson = prefs[settingsKey(effect.id)]
                    val settings = if (!settingsJson.isNullOrEmpty()) {
                        try {
                            @Suppress("UNCHECKED_CAST")
                            gson.fromJson(settingsJson, settingsMapType) as? Map<String, Any> ?: emptyMap()
                        } catch (e: Exception) {
                            logger.e("Failed to parse settings for effect ${effect.id}", e)
                            emptyMap()
                        }
                    } else {
                        effect.defaultSettings
                    }

                    // Cache settings for quick access
                    settingsCache[effect.id] = settings

                    if (isActive) {
                        states[effect.id] = EffectState(
                            effect = effect,
                            isActive = true,
                            currentIntensity = intensity,
                            settings = settings
                        )

                        // Re-enable effect if it was active
                        withContext(Dispatchers.Main) {
                            try {
                                effect.enable(context, windowManager, intensity * _masterIntensity.value)
                                logger.d("Re-enabled effect: ${effect.id}")
                            } catch (e: Exception) {
                                logger.e("Failed to re-enable effect ${effect.id}", e)
                            }
                        }
                    }
                }

                _activeEffects.value = states
                logger.d("Loaded ${states.size} active effect states")
            }
        }
    }

    /**
     * Enable an effect with intensity and optional settings
     */
    fun enableEffect(
        effectId: String,
        intensity: Float? = null,
        settings: Map<String, Any>? = null
    ) {
        val effect = _allEffects.value.find { it.id == effectId } ?: run {
            logger.w("Effect not found: $effectId")
            return
        }
        val finalIntensity = intensity ?: effect.defaultIntensity
        val finalSettings = settings ?: effect.defaultSettings

        logger.d("Enabling effect: $effectId with intensity $finalIntensity")

        scope.launch(Dispatchers.Main) {
            // Apply master intensity and multiplier
            val adjustedIntensity = finalIntensity * _masterIntensity.value * _intensityMultiplier.value

            try {
                effect.enable(context, windowManager, adjustedIntensity)
            } catch (e: Exception) {
                logger.e("Failed to enable effect $effectId", e)
                return@launch
            }

            // Update state
            val currentStates = _activeEffects.value.toMutableMap()
            currentStates[effectId] = EffectState(
                effect = effect,
                isActive = true,
                currentIntensity = finalIntensity,
                settings = finalSettings
            )
            _activeEffects.value = currentStates

            // Cache settings
            settingsCache[effectId] = finalSettings

            // Persist
            persistEffectState(effectId, finalIntensity, finalSettings)
            progressionTracker.onEffectEnabled(effectId)
        }
    }

    /**
     * Disable an effect
     */
    fun disableEffect(effectId: String) {
        val effect = _allEffects.value.find { it.id == effectId } ?: run {
            logger.w("Effect not found: $effectId")
            return
        }

        logger.d("Disabling effect: $effectId")

        scope.launch(Dispatchers.Main) {
            try {
                effect.disable()
            } catch (e: Exception) {
                logger.e("Failed to disable effect $effectId", e)
            }

            // Update state
            val currentStates = _activeEffects.value.toMutableMap()
            currentStates.remove(effectId)
            _activeEffects.value = currentStates

            // Remove from cache
            settingsCache.remove(effectId)

            // Persist
            context.effectDataStore.edit { prefs ->
                prefs[activeKey(effectId)] = false
            }
        }
    }

    /**
     * Toggle effect on/off
     */
    fun toggleEffect(effectId: String, intensity: Float? = null) {
        val isActive = _activeEffects.value.containsKey(effectId)
        if (isActive) {
            disableEffect(effectId)
        } else {
            enableEffect(effectId, intensity)
        }
    }

    /**
     * Set intensity for an active effect
     */
    fun setEffectIntensity(effectId: String, intensity: Float) {
        val effect = _allEffects.value.find { it.id == effectId } ?: run {
            logger.w("Effect not found: $effectId")
            return
        }
        val coercedIntensity = intensity.coerceIn(effect.minIntensity, effect.maxIntensity)

        logger.d("Setting intensity for $effectId: $coercedIntensity")

        scope.launch(Dispatchers.Main) {
            val adjustedIntensity = coercedIntensity * _masterIntensity.value * _intensityMultiplier.value
            try {
                effect.setIntensity(adjustedIntensity)
            } catch (e: Exception) {
                logger.e("Failed to set intensity for $effectId", e)
                return@launch
            }

            // Update state
            val currentStates = _activeEffects.value.toMutableMap()
            currentStates[effectId]?.let { state ->
                currentStates[effectId] = state.copy(currentIntensity = coercedIntensity)
            }
            _activeEffects.value = currentStates

            // Persist
            context.effectDataStore.edit { prefs ->
                prefs[intensityKey(effectId)] = coercedIntensity
            }
        }
    }

    /**
     * Update settings for an effect
     */
    fun updateEffectSettings(effectId: String, settings: Map<String, Any>) {
        val effect = _allEffects.value.find { it.id == effectId } ?: run {
            logger.w("Effect not found: $effectId")
            return
        }

        logger.d("Updating settings for $effectId: $settings")

        // Cache settings
        settingsCache[effectId] = settings

        // Persist
        scope.launch {
            context.effectDataStore.edit { prefs ->
                prefs[settingsKey(effectId)] = gson.toJson(settings)
            }
        }

        // If effect is active, update it
        _activeEffects.value[effectId]?.let { state ->
            scope.launch(Dispatchers.Main) {
                try {
                    effect.updateSettings(settings)
                } catch (e: Exception) {
                    logger.e("Failed to update settings for $effectId", e)
                }
            }
        }
    }

    /**
     * Get settings for an effect
     */
    fun getEffectSettings(effectId: String): Map<String, Any> {
        return settingsCache[effectId] ?: run {
            val effect = _allEffects.value.find { it.id == effectId }
            effect?.defaultSettings ?: emptyMap()
        }
    }

    /**
     * Set master intensity (affects all effects)
     */
    fun setMasterIntensity(intensity: Float) {
        val coerced = intensity.coerceIn(0.2f, 2f)
        logger.d("Setting master intensity: $coerced")
        _masterIntensity.value = coerced

        // Update all active effects
        _activeEffects.value.keys.forEach { effectId ->
            _activeEffects.value[effectId]?.let { state ->
                setEffectIntensity(effectId, state.currentIntensity)
            }
        }
    }

    /**
     * Boost all effects temporarily
     */
    fun boostAll(durationMs: Long = 3000, boostAmount: Float = 0.5f) {
        logger.d("Boosting all effects for ${durationMs}ms with +$boostAmount")
        val originalMultiplier = _intensityMultiplier.value
        _intensityMultiplier.value = 1f + boostAmount

        // Apply to all active effects
        _activeEffects.value.forEach { (id, state) ->
            setEffectIntensity(id, state.currentIntensity)
        }

        scope.launch {
            delay(durationMs)
            _intensityMultiplier.value = originalMultiplier

            // Restore all effects
            _activeEffects.value.forEach { (id, state) ->
                setEffectIntensity(id, state.currentIntensity)
            }
            logger.d("Boost ended, restored original multiplier")
        }
    }

    /**
     * Apply an effect preset
     */
    fun applyPreset(preset: EffectPreset) {
        logger.d("Applying preset: ${preset.name}")
        scope.launch(Dispatchers.Main) {
            // Disable all current effects
            _activeEffects.value.keys.forEach { disableEffect(it) }

            // Small delay for clean transition
            delay(100)

            // Enable preset effects
            preset.effects.forEach { config ->
                enableEffect(config.effectId, config.intensity)
            }

            _currentPreset.value = preset
            progressionTracker.onEffectPresetUsed(preset.id, preset.effects.size)
            logger.d("Preset applied successfully")
        }
    }

    /**
     * Clear all effects
     */
    fun clearAllEffects() {
        logger.d("Clearing all effects")
        scope.launch(Dispatchers.Main) {
            _activeEffects.value.keys.toList().forEach { disableEffect(it) }
            _currentPreset.value = null
            settingsCache.clear()
        }
    }

    /**
     * Get effects by category
     */
    fun getEffectsByCategory(category: EffectCategory): List<EffectV2> {
        return _allEffects.value.filter { it.category == category }
    }

    /**
     * Search effects
     */
    fun searchEffects(query: String): List<EffectV2> {
        return _allEffects.value.filter { effect ->
            effect.name.contains(query, ignoreCase = true) ||
            effect.description.contains(query, ignoreCase = true) ||
            effect.category.name.contains(query, ignoreCase = true)
        }
    }

    /**
     * Enable/disable haptics globally
     */
    fun setHapticsEnabled(enabled: Boolean) {
        logger.d("Setting haptics enabled: $enabled")
        _hapticsEnabled.value = enabled
    }

    /**
     * Enable/disable audio globally
     */
    fun setAudioEnabled(enabled: Boolean) {
        logger.d("Setting audio enabled: $enabled")
        _audioEnabled.value = enabled
    }

    /**
     * Get combined intensity for an effect
     */
    fun getCombinedIntensity(effectId: String): Float {
        val state = _activeEffects.value[effectId] ?: return 0f
        return state.currentIntensity * _masterIntensity.value * _intensityMultiplier.value
    }

    /**
     * Check if any effects are active
     */
    fun hasActiveEffects(): Boolean = _activeEffects.value.isNotEmpty()

    /**
     * Get active effect count
     */
    fun getActiveEffectCount(): Int = _activeEffects.value.size

    /**
     * Get effect state by ID
     */
    fun getEffectState(effectId: String): EffectState? {
        return _activeEffects.value[effectId]
    }

    /**
     * Persist effect state to DataStore
     */
    private suspend fun persistEffectState(
        effectId: String,
        intensity: Float,
        settings: Map<String, Any>
    ) {
        context.effectDataStore.edit { prefs ->
            prefs[activeKey(effectId)] = true
            prefs[intensityKey(effectId)] = intensity
            prefs[settingsKey(effectId)] = gson.toJson(settings)
        }
    }

    /**
     * Cleanup resources
     */
    fun shutdown() {
        logger.d("Shutting down EffectEngineV2")
        scope.launch(Dispatchers.Main) {
            clearAllEffects()
        }
        scope.cancel()
    }

    // ═════════════════════════════════════════════════════════════
    // SINGLETON
    // ═════════════════════════════════════════════════════════════

    companion object {
        @Volatile
        private var instance: EffectEngineV2? = null

        fun getInstance(context: Context): EffectEngineV2 {
            return instance ?: synchronized(this) {
                instance ?: EffectEngineV2(context.applicationContext).also { instance = it }
            }
        }

        fun destroy() {
            instance?.shutdown()
            instance = null
        }
    }
}
