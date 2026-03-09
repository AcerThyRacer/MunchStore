package com.sugarmunch.app.analytics

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Analytics Manager
 */
class AnalyticsManagerTest {

    @Test
    fun `UserStats should calculate average session time correctly`() {
        // Given
        val stats = UserStats(
            totalSessions = 10,
            totalTimeSpent = 600000, // 10 minutes in ms
            totalInstalls = 5,
            themesTried = 3,
            effectsUsed = 4,
            achievementsUnlocked = 2,
            shopPurchases = 1
        )

        // Then - Average should be 1 minute (60000ms) per session
        assertThat(stats.averageSessionMinutes).isEqualTo(1)
    }

    @Test
    fun `UserStats should calculate total hours correctly`() {
        // Given
        val stats = UserStats(
            totalSessions = 1,
            totalTimeSpent = 3600000, // 1 hour in ms
            totalInstalls = 0,
            themesTried = 0,
            effectsUsed = 0,
            achievementsUnlocked = 0,
            shopPurchases = 0
        )

        // Then
        assertThat(stats.totalTimeHours).isEqualTo(1)
    }

    @Test
    fun `UserStats should handle zero sessions`() {
        // Given
        val stats = UserStats(
            totalSessions = 0,
            totalTimeSpent = 0,
            totalInstalls = 0,
            themesTried = 0,
            effectsUsed = 0,
            achievementsUnlocked = 0,
            shopPurchases = 0
        )

        // Then - Should not crash, return 0
        assertThat(stats.averageSessionMinutes).isEqualTo(0)
    }

    @Test
    fun `InstallEntry should have correct data`() {
        // Given
        val entry = InstallEntry(
            date = System.currentTimeMillis(),
            appId = "com.test.app",
            appName = "Test App",
            category = "Video & Music",
            sizeBytes = 50000000
        )

        // Then
        assertThat(entry.appId).isEqualTo("com.test.app")
        assertThat(entry.appName).isEqualTo("Test App")
        assertThat(entry.category).isEqualTo("Video & Music")
        assertThat(entry.sizeBytes).isEqualTo(50000000)
    }

    @Test
    fun `EngagementTrend enum should have all values`() {
        // Then
        val trends = EngagementTrend.values()
        assertThat(trends).hasSize(3)
        assertThat(trends.map { it.name }).containsExactly(
            "RISING", "STABLE", "DECLINING"
        )
    }

    @Test
    fun `WeeklyInsights should have correct data`() {
        // Given
        val insights = WeeklyInsights(
            mostInstalledCategory = "Video & Music",
            averageDailyInstalls = 2.5f,
            favoriteFeature = "Themes",
            engagementTrend = EngagementTrend.RISING
        )

        // Then
        assertThat(insights.mostInstalledCategory).isEqualTo("Video & Music")
        assertThat(insights.averageDailyInstalls).isEqualTo(2.5f)
        assertThat(insights.favoriteFeature).isEqualTo("Themes")
        assertThat(insights.engagementTrend).isEqualTo(EngagementTrend.RISING)
    }

    @Test
    fun `UserStats with realistic values should be valid`() {
        // Given
        val stats = UserStats(
            totalSessions = 100,
            totalTimeSpent = 7200000, // 2 hours
            totalInstalls = 25,
            themesTried = 8,
            effectsUsed = 12,
            achievementsUnlocked = 15,
            shopPurchases = 3
        )

        // Then
        assertThat(stats.totalSessions).isEqualTo(100)
        assertThat(stats.totalInstalls).isEqualTo(25)
        assertThat(stats.themesTried).isEqualTo(8)
        assertThat(stats.effectsUsed).isEqualTo(12)
        assertThat(stats.achievementsUnlocked).isEqualTo(15)
        assertThat(stats.shopPurchases).isEqualTo(3)
        assertThat(stats.totalTimeHours).isEqualTo(2)
    }
}
