package com.sugarmunch.app.theme.builder

import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.sugarmunch.app.theme.profile.ThemeTransportEnvelope
import com.sugarmunch.app.theme.profile.parseThemeImportCandidate
import com.sugarmunch.app.theme.profile.toBase64Payload
import com.sugarmunch.app.theme.profile.toCustomTheme
import com.sugarmunch.app.theme.profile.toThemeProfile
import kotlinx.serialization.Serializable

/**
 * Theme Builder State - Holds all customization options for building a custom theme.
 *
 * This data class serves as the source of truth for the theme builder UI, containing
 * all configurable properties including colors, gradient settings, particle effects,
 * and animation preferences.
 */
@Serializable
data class ThemeBuilderState(
    val primaryColor: Long = 0xFFFF69B4, // Hot pink
    val secondaryColor: Long = 0xFF9370DB, // Medium purple
    val backgroundColor: Long = 0xFF1A1A2E, // Dark blue
    val surfaceColor: Long = 0xFF16213E, // Navy
    val customColors: List<Long> = emptyList(),
    val gradientType: GradientType = GradientType.LINEAR,
    val gradientAngle: Float = 45f,
    val gradientColors: List<Long> = listOf(0xFFFF69B4, 0xFF9370DB),
    val enableParticles: Boolean = false,
    val particleType: ParticleType = ParticleType.CIRCLES,
    val particleDensity: Int = 20,
    val particleSpeed: Float = 1f,
    val enableAnimation: Boolean = true,
    val enableBlur: Boolean = false
) {
    // Color helpers
    fun primaryColorAsColor(): Color = Color(primaryColor)
    fun secondaryColorAsColor(): Color = Color(secondaryColor)
    fun backgroundColorAsColor(): Color = Color(backgroundColor)
    fun surfaceColorAsColor(): Color = Color(surfaceColor)
    fun gradientColorsAsColors(): List<Color> = gradientColors.map { Color(it) }
    fun customColorsAsColors(): List<Color> = customColors.map { Color(it) }

    /**
     * Convert this builder state to a [CustomTheme] instance.
     */
    fun toCustomTheme(): CustomTheme {
        return CustomTheme(
            name = "Custom Theme",
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            backgroundColor = backgroundColor,
            surfaceColor = surfaceColor,
            gradientType = gradientType,
            gradientAngle = gradientAngle,
            gradientColors = gradientColors,
            enableParticles = enableParticles,
            particleType = particleType,
            particleDensity = particleDensity,
            particleSpeed = particleSpeed,
            enableAnimation = enableAnimation,
            enableBlur = enableBlur
        )
    }

    companion object {
        /**
         * Create a [ThemeBuilderState] from an existing [CustomTheme].
         */
        fun fromCustomTheme(theme: CustomTheme): ThemeBuilderState {
            return ThemeBuilderState(
                primaryColor = theme.primaryColor,
                secondaryColor = theme.secondaryColor,
                backgroundColor = theme.backgroundColor,
                surfaceColor = theme.surfaceColor,
                gradientType = theme.gradientType,
                gradientAngle = theme.gradientAngle,
                gradientColors = theme.gradientColors,
                enableParticles = theme.enableParticles,
                particleType = theme.particleType,
                particleDensity = theme.particleDensity,
                particleSpeed = theme.particleSpeed,
                enableAnimation = theme.enableAnimation,
                enableBlur = theme.enableBlur
            )
        }
    }
}

/**
 * Custom Theme - Serializable theme data that can be exported and imported.
 *
 * This data class represents a complete theme configuration that can be persisted,
 * shared via export codes, and applied to the application.
 */
@Serializable
data class CustomTheme(
    val name: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val gradientType: GradientType = GradientType.LINEAR,
    val gradientAngle: Float = 45f,
    val gradientColors: List<Long> = emptyList(),
    val enableParticles: Boolean = false,
    val particleType: ParticleType = ParticleType.CIRCLES,
    val particleDensity: Int = 20,
    val particleSpeed: Float = 1f,
    val enableAnimation: Boolean = true,
    val enableBlur: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val version: String = "1.0"
) {
    /**
     * Convert theme to export code (base64 encoded JSON).
     */
    fun toExportCode(): String {
        return ThemeTransportEnvelope(profile = toThemeProfile()).toBase64Payload(Gson())
    }

    /**
     * Create theme from export code.
     */
    companion object {
        fun fromExportCode(code: String): CustomTheme? {
            return parseThemeImportCandidate(code, Gson())?.profile?.toCustomTheme()
        }
    }
}
