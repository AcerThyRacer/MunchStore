package com.sugarmunch.app.plugin

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.effects.v2.engine.EffectEngineV2
import com.sugarmunch.app.plugin.api.SugarMunchPluginApi
import com.sugarmunch.app.plugin.model.*
import com.sugarmunch.app.plugin.security.PluginSecurity
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private val Context.pluginDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_plugins")

/**
 * SugarMunch Plugin Manager - Core plugin system
 * 
 * Manages the complete lifecycle of plugins including loading, unloading,
 * enabling, disabling, and security sandboxing.
 * 
 * Similar to VS Code's extension system or Minecraft's mod loader,
 * plugins can add custom effects, themes, and features.
 */
class PluginManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val security = PluginSecurity(context)
    
    // Plugin storage directory
    private val pluginsDir by lazy {
        File(context.filesDir, "plugins").apply { mkdirs() }
    }
    
    // Loaded plugins registry
    private val _loadedPlugins = MutableStateFlow<Map<String, LoadedPlugin>>(emptyMap())
    val loadedPlugins: StateFlow<Map<String, LoadedPlugin>> = _loadedPlugins.asStateFlow()
    
    // Active (enabled) plugins
    private val _activePlugins = MutableStateFlow<Set<String>>(emptySet())
    val activePlugins: StateFlow<Set<String>> = _activePlugins.asStateFlow()
    
    // Plugin states
    private val pluginStates = ConcurrentHashMap<String, PluginState>()
    
    // Plugin API instance (shared across all plugins)
    private val pluginApi = SugarMunchPluginApi.getInstance(context)
    
    // DataStore keys
    private fun enabledKey(pluginId: String) = booleanPreferencesKey("plugin_enabled_$pluginId")
    private fun metadataKey(pluginId: String) = stringPreferencesKey("plugin_metadata_$pluginId")
    
    // Event callbacks
    private val eventListeners = ConcurrentHashMap<PluginEvent, MutableList<(PluginEventData) -> Unit>>()
    
    init {
        loadSavedPluginStates()
        discoverInstalledPlugins()
    }
    
    /**
     * Load a plugin from a file or directory
     * @param pluginFile The plugin file (.smpkg) or directory
     * @return Result containing the loaded plugin or error
     */
    suspend fun loadPlugin(pluginFile: File): Result<LoadedPlugin> = withContext(Dispatchers.IO) {
        try {
            // Validate plugin structure
            val manifest = security.validatePlugin(pluginFile).getOrThrow()
            
            // Check if already loaded
            if (_loadedPlugins.value.containsKey(manifest.id)) {
                return@withContext Result.failure(
                    PluginException.AlreadyLoaded("Plugin ${manifest.id} is already loaded")
                )
            }
            
            // Check permissions
            if (!security.checkPermissions(manifest)) {
                return@withContext Result.failure(
                    PluginException.PermissionDenied("Plugin ${manifest.id} requires permissions not granted")
                )
            }
            
            // Create plugin instance
            val plugin = PluginInstance(
                manifest = manifest,
                sourceFile = pluginFile,
                context = createPluginContext(manifest)
            )
            
            // Initialize plugin
            plugin.initialize()
            
            // Register with loaded plugins
            val loadedPlugin = LoadedPlugin(
                instance = plugin,
                manifest = manifest,
                state = PluginState.LOADED,
                loadTime = System.currentTimeMillis()
            )
            
            _loadedPlugins.update { it + (manifest.id to loadedPlugin) }
            pluginStates[manifest.id] = PluginState.LOADED
            
            // Emit event
            emitEvent(PluginEvent.LOADED, PluginEventData.Loaded(manifest.id))
            
            // Auto-enable if it was previously enabled
            val wasEnabled = context.pluginDataStore.data.first()[enabledKey(manifest.id)] ?: false
            if (wasEnabled && manifest.autoEnable) {
                enablePlugin(manifest.id)
            }
            
            Result.success(loadedPlugin)
        } catch (e: Exception) {
            Result.failure(PluginException.LoadFailed("Failed to load plugin: ${e.message}", e))
        }
    }
    
    /**
     * Unload a plugin and clean up resources
     * @param pluginId The plugin ID to unload
     */
    suspend fun unloadPlugin(pluginId: String): Result<Unit> = withContext(Dispatchers.Default) {
        val plugin = _loadedPlugins.value[pluginId]
            ?: return@withContext Result.failure(
                PluginException.NotFound("Plugin $pluginId not found")
            )
        
        try {
            // Disable first if active
            if (_activePlugins.value.contains(pluginId)) {
                disablePlugin(pluginId)
            }
            
            // Shutdown plugin
            plugin.instance.shutdown()
            
            // Remove from registry
            _loadedPlugins.update { it - pluginId }
            pluginStates.remove(pluginId)
            
            // Clean up resources
            pluginApi.unregisterAllForPlugin(pluginId)
            
            emitEvent(PluginEvent.UNLOADED, PluginEventData.Unloaded(pluginId))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(PluginException.UnloadFailed("Failed to unload plugin: ${e.message}", e))
        }
    }
    
    /**
     * Enable a plugin and activate its features
     * @param pluginId The plugin ID to enable
     */
    fun enablePlugin(pluginId: String): Result<Unit> {
        val plugin = _loadedPlugins.value[pluginId]
            ?: return Result.failure(PluginException.NotFound("Plugin $pluginId not found"))
        
        if (_activePlugins.value.contains(pluginId)) {
            return Result.success(Unit) // Already enabled
        }
        
        return try {
            // Check dependencies
            val missingDeps = checkDependencies(plugin.manifest)
            if (missingDeps.isNotEmpty()) {
                return Result.failure(
                    PluginException.DependencyMissing("Missing dependencies: ${missingDeps.joinToString()}")
                )
            }
            
            // Activate plugin
            plugin.instance.activate()
            
            _activePlugins.update { it + pluginId }
            pluginStates[pluginId] = PluginState.ACTIVE
            
            // Persist state
            scope.launch {
                context.pluginDataStore.edit { prefs ->
                    prefs[enabledKey(pluginId)] = true
                }
            }
            
            emitEvent(PluginEvent.ENABLED, PluginEventData.Enabled(pluginId))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(PluginException.EnableFailed("Failed to enable plugin: ${e.message}", e))
        }
    }
    
    /**
     * Disable a plugin without unloading
     * @param pluginId The plugin ID to disable
     */
    fun disablePlugin(pluginId: String): Result<Unit> {
        val plugin = _loadedPlugins.value[pluginId]
            ?: return Result.failure(PluginException.NotFound("Plugin $pluginId not found"))
        
        if (!_activePlugins.value.contains(pluginId)) {
            return Result.success(Unit) // Already disabled
        }
        
        return try {
            // Deactivate plugin
            plugin.instance.deactivate()
            
            _activePlugins.update { it - pluginId }
            pluginStates[pluginId] = PluginState.LOADED
            
            // Persist state
            scope.launch {
                context.pluginDataStore.edit { prefs ->
                    prefs[enabledKey(pluginId)] = false
                }
            }
            
            emitEvent(PluginEvent.DISABLED, PluginEventData.Disabled(pluginId))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(PluginException.DisableFailed("Failed to disable plugin: ${e.message}", e))
        }
    }
    
    /**
     * Get all loaded plugins
     */
    fun getLoadedPlugins(): List<LoadedPlugin> = _loadedPlugins.value.values.toList()
    
    /**
     * Get all active plugins
     */
    fun getActivePlugins(): List<LoadedPlugin> {
        return _activePlugins.value.mapNotNull { _loadedPlugins.value[it] }
    }
    
    /**
     * Get a specific plugin by ID
     */
    fun getPlugin(pluginId: String): LoadedPlugin? = _loadedPlugins.value[pluginId]
    
    /**
     * Check if a plugin is loaded
     */
    fun isPluginLoaded(pluginId: String): Boolean = _loadedPlugins.value.containsKey(pluginId)
    
    /**
     * Check if a plugin is active
     */
    fun isPluginActive(pluginId: String): Boolean = _activePlugins.value.contains(pluginId)
    
    /**
     * Get plugins by category
     */
    fun getPluginsByCategory(category: PluginCategory): List<LoadedPlugin> {
        return _loadedPlugins.value.values.filter { it.manifest.category == category }
    }
    
    /**
     * Search plugins
     */
    fun searchPlugins(query: String): List<LoadedPlugin> {
        return _loadedPlugins.value.values.filter { plugin ->
            plugin.manifest.name.contains(query, ignoreCase = true) ||
            plugin.manifest.description.contains(query, ignoreCase = true) ||
            plugin.manifest.author.contains(query, ignoreCase = true) ||
            plugin.manifest.tags.any { it.contains(query, ignoreCase = true) }
        }
    }
    
    /**
     * Install a plugin from a file
     */
    suspend fun installPlugin(sourceFile: File): Result<LoadedPlugin> = withContext(Dispatchers.IO) {
        try {
            // Validate before installing
            val manifest = security.validatePlugin(sourceFile).getOrThrow()
            
            // Check signature
            if (!security.verifySignature(sourceFile, manifest)) {
                return@withContext Result.failure(
                    PluginException.SecurityException("Plugin signature verification failed")
                )
            }
            
            // Copy to plugins directory
            val targetDir = File(pluginsDir, manifest.id)
            sourceFile.copyRecursively(targetDir, overwrite = true)
            
            // Load the installed plugin
            loadPlugin(targetDir)
        } catch (e: Exception) {
            Result.failure(PluginException.InstallFailed("Failed to install plugin: ${e.message}", e))
        }
    }
    
    /**
     * Uninstall a plugin completely
     */
    suspend fun uninstallPlugin(pluginId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Unload first
            unloadPlugin(pluginId)
            
            // Remove files
            val pluginDir = File(pluginsDir, pluginId)
            if (pluginDir.exists()) {
                pluginDir.deleteRecursively()
            }
            
            // Clear preferences
            context.pluginDataStore.edit { prefs ->
                prefs.remove(enabledKey(pluginId))
                prefs.remove(metadataKey(pluginId))
            }
            
            emitEvent(PluginEvent.UNINSTALLED, PluginEventData.Uninstalled(pluginId))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(PluginException.UninstallFailed("Failed to uninstall plugin: ${e.message}", e))
        }
    }
    
    /**
     * Register an event listener
     */
    fun addEventListener(event: PluginEvent, listener: (PluginEventData) -> Unit) {
        eventListeners.getOrPut(event) { mutableListOf() }.add(listener)
    }
    
    /**
     * Remove an event listener
     */
    fun removeEventListener(event: PluginEvent, listener: (PluginEventData) -> Unit) {
        eventListeners[event]?.remove(listener)
    }
    
    /**
     * Reload all plugins (useful for development)
     */
    suspend fun reloadAllPlugins(): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val plugins = _loadedPlugins.value.keys.toList()
            plugins.forEach { unloadPlugin(it) }
            discoverInstalledPlugins()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private methods
    
    private fun createPluginContext(manifest: PluginManifest): PluginContext {
        return PluginContext(
            manifest = manifest,
            api = pluginApi,
            filesDir = File(pluginsDir, manifest.id),
            cacheDir = File(context.cacheDir, "plugins/${manifest.id}").apply { mkdirs() },
            effectEngine = EffectEngineV2.getInstance(context),
            themeManager = ThemeManager.getInstance(context)
        )
    }
    
    private fun checkDependencies(manifest: PluginManifest): List<String> {
        return manifest.dependencies.filter { dep ->
            !_loadedPlugins.value.containsKey(dep.id) ||
            (_loadedPlugins.value[dep.id]?.manifest?.version?.let { 
                !dep.isSatisfiedBy(it) 
            } ?: true)
        }.map { it.id }
    }
    
    private fun discoverInstalledPlugins() {
        scope.launch {
            pluginsDir.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
                try {
                    loadPlugin(dir)
                } catch (e: Exception) {
                    // Log error but continue loading other plugins
                    Log.e(TAG, "Failed to load plugin from: ${dir.name}", e)
                }
            }
        }
    }
    
    private fun loadSavedPluginStates() {
        scope.launch {
            context.pluginDataStore.data.collect { prefs ->
                // States are loaded per-plugin in loadPlugin
            }
        }
    }
    
    private fun emitEvent(event: PluginEvent, data: PluginEventData) {
        scope.launch {
            eventListeners[event]?.forEach { listener ->
                try {
                    listener(data)
                } catch (e: Exception) {
                    Log.e(TAG, "Plugin event listener threw exception: $event", e)
                }
            }
        }
    }
    
    companion object {
        @Volatile
        private var instance: PluginManager? = null
        
        fun getInstance(context: Context): PluginManager {
            return instance ?: synchronized(this) {
                instance ?: PluginManager(context.applicationContext).also { instance = it }
            }
        }
        
        fun destroy() {
            instance?.let { manager ->
                manager.scope.cancel()
                manager._loadedPlugins.value.values.forEach {
                    try {
                        it.instance.shutdown()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error shutting down plugin: ${it.metadata.id}", e)
                    }
                }
            }
            instance = null
        }
    }
}

