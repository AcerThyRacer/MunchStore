package com.sugarmunch.app.data.repository

import com.sugarmunch.app.data.AppEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced Search & Filter System for SugarMunch Catalog
 * 
 * Features:
 * - Full-text search across name, description, package name
 * - Category filtering
 * - Rating filtering
 * - Size filtering
 * - Sort options (name, rating, size, date)
 * - Recent searches history
 * - Search suggestions
 * - Filter presets
 */
@Singleton
class SearchRepository @Inject constructor() {

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: Flow<String> = _searchQuery.asStateFlow()

    private val _activeFilters = MutableStateFlow(SearchFilters())
    val activeFilters: Flow<SearchFilters> = _activeFilters.asStateFlow()

    private val _sortBy = MutableStateFlow(SortOption.RELEVANCE)
    val sortBy: Flow<SortOption> = _sortBy.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<RecentSearch>>(emptyList())
    val recentSearches: Flow<List<RecentSearch>> = _recentSearches.asStateFlow()

    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: Flow<List<String>> = _searchSuggestions.asStateFlow()

    // Filter presets
    val filterPresets = listOf(
        FilterPreset("All Apps", SearchFilters()),
        FilterPreset("Highly Rated", SearchFilters(minRating = 4.0f)),
        FilterPreset("Small Apps", SearchFilters(maxSizeMB = 50)),
        FilterPreset("Video & Music", SearchFilters(categories = setOf("Video & Music"))),
        FilterPreset("Games", SearchFilters(categories = setOf("Games"))),
        FilterPreset("Productivity", SearchFilters(categories = setOf("Productivity")))
    )

    /**
     * Update search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            addRecentSearch(query)
            updateSuggestions(query)
        }
    }

    /**
     * Update active filters
     */
    fun setFilters(filters: SearchFilters) {
        _activeFilters.value = filters
    }

    /**
     * Update sort option
     */
    fun setSortOption(sortOption: SortOption) {
        _sortBy.value = sortOption
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        _activeFilters.value = SearchFilters()
    }

    /**
     * Apply filter preset
     */
    fun applyPreset(preset: FilterPreset) {
        _activeFilters.value = preset.filters
    }

    /**
     * Search and filter apps
     */
    fun searchAndFilter(
        apps: List<AppEntry>,
        query: String,
        filters: SearchFilters,
        sortOption: SortOption
    ): List<AppEntry> {
        var results = apps

        // Apply text search
        if (query.isNotBlank()) {
            results = applyTextSearch(results, query)
        }

        // Apply filters
        results = applyFilters(results, filters)

        // Apply sorting
        results = applySorting(results, sortOption, query)

        return results
    }

    /**
     * Apply text search across multiple fields
     */
    private fun applyTextSearch(apps: List<AppEntry>, query: String): List<AppEntry> {
        val lowercaseQuery = query.lowercase()
        
        return apps.filter { app ->
            app.name.lowercase().contains(lowercaseQuery) ||
            app.description.lowercase().contains(lowercaseQuery) ||
            app.packageName.lowercase().contains(lowercaseQuery) ||
            app.category?.lowercase()?.contains(lowercaseQuery) == true
        }
    }

    /**
     * Apply active filters
     */
    private fun applyFilters(apps: List<AppEntry>, filters: SearchFilters): List<AppEntry> {
        return apps.filter { app ->
            // Category filter
            if (filters.categories.isNotEmpty()) {
                val appCategory = app.category ?: ""
                if (!filters.categories.any { cat -> 
                        appCategory.contains(cat, ignoreCase = true) 
                    }) {
                    return@filter false
                }
            }

            // Rating filter
            if (filters.minRating > 0) {
                val appRating = app.rating ?: 0f
                if (appRating < filters.minRating) {
                    return@filter false
                }
            }

            // Size filter
            if (filters.maxSizeMB > 0) {
                val appSizeMB = (app.size ?: 0) / (1024 * 1024)
                if (appSizeMB > filters.maxSizeMB) {
                    return@filter false
                }
            }

            // Featured filter
            if (filters.featuredOnly && !app.featured) {
                return@filter false
            }

            // Free apps filter
            if (filters.freeOnly && (app.price ?: 0) > 0) {
                return@filter false
            }

            true
        }
    }

    /**
     * Apply sorting
     */
    private fun applySorting(
        apps: List<AppEntry>,
        sortOption: SortOption,
        query: String = ""
    ): List<AppEntry> {
        return when (sortOption) {
            SortOption.RELEVANCE -> {
                // Relevance based on query match quality
                if (query.isNotBlank()) {
                    apps.sortedByDescending { app ->
                        calculateRelevance(app, query)
                    }
                } else {
                    apps
                }
            }
            SortOption.NAME_A_Z -> apps.sortedBy { it.name.lowercase() }
            SortOption.NAME_Z_A -> apps.sortedByDescending { it.name.lowercase() }
            SortOption.RATING_HIGH -> apps.sortedByDescending { it.rating ?: 0f }
            SortOption.RATING_LOW -> apps.sortedBy { it.rating ?: 0f }
            SortOption.SIZE_SMALL -> apps.sortedBy { it.size ?: 0 }
            SortOption.SIZE_LARGE -> apps.sortedByDescending { it.size ?: 0 }
            SortOption.NEWEST -> apps.sortedByDescending { it.dateAdded ?: 0L }
            SortOption.POPULAR -> apps.sortedByDescending { it.downloadCount ?: 0 }
        }
    }

