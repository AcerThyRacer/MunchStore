package com.sugarmunch.app.data.repository

import com.google.common.truth.Truth.assertThat
import com.sugarmunch.app.data.AppEntry
import org.junit.Test

/**
 * Unit tests for SearchRepository
 */
class SearchRepositoryTest {

    private val searchRepository = SearchRepository()

    private val sampleApps = listOf(
        AppEntry(
            id = "app1",
            name = "YouTube",
            packageName = "com.google.android.youtube",
            description = "Watch and share videos",
            category = "Video & Music",
            rating = 4.5f,
            size = 50 * 1024 * 1024,
            featured = true,
            downloadCount = 1000000
        ),
        AppEntry(
            id = "app2",
            name = "Spotify",
            packageName = "com.spotify.music",
            description = "Music streaming",
            category = "Video & Music",
            rating = 4.3f,
            size = 30 * 1024 * 1024,
            featured = true,
            downloadCount = 800000
        ),
        AppEntry(
            id = "app3",
            name = "Instagram",
            packageName = "com.instagram.android",
            description = "Photo and video sharing",
            category = "Social",
            rating = 4.2f,
            size = 40 * 1024 * 1024,
            featured = false,
            downloadCount = 900000
        ),
        AppEntry(
            id = "app4",
            name = "Telegram",
            packageName = "org.telegram.messenger",
            description = "Fast messaging app",
            category = "Social",
            rating = 4.6f,
            size = 25 * 1024 * 1024,
            featured = false,
            downloadCount = 500000
        )
    )

    @Test
    fun `test search by name returns matching apps`() {
        // Given
        val query = "YouTube"

        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = query,
            filters = SearchFilters(),
            sortOption = SortOption.RELEVANCE
        )