/**
 * Represents a loaded plugin with its state
 */
data class LoadedPlugin(
    val instance: PluginInstance,
    val manifest: PluginManifest,
    val state: PluginState,
    val loadTime: Long
) {
    val isActive: Boolean get() = state == PluginState.ACTIVE
    val id: String get() = manifest.id
}

/**
 * Plugin lifecycle states
 */
enum class PluginState {
    DISCOVERED,     // Found but not loaded
    LOADING,        // Currently loading
    LOADED,         // Loaded but not active
    ACTIVATING,     // Currently activating
    ACTIVE,         // Fully active
    DEACTIVATING,   // Currently deactivating
    ERROR           // Error state
}

/**
 * Plugin lifecycle events
 */
enum class PluginEvent {
    LOADED,
    UNLOADED,
    ENABLED,
    DISABLED,
    ERROR,
    UNINSTALLED
}

/**
 * Event data for plugin events
 */
sealed class PluginEventData {
    abstract val pluginId: String
    
    data class Loaded(override val pluginId: String) : PluginEventData()
    data class Unloaded(override val pluginId: String) : PluginEventData()
    data class Enabled(override val pluginId: String) : PluginEventData()
    data class Disabled(override val pluginId: String) : PluginEventData()
    data class Error(override val pluginId: String, val error: Throwable) : PluginEventData()
    data class Uninstalled(override val pluginId: String) : PluginEventData()
}

/**
 * Plugin exceptions
 */
sealed class PluginException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NotFound(message: String) : PluginException(message)
    class AlreadyLoaded(message: String) : PluginException(message)
    class LoadFailed(message: String, cause: Throwable? = null) : PluginException(message, cause)
    class UnloadFailed(message: String, cause: Throwable? = null) : PluginException(message, cause)
    class EnableFailed(message: String, cause: Throwable? = null) : PluginException(message, cause)
    class DisableFailed(message: String, cause: Throwable? = null) : PluginException(message, cause)
    class InstallFailed(message: String, cause: Throwable? = null) : PluginException(message, cause)
    class UninstallFailed(message: String, cause: Throwable? = null) : PluginException(message, cause)
    class PermissionDenied(message: String) : PluginException(message)
    class DependencyMissing(message: String) : PluginException(message)
    class SecurityException(message: String) : PluginException(message)
    class InvalidManifest(message: String) : PluginException(message)
}
