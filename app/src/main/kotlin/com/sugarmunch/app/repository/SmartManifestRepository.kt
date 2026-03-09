package com.sugarmunch.app.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.AppsManifest
import com.sugarmunch.app.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * SmartManifestRepository - Offline-first repository with smart sync strategies
 * 
 * Features:
 * - Offline-first architecture (serves from cache when offline)
 * - Smart conflict resolution
 * - Incremental sync with ETag support
 * - Background refresh strategies
 * - Sync state management
 */
class SmartManifestRepository(
    private val context: Context,
    private val manifestUrl: String = DEFAULT_MANIFEST_URL
) {
    private val database = AppDatabase.getDatabase(context)
    private val appDao = database.appDao()
    private val syncStateDao = database.syncStateDao()
    private val cachedAppDao = database.cachedAppDao()
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    // Network state tracking
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    // ═════════════════════════════════════════════════════════════
    // CORE SYNC OPERATIONS
    // ═════════════════════════════════════════════════════════════

    /**
     * Smart sync operation - the main method for synchronizing manifest data.
     * Implements offline-first strategy with automatic fallback.
     * 
     * @param forceNetwork If true, bypasses cache and fetches from network
     * @param allowStale If true, returns cached data if network fails
     * @return Result containing list of apps or error
     */
    suspend fun sync(
        forceNetwork: Boolean = false,
        allowStale: Boolean = true
    ): Result<List<AppEntry>> = withContext(Dispatchers.IO) {
        try {
            // Check if we have network
            if (!isNetworkAvailable()) {
                _isOfflineMode.value = true
                return@withContext if (allowStale) {
                    getCachedAppsOrFail()
                } else {
                    Result.failure(IOException("No network connection"))
                }
            }

            _isOfflineMode.value = false

            // If not forcing network, check if cache is fresh enough
            if (!forceNetwork && isCacheFresh()) {
                return@withContext getCachedAppsOrFail()
            }

            // Perform network sync
            performNetworkSync()
        } catch (e: Exception) {
            if (allowStale) {
                getCachedAppsOrFail()
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Force a complete refresh from network, clearing local cache first.
     * Use this for manual refresh (e.g., pull-to-refresh).
     */
    suspend fun forceRefresh(): Result<List<AppEntry>> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext Result.failure(IOException("No network connection"))
        }

        // Clear old data
        appDao.deleteAllApps()
        
        // Perform fresh sync
        performNetworkSync()
    }

    /**
     * Perform incremental sync - only fetch changes since last sync
     */
    suspend fun incrementalSync(): Result<List<AppEntry>> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext getCachedAppsOrFail()
        }

        val syncState = syncStateDao.getSyncState()
        val etag = syncState?.etag

        try {
            val request = Request.Builder()
                .url(manifestUrl)
                .apply {
                    etag?.let { header("If-None-Match", it) }
                }
                .build()

            val response = client.newCall(request).execute()

            // 304 Not Modified - cache is still valid
            if (response.code == 304) {
                return@withContext getCachedAppsOrFail()
            }

            if (!response.isSuccessful) {
                return@withContext getCachedAppsOrFail()
            }

            // Parse and update
            val body = response.body?.string() ?: throw IOException("Empty response")
            val manifest = parseManifest(body)
            
            // Update cache
            updateLocalCache(manifest)
            
            // Update sync state with new ETag
            val newEtag = response.header("ETag")
            updateSyncState(etag = newEtag)

            Result.success(manifest.apps)
        } catch (e: Exception) {
            getCachedAppsOrFail()
        }
    }

    // ═════════════════════════════════════════════════════════════
    // DATA ACCESS METHODS
    // ═════════════════════════════════════════════════════════════

    /**
     * Get apps with automatic cache fallback - Flow version for reactive UI
     */
    fun getAppsWithCache(): Flow<List<AppEntry>> {
        return appDao.getAllApps()
            .map { entities ->
                entities.map { it.toAppEntry() }
            }
            .catch { e ->
                emit(emptyList())
            }
    }

    /**
     * Get apps by category with offline support
     */
    suspend fun getAppsByCategory(category: String): List<AppEntry> = withContext(Dispatchers.IO) {
        appDao.getAllApps()
            .firstOrNull()
            ?.filter { it.category == category }
            ?.map { it.toAppEntry() }
            ?: emptyList()
    }

    /**
     * Get featured apps
     */
    suspend fun getFeaturedApps(): List<AppEntry> = withContext(Dispatchers.IO) {
        appDao.getAllApps()
            .firstOrNull()
            ?.filter { it.featured == true }
            ?.sortedBy { it.sortOrder ?: Int.MAX_VALUE }
            ?.map { it.toAppEntry() }
            ?: emptyList()
    }

    /**
     * Search apps locally (works offline)
     */
    suspend fun searchApps(query: String): List<AppEntry> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        
        val lowercaseQuery = query.lowercase()
        appDao.getAllApps()
            .firstOrNull()
            ?.filter { entity ->
                entity.name.lowercase().contains(lowercaseQuery) ||
                entity.description.lowercase().contains(lowercaseQuery) ||
                entity.packageName.lowercase().contains(lowercaseQuery)
            }
            ?.map { it.toAppEntry() }
            ?: emptyList()
    }

    /**
     * Get a single app by ID
     */
    suspend fun getAppById(appId: String): AppEntry? = withContext(Dispatchers.IO) {
        appDao.getAppById(appId)?.toAppEntry()
    }

    /**
     * Check if we have any cached apps
     */
    suspend fun hasCachedApps(): Boolean = withContext(Dispatchers.IO) {
        appDao.getAppCount() > 0
    }

    /**
     * Get last successful sync time
     */
    suspend fun getLastSyncTime(): Long? = withContext(Dispatchers.IO) {
        syncStateDao.getSyncState()?.lastSuccessfulSync
    }

    // ═════════════════════════════════════════════════════════════
    // SYNC STATE MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    /**
     * Get current sync status
     */
    fun getSyncStatus(): Flow<SyncStatus> {
        return flow {
            while (true) {
                val state = syncStateDao.getSyncState()
                val hasCache = hasCachedApps()
                
                emit(
                    SyncStatus(
                        lastSuccessfulSync = state?.lastSuccessfulSync,
                        lastAttemptedSync = state?.lastAttemptedSync,
                        lastError = state?.lastSyncError,
                        isOfflineMode = _isOfflineMode.value,
                        hasCachedData = hasCache,
                        isSyncing = false // Could track actual sync state
                    )
                )
                kotlinx.coroutines.delay(5000) // Update every 5 seconds
            }
        }
    }

    /**
     * Clear all sync errors and reset state
     */
    suspend fun resetSyncState() = withContext(Dispatchers.IO) {
        syncStateDao.saveSyncState(
            SyncStateEntity(
                id = 1,
                lastSuccessfulSync = null,
                lastAttemptedSync = null,
                syncAttemptCount = 0,
                lastSyncError = null,
                pendingChangesCount = 0,
                isOfflineMode = false
            )
        )
    }

    // ═════════════════════════════════════════════════════════════
    // CONFLICT RESOLUTION
    // ═════════════════════════════════════════════════════════════

    /**
     * Smart conflict resolution strategy:
     * - For new apps: always add
     * - For existing apps: use server version (network wins)
     * - For local-only apps: preserve with conflict flag
     */
    private suspend fun resolveConflicts(
        serverApps: List<AppEntry>,
        localApps: List<AppEntity>
    ): List<AppEntity> {
        val serverMap = serverApps.associateBy { it.id }
        val localMap = localApps.associateBy { it.id }
        
        val result = mutableListOf<AppEntity>()
        
        // Process all server apps (server wins)
        serverApps.forEach { serverApp ->
            result.add(serverApp.toEntity())
        }
        
        // Preserve local-only apps (optional - could be removed if pure server-sync)
        localApps.forEach { localApp ->
            if (!serverMap.containsKey(localApp.id)) {
                // App removed from server - could mark as deprecated
                // For now, we keep it with a flag
                result.add(localApp.copy(badge = "Removed"))
            }
        }
        
        return result
    }

    // ═════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════

    private suspend fun performNetworkSync(): Result<List<AppEntry>> {
        return try {
            val request = Request.Builder()
                .url(manifestUrl)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }

            val body = response.body?.string() ?: throw IOException("Empty response")
            val manifest = parseManifest(body)

            // Update local cache
            updateLocalCache(manifest)

            // Update sync state
            updateSyncState(
                etag = response.header("ETag"),
                serverTimestamp = response.header("Last-Modified")?.let { parseHttpDate(it) }
            )

            Result.success(manifest.apps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getCachedAppsOrFail(): Result<List<AppEntry>> {
        val cached = appDao.getAllApps().firstOrNull()
        return if (!cached.isNullOrEmpty()) {
            Result.success(cached.map { it.toAppEntry() })
        } else {
            Result.failure(IOException("No cached data available"))
        }
    }

    private suspend fun updateLocalCache(manifest: AppsManifest) {
        // Convert to entities
        val entities = manifest.apps.map { it.toEntity() }
        
        // Get existing for conflict resolution
        val existing = appDao.getAllApps().firstOrNull() ?: emptyList()
        
        // Resolve conflicts
        val resolved = resolveConflicts(manifest.apps, existing)
        
        // Update database
        appDao.deleteAllApps()
        appDao.insertApps(resolved)
        
        // Also update cached apps metadata
        updateCachedAppMetadata(manifest.apps)
    }

    private suspend fun updateCachedAppMetadata(apps: List<AppEntry>) {
        apps.forEach { app ->
            val existing = cachedAppDao.getCachedApp(app.id)
            if (existing == null) {
                cachedAppDao.insertCachedApp(
                    CachedAppEntity(
                        appId = app.id,
                        name = app.name,
                        packageName = app.packageName,
                        description = app.description,
                        iconUrl = app.iconUrl,
                        downloadUrl = app.downloadUrl,
                        version = app.version,
                        source = app.source,
                        category = app.category,
                        accentColor = app.accentColor,
                        badge = app.badge,
                        featured = app.featured,
                        sortOrder = app.sortOrder
                    )
                )
            }
        }
    }

    private suspend fun updateSyncState(etag: String? = null, serverTimestamp: Long? = null) {
        val current = syncStateDao.getSyncState()
        syncStateDao.saveSyncState(
            SyncStateEntity(
                id = 1,
                lastSuccessfulSync = System.currentTimeMillis(),
                lastAttemptedSync = System.currentTimeMillis(),
                syncAttemptCount = 0,
                lastSyncError = null,
                etag = etag ?: current?.etag,
                serverTimestamp = serverTimestamp ?: current?.serverTimestamp
            )
        )
    }

    private fun isCacheFresh(): Boolean {
        val lastSync = runBlocking { syncStateDao.getSyncState()?.lastSuccessfulSync }
        lastSync ?: return false
        
        val age = System.currentTimeMillis() - lastSync
        return age < CACHE_FRESHNESS_THRESHOLD_MS
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun parseManifest(json: String): AppsManifest {
        return com.google.gson.Gson().fromJson(json, AppsManifest::class.java)
    }

    private fun parseHttpDate(dateString: String): Long? {
        return try {
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
                .parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }

    // ═════════════════════════════════════════════════════════════
    // COMPANION
    // ═════════════════════════════════════════════════════════════

    companion object {
        const val DEFAULT_MANIFEST_URL = "https://raw.githubusercontent.com/sugarmunch/SugarMunch/main/docs/sugarmunch-apps.json"
        
        private const val CONNECT_TIMEOUT_SECONDS = 15L
        private const val READ_TIMEOUT_SECONDS = 15L
        private const val CACHE_FRESHNESS_THRESHOLD_MS = 30L * 60 * 1000 // 30 minutes
    }
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

data class SyncStatus(
    val lastSuccessfulSync: Long?,
    val lastAttemptedSync: Long?,
    val lastError: String?,
    val isOfflineMode: Boolean,
    val hasCachedData: Boolean,
    val isSyncing: Boolean
) {
    val isStale: Boolean
        get() {
            if (lastSuccessfulSync == null) return true
            val age = System.currentTimeMillis() - lastSuccessfulSync
            return age > 30 * 60 * 1000 // 30 minutes
        }
    
    val timeSinceLastSync: String
        get() = lastSuccessfulSync?.let {
            val diff = System.currentTimeMillis() - it
            when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
                else -> "${diff / (24 * 60 * 60 * 1000)}d ago"
            }
        } ?: "Never"
}

// Extension functions
private fun AppEntry.toEntity() = AppEntity(
    id = id,
    name = name,
    packageName = packageName,
    description = description,
    iconUrl = iconUrl,
    downloadUrl = downloadUrl,
    version = version,
    source = source,
    category = category,
    accentColor = accentColor,
    badge = badge,
    featured = featured,
    sortOrder = sortOrder
)

private fun AppEntity.toAppEntry() = AppEntry(
    id = id,
    name = name,
    packageName = packageName,
    description = description,
    iconUrl = iconUrl,
    downloadUrl = downloadUrl,
    version = version,
    source = source,
    category = category,
    accentColor = accentColor,
    badge = badge,
    featured = featured,
    sortOrder = sortOrder
)
