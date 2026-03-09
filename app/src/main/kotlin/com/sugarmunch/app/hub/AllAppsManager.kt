package com.sugarmunch.app.hub

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.local.FolderEntity
import com.sugarmunch.app.data.repository.FolderRepository
import com.sugarmunch.app.plugin.store.PluginStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified app information including both SugarMunch apps and system apps
 */
data class UnifiedAppInfo(
    val id: String,
    val name: String,
    val packageName: String,
    val description: String = "",
    val icon: Drawable?,
    val iconUrl: String? = null,
    val category: String? = null,
    val isSugarMunchApp: Boolean,
    val isSystemApp: Boolean,
    val isInstalled: Boolean,
    val versionName: String? = null,
    val versionCode: Long = 0,
    val installTime: Long = 0,
    val updateTime: Long = 0,
    val sizeBytes: Long = 0,
    val accentColor: String? = null,
    val badge: String? = null,
    val featured: Boolean = false,
    val downloadUrl: String? = null,
    val source: String? = null,
    val folderIds: List<String> = emptyList(),
    val launchCount: Int = 0,
    val lastLaunchTime: Long = 0,
    val isFavorite: Boolean = false,
    val customIconPath: String? = null,
    val customName: String? = null,
    // Rating system (0.0 - 5.0)
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    // User's personal rating for this app
    val userRating: Float = 0f,
    // Plugin-specific fields
    val isPlugin: Boolean = false,
    val pluginRating: Float = 0f,
    val pluginCategory: com.sugarmunch.app.plugin.model.PluginCategory? = null
) {
    val displayName: String get() = customName ?: name
    val sortKey: String get() = displayName.lowercase(Locale.getDefault())

    fun toAppEntry(): AppEntry = AppEntry(
        id = id,
        name = name,
        packageName = packageName,
        description = description,
        iconUrl = iconUrl,
        downloadUrl = downloadUrl ?: "",
        version = versionName ?: "",
        source = source,
        category = category,
        accentColor = accentColor,
        badge = badge,
        featured = featured
    )
}

/**
 * App statistics for usage tracking
 */
data class AppUsageStats(
    val packageName: String,
    val launchCount: Int,
    val totalTimeInForegroundMs: Long,
    val lastLaunchTime: Long,
    val dailyLaunchCount: Int,
    val weeklyLaunchCount: Int,
    val monthlyLaunchCount: Int
)

/**
 * App filter options
 */
data class AppFilter(
    val query: String = "",
    val categories: Set<String> = emptySet(),
    val showSystemApps: Boolean = true,
    val showSugarMunchApps: Boolean = true,
    val showInstalledOnly: Boolean = false,
    val showUninstalledOnly: Boolean = false,
    val showFavoritesOnly: Boolean = false,
    val showFeaturedOnly: Boolean = false,
    val minRating: Float = 0f,
    val maxSize: Long = Long.MAX_VALUE,
    val tags: Set<String> = emptySet()
)

/**
 * App sort options
 */
enum class AppSortMode {
    NAME_ASC,
    NAME_DESC,
    INSTALL_DATE_NEWEST,
    INSTALL_DATE_OLDEST,
    USAGE_MOST_USED,
    USAGE_LEAST_USED,
    SIZE_LARGEST,
    SIZE_SMALLEST,
    LAST_LAUNCHED,
    RATING_HIGHEST,
    CUSTOM
}

/**
 * AllAppsManager - Singleton to manage ALL installed apps and SugarMunch apps.
 * Provides unified access to both system apps and SugarMunch catalog apps.
 */
