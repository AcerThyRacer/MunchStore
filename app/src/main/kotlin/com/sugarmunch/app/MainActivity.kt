package com.sugarmunch.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.sugarmunch.app.data.LocalPreferencesRepository
import com.sugarmunch.app.data.PreferencesRepository
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.ThemeCategory
import com.sugarmunch.app.theme.profile.parseThemeImportCandidate
import com.sugarmunch.app.ui.navigation.SugarMunchNavHost
import com.sugarmunch.app.ui.screens.SplashScreen
import com.sugarmunch.app.ui.theme.SugarMunchTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Activity - Entry point for SugarMunch app
 * Uses Hilt for dependency injection
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository
    
    @Inject
    lateinit var biometricThemeManager: com.sugarmunch.app.ai.BiometricThemeManager
    
    @Inject
    lateinit var acousticAmbientAdapter: com.sugarmunch.app.ai.AcousticAmbientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        
        // Start Contextual AI Adapters
        biometricThemeManager.start()
        acousticAmbientAdapter.start()
        
        // GlobalExceptionHandler is set as default in SugarMunchApplication; do not override here
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalPreferencesRepository provides preferencesRepository) {
                SugarMunchTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        SugarMunchApp()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Track session start for analytics
        lifecycleScope.launch {
            // Analytics tracking would go here
        }
    }

    override fun onPause() {
        super.onPause()
        // Track session end for analytics
        lifecycleScope.launch {
            // Analytics tracking would go here
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    /** Handle sugarmunch://theme?id=xxx from LollipopLauncher/Candy Store for cross-app theme sync. */
    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        val scheme = data.scheme ?: return
        val host = data.host ?: return
        if (scheme != "sugarmunch" || host != "theme") return
        val themeManager = ThemeManager.getInstance(this)
        data.getQueryParameter("id")?.let { themeId ->
            themeManager.setThemeById(themeId)
            return
        }
        data.getQueryParameter("payload")?.let { payload ->
            parseThemeImportCandidate(payload, Gson())?.profile?.let { profile ->
                themeManager.saveThemeProfile(
                    profile.copy(
                        category = ThemeCategory.CUSTOM,
                        metadata = profile.metadata.copy(builtIn = false)
                    ),
                    activate = true
                )
            }
        }
    }
}

@Composable
fun SugarMunchApp() {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(
            onSplashComplete = { showSplash = false }
        )
    } else {
        SugarMunchNavHost()
    }
}
