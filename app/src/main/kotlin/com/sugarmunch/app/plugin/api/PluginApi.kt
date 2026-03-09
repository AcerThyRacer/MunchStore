package com.sugarmunch.app.plugin.api

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sugarmunch.app.effects.v2.engine.EffectEngineV2
import com.sugarmunch.app.effects.v2.model.*
import com.sugarmunch.app.plugin.EffectPlugin
import com.sugarmunch.app.plugin.ThemePlugin
import com.sugarmunch.app.plugin.model.*
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * SugarMunch Plugin API - Public API for plugin developers
 * 
 * This is the main interface that plugin developers use to interact with
 * SugarMunch. It provides methods to register effects, themes, UI components,
 * and listen to app events.
 * 
 * Example usage:
 * ```kotlin
 * class MyPlugin : SugarMunchPlugin {
 *     override fun onLoad(api: SugarMunchPluginApi) {
 *         api.registerEffect(CustomParticleEffect())
 *         api.registerTheme(MyCustomTheme())
 *     }
 * }
 * ```
 */
class SugarMunchPluginApi private constructor(private val context: Context) {
    
    // Effect engine and theme manager
    private val effectEngine: EffectEngineV2 by lazy { 
        EffectEngineV2.getInstance(context) 
    }
    private val themeManager: ThemeManager by lazy { 
        ThemeManager.getInstance(context) 
    }
    
    // Registered components by plugin
    private val pluginEffects = ConcurrentHashMap<String, MutableSet<String>>()
    private val pluginThemes = ConcurrentHashMap<String, MutableSet<String>>()
    private val pluginUiComponents = ConcurrentHashMap<String, MutableSet<String>>()
    
    // Custom effect registry
    private val _registeredEffects = MutableStateFlow<Map<String, RegisteredEffect>>(emptyMap())
    val registeredEffects: StateFlow<Map<String, RegisteredEffect>> = _registeredEffects.asStateFlow()
    
    // Custom theme registry
    private val _registeredThemes = MutableStateFlow<Map<String, RegisteredTheme>>(emptyMap())
    val registeredThemes: StateFlow<Map<String, RegisteredTheme>> = _registeredThemes.asStateFlow()
    
    // UI component registry
    private val _registeredComponents = MutableStateFlow<Map<String, RegisteredComponent>>(emptyMap())
    val registeredComponents: StateFlow<Map<String, RegisteredComponent>> = _registeredComponents.asStateFlow()
    
    // Event flows
    private val _appEvents = MutableSharedFlow<AppEvent>(extraBufferCapacity = 64)
    val appEvents: SharedFlow<AppEvent> = _appEvents.asSharedFlow()
    
    // Effect toggle events
    private val _effectToggles = MutableSharedFlow<EffectToggleEvent>(extraBufferCapacity = 64)
    val effectToggles: SharedFlow<EffectToggleEvent> = _effectToggles.asSharedFlow()
    
    // App install events
    private val _appInstalls = MutableSharedFlow<AppInstallEvent>(extraBufferCapacity = 64)
    val appInstalls: SharedFlow<AppInstallEvent> = _appInstalls.asSharedFlow()
    
    // Settings change events
    private val _settingsChanges = MutableSharedFlow<SettingsChangeEvent>(extraBufferCapacity = 64)
    val settingsChanges: SharedFlow<SettingsChangeEvent> = _settingsChanges.asSharedFlow()
    
    // ========== EFFECT REGISTRATION ==========
    
