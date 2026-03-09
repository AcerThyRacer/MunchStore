package com.sugarmunch.tv.data

import android.content.Context
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.repository.SmartManifestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository optimized for TV interface
 * 
 * Features:
 * - Larger page sizes for TV browsing (fewer loads)
 * - TV-specific caching strategy
 * - Horizontal row-optimized data grouping
 * - Featured apps curation for TV
 */
class TvRepository(
    private val context: Context,
    private val baseRepository: SmartManifestRepository
) {
    private val tvCache = TvCache()

    companion object {
        // TV-optimized page sizes
        const val TV_PAGE_SIZE = 20 // Larger than mobile for row display
        const val TV_MAX_CACHED_PAGES = 10
        const val TV_CACHE_EXPIRY_MS = 10 * 60 * 1000 // 10 minutes
    }

    /**
     * Get apps optimized for TV display
     * Returns larger batches for smooth horizontal scrolling
     */
    suspend fun getAppsForTv(
        page: Int = 0,
        forceRefresh: Boolean = false
    ): Result<List<AppEntry>> = withContext(Dispatchers.IO) {
        // Check cache first
        if (!forceRefresh) {
            val cached = tvCache.getApps(page)
            if (cached != null) {
                return@withContext Result.success(cached)
            }
        }

        // Fetch from base repository
        val result = baseRepository.sync(forceNetwork = forceRefresh)
        
        result.onSuccess { apps ->
            // Paginate for TV
            val startIndex = page * TV_PAGE_SIZE
            val paginatedApps = apps.drop(startIndex).take(TV_PAGE_SIZE)
            tvCache.putApps(page, paginatedApps)
        }

        result
    }

    /**
     * Get featured apps specifically curated for TV
     * These are apps optimized for TV use (leanback, D-pad support, etc.)
     */
    suspend fun getFeaturedAppsForTv(): List<AppEntry> = withContext(Dispatchers.IO) {
        // Check cache first
        tvCache.getFeaturedApps()?.let { return@withContext it }

        // Get from base repository
        val featured = baseRepository.sync().getOrNull()
            ?.filter { it.featured == true }
            ?.sortedBy { it.sortOrder ?: Int.MAX_VALUE }
            ?.take(10) // Limit featured for TV
            ?: emptyList()

        tvCache.putFeaturedApps(featured)
        featured
    }

    /**
     * Get apps grouped by category for TV horizontal rows
     */
    suspend fun getAppsByCategoryForTv(): Map<String, List<AppEntry>> = withContext(Dispatchers.IO) {
        // Check cache
        tvCache.getCategoryApps()?.let { return@withContext it }

        val apps = baseRepository.sync().getOrNull() ?: emptyList()
        
        val grouped = apps
            .groupBy { it.category ?: "Other" }
            .mapValues { (_, appsInCategory) ->
                appsInCategory
                    .sortedByDescending { it.featured == true }
                    .take(TV_PAGE_SIZE) // Limit per row for TV
            }
            .toSortedMap() // Consistent ordering

        tvCache.putCategoryApps(grouped)
        grouped
    }

    /**
     * Search apps with TV-optimized results
     */
    suspend fun searchAppsForTv(query: String): List<AppEntry> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        // Search local cache first for instant results
        val cachedResults = tvCache.search(query)
        if (cachedResults.isNotEmpty()) {
            return@withContext cachedResults
        }

        // Search from repository
        baseRepository.searchApps(query)
    }

    /**
     * Get a single app by ID
     */
    suspend fun getAppByIdForTv(appId: String): AppEntry? = withContext(Dispatchers.IO) {
        // Check cache first
        tvCache.getApp(appId)?.let { return@withContext it }

        // Fetch from base repository
        baseRepository.getAppById(appId)
    }

    /**
     * Get related apps for detail screen
     */
    suspend fun getRelatedAppsForTv(
        appId: String,
        category: String?,
        limit: Int = 6
    ): List<AppEntry> = withContext(Dispatchers.IO) {
        val apps = baseRepository.sync().getOrNull() ?: emptyList()
        
        apps
            .filter { it.id != appId && it.category == category }
            .shuffled() // Randomize for variety
            .take(limit)
    }

    /**
     * Refresh all TV cache
     */
    suspend fun refreshForTv(): Result<Unit> = withContext(Dispatchers.IO) {
        tvCache.clear()
        baseRepository.forceRefresh().map { }
    }

    /**
     * Get popular apps for TV recommendations
     */
    suspend fun getPopularAppsForTv(limit: Int = 10): List<AppEntry> = withContext(Dispatchers.IO) {
        val apps = baseRepository.sync().getOrNull() ?: emptyList()
        // In real implementation, this would use download counts or ratings
        apps.shuffled().take(limit)
    }

    /**
     * Get new/recently added apps
     */
    suspend fun getNewAppsForTv(limit: Int = 10): List<AppEntry> = withContext(Dispatchers.IO) {
        val apps = baseRepository.sync().getOrNull() ?: emptyList()
        // In real implementation, this would use publish dates
        apps
            .filter { it.badge == "New" || it.badge == "Updated" }
            .take(limit)
    }
}