        // Then
        assertThat(results).hasSize(1)
        assertThat(results.first().name).isEqualTo("YouTube")
    }

    @Test
    fun `test search by description returns matching apps`() {
        // Given
        val query = "music"

        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = query,
            filters = SearchFilters(),
            sortOption = SortOption.RELEVANCE
        )

        // Then
        assertThat(results).hasSize(2) // YouTube and Spotify
    }

    @Test
    fun `test search by package name returns matching apps`() {
        // Given
        val query = "telegram"

        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = query,
            filters = SearchFilters(),
            sortOption = SortOption.RELEVANCE
        )

        // Then
        assertThat(results).hasSize(1)
        assertThat(results.first().packageName).contains("telegram")
    }

    @Test
    fun `test filter by category returns matching apps`() {
        // Given
        val filters = SearchFilters(categories = setOf("Social"))

        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = "",
            filters = filters,
            sortOption = SortOption.RELEVANCE
        )

        // Then
        assertThat(results).hasSize(2) // Instagram and Telegram
        assertThat(results.all { it.category == "Social" }).isTrue()
    }

    @Test
    fun `test filter by minimum rating returns matching apps`() {
        // Given
        val filters = SearchFilters(minRating = 4.5f)

        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = "",
            filters = filters,
            sortOption = SortOption.RELEVANCE
        )

        // Then
        assertThat(results).hasSize(2) // YouTube and Telegram
        assertThat(results.all { (it.rating ?: 0f) >= 4.5f }).isTrue()
    }

    @Test
    fun `test filter by featured only returns featured apps`() {
        // Given
        val filters = SearchFilters(featuredOnly = true)

        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = "",
            filters = filters,
            sortOption = SortOption.RELEVANCE
        )

        // Then
        assertThat(results).hasSize(2) // YouTube and Spotify
        assertThat(results.all { it.featured }).isTrue()
    }

    @Test
    fun `test sort by name A-Z returns apps in alphabetical order`() {
        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = "",
            filters = SearchFilters(),
            sortOption = SortOption.NAME_A_Z
        )

        // Then
        assertThat(results.map { it.name }).containsExactly(
            "Instagram", "Spotify", "Telegram", "YouTube"
        ).inOrder()
    }

    @Test
    fun `test sort by name Z-A returns apps in reverse alphabetical order`() {
        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = "",
            filters = SearchFilters(),
            sortOption = SortOption.NAME_Z_A
        )

        // Then
        assertThat(results.map { it.name }).containsExactly(
            "YouTube", "Telegram", "Spotify", "Instagram"
        ).inOrder()
    }

    @Test
    fun `test sort by rating high-low returns apps in descending rating order`() {
        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = "",
            filters = SearchFilters(),
            sortOption = SortOption.RATING_HIGH
        )

        // Then
        assertThat(results.map { it.rating }).containsExactly(
            4.6f, 4.5f, 4.3f, 4.2f
        ).inOrder()
    }

    @Test
    fun `test sort by popularity returns apps in descending download order`() {
        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = "",
            filters = SearchFilters(),
            sortOption = SortOption.POPULAR
        )

        // Then
        assertThat(results.map { it.downloadCount }).containsExactly(
            1000000, 900000, 800000, 500000
        ).inOrder()
    }

    @Test
    fun `test combined search and filter returns correct results`() {
        // Given
        val query = "music"
        val filters = SearchFilters(minRating = 4.3f)

        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = query,
            filters = filters,
            sortOption = SortOption.RELEVANCE
        )

        // Then
        assertThat(results).hasSize(1)
        assertThat(results.first().name).isEqualTo("Spotify")
    }

    @Test
    fun `test search with no results returns empty list`() {
        // Given
        val query = "nonexistent_app_xyz"

        // When
        val results = searchRepository.searchAndFilter(
            apps = sampleApps,
            query = query,
            filters = SearchFilters(),
            sortOption = SortOption.RELEVANCE
        )

        // Then
        assertThat(results).isEmpty()
    }

    @Test
    fun `test SearchFilters hasActiveFilters returns correct values`() {
        // Given
        val emptyFilters = SearchFilters()
        val activeFilters = SearchFilters(categories = setOf("Games"), minRating = 4.0f)

        // Then
        assertThat(emptyFilters.hasActiveFilters()).isFalse()
        assertThat(activeFilters.hasActiveFilters()).isTrue()
    }

    @Test
    fun `test SearchFilters getActiveFilterCount returns correct count`() {
        // Given
        val filters = SearchFilters(
            categories = setOf("Games", "Social"),
            minRating = 4.0f,
            featuredOnly = true
        )

        // Then
        assertThat(filters.getActiveFilterCount()).isEqualTo(3)
    }

    @Test
    fun `test SortOption displayName values`() {
        // Then
        assertThat(SortOption.RELEVANCE.displayName).isEqualTo("Relevance")
        assertThat(SortOption.NAME_A_Z.displayName).isEqualTo("Name (A-Z)")
        assertThat(SortOption.RATING_HIGH.displayName).isEqualTo("Rating (High-Low)")
    }
}

/**
 * Unit tests for Search Filters
 */
class SearchFiltersTest {

    @Test
    fun `test empty filters has no active filters`() {
        // Given
        val filters = SearchFilters()

        // Then
        assertThat(filters.hasActiveFilters()).isFalse()
        assertThat(filters.getActiveFilterCount()).isEqualTo(0)
    }

    @Test
    fun `test filters with categories has active filters`() {
        // Given
        val filters = SearchFilters(categories = setOf("Games"))

        // Then
        assertThat(filters.hasActiveFilters()).isTrue()
        assertThat(filters.getActiveFilterCount()).isEqualTo(1)
    }

    @Test
    fun `test filters with multiple active filters returns correct count`() {
        // Given
        val filters = SearchFilters(
            categories = setOf("Games", "Social"),
            minRating = 4.0f,
            maxSizeMB = 100,
            featuredOnly = true,
            freeOnly = true
        )

        // Then
        assertThat(filters.hasActiveFilters()).isTrue()
        assertThat(filters.getActiveFilterCount()).isEqualTo(5)
    }

    @Test
    fun `test filter presets are correctly defined`() {
        // Given
        val repository = SearchRepository()
        val presets = repository.filterPresets

        // Then
        assertThat(presets).hasSize(6)
        assertThat(presets.map { it.name }).containsExactly(
            "All Apps", "Highly Rated", "Small Apps",
            "Video & Music", "Games", "Productivity"
        )
    }
}