    /**
     * Register a custom effect from a plugin
     * @param pluginId The plugin ID registering this effect
     * @param effect The effect plugin to register
     * @return Result containing the registered effect ID or error
     */
    fun registerEffect(pluginId: String, effect: EffectPlugin): Result<String> {
        return try {
            val effectId = "${pluginId}.${effect.id}"
            
            val registeredEffect = RegisteredEffect(
                id = effectId,
                pluginId = pluginId,
                name = effect.name,
                description = effect.description,
                category = effect.category,
                effect = effect
            )
            
            _registeredEffects.update { it + (effectId to registeredEffect) }
            pluginEffects.getOrPut(pluginId) { mutableSetOf() }.add(effectId)
            
            Result.success(effectId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Unregister an effect
     * @param pluginId The plugin ID that registered this effect
     * @param effectId The effect ID to unregister
     */
    fun unregisterEffect(pluginId: String, effectId: String): Result<Unit> {
        return try {
            val registered = _registeredEffects.value[effectId]
            if (registered?.pluginId != pluginId) {
                return Result.failure(SecurityException("Plugin $pluginId cannot unregister effect $effectId"))
            }
            
            _registeredEffects.update { it - effectId }
            pluginEffects[pluginId]?.remove(effectId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get an effect by ID
     */
    fun getEffect(effectId: String): EffectPlugin? {
        return _registeredEffects.value[effectId]?.effect
    }
    
    /**
     * Get all effects registered by a plugin
     */
    fun getPluginEffects(pluginId: String): List<RegisteredEffect> {
        return pluginEffects[pluginId]?.mapNotNull { _registeredEffects.value[it] } ?: emptyList()
    }
    
    // ========== THEME REGISTRATION ==========
    
    /**
     * Register a custom theme from a plugin
     * @param pluginId The plugin ID registering this theme
     * @param theme The theme plugin to register
     * @return Result containing the registered theme ID or error
     */
    fun registerTheme(pluginId: String, theme: ThemePlugin): Result<String> {
        return try {
            val themeId = "${pluginId}.${theme.id}"
            
            val registeredTheme = RegisteredTheme(
                id = themeId,
                pluginId = pluginId,
                name = theme.name,
                description = theme.description,
                category = theme.category,
                theme = theme
            )
            
            _registeredThemes.update { it + (themeId to registeredTheme) }
            pluginThemes.getOrPut(pluginId) { mutableSetOf() }.add(themeId)
            
            Result.success(themeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Unregister a theme
     * @param pluginId The plugin ID that registered this theme
     * @param themeId The theme ID to unregister
     */
    fun unregisterTheme(pluginId: String, themeId: String): Result<Unit> {
        return try {
            val registered = _registeredThemes.value[themeId]
            if (registered?.pluginId != pluginId) {
                return Result.failure(SecurityException("Plugin $pluginId cannot unregister theme $themeId"))
            }
            
            _registeredThemes.update { it - themeId }
            pluginThemes[pluginId]?.remove(themeId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a theme by ID
     */
    fun getTheme(themeId: String): ThemePlugin? {
        return _registeredThemes.value[themeId]?.theme
    }
    
    /**
     * Get all themes registered by a plugin
     */
    fun getPluginThemes(pluginId: String): List<RegisteredTheme> {
        return pluginThemes[pluginId]?.mapNotNull { _registeredThemes.value[it] } ?: emptyList()
    }
    
    // ========== UI COMPONENT REGISTRATION ==========
    
    /**
     * Register a custom UI component
     * @param pluginId The plugin ID registering this component
     * @param componentId Unique component ID (within plugin namespace)
     * @param component The component definition
     */
    fun registerComponent(
        pluginId: String,
        componentId: String,
        component: PluginUiComponent
    ): Result<String> {
        return try {
            val fullId = "$pluginId.$componentId"
            
            val registeredComponent = RegisteredComponent(
                id = fullId,
                pluginId = pluginId,
                name = component.name,
                component = component
            )
            
            _registeredComponents.update { it + (fullId to registeredComponent) }
            pluginUiComponents.getOrPut(pluginId) { mutableSetOf() }.add(fullId)
            
            Result.success(fullId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a registered component
     */
    fun getComponent(componentId: String): RegisteredComponent? {
        return _registeredComponents.value[componentId]
    }
    
    // ========== EVENT HOOKS ==========
    
    /**
     * Called when an app is installed
     * Plugins can use this to apply default effects/themes
     */
    fun onAppInstall(packageName: String, appName: String, isSystemApp: Boolean) {
        _appInstalls.tryEmit(AppInstallEvent(packageName, appName, isSystemApp, System.currentTimeMillis()))
    }
    
    /**
     * Called when an effect is toggled on/off
     */
    fun onEffectToggle(effectId: String, isEnabled: Boolean, intensity: Float) {
        _effectToggles.tryEmit(EffectToggleEvent(effectId, isEnabled, intensity, System.currentTimeMillis()))
    }
    
    /**
     * Called when app settings change
     */
    fun onSettingsChange(key: String, value: Any) {
        _settingsChanges.tryEmit(SettingsChangeEvent(key, value, System.currentTimeMillis()))
    }
    
    /**
     * Emit a generic app event
     */
    fun emitEvent(event: AppEvent) {
        _appEvents.tryEmit(event)
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Get the current theme colors
     */
    fun getCurrentColors(): AdjustedColors {
        return themeManager.currentTheme.value.getColorsForIntensity(
            themeManager.themeIntensity.value
        )
    }
    
    /**
     * Get current theme intensity
     */
    fun getCurrentIntensity(): Float {
        return themeManager.themeIntensity.value
    }
    
    /**
     * Check if an effect is currently active
     */
    fun isEffectActive(effectId: String): Boolean {
        return effectEngine.activeEffects.value.containsKey(effectId)
    }
    
    /**
     * Get list of active effect IDs
     */
    fun getActiveEffectIds(): Set<String> {
        return effectEngine.activeEffectIds.value
    }
    
    /**
     * Enable an effect programmatically
     */
    fun enableEffect(effectId: String, intensity: Float = 1f) {
        effectEngine.enableEffect(effectId, intensity)
    }
    
    /**
     * Disable an effect programmatically
     */
    fun disableEffect(effectId: String) {
        effectEngine.disableEffect(effectId)
    }
    
    /**
     * Toggle an effect
     */
    fun toggleEffect(effectId: String, intensity: Float? = null) {
        effectEngine.toggleEffect(effectId, intensity)
    }
    
    /**
     * Set effect intensity
     */
    fun setEffectIntensity(effectId: String, intensity: Float) {
        effectEngine.setEffectIntensity(effectId, intensity)
    }
    
    /**
     * Apply a theme
     */
    fun applyTheme(theme: CandyTheme) {
        themeManager.setTheme(theme)
    }
    
    /**
     * Boost all effects temporarily
     */
    fun boostEffects(durationMs: Long = 3000, boostAmount: Float = 0.5f) {
        effectEngine.boostAll(durationMs, boostAmount)
    }
    
    /**
     * Unregister all components for a plugin (called on plugin unload)
     */
    internal fun unregisterAllForPlugin(pluginId: String) {
        // Unregister effects
        pluginEffects[pluginId]?.toList()?.forEach { effectId ->
            _registeredEffects.update { it - effectId }
        }
        pluginEffects.remove(pluginId)
        
        // Unregister themes
        pluginThemes[pluginId]?.toList()?.forEach { themeId ->
            _registeredThemes.update { it - themeId }
        }
        pluginThemes.remove(pluginId)
        
        // Unregister UI components
        pluginUiComponents[pluginId]?.toList()?.forEach { componentId ->
            _registeredComponents.update { it - componentId }
        }
        pluginUiComponents.remove(pluginId)
    }
    
    companion object {
        @Volatile
        private var instance: SugarMunchPluginApi? = null
        
        fun getInstance(context: Context): SugarMunchPluginApi {
            return instance ?: synchronized(this) {
                instance ?: SugarMunchPluginApi(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
}

// ========== DATA CLASSES ==========

/**
 * A registered effect from a plugin
 */
data class RegisteredEffect(
    val id: String,
    val pluginId: String,
    val name: String,
    val description: String,
    val category: EffectCategory,
    val effect: EffectPlugin
)

/**
 * A registered theme from a plugin
 */
data class RegisteredTheme(
    val id: String,
    val pluginId: String,
    val name: String,
    val description: String,
    val category: ThemeCategory,
    val theme: ThemePlugin
)

/**
 * A registered UI component from a plugin
 */
data class RegisteredComponent(
    val id: String,
    val pluginId: String,
    val name: String,
    val component: PluginUiComponent
)

// ========== EVENT CLASSES ==========

/**
 * Base class for app events
 */
sealed class AppEvent {
    abstract val timestamp: Long
}

/**
 * Event fired when an app is installed
 */
data class AppInstallEvent(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    override val timestamp: Long
) : AppEvent()

/**
 * Event fired when an effect is toggled
 */
data class EffectToggleEvent(
    val effectId: String,
    val isEnabled: Boolean,
    val intensity: Float,
    override val timestamp: Long
) : AppEvent()

/**
 * Event fired when settings change
 */
data class SettingsChangeEvent(
    val key: String,
    val value: Any,
    override val timestamp: Long
) : AppEvent()

/**
 * Custom app event that plugins can emit
 */
data class CustomAppEvent(
    val eventType: String,
    val data: Map<String, Any>,
    override val timestamp: Long = System.currentTimeMillis()
) : AppEvent()

// ========== UI COMPONENT ==========

/**
 * A UI component that can be registered by plugins
 */
interface PluginUiComponent {
    val name: String
    val description: String
    val placement: UiPlacement
    
    @Composable
    fun Content(modifier: Modifier = Modifier)
}

/**
 * Where in the UI a component can be placed
 */
enum class UiPlacement {
    HOME_SCREEN,        // Main catalog screen
    SETTINGS_SCREEN,    // Settings screen
    EFFECTS_SCREEN,     // Effects screen
    OVERLAY,           // Floating overlay
    TILE,              // Quick settings tile
    WIDGET             // Home screen widget
}