/**
 * TV-specific cache with larger capacity and longer retention
 */
private class TvCache {
    private val appsCache = mutableMapOf<Int, Pair<List<AppEntry>, Long>>()
    private var featuredAppsCache: Pair<List<AppEntry>, Long>? = null
    private var categoryAppsCache: Pair<Map<String, List<AppEntry>>, Long>? = null
    private val appIdCache = mutableMapOf<String, Pair<AppEntry, Long>>()

    companion object {
        const val CACHE_TTL_MS = 10 * 60 * 1000 // 10 minutes
    }

    fun getApps(page: Int): List<AppEntry>? {
        val cached = appsCache[page] ?: return null
        return if (isValid(cached.second)) cached.first else null
    }

    fun putApps(page: Int, apps: List<AppEntry>) {
        appsCache[page] = apps to System.currentTimeMillis()
        // Also index by ID for quick lookup
        apps.forEach { app ->
            appIdCache[app.id] = app to System.currentTimeMillis()
        }
    }

    fun getFeaturedApps(): List<AppEntry>? {
        val cached = featuredAppsCache ?: return null
        return if (isValid(cached.second)) cached.first else null
    }

    fun putFeaturedApps(apps: List<AppEntry>) {
        featuredAppsCache = apps to System.currentTimeMillis()
    }

    fun getCategoryApps(): Map<String, List<AppEntry>>? {
        val cached = categoryAppsCache ?: return null
        return if (isValid(cached.second)) cached.first else null
    }

    fun putCategoryApps(apps: Map<String, List<AppEntry>>) {
        categoryAppsCache = apps to System.currentTimeMillis()
        // Index all apps by ID
        apps.values.flatten().forEach { app ->
            appIdCache[app.id] = app to System.currentTimeMillis()
        }
    }

    fun getApp(appId: String): AppEntry? {
        val cached = appIdCache[appId] ?: return null
        return if (isValid(cached.second)) cached.first else null
    }

    fun search(query: String): List<AppEntry> {
        val lowercaseQuery = query.lowercase()
        return appIdCache.values
            .filter { (_, timestamp) -> isValid(timestamp) }
            .map { it.first }
            .filter { app ->
                app.name.lowercase().contains(lowercaseQuery) ||
                app.description.lowercase().contains(lowercaseQuery) ||
                app.category?.lowercase()?.contains(lowercaseQuery) == true
            }
    }

    fun clear() {
        appsCache.clear()
        featuredAppsCache = null
        categoryAppsCache = null
        appIdCache.clear()
    }

    private fun isValid(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp < CACHE_TTL_MS
    }
}
