package com.sugarmunch.app.plugin.model

import android.util.Log
import org.json.JSONObject

/**
 * Plugin Manifest - Defines a plugin's metadata and requirements
 * 
 * This is the core descriptor for any SugarMunch plugin, similar to
 * Android's AndroidManifest.xml or VS Code's package.json.
 */
data class PluginManifest(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val author: String,
    val authorId: String,
    val category: PluginCategory,
    val minApiLevel: Int,
    val targetApiLevel: Int,
    val minSugarMunchVersion: String,
    val maxSugarMunchVersion: String?,
    val permissions: Set<PluginPermission>,
    val dependencies: List<PluginDependency>,
    val tags: List<String>,
    val autoEnable: Boolean,
    val entryPoint: String,
    val resources: PluginResources,
    val settings: List<PluginSetting>
) {
    /**
     * Check if this plugin is compatible with a SugarMunch version
     */
    fun isCompatibleWith(sugarMunchVersion: String): Boolean {
        // Simple semver comparison
        return compareVersions(sugarMunchVersion, minSugarMunchVersion) >= 0 &&
               (maxSugarMunchVersion == null || 
                compareVersions(sugarMunchVersion, maxSugarMunchVersion) <= 0)
    }
    
    /**
     * Convert manifest to JSON
     */
    fun toJson(): String {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("description", description)
            put("version", version)
            put("author", author)
            put("authorId", authorId)
            put("category", category.name)
            put("minApiLevel", minApiLevel)
            put("targetApiLevel", targetApiLevel)
            put("minSugarMunchVersion", minSugarMunchVersion)
            maxSugarMunchVersion?.let { put("maxSugarMunchVersion", it) }
            put("permissions", permissions.map { it.name })
            put("dependencies", dependencies.map { 
                JSONObject().apply {
                    put("id", it.id)
                    put("versionRange", it.versionRange)
                    put("optional", it.optional)
                }
            })
            put("tags", tags)
            put("autoEnable", autoEnable)
            put("entryPoint", entryPoint)
            put("resources", JSONObject().apply {
                put("icon", resources.icon)
                put("screenshots", resources.screenshots)
                put("banner", resources.banner)
            })
            put("settings", settings.map {
                JSONObject().apply {
                    put("key", it.key)
                    put("label", it.label)
                    put("type", it.type.name)
                    put("defaultValue", it.defaultValue)
                    put("description", it.description)
                }
            })
        }.toString()
    }
    
    companion object {
        /**
         * Parse manifest from JSON
         */
        fun fromJson(json: String): PluginManifest? {
            return try {
                val obj = JSONObject(json)
                PluginManifest(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    description = obj.getString("description"),
                    version = obj.getString("version"),
                    author = obj.getString("author"),
                    authorId = obj.getString("authorId"),
                    category = PluginCategory.valueOf(obj.getString("category")),
                    minApiLevel = obj.getInt("minApiLevel"),
                    targetApiLevel = obj.getInt("targetApiLevel"),
                    minSugarMunchVersion = obj.getString("minSugarMunchVersion"),
                    maxSugarMunchVersion = obj.optString("maxSugarMunchVersion").takeIf { it.isNotEmpty() },
                    permissions = obj.getJSONArray("permissions").let { arr ->
                        (0 until arr.length()).map { 
                            PluginPermission.valueOf(arr.getString(it)) 
                        }.toSet()
                    },
                    dependencies = obj.getJSONArray("dependencies").let { arr ->
                        (0 until arr.length()).map {
                            val dep = arr.getJSONObject(it)
                            PluginDependency(
                                id = dep.getString("id"),
                                versionRange = dep.getString("versionRange"),
                                optional = dep.optBoolean("optional", false)
                            )
                        }
                    },
                    tags = obj.getJSONArray("tags").let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    },
                    autoEnable = obj.optBoolean("autoEnable", false),
                    entryPoint = obj.getString("entryPoint"),
                    resources = obj.getJSONObject("resources").let { res ->
                        PluginResources(
                            icon = res.getString("icon"),
                            screenshots = res.getJSONArray("screenshots").let { arr ->
                                (0 until arr.length()).map { arr.getString(it) }
                            },
                            banner = res.optString("banner").takeIf { it.isNotEmpty() }
                        )
                    },
                    settings = obj.getJSONArray("settings").let { arr ->
                        (0 until arr.length()).map {
                            val set = arr.getJSONObject(it)
                            PluginSetting(
                                key = set.getString("key"),
                                label = set.getString("label"),
                                type = PluginSettingType.valueOf(set.getString("type")),
                                defaultValue = set.get("defaultValue"),
                                description = set.optString("description")
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("PluginModels", "Failed to parse plugin manifest", e)
                null
            }
        }
        
        private fun compareVersions(v1: String, v2: String): Int {
            val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
            val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
            
            for (i in 0 until maxOf(parts1.size, parts2.size)) {
                val p1 = parts1.getOrElse(i) { 0 }
                val p2 = parts2.getOrElse(i) { 0 }
                
                if (p1 != p2) return p1 - p2
            }
            return 0
        }
    }
}

/**
 * Plugin category
 */
enum class PluginCategory {
    EFFECTS,    // Visual effects
    THEMES,     // Color themes
    TOOLS,      // Utility tools
    WIDGETS,    // Home screen widgets
    OVERLAYS,   // Screen overlays
    INTEGRATIONS,  // App integrations
    MISC        // Miscellaneous
}

/**
 * Plugin permissions
 */
enum class PluginPermission {
    NETWORK,           // Internet access
    STORAGE_READ,      // Read external storage
    STORAGE_WRITE,     // Write external storage
    OVERLAY,           // System overlay permission
    NOTIFICATIONS,     // Post notifications
    BATTERY,           // Battery stats access
    SYSTEM_SETTINGS,   // Modify system settings
    SYSTEM_HOOKS,      // Hook into system (dangerous)
    ACCESSIBILITY      // Accessibility service (dangerous)
}

/**
 * Plugin dependency
 */
data class PluginDependency(
    val id: String,
    val versionRange: String,  // e.g., ">=1.0.0 <2.0.0"
    val optional: Boolean = false
) {
    /**
     * Check if a version satisfies this dependency
     */
    fun isSatisfiedBy(version: String): Boolean {
        // Simple version range check
        return when {
            versionRange.startsWith(">=") -> {
                compareVersions(version, versionRange.substring(2)) >= 0
            }
            versionRange.startsWith(">") -> {
                compareVersions(version, versionRange.substring(1)) > 0
            }
            versionRange.startsWith("<=") -> {
                compareVersions(version, versionRange.substring(2)) <= 0
            }
            versionRange.startsWith("<") -> {
                compareVersions(version, versionRange.substring(1)) < 0
            }
            versionRange.startsWith("=") || versionRange.startsWith("^") -> {
                version.startsWith(versionRange.substring(1))
            }
            else -> version == versionRange
        }
    }
    
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            
            if (p1 != p2) return p1 - p2
        }
        return 0
    }
}

