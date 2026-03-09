package com.lollipop.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lollipop.launcher.ui.theme.LollipopTheme

/**
 * Main launcher activity - the heart of LollipopLauncher
 * Provides a clean, candy-themed home screen experience
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LollipopTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LauncherScreen()
                }
            }
        }
    }
}

@Composable
fun LauncherScreen() {
    var selectedApp by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🍭 Lollipop Launcher",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Your candy-themed home screen",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // TODO: Add app grid, search bar, widgets
            // TODO: Integrate with SugarMunch effects system
            // TODO: Add customization options
            
            OutlinedButton(onClick = { /* TODO: Open app drawer */ }) {
                Text("Open App Drawer")
            }
        }
    }
}
