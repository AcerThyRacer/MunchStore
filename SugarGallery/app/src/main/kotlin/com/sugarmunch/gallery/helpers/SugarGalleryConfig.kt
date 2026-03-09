package com.sugarmunch.gallery.helpers

import android.content.Context
import android.content.SharedPreferences
import com.sugarmunch.gallery.SugarGalleryApplication

/**
 * SugarGallery Configuration Helper
 * Manages all user preferences and settings
 */
class SugarGalleryConfig(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // ═════════════════════════════════════════════════════════════════
    // THEME SETTINGS
    // ═════════════════════════════════════════════════════════════════

    fun getSugarTheme(): String = prefs.getString(KEY_SUGAR_THEME, "cotton_candy") ?: "cotton_candy"

    fun saveSugarTheme(themeId: String) {
        prefs.edit().putString(KEY_SUGAR_THEME, themeId).apply()
    }

    fun isCandyModeEnabled(): Boolean = prefs.getBoolean(KEY_CANDY_MODE, true)

    fun saveCandyModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CANDY_MODE, enabled).apply()
    }

    fun getSugarIntensity(): Float = prefs.getFloat(KEY_SUGAR_INTENSITY, 1.0f)

    fun saveSugarIntensity(intensity: Float) {
        prefs.edit().putFloat(KEY_SUGAR_INTENSITY, intensity.coerceIn(0f, 2f)).apply()
    }

    // ═════════════════════════════════════════════════════════════════
    // DISPLAY SETTINGS
    // ═════════════════════════════════════════════════════════════════

    fun getColumnCount(): Int = prefs.getInt(KEY_COLUMN_COUNT, 3)

    fun saveColumnCount(count: Int) {
        prefs.edit().putInt(KEY_COLUMN_COUNT, count.coerceIn(2, 6)).apply()
    }

    fun showMediaDetails(): Boolean = prefs.getBoolean(KEY_SHOW_MEDIA_DETAILS, true)

    fun saveShowMediaDetails(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_MEDIA_DETAILS, show).apply()
    }

    fun getThumbnailQuality(): Int = prefs.getInt(KEY_THUMBNAIL_QUALITY, 80)

    fun saveThumbnailQuality(quality: Int) {
        prefs.edit().putInt(KEY_THUMBNAIL_QUALITY, quality.coerceIn(50, 100)).apply()
    }

    // ═════════════════════════════════════════════════════════════════
    // ANIMATION SETTINGS
    // ═════════════════════════════════════════════════════════════════

    fun enableAnimations(): Boolean = prefs.getBoolean(KEY_ENABLE_ANIMATIONS, true)

    fun saveEnableAnimations(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLE_ANIMATIONS, enabled).apply()
    }

    fun getAnimationSpeed(): Float = prefs.getFloat(KEY_ANIMATION_SPEED, 1.0f)

    fun saveAnimationSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_ANIMATION_SPEED, speed.coerceIn(0.5f, 2.0f)).apply()
    }

    fun enableParticleEffects(): Boolean = prefs.getBoolean(KEY_PARTICLE_EFFECTS, true)

    fun saveParticleEffects(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PARTICLE_EFFECTS, enabled).apply()
    }

    // ═════════════════════════════════════════════════════════════════
    // PRIVACY SETTINGS
    // ═════════════════════════════════════════════════════════════════

    fun isAppLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK, false)

    fun saveAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply()
    }

    fun getLockType(): String = prefs.getString(KEY_LOCK_TYPE, "none") ?: "none"

    fun saveLockType(type: String) {
        prefs.edit().putString(KEY_LOCK_TYPE, type).apply()
    }

    fun getHiddenFolders(): Set<String> = prefs.getStringSet(KEY_HIDDEN_FOLDERS, emptySet()) ?: emptySet()

    fun saveHiddenFolders(folders: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN_FOLDERS, folders).apply()
    }

    fun addHiddenFolder(folderPath: String) {
        val current = getHiddenFolders().toMutableSet()
        current.add(folderPath)
        saveHiddenFolders(current)
    }

    fun removeHiddenFolder(folderPath: String) {
        val current = getHiddenFolders().toMutableSet()
        current.remove(folderPath)
        saveHiddenFolders(current)
    }

    // ═════════════════════════════════════════════════════════════════
    // RECYCLE BIN SETTINGS
    // ═════════════════════════════════════════════════════════════════

    fun enableRecycleBin(): Boolean = prefs.getBoolean(KEY_RECYCLE_BIN, true)

    fun saveEnableRecycleBin(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_RECYCLE_BIN, enabled).apply()
    }

    fun getRecycleBinDays(): Int = prefs.getInt(KEY_RECYCLE_BIN_DAYS, 30)

    fun saveRecycleBinDays(days: Int) {
        prefs.edit().putInt(KEY_RECYCLE_BIN_DAYS, days.coerceIn(1, 90)).apply()
    }

    // ═════════════════════════════════════════════════════════════════
    // EDITOR SETTINGS
    // ═════════════════════════════════════════════════════════════════

    fun getDefaultFilter(): String = prefs.getString(KEY_DEFAULT_FILTER, "none") ?: "none"

    fun saveDefaultFilter(filter: String) {
        prefs.edit().putString(KEY_DEFAULT_FILTER, filter).apply()
    }

    fun autoSaveEdits(): Boolean = prefs.getBoolean(KEY_AUTO_SAVE_EDITS, false)

    fun saveAutoSaveEdits(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE_EDITS, enabled).apply()
    }

    fun getEditQuality(): Int = prefs.getInt(KEY_EDIT_QUALITY, 90)

    fun saveEditQuality(quality: Int) {
        prefs.edit().putInt(KEY_EDIT_QUALITY, quality.coerceIn(50, 100)).apply()
    }

    // ═════════════════════════════════════════════════════════════════
    // MISC SETTINGS
    // ═════════════════════════════════════════════════════════════════

    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

    fun saveFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun getLastVersionCode(): Int = prefs.getInt(KEY_LAST_VERSION_CODE, 0)

    fun saveLastVersionCode(code: Int) {
        prefs.edit().putInt(KEY_LAST_VERSION_CODE, code).apply()
    }

    fun getFontSize(): Float = prefs.getFloat(KEY_FONT_SIZE, 1.0f)

    fun saveFontSize(size: Float) {
        prefs.edit().putFloat(KEY_FONT_SIZE, size.coerceIn(0.8f, 1.5f)).apply()
    }

    fun enableHapticFeedback(): Boolean = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)

    fun saveHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    // ═════════════════════════════════════════════════════════════════
    // CLEAR ALL SETTINGS
    // ═════════════════════════════════════════════════════════════════

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "sugargallery_prefs"

        // Theme keys
        private const val KEY_SUGAR_THEME = "sugar_theme"
        private const val KEY_CANDY_MODE = "candy_mode"
        private const val KEY_SUGAR_INTENSITY = "sugar_intensity"

        // Display keys
        private const val KEY_COLUMN_COUNT = "column_count"
        private const val KEY_SHOW_MEDIA_DETAILS = "show_media_details"
        private const val KEY_THUMBNAIL_QUALITY = "thumbnail_quality"

        // Animation keys
        private const val KEY_ENABLE_ANIMATIONS = "enable_animations"
        private const val KEY_ANIMATION_SPEED = "animation_speed"
        private const val KEY_PARTICLE_EFFECTS = "particle_effects"

        // Privacy keys
        private const val KEY_APP_LOCK = "app_lock"
        private const val KEY_LOCK_TYPE = "lock_type"
        private const val KEY_HIDDEN_FOLDERS = "hidden_folders"

        // Recycle bin keys
        private const val KEY_RECYCLE_BIN = "recycle_bin"
        private const val KEY_RECYCLE_BIN_DAYS = "recycle_bin_days"

        // Editor keys
        private const val KEY_DEFAULT_FILTER = "default_filter"
        private const val KEY_AUTO_SAVE_EDITS = "auto_save_edits"
        private const val KEY_EDIT_QUALITY = "edit_quality"

        // Misc keys
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_LAST_VERSION_CODE = "last_version_code"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
    }
}