@Singleton
class AllAppsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val folderRepository: FolderRepository,
    private val pluginStore: PluginStore
) {
    private val packageManager = context.packageManager

    private val _allApps = MutableStateFlow<List<UnifiedAppInfo>>(emptyList())
    val allApps: StateFlow<List<UnifiedAppInfo>> = _allApps.asStateFlow()

    private val _sugarMunchApps = MutableStateFlow<List<UnifiedAppInfo>>(emptyList())
    val sugarMunchApps: StateFlow<List<UnifiedAppInfo>> = _sugarMunchApps.asStateFlow()

    private val _systemApps = MutableStateFlow<List<UnifiedAppInfo>>(emptyList())
    val systemApps: StateFlow<List<UnifiedAppInfo>> = _systemApps.asStateFlow()

    private val _installedApps = MutableStateFlow<List<UnifiedAppInfo>>(emptyList())
    val installedApps: StateFlow<List<UnifiedAppInfo>> = _installedApps.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _recentApps = MutableStateFlow<List<UnifiedAppInfo>>(emptyList())
    val recentApps: StateFlow<List<UnifiedAppInfo>> = _recentApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val launchHistory = mutableMapOf<String, MutableList<Long>>()
    private val appCache = mutableMapOf<String, UnifiedAppInfo>()

    companion object {
        private const val TAG = "AllAppsManager"
        
        @Volatile
        private var INSTANCE: AllAppsManager? = null

        fun getInstance(context: Context, folderRepository: FolderRepository): AllAppsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AllAppsManager(context, folderRepository).also { INSTANCE = it }
            }
        }
    }

    /**
     * Load all apps - both SugarMunch and system apps
     */
    suspend fun loadAllApps(
        sugarMunchEntries: List<AppEntry> = emptyList()
    ) = withContext(Dispatchers.IO) {
        _isLoading.value = true

        try {
            // Load system apps
            val systemAppsList = loadSystemApps()
            _systemApps.value = systemAppsList

            // Convert SugarMunch entries to unified info
            val sugarMunchList = sugarMunchEntries.map { entry ->
                val isInstalled = systemAppsList.any { it.packageName == entry.packageName }
                UnifiedAppInfo(
                    id = entry.id,
                    name = entry.name,
                    packageName = entry.packageName,
                    description = entry.description,
                    icon = null, // Loaded asynchronously
                    iconUrl = entry.iconUrl,
                    category = entry.category,
                    isSugarMunchApp = true,
                    isSystemApp = false,
                    isInstalled = isInstalled,
                    versionName = entry.version,
                    accentColor = entry.accentColor,
                    badge = entry.badge,
                    featured = entry.featured == true,
                    downloadUrl = entry.downloadUrl,
                    source = entry.source
                )
            }
            _sugarMunchApps.value = sugarMunchList

            // Merge all apps
            val allAppsList = (systemAppsList + sugarMunchList)
                .distinctBy { it.packageName }
                .sortedBy { it.sortKey }
            _allApps.value = allAppsList

            // Update installed apps
            _installedApps.value = allAppsList.filter { it.isInstalled }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to load apps", e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Load all installed system apps
     */
    private fun loadSystemApps(): List<UnifiedAppInfo> {
        val apps = mutableListOf<UnifiedAppInfo>()

        try {
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            for (packageInfo in packages) {
                val appInfo = packageInfo.applicationInfo ?: continue

                // Skip SugarMunch's own packages
                if (appInfo.packageName.startsWith("com.sugarmunch")) continue

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val unifiedApp = UnifiedAppInfo(
                    id = appInfo.packageName,
                    name = appInfo.loadLabel(packageManager).toString(),
                    packageName = appInfo.packageName,
                    description = "", // Not available for system apps
                    icon = appInfo.loadIcon(packageManager),
                    category = getCategoryForApp(appInfo),
                    isSugarMunchApp = false,
                    isSystemApp = isSystem,
                    isInstalled = true,
                    versionName = packageInfo.versionName,
                    versionCode = packageInfo.longVersionCode,
                    installTime = packageInfo.firstInstallTime,
                    updateTime = packageInfo.lastUpdateTime,
                    sizeBytes = getAppSize(appInfo),
                    accentColor = extractDominantColor(appInfo)
                )

                apps.add(unifiedApp)
                appCache[appInfo.packageName] = unifiedApp
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load system apps", e)
        }

        return apps.sortedBy { it.sortKey }
    }

    /**
     * Get app by package name
     */
    fun getAppByPackageName(packageName: String): UnifiedAppInfo? {
        return appCache[packageName] ?: _allApps.value.find { it.packageName == packageName }
    }

    /**
     * Get app by ID
     */
    fun getAppById(id: String): UnifiedAppInfo? {
        return _allApps.value.find { it.id == id }
    }

    /**
     * Search apps with filter
     */
    fun searchApps(filter: AppFilter): List<UnifiedAppInfo> {
        return _allApps.value.filter { app ->
            // Query filter
            (filter.query.isBlank() ||
                app.name.contains(filter.query, ignoreCase = true) ||
                app.packageName.contains(filter.query, ignoreCase = true) ||
                app.description.contains(filter.query, ignoreCase = true)) &&
            // Category filter
            (filter.categories.isEmpty() || app.category in filter.categories) &&
            // System apps toggle
            (filter.showSystemApps || !app.isSystemApp) &&
            // SugarMunch apps toggle
            (filter.showSugarMunchApps || !app.isSugarMunchApp) &&
            // Installed filter
            (!filter.showInstalledOnly || app.isInstalled) &&
            (!filter.showUninstalledOnly || !app.isInstalled) &&
            // Favorites filter
            (!filter.showFavoritesOnly || app.isFavorite) &&
            // Featured filter
            (!filter.showFeaturedOnly || app.featured)
        }
    }

    /**
     * Sort apps by mode
     */
    fun sortApps(apps: List<UnifiedAppInfo>, mode: AppSortMode): List<UnifiedAppInfo> {
        return when (mode) {
            AppSortMode.NAME_ASC -> apps.sortedBy { it.sortKey }
            AppSortMode.NAME_DESC -> apps.sortedByDescending { it.sortKey }
            AppSortMode.INSTALL_DATE_NEWEST -> apps.sortedByDescending { it.installTime }
            AppSortMode.INSTALL_DATE_OLDEST -> apps.sortedBy { it.installTime }
            AppSortMode.USAGE_MOST_USED -> apps.sortedByDescending { it.launchCount }
            AppSortMode.USAGE_LEAST_USED -> apps.sortedBy { it.launchCount }
            AppSortMode.SIZE_LARGEST -> apps.sortedByDescending { it.sizeBytes }
            AppSortMode.SIZE_SMALLEST -> apps.sortedBy { it.sizeBytes }
            AppSortMode.LAST_LAUNCHED -> apps.sortedByDescending { it.lastLaunchTime }
            AppSortMode.RATING_HIGHEST -> apps.sortedByDescending { 
                // Use user rating if available, otherwise use app store rating
                // Apps with no rating go to the end
                val effectiveRating = if (it.userRating > 0) it.userRating else it.rating
                if (effectiveRating > 0) effectiveRating else -1f
            }
            AppSortMode.CUSTOM -> apps
        }
    }

    /**
     * Get apps in a specific folder
     */
    suspend fun getAppsInFolder(folderId: String): List<UnifiedAppInfo> = withContext(Dispatchers.IO) {
        val folder = folderRepository.getFolderById(folderId) ?: return@withContext emptyList()
        folder.appIds.mapNotNull { getAppById(it) }
    }

    /**
     * Get apps by category
     */
    fun getAppsByCategory(category: String): List<UnifiedAppInfo> {
        return _allApps.value.filter { it.category == category }
    }

    /**
     * Add app to SugarMunch (create entry for system app)
     */
    suspend fun addAppToSugarMunch(packageName: String): UnifiedAppInfo? = withContext(Dispatchers.IO) {
        val app = getAppByPackageName(packageName) ?: return@withContext null

        // Create SugarMunch entry
        val entry = app.toAppEntry()

        // Update the app to be a SugarMunch app
        val updated = app.copy(isSugarMunchApp = true)

        // Update cache
        appCache[packageName] = updated

        // Update lists
        _sugarMunchApps.value = (_sugarMunchApps.value + updated).distinctBy { it.packageName }
        _allApps.value = _allApps.value.map { if (it.packageName == packageName) updated else it }

        updated
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite(appId: String) {
        val currentFavorites = _favorites.value.toMutableSet()
        if (currentFavorites.contains(appId)) {
            currentFavorites.remove(appId)
        } else {
            currentFavorites.add(appId)
        }
        _favorites.value = currentFavorites

        // Update app in list
        _allApps.value = _allApps.value.map { app ->
            if (app.id == appId) app.copy(isFavorite = currentFavorites.contains(appId)) else app
        }
    }

    /**
     * Record app launch
     */
    fun recordAppLaunch(packageName: String) {
        val now = System.currentTimeMillis()
        launchHistory.getOrPut(packageName) { mutableListOf() }.add(now)

        // Update app stats
        _allApps.value = _allApps.value.map { app ->
            if (app.packageName == packageName) {
                app.copy(
                    launchCount = app.launchCount + 1,
                    lastLaunchTime = now
                )
            } else app
        }

        // Update recent apps
        updateRecentApps()
    }

    /**
     * Update recent apps list
     */
    private fun updateRecentApps() {
        val recent = _allApps.value
            .filter { it.lastLaunchTime > 0 }
            .sortedByDescending { it.lastLaunchTime }
            .take(10)
        _recentApps.value = recent
    }

    /**
     * Launch an app
     */
    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                recordAppLaunch(packageName)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Uninstall an app
     */
    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to uninstall app: $packageName", e)
        }
    }

    /**
     * Open app details in settings
     */
    fun openAppSettings(packageName: String) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app settings: $packageName", e)
        }
    }

    /**
     * Get all categories
     */
    fun getAllCategories(): Set<String> {
        return _allApps.value.mapNotNull { it.category }.toSet()
    }

    /**
     * Get category for app based on ApplicationInfo
     */
    private fun getCategoryForApp(appInfo: ApplicationInfo): String {
        return when {
            appInfo.category == ApplicationInfo.CATEGORY_GAME -> "Games"
            appInfo.category == ApplicationInfo.CATEGORY_SOCIAL -> "Social"
            appInfo.category == ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
            appInfo.category == ApplicationInfo.CATEGORY_ACCESSIBILITY -> "Accessibility"
            appInfo.category == ApplicationInfo.CATEGORY_AUDIO -> "Audio"
            appInfo.category == ApplicationInfo.CATEGORY_IMAGE -> "Image"
            appInfo.category == ApplicationInfo.CATEGORY_VIDEO -> "Video"
            appInfo.category == ApplicationInfo.CATEGORY_MAPS -> "Maps"
            appInfo.category == ApplicationInfo.CATEGORY_NEWS -> "News"
            else -> "Other"
        }
    }

    /**
     * Extract dominant color from app icon using Palette API.
     * 
     * Priority order for color selection:
     * 1. Vibrant color (most visually appealing)
     * 2. Dominant color (most common)
     * 3. Muted vibrant (fallback)
     * 4. Light vibrant (final fallback)
     *
     * @param appInfo Application info containing the icon
     * @return Hex color string (e.g., "#FF5722") or null if extraction fails
     */
    private fun extractDominantColor(appInfo: ApplicationInfo): String? {
        return try {
            // Load the app icon
            val drawable: Drawable = appInfo.loadIcon(packageManager) ?: return null
            
            // Convert drawable to bitmap
            val bitmap: Bitmap = when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> {
                    // For vector drawables and other types, create a bitmap
                    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 108
                    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 108
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bmp ->
                        val canvas = android.graphics.Canvas(bmp)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                    }
                }
            }
            
            // Generate palette asynchronously would be better, but for simplicity we do sync
            // In a production app, consider using Palette.Builder().generate() with callback
            val palette = Palette.from(bitmap)
                .maximumColorCount(16)
                .resizeBitmapArea(192) // Optimize for speed
                .generate()
            
            // Try to get the best color in priority order
            val color = palette.vibrantSwatch?.rgb
                ?: palette.dominantSwatch?.rgb
                ?: palette.mutedSwatch?.rgb
                ?: palette.lightVibrantSwatch?.rgb
                ?: palette.darkVibrantSwatch?.rgb
                ?: palette.lightMutedSwatch?.rgb
                ?: palette.darkMutedSwatch?.rgb
            
            color?.let { intColor ->
                // Convert to hex string with alpha removed
                String.format("#%06X", 0xFFFFFF and intColor)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Could not extract color from ${appInfo.packageName}: ${e.message}")
            null
        }
    }

    /**
     * Extract dominant color asynchronously (recommended for production use).
     * This method should be called from a background thread.
     *
     * @param appInfo Application info containing the icon
     * @return Hex color string or null if extraction fails
     */
    suspend fun extractDominantColorAsync(appInfo: ApplicationInfo): String? = withContext(Dispatchers.Default) {
        extractDominantColor(appInfo)
    }

    /**
     * Get app size
     */
    private fun getAppSize(appInfo: ApplicationInfo): Long {
        return try {
            val sourceDir = appInfo.sourceDir
            java.io.File(sourceDir).length()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Format size for display
     */
    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    /**
     * Format date for display
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Refresh apps list
     */
    suspend fun refresh() {
        _allApps.value = emptyList()
        appCache.clear()
        loadAllApps(_sugarMunchApps.value.map { it.toAppEntry() })
    }

    // ========== PLUGIN METHODS ==========

    /**
     * Install a plugin and update app list
     */
    suspend fun installPlugin(pluginId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val plugin = pluginStore.getPluginDetails(pluginId)
                ?: return@withContext Result.failure(Exception("Plugin not found: $pluginId"))

            // Install via PluginStore
            val installResult = pluginStore.installPlugin(pluginId)

            if (installResult.isSuccess) {
                // Update app list to reflect new installation
                refresh()
                Result.success(Unit)
            } else {
                Result.failure(installResult.exceptionOrNull() ?: Exception("Install failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uninstall a plugin
     */
    suspend fun uninstallPlugin(pluginId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = pluginStore.uninstallPlugin(pluginId)
            if (result.isSuccess) {
                refresh()
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get plugin install status
     */
    fun getPluginInstallStatus(pluginId: String): com.sugarmunch.app.plugin.store.InstallStatus {
        return pluginStore.getInstallStatus(pluginId)
    }

    /**
     * Check if a plugin is installed
     */
    fun isPluginInstalled(pluginId: String): Boolean {
        return pluginStore.isPluginInstalled(pluginId)
    }

    /**
     * Get all installed plugins as UnifiedAppInfo
     */
    suspend fun getInstalledPluginsAsUnifiedInfo(): List<UnifiedAppInfo> = withContext(Dispatchers.IO) {
        val installedPluginIds = pluginStore.installedPlugins.value
        installedPluginIds.mapNotNull { pluginId ->
            pluginStore.getPluginDetails(pluginId)?.let { plugin ->
                UnifiedAppInfo(
                    id = plugin.id,
                    name = plugin.name,
                    packageName = plugin.id,
                    description = plugin.description,
                    icon = null,
                    iconUrl = plugin.iconUrl,
                    category = plugin.category.name,
                    isSugarMunchApp = true,
                    isSystemApp = false,
                    isInstalled = true,
                    versionName = plugin.version,
                    isPlugin = true,
                    pluginRating = plugin.rating,
                    pluginCategory = plugin.category,
                    rating = plugin.rating,
                    ratingCount = plugin.ratingCount
                )
            }
        }
    }
}
