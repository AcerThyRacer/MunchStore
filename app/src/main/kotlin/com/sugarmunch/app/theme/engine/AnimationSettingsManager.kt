package com.sugarmunch.app.theme.engine

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.theme.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private val Context.animationDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_animations")

/**
 * 🎬 ANIMATION SETTINGS MANAGER
 * Controls all animation customization with reactive state
 */
class AnimationSettingsManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _json = Json { ignoreUnknownKeys = true }
    
    // ═════════════════════════════════════════════════════════════════
    // STATE FLOW - Reactive animation settings
    // ═════════════════════════════════════════════════════════════════
    
    private val _settings = MutableStateFlow(AnimationSettings.DEFAULT)
    val settings: StateFlow<AnimationSettings> = _settings.asStateFlow()
    
    // Convenience accessors
    val animationsEnabled: StateFlow<Boolean> = _settings.map { it.animationsEnabled }
        .stateIn(scope, SharingStarted.Eagerly, true)
    
    val reduceMotion: StateFlow<Boolean> = _settings.map { it.reduceMotion }
        .stateIn(scope, SharingStarted.Eagerly, false)
    
    val transitionDuration: StateFlow<Float> = _settings.map { it.transitionDuration }
        .stateIn(scope, SharingStarted.Eagerly, 300f)
    
    val transitionType: StateFlow<TransitionType> = _settings.map { it.transitionType }
        .stateIn(scope, SharingStarted.Eagerly, TransitionType.SMOOTH)
    
    val particlesEnabled: StateFlow<Boolean> = _settings.map { it.particlesEnabled }
        .stateIn(scope, SharingStarted.Eagerly, true)
    
    val particleDensity: StateFlow<ParticleDensity> = _settings.map { it.particleDensity }
        .stateIn(scope, SharingStarted.Eagerly, ParticleDensity.MEDIUM)
    
    val particleSpeed: StateFlow<Float> = _settings.map { it.particleSpeed }
        .stateIn(scope, SharingStarted.Eagerly, 1f)
    
    val listAnimationType: StateFlow<ListAnimationType> = _settings.map { it.listItemEntrance }
        .stateIn(scope, SharingStarted.Eagerly, ListAnimationType.FADE_SLIDE)
    
    val listStaggerDelay: StateFlow<Float> = _settings.map { it.listStaggerDelay }
        .stateIn(scope, SharingStarted.Eagerly, 50f)
    
    val buttonPressScale: StateFlow<Float> = _settings.map { it.buttonPressScale }
        .stateIn(scope, SharingStarted.Eagerly, 0.95f)
    
    val shimmerEnabled: StateFlow<Boolean> = _settings.map { it.shimmerEnabled }
        .stateIn(scope, SharingStarted.Eagerly, true)
    
    val backgroundAnimationEnabled: StateFlow<Boolean> = _settings.map { it.backgroundAnimationEnabled }
        .stateIn(scope, SharingStarted.Eagerly, true)
    
    val hapticFeedback: StateFlow<Boolean> = _settings.map { it.hapticFeedback }
        .stateIn(scope, SharingStarted.Eagerly, true)
    
    val fabAnimationType: StateFlow<FabAnimationType> = _settings.map { it.fabEntranceAnimation }
        .stateIn(scope, SharingStarted.Eagerly, FabAnimationType.BOUNCE)
    
    val confettiOnSuccess: StateFlow<Boolean> = _settings.map { it.confettiOnSuccess }
        .stateIn(scope, SharingStarted.Eagerly, true)
    
    // ═════════════════════════════════════════════════════════════════
    // DERIVED PROPERTIES
    // ═════════════════════════════════════════════════════════════════
    
    val effectiveAnimationsEnabled: StateFlow<Boolean> = combine(
        animationsEnabled,
        reduceMotion
    ) { enabled, reduce -> enabled && !reduce }
        .stateIn(scope, SharingStarted.Eagerly, true)
    
    val actualParticleDensity: StateFlow<ParticleDensity> = combine(
        particlesEnabled,
        particleDensity
    ) { enabled, density ->
        if (enabled) density else ParticleDensity.OFF
    }.stateIn(scope, SharingStarted.Eagerly, ParticleDensity.MEDIUM)
    
    val actualTransitionDuration: StateFlow<Int> = combine(
        effectiveAnimationsEnabled,
        transitionDuration
    ) { enabled, duration ->
        if (enabled) duration.toInt() else 0
    }.stateIn(scope, SharingStarted.Eagerly, 300)
    
    val isUltraMode: StateFlow<Boolean> = combine(
        particleDensity,
        particleSpeed
    ) { density, speed ->
        density == ParticleDensity.EXTREME && speed > 1.5f
    }.stateIn(scope, SharingStarted.Eagerly, false)
    
    init {
        loadSettings()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // SETTINGS OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    private fun loadSettings() {
        scope.launch {
            context.animationDataStore.data.collect { prefs ->
                val json = prefs[AnimationSettingsKeys.SETTINGS_JSON]
                val settings = json?.let {
                    try {
                        _json.decodeFromString(AnimationSettings.serializer(), it)
                    } catch (e: Exception) {
                        null
                    }
                } ?: AnimationSettings.DEFAULT
                _settings.value = settings
            }
        }
    }
    
    private fun saveSettings() {
        scope.launch {
            context.animationDataStore.edit { prefs ->
                prefs[AnimationSettingsKeys.SETTINGS_JSON] = 
                    _json.encodeToString(AnimationSettings.serializer(), _settings.value)
            }
        }
    }
    
    fun updateSettings(newSettings: AnimationSettings) {
        _settings.value = newSettings
        saveSettings()
    }
    
    fun updateSettings(block: AnimationSettings.() -> AnimationSettings) {
        _settings.value = block(_settings.value)
        saveSettings()
    }
    
    // ═════════════════════════════════════════════════════════════════
    // PRESET OPERATIONS
    // ═════════════════════════════════════════════════════════════════
    
    fun applyPreset(preset: AnimationSettings) {
        _settings.value = preset
        saveSettings()
    }
    
    fun applyPresetByName(name: String) {
        AnimationPresets.ALL_PRESETS.find { it.first == name.lowercase() }?.let {
            applyPreset(it.second)
        }
    }
    
    fun resetToDefault() {
        applyPreset(AnimationSettings.DEFAULT)
    }
    
    // ═════════════════════════════════════════════════════════════════
    // INDIVIDUAL SETTING UPDATES
    // ═════════════════════════════════════════════════════════════════
    
    fun setAnimationsEnabled(enabled: Boolean) {
        updateSettings { copy(animationsEnabled = enabled) }
    }
    
    fun setReduceMotion(enabled: Boolean) {
        updateSettings { copy(reduceMotion = enabled) }
    }
    
    fun setTransitionType(type: TransitionType) {
        updateSettings { copy(transitionType = type) }
    }
    
    fun setTransitionDuration(durationMs: Float) {
        updateSettings { copy(transitionDuration = durationMs.coerceIn(0f, 2000f)) }
    }
    
    fun setListAnimationType(type: ListAnimationType) {
        updateSettings { copy(listItemEntrance = type) }
    }
    
    fun setListStaggerDelay(delayMs: Float) {
        updateSettings { copy(listStaggerDelay = delayMs.coerceIn(0f, 500f)) }
    }
    
    fun setParticleDensity(density: ParticleDensity) {
        updateSettings { copy(particleDensity = density) }
    }
    
    fun setParticleSpeed(speed: Float) {
        updateSettings { copy(particleSpeed = speed.coerceIn(0.1f, 3f)) }
    }
    
    fun setParticlesEnabled(enabled: Boolean) {
        updateSettings { copy(particlesEnabled = enabled) }
    }
    
    fun setButtonPressScale(scale: Float) {
        updateSettings { copy(buttonPressScale = scale.coerceIn(0.8f, 1f)) }
    }
    
    fun setShimmerEnabled(enabled: Boolean) {
        updateSettings { copy(shimmerEnabled = enabled) }
    }
    
    fun setShimmerSpeed(speed: Float) {
        updateSettings { copy(shimmerSpeed = speed.coerceIn(0.1f, 3f)) }
    }
    
    fun setBackgroundAnimationEnabled(enabled: Boolean) {
        updateSettings { copy(backgroundAnimationEnabled = enabled) }
    }
    
    fun setHapticFeedback(enabled: Boolean) {
        updateSettings { copy(hapticFeedback = enabled) }
    }
    
    fun setFabAnimationType(type: FabAnimationType) {
        updateSettings { copy(fabEntranceAnimation = type) }
    }
    
    fun setConfettiOnSuccess(enabled: Boolean) {
        updateSettings { copy(confettiOnSuccess = enabled) }
    }
    
    fun setSpringStiffness(stiffness: Float) {
        updateSettings { copy(springStiffness = stiffness.coerceIn(100f, 1000f)) }
    }
    
    fun setSpringDamping(damping: Float) {
        updateSettings { copy(springDamping = damping.coerceIn(0.1f, 1f)) }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // UTILITY FUNCTIONS
    // ═════════════════════════════════════════════════════════════════
    
    fun getTransitionDurationMs(): Int = actualTransitionDuration.value
    
    fun getListStaggerDelayMs(): Long = 
        if (effectiveAnimationsEnabled.value) listStaggerDelay.value.toLong() else 0L
    
    fun getParticleCount(): IntRange = when (actualParticleDensity.value) {
        ParticleDensity.OFF -> 0..0
        ParticleDensity.MINIMAL -> 10..20
        ParticleDensity.LOW -> 20..40
        ParticleDensity.MEDIUM -> 40..80
        ParticleDensity.HIGH -> 80..150
        ParticleDensity.EXTREME -> 150..300
    }
    
    fun getParticleSpeedMultiplier(): Float = 
        if (effectiveAnimationsEnabled.value) particleSpeed.value else 0f
    
    fun shouldShowParticles(): Boolean = actualParticleDensity.value != ParticleDensity.OFF
    
    fun shouldAnimate(): Boolean = effectiveAnimationsEnabled.value
    
    fun getCurrentPresetName(): String? {
        return AnimationPresets.ALL_PRESETS.find { it.second == _settings.value }?.first
    }
    
    // ═════════════════════════════════════════════════════════════════
    // COMPANION
    // ═════════════════════════════════════════════════════════════════
    
    companion object {
        @Volatile
        private var instance: AnimationSettingsManager? = null
        
        fun getInstance(context: Context): AnimationSettingsManager {
            return instance ?: synchronized(this) {
                instance ?: AnimationSettingsManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
        
        fun destroy() {
            instance = null
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// EXTENSIONS FOR EASY ACCESS
// ═════════════════════════════════════════════════════════════════

fun AnimationSettingsManager.getSpringSpec() = androidx.compose.animation.core.spring(
    stiffness = settings.value.springStiffness,
    dampingRatio = settings.value.springDamping
)

suspend fun AnimationSettingsManager.withReducedMotion(block: suspend () -> Unit) {
    if (shouldAnimate()) block()
}
