package com.sugarmunch.gallery

import android.app.Application
import android.content.Context
import com.sugarmunch.gallery.data.repositories.SugarThemeRepository
import com.sugarmunch.gallery.helpers.SugarGalleryConfig

/**
 * SugarGallery Application Class
 * Candy-themed photo and video gallery with extreme customization
 */
class SugarGalleryApplication : Application() {

    companion object {
        lateinit var instance: SugarGalleryApplication
            private set

        /**
         * Get the current sugar theme ID
         */
        var currentSugarThemeId: String = "cotton_candy"
            private set

        /**
         * Check if candy mode is enabled
         */
        var isCandyModeEnabled: Boolean = true
            private set

        /**
         * Sugar intensity level (0.0 - 2.0)
         */
        var sugarIntensity: Float = 1.0f
            private set
    }

    lateinit var config: SugarGalleryConfig
        private set

    lateinit var themeRepository: SugarThemeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        config = SugarGalleryConfig(this)
        themeRepository = SugarThemeRepository(this)
        
        // Load saved preferences
        loadPreferences()
    }

    private fun loadPreferences() {
        currentSugarThemeId = config.getSugarTheme()
        isCandyModeEnabled = config.isCandyModeEnabled()
        sugarIntensity = config.getSugarIntensity()
    }

    /**
     * Set the current sugar theme
     */
    fun setSugarTheme(themeId: String) {
        currentSugarThemeId = themeId
        config.saveSugarTheme(themeId)
    }

    /**
     * Toggle candy mode
     */
    fun toggleCandyMode(enabled: Boolean) {
        isCandyModeEnabled = enabled
        config.saveCandyModeEnabled(enabled)
    }

    /**
     * Set sugar intensity level
     */
    fun setSugarIntensity(intensity: Float) {
        sugarIntensity = intensity.coerceIn(0f, 2f)
        config.saveSugarIntensity(sugarIntensity)
    }

    /**
     * Get all available sugar themes
     */
    fun getAvailableSugarThemes(): List<SugarTheme> {
        return SugarTheme.ALL_THEMES
    }
}
