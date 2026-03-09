package com.sugarmunch.app.tiles

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.sugarmunch.app.MainActivity
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.CandyTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Quick Settings tile for cycling through favorite candy themes
 * 
 * Features:
 * - Tap to switch to next theme in favorites list
 * - Shows current theme name in subtitle
 * - Long press to open theme settings
 * - Visual feedback with theme-colored icon
 */
class ThemeSwitcherTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var themeManager: ThemeManager? = null
    private var collectionJob: Job? = null

    // List of favorite themes to cycle through
    private val favoriteThemeIds = listOf(
        "classic_candy",
        "bubblegum_pink", 
        "mint_chocolate",
        "caramel_dream",
        "berry_burst",
        "cotton_candy",
        "midnight_licorice"
    )

    companion object {
        const val EXTRA_OPEN_THEME_SETTINGS = "extra_open_theme_settings"
    }

    override fun onCreate() {
        super.onCreate()
        themeManager = ThemeManager.getInstance(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
        startCollectingThemeState()
    }

    override fun onStopListening() {
        super.onStopListening()
        collectionJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        collectionJob?.cancel()
        scope.cancel()
    }

    override fun onClick() {
        super.onClick()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isLocked) {
                unlockAndRun { cycleToNextTheme() }
                return
            }
        }
        
        cycleToNextTheme()
    }

    override fun onLongClick() {
        super.onLongClick()
        openThemeSettings()
    }

    /**
     * Cycle to the next theme in the favorites list
     */
    private fun cycleToNextTheme() {
        val manager = themeManager ?: return
        val allThemes = manager.allThemes.value
        
        if (allThemes.isEmpty()) {
            showToast("No themes available")
            return
        }
        
        // Get current theme
        val currentTheme = manager.currentTheme.value
        val currentIndex = favoriteThemeIds.indexOf(currentTheme.id)
        
        // Find next theme in favorites
        val nextIndex = if (currentIndex == -1 || currentIndex >= favoriteThemeIds.size - 1) {
            0
        } else {
            currentIndex + 1
        }
        
        // Get the next theme ID and find the theme
        val nextThemeId = favoriteThemeIds[nextIndex]
        val nextTheme = allThemes.find { it.id == nextThemeId }
            ?: allThemes.firstOrNull()
        
        nextTheme?.let { theme ->
            manager.setTheme(theme)
            showToast("🎨 ${theme.name}")
            updateTileForTheme(theme)
        }
    }

    /**
     * Open main app to theme settings
     */
    private fun openThemeSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_THEME_SETTINGS, true)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                3,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        } else {
            startActivity(intent)
        }
    }

    /**
     * Start collecting theme state changes
     */
    private fun startCollectingThemeState() {
        collectionJob?.cancel()
        collectionJob = scope.launch {
            themeManager?.currentTheme?.collectLatest { theme ->
                updateTileForTheme(theme)
            }
        }
    }

    /**
     * Update tile state
     */
    private fun updateTileState() {
        val manager = themeManager ?: return
        val currentTheme = manager.currentTheme.value
        updateTileForTheme(currentTheme)
    }

    /**
     * Update tile appearance based on current theme
     */
    private fun updateTileForTheme(theme: CandyTheme) {
        qsTile?.let { tile ->
            tile.state = Tile.STATE_ACTIVE
            tile.label = "🎨 Theme"
            tile.subtitle = theme.name
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.contentDescription = "Current theme: ${theme.name}. Tap to cycle themes."
                
                // Use different icons based on theme category
                val iconRes = when (theme.category) {
                    com.sugarmunch.app.theme.model.ThemeCategory.CLASSIC -> 
                        android.R.drawable.ic_menu_preferences
                    com.sugarmunch.app.theme.model.ThemeCategory.DARK -> 
                        android.R.drawable.ic_menu_night_mode
                    com.sugarmunch.app.theme.model.ThemeCategory.NEON -> 
                        android.R.drawable.ic_menu_gallery
                    com.sugarmunch.app.theme.model.ThemeCategory.SEASONAL -> 
                        android.R.drawable.ic_menu_month
                    com.sugarmunch.app.theme.model.ThemeCategory.SPECIAL -> 
                        android.R.drawable.ic_menu_star
                    else -> android.R.drawable.ic_menu_preferences
                }
                
                tile.icon = Icon.createWithResource(this, iconRes)
            }
            
            tile.updateTile()
        }
    }

    /**
     * Show a toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
