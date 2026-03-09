package com.sugarmunch.gallery.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.sugarmunch.gallery.R
import com.sugarmunch.gallery.SugarGalleryApplication
import com.sugarmunch.gallery.SugarTheme
import com.sugarmunch.gallery.databinding.ActivityMainSugarBinding
import com.sugarmunch.gallery.ui.fragments.MediaGridFragment
import com.sugarmunch.gallery.ui.fragments.SettingsSugarFragment
import com.sugarmunch.gallery.ui.fragments.ThemePickerFragment
import kotlinx.coroutines.launch

/**
 * SugarGallery Main Activity
 * Candy-themed photo and video gallery
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainSugarBinding
    private lateinit var application: SugarGalleryApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        application = SugarGalleryApplication.instance
        
        // Apply sugar theme before setting content view
        applyCurrentSugarTheme()
        
        // Setup window for edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityMainSugarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupBottomNavigation()
        loadInitialFragment()
        setupFloatingActionButton()
    }

    override fun onResume() {
        super.onResume()
        // Re-apply theme in case it changed
        applyCurrentSugarTheme()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "SugarGallery"
        supportActionBar?.subtitle = "🍬 Candy-Themed Gallery"
        
        binding.toolbar.setNavigationOnClickListener {
            openThemePicker()
        }
        
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_settings -> {
                    openSettings()
                    true
                }
                R.id.menu_theme_picker -> {
                    openThemePicker()
                    true
                }
                R.id.menu_recycle_bin -> {
                    openRecycleBin()
                    true
                }
                R.id.menu_hidden_folders -> {
                    openHiddenFolders()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_photos -> {
                    showMediaFragment(MediaFilter.PHOTOS)
                    true
                }
                R.id.nav_videos -> {
                    showMediaFragment(MediaFilter.VIDEOS)
                    true
                }
                R.id.nav_all -> {
                    showMediaFragment(MediaFilter.ALL)
                    true
                }
                R.id.nav_favorites -> {
                    showFavoritesFragment()
                    true
                }
                R.id.nav_folders -> {
                    showFoldersFragment()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadInitialFragment() {
        if (savedInstanceState == null) {
            showMediaFragment(MediaFilter.ALL)
        }
    }

    private fun setupFloatingActionButton() {
        binding.fab.setOnClickListener { view ->
            showCandyAnimation()
            // In a real implementation, this would open camera or import photos
        }
    }

    private fun showMediaFragment(filter: MediaFilter) {
        val fragment = MediaGridFragment.newInstance(filter)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showFavoritesFragment() {
        val fragment = MediaGridFragment.newInstance(MediaFilter.FAVORITES)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showFoldersFragment() {
        val fragment = FolderBrowserFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun openSettings() {
        val fragment = SettingsSugarFragment()
        fragment.show(supportFragmentManager, "settings")
    }

    private fun openThemePicker() {
        val fragment = ThemePickerFragment()
        fragment.show(supportFragmentManager, "theme_picker")
    }

    private fun openRecycleBin() {
        val intent = Intent(this, RecycleBinActivity::class.java)
        startActivity(intent)
    }

    private fun openHiddenFolders() {
        val intent = Intent(this, HiddenFoldersActivity::class.java)
        startActivity(intent)
    }

    private fun showCandyAnimation() {
        // Show candy burst animation
        lifecycleScope.launch {
            // In a real implementation, this would trigger particle effects
        }
    }

    /**
     * Apply the current sugar theme to the activity
     */
    private fun applyCurrentSugarTheme() {
        val theme = application.themeRepository.selectedTheme.value 
            ?: SugarTheme.getById(application.currentSugarThemeId)
            ?: SugarTheme.ALL_THEMES.first()
        
        applySugarTheme(theme)
    }

    /**
     * Apply a specific sugar theme
     */
    fun applySugarTheme(theme: SugarTheme) {
        // Get colors from resources
        val primaryColor = getColorOrThrow(theme.primaryColor)
        val accentColor = getColorOrThrow(theme.accentColor)
        val backgroundColor = getColorOrThrow(theme.backgroundColor)
        val surfaceColor = getColorOrThrow(theme.surfaceColor)
        
        // Apply to window
        window.statusBarColor = primaryColor
        window.navigationBarColor = surfaceColor
        
        // Apply to toolbar
        binding.toolbar.setBackgroundColor(primaryColor)
        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.setSubtitleTextColor(Color.WHITE.copy(alpha = 0.8f))
        
        // Apply to bottom navigation
        binding.bottomNavigation.setBackgroundColor(surfaceColor)
        binding.bottomNavigation.itemActiveColor = accentColor
        
        // Apply to FAB
        binding.fab.setBackgroundColor(accentColor)
        
        // Apply to background
        binding.root.setBackgroundColor(backgroundColor)
        
        // Update theme in application
        application.setSugarTheme(theme.id)
    }

    private fun getColorOrThrow(colorRes: Int): Int {
        return try {
            getColor(colorRes)
        } catch (e: Exception) {
            Color.parseColor("#FF69B4") // Fallback to hot pink
        }
    }

    /**
     * Refresh the current theme
     */
    fun refreshTheme() {
        applyCurrentSugarTheme()
    }
}

/**
 * Media filter types
 */
enum class MediaFilter {
    ALL,
    PHOTOS,
    VIDEOS,
    FAVORITES,
    HIDDEN
}
