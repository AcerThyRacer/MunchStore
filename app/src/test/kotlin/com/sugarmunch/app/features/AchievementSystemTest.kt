package com.sugarmunch.app.features

import com.google.common.truth.Truth.assertThat
import com.sugarmunch.app.features.model.AchievementCategory
import com.sugarmunch.app.features.model.AchievementRarity
import org.junit.Test

/**
 * Unit tests for AchievementSystem
 */
class AchievementSystemTest {

    @Test
    fun `ALL_ACHIEVEMENTS should contain 50 achievements`() {
        // Then
        assertThat(AchievementSystem.ALL_ACHIEVEMENTS).hasSize(50)
    }

    @Test
    fun `getById should return correct achievement`() {
        // When
        val achievement = AchievementSystem.getById("first_bite")

        // Then
        assertThat(achievement).isNotNull()
        assertThat(achievement?.id).isEqualTo("first_bite")
        assertThat(achievement?.name).isEqualTo("First Bite")
    }

    @Test
    fun `getById should return null for unknown id`() {
        // When
        val achievement = AchievementSystem.getById("nonexistent_id")

        // Then
        assertThat(achievement).isNull()
    }

    @Test
    fun `getByCategory should return achievements for INSTALLER category`() {
        // When
        val achievements = AchievementSystem.getByCategory(AchievementCategory.INSTALLER)

        // Then
        assertThat(achievements).isNotEmpty()
        assertThat(achievements.all { it.category == AchievementCategory.INSTALLER }).isTrue()
    }

    @Test
    fun `getByCategory should return achievements for CUSTOMIZER category`() {
        // When
        val achievements = AchievementSystem.getByCategory(AchievementCategory.CUSTOMIZER)

        // Then
        assertThat(achievements).isNotEmpty()
        assertThat(achievements.all { it.category == AchievementCategory.CUSTOMIZER }).isTrue()
    }

    @Test
    fun `getByCategory should return achievements for SOCIAL category`() {
        // When
        val achievements = AchievementSystem.getByCategory(AchievementCategory.SOCIAL)

        // Then
        assertThat(achievements).isNotEmpty()
        assertThat(achievements.all { it.category == AchievementCategory.SOCIAL }).isTrue()
    }

    @Test
    fun `getByRarity should return COMMON achievements`() {
        // When
        val achievements = AchievementSystem.getByRarity(AchievementRarity.COMMON)

        // Then
        assertThat(achievements).isNotEmpty()
        assertThat(achievements.all { it.rarity == AchievementRarity.COMMON }).isTrue()
    }

    @Test
    fun `getByRarity should return LEGENDARY achievements`() {
        // When
        val achievements = AchievementSystem.getByRarity(AchievementRarity.LEGENDARY)

        // Then
        assertThat(achievements).isNotEmpty()
        assertThat(achievements.all { it.rarity == AchievementRarity.LEGENDARY }).isTrue()
    }

    @Test
    fun `getHiddenAchievements should return only hidden achievements`() {
        // When
        val hiddenAchievements = AchievementSystem.getHiddenAchievements()

        // Then
        assertThat(hiddenAchievements).isNotEmpty()
        assertThat(hiddenAchievements.all { it.hidden }).isTrue()
    }

    @Test
    fun `getVisibleAchievements should return only visible achievements`() {
        // When
        val visibleAchievements = AchievementSystem.getVisibleAchievements()

        // Then
        assertThat(visibleAchievements).isNotEmpty()
        assertThat(visibleAchievements.all { !it.hidden }).isTrue()
    }

    @Test
    fun `all achievements should have unique ids`() {
        // Given
        val ids = AchievementSystem.ALL_ACHIEVEMENTS.map { it.id }

        // Then
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun `all achievements should have non-empty fields`() {
        // When
        val achievements = AchievementSystem.ALL_ACHIEVEMENTS

        // Then
        achievements.forEach { achievement ->
            assertThat(achievement.id).isNotEmpty()
            assertThat(achievement.name).isNotEmpty()
            assertThat(achievement.description).isNotEmpty()
            assertThat(achievement.icon).isNotEmpty()
        }
    }

    @Test
    fun `INSTALLER achievements should have correct order`() {
        // Given
        val installerAchievements = AchievementSystem.getByCategory(AchievementCategory.INSTALLER)
            .sortedBy { it.order }

        // Then
        assertThat(installerAchievements).isNotEmpty()
        // First installer achievement should be "first_bite"
        assertThat(installerAchievements.first().id).isEqualTo("first_bite")
    }

    @Test
    fun `achievement rarities should have correct point values`() {
        // Then
        assertThat(AchievementRarity.COMMON.points).isEqualTo(10)
        assertThat(AchievementRarity.UNCOMMON.points).isEqualTo(25)
        assertThat(AchievementRarity.RARE.points).isEqualTo(50)
        assertThat(AchievementRarity.EPIC.points).isEqualTo(100)
        assertThat(AchievementRarity.LEGENDARY.points).isEqualTo(250)
    }

    @Test
    fun `achievement categories should all be represented`() {
        // Given
        val allCategories = AchievementCategory.values().toSet()
        val representedCategories = AchievementSystem.ALL_ACHIEVEMENTS
            .map { it.category }
            .toSet()

        // Then
        assertThat(representedCategories).containsAllIn(allCategories)
    }

    @Test
    fun `hidden achievements should include Night Owl and Early Bird`() {
        // When
        val hiddenAchievements = AchievementSystem.getHiddenAchievements()

        // Then
        val ids = hiddenAchievements.map { it.id }
        assertThat(ids).contains("night_owl")
        assertThat(ids).contains("early_bird")
    }

    @Test
    fun `completionist achievement should require unlocking all`() {
        // When
        val completionist = AchievementSystem.getById("completionist")

        // Then
        assertThat(completionist).isNotNull()
        assertThat(completionist?.requirementType).isEqualTo("unlock_all")
        assertThat(completionist?.requirementValue).isEqualTo(50)
    }
}