/**
 * Plugin resources
 */
data class PluginResources(
    val icon: String,
    val screenshots: List<String>,
    val banner: String? = null
)

/**
 * Plugin setting
 */
data class PluginSetting(
    val key: String,
    val label: String,
    val type: PluginSettingType,
    val defaultValue: Any,
    val description: String = ""
)

/**
 * Plugin setting types
 */
enum class PluginSettingType {
    STRING,
    NUMBER,
    BOOLEAN,
    COLOR,
    SELECT,
    MULTI_SELECT,
    SLIDER
}

/**
 * Plugin context provided to plugins at runtime
 */
data class PluginContext(
    val manifest: PluginManifest,
    val api: com.sugarmunch.app.plugin.api.SugarMunchPluginApi,
    val filesDir: java.io.File,
    val cacheDir: java.io.File,
    val effectEngine: com.sugarmunch.app.effects.v2.engine.EffectEngineV2,
    val themeManager: com.sugarmunch.app.theme.engine.ThemeManager
)

/**
 * Plugin instance interface
 */
interface PluginInstance {
    val manifest: PluginManifest
    val sourceFile: java.io.File
    val context: PluginContext
    
    fun initialize()
    fun activate()
    fun deactivate()
    fun shutdown()
}

/**
 * Base plugin implementation
 */
abstract class BasePluginInstance(
    override val manifest: PluginManifest,
    override val sourceFile: java.io.File,
    override val context: PluginContext
) : PluginInstance {
    
    private var isInitialized = false
    private var isActive = false
    
    override fun initialize() {
        if (!isInitialized) {
            onInitialize()
            isInitialized = true
        }
    }
    
    override fun activate() {
        if (!isActive) {
            onActivate()
            isActive = true
        }
    }
    
    override fun deactivate() {
        if (isActive) {
            onDeactivate()
            isActive = false
        }
    }
    
    override fun shutdown() {
        if (isActive) {
            deactivate()
        }
        onShutdown()
        isInitialized = false
    }
    
    protected abstract fun onInitialize()
    protected abstract fun onActivate()
    protected abstract fun onDeactivate()
    protected abstract fun onShutdown()
}

// ========== STORE MODELS ==========

/**
 * Plugin available in the store
 */
data class StorePlugin(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val author: String,
    val authorId: String,
    val category: PluginCategory,
    val tags: List<String>,
    val rating: Float,
    val ratingCount: Int,
    val downloadCount: Int,
    val iconUrl: String,
    val screenshots: List<String>,
    val downloadUrl: String,
    val checksum: String,
    val size: Long,  // in bytes
    val minApiLevel: Int,
    val publishDate: Long,
    val changelog: List<ChangelogEntry>,
    val isFeatured: Boolean = false
) {
    val formattedSize: String
        get() = when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
        }
    
    val formattedDownloads: String
        get() = when {
            downloadCount < 1000 -> downloadCount.toString()
            downloadCount < 1000000 -> "${downloadCount / 1000}K"
            else -> String.format("%.1fM", downloadCount / 1000000.0)
        }
    
    fun isUpdateAvailable(installedVersion: String): Boolean {
        return compareVersions(version, installedVersion) > 0
    }
    
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            
            if (p1 != p2) return p1 - p2
        }
        return 0
    }
}

/**
 * Changelog entry
 */
data class ChangelogEntry(
    val version: String,
    val changes: String,
    val date: Long
)

/**
 * Plugin review
 */
data class PluginReview(
    val id: String,
    val pluginId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val text: String,
    val date: Long,
    val helpfulCount: Int,
    val isVerifiedPurchase: Boolean
)

/**
 * Developer info
 */
data class DeveloperInfo(
    val id: String,
    val name: String,
    val bio: String,
    val avatarUrl: String,
    val website: String?,
    val pluginCount: Int,
    val totalDownloads: Int,
    val joinedDate: Long
)
