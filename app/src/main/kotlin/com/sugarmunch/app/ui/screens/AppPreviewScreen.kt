package com.sugarmunch.app.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.data.ManifestRepository
import com.sugarmunch.app.phaseone.PhaseOneUtilities
import com.sugarmunch.app.theme.engine.ThemeRuntimeSnapshot
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.presets.ThemePresets
import com.sugarmunch.app.theme.profile.toThemeProfile
import com.sugarmunch.app.ui.typography.toDynamicTypographyConfig
import kotlinx.coroutines.flow.first

/**
 * WebView-based "Try before install" preview for apps with previewUrl.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPreviewScreen(
    appId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { ManifestRepository(context) }
    var previewUrl by remember { mutableStateOf<String?>(null) }
    var appName by remember { mutableStateOf(appId) }
    LaunchedEffect(appId) {
        val spec = PhaseOneUtilities.specFor(appId)
        repo.fetchApps()
        val apps = repo.getCachedApps()?.let { it.first() } ?: emptyList()
        val app = apps.find { it.id == appId }
        appName = app?.name ?: spec?.fallbackApp?.name ?: appId
        previewUrl = app?.previewUrl ?: spec?.fallbackApp?.previewUrl
    }
    AppPreviewContent(appId = appId, previewUrl = previewUrl, appName = appName, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPreviewContent(
    appId: String,
    previewUrl: String?,
    appName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val fallbackTheme = remember { ThemePresets.getDefault() }
    val runtime by themeManager.observeThemeRuntime(appId).collectAsState(
        initial = ThemeRuntimeSnapshot(
            profile = fallbackTheme.toThemeProfile(),
            theme = fallbackTheme,
            colors = fallbackTheme.getColorsForIntensity(1f),
            typography = fallbackTheme.toThemeProfile().typography.toDynamicTypographyConfig(context, emptyList()),
            themeIntensity = 1f,
            backgroundIntensity = 1f,
            particleIntensity = 1f,
            animationIntensity = 1f
        )
    )
    val colors = runtime.colors
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview: $appName", color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) {
        if (previewUrl.isNullOrBlank()) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No preview available", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        loadUrl(previewUrl!!)
                    }
                }
            )
        }
    }
}