    /**
     * Calculate relevance score for an app
     */
    private fun calculateRelevance(app: AppEntry, query: String): Int {
        val lowercaseQuery = query.lowercase()
        var score = 0

        // Exact name match (highest priority)
        if (app.name.lowercase() == lowercaseQuery) {
            score += 100
        }

        // Name starts with query
        if (app.name.lowercase().startsWith(lowercaseQuery)) {
            score += 50
        }

        // Name contains query
        if (app.name.lowercase().contains(lowercaseQuery)) {
            score += 25
        }

        // Package name match
        if (app.packageName.lowercase().contains(lowercaseQuery)) {
            score += 15
        }

        // Description match
        if (app.description.lowercase().contains(lowercaseQuery)) {
            score += 10
        }

        // Boost featured apps
        if (app.featured) {
            score += 5
        }

        // Boost high-rated apps
        score += ((app.rating ?: 0f) * 2).toInt()

        return score
    }

    /**
     * Add search to recent searches
     */
    private fun addRecentSearch(query: String) {
        val recentSearch = RecentSearch(
            query = query,
            timestamp = System.currentTimeMillis()
        )
        
        _recentSearches.value = buildList {
            // Add new search at the beginning
            add(recentSearch)
            // Add existing searches (excluding duplicates)
            addAll(_recentSearches.value.filter { it.query != query })
            // Keep only last 10 searches
            take(10)
        }
    }

    /**
     * Clear recent searches
     */
    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    /**
     * Remove specific recent search
     */
    fun removeRecentSearch(query: String) {
        _recentSearches.value = _recentSearches.value.filter { it.query != query }
    }

    /**
     * Update search suggestions
     */
    private fun updateSuggestions(query: String) {
        if (query.length < 2) {
            _searchSuggestions.value = emptyList()
            return
        }

        // Generate suggestions from recent searches and popular apps
        val suggestions = buildList {
            // Add matching recent searches
            addAll(
                _recentSearches.value
                    .filter { it.query.lowercase().contains(query.lowercase()) }
                    .map { it.query }
            )

            // Add popular search suggestions (would come from server in production)
            val popularSearches = listOf(
                "YouTube", "Spotify", "Instagram", "TikTok", 
                "Telegram", "WhatsApp", "Discord", "Netflix"
            )
            addAll(
                popularSearches.filter { 
                    it.lowercase().contains(query.lowercase()) 
                }
            )
        }.distinct().take(5)

        _searchSuggestions.value = suggestions
    }

    /**
     * Get search statistics
     */
    fun getSearchStats(): SearchStatistics {
        val recentSearchesList = _recentSearches.value
        return SearchStatistics(
            totalSearches = recentSearchesList.size,
            uniqueSearches = recentSearchesList.map { it.query }.distinct().size,
            lastSearchTime = recentSearchesList.firstOrNull()?.timestamp
        )
    }
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

/**
 * Search filters configuration
 */
data class SearchFilters(
    val categories: Set<String> = emptySet(),
    val minRating: Float = 0f,
    val maxSizeMB: Int = 0,  // 0 = no limit
    val featuredOnly: Boolean = false,
    val freeOnly: Boolean = false
) {
    fun hasActiveFilters(): Boolean {
        return categories.isNotEmpty() || 
               minRating > 0 || 
               maxSizeMB > 0 || 
               featuredOnly || 
               freeOnly
    }

    fun getActiveFilterCount(): Int {
        var count = 0
        if (categories.isNotEmpty()) count++
        if (minRating > 0) count++
        if (maxSizeMB > 0) count++
        if (featuredOnly) count++
        if (freeOnly) count++
        return count
    }
}

/**
 * Sort options
 */
enum class SortOption(val displayName: String) {
    RELEVANCE("Relevance"),
    NAME_A_Z("Name (A-Z)"),
    NAME_Z_A("Name (Z-A)"),
    RATING_HIGH("Rating (High-Low)"),
    RATING_LOW("Rating (Low-High)"),
    SIZE_SMALL("Size (Small-Large)"),
    SIZE_LARGE("Size (Large-Small)"),
    NEWEST("Newest"),
    POPULAR("Most Popular")
}

/**
 * Recent search entry
 */
data class RecentSearch(
    val query: String,
    val timestamp: Long
) {
    fun getDisplayTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
            else -> android.text.format.DateFormat.format("MMM dd", timestamp).toString()
        }
    }
}

/**
 * Filter preset for quick filtering
 */
data class FilterPreset(
    val name: String,
    val filters: SearchFilters
)

/**
 * Search statistics
 */
data class SearchStatistics(
    val totalSearches: Int,
    val uniqueSearches: Int,
    val lastSearchTime: Long?
)

/**
 * Search result with metadata
 */
data class SearchResult(
    val apps: List<AppEntry>,
    val query: String,
    val filters: SearchFilters,
    val sortOption: SortOption,
    val totalResults: Int,
    val searchTimeMs: Long
) {
    val hasResults: Boolean get() = apps.isNotEmpty()
    val resultSummary: String get() {
        return when {
            totalResults == 0 -> "No results found"
            totalResults == 1 -> "1 result found"
            else -> "$totalResults results found"
        }
    }
}
