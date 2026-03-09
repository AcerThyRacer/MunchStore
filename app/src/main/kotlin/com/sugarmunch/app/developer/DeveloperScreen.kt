package com.sugarmunch.app.developer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager

/**
 * Developer studio: submit app for QA, view reports, generate assets.
 * Full automation (spin up emulator, play-through) requires backend; this is the in-app entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val colors = currentTheme.getColorsForIntensity(1f)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer studio", color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AnimatedThemeBackground()
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    "Submit for QA",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.primary
                )
                Text(
                    "Upload APK for automated play-through and UI/UX report. (Backend required.)",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Generate assets",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.primary
                )
                Text(
                    "AI-generated candy-themed store assets. (API key required.)",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
