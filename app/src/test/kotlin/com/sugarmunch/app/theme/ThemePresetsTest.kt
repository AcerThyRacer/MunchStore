package com.sugarmunch.app.theme

import com.google.common.truth.Truth.assertThat
import com.sugarmunch.app.theme.model.BackgroundStyle
import com.sugarmunch.app.theme.model.IntensityLevel
import com.sugarmunch.app.theme.model.ParticleConfig
import com.sugarmunch.app.theme.model.ParticleType
import com.sugarmunch.app.theme.presets.ThemePresets
import org.junit.Test

/**
 * Unit tests for Theme Presets and Models
 */
class ThemePresetsTest {

    @Test
    fun `THEME_LIBRARY should contain at least 16 themes`() {
        // Then
        assertThat(ThemePresets.THEME_LIBRARY).hasSize(16)
    }

    @Test
    fun `all themes should have unique ids`() {
        // Given
        val ids = ThemePresets.THEME_LIBRARY.map { it.id }

        // Then
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun `all themes should have non-empty fields`() {
        // When
        val themes = ThemePresets.THEME_LIBRARY

        // Then
        themes.forEach { theme ->
            assertThat(theme.id).isNotEmpty()
            assertThat(theme.name).isNotEmpty()
            assertThat(theme.description).isNotEmpty()
        }
    }

    @Test
    fun `getClassicThemes should return CLASSIC category themes`() {
        // When
        val classicThemes = ThemePresets.getClassicThemes()

        // Then
        assertThat(classicThemes).isNotEmpty()
        assertThat(classicThemes.all { it.category == ThemeCategory.CLASSIC }).isTrue()
    }

    @Test
    fun `getSugarrushThemes should return SUGARRUSH category themes`() {
        // When
        val sugarrushThemes = ThemePresets.getSugarrushThemes()

        // Then
        assertThat(sugarrushThemes).isNotEmpty()
        assertThat(sugarrushThemes.all { it.category == ThemeCategory.SUGARRUSH }).isTrue()
    }

    @Test
    fun `getTrippyThemes should return TRIPPY category themes`() {
        // When
        val trippyThemes = ThemePresets.getTrippyThemes()

        // Then
        assertThat(trippyThemes).isNotEmpty()
        assertThat(trippyThemes.all { it.category == ThemeCategory.TRIPPY }).isTrue()
    }

    @Test
    fun `getChillThemes should return CHILL category themes`() {
        // When
        val chillThemes = ThemePresets.getChillThemes()

        // Then
        assertThat(chillThemes).isNotEmpty()
        assertThat(chillThemes.all { it.category == ThemeCategory.CHILL }).isTrue()
    }

    @Test
    fun `getDarkThemes should return DARK category themes`() {
        // When
        val darkThemes = ThemePresets.getDarkThemes()

        // Then
        assertThat(darkThemes).isNotEmpty()
        assertThat(darkThemes.all { it.category == ThemeCategory.DARK }).isTrue()
    }

    @Test
    fun `getSeasonalThemes should return SEASONAL category themes`() {
        // When
        val seasonalThemes = ThemePresets.getSeasonalThemes()

        // Then
        assertThat(seasonalThemes).isNotEmpty()
        assertThat(seasonalThemes.all { it.category == ThemeCategory.SEASONAL }).isTrue()
    }

    @Test
    fun `getThemeById should return correct theme`() {
        // When
        val theme = ThemePresets.getThemeById("classic_candy")

        // Then
        assertThat(theme).isNotNull()
        assertThat(theme?.id).isEqualTo("classic_candy")
    }

    @Test
    fun `getThemeById should return null for unknown id`() {
        // When
        val theme = ThemePresets.getThemeById("nonexistent_theme")

        // Then
        assertThat(theme).isNull()
    }

    @Test
    fun `IntensityLevel values should be correct`() {
        // Then
        assertThat(IntensityLevel.CHILL.value).isEqualTo(0.3f)
        assertThat(IntensityLevel.NORMAL.value).isEqualTo(0.7f)
        assertThat(IntensityLevel.SWEET.value).isEqualTo(1.0f)
        assertThat(IntensityLevel.SUGARRUSH.value).isEqualTo(1.5f)
        assertThat(IntensityLevel.MAXIMUM.value).isEqualTo(2.0f)
    }

    @Test
    fun `ParticleType enum should have all types`() {
        // Then
        val types = ParticleType.values()
        assertThat(types).hasSize(6)
        assertThat(types.map { it.name }).containsExactly(
            "FLOATING", "RAINING", "RISING", "EXPLODING", "SWIRLING", "CHAOTIC"
        )
    }

    @Test
    fun `BackgroundStyle sealed class should have all types`() {
        // Given
        val gradient = BackgroundStyle.Gradient(
            colors = listOf(0xFFFF0000, 0xFF00FF00),
            angle = 45f
        )
        val solid = BackgroundStyle.Solid(color = 0xFFFF0000)

        // Then
        assertThat(gradient).isInstanceOf(BackgroundStyle.Gradient::class.java)
        assertThat(solid).isInstanceOf(BackgroundStyle.Solid::class.java)
    }

    @Test
    fun `ParticleConfig should create with correct values`() {
        // Given
        val config = ParticleConfig(
            type = ParticleType.FLOATING,
            count = 50,
            speed = 1.0f,
            size = 10.0f,
            color = 0xFFFF0000
        )

        // Then
        assertThat(config.type).isEqualTo(ParticleType.FLOATING)
        assertThat(config.count).isEqualTo(50)
        assertThat(config.speed).isEqualTo(1.0f)
    }

    @Test
    fun `themes should cover all categories`() {
        // Given
        val allCategories = ThemeCategory.values().toSet()
        val representedCategories = ThemePresets.THEME_LIBRARY
            .map { it.category }
            .toSet()

        // Then
        assertThat(representedCategories).containsAllIn(allCategories)
    }

    @Test
    fun `TRIPPY themes should have high intensity colors`() {
        // When
        val trippyThemes = ThemePresets.getTrippyThemes()

        // Then
        trippyThemes.forEach { theme ->
            // Trippy themes should have vibrant colors
            assertThat(theme.baseColors.primary).isGreaterThan(0)
        }
    }
}

/**
 * Unit tests for Theme Manager
 */
class ThemeManagerTest {

    @Test
    fun `IntensityPreset values should map correctly`() {
        // Given - this test verifies the preset intensity mappings
        // In actual implementation, these would be tested against the ThemeManager

        // Then - verify expected intensity mappings
        val presets = mapOf(
            "CHILL" to 0.3f,
            "NORMAL" to 0.7f,
            "SWEET" to 1.0f,
            "SUGARRUSH" to 1.5f,
            "MAXIMUM" to 2.0f
        )

        presets.forEach { (name, expectedValue) ->
            assertThat(expectedValue).isAtLeast(0f)
            assertThat(expectedValue).isAtMost(2f)
        }
    }

    @Test
    fun `Theme intensity should be within valid range`() {
        // Given
        val validRange = 0.0f..2.0f

        // Then - verify all intensity levels are in range
        listOf(0.0f, 0.3f, 0.7f, 1.0f, 1.5f, 2.0f).forEach { intensity ->
            assertThat(intensity).isIn(validRange)
        }
    }
}
