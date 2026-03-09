package com.sugarmunch.app.plugin.store

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.plugin.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

private val Context.pluginStoreDataStore: DataStore<Preferences> by preferencesDataStore(name = "sugarmunch_plugin_store")

/**
 * SugarMunch Plugin Store - In-app plugin browser and manager
 * 
 * Provides functionality to:
 * - Browse available plugins from remote repository
 * - Search and filter plugins by category
 * - Download and install plugins
 * - Rate and review plugins
 * - Manage plugin versions and updates
 * 
 * Similar to VS Code Marketplace or Minecraft CurseForge.
 */
class PluginStore private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Repository base URL
    private val repositoryUrl = "https://plugins.sugarmunch.app/api/v1"
    
    // Cache directory for downloads
    private val cacheDir by lazy {
        File(context.cacheDir, "plugin_store").apply { mkdirs() }
    }
    
    // Available plugins from repository
    private val _availablePlugins = MutableStateFlow<Map<String, StorePlugin>>(emptyMap())
    val availablePlugins: StateFlow<Map<String, StorePlugin>> = _availablePlugins.asStateFlow()
    
    // Categories
    private val _categories = MutableStateFlow<List<PluginCategory>>(PluginCategory.values().toList())
    val categories: StateFlow<List<PluginCategory>> = _categories.asStateFlow()
    
    // Featured plugins
    private val _featuredPlugins = MutableStateFlow<List<StorePlugin>>(emptyList())
    val featuredPlugins: StateFlow<List<StorePlugin>> = _featuredPlugins.asStateFlow()
    
    // Top rated plugins
    private val _topRatedPlugins = MutableStateFlow<List<StorePlugin>>(emptyList())
    val topRatedPlugins: StateFlow<List<StorePlugin>> = _topRatedPlugins.asStateFlow()
    
    // New plugins
    private val _newPlugins = MutableStateFlow<List<StorePlugin>>(emptyList())
    val newPlugins: StateFlow<List<StorePlugin>> = _newPlugins.asStateFlow()
    
    // User ratings cache
    private val userRatings = ConcurrentHashMap<String, Int>()
    
    // Download progress
    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    // Install status
    private val _installStatus = MutableStateFlow<Map<String, InstallStatus>>(emptyMap())
    val installStatus: StateFlow<Map<String, InstallStatus>> = _installStatus.asStateFlow()

    // Installed plugins (list of plugin IDs)
    val installedPlugins: StateFlow<List<String>> = _installStatus
        .map { statusMap ->
            statusMap.filter { it.value == InstallStatus.INSTALLED }.keys.toList()
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshPlugins()
    }
    
    // ========== BROWSING METHODS ==========
    
    /**
     * Search plugins by query string
     * @param query The search query
     * @param category Optional category filter
     * @return List of matching plugins
     */
    fun searchPlugins(
        query: String,
        category: PluginCategory? = null
    ): Flow<List<StorePlugin>> = flow {
        val filtered = _availablePlugins.value.values.filter { plugin ->
            val matchesQuery = plugin.name.contains(query, ignoreCase = true) ||
                    plugin.description.contains(query, ignoreCase = true) ||
                    plugin.author.contains(query, ignoreCase = true) ||
                    plugin.tags.any { it.contains(query, ignoreCase = true) }
            
            val matchesCategory = category == null || plugin.category == category
            
            matchesQuery && matchesCategory
        }.sortedByDescending { it.downloadCount }
        
        emit(filtered)
    }.flowOn(Dispatchers.Default)
    
    /**
     * Get plugins by category
     */
    fun getPluginsByCategory(category: PluginCategory): List<StorePlugin> {
        return _availablePlugins.value.values
            .filter { it.category == category }
            .sortedByDescending { it.rating }
    }
    
    /**
     * Get plugin details
     */
    fun getPluginDetails(pluginId: String): StorePlugin? {
        return _availablePlugins.value[pluginId]
    }
    
    /**
     * Get plugins by developer
     */
    fun getPluginsByDeveloper(developerId: String): List<StorePlugin> {
        return _availablePlugins.value.values
            .filter { it.authorId == developerId }
            .sortedByDescending { it.downloadCount }
    }
    
    /**
     * Get related plugins (similar to given plugin)
     */
    fun getRelatedPlugins(pluginId: String, limit: Int = 5): List<StorePlugin> {
        val plugin = _availablePlugins.value[pluginId] ?: return emptyList()
        
        return _availablePlugins.value.values
            .filter { it.id != pluginId }
            .map { 
                val tagScore = it.tags.intersect(plugin.tags).size
                val categoryScore = if (it.category == plugin.category) 2 else 0
                it to (tagScore + categoryScore)
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
    
    // ========== INSTALLATION METHODS ==========
    
    /**
     * Download and install a plugin
     * @param pluginId The plugin ID to install
     * @return Result containing the installed plugin file or error
     */
    suspend fun installPlugin(pluginId: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val plugin = _availablePlugins.value[pluginId]
                ?: return@withContext Result.failure(StoreException.NotFound("Plugin not found: $pluginId"))
            
            // Check if already installed
            if (isPluginInstalled(pluginId)) {
                return@withContext Result.failure(StoreException.AlreadyInstalled("Plugin already installed"))
            }
            
            // Update status
            _installStatus.update { it + (pluginId to InstallStatus.DOWNLOADING) }
            
            // Download plugin
            val downloadedFile = downloadPlugin(plugin, onProgress = { progress ->
                _downloadProgress.update { it + (pluginId to progress) }
            }).getOrThrow()
            
            // Update status
            _installStatus.update { it + (pluginId to InstallStatus.INSTALLING) }
            
            // Verify download
            if (!verifyDownload(downloadedFile, plugin)) {
                downloadedFile.delete()
                return@withContext Result.failure(StoreException.VerificationFailed("Download verification failed"))
            }
            
            // Install via PluginManager would go here
            // For now, just return the downloaded file
            
            // Update status
            _installStatus.update { it + (pluginId to InstallStatus.INSTALLED) }
            _downloadProgress.update { it - pluginId }
            
            // Track download
            trackDownload(pluginId)
            
            Result.success(downloadedFile)
        } catch (e: Exception) {
            _installStatus.update { it + (pluginId to InstallStatus.ERROR) }
            _downloadProgress.update { it - pluginId }
            Result.failure(StoreException.InstallFailed("Failed to install plugin: ${e.message}", e))
        }
    }
    
    /**
     * Update an installed plugin
     * @param pluginId The plugin ID to update
     * @return Result containing the updated plugin file or error
     */
    suspend fun updatePlugin(pluginId: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val plugin = _availablePlugins.value[pluginId]
                ?: return@withContext Result.failure(StoreException.NotFound("Plugin not found: $pluginId"))
            
            val installedVersion = getInstalledVersion(pluginId)
            if (installedVersion == null) {
                return@withContext Result.failure(StoreException.NotInstalled("Plugin is not installed"))
            }
            
            if (!plugin.isUpdateAvailable(installedVersion)) {
                return@withContext Result.failure(StoreException.NoUpdate("No update available"))
            }
            
            // Download and install new version
            installPlugin(pluginId)
        } catch (e: Exception) {
            Result.failure(StoreException.UpdateFailed("Failed to update plugin: ${e.message}", e))
        }
    }
    
    /**
     * Uninstall a plugin
     */
    suspend fun uninstallPlugin(pluginId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Implementation would call PluginManager to uninstall
            _installStatus.update { it - pluginId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(StoreException.UninstallFailed("Failed to uninstall plugin: ${e.message}", e))
        }
    }
    
    /**
     * Check for updates for all installed plugins
     */
    suspend fun checkForUpdates(): Map<String, StorePlugin> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, StorePlugin>()
        
        getInstalledPlugins().forEach { installedId ->
            _availablePlugins.value[installedId]?.let { available ->
                val installedVersion = getInstalledVersion(installedId)
                if (installedVersion != null && available.isUpdateAvailable(installedVersion)) {
                    updates[installedId] = available
                }
            }
        }
        
        updates
    }
    
    // ========== RATINGS AND REVIEWS ==========
    
    /**
     * Rate a plugin
     * @param pluginId The plugin ID
     * @param rating Rating from 1-5
     */
    suspend fun ratePlugin(pluginId: String, rating: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (rating !in 1..5) {
                return@withContext Result.failure(StoreException.InvalidRating("Rating must be 1-5"))
            }
            
            // Check if user has installed the plugin
            if (!isPluginInstalled(pluginId)) {
                return@withContext Result.failure(
                    StoreException.NotInstalled("You must install the plugin before rating")
                )
            }
            
            // Submit rating
            submitRatingToServer(pluginId, rating).getOrThrow()
            
            // Update local cache
            userRatings[pluginId] = rating
            context.pluginStoreDataStore.edit { prefs ->
                prefs[intPreferencesKey("rating_$pluginId")] = rating
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(StoreException.RatingFailed("Failed to submit rating: ${e.message}", e))
        }
    }
    
    /**
     * Get user's rating for a plugin
     */
    fun getUserRating(pluginId: String): Int? {
        return userRatings[pluginId]
    }
    
    /**
     * Get reviews for a plugin
     */
    suspend fun getPluginReviews(
        pluginId: String,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<List<PluginReview>> = withContext(Dispatchers.IO) {
        try {
            // Fetch from server
            val reviews = fetchReviewsFromServer(pluginId, page, pageSize)
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(StoreException.FetchFailed("Failed to fetch reviews: ${e.message}", e))
        }
    }
    
    /**
     * Submit a review
     */
    suspend fun submitReview(
        pluginId: String,
        rating: Int,
        text: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (text.length > 1000) {
                return@withContext Result.failure(StoreException.InvalidReview("Review too long (max 1000 chars)"))
            }
            
            submitReviewToServer(pluginId, rating, text).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(StoreException.ReviewFailed("Failed to submit review: ${e.message}", e))
        }
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Refresh plugin list from repository
     */
    fun refreshPlugins() {
        scope.launch {
            try {
                val plugins = fetchPluginsFromServer()
                _availablePlugins.value = plugins.associateBy { it.id }
                
                // Update featured, top rated, new
                _featuredPlugins.value = plugins.filter { it.isFeatured }
                _topRatedPlugins.value = plugins.sortedByDescending { it.rating }.take(10)
                _newPlugins.value = plugins.sortedByDescending { it.publishDate }.take(10)
                
            } catch (e: Exception) {
                Log.e("PluginStore", "Failed to refresh plugin list", e)
            }
        }
    }
    
    /**
     * Check if a plugin is installed
     */
    fun isPluginInstalled(pluginId: String): Boolean {
        // Implementation would check PluginManager
        return _installStatus.value[pluginId] == InstallStatus.INSTALLED
    }
    
    /**
     * Get installed plugins
     */
    fun getInstalledPlugins(): List<String> {
        return _installStatus.value.filter { it.value == InstallStatus.INSTALLED }.keys.toList()
    }
    
    /**
     * Get installed version of a plugin
     */
    fun getInstalledVersion(pluginId: String): String? {
        // Implementation would check PluginManager
        return null
    }
    
    /**
     * Get download progress for a plugin
     */
    fun getDownloadProgress(pluginId: String): Float {
        return _downloadProgress.value[pluginId] ?: 0f
    }
    
    /**
     * Get install status for a plugin
     */
    fun getInstallStatus(pluginId: String): InstallStatus {
        return _installStatus.value[pluginId] ?: InstallStatus.NOT_INSTALLED
    }
    
    // ========== PRIVATE METHODS ==========
    
    private suspend fun downloadPlugin(
        plugin: StorePlugin,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val targetFile = File(cacheDir, "${plugin.id}_${plugin.version}.smpkg")
            
            // Download with progress tracking
            val url = URL(plugin.downloadUrl)
            val connection = url.openConnection()
            val totalSize = connection.contentLength
            
            connection.getInputStream().use { input ->
                targetFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var downloaded = 0
                    var read: Int
                    
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        
                        if (totalSize > 0) {
                            onProgress(downloaded.toFloat() / totalSize)
                        }
                    }
                }
            }
            
            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun verifyDownload(file: File, plugin: StorePlugin): Boolean {
        // Verify file checksum
        return try {
            val calculatedHash = calculateFileHash(file)
            calculatedHash.equals(plugin.checksum, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun calculateFileHash(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buffer = ByteArray(8192)
            var read: Int
            while (stream.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    private suspend fun fetchPluginsFromServer(): List<StorePlugin> {
        // Mock implementation - in production, this would call the API
        return MockData.getMockPlugins()
    }
    
    private suspend fun submitRatingToServer(pluginId: String, rating: Int): Result<Unit> {
        // Mock implementation
        return Result.success(Unit)
    }
    
    private suspend fun submitReviewToServer(
        pluginId: String,
        rating: Int,
        text: String
    ): Result<Unit> {
        // Mock implementation
        return Result.success(Unit)
    }
    
    private suspend fun fetchReviewsFromServer(
        pluginId: String,
        page: Int,
        pageSize: Int
    ): List<PluginReview> {
        // Mock implementation
        return emptyList()
    }
    
    private fun trackDownload(pluginId: String) {
        scope.launch {
            // Send analytics to server
        }
    }
    
    companion object {
        @Volatile
        private var instance: PluginStore? = null
        
        fun getInstance(context: Context): PluginStore {
            return instance ?: synchronized(this) {
                instance ?: PluginStore(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Plugin install status
 */
enum class InstallStatus {
    NOT_INSTALLED,
    DOWNLOADING,
    INSTALLING,
    INSTALLED,
    UPDATING,
    ERROR
}

/**
 * Store exceptions
 */
sealed class StoreException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NotFound(message: String) : StoreException(message)
    class AlreadyInstalled(message: String) : StoreException(message)
    class NotInstalled(message: String) : StoreException(message)
    class VerificationFailed(message: String) : StoreException(message)
    class InstallFailed(message: String, cause: Throwable? = null) : StoreException(message, cause)
    class UpdateFailed(message: String, cause: Throwable? = null) : StoreException(message, cause)
    class UninstallFailed(message: String, cause: Throwable? = null) : StoreException(message, cause)
    class NoUpdate(message: String) : StoreException(message)
    class InvalidRating(message: String) : StoreException(message)
    class InvalidReview(message: String) : StoreException(message)
    class RatingFailed(message: String, cause: Throwable? = null) : StoreException(message, cause)
    class ReviewFailed(message: String, cause: Throwable? = null) : StoreException(message, cause)
    class FetchFailed(message: String, cause: Throwable? = null) : StoreException(message, cause)
}

/**
 * Mock data for development
 */
object MockData {
    fun getMockPlugins(): List<StorePlugin> = listOf(
        StorePlugin(
            id = "com.cyberdev.neon",
            name = "Neon Dreams",
            description = "Cyberpunk-inspired neon effects and themes",
            version = "1.2.0",
            author = "CyberDev",
            authorId = "cyberdev",
            category = PluginCategory.THEMES,
            tags = listOf("cyberpunk", "neon", "dark"),
            rating = 4.8f,
            ratingCount = 1247,
            downloadCount = 15243,
            iconUrl = "https://plugins.sugarmunch.app/icons/neon.png",
            screenshots = listOf(
                "https://plugins.sugarmunch.app/screens/neon1.jpg",
                "https://plugins.sugarmunch.app/screens/neon2.jpg"
            ),
            downloadUrl = "https://plugins.sugarmunch.app/download/neon_1.2.0.smpkg",
            checksum = "abc123...",
            size = 2457600,
            minApiLevel = 26,
            publishDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
            changelog = listOf(
                ChangelogEntry("1.2.0", "Added new pulse effects", System.currentTimeMillis()),
                ChangelogEntry("1.1.0", "Improved performance", System.currentTimeMillis() - 15L * 24 * 60 * 60 * 1000),
                ChangelogEntry("1.0.0", "Initial release", System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
            ),
            isFeatured = true
        ),
        StorePlugin(
            id = "com.nature.green",
            name = "Nature Pack",
            description = "Organic themes and calming particle effects",
            version = "2.0.1",
            author = "NatureThemes",
            authorId = "naturethemes",
            category = PluginCategory.EFFECTS,
            tags = listOf("nature", "calm", "green", "organic"),
            rating = 4.5f,
            ratingCount = 892,
            downloadCount = 8934,
            iconUrl = "https://plugins.sugarmunch.app/icons/nature.png",
            screenshots = listOf(),
            downloadUrl = "https://plugins.sugarmunch.app/download/nature_2.0.1.smpkg",
            checksum = "def456...",
            size = 1835008,
            minApiLevel = 24,
            publishDate = System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000,
            changelog = listOf(),
            isFeatured = false
        ),
        StorePlugin(
            id = "com.retro.pixel",
            name = "Pixel Paradise",
            description = "8-bit retro effects and pixel art themes",
            version = "1.0.5",
            author = "RetroCoder",
            authorId = "retrocoder",
            category = PluginCategory.EFFECTS,
            tags = listOf("retro", "pixel", "8bit", "gaming"),
            rating = 4.7f,
            ratingCount = 567,
            downloadCount = 4521,
            iconUrl = "https://plugins.sugarmunch.app/icons/pixel.png",
            screenshots = listOf(),
            downloadUrl = "https://plugins.sugarmunch.app/download/pixel_1.0.5.smpkg",
            checksum = "ghi789...",
            size = 1048576,
            minApiLevel = 24,
            publishDate = System.currentTimeMillis() - 10L * 24 * 60 * 60 * 1000,
            changelog = listOf(),
            isFeatured = true
        ),
        StorePlugin(
            id = "com.anime.kawaii",
            name = "Kawaii Collection",
            description = "Cute anime-inspired themes and effects",
            version = "1.5.0",
            author = "AnimeStudio",
            authorId = "animestudio",
            category = PluginCategory.THEMES,
            tags = listOf("anime", "cute", "kawaii", "pastel"),
            rating = 4.6f,
            ratingCount = 2103,
            downloadCount = 22109,
            iconUrl = "https://plugins.sugarmunch.app/icons/kawaii.png",
            screenshots = listOf(),
            downloadUrl = "https://plugins.sugarmunch.app/download/kawaii_1.5.0.smpkg",
            checksum = "jkl012...",
            size = 3145728,
            minApiLevel = 26,
            publishDate = System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000,
            changelog = listOf(),
            isFeatured = true
        ),
        StorePlugin(
            id = "com.util.battery",
            name = "Battery Widget Pro",
            description = "Advanced battery monitoring with beautiful visualizations",
            version = "3.1.0",
            author = "UtilityDev",
            authorId = "utilitydev",
            category = PluginCategory.TOOLS,
            tags = listOf("utility", "battery", "widget", "stats"),
            rating = 4.3f,
            ratingCount = 432,
            downloadCount = 3456,
            iconUrl = "https://plugins.sugarmunch.app/icons/battery.png",
            screenshots = listOf(),
            downloadUrl = "https://plugins.sugarmunch.app/download/battery_3.1.0.smpkg",
            checksum = "mno345...",
            size = 524288,
            minApiLevel = 24,
            publishDate = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000,
            changelog = listOf(),
            isFeatured = false
        )
    )
}
